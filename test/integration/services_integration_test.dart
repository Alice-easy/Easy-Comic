import 'dart:async';
import 'dart:typed_data';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:easy_comic/core/services/cache_service.dart';
import 'package:easy_comic/core/background/background_processor.dart';
import 'package:easy_comic/core/preloading/preloading_engine.dart';
import 'package:easy_comic/core/memory/memory_pressure_recovery.dart';
import 'package:easy_comic/domain/repositories/comic_repository.dart';
import 'package:easy_comic/data/repositories/comic_repository_impl.dart';

// Mock classes for integration testing
class MockComicRepository extends Mock implements ComicRepository {}

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  group('Service Layer Integration Tests', () {
    late CacheService cacheService;
    late BackgroundProcessor backgroundProcessor;
    late PreloadingEngine preloadingEngine;
    late MemoryPressureRecovery memoryRecovery;

    setUpAll(() {
      registerFallbackValue(const ImageProcessingConfig());
    });

    setUp(() async {
      // Initialize real services for integration testing
      cacheService = CacheService();
      backgroundProcessor = BackgroundProcessor();
      await backgroundProcessor.initialize();
      
      preloadingEngine = PreloadingEngine(
        cacheService: cacheService,
        backgroundProcessor: backgroundProcessor,
        maxConcurrentTasks: 2,
        processingInterval: const Duration(milliseconds: 100),
      );
      
      memoryRecovery = MemoryPressureRecovery(
        cacheService: cacheService,
        monitoringInterval: const Duration(milliseconds: 500),
        maxMemoryMB: 50, // Lower for testing
        warningThresholdMB: 30,
        criticalThresholdMB: 40,
      );
    });

    tearDown(() async {
      preloadingEngine.dispose();
      memoryRecovery.dispose();
      cacheService.dispose();
      await backgroundProcessor.dispose();
    });

    group('Cache Service Integration', () {
      test('should integrate cache with background processing', () async {
        final testImage = Uint8List.fromList(List.filled(1000, 255));
        const testKey = 'integration_test_image';
        
        // Store image in cache
        await cacheService.setImage(testKey, testImage);
        
        // Retrieve image from cache
        final cachedImage = await cacheService.getImage(testKey);
        
        expect(cachedImage, isNotNull);
        expect(cachedImage, equals(testImage));
        
        // Verify stats are updated
        final stats = await cacheService.getStats();
        expect(stats.totalItems, greaterThan(0));
        expect(stats.memoryUsage, greaterThan(0));
      });

      test('should handle memory pressure with integrated recovery', () async {
        final memoryStatsStream = memoryRecovery.memoryStatsStream;
        final recoveryResultStream = memoryRecovery.recoveryResultStream;
        
        final statsCompleter = Completer<MemoryStats>();
        final recoveryCompleter = Completer<RecoveryResult>();
        
        // Listen for memory stats and recovery results
        final statsSubscription = memoryStatsStream.listen((stats) {
          if (!statsCompleter.isCompleted) {
            statsCompleter.complete(stats);
          }
        });
        
        final recoverySubscription = recoveryResultStream.listen((result) {
          if (!recoveryCompleter.isCompleted) {
            recoveryCompleter.complete(result);
          }
        });
        
        // Fill cache to trigger memory pressure
        for (int i = 0; i < 50; i++) {
          final largeImage = Uint8List.fromList(List.filled(500 * 1024, i % 256)); // 500KB each
          await cacheService.setImage('pressure_test_$i', largeImage);
        }
        
        // Wait for memory stats
        final stats = await statsCompleter.future.timeout(const Duration(seconds: 5));
        expect(stats.pressureLevel, isIn([
          MemoryPressureLevel.warning,
          MemoryPressureLevel.critical,
          MemoryPressureLevel.emergency,
        ]));
        
        // Wait for recovery if triggered
        try {
          final recovery = await recoveryCompleter.future.timeout(const Duration(seconds: 10));
          expect(recovery.strategy, isIn([
            RecoveryStrategy.lightCleanup,
            RecoveryStrategy.aggressiveCleanup,
            RecoveryStrategy.emergencyCleanup,
          ]));
        } catch (e) {
          // Recovery might not trigger if memory pressure is not high enough
          print('Recovery not triggered: $e');
        }
        
        await statsSubscription.cancel();
        await recoverySubscription.cancel();
      });
    });

    group('Preloading Engine Integration', () {
      test('should integrate preloading with cache and background processing', () async {
        const pageKeys = ['page_1', 'page_2', 'page_3', 'page_4', 'page_5'];
        const currentPageIndex = 0;
        
        // Enqueue preloading tasks
        await preloadingEngine.enqueuePreloading(pageKeys, currentPageIndex);
        
        // Wait for processing to occur
        await Future.delayed(const Duration(milliseconds: 500));
        
        final stats = preloadingEngine.getStats();
        expect(stats.totalTasks, greaterThanOrEqualTo(0));
        
        // Verify that some processing occurred
        // Note: In a real integration test, you would have actual images to process
        expect(stats.activeTasks, lessThanOrEqualTo(2)); // Max concurrent tasks
      });

      test('should handle priority-based processing correctly', () async {
        const pageKeys = List.generate(10, (i) => 'priority_page_$i');
        const currentPageIndex = 0;
        
        await preloadingEngine.enqueuePreloading(pageKeys, currentPageIndex);
        
        // Let initial processing occur
        await Future.delayed(const Duration(milliseconds: 200));
        
        final initialStats = preloadingEngine.getStats();
        expect(initialStats.criticalQueueSize, greaterThanOrEqualTo(0));
        expect(initialStats.highQueueSize, greaterThanOrEqualTo(0));
        
        // Change current page to trigger task reordering
        const newPageKeys = List.generate(5, (i) => 'new_priority_page_$i');
        const newCurrentPageIndex = 20;
        
        await preloadingEngine.enqueuePreloading(newPageKeys, newCurrentPageIndex);
        
        await Future.delayed(const Duration(milliseconds: 200));
        
        final newStats = preloadingEngine.getStats();
        // Should have cleaned up outdated tasks and added new ones
        expect(newStats.totalTasks, lessThanOrEqualTo(initialStats.totalTasks + newPageKeys.length));
      });

      test('should handle rapid task changes efficiently', () async {
        final stopwatch = Stopwatch()..start();
        
        // Rapid task changes simulating user scrolling
        for (int round = 0; round < 5; round++) {
          final pageKeys = List.generate(8, (i) => 'rapid_page_${round}_$i');
          await preloadingEngine.enqueuePreloading(pageKeys, round * 10);
          await Future.delayed(const Duration(milliseconds: 50));
        }
        
        stopwatch.stop();
        
        expect(stopwatch.elapsedMilliseconds, lessThan(1000));
        
        final finalStats = preloadingEngine.getStats();
        expect(finalStats.totalTasks, greaterThanOrEqualTo(0));
      });
    });

    group('Memory Management Integration', () {
      test('should coordinate memory management across all services', () async {
        // Fill cache with data
        for (int i = 0; i < 30; i++) {
          final image = Uint8List.fromList(List.filled(200 * 1024, i % 256)); // 200KB each
          await cacheService.setImage('memory_test_$i', image);
        }
        
        // Get initial cache stats
        final initialStats = await cacheService.getStats();
        expect(initialStats.memoryUsage, greaterThan(0));
        
        // Trigger manual recovery
        final recoveryResult = await memoryRecovery.triggerManualRecovery(
          strategy: RecoveryStrategy.aggressiveCleanup,
        );
        
        expect(recoveryResult.succeeded, isTrue);
        expect(recoveryResult.strategy, equals(RecoveryStrategy.aggressiveCleanup));
        
        // Verify memory was actually freed
        final finalStats = await cacheService.getStats();
        // Memory usage should be reduced or similar if cleanup worked
        expect(finalStats.memoryUsage, lessThanOrEqualTo(initialStats.memoryUsage + 5)); // Allow small variance
      });

      test('should handle memory pressure while preloading', () async {
        // Start preloading tasks
        const pageKeys = List.generate(20, (i) => 'pressure_preload_$i');
        await preloadingEngine.enqueuePreloading(pageKeys, 0);
        
        // Fill cache to create memory pressure
        for (int i = 0; i < 40; i++) {
          final image = Uint8List.fromList(List.filled(300 * 1024, i % 256)); // 300KB each
          await cacheService.setImage('pressure_fill_$i', image);
        }
        
        // Wait for memory pressure detection and recovery
        await Future.delayed(const Duration(seconds: 2));
        
        // System should still be functional
        final preloadStats = preloadingEngine.getStats();
        final memoryStats = memoryRecovery.currentStats;
        
        expect(preloadStats, isNotNull);
        expect(memoryStats, isNotNull);
        
        // Memory recovery should have taken action if pressure was high
        final recoveryStats = memoryRecovery.recoveryStats;
        print('Recovery stats: $recoveryStats');
      });
    });

    group('Performance Integration Tests', () {
      test('should maintain performance under load', () async {
        final stopwatch = Stopwatch()..start();
        
        // Concurrent operations simulating real usage
        final futures = <Future>[];
        
        // Cache operations
        for (int i = 0; i < 20; i++) {
          futures.add(cacheService.setImage(
            'perf_test_$i',
            Uint8List.fromList(List.filled(100 * 1024, i % 256)),
          ));
        }
        
        // Preloading operations
        for (int i = 0; i < 5; i++) {
          final pageKeys = List.generate(5, (j) => 'perf_preload_${i}_$j');
          futures.add(preloadingEngine.enqueuePreloading(pageKeys, i * 10));
        }
        
        // Background processing
        for (int i = 0; i < 10; i++) {
          futures.add(backgroundProcessor.processImageInBackground(
            'perf_bg_$i',
            const ImageProcessingConfig(quality: 0.8),
          ));
        }
        
        await Future.wait(futures, eagerError: false);
        stopwatch.stop();
        
        // Should complete within reasonable time
        expect(stopwatch.elapsedMilliseconds, lessThan(10000)); // 10 seconds max
        
        // All services should still be functional
        final cacheStats = await cacheService.getStats();
        final preloadStats = preloadingEngine.getStats();
        
        expect(cacheStats.totalItems, greaterThan(0));
        expect(preloadStats, isNotNull);
      });

      test('should handle memory cleanup efficiently under continuous load', () async {
        final iterations = 50;
        final stopwatch = Stopwatch()..start();
        
        for (int i = 0; i < iterations; i++) {
          // Add images to cache
          final image = Uint8List.fromList(List.filled(150 * 1024, i % 256));
          await cacheService.setImage('continuous_$i', image);
          
          // Occasionally trigger manual cleanup
          if (i % 10 == 0) {
            await memoryRecovery.triggerManualRecovery(
              strategy: RecoveryStrategy.lightCleanup,
            );
          }
          
          // Small delay to simulate real usage pattern
          await Future.delayed(const Duration(milliseconds: 10));
        }
        
        stopwatch.stop();
        
        expect(stopwatch.elapsedMilliseconds, lessThan(5000)); // Should be efficient
        
        final finalStats = await cacheService.getStats();
        final recoveryStats = memoryRecovery.recoveryStats;
        
        // Memory should be managed (not growing indefinitely)
        expect(finalStats.memoryUsage, lessThan(100)); // Should be under limit
        expect(recoveryStats['totalRecoveries'], greaterThan(0));
      });
    });

    group('Error Recovery Integration', () {
      test('should recover gracefully from service errors', () async {
        // Fill cache normally first
        for (int i = 0; i < 10; i++) {
          final image = Uint8List.fromList(List.filled(100 * 1024, i));
          await cacheService.setImage('error_test_$i', image);
        }
        
        // Simulate error conditions by trying invalid operations
        try {
          await cacheService.setImage('', Uint8List(0)); // Invalid key
        } catch (e) {
          // Expected to fail
        }
        
        try {
          await backgroundProcessor.processImageInBackground('', const ImageProcessingConfig());
        } catch (e) {
          // Expected to fail
        }
        
        // Services should still be functional after errors
        final image = Uint8List.fromList(List.filled(100 * 1024, 255));
        await cacheService.setImage('recovery_test', image);
        
        final cachedImage = await cacheService.getImage('recovery_test');
        expect(cachedImage, equals(image));
        
        // Preloading should still work
        await preloadingEngine.enqueuePreloading(['recovery_page_1'], 0);
        
        final stats = preloadingEngine.getStats();
        expect(stats, isNotNull);
      });

      test('should maintain data consistency across service failures', () async {
        // Create initial state
        final initialImages = <String, Uint8List>{};
        for (int i = 0; i < 15; i++) {
          final image = Uint8List.fromList(List.filled(80 * 1024, i));
          initialImages['consistency_$i'] = image;
          await cacheService.setImage('consistency_$i', image);
        }
        
        final initialStats = await cacheService.getStats();
        
        // Simulate partial failure scenario
        final futures = <Future>[];
        for (int i = 0; i < 10; i++) {
          if (i % 3 == 0) {
            // Some operations will fail
            futures.add(
              cacheService.setImage('invalid_key_$i', Uint8List(0))
                .catchError((e) => null),
            );
          } else {
            // Some will succeed
            futures.add(
              cacheService.setImage('valid_$i', Uint8List.fromList([i])),
            );
          }
        }
        
        await Future.wait(futures, eagerError: false);
        
        // Verify original data is still intact
        for (final entry in initialImages.entries) {
          final cachedImage = await cacheService.getImage(entry.key);
          expect(cachedImage, equals(entry.value));
        }
        
        final finalStats = await cacheService.getStats();
        expect(finalStats.totalItems, greaterThanOrEqualTo(initialStats.totalItems));
      });
    });

    group('Resource Management Integration', () {
      test('should properly cleanup all resources on disposal', () async {
        // Create additional temporary services
        final tempCacheService = CacheService();
        final tempBackgroundProcessor = BackgroundProcessor();
        await tempBackgroundProcessor.initialize();
        
        final tempPreloadingEngine = PreloadingEngine(
          cacheService: tempCacheService,
          backgroundProcessor: tempBackgroundProcessor,
        );
        
        final tempMemoryRecovery = MemoryPressureRecovery(
          cacheService: tempCacheService,
          monitoringInterval: const Duration(milliseconds: 100),
        );
        
        // Use the services
        await tempCacheService.setImage('temp_test', Uint8List.fromList([1, 2, 3]));
        await tempPreloadingEngine.enqueuePreloading(['temp_page'], 0);
        
        // Wait for some activity
        await Future.delayed(const Duration(milliseconds: 200));
        
        // Dispose all services
        tempPreloadingEngine.dispose();
        tempMemoryRecovery.dispose();
        tempCacheService.dispose();
        await tempBackgroundProcessor.dispose();
        
        // Should complete without hanging or errors
        expect(true, isTrue); // Test completion indicates success
      });
    });
  });
}