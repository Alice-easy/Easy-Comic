import 'dart:async';
import 'dart:typed_data';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:easy_comic/core/services/progress_persistence_manager_impl.dart';
import 'package:easy_comic/core/services/page_preloading_service.dart';
import 'package:easy_comic/core/services/enhanced_cache_service.dart';
import 'package:easy_comic/core/error/error_handler_chain.dart';

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  group('Production Integration Tests', () {
    group('Progress Persistence Performance', () {
      testWidgets('should handle high-volume progress updates efficiently', (tester) async {
        // This test simulates a real reading session with frequent page changes
        final progressManager = ProgressPersistenceManager(
          // Use in-memory database for testing
          createInMemoryDatabase(),
          batchFlushInterval: const Duration(milliseconds: 50),
          maxBatchSize: 5,
        );

        const comicId = 'performance_test_comic';
        const totalPages = 100;
        
        final stopwatch = Stopwatch()..start();

        // Simulate rapid page navigation (like a user quickly flipping through pages)
        for (int page = 0; page < 50; page++) {
          await progressManager.saveProgress(
            comicId,
            page,
            totalPages: totalPages,
            forceImmediate: false, // Use batching
          );
          
          // Small delay to simulate realistic reading pace
          await Future.delayed(const Duration(milliseconds: 20));
        }

        // Force flush any remaining batched updates
        await progressManager.flushBatchBuffer();
        
        stopwatch.stop();

        // Performance assertion: Should complete 50 updates in under 2 seconds
        expect(stopwatch.elapsedMilliseconds, lessThan(2000));

        // Verify final progress
        final result = await progressManager.loadProgress(comicId);
        expect(result.isSuccess, isTrue);
        expect(result.getOrNull()?.currentPage, equals(49));

        progressManager.dispose();
      });

      testWidgets('should maintain data consistency under concurrent load', (tester) async {
        final progressManager = ProgressPersistenceManager(
          createInMemoryDatabase(),
          batchFlushInterval: const Duration(milliseconds: 100),
          maxBatchSize: 10,
        );

        const numComics = 20;
        const updatesPerComic = 10;

        // Create concurrent progress updates for multiple comics
        final futures = <Future>[];
        
        for (int comicIndex = 0; comicIndex < numComics; comicIndex++) {
          final comicId = 'comic_$comicIndex';
          
          for (int page = 0; page < updatesPerComic; page++) {
            futures.add(progressManager.saveProgress(
              comicId,
              page,
              totalPages: 50,
              forceImmediate: false,
            ));
          }
        }

        // Wait for all concurrent operations to complete
        final results = await Future.wait(futures);

        // All operations should succeed
        expect(results.every((result) => result.isSuccess), isTrue);

        // Verify data consistency - each comic should have final page = updatesPerComic - 1
        for (int comicIndex = 0; comicIndex < numComics; comicIndex++) {
          final comicId = 'comic_$comicIndex';
          final progress = await progressManager.loadProgress(comicId);
          
          expect(progress.isSuccess, isTrue);
          expect(progress.getOrNull()?.currentPage, equals(updatesPerComic - 1));
        }

        progressManager.dispose();
      });
    });

    group('Cache and Preloading Integration', () {
      testWidgets('should demonstrate optimal cache hit rates in realistic usage', (tester) async {
        final cacheService = EnhancedCacheService();
        final preloadingService = PagePreloadingService(cacheService);

        const comicId = 'cache_performance_comic';
        final pages = _createTestPages(30);

        // Start aggressive preloading
        await preloadingService.startPreloading(
          comicId,
          0, // Start from first page
          pages,
          PreloadingStrategy.aggressive,
        );

        // Allow preloading to work
        await Future.delayed(const Duration(milliseconds: 500));

        // Simulate user reading behavior - sequential page access with some back-and-forth
        final accessedPages = [0, 1, 2, 3, 4, 3, 4, 5, 6, 7, 6, 7, 8, 9, 10];
        int cacheHits = 0;

        for (final pageIndex in accessedPages) {
          final imageKey = '${comicId}_page_${pageIndex}';
          final image = await cacheService.getImage(imageKey);
          
          if (image != null) {
            cacheHits++;
          }
        }

        final hitRate = cacheHits / accessedPages.length;
        
        // Should have high cache hit rate due to preloading
        expect(hitRate, greaterThan(0.8)); // > 80% hit rate

        final stats = cacheService.getStatistics();
        expect(stats.hitRate, greaterThan(0.8));

        preloadingService.dispose();
        cacheService.dispose();
      });

      testWidgets('should adapt preloading strategy based on memory pressure', (tester) async {
        final cacheService = EnhancedCacheService();
        final preloadingService = PagePreloadingService(cacheService);

        const comicId = 'memory_pressure_comic';
        final pages = _createTestPages(50);

        // Start with aggressive preloading under low memory pressure
        preloadingService.updateMemoryPressure(MemoryPressureLevel.low);
        await preloadingService.startPreloading(
          comicId,
          10,
          pages,
          PreloadingStrategy.adaptive,
        );

        await Future.delayed(const Duration(milliseconds: 200));
        final statsLowPressure = preloadingService.getStatistics();

        // Simulate high memory pressure
        preloadingService.updateMemoryPressure(MemoryPressureLevel.high);
        await preloadingService.startPreloading(
          comicId,
          15,
          pages,
          PreloadingStrategy.adaptive,
        );

        await Future.delayed(const Duration(milliseconds: 200));
        final statsHighPressure = preloadingService.getStatistics();

        // Under high pressure, should create fewer preload tasks
        expect(statsHighPressure.totalTasksCreated, 
               lessThanOrEqualTo(statsLowPressure.totalTasksCreated));

        preloadingService.dispose();
        cacheService.dispose();
      });
    });

    group('Error Handler Chain Integration', () {
      testWidgets('should maintain application stability under error conditions', (tester) async {
        final errorHandler = ErrorHandlerChain();
        errorHandler.initialize(isDevelopment: false);

        final errorReports = <ErrorReport>[];
        final subscription = errorHandler.errorReportStream.listen(errorReports.add);

        // Simulate various error conditions
        const testErrors = [
          'Network timeout during comic loading',
          'File not found: comic.cbz',
          'Insufficient memory for image cache',
          'Database connection failed',
          'Invalid archive format',
        ];

        for (final errorMessage in testErrors) {
          final error = Exception(errorMessage);
          errorHandler.reportError(error, StackTrace.current, ErrorContext(
            userAction: 'Testing error handling',
            appState: {'test': true},
            deviceInfo: const DeviceInfo(platform: 'test'),
            buildVersion: '1.0.0-test',
          ));
        }

        await Future.delayed(const Duration(milliseconds: 200));

        // Should have captured all errors
        expect(errorReports.length, equals(testErrors.length));

        // Should classify errors with appropriate severity
        final lowSeverityErrors = errorReports.where((r) => r.severity == ErrorSeverity.low);
        final mediumSeverityErrors = errorReports.where((r) => r.severity == ErrorSeverity.medium);
        final highSeverityErrors = errorReports.where((r) => r.severity == ErrorSeverity.high);

        expect(lowSeverityErrors.length + mediumSeverityErrors.length + highSeverityErrors.length,
               equals(testErrors.length));

        await subscription.cancel();
        errorHandler.dispose();
      });
    });

    group('End-to-End Reading Session', () {
      testWidgets('should handle complete reading session with all services', (tester) async {
        // Initialize all services
        final cacheService = EnhancedCacheService();
        final preloadingService = PagePreloadingService(cacheService);
        final progressManager = ProgressPersistenceManager(
          createInMemoryDatabase(),
          batchFlushInterval: const Duration(milliseconds: 100),
        );
        final errorHandler = ErrorHandlerChain();
        errorHandler.initialize(isDevelopment: false);

        const comicId = 'end_to_end_test_comic';
        final pages = _createTestPages(20);

        try {
          // Start reading session
          await preloadingService.startPreloading(
            comicId,
            0,
            pages,
            PreloadingStrategy.standard,
          );

          // Simulate reading session - page by page navigation
          for (int currentPage = 0; currentPage < 10; currentPage++) {
            // Save progress
            await progressManager.saveProgress(
              comicId,
              currentPage,
              totalPages: pages.length,
            );

            // Preload adjacent pages
            await preloadingService.startPreloading(
              comicId,
              currentPage,
              pages,
              PreloadingStrategy.adaptive,
            );

            // Access current page image (cache hit/miss)
            final imageKey = '${comicId}_page_$currentPage';
            await cacheService.getImage(imageKey);

            // Small delay to simulate reading time
            await Future.delayed(const Duration(milliseconds: 50));
          }

          // Verify session results
          final finalProgress = await progressManager.loadProgress(comicId);
          expect(finalProgress.isSuccess, isTrue);
          expect(finalProgress.getOrNull()?.currentPage, equals(9));

          final cacheStats = cacheService.getStatistics();
          expect(cacheStats.totalItems, greaterThan(0));

          final preloadStats = preloadingService.getStatistics();
          expect(preloadStats.totalTasksCreated, greaterThan(0));
          expect(preloadStats.successRate, greaterThan(0.5));

        } finally {
          // Clean up
          preloadingService.dispose();
          cacheService.dispose();
          progressManager.dispose();
          errorHandler.dispose();
        }
      });
    });

    group('Memory Management Integration', () {
      testWidgets('should maintain memory usage within acceptable limits', (tester) async {
        final cacheService = EnhancedCacheService();
        final preloadingService = PagePreloadingService(cacheService);

        const comicId = 'memory_test_comic';
        final largePages = _createLargeTestPages(100); // 100 pages of 1MB each

        // Monitor memory pressure
        final pressureLevels = <MemoryPressureLevel>[];
        final subscription = cacheService.memoryPressureStream.listen(pressureLevels.add);

        try {
          // Aggressively cache large images
          for (int i = 0; i < 50; i++) {
            final imageKey = '${comicId}_page_$i';
            await cacheService.cacheImage(imageKey, largePages[i].imageData);
            
            // Check if memory pressure increased
            if (i % 10 == 0) {
              await Future.delayed(const Duration(milliseconds: 50));
            }
          }

          final stats = cacheService.getStatistics();
          
          // Memory usage should be reasonable (< 100MB for test)
          expect(stats.memoryUsage, lessThan(100));
          
          // Should have triggered memory pressure responses
          expect(pressureLevels.isNotEmpty, isTrue);

        } finally {
          await subscription.cancel();
          preloadingService.dispose();
          cacheService.dispose();
        }
      });
    });
  });
}

// Helper functions
dynamic createInMemoryDatabase() {
  // In a real implementation, this would return an in-memory Drift database
  // For testing purposes, we'll return a mock
  return null;
}

List<ComicPage> _createTestPages(int count) {
  return List.generate(count, (index) => ComicPage(
    index: index,
    path: 'page_${index}.jpg',
    imageData: Uint8List.fromList(List.filled(1024, index % 256)), // 1KB per page
  ));
}

List<ComicPage> _createLargeTestPages(int count) {
  return List.generate(count, (index) => ComicPage(
    index: index,
    path: 'large_page_${index}.jpg',
    imageData: Uint8List.fromList(List.filled(1024 * 1024, index % 256)), // 1MB per page
  ));
}

class ComicPage {
  final int index;
  final String path;
  final Uint8List imageData;

  const ComicPage({
    required this.index,
    required this.path,
    required this.imageData,
  });
}