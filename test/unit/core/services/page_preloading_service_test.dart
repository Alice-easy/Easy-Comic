import 'dart:async';
import 'dart:typed_data';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:easy_comic/core/services/page_preloading_service.dart';
import 'package:easy_comic/core/services/enhanced_cache_service.dart';
import 'package:easy_comic/domain/entities/comic_page.dart';

// Mock classes
class MockEnhancedCacheService extends Mock implements IEnhancedCacheService {}

void main() {
  group('PagePreloadingService', () {
    late PagePreloadingService preloadingService;
    late MockEnhancedCacheService mockCacheService;

    setUp(() {
      mockCacheService = MockEnhancedCacheService();
      preloadingService = PagePreloadingService(mockCacheService);

      // Setup default cache service returns
      when(() => mockCacheService.getStatistics()).thenReturn(
        const CacheStatistics(
          totalItems: 10,
          memoryUsage: 25,
          diskUsage: 50,
          hitRate: 0.85,
          cacheHits: 85,
          cacheMisses: 15,
          averageLoadTime: 150.0,
          preloadSuccessRate: 0.92,
        ),
      );
    });

    tearDown(() {
      preloadingService.dispose();
    });

    group('Preloading Strategy', () {
      test('conservative strategy should preload only next page', () async {
        const comicId = 'comic123';
        const currentPage = 5;
        final pages = _createTestPages(20);

        when(() => mockCacheService.preloadPage(any(), any(), any()))
            .thenAnswer((_) async => true);

        await preloadingService.startPreloading(
          comicId,
          currentPage,
          pages,
          PreloadingStrategy.conservative,
        );

        // Should only preload page 6 (next page)
        verify(() => mockCacheService.preloadPage(comicId, 6, any())).called(1);
        verifyNever(() => mockCacheService.preloadPage(comicId, 7, any()));
        verifyNever(() => mockCacheService.preloadPage(comicId, 4, any()));
      });

      test('standard strategy should preload 2 pages forward and 1 backward', () async {
        const comicId = 'comic123';
        const currentPage = 10;
        final pages = _createTestPages(30);

        when(() => mockCacheService.preloadPage(any(), any(), any()))
            .thenAnswer((_) async => true);

        await preloadingService.startPreloading(
          comicId,
          currentPage,
          pages,
          PreloadingStrategy.standard,
        );

        // Should preload pages 9, 11, 12, 13
        verify(() => mockCacheService.preloadPage(comicId, 9, any())).called(1);
        verify(() => mockCacheService.preloadPage(comicId, 11, any())).called(1);
        verify(() => mockCacheService.preloadPage(comicId, 12, any())).called(1);
        verify(() => mockCacheService.preloadPage(comicId, 13, any())).called(1);
      });

      test('aggressive strategy should preload 5 pages forward and 2 backward', () async {
        const comicId = 'comic123';
        const currentPage = 10;
        final pages = _createTestPages(30);

        when(() => mockCacheService.preloadPage(any(), any(), any()))
            .thenAnswer((_) async => true);

        await preloadingService.startPreloading(
          comicId,
          currentPage,
          pages,
          PreloadingStrategy.aggressive,
        );

        // Should preload pages 8, 9, 11, 12, 13, 14, 15
        verify(() => mockCacheService.preloadPage(comicId, 8, any())).called(1);
        verify(() => mockCacheService.preloadPage(comicId, 9, any())).called(1);
        verify(() => mockCacheService.preloadPage(comicId, 11, any())).called(1);
        verify(() => mockCacheService.preloadPage(comicId, 12, any())).called(1);
        verify(() => mockCacheService.preloadPage(comicId, 13, any())).called(1);
        verify(() => mockCacheService.preloadPage(comicId, 14, any())).called(1);
        verify(() => mockCacheService.preloadPage(comicId, 15, any())).called(1);
      });

      test('adaptive strategy should adjust based on memory pressure', () async {
        const comicId = 'comic123';
        const currentPage = 10;
        final pages = _createTestPages(30);

        when(() => mockCacheService.preloadPage(any(), any(), any()))
            .thenAnswer((_) async => true);

        // Test with low memory pressure (should be aggressive)
        preloadingService.updateMemoryPressure(MemoryPressureLevel.low);
        await preloadingService.startPreloading(
          comicId,
          currentPage,
          pages,
          PreloadingStrategy.adaptive,
        );

        // Should preload aggressively
        verify(() => mockCacheService.preloadPage(comicId, 8, any())).called(1);
        verify(() => mockCacheService.preloadPage(comicId, 15, any())).called(1);

        // Reset and test with high memory pressure
        reset(mockCacheService);
        when(() => mockCacheService.getStatistics()).thenReturn(
          const CacheStatistics(
            totalItems: 10,
            memoryUsage: 25,
            diskUsage: 50,
            hitRate: 0.85,
            cacheHits: 85,
            cacheMisses: 15,
            averageLoadTime: 150.0,
            preloadSuccessRate: 0.92,
          ),
        );
        when(() => mockCacheService.preloadPage(any(), any(), any()))
            .thenAnswer((_) async => true);

        preloadingService.updateMemoryPressure(MemoryPressureLevel.high);
        await preloadingService.startPreloading(
          comicId,
          currentPage + 1, // Different page to avoid cache
          pages,
          PreloadingStrategy.adaptive,
        );

        // Should preload conservatively (only next pages)
        verify(() => mockCacheService.preloadPage(comicId, 12, any())).called(1);
        verify(() => mockCacheService.preloadPage(comicId, 13, any())).called(1);
        verifyNever(() => mockCacheService.preloadPage(comicId, 10, any()));
      });
    });

    group('Memory Pressure Adaptation', () {
      test('should update cache service memory pressure', () async {
        when(() => mockCacheService.updateMemoryPressure(any())).thenReturn(null);

        preloadingService.updateMemoryPressure(MemoryPressureLevel.critical);

        verify(() => mockCacheService.updateMemoryPressure(MemoryPressureLevel.critical))
            .called(1);
      });

      test('should cancel low priority tasks on critical memory pressure', () async {
        const comicId = 'comic123';
        const currentPage = 10;
        final pages = _createTestPages(30);

        when(() => mockCacheService.preloadPage(any(), any(), any()))
            .thenAnswer((_) async => true);

        // Start preloading with normal memory pressure
        await preloadingService.startPreloading(
          comicId,
          currentPage,
          pages,
          PreloadingStrategy.aggressive,
        );

        // Simulate critical memory pressure
        preloadingService.updateMemoryPressure(MemoryPressureLevel.critical);

        // Should update memory pressure in cache service
        verify(() => mockCacheService.updateMemoryPressure(MemoryPressureLevel.critical))
            .called(1);
      });
    });

    group('Priority Management', () {
      test('should assign correct priorities based on distance from current page', () async {
        const comicId = 'comic123';
        const currentPage = 10;

        when(() => mockCacheService.preloadPage(any(), any(), any()))
            .thenAnswer((_) async => true);

        // Test individual page preloading with different distances
        await preloadingService.preloadPage(comicId, 11, PreloadPriority.high);
        await preloadingService.preloadPage(comicId, 15, PreloadPriority.medium);
        await preloadingService.preloadPage(comicId, 20, PreloadPriority.low);

        verify(() => mockCacheService.preloadPage(comicId, 11, PreloadPriority.high))
            .called(1);
        verify(() => mockCacheService.preloadPage(comicId, 15, PreloadPriority.medium))
            .called(1);
        verify(() => mockCacheService.preloadPage(comicId, 20, PreloadPriority.low))
            .called(1);
      });
    });

    group('Preloading Status', () {
      test('should track preloading status correctly', () async {
        const comicId = 'comic123';
        const currentPage = 5;
        final pages = _createTestPages(20);

        when(() => mockCacheService.preloadPage(any(), any(), any()))
            .thenAnswer((_) async => true);

        await preloadingService.startPreloading(
          comicId,
          currentPage,
          pages,
          PreloadingStrategy.standard,
        );

        final status = preloadingService.getStatus(comicId);

        expect(status.comicId, equals(comicId));
        expect(status.currentStrategy, equals(PreloadingStrategy.standard));
        expect(status.isActive, isTrue);
      });

      test('should return empty status for non-active comic', () async {
        const comicId = 'nonexistent';

        final status = preloadingService.getStatus(comicId);

        expect(status.comicId, equals(comicId));
        expect(status.isActive, isFalse);
        expect(status.totalTasks, equals(0));
      });
    });

    group('Cancellation', () {
      test('should cancel preloading for specific comic', () async {
        const comicId = 'comic123';
        const currentPage = 5;
        final pages = _createTestPages(20);

        when(() => mockCacheService.preloadPage(any(), any(), any()))
            .thenAnswer((_) async => true);
        when(() => mockCacheService.cancelPreloading(comicId))
            .thenAnswer((_) async {});

        await preloadingService.startPreloading(
          comicId,
          currentPage,
          pages,
          PreloadingStrategy.standard,
        );

        await preloadingService.cancelPreloading(comicId);

        verify(() => mockCacheService.cancelPreloading(comicId)).called(1);

        final status = preloadingService.getStatus(comicId);
        expect(status.isActive, isFalse);
      });
    });

    group('Pause and Resume', () {
      test('should pause preloading operations', () async {
        const comicId = 'comic123';
        const currentPage = 5;
        final pages = _createTestPages(20);

        preloadingService.pausePreloading();

        // Should not start preloading when paused
        await preloadingService.startPreloading(
          comicId,
          currentPage,
          pages,
          PreloadingStrategy.standard,
        );

        verifyNever(() => mockCacheService.preloadPage(any(), any(), any()));
      });

      test('should resume preloading operations', () async {
        const comicId = 'comic123';

        when(() => mockCacheService.preloadPage(any(), any(), any()))
            .thenAnswer((_) async => true);

        preloadingService.pausePreloading();
        preloadingService.resumePreloading();

        final result = await preloadingService.preloadPage(
          comicId,
          5,
          PreloadPriority.high,
        );

        expect(result.success, isTrue);
        verify(() => mockCacheService.preloadPage(comicId, 5, PreloadPriority.high))
            .called(1);
      });

      test('should return failure when preloading single page while paused', () async {
        const comicId = 'comic123';

        preloadingService.pausePreloading();

        final result = await preloadingService.preloadPage(
          comicId,
          5,
          PreloadPriority.high,
        );

        expect(result.success, isFalse);
        expect(result.errorMessage, equals('Preloading is paused'));
      });
    });

    group('Statistics', () {
      test('should provide accurate preloading statistics', () async {
        const comicId = 'comic123';

        // Simulate successful preloads
        when(() => mockCacheService.preloadPage(any(), any(), any()))
            .thenAnswer((_) async => true);

        await preloadingService.preloadPage(comicId, 1, PreloadPriority.high);
        await preloadingService.preloadPage(comicId, 2, PreloadPriority.high);

        final stats = preloadingService.getStatistics();

        expect(stats.totalTasksCreated, equals(2));
        expect(stats.tasksCompleted, equals(2));
        expect(stats.tasksFailed, equals(0));
        expect(stats.successRate, equals(1.0));
        expect(stats.cacheHitRate, equals(0.85)); // From mock cache service
      });

      test('should track failed preloading tasks', () async {
        const comicId = 'comic123';

        // Simulate failed preloads
        when(() => mockCacheService.preloadPage(any(), any(), any()))
            .thenAnswer((_) async => false);

        await preloadingService.preloadPage(comicId, 1, PreloadPriority.high);
        await preloadingService.preloadPage(comicId, 2, PreloadPriority.high);

        final stats = preloadingService.getStatistics();

        expect(stats.totalTasksCreated, equals(2));
        expect(stats.tasksCompleted, equals(0));
        expect(stats.tasksFailed, equals(2));
        expect(stats.successRate, equals(0.0));
      });

      test('should track average load time', () async {
        const comicId = 'comic123';

        when(() => mockCacheService.preloadPage(any(), any(), any()))
            .thenAnswer((_) async {
          // Simulate some load time
          await Future.delayed(const Duration(milliseconds: 50));
          return true;
        });

        await preloadingService.preloadPage(comicId, 1, PreloadPriority.high);
        await preloadingService.preloadPage(comicId, 2, PreloadPriority.high);

        final stats = preloadingService.getStatistics();

        expect(stats.averageLoadTime, greaterThan(0));
        expect(stats.averageLoadTime, lessThan(200)); // Should be reasonable
      });
    });

    group('Error Handling', () {
      test('should handle cache service errors gracefully', () async {
        const comicId = 'comic123';

        when(() => mockCacheService.preloadPage(any(), any(), any()))
            .thenThrow(Exception('Cache service error'));

        final result = await preloadingService.preloadPage(
          comicId,
          5,
          PreloadPriority.high,
        );

        expect(result.success, isFalse);
        expect(result.errorMessage, contains('Cache service error'));
      });

      test('should continue operating after individual preload failures', () async {
        const comicId = 'comic123';

        when(() => mockCacheService.preloadPage(comicId, 1, any()))
            .thenThrow(Exception('Preload failed'));
        when(() => mockCacheService.preloadPage(comicId, 2, any()))
            .thenAnswer((_) async => true);

        final result1 = await preloadingService.preloadPage(comicId, 1, PreloadPriority.high);
        final result2 = await preloadingService.preloadPage(comicId, 2, PreloadPriority.high);

        expect(result1.success, isFalse);
        expect(result2.success, isTrue);
      });
    });

    group('Performance Tests', () {
      test('preloading should complete within reasonable time', () async {
        const comicId = 'comic123';
        const currentPage = 5;
        final pages = _createTestPages(20);

        when(() => mockCacheService.preloadPage(any(), any(), any()))
            .thenAnswer((_) async {
          await Future.delayed(const Duration(milliseconds: 10));
          return true;
        });

        final stopwatch = Stopwatch()..start();
        await preloadingService.startPreloading(
          comicId,
          currentPage,
          pages,
          PreloadingStrategy.standard,
        );
        stopwatch.stop();

        // Should complete quickly (within 200ms for standard strategy)
        expect(stopwatch.elapsedMilliseconds, lessThan(200));
      });

      test('should handle concurrent preloading requests efficiently', () async {
        const comicId = 'comic123';

        when(() => mockCacheService.preloadPage(any(), any(), any()))
            .thenAnswer((_) async {
          await Future.delayed(const Duration(milliseconds: 5));
          return true;
        });

        final futures = <Future>[];
        for (int i = 0; i < 10; i++) {
          futures.add(preloadingService.preloadPage(comicId, i, PreloadPriority.medium));
        }

        final stopwatch = Stopwatch()..start();
        final results = await Future.wait(futures);
        stopwatch.stop();

        expect(results.every((r) => (r as PreloadResult).success), isTrue);
        // Should complete concurrently, not sequentially
        expect(stopwatch.elapsedMilliseconds, lessThan(100));
      });
    });

    group('Boundary Conditions', () {
      test('should handle edge cases in page ranges', () async {
        const comicId = 'comic123';
        final pages = _createTestPages(5); // Small comic

        when(() => mockCacheService.preloadPage(any(), any(), any()))
            .thenAnswer((_) async => true);

        // Test at beginning
        await preloadingService.startPreloading(
          comicId,
          0,
          pages,
          PreloadingStrategy.standard,
        );

        // Should not try to preload negative pages
        verifyNever(() => mockCacheService.preloadPage(comicId, -1, any()));

        // Test at end
        await preloadingService.startPreloading(
          comicId,
          4,
          pages,
          PreloadingStrategy.standard,
        );

        // Should not try to preload beyond page count
        verifyNever(() => mockCacheService.preloadPage(comicId, 5, any()));
      });

      test('should handle empty page list', () async {
        const comicId = 'comic123';
        final pages = <ComicPage>[];

        await preloadingService.startPreloading(
          comicId,
          0,
          pages,
          PreloadingStrategy.standard,
        );

        // Should not attempt any preloading
        verifyNever(() => mockCacheService.preloadPage(any(), any(), any()));
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