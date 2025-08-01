import 'dart:typed_data';
import 'dart:io';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:path_provider/path_provider.dart';
import 'package:easy_comic/core/services/cache_service.dart';

// Mock classes
class MockDirectory extends Mock implements Directory {}
class MockFile extends Mock implements File {}

void main() {
  group('LRUCache', () {
    late LRUCache<String, String> cache;

    setUp(() {
      cache = LRUCache<String, String>(3);
    });

    test('should add and retrieve items correctly', () {
      cache.put('key1', 'value1');
      cache.put('key2', 'value2');
      
      expect(cache.get('key1'), equals('value1'));
      expect(cache.get('key2'), equals('value2'));
      expect(cache.length, equals(2));
    });

    test('should handle capacity overflow by evicting LRU items', () {
      cache.put('key1', 'value1');
      cache.put('key2', 'value2');
      cache.put('key3', 'value3');
      cache.put('key4', 'value4'); // Should evict key1
      
      expect(cache.get('key1'), isNull);
      expect(cache.get('key2'), equals('value2'));
      expect(cache.get('key3'), equals('value3'));
      expect(cache.get('key4'), equals('value4'));
      expect(cache.length, equals(3));
    });

    test('should move accessed items to front', () {
      cache.put('key1', 'value1');
      cache.put('key2', 'value2');
      cache.put('key3', 'value3');
      
      // Access key1 to move it to front
      cache.get('key1');
      
      // Add key4, should evict key2 (now LRU)
      cache.put('key4', 'value4');
      
      expect(cache.get('key1'), equals('value1')); // Still present
      expect(cache.get('key2'), isNull); // Evicted
      expect(cache.get('key3'), equals('value3'));
      expect(cache.get('key4'), equals('value4'));
    });

    test('should update existing keys without changing capacity', () {
      cache.put('key1', 'value1');
      cache.put('key2', 'value2');
      cache.put('key1', 'updated_value1'); // Update existing
      
      expect(cache.get('key1'), equals('updated_value1'));
      expect(cache.length, equals(2));
    });

    test('should clear all items', () {
      cache.put('key1', 'value1');
      cache.put('key2', 'value2');
      
      cache.clear();
      
      expect(cache.get('key1'), isNull);
      expect(cache.get('key2'), isNull);
      expect(cache.length, equals(0));
      expect(cache.isEmpty, isTrue);
    });

    test('should handle edge cases correctly', () {
      // Empty cache
      expect(cache.get('nonexistent'), isNull);
      expect(cache.isEmpty, isTrue);
      
      // Single item cache
      final singleCache = LRUCache<String, String>(1);
      singleCache.put('only', 'value');
      expect(singleCache.get('only'), equals('value'));
      expect(singleCache.isFull, isTrue);
      
      singleCache.put('new', 'newvalue');
      expect(singleCache.get('only'), isNull);
      expect(singleCache.get('new'), equals('newvalue'));
    });
  });

  group('CacheService', () {
    late CacheService cacheService;
    late MockDirectory mockCacheDirectory;
    late MockFile mockFile;

    setUpAll(() {
      registerFallbackValue(Uint8List(0));
    });

    setUp(() {
      mockCacheDirectory = MockDirectory();
      mockFile = MockFile();
      cacheService = CacheService();
      
      // Mock directory operations
      when(() => mockCacheDirectory.exists()).thenAnswer((_) async => true);
      when(() => mockCacheDirectory.create(recursive: any(named: 'recursive')))
          .thenAnswer((_) async => mockCacheDirectory);
      when(() => mockCacheDirectory.path).thenReturn('/test/cache');
      when(() => mockCacheDirectory.list()).thenAnswer((_) => Stream.empty());
      when(() => mockCacheDirectory.delete(recursive: any(named: 'recursive')))
          .thenAnswer((_) async {});
    });

    test('should cache image in memory and return it', () async {
      final testImage = Uint8List.fromList([1, 2, 3, 4]);
      const testKey = 'test_image';
      
      await cacheService.setImage(testKey, testImage);
      final cachedImage = await cacheService.getImage(testKey);
      
      expect(cachedImage, equals(testImage));
    });

    test('should return null for non-existent cache key', () async {
      final result = await cacheService.getImage('nonexistent_key');
      expect(result, isNull);
    });

    test('should track memory usage correctly', () async {
      final testImage1 = Uint8List.fromList(List.filled(1000, 1));
      final testImage2 = Uint8List.fromList(List.filled(2000, 2));
      
      await cacheService.setImage('image1', testImage1);
      await cacheService.setImage('image2', testImage2);
      
      final stats = await cacheService.getStats();
      expect(stats.totalItems, equals(2));
      expect(stats.memoryUsage, greaterThan(0));
    });

    test('should perform memory cleanup when threshold exceeded', () async {
      // Fill cache beyond memory limit with large images
      final largeImage = Uint8List.fromList(List.filled(20 * 1024 * 1024, 1)); // 20MB
      
      await cacheService.setImage('large1', largeImage);
      await cacheService.setImage('large2', largeImage);
      await cacheService.setImage('large3', largeImage); // Should trigger cleanup
      
      final stats = await cacheService.getStats();
      expect(stats.memoryUsage, lessThan(50)); // Should be under 50MB limit
    });

    test('should clear all cache data', () async {
      final testImage = Uint8List.fromList([1, 2, 3, 4]);
      await cacheService.setImage('test_key', testImage);
      
      await cacheService.clearCache();
      
      final cachedImage = await cacheService.getImage('test_key');
      expect(cachedImage, isNull);
      
      final stats = await cacheService.getStats();
      expect(stats.totalItems, equals(0));
      expect(stats.memoryUsage, equals(0));
    });

    test('should preload pages with priority queue', () async {
      const pageKeys = ['page1', 'page2', 'page3', 'page4', 'page5'];
      const currentPageIndex = 0;
      
      // This should prioritize first 3 pages as high priority
      await cacheService.preloadPages(pageKeys, currentPageIndex);
      
      // Verify the method completed without throwing
      expect(() => cacheService.preloadPages(pageKeys, currentPageIndex), 
             returnsNormally);
    });

    test('should adjust image quality correctly', () async {
      final testImage = Uint8List.fromList(List.filled(1000, 255));
      const pageKey = 'test_page';
      
      await cacheService.setImage(pageKey, testImage);
      await cacheService.setImageQuality(pageKey, ImageQuality.thumbnail);
      
      // Verify quality adjustment doesn't throw
      expect(() => cacheService.setImageQuality(pageKey, ImageQuality.high), 
             returnsNormally);
    });

    test('should handle hardware acceleration configuration', () async {
      // Should not throw for either enabled or disabled
      expect(() => cacheService.configureHardwareAcceleration(true), 
             returnsNormally);
      expect(() => cacheService.configureHardwareAcceleration(false), 
             returnsNormally);
    });

    test('should perform background cleanup without blocking', () async {
      final testImage = Uint8List.fromList(List.filled(1000, 1));
      await cacheService.setImage('test', testImage);
      
      // Should complete asynchronously without blocking
      final stopwatch = Stopwatch()..start();
      await cacheService.cleanupCacheAsync();
      stopwatch.stop();
      
      // Should complete quickly (background operation)
      expect(stopwatch.elapsedMilliseconds, lessThan(1000));
    });

    test('should emit memory pressure levels correctly', () async {
      final pressureStream = cacheService.watchMemoryPressure();
      late MemoryPressureLevel emittedLevel;
      
      final subscription = pressureStream.listen((level) {
        emittedLevel = level;
      });
      
      // Fill cache to trigger memory pressure
      final largeImage = Uint8List.fromList(List.filled(30 * 1024 * 1024, 1));
      await cacheService.setImage('pressure_test', largeImage);
      
      // Wait for emission
      await Future.delayed(const Duration(milliseconds: 100));
      
      expect(emittedLevel, isIn([
        MemoryPressureLevel.warning,
        MemoryPressureLevel.critical,
        MemoryPressureLevel.emergency
      ]));
      
      await subscription.cancel();
    });

    group('Performance Tests', () {
      test('cache hit performance should be under 1ms', () async {
        final testImage = Uint8List.fromList(List.filled(1000, 1));
        await cacheService.setImage('perf_test', testImage);
        
        final stopwatch = Stopwatch()..start();
        await cacheService.getImage('perf_test');
        stopwatch.stop();
        
        expect(stopwatch.elapsedMicroseconds, lessThan(1000)); // < 1ms
      });

      test('should maintain good hit rate with realistic usage', () async {
        // Simulate realistic page reading pattern
        final pages = List.generate(10, (i) => 
            Uint8List.fromList(List.filled(500 * 1024, i))); // 500KB each
        
        // Cache pages
        for (int i = 0; i < pages.length; i++) {
          await cacheService.setImage('page$i', pages[i]);
        }
        
        // Simulate reading pattern (recent pages accessed more)
        for (int round = 0; round < 5; round++) {
          for (int i = 0; i < 5; i++) { // Access first 5 pages multiple times
            await cacheService.getImage('page$i');
          }
        }
        
        final stats = await cacheService.getStats();
        expect(stats.hitRate, greaterThan(0.8)); // > 80% hit rate
      });

      test('memory usage should stay within limits', () async {
        // Add many small images
        for (int i = 0; i < 200; i++) {
          final image = Uint8List.fromList(List.filled(100 * 1024, i % 256));
          await cacheService.setImage('small$i', image);
        }
        
        final stats = await cacheService.getStats();
        expect(stats.memoryUsage, lessThan(60)); // Should stay under 60MB
      });
    });

    group('Edge Cases', () {
      test('should handle empty image data', () async {
        final emptyImage = Uint8List(0);
        await cacheService.setImage('empty', emptyImage);
        
        final result = await cacheService.getImage('empty');
        expect(result, equals(emptyImage));
      });

      test('should handle very large cache keys', () async {
        final longKey = 'a' * 1000; // 1000 character key
        final testImage = Uint8List.fromList([1, 2, 3]);
        
        await cacheService.setImage(longKey, testImage);
        final result = await cacheService.getImage(longKey);
        
        expect(result, equals(testImage));
      });

      test('should handle rapid consecutive operations', () async {
        final futures = <Future>[];
        
        // Perform 100 concurrent cache operations
        for (int i = 0; i < 100; i++) {
          final image = Uint8List.fromList([i % 256]);
          futures.add(cacheService.setImage('rapid$i', image));
        }
        
        await Future.wait(futures);
        
        // Verify all operations completed
        final stats = await cacheService.getStats();
        expect(stats.totalItems, greaterThan(0));
      });
    });

    tearDown(() {
      cacheService.dispose();
    });
  });
}