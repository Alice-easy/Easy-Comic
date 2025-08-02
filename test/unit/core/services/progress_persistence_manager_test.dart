import 'dart:async';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:drift/drift.dart' as drift;
import 'package:easy_comic/core/services/progress_persistence_manager_impl.dart';
import 'package:easy_comic/data/drift_db.dart';
import 'package:easy_comic/domain/entities/comic_progress.dart';
import 'package:easy_comic/domain/services/progress_persistence_manager.dart';

// Mock classes
class MockAppDatabase extends Mock implements AppDatabase {}
class MockComicProgressDao extends Mock implements ComicProgressDao {}

void main() {
  group('ProgressPersistenceManager', () {
    late ProgressPersistenceManager progressManager;
    late MockAppDatabase mockDatabase;
    late MockComicProgressDao mockDao;

    setUpAll(() {
      registerFallbackValue(ComicProgressCompanion(
        comicId: const drift.Value('test'),
        currentPage: const drift.Value(0),
        lastUpdated: drift.Value(DateTime.now()),
      ));
      registerFallbackValue(<ComicProgressCompanion>[]);
    });

    setUp(() {
      mockDatabase = MockAppDatabase();
      mockDao = MockComicProgressDao();
      
      when(() => mockDatabase.comicProgressDao).thenReturn(mockDao);
      
      progressManager = ProgressPersistenceManager(
        mockDatabase,
        batchFlushInterval: const Duration(milliseconds: 100),
        maxBatchSize: 3,
        maxRetryAttempts: 2,
        retryDelays: [
          const Duration(milliseconds: 10),
          const Duration(milliseconds: 20),
        ],
      );
    });

    tearDown(() {
      progressManager.dispose();
    });

    group('Progress Saving', () {
      test('should save progress immediately when forceImmediate is true', () async {
        const comicId = 'comic123';
        const currentPage = 5;
        const totalPages = 100;

        when(() => mockDao.saveProgress(any())).thenAnswer((_) async {});

        final result = await progressManager.saveProgress(
          comicId,
          currentPage,
          totalPages: totalPages,
          forceImmediate: true,
        );

        expect(result.isSuccess, isTrue);
        verify(() => mockDao.saveProgress(any())).called(1);
      });

      test('should use batch processing when forceImmediate is false', () async {
        const comicId = 'comic123';
        const currentPage = 5;

        final result = await progressManager.saveProgress(
          comicId,
          currentPage,
          forceImmediate: false,
        );

        expect(result.isSuccess, isTrue);
        // Should not call database immediately
        verifyNever(() => mockDao.saveProgress(any()));
        
        // Wait for batch flush
        await Future.delayed(const Duration(milliseconds: 150));
        verify(() => mockDao.batchSaveProgress(any())).called(1);
      });

      test('should flush batch when max batch size is reached', () async {
        when(() => mockDao.batchSaveProgress(any())).thenAnswer((_) async {});

        // Add items to reach max batch size
        for (int i = 0; i < 3; i++) {
          await progressManager.saveProgress('comic$i', i, forceImmediate: false);
        }

        // Should trigger batch flush
        verify(() => mockDao.batchSaveProgress(any())).called(1);
      });

      test('should retry on save failure with exponential backoff', () async {
        const comicId = 'comic123';
        const currentPage = 5;

        // First two attempts fail, third succeeds
        when(() => mockDao.saveProgress(any()))
            .thenThrow(Exception('Database error'))
            .thenThrow(Exception('Database error'))
            .thenAnswer((_) async {});

        final result = await progressManager.saveProgress(
          comicId,
          currentPage,
          forceImmediate: true,
        );

        expect(result.isSuccess, isTrue);
        verify(() => mockDao.saveProgress(any())).called(3);
      });

      test('should fail after max retry attempts', () async {
        const comicId = 'comic123';
        const currentPage = 5;

        when(() => mockDao.saveProgress(any()))
            .thenThrow(Exception('Persistent database error'));

        final result = await progressManager.saveProgress(
          comicId,
          currentPage,
          forceImmediate: true,
        );

        expect(result.isFailure, isTrue);
        verify(() => mockDao.saveProgress(any())).called(3); // Initial + 2 retries
      });
    });

    group('Progress Loading', () {
      test('should load progress from memory cache if available', () async {
        const comicId = 'comic123';
        final progress = ComicProgress(
          id: '1',
          comicId: comicId,
          currentPage: 10,
          totalPages: 100,
          lastUpdated: DateTime.now(),
          isCompleted: false,
          syncStatus: const SyncStatus.synced(),
          readingTimeSeconds: 300,
          metadata: {},
        );

        // First save to populate cache
        when(() => mockDao.saveProgress(any())).thenAnswer((_) async {});
        await progressManager.saveProgress(comicId, 10, forceImmediate: true);

        // Now load should use cache
        final result = await progressManager.loadProgress(comicId);

        expect(result.isSuccess, isTrue);
        expect(result.getOrNull()?.currentPage, equals(10));
      });

      test('should load progress from database if not in cache', () async {
        const comicId = 'comic123';
        final mockModel = ComicProgressModel(
          id: 1,
          comicId: comicId,
          currentPage: 15,
          totalPages: 100,
          lastUpdated: DateTime.now(),
          isCompleted: false,
          syncStatus: 'synced',
          syncETag: null,
          lastSyncTime: null,
          readingTimeSeconds: 450,
          metadata: '{}',
        );

        when(() => mockDao.getProgress(comicId))
            .thenAnswer((_) async => mockModel);

        final result = await progressManager.loadProgress(comicId);

        expect(result.isSuccess, isTrue);
        expect(result.getOrNull()?.currentPage, equals(15));
        expect(result.getOrNull()?.readingTimeSeconds, equals(450));
      });

      test('should return failure when progress not found', () async {
        const comicId = 'nonexistent';

        when(() => mockDao.getProgress(comicId))
            .thenAnswer((_) async => null);

        final result = await progressManager.loadProgress(comicId);

        expect(result.isFailure, isTrue);
      });
    });

    group('Batch Operations', () {
      test('should handle batch progress saving successfully', () async {
        final updates = [
          ProgressUpdate(
            comicId: 'comic1',
            currentPage: 5,
            timestamp: DateTime.now(),
            forceImmediate: false,
          ),
          ProgressUpdate(
            comicId: 'comic2',
            currentPage: 10,
            timestamp: DateTime.now(),
            forceImmediate: false,
          ),
        ];

        when(() => mockDao.batchSaveProgress(any())).thenAnswer((_) async {});

        final results = await progressManager.saveBatchProgress(updates);

        expect(results.length, equals(2));
        expect(results.every((r) => r.isSuccess), isTrue);
        verify(() => mockDao.batchSaveProgress(any())).called(1);
      });

      test('should handle batch save failures gracefully', () async {
        final updates = [
          ProgressUpdate(
            comicId: 'comic1',
            currentPage: 5,
            timestamp: DateTime.now(),
            forceImmediate: false,
          ),
        ];

        when(() => mockDao.batchSaveProgress(any()))
            .thenThrow(Exception('Batch save failed'));

        final results = await progressManager.saveBatchProgress(updates);

        expect(results.length, equals(1));
        expect(results.first.isFailure, isTrue);
      });
    });

    group('Sync Operations', () {
      test('should get pending sync progress', () async {
        final mockModels = [
          ComicProgressModel(
            id: 1,
            comicId: 'comic1',
            currentPage: 5,
            totalPages: 50,
            lastUpdated: DateTime.now(),
            isCompleted: false,
            syncStatus: 'pending',
            syncETag: null,
            lastSyncTime: null,
            readingTimeSeconds: 120,
            metadata: '{}',
          ),
        ];

        when(() => mockDao.getPendingSyncProgress())
            .thenAnswer((_) async => mockModels);

        final result = await progressManager.getPendingSync();

        expect(result.length, equals(1));
        expect(result.first.syncStatus.isPending, isTrue);
      });

      test('should mark progress as synced', () async {
        const comicId = 'comic123';
        const etag = 'sync_etag_123';

        when(() => mockDao.markAsSynced(comicId, etag))
            .thenAnswer((_) async {});

        await progressManager.markSynced(comicId, etag);

        verify(() => mockDao.markAsSynced(comicId, etag)).called(1);
      });

      test('should mark progress as conflict', () async {
        const comicId = 'comic123';

        when(() => mockDao.saveProgress(any())).thenAnswer((_) async {});

        await progressManager.markConflict(comicId);

        verify(() => mockDao.saveProgress(any())).called(1);
      });
    });

    group('Memory Management', () {
      test('should update memory cache when saving progress', () async {
        const comicId = 'comic123';
        const currentPage = 10;

        when(() => mockDao.saveProgress(any())).thenAnswer((_) async {});

        await progressManager.saveProgress(
          comicId,
          currentPage,
          forceImmediate: true,
        );

        // Load should now use cache
        final result = await progressManager.loadProgress(comicId);
        expect(result.isSuccess, isTrue);
        expect(result.getOrNull()?.currentPage, equals(currentPage));
        
        // Database should only be called once (for save, not load)
        verify(() => mockDao.saveProgress(any())).called(1);
        verifyNever(() => mockDao.getProgress(any()));
      });

      test('should clear cache when deleting progress', () async {
        const comicId = 'comic123';

        when(() => mockDao.deleteProgress(comicId)).thenAnswer((_) async {});
        when(() => mockDao.getProgress(comicId)).thenAnswer((_) async => null);

        // First save to populate cache
        when(() => mockDao.saveProgress(any())).thenAnswer((_) async {});
        await progressManager.saveProgress(comicId, 5, forceImmediate: true);

        // Delete progress
        await progressManager.deleteProgress(comicId);

        // Load should now query database, not cache
        final result = await progressManager.loadProgress(comicId);
        expect(result.isFailure, isTrue);
        verify(() => mockDao.getProgress(comicId)).called(1);
      });
    });

    group('Statistics', () {
      test('should provide progress statistics', () async {
        final completedModels = [
          ComicProgressModel(
            id: 1,
            comicId: 'comic1',
            currentPage: 50,
            totalPages: 50,
            lastUpdated: DateTime.now(),
            isCompleted: true,
            syncStatus: 'synced',
            syncETag: 'etag1',
            lastSyncTime: DateTime.now(),
            readingTimeSeconds: 1800,
            metadata: '{}',
          ),
        ];

        final pendingModels = [
          ComicProgressModel(
            id: 2,
            comicId: 'comic2',
            currentPage: 10,
            totalPages: 30,
            lastUpdated: DateTime.now(),
            isCompleted: false,
            syncStatus: 'pending',
            syncETag: null,
            lastSyncTime: null,
            readingTimeSeconds: 600,
            metadata: '{}',
          ),
        ];

        when(() => mockDao.getCompletedComics())
            .thenAnswer((_) async => completedModels);
        when(() => mockDao.getPendingSyncProgress())
            .thenAnswer((_) async => pendingModels);

        final stats = await progressManager.getStatistics();

        expect(stats.completedComics, equals(1));
        expect(stats.pendingSyncComics, equals(1));
        expect(stats.totalReadingTimeSeconds, equals(1800));
      });
    });

    group('Stream Updates', () {
      test('should emit progress updates via stream', () async {
        const comicId = 'comic123';
        final progressStream = progressManager.watchProgress(comicId);
        
        final streamController = StreamController<ComicProgressModel?>();
        when(() => mockDao.watchProgress(comicId))
            .thenAnswer((_) => streamController.stream);
        when(() => mockDao.saveProgress(any())).thenAnswer((_) async {});

        final progressUpdates = <ComicProgress?>[];
        final subscription = progressStream.listen(progressUpdates.add);

        // Emit progress update
        final mockModel = ComicProgressModel(
          id: 1,
          comicId: comicId,
          currentPage: 15,
          totalPages: 100,
          lastUpdated: DateTime.now(),
          isCompleted: false,
          syncStatus: 'synced',
          syncETag: null,
          lastSyncTime: null,
          readingTimeSeconds: 300,
          metadata: '{}',
        );
        
        streamController.add(mockModel);

        await Future.delayed(const Duration(milliseconds: 10));

        expect(progressUpdates.length, equals(1));
        expect(progressUpdates.first?.currentPage, equals(15));

        await subscription.cancel();
        await streamController.close();
      });
    });

    group('Error Handling', () {
      test('should handle database exceptions gracefully', () async {
        const comicId = 'comic123';

        when(() => mockDao.getProgress(comicId))
            .thenThrow(Exception('Database connection failed'));

        final result = await progressManager.loadProgress(comicId);

        expect(result.isFailure, isTrue);
      });

      test('should continue operating after non-critical errors', () async {
        const comicId = 'comic123';

        // First call fails, second succeeds
        when(() => mockDao.saveProgress(any()))
            .thenThrow(Exception('Temporary error'))
            .thenAnswer((_) async {});

        // First save fails
        final result1 = await progressManager.saveProgress(
          comicId, 5, forceImmediate: true);
        expect(result1.isFailure, isTrue);

        // Second save succeeds
        final result2 = await progressManager.saveProgress(
          comicId, 10, forceImmediate: true);
        expect(result2.isSuccess, isTrue);
      });
    });

    group('Performance Tests', () {
      test('batch processing should be faster than individual saves', () async {
        when(() => mockDao.saveProgress(any())).thenAnswer(
          (_) async => Future.delayed(const Duration(milliseconds: 10)));
        when(() => mockDao.batchSaveProgress(any())).thenAnswer(
          (_) async => Future.delayed(const Duration(milliseconds: 15)));

        const numOperations = 10;

        // Test individual saves
        final individualStart = DateTime.now();
        for (int i = 0; i < numOperations; i++) {
          await progressManager.saveProgress('comic$i', i, forceImmediate: true);
        }
        final individualDuration = DateTime.now().difference(individualStart);

        // Test batch save
        final batchStart = DateTime.now();
        final updates = List.generate(numOperations, (i) => ProgressUpdate(
          comicId: 'comic$i',
          currentPage: i,
          timestamp: DateTime.now(),
          forceImmediate: false,
        ));
        await progressManager.saveBatchProgress(updates);
        final batchDuration = DateTime.now().difference(batchStart);

        expect(batchDuration.inMilliseconds, lessThan(individualDuration.inMilliseconds));
      });

      test('memory cache should improve load performance', () async {
        const comicId = 'comic123';

        when(() => mockDao.saveProgress(any())).thenAnswer((_) async {});
        when(() => mockDao.getProgress(comicId)).thenAnswer(
          (_) async => Future.delayed(
            const Duration(milliseconds: 10),
            () => ComicProgressModel(
              id: 1,
              comicId: comicId,
              currentPage: 10,
              totalPages: 100,
              lastUpdated: DateTime.now(),
              isCompleted: false,
              syncStatus: 'synced',
              syncETag: null,
              lastSyncTime: null,
              readingTimeSeconds: 300,
              metadata: '{}',
            ),
          ),
        );

        // First load (from database)
        final firstLoadStart = DateTime.now();
        await progressManager.loadProgress(comicId);
        final firstLoadDuration = DateTime.now().difference(firstLoadStart);

        // Save to populate cache
        await progressManager.saveProgress(comicId, 15, forceImmediate: true);

        // Second load (from cache)
        final secondLoadStart = DateTime.now();
        await progressManager.loadProgress(comicId);
        final secondLoadDuration = DateTime.now().difference(secondLoadStart);

        expect(secondLoadDuration.inMicroseconds, 
               lessThan(firstLoadDuration.inMicroseconds));
      });
    });
  });
}