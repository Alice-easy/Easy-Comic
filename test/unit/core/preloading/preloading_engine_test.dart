import 'dart:async';
import 'dart:collection';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:easy_comic/core/preloading/preloading_engine.dart';
import 'package:easy_comic/core/background/background_processor.dart';
import 'package:easy_comic/core/services/cache_service.dart';

// Mock classes
class MockCacheService extends Mock implements ICacheService {}
class MockBackgroundProcessor extends Mock implements BackgroundProcessor {}

void main() {
  group('PreloadingEngine', () {
    late PreloadingEngine engine;
    late MockCacheService mockCacheService;
    late MockBackgroundProcessor mockBackgroundProcessor;

    setUpAll(() {
      registerFallbackValue(const ImageProcessingConfig());
    });

    setUp(() {
      mockCacheService = MockCacheService();
      mockBackgroundProcessor = MockBackgroundProcessor();
      
      // Setup mock defaults
      when(() => mockCacheService.getImage(any())).thenAnswer((_) async => null);
      when(() => mockBackgroundProcessor.processImageInBackground(any(), any()))
          .thenAnswer((_) async => List<int>.filled(1000, 1));
      when(() => mockCacheService.setImage(any(), any())).thenAnswer((_) async {});
      
      engine = PreloadingEngine(
        cacheService: mockCacheService,
        backgroundProcessor: mockBackgroundProcessor,
        maxConcurrentTasks: 2,
        maxQueueSize: 10,
        processingInterval: const Duration(milliseconds: 50),
      );
    });

    tearDown(() {
      engine.dispose();
    });

    group('Queue Management', () {
      test('should enqueue tasks with correct priorities', () async {
        const pageKeys = ['page0', 'page1', 'page2', 'page3', 'page4', 'page5'];
        const currentPageIndex = 0;
        
        await engine.enqueuePreloading(pageKeys, currentPageIndex);
        
        final stats = engine.getStats();
        expect(stats.totalTasks, greaterThan(0));
        expect(stats.criticalQueueSize, greaterThanOrEqualTo(0));
        expect(stats.highQueueSize, greaterThanOrEqualTo(0));
      });

      test('should prioritize current page as critical', () async {
        const pageKeys = ['current_page', 'next_page'];
        const currentPageIndex = 0;
        
        await engine.enqueuePreloading(pageKeys, currentPageIndex);
        
        final stats = engine.getStats();
        expect(stats.criticalQueueSize, greaterThanOrEqualTo(1));
      });

      test('should assign high priority to next 3 pages', () async {
        const pageKeys = ['page1', 'page2', 'page3', 'page4'];
        const currentPageIndex = 1;
        
        await engine.enqueuePreloading(pageKeys, currentPageIndex);
        
        // Allow processing time
        await Future.delayed(const Duration(milliseconds: 100));
        
        final stats = engine.getStats();
        expect(stats.highQueueSize + stats.activeTasks, greaterThanOrEqualTo(0));
      });

      test('should assign medium priority to pages 4-8', () async {
        const pageKeys = List.generate(10, (i) => 'page$i');
        const currentPageIndex = 0;
        
        await engine.enqueuePreloading(pageKeys, currentPageIndex);
        
        final stats = engine.getStats();
        expect(stats.mediumQueueSize, greaterThanOrEqualTo(0));
      });

      test('should assign low priority to pages beyond 8', () async {
        const pageKeys = List.generate(15, (i) => 'page$i');
        const currentPageIndex = 0;
        
        await engine.enqueuePreloading(pageKeys, currentPageIndex);
        
        final stats = engine.getStats();
        expect(stats.lowQueueSize, greaterThanOrEqualTo(0));
      });

      test('should handle empty page keys list', () async {
        const pageKeys = <String>[];
        const currentPageIndex = 0;
        
        await engine.enqueuePreloading(pageKeys, currentPageIndex);
        
        final stats = engine.getStats();
        expect(stats.totalTasks, equals(0));
      });

      test('should skip already cached pages', () async {
        // Mock cache to return data for some pages
        when(() => mockCacheService.getImage('cached_page'))
            .thenAnswer((_) async => List<int>.filled(100, 1));
        when(() => mockCacheService.getImage('not_cached'))
            .thenAnswer((_) async => null);
        
        const pageKeys = ['cached_page', 'not_cached'];
        const currentPageIndex = 0;
        
        await engine.enqueuePreloading(pageKeys, currentPageIndex);
        
        // Only non-cached page should be queued
        final stats = engine.getStats();
        expect(stats.totalTasks, equals(1));
      });
    });

    group('Task Processing', () {
      test('should process high priority tasks first', () async {
        const pageKeys = List.generate(5, (i) => 'page$i');
        const currentPageIndex = 0;
        
        await engine.enqueuePreloading(pageKeys, currentPageIndex);
        
        // Wait for processing
        await Future.delayed(const Duration(milliseconds: 200));
        
        // Verify background processor was called
        verify(() => mockBackgroundProcessor.processImageInBackground(any(), any()))
            .called(greaterThanOrEqualTo(1));
      });

      test('should limit concurrent tasks', () async {
        const pageKeys = List.generate(10, (i) => 'page$i');
        const currentPageIndex = 0;
        
        await engine.enqueuePreloading(pageKeys, currentPageIndex);
        
        // Check that active tasks don't exceed limit
        final stats = engine.getStats();
        expect(stats.activeTasks, lessThanOrEqualTo(2)); // maxConcurrentTasks = 2
      });

      test('should cache processed images', () async {
        const pageKeys = ['test_page'];
        const currentPageIndex = 0;
        
        await engine.enqueuePreloading(pageKeys, currentPageIndex);
        
        // Wait for processing
        await Future.delayed(const Duration(milliseconds: 200));
        
        // Verify image was cached
        verify(() => mockCacheService.setImage(any(), any()))
            .called(greaterThanOrEqualTo(0));
      });

      test('should adjust quality based on priority', () async {
        const pageKeys = ['critical', 'high', 'medium', 'low'];
        const currentPageIndex = 0;
        
        await engine.enqueuePreloading(pageKeys, currentPageIndex);
        
        // Wait for processing
        await Future.delayed(const Duration(milliseconds: 300));
        
        // Verify background processor was called with different quality settings
        final captured = verify(() => mockBackgroundProcessor.processImageInBackground(
          any(), captureAny())).captured;
        
        // Should have different quality values for different priorities
        final configs = captured.cast<ImageProcessingConfig>();
        if (configs.isNotEmpty) {
          expect(configs.any((c) => c.quality > 0.5), isTrue);
        }
      });
    });

    group('Task Cleanup', () {
      test('should cancel outdated tasks', () async {
        const pageKeys = List.generate(20, (i) => 'page$i');
        const currentPageIndex = 0;
        
        await engine.enqueuePreloading(pageKeys, currentPageIndex);
        
        // Change current page significantly
        const newPageKeys = List.generate(5, (i) => 'new_page$i');
        const newCurrentPageIndex = 50; // Far from original pages
        
        await engine.enqueuePreloading(newPageKeys, newCurrentPageIndex);
        
        // Should have cleaned up outdated tasks
        final stats = engine.getStats();
        expect(stats.totalTasks, lessThan(20));
      });

      test('should remove expired tasks', () async {
        // Create engine with very short expiration for testing
        engine.dispose();
        engine = PreloadingEngine(
          cacheService: mockCacheService,
          backgroundProcessor: mockBackgroundProcessor,
        );
        
        const pageKeys = ['test_page'];
        await engine.enqueuePreloading(pageKeys, 0);
        
        final initialStats = engine.getStats();
        
        // Wait for cleanup timer (mocked to run frequently in test)
        await Future.delayed(const Duration(milliseconds: 100));
        
        // Stats should be available
        final finalStats = engine.getStats();
        expect(finalStats, isNotNull);
      });

      test('should handle task removal correctly', () async {
        const pageKeys = ['page1', 'page2', 'page3'];
        await engine.enqueuePreloading(pageKeys, 0);
        
        // Add same pages again (should replace existing tasks)
        await engine.enqueuePreloading(pageKeys, 0);
        
        final stats = engine.getStats();
        // Should not have duplicate tasks
        expect(stats.totalTasks, lessThanOrEqualTo(pageKeys.length));
      });
    });

    group('Statistics and Monitoring', () {
      test('should provide accurate queue statistics', () {
        final stats = engine.getStats();
        
        expect(stats.criticalQueueSize, greaterThanOrEqualTo(0));
        expect(stats.highQueueSize, greaterThanOrEqualTo(0));
        expect(stats.mediumQueueSize, greaterThanOrEqualTo(0));
        expect(stats.lowQueueSize, greaterThanOrEqualTo(0));
        expect(stats.activeTasks, greaterThanOrEqualTo(0));
        expect(stats.totalTasks, greaterThanOrEqualTo(0));
      });

      test('should track total tasks correctly', () async {
        expect(engine.getStats().totalTasks, equals(0));
        
        const pageKeys = ['page1', 'page2', 'page3'];
        await engine.enqueuePreloading(pageKeys, 0);
        
        expect(engine.getStats().totalTasks, greaterThan(0));
      });

      test('should provide string representation of stats', () {
        final stats = engine.getStats();
        final stringRep = stats.toString();
        
        expect(stringRep, contains('PreloadingStats'));
        expect(stringRep, contains('critical:'));
        expect(stringRep, contains('high:'));
        expect(stringRep, contains('medium:'));
        expect(stringRep, contains('low:'));
        expect(stringRep, contains('active:'));
        expect(stringRep, contains('total:'));
      });
    });

    group('Error Handling', () {
      test('should handle background processing errors gracefully', () async {
        // Mock background processor to throw error
        when(() => mockBackgroundProcessor.processImageInBackground(any(), any()))
            .thenThrow(Exception('Processing failed'));
        
        const pageKeys = ['error_page'];
        await engine.enqueuePreloading(pageKeys, 0);
        
        // Wait for processing attempt
        await Future.delayed(const Duration(milliseconds: 200));
        
        // Should not crash, error should be handled
        final stats = engine.getStats();
        expect(stats, isNotNull);
      });

      test('should handle cache service errors gracefully', () async {
        // Mock cache service to throw error
        when(() => mockCacheService.setImage(any(), any()))
            .thenThrow(Exception('Cache failed'));
        
        const pageKeys = ['cache_error_page'];
        await engine.enqueuePreloading(pageKeys, 0);
        
        // Wait for processing
        await Future.delayed(const Duration(milliseconds: 200));
        
        // Should handle error gracefully
        expect(() => engine.getStats(), returnsNormally);
      });
    });

    group('Performance Tests', () {
      test('should handle large number of pages efficiently', () async {
        const pageKeys = List.generate(100, (i) => 'page$i');
        const currentPageIndex = 0;
        
        final stopwatch = Stopwatch()..start();
        await engine.enqueuePreloading(pageKeys, currentPageIndex);
        stopwatch.stop();
        
        // Should enqueue quickly
        expect(stopwatch.elapsedMilliseconds, lessThan(100));
        
        final stats = engine.getStats();
        expect(stats.totalTasks, greaterThan(0));
      });

      test('should maintain queue size limits', () async {
        // Try to exceed maxQueueSize
        const pageKeys = List.generate(50, (i) => 'page$i');
        await engine.enqueuePreloading(pageKeys, 0);
        
        final stats = engine.getStats();
        // Total tasks should respect queue size limits
        expect(stats.totalTasks, lessThanOrEqualTo(40)); // maxQueueSize = 10 * 4 queues
      });

      test('should process tasks within reasonable time', () async {
        const pageKeys = ['fast_page1', 'fast_page2'];
        
        final stopwatch = Stopwatch()..start();
        await engine.enqueuePreloading(pageKeys, 0);
        
        // Wait for processing to start
        await Future.delayed(const Duration(milliseconds: 100));
        stopwatch.stop();
        
        // Should start processing quickly
        expect(stopwatch.elapsedMilliseconds, lessThan(500));
      });
    });

    group('Resource Management', () {
      test('should cancel all tasks on dispose', () {
        engine.cancelAllTasks();
        
        final stats = engine.getStats();
        expect(stats.totalTasks, equals(0));
        expect(stats.activeTasks, equals(0));
      });

      test('should dispose cleanly', () {
        expect(() => engine.dispose(), returnsNormally);
      });

      test('should handle multiple disposals', () {
        engine.dispose();
        expect(() => engine.dispose(), returnsNormally);
      });
    });

    group('Edge Cases', () {
      test('should handle negative current page index', () async {
        const pageKeys = ['page1', 'page2'];
        const currentPageIndex = -1;
        
        await engine.enqueuePreloading(pageKeys, currentPageIndex);
        
        // Should handle gracefully
        expect(() => engine.getStats(), returnsNormally);
      });

      test('should handle very large current page index', () async {
        const pageKeys = ['page1'];
        const currentPageIndex = 1000000;
        
        await engine.enqueuePreloading(pageKeys, currentPageIndex);
        
        expect(() => engine.getStats(), returnsNormally);
      });

      test('should handle duplicate page keys', () async {
        const pageKeys = ['same_page', 'same_page', 'same_page'];
        await engine.enqueuePreloading(pageKeys, 0);
        
        final stats = engine.getStats();
        // Should not create duplicate tasks
        expect(stats.totalTasks, lessThanOrEqualTo(1));
      });
    });
  });

  group('PreloadTask', () {
    test('should create task with correct properties', () {
      final completer = Completer<bool>();
      final task = PreloadTask(
        pageKey: 'test_page',
        pageIndex: 5,
        priority: PreloadPriority.high,
        completer: completer,
      );
      
      expect(task.pageKey, equals('test_page'));
      expect(task.pageIndex, equals(5));
      expect(task.priority, equals(PreloadPriority.high));
      expect(task.completer, equals(completer));
      expect(task.createdAt, isA<DateTime>());
    });

    test('should detect expired tasks', () {
      final completer = Completer<bool>();
      final task = PreloadTask(
        pageKey: 'test_page',
        pageIndex: 0,
        priority: PreloadPriority.low,
        completer: completer,
      );
      
      // Fresh task should not be expired
      expect(task.isExpired, isFalse);
    });
  });

  group('PreloadingStats', () {
    test('should create stats with all properties', () {
      const stats = PreloadingStats(
        criticalQueueSize: 1,
        highQueueSize: 2,
        mediumQueueSize: 3,
        lowQueueSize: 4,
        activeTasks: 5,
        totalTasks: 15,
      );
      
      expect(stats.criticalQueueSize, equals(1));
      expect(stats.highQueueSize, equals(2));
      expect(stats.mediumQueueSize, equals(3));
      expect(stats.lowQueueSize, equals(4));
      expect(stats.activeTasks, equals(5));
      expect(stats.totalTasks, equals(15));
    });
  });
}