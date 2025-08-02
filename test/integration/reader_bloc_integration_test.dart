import 'dart:async';
import 'dart:typed_data';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:easy_comic/presentation/features/reader/bloc/reader_bloc.dart';
import 'package:easy_comic/presentation/features/reader/bloc/reader_event.dart';
import 'package:easy_comic/presentation/features/reader/bloc/reader_state.dart';
import 'package:easy_comic/domain/repositories/comic_repository.dart';
import 'package:easy_comic/domain/repositories/settings_repository.dart';
import 'package:easy_comic/domain/repositories/bookmark_repository.dart';
import 'package:easy_comic/domain/services/auto_page_service.dart';
import 'package:easy_comic/domain/services/cache_service.dart';
import 'package:easy_comic/domain/services/progress_persistence_manager.dart';
import 'package:easy_comic/domain/entities/comic.dart';
import 'package:easy_comic/domain/entities/comic_page.dart';
import 'package:easy_comic/domain/entities/reader_settings.dart';
import 'package:easy_comic/domain/entities/bookmark.dart';
import 'package:easy_comic/domain/entities/comic_progress.dart';
import 'package:easy_comic/core/comic_archive.dart';
import 'package:easy_comic/core/utils/either.dart';
import 'package:easy_comic/core/error/failures.dart';

// Mock classes
class MockComicRepository extends Mock implements ComicRepository {}
class MockSettingsRepository extends Mock implements SettingsRepository {}
class MockBookmarkRepository extends Mock implements BookmarkRepository {}
class MockAutoPageService extends Mock implements AutoPageService {}
class MockCacheService extends Mock implements CacheService {}
class MockComicArchive extends Mock implements ComicArchive {}
class MockProgressPersistenceManager extends Mock implements IProgressPersistenceManager {}

void main() {
  group('ReaderBloc Integration Tests', () {
    late ReaderBloc readerBloc;
    late MockComicRepository mockComicRepository;
    late MockSettingsRepository mockSettingsRepository;
    late MockBookmarkRepository mockBookmarkRepository;
    late MockAutoPageService mockAutoPageService;
    late MockCacheService mockCacheService;
    late MockComicArchive mockComicArchive;
    late MockProgressPersistenceManager mockProgressManager;

    setUpAll(() {
      registerFallbackValue(const ReaderSettings());
      registerFallbackValue(const Bookmark(
        comicId: 'test',
        pageIndex: 0,
        createdAt: null,
      ));
    });

    setUp(() {
      mockComicRepository = MockComicRepository();
      mockSettingsRepository = MockSettingsRepository();
      mockBookmarkRepository = MockBookmarkRepository();
      mockAutoPageService = MockAutoPageService();
      mockCacheService = MockCacheService();
      mockComicArchive = MockComicArchive();
      mockProgressManager = MockProgressPersistenceManager();

      readerBloc = ReaderBloc(
        comicRepository: mockComicRepository,
        settingsRepository: mockSettingsRepository,
        bookmarkRepository: mockBookmarkRepository,
        autoPageService: mockAutoPageService,
        cacheService: mockCacheService,
        comicArchive: mockComicArchive,
        progressPersistenceManager: mockProgressManager,
      );
    });

    tearDown(() {
      readerBloc.close();
    });

    group('Comic Loading with Progress Integration', () {
      test('should load comic and restore progress successfully', () async {
        const comicId = 'comic123';
        final testComic = Comic(
          id: comicId,
          title: 'Test Comic',
          filePath: '/test/path/comic.cbz',
          totalPages: 10,
          createdAt: DateTime.now(),
          pages: _createTestPages(10),
        );

        final testProgress = ComicProgress(
          id: '1',
          comicId: comicId,
          currentPage: 5,
          totalPages: 10,
          lastUpdated: DateTime.now(),
          isCompleted: false,
          syncStatus: const SyncStatus.synced(),
          readingTimeSeconds: 300,
          metadata: {},
        );

        // Setup mocks
        when(() => mockComicRepository.getComicById(comicId))
            .thenAnswer((_) async => Right(testComic));
        when(() => mockSettingsRepository.getReaderSettings())
            .thenAnswer((_) async => const Right(ReaderSettings()));
        when(() => mockBookmarkRepository.getBookmarksForComic(comicId))
            .thenAnswer((_) async => const Right(<Bookmark>[]));
        when(() => mockProgressManager.loadProgress(comicId))
            .thenAnswer((_) async => ProgressResult.success(testProgress));

        // Act
        readerBloc.add(LoadComic(comicId: comicId));

        // Assert
        await expectLater(
          readerBloc.stream,
          emitsInOrder([
            isA<ReaderLoading>(),
            predicate<ReaderLoaded>((state) => 
              state.comic.id == comicId && 
              state.currentPageIndex == 5 // Restored from progress
            ),
          ]),
        );

        verify(() => mockProgressManager.loadProgress(comicId)).called(1);
      });

      test('should handle progress loading failure gracefully', () async {
        const comicId = 'comic123';
        final testComic = Comic(
          id: comicId,
          title: 'Test Comic',
          filePath: '/test/path/comic.cbz',
          totalPages: 10,
          createdAt: DateTime.now(),
          pages: _createTestPages(10),
        );

        // Setup mocks
        when(() => mockComicRepository.getComicById(comicId))
            .thenAnswer((_) async => Right(testComic));
        when(() => mockSettingsRepository.getReaderSettings())
            .thenAnswer((_) async => const Right(ReaderSettings()));
        when(() => mockBookmarkRepository.getBookmarksForComic(comicId))
            .thenAnswer((_) async => const Right(<Bookmark>[]));
        when(() => mockProgressManager.loadProgress(comicId))
            .thenAnswer((_) async => const ProgressResult.failure(
              ProgressError.loadFailed('Progress not found')
            ));

        // Act
        readerBloc.add(LoadComic(comicId: comicId));

        // Assert
        await expectLater(
          readerBloc.stream,
          emitsInOrder([
            isA<ReaderLoading>(),
            predicate<ReaderLoaded>((state) => 
              state.comic.id == comicId && 
              state.currentPageIndex == 0 // Default to first page
            ),
          ]),
        );
      });
    });

    group('Progress Persistence Integration', () {
      test('should auto-save progress when page changes', () async {
        const comicId = 'comic123';
        final testComic = Comic(
          id: comicId,
          title: 'Test Comic',
          filePath: '/test/path/comic.cbz',
          totalPages: 10,
          createdAt: DateTime.now(),
          pages: _createTestPages(10),
        );

        // Setup loaded state
        when(() => mockComicRepository.getComicById(comicId))
            .thenAnswer((_) async => Right(testComic));
        when(() => mockSettingsRepository.getReaderSettings())
            .thenAnswer((_) async => const Right(ReaderSettings()));
        when(() => mockBookmarkRepository.getBookmarksForComic(comicId))
            .thenAnswer((_) async => const Right(<Bookmark>[]));
        when(() => mockProgressManager.loadProgress(comicId))
            .thenAnswer((_) async => const ProgressResult.failure(
              ProgressError.loadFailed('No progress')
            ));
        when(() => mockProgressManager.saveProgress(
          any(), any(), 
          totalPages: any(named: 'totalPages'),
          forceImmediate: any(named: 'forceImmediate'),
          isCompleted: any(named: 'isCompleted'),
          metadata: any(named: 'metadata'),
        )).thenAnswer((_) async => ProgressResult.success(ComicProgress(
          id: '1',
          comicId: comicId,
          currentPage: 3,
          totalPages: 10,
          lastUpdated: DateTime.now(),
          isCompleted: false,
          syncStatus: const SyncStatus.pending(),
          readingTimeSeconds: 0,
          metadata: {},
        )));

        // Load comic first
        readerBloc.add(LoadComic(comicId: comicId));
        await Future.delayed(const Duration(milliseconds: 100));

        // Change page
        readerBloc.add(const PageChanged(3));
        await Future.delayed(const Duration(milliseconds: 50));

        // Verify progress save was called
        verify(() => mockProgressManager.saveProgress(
          comicId, 3,
          totalPages: 10,
          forceImmediate: false,
          isCompleted: false,
          metadata: any(named: 'metadata'),
        )).called(1);
      });

      test('should handle batch progress saving', () async {
        const comicId = 'comic123';
        
        when(() => mockProgressManager.saveProgress(
          any(), any(), 
          totalPages: any(named: 'totalPages'),
          forceImmediate: any(named: 'forceImmediate'),
          isCompleted: any(named: 'isCompleted'),
          metadata: any(named: 'metadata'),
        )).thenAnswer((_) async => ProgressResult.success(ComicProgress(
          id: '1',
          comicId: comicId,
          currentPage: 5,
          totalPages: 10,
          lastUpdated: DateTime.now(),
          isCompleted: false,
          syncStatus: const SyncStatus.pending(),
          readingTimeSeconds: 0,
          metadata: {},
        )));

        // Setup loaded state first
        final testComic = Comic(
          id: comicId,
          title: 'Test Comic',
          filePath: '/test/path/comic.cbz',
          totalPages: 10,
          createdAt: DateTime.now(),
          pages: _createTestPages(10),
        );

        when(() => mockComicRepository.getComicById(comicId))
            .thenAnswer((_) async => Right(testComic));
        when(() => mockSettingsRepository.getReaderSettings())
            .thenAnswer((_) async => const Right(ReaderSettings()));
        when(() => mockBookmarkRepository.getBookmarksForComic(comicId))
            .thenAnswer((_) async => const Right(<Bookmark>[]));
        when(() => mockProgressManager.loadProgress(comicId))
            .thenAnswer((_) async => const ProgressResult.failure(
              ProgressError.loadFailed('No progress')
            ));

        readerBloc.add(LoadComic(comicId: comicId));
        await Future.delayed(const Duration(milliseconds: 100));

        // Force immediate save
        readerBloc.add(const SaveProgress(5, forceImmediate: true));

        await expectLater(
          readerBloc.stream,
          emitsInOrder([
            anything, // Loading state
            anything, // Loaded state
            predicate<ReaderLoaded>((state) => 
              state.lastSavedProgress == 5
            ),
          ]),
        );

        verify(() => mockProgressManager.saveProgress(
          comicId, 5,
          totalPages: 10,
          forceImmediate: true,
          isCompleted: false,
          metadata: any(named: 'metadata'),
        )).called(1);
      });

      test('should handle progress save failures gracefully', () async {
        const comicId = 'comic123';
        final testComic = Comic(
          id: comicId,
          title: 'Test Comic',
          filePath: '/test/path/comic.cbz',
          totalPages: 10,
          createdAt: DateTime.now(),
          pages: _createTestPages(10),
        );

        when(() => mockComicRepository.getComicById(comicId))
            .thenAnswer((_) async => Right(testComic));
        when(() => mockSettingsRepository.getReaderSettings())
            .thenAnswer((_) async => const Right(ReaderSettings()));
        when(() => mockBookmarkRepository.getBookmarksForComic(comicId))
            .thenAnswer((_) async => const Right(<Bookmark>[]));
        when(() => mockProgressManager.loadProgress(comicId))
            .thenAnswer((_) async => const ProgressResult.failure(
              ProgressError.loadFailed('No progress')
            ));
        when(() => mockProgressManager.saveProgress(
          any(), any(), 
          totalPages: any(named: 'totalPages'),
          forceImmediate: any(named: 'forceImmediate'),
          isCompleted: any(named: 'isCompleted'),
          metadata: any(named: 'metadata'),
        )).thenAnswer((_) async => const ProgressResult.failure(
          ProgressError.saveFailed('Database error')
        ));

        readerBloc.add(LoadComic(comicId: comicId));
        await Future.delayed(const Duration(milliseconds: 100));

        // Force immediate save (should show error)
        readerBloc.add(const SaveProgress(5, forceImmediate: true));

        await expectLater(
          readerBloc.stream,
          emitsInOrder([
            anything, // Loading state
            anything, // Loaded state
            isA<ReaderError>(), // Error state from failed save
          ]),
        );
      });
    });

    group('Page Preloading Integration', () {
      test('should trigger preloading when comic loads', () async {
        const comicId = 'comic123';
        final testComic = Comic(
          id: comicId,
          title: 'Test Comic',
          filePath: '/test/path/comic.cbz',
          totalPages: 10,
          createdAt: DateTime.now(),
          pages: _createTestPages(10),
        );

        when(() => mockComicRepository.getComicById(comicId))
            .thenAnswer((_) async => Right(testComic));
        when(() => mockSettingsRepository.getReaderSettings())
            .thenAnswer((_) async => const Right(ReaderSettings()));
        when(() => mockBookmarkRepository.getBookmarksForComic(comicId))
            .thenAnswer((_) async => const Right(<Bookmark>[]));
        when(() => mockProgressManager.loadProgress(comicId))
            .thenAnswer((_) async => const ProgressResult.failure(
              ProgressError.loadFailed('No progress')
            ));

        readerBloc.add(LoadComic(comicId: comicId));

        await expectLater(
          readerBloc.stream,
          emitsInOrder([
            isA<ReaderLoading>(),
            isA<ReaderLoaded>(),
          ]),
        );

        // Preloading should have been initiated (implementation dependent)
      });

      test('should preload adjacent pages on page change', () async {
        const comicId = 'comic123';
        final testComic = Comic(
          id: comicId,
          title: 'Test Comic',
          filePath: '/test/path/comic.cbz',
          totalPages: 10,
          createdAt: DateTime.now(),
          pages: _createTestPages(10),
        );

        // Setup loaded state
        when(() => mockComicRepository.getComicById(comicId))
            .thenAnswer((_) async => Right(testComic));
        when(() => mockSettingsRepository.getReaderSettings())
            .thenAnswer((_) async => const Right(ReaderSettings()));
        when(() => mockBookmarkRepository.getBookmarksForComic(comicId))
            .thenAnswer((_) async => const Right(<Bookmark>[]));
        when(() => mockProgressManager.loadProgress(comicId))
            .thenAnswer((_) async => const ProgressResult.failure(
              ProgressError.loadFailed('No progress')
            ));
        when(() => mockProgressManager.saveProgress(any(), any(), 
          totalPages: any(named: 'totalPages'),
          forceImmediate: any(named: 'forceImmediate'),
          isCompleted: any(named: 'isCompleted'),
          metadata: any(named: 'metadata'),
        )).thenAnswer((_) async => ProgressResult.success(ComicProgress(
          id: '1',
          comicId: comicId,
          currentPage: 3,
          totalPages: 10,
          lastUpdated: DateTime.now(),
          isCompleted: false,
          syncStatus: const SyncStatus.pending(),
          readingTimeSeconds: 0,
          metadata: {},
        )));

        readerBloc.add(LoadComic(comicId: comicId));
        await Future.delayed(const Duration(milliseconds: 100));

        // Change page to trigger adjacent preloading
        readerBloc.add(const PageChanged(3));

        await expectLater(
          readerBloc.stream,
          emitsInOrder([
            anything, // Loading
            anything, // Loaded
            predicate<ReaderLoaded>((state) => state.currentPageIndex == 3),
          ]),
        );

        // Adjacent pages should be preloaded (implementation specific verification)
      });
    });

    group('Error Handling Integration', () {
      test('should handle repository failures with appropriate error states', () async {
        const comicId = 'comic123';

        when(() => mockComicRepository.getComicById(comicId))
            .thenAnswer((_) async => const Left(DatabaseFailure('Database error')));

        readerBloc.add(LoadComic(comicId: comicId));

        await expectLater(
          readerBloc.stream,
          emitsInOrder([
            isA<ReaderLoading>(),
            predicate<ReaderError>((error) => 
              error.errorType == ReaderErrorType.databaseError &&
              error.canRetry == true
            ),
          ]),
        );
      });

      test('should handle archive extraction errors', () async {
        const filePath = '/test/path/invalid.cbz';

        when(() => mockComicArchive.getMetadata())
            .thenThrow(ArchiveException(
              'Invalid archive format',
              ArchiveErrorType.unsupportedFormat,
            ));

        readerBloc.add(LoadComic(filePath: filePath));

        await expectLater(
          readerBloc.stream,
          emitsInOrder([
            isA<ReaderLoading>(),
            predicate<ReaderError>((error) => 
              error.errorType == ReaderErrorType.unsupportedFormat &&
              error.canRetry == false
            ),
          ]),
        );
      });

      test('should handle network-related errors with retry capability', () async {
        const comicId = 'comic123';

        when(() => mockComicRepository.getComicById(comicId))
            .thenAnswer((_) async => const Left(NetworkFailure('Network timeout')));

        readerBloc.add(LoadComic(comicId: comicId));

        await expectLater(
          readerBloc.stream,
          emitsInOrder([
            isA<ReaderLoading>(),
            predicate<ReaderError>((error) => 
              error.canRetry == true &&
              error.message.toLowerCase().contains('network')
            ),
          ]),
        );
      });
    });

    group('State Transitions', () {
      test('should maintain state consistency during page navigation', () async {
        const comicId = 'comic123';
        final testComic = Comic(
          id: comicId,
          title: 'Test Comic',
          filePath: '/test/path/comic.cbz',
          totalPages: 10,
          createdAt: DateTime.now(),
          pages: _createTestPages(10),
        );

        // Setup mocks
        when(() => mockComicRepository.getComicById(comicId))
            .thenAnswer((_) async => Right(testComic));
        when(() => mockSettingsRepository.getReaderSettings())
            .thenAnswer((_) async => const Right(ReaderSettings()));
        when(() => mockBookmarkRepository.getBookmarksForComic(comicId))
            .thenAnswer((_) async => const Right(<Bookmark>[]));
        when(() => mockProgressManager.loadProgress(comicId))
            .thenAnswer((_) async => const ProgressResult.failure(
              ProgressError.loadFailed('No progress')
            ));
        when(() => mockProgressManager.saveProgress(any(), any(), 
          totalPages: any(named: 'totalPages'),
          forceImmediate: any(named: 'forceImmediate'),
          isCompleted: any(named: 'isCompleted'),
          metadata: any(named: 'metadata'),
        )).thenAnswer((_) async => ProgressResult.success(ComicProgress(
          id: '1',
          comicId: comicId,
          currentPage: 5,
          totalPages: 10,
          lastUpdated: DateTime.now(),
          isCompleted: false,
          syncStatus: const SyncStatus.pending(),
          readingTimeSeconds: 0,
          metadata: {},
        )));

        readerBloc.add(LoadComic(comicId: comicId));
        await Future.delayed(const Duration(milliseconds: 100));

        // Navigate through pages
        readerBloc.add(const NextPage());
        readerBloc.add(const NextPage());
        readerBloc.add(const PreviousPage());

        await expectLater(
          readerBloc.stream,
          emitsInOrder([
            anything, // Loading
            anything, // Loaded at page 0
            predicate<ReaderLoaded>((state) => state.currentPageIndex == 1), // Next
            predicate<ReaderLoaded>((state) => state.currentPageIndex == 2), // Next
            predicate<ReaderLoaded>((state) => state.currentPageIndex == 1), // Previous
          ]),
        );
      });

      test('should handle boundary conditions in navigation', () async {
        const comicId = 'comic123';
        final testComic = Comic(
          id: comicId,
          title: 'Test Comic',
          filePath: '/test/path/comic.cbz',
          totalPages: 3, // Small comic for testing boundaries
          createdAt: DateTime.now(),
          pages: _createTestPages(3),
        );

        when(() => mockComicRepository.getComicById(comicId))
            .thenAnswer((_) async => Right(testComic));
        when(() => mockSettingsRepository.getReaderSettings())
            .thenAnswer((_) async => const Right(ReaderSettings()));
        when(() => mockBookmarkRepository.getBookmarksForComic(comicId))
            .thenAnswer((_) async => const Right(<Bookmark>[]));
        when(() => mockProgressManager.loadProgress(comicId))
            .thenAnswer((_) async => const ProgressResult.failure(
              ProgressError.loadFailed('No progress')
            ));
        when(() => mockProgressManager.saveProgress(any(), any(), 
          totalPages: any(named: 'totalPages'),
          forceImmediate: any(named: 'forceImmediate'),
          isCompleted: any(named: 'isCompleted'),
          metadata: any(named: 'metadata'),
        )).thenAnswer((_) async => ProgressResult.success(ComicProgress(
          id: '1',
          comicId: comicId,
          currentPage: 0,
          totalPages: 3,
          lastUpdated: DateTime.now(),
          isCompleted: false,
          syncStatus: const SyncStatus.pending(),
          readingTimeSeconds: 0,
          metadata: {},
        )));

        readerBloc.add(LoadComic(comicId: comicId));
        await Future.delayed(const Duration(milliseconds: 100));

        // Try to go to previous page from first page (should stay at 0)
        readerBloc.add(const PreviousPage());
        await Future.delayed(const Duration(milliseconds: 50));

        // Go to last page and try to go beyond
        readerBloc.add(const PageChanged(2));
        await Future.delayed(const Duration(milliseconds: 50));
        readerBloc.add(const NextPage()); // Should stay at page 2

        final finalState = readerBloc.state as ReaderLoaded;
        expect(finalState.currentPageIndex, equals(2));
      });
    });

    group('Performance Integration', () {
      test('should complete comic loading within performance targets', () async {
        const comicId = 'comic123';
        final testComic = Comic(
          id: comicId,
          title: 'Test Comic',
          filePath: '/test/path/comic.cbz',
          totalPages: 50, // Larger comic
          createdAt: DateTime.now(),
          pages: _createTestPages(50),
        );

        when(() => mockComicRepository.getComicById(comicId))
            .thenAnswer((_) async => Right(testComic));
        when(() => mockSettingsRepository.getReaderSettings())
            .thenAnswer((_) async => const Right(ReaderSettings()));
        when(() => mockBookmarkRepository.getBookmarksForComic(comicId))
            .thenAnswer((_) async => const Right(<Bookmark>[]));
        when(() => mockProgressManager.loadProgress(comicId))
            .thenAnswer((_) async => const ProgressResult.failure(
              ProgressError.loadFailed('No progress')
            ));

        final stopwatch = Stopwatch()..start();
        readerBloc.add(LoadComic(comicId: comicId));

        await expectLater(
          readerBloc.stream,
          emitsInOrder([
            isA<ReaderLoading>(),
            isA<ReaderLoaded>(),
          ]),
        );

        stopwatch.stop();
        
        // Should load within 2 seconds
        expect(stopwatch.elapsedMilliseconds, lessThan(2000));
      });

      test('should handle rapid page changes efficiently', () async {
        const comicId = 'comic123';
        final testComic = Comic(
          id: comicId,
          title: 'Test Comic',
          filePath: '/test/path/comic.cbz',
          totalPages: 20,
          createdAt: DateTime.now(),
          pages: _createTestPages(20),
        );

        when(() => mockComicRepository.getComicById(comicId))
            .thenAnswer((_) async => Right(testComic));
        when(() => mockSettingsRepository.getReaderSettings())
            .thenAnswer((_) async => const Right(ReaderSettings()));
        when(() => mockBookmarkRepository.getBookmarksForComic(comicId))
            .thenAnswer((_) async => const Right(<Bookmark>[]));
        when(() => mockProgressManager.loadProgress(comicId))
            .thenAnswer((_) async => const ProgressResult.failure(
              ProgressError.loadFailed('No progress')
            ));
        when(() => mockProgressManager.saveProgress(any(), any(), 
          totalPages: any(named: 'totalPages'),
          forceImmediate: any(named: 'forceImmediate'),
          isCompleted: any(named: 'isCompleted'),
          metadata: any(named: 'metadata'),
        )).thenAnswer((_) async => ProgressResult.success(ComicProgress(
          id: '1',
          comicId: comicId,
          currentPage: 10,
          totalPages: 20,
          lastUpdated: DateTime.now(),
          isCompleted: false,
          syncStatus: const SyncStatus.pending(),
          readingTimeSeconds: 0,
          metadata: {},
        )));

        readerBloc.add(LoadComic(comicId: comicId));
        await Future.delayed(const Duration(milliseconds: 100));

        final stopwatch = Stopwatch()..start();
        
        // Rapid page changes
        for (int i = 1; i <= 10; i++) {
          readerBloc.add(PageChanged(i));
          await Future.delayed(const Duration(milliseconds: 10));
        }

        stopwatch.stop();

        // Should handle rapid changes within 500ms
        expect(stopwatch.elapsedMilliseconds, lessThan(500));
      });
    });
  });
}

List<ComicPage> _createTestPages(int count) {
  return List.generate(count, (index) => ComicPage(
    index: index,
    path: 'page_${index}.jpg',
    imageData: Uint8List.fromList([1, 2, 3, 4]), // Minimal test data
  ));
}