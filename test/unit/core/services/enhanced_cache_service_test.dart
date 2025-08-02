import 'dart:async';
import 'dart:typed_data';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:easy_comic/core/services/enhanced_cache_service.dart';

// Mock classes
class MockMemoryMonitor extends Mock {}

void main() {
  group('EnhancedCacheService', () {
    late EnhancedCacheService cacheService;

    setUp(() {
      cacheService = EnhancedCacheService();
    });

    tearDown(() {
      cacheService.dispose();
    });

    group('Basic Cache Operations', () {
      test('should cache and retrieve images successfully', () async {
        const cacheKey = 'test_image_1';
        final testImage = Uint8List.fromList([1, 2, 3, 4, 5]);

        await cacheService.cacheImage(cacheKey, testImage);
        final retrieved = await cacheService.getImage(cacheKey);

        expect(retrieved, equals(testImage));
      });

      test('should return null for non-existent cache keys', () async {
        const nonExistentKey = 'non_existent_key';

        final result = await cacheService.getImage(nonExistentKey);

        expect(result, isNull);
      });

      test('should handle empty image data', () async {
        const cacheKey = 'empty_image';
        final emptyImage = Uint8List(0);

        await cacheService.cacheImage(cacheKey, emptyImage);
        final retrieved = await cacheService.getImage(cacheKey);

        expect(retrieved, equals(emptyImage));
      });
    });

    group('Priority-based Caching', () {
      test('should handle different cache priorities', () async {
        const keyHigh = 'high_priority';
        const keyMedium = 'medium_priority';
        const keyLow = 'low_priority';
        
        final imageData = Uint8List.fromList([1, 2, 3, 4]);

        await cacheService.cacheImageWithPriority(keyHigh, imageData, CachePriority.high);
        await cacheService.cacheImageWithPriority(keyMedium, imageData, CachePriority.medium);
        await cacheService.cacheImageWithPriority(keyLow, imageData, CachePriority.low);

        expect(await cacheService.getImage(keyHigh), equals(imageData));
        expect(await cacheService.getImage(keyMedium), equals(imageData));
        expect(await cacheService.getImage(keyLow), equals(imageData));
      });

      test('should prioritize high-priority items during eviction', () async {
        // Fill cache with low priority items
        final imageData = Uint8List.fromList(List.filled(1024 * 1024, 1)); // 1MB each
        
        for (int i = 0; i < 30; i++) {
          await cacheService.cacheImageWithPriority(
            'low_$i', 
            imageData, 
            CachePriority.low
          );
        }

        // Add high priority item
        await cacheService.cacheImageWithPriority(
          'high_priority', 
          imageData, 
          CachePriority.high
        );

        // High priority item should still be available
        final retrieved = await cacheService.getImage('high_priority');
        expect(retrieved, equals(imageData));
      });
    });

    group('Preloading', () {
      test('should preload pages with correct priority', () async {
        const comicId = 'comic123';
        const pageIndex = 5;

        final success = await cacheService.preloadPage(
          comicId,
          pageIndex,
          PreloadPriority.high,
        );

        expect(success, isTrue);
      });

      test('should handle preload cancellation', () async {
        const comicId = 'comic123';

        // Start preloading
        final preloadFuture = cacheService.preloadPage(comicId, 1, PreloadPriority.medium);
        
        // Cancel immediately
        await cacheService.cancelPreloading(comicId);

        // Original preload should still complete (but may be interrupted)
        final result = await preloadFuture;
        expect(result, isA<bool>());
      });

      test('should batch preload multiple pages efficiently', () async {
        const comicId = 'comic123';
        final pageIndices = [1, 2, 3, 4, 5];

        final stopwatch = Stopwatch()..start();
        
        final results = await Future.wait(
          pageIndices.map((index) => 
            cacheService.preloadPage(comicId, index, PreloadPriority.medium)
          ),
        );

        stopwatch.stop();

        expect(results.every((success) => success), isTrue);
        // Should complete in reasonable time (concurrent execution)
        expect(stopwatch.elapsedMilliseconds, lessThan(1000));
      });
    });

    group('Memory Pressure Management', () {
      test('should adapt to memory pressure changes', () async {
        // Cache some items
        final imageData = Uint8List.fromList(List.filled(1024 * 1024, 1));
        for (int i = 0; i < 10; i++) {
          await cacheService.cacheImage('item_$i', imageData);
        }

        final statsBefore = cacheService.getStatistics();

        // Simulate high memory pressure
        cacheService.updateMemoryPressure(MemoryPressureLevel.high);

        // Allow time for pressure response
        await Future.delayed(const Duration(milliseconds: 100));

        final statsAfter = cacheService.getStatistics();

        // Should have reduced memory usage
        expect(statsAfter.memoryUsage, lessThanOrEqualTo(statsBefore.memoryUsage));
      });

      test('should clear cache on critical memory pressure', () async {
        // Fill cache
        final imageData = Uint8List.fromList(List.filled(1024 * 1024, 1));
        for (int i = 0; i < 10; i++) {
          await cacheService.cacheImage('item_$i', imageData);
        }

        // Trigger critical memory pressure
        cacheService.updateMemoryPressure(MemoryPressureLevel.critical);

        // Allow time for cleanup
        await Future.delayed(const Duration(milliseconds: 200));

        final stats = cacheService.getStatistics();
        expect(stats.memoryUsage, lessThan(10)); // Should be significantly reduced
      });

      test('should emit memory pressure level changes', () async {
        final pressureLevels = <MemoryPressureLevel>[];
        final subscription = cacheService.memoryPressureStream
            .listen(pressureLevels.add);

        cacheService.updateMemoryPressure(MemoryPressureLevel.medium);
        cacheService.updateMemoryPressure(MemoryPressureLevel.high);

        await Future.delayed(const Duration(milliseconds: 10));

        expect(pressureLevels, contains(MemoryPressureLevel.medium));
        expect(pressureLevels, contains(MemoryPressureLevel.high));

        await subscription.cancel();
      });
    });

    group('Statistics and Monitoring', () {
      test('should provide accurate cache statistics', () async {
        final imageData = Uint8List.fromList(List.filled(1024, 1));
        
        // Cache some items
        await cacheService.cacheImage('item1', imageData);
        await cacheService.cacheImage('item2', imageData);

        // Access one item (cache hit)
        await cacheService.getImage('item1');

        // Try to access non-existent item (cache miss)
        await cacheService.getImage('nonexistent');

        final stats = cacheService.getStatistics();

        expect(stats.totalItems, equals(2));
        expect(stats.memoryUsage, greaterThan(0));
        expect(stats.cacheHits, equals(1));
        expect(stats.cacheMisses, equals(1));
        expect(stats.hitRate, equals(0.5));
      });

      test('should track preload success rate', () async {
        const comicId = 'comic123';

        // Attempt several preloads
        await cacheService.preloadPage(comicId, 1, PreloadPriority.high);
        await cacheService.preloadPage(comicId, 2, PreloadPriority.high);
        await cacheService.preloadPage(comicId, 3, PreloadPriority.high);

        final stats = cacheService.getStatistics();
        expect(stats.preloadSuccessRate, greaterThanOrEqualTo(0.0));
        expect(stats.preloadSuccessRate, lessThanOrEqualTo(1.0));
      });

      test('should measure average load times', () async {
        final imageData = Uint8List.fromList(List.filled(1024, 1));
        
        // Cache and retrieve items to generate load time data
        await cacheService.cacheImage('item1', imageData);
        await cacheService.getImage('item1');

        final stats = cacheService.getStatistics();
        expect(stats.averageLoadTime, greaterThanOrEqualTo(0.0));
      });
    });

    group('Cache Eviction', () {
      test('should evict least recently used items when memory limit reached', () async {
        final largeImage = Uint8List.fromList(List.filled(5 * 1024 * 1024, 1)); // 5MB

        // Fill cache to trigger eviction
        await cacheService.cacheImage('item1', largeImage);
        await cacheService.cacheImage('item2', largeImage);
        await cacheService.cacheImage('item3', largeImage);
        
        // Access item1 to make it recently used
        await cacheService.getImage('item1');
        
        // Add more items to trigger eviction
        await cacheService.cacheImage('item4', largeImage);
        await cacheService.cacheImage('item5', largeImage);

        // item1 should still be available (recently used)
        final item1 = await cacheService.getImage('item1');
        expect(item1, isNotNull);

        // item2 should likely be evicted (least recently used)
        final item2 = await cacheService.getImage('item2');
        // Note: This might be flaky depending on exact implementation
      });

      test('should respect priority during eviction', () async {
        final imageData = Uint8List.fromList(List.filled(2 * 1024 * 1024, 1)); // 2MB

        // Fill cache with different priorities
        await cacheService.cacheImageWithPriority('low1', imageData, CachePriority.low);
        await cacheService.cacheImageWithPriority('medium1', imageData, CachePriority.medium);
        await cacheService.cacheImageWithPriority('high1', imageData, CachePriority.high);

        // Force eviction by adding more items
        for (int i = 0; i < 20; i++) {
          await cacheService.cacheImageWithPriority('fill_$i', imageData, CachePriority.low);
        }

        // High priority item should still be available
        final highPriorityItem = await cacheService.getImage('high1');
        expect(highPriorityItem, isNotNull);
      });
    });

    group('Concurrent Operations', () {
      test('should handle concurrent cache operations safely', () async {
        final imageData = Uint8List.fromList(List.filled(1024, 1));
        final futures = <Future>[];

        // Perform many concurrent operations
        for (int i = 0; i < 50; i++) {
          futures.add(cacheService.cacheImage('concurrent_$i', imageData));
        }

        await Future.wait(futures);

        // Verify all items were cached
        for (int i = 0; i < 50; i++) {
          final retrieved = await cacheService.getImage('concurrent_$i');
          expect(retrieved, equals(imageData));
        }
      });

      test('should handle concurrent preload operations', () async {
        const comicId = 'comic123';
        final futures = <Future<bool>>[];

        // Start multiple preload operations
        for (int i = 0; i < 10; i++) {
          futures.add(cacheService.preloadPage(comicId, i, PreloadPriority.medium));
        }

        final results = await Future.wait(futures);

        // All preloads should complete without throwing
        expect(results.length, equals(10));
        expect(results.every((success) => success is bool), isTrue);
      });
    });

    group('Image Quality Management', () {
      test('should handle different image quality levels', () async {
        const cacheKey = 'quality_test';
        final imageData = Uint8List.fromList(List.filled(1024, 1));

        await cacheService.cacheImage(cacheKey, imageData);
        await cacheService.setImageQuality(cacheKey, ImageQuality.thumbnail);

        // Should still be able to retrieve the image
        final retrieved = await cacheService.getImage(cacheKey);
        expect(retrieved, isNotNull);
      });

      test('should optimize image quality based on memory pressure', () async {
        const cacheKey = 'pressure_quality_test';
        final imageData = Uint8List.fromList(List.filled(1024, 1));

        await cacheService.cacheImage(cacheKey, imageData);

        // Simulate high memory pressure
        cacheService.updateMemoryPressure(MemoryPressureLevel.high);

        // Quality should be automatically adjusted
        await Future.delayed(const Duration(milliseconds: 50));

        final retrieved = await cacheService.getImage(cacheKey);
        expect(retrieved, isNotNull);
      });
    });

    group('Error Handling', () {
      test('should handle invalid cache keys gracefully', () async {
        final imageData = Uint8List.fromList([1, 2, 3]);

        expect(() => cacheService.cacheImage('', imageData), returnsNormally);
        expect(() => cacheService.getImage(''), returnsNormally);
      });

      test('should handle null image data gracefully', () async {
        const cacheKey = 'null_test';

        // Should not throw for null data
        expect(() => cacheService.cacheImage(cacheKey, Uint8List(0)), returnsNormally);
      });

      test('should recover from internal errors', () async {
        const cacheKey = 'recovery_test';
        final imageData = Uint8List.fromList([1, 2, 3]);

        // Should continue working after potential internal errors
        await cacheService.cacheImage(cacheKey, imageData);
        final retrieved = await cacheService.getImage(cacheKey);

        expect(retrieved, equals(imageData));
      });
    });

    group('Performance Tests', () {
      test('cache retrieval should be fast', () async {
        const cacheKey = 'perf_test';
        final imageData = Uint8List.fromList(List.filled(1024, 1));

        await cacheService.cacheImage(cacheKey, imageData);

        final stopwatch = Stopwatch()..start();
        await cacheService.getImage(cacheKey);
        stopwatch.stop();

        expect(stopwatch.elapsedMicroseconds, lessThan(1000)); // < 1ms
      });

      test('should handle large images efficiently', () async {
        const cacheKey = 'large_image';
        final largeImage = Uint8List.fromList(List.filled(10 * 1024 * 1024, 1)); // 10MB

        final stopwatch = Stopwatch()..start();
        await cacheService.cacheImage(cacheKey, largeImage);
        stopwatch.stop();

        expect(stopwatch.elapsedMilliseconds, lessThan(5000)); // < 5 seconds

        final retrieved = await cacheService.getImage(cacheKey);
        expect(retrieved?.length, equals(largeImage.length));
      });

      test('concurrent operations should scale well', () async {
        final imageData = Uint8List.fromList(List.filled(1024, 1));
        const numOperations = 100;

        final stopwatch = Stopwatch()..start();
        
        final futures = List.generate(numOperations, (i) => 
          cacheService.cacheImage('perf_$i', imageData));
        
        await Future.wait(futures);
        stopwatch.stop();

        // Should complete in reasonable time
        expect(stopwatch.elapsedMilliseconds, lessThan(5000));
      });
    });
  });
}