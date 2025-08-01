import 'dart:async';
import 'dart:io';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:easy_comic/core/memory/memory_pressure_recovery.dart';
import 'package:easy_comic/core/services/cache_service.dart';

// Mock classes
class MockCacheService extends Mock implements ICacheService {}

void main() {
  group('MemoryPressureRecovery', () {
    late MemoryPressureRecovery recovery;
    late MockCacheService mockCacheService;

    setUp(() {
      mockCacheService = MockCacheService();
      
      // Setup mock defaults
      when(() => mockCacheService.getStats()).thenAnswer((_) async => 
          const CacheStats(
            totalItems: 10,
            memoryUsage: 30.0, // 30MB
            diskUsage: 5.0,
            hitCount: 100,
            missCount: 20,
            hitRate: 0.83,
          ));
      when(() => mockCacheService.cleanupCacheAsync()).thenAnswer((_) async {});
      when(() => mockCacheService.clearCache()).thenAnswer((_) async {});
      
      recovery = MemoryPressureRecovery(
        cacheService: mockCacheService,
        monitoringInterval: const Duration(milliseconds: 100), // Fast for testing
        maxMemoryMB: 100,
        warningThresholdMB: 60,
        criticalThresholdMB: 80,
      );
    });

    tearDown(() {
      recovery.dispose();
    });

    group('Memory Pressure Detection', () {
      test('should detect normal memory pressure', () async {
        // Mock low memory usage
        when(() => mockCacheService.getStats()).thenAnswer((_) async => 
            const CacheStats(
              totalItems: 5,
              memoryUsage: 20.0, // 20MB - normal
              diskUsage: 2.0,
              hitCount: 50,
              missCount: 10,
              hitRate: 0.83,
            ));

        final statsStream = recovery.memoryStatsStream;
        final stats = await statsStream.first;

        expect(stats.pressureLevel, equals(MemoryPressureLevel.normal));
        expect(stats.usedMemoryMB, lessThan(60)); // Below warning threshold
      });

      test('should detect warning memory pressure', () async {
        // Mock moderate memory usage
        when(() => mockCacheService.getStats()).thenAnswer((_) async => 
            const CacheStats(
              totalItems: 15,
              memoryUsage: 45.0, // 45MB -> ~67MB estimated (warning level)
              diskUsage: 8.0,
              hitCount: 150,
              missCount: 30,
              hitRate: 0.83,
            ));

        final statsStream = recovery.memoryStatsStream;
        final stats = await statsStream.first;

        expect(stats.pressureLevel, equals(MemoryPressureLevel.warning));
      });

      test('should detect critical memory pressure', () async {
        // Mock high memory usage
        when(() => mockCacheService.getStats()).thenAnswer((_) async => 
            const CacheStats(
              totalItems: 25,
              memoryUsage: 60.0, // 60MB -> ~90MB estimated (critical level)
              diskUsage: 15.0,
              hitCount: 200,
              missCount: 50,
              hitRate: 0.8,
            ));

        final statsStream = recovery.memoryStatsStream;
        final stats = await statsStream.first;

        expect(stats.pressureLevel, equals(MemoryPressureLevel.critical));
      });

      test('should detect emergency memory pressure', () async {
        // Mock very high memory usage
        when(() => mockCacheService.getStats()).thenAnswer((_) async => 
            const CacheStats(
              totalItems: 50,
              memoryUsage: 80.0, // 80MB -> ~120MB estimated (emergency level)
              diskUsage: 25.0,
              hitCount: 300,
              missCount: 100,
              hitRate: 0.75,
            ));

        final statsStream = recovery.memoryStatsStream;
        final stats = await statsStream.first;

        expect(stats.pressureLevel, equals(MemoryPressureLevel.emergency));
      });
    });

    group('Recovery Strategy Selection', () {
      test('should select light cleanup for warning level', () async {
        when(() => mockCacheService.getStats()).thenAnswer((_) async => 
            const CacheStats(
              totalItems: 15,
              memoryUsage: 45.0, // Warning level
              diskUsage: 8.0,
              hitCount: 150,
              missCount: 30,
              hitRate: 0.83,
            ));

        final resultStream = recovery.recoveryResultStream;
        final resultFuture = resultStream.first;

        // Wait for automatic recovery to trigger
        await Future.delayed(const Duration(milliseconds: 200));

        final result = await resultFuture;
        expect(result.strategy, equals(RecoveryStrategy.lightCleanup));
      });

      test('should select aggressive cleanup for critical level', () async {
        when(() => mockCacheService.getStats()).thenAnswer((_) async => 
            const CacheStats(
              totalItems: 25,
              memoryUsage: 60.0, // Critical level
              diskUsage: 15.0,
              hitCount: 200,
              missCount: 50,
              hitRate: 0.8,
            ));

        final resultStream = recovery.recoveryResultStream;
        final resultFuture = resultStream.first;

        // Wait for automatic recovery to trigger
        await Future.delayed(const Duration(milliseconds: 200));

        final result = await resultFuture;
        expect(result.strategy, equals(RecoveryStrategy.aggressiveCleanup));
      });

      test('should select emergency cleanup for emergency level', () async {
        when(() => mockCacheService.getStats()).thenAnswer((_) async => 
            const CacheStats(
              totalItems: 50,
              memoryUsage: 80.0, // Emergency level
              diskUsage: 25.0,
              hitCount: 300,
              missCount: 100,
              hitRate: 0.75,
            ));

        final resultStream = recovery.recoveryResultStream;
        final resultFuture = resultStream.first;

        // Wait for automatic recovery to trigger
        await Future.delayed(const Duration(milliseconds: 200));

        final result = await resultFuture;
        expect(result.strategy, equals(RecoveryStrategy.emergencyCleanup));
      });
    });

    group('Recovery Execution', () {
      test('should execute light cleanup correctly', () async {
        final result = await recovery.triggerManualRecovery(
          strategy: RecoveryStrategy.lightCleanup,
        );

        expect(result.strategy, equals(RecoveryStrategy.lightCleanup));
        expect(result.succeeded, isTrue);
        
        // Verify cache cleanup was called
        verify(() => mockCacheService.cleanupCacheAsync()).called(1);
      });

      test('should execute aggressive cleanup correctly', () async {
        final result = await recovery.triggerManualRecovery(
          strategy: RecoveryStrategy.aggressiveCleanup,
        );

        expect(result.strategy, equals(RecoveryStrategy.aggressiveCleanup));
        expect(result.succeeded, isTrue);
        
        // Verify cache cleanup was called
        verify(() => mockCacheService.cleanupCacheAsync()).called(1);
      });

      test('should execute emergency cleanup correctly', () async {
        final result = await recovery.triggerManualRecovery(
          strategy: RecoveryStrategy.emergencyCleanup,
        );

        expect(result.strategy, equals(RecoveryStrategy.emergencyCleanup));
        expect(result.succeeded, isTrue);
        
        // Verify full cache clear was called
        verify(() => mockCacheService.clearCache()).called(1);
        
        // Verify quality was degraded to minimum
        expect(recovery.currentQuality, equals(ImageQuality.thumbnail));
      });

      test('should track memory freed during recovery', () async {
        // Mock different memory usage before and after
        var highMemory = true;
        when(() => mockCacheService.getStats()).thenAnswer((_) async {
          if (highMemory) {
            return const CacheStats(
              totalItems: 40,
              memoryUsage: 70.0, // High memory
              diskUsage: 20.0,
              hitCount: 250,
              missCount: 50,
              hitRate: 0.83,
            );
          } else {
            return const CacheStats(
              totalItems: 20,
              memoryUsage: 30.0, // Lower memory after cleanup
              diskUsage: 10.0,
              hitCount: 250,
              missCount: 50,
              hitRate: 0.83,
            );
          }
        });

        when(() => mockCacheService.cleanupCacheAsync()).thenAnswer((_) async {
          highMemory = false; // Simulate memory reduction
        });

        final result = await recovery.triggerManualRecovery(
          strategy: RecoveryStrategy.aggressiveCleanup,
        );

        expect(result.memoryFreedMB, greaterThan(0));
        expect(result.succeeded, isTrue);
      });
    });

    group('Image Quality Management', () {
      test('should start with high quality', () {
        expect(recovery.currentQuality, equals(ImageQuality.high));
      });

      test('should degrade quality during recovery', () async {
        await recovery.triggerManualRecovery(
          strategy: RecoveryStrategy.emergencyCleanup,
        );

        expect(recovery.currentQuality, equals(ImageQuality.thumbnail));
      });

      test('should reset quality to default', () {
        // First degrade quality
        recovery.triggerManualRecovery(
          strategy: RecoveryStrategy.emergencyCleanup,
        );

        recovery.resetImageQuality();
        expect(recovery.currentQuality, equals(ImageQuality.high));
      });
    });

    group('Statistics and Monitoring', () {
      test('should provide current memory statistics', () async {
        // Wait for initial stats to be collected
        await Future.delayed(const Duration(milliseconds: 150));

        final stats = recovery.currentStats;
        expect(stats, isNotNull);
        expect(stats!.totalMemoryMB, equals(100));
        expect(stats.pressureLevel, isA<MemoryPressureLevel>());
        expect(stats.timestamp, isA<DateTime>());
      });

      test('should track recovery statistics', () async {
        // Perform a recovery
        await recovery.triggerManualRecovery(
          strategy: RecoveryStrategy.lightCleanup,
        );

        final stats = recovery.recoveryStats;
        expect(stats['totalRecoveries'], equals(1));
        expect(stats['currentQuality'], equals('high'));
        expect(stats['lastRecoveryTime'], isNotNull);
      });

      test('should stream memory statistics continuously', () async {
        final statsStream = recovery.memoryStatsStream;
        final stats = <MemoryStats>[];

        final subscription = statsStream.listen(stats.add);

        // Wait for multiple stats updates
        await Future.delayed(const Duration(milliseconds: 300));

        expect(stats, isNotEmpty);
        expect(stats.length, greaterThanOrEqualTo(2));

        await subscription.cancel();
      });

      test('should stream recovery results', () async {
        final resultStream = recovery.recoveryResultStream;
        final results = <RecoveryResult>[];

        final subscription = resultStream.listen(results.add);

        // Trigger recovery
        await recovery.triggerManualRecovery(
          strategy: RecoveryStrategy.lightCleanup,
        );

        expect(results, hasLength(1));
        expect(results.first.strategy, equals(RecoveryStrategy.lightCleanup));

        await subscription.cancel();
      });
    });

    group('Error Handling', () {
      test('should handle cache service errors gracefully', () async {
        when(() => mockCacheService.cleanupCacheAsync())
            .thenThrow(Exception('Cache cleanup failed'));

        final result = await recovery.triggerManualRecovery(
          strategy: RecoveryStrategy.lightCleanup,
        );

        expect(result.succeeded, isFalse);
        expect(result.error, isNotNull);
        expect(result.error, contains('Cache cleanup failed'));
      });

      test('should handle stats collection errors', () async {
        when(() => mockCacheService.getStats())
            .thenThrow(Exception('Stats collection failed'));

        // Should not crash, but continue operating
        await Future.delayed(const Duration(milliseconds: 200));

        expect(() => recovery.recoveryStats, returnsNormally);
      });

      test('should continue monitoring after recovery errors', () async {
        when(() => mockCacheService.cleanupCacheAsync())
            .thenThrow(Exception('Temporary failure'));

        // First recovery fails
        final result1 = await recovery.triggerManualRecovery(
          strategy: RecoveryStrategy.lightCleanup,
        );
        expect(result1.succeeded, isFalse);

        // Reset mock to succeed
        when(() => mockCacheService.cleanupCacheAsync()).thenAnswer((_) async {});

        // Second recovery should work
        final result2 = await recovery.triggerManualRecovery(
          strategy: RecoveryStrategy.lightCleanup,
        );
        expect(result2.succeeded, isTrue);
      });
    });

    group('Performance Tests', () {
      test('should monitor memory efficiently', () async {
        final stopwatch = Stopwatch()..start();

        // Let monitoring run for a short period
        await Future.delayed(const Duration(milliseconds: 500));

        stopwatch.stop();

        // Monitoring should not use excessive CPU
        final stats = recovery.currentStats;
        expect(stats, isNotNull);
        expect(stopwatch.elapsedMilliseconds, lessThan(1000));
      });

      test('should execute recovery quickly', () async {
        final stopwatch = Stopwatch()..start();

        await recovery.triggerManualRecovery(
          strategy: RecoveryStrategy.lightCleanup,
        );

        stopwatch.stop();

        // Recovery should complete quickly
        expect(stopwatch.elapsedMilliseconds, lessThan(500));
      });

      test('should handle concurrent recovery requests', () async {
        final futures = <Future<RecoveryResult>>[];

        // Submit multiple concurrent recovery requests
        for (int i = 0; i < 5; i++) {
          futures.add(recovery.triggerManualRecovery(
            strategy: RecoveryStrategy.lightCleanup,
          ));
        }

        final results = await Future.wait(futures);

        expect(results, hasLength(5));
        // All should complete, but some might be no-ops if recovery is already running
        for (final result in results) {
          expect(result, isA<RecoveryResult>());
        }
      });
    });

    group('Resource Management', () {
      test('should dispose cleanly', () {
        expect(() => recovery.dispose(), returnsNormally);
      });

      test('should stop monitoring after disposal', () async {
        recovery.dispose();

        // Wait a bit to ensure no more updates
        await Future.delayed(const Duration(milliseconds: 200));

        // Should not crash accessing disposed resources
        expect(() => recovery.recoveryStats, returnsNormally);
      });

      test('should handle multiple disposals', () {
        recovery.dispose();
        expect(() => recovery.dispose(), returnsNormally);
      });
    });

    group('Edge Cases', () {
      test('should handle zero memory usage', () async {
        when(() => mockCacheService.getStats()).thenAnswer((_) async => 
            const CacheStats(
              totalItems: 0,
              memoryUsage: 0.0,
              diskUsage: 0.0,
              hitCount: 0,
              missCount: 0,
              hitRate: 0.0,
            ));

        final statsStream = recovery.memoryStatsStream;
        final stats = await statsStream.first;

        expect(stats.pressureLevel, equals(MemoryPressureLevel.normal));
        expect(stats.usedMemoryMB, equals(0));
      });

      test('should handle very high memory usage', () async {
        when(() => mockCacheService.getStats()).thenAnswer((_) async => 
            const CacheStats(
              totalItems: 1000,
              memoryUsage: 200.0, // Very high
              diskUsage: 50.0,
              hitCount: 1000,
              missCount: 200,
              hitRate: 0.83,
            ));

        final statsStream = recovery.memoryStatsStream;
        final stats = await statsStream.first;

        expect(stats.pressureLevel, equals(MemoryPressureLevel.emergency));
      });

      test('should handle rapid memory fluctuations', () async {
        var memoryLevel = 0;
        when(() => mockCacheService.getStats()).thenAnswer((_) async {
          memoryLevel = (memoryLevel + 10) % 100; // Fluctuating memory
          return CacheStats(
            totalItems: memoryLevel ~/ 2,
            memoryUsage: memoryLevel.toDouble(),
            diskUsage: 5.0,
            hitCount: 100,
            missCount: 20,
            hitRate: 0.83,
          );
        });

        final statsStream = recovery.memoryStatsStream;
        final stats = <MemoryStats>[];

        final subscription = statsStream.listen(stats.add);

        // Wait for several updates
        await Future.delayed(const Duration(milliseconds: 500));

        expect(stats, isNotEmpty);
        expect(stats.length, greaterThan(3));

        await subscription.cancel();
      });
    });
  });

  group('MemoryStats', () {
    test('should create stats with correct properties', () {
      final now = DateTime.now();
      const stats = MemoryStats(
        totalMemoryMB: 100,
        usedMemoryMB: 75,
        cacheMemoryMB: 50,
        usagePercentage: 75.0,
        pressureLevel: MemoryPressureLevel.critical,
        timestamp: now,
      );

      expect(stats.totalMemoryMB, equals(100));
      expect(stats.usedMemoryMB, equals(75));
      expect(stats.cacheMemoryMB, equals(50));
      expect(stats.usagePercentage, equals(75.0));
      expect(stats.pressureLevel, equals(MemoryPressureLevel.critical));
      expect(stats.timestamp, equals(now));
    });

    test('should provide meaningful string representation', () {
      const stats = MemoryStats(
        totalMemoryMB: 100,
        usedMemoryMB: 75,
        cacheMemoryMB: 50,
        usagePercentage: 75.0,
        pressureLevel: MemoryPressureLevel.critical,
        timestamp: DateTime.now(),
      );

      final stringRep = stats.toString();
      expect(stringRep, contains('MemoryStats'));
      expect(stringRep, contains('75MB'));
      expect(stringRep, contains('50MB'));
      expect(stringRep, contains('critical'));
      expect(stringRep, contains('75.0%'));
    });
  });

  group('RecoveryResult', () {
    test('should create result with all properties', () {
      const result = RecoveryResult(
        memoryFreedMB: 25,
        strategy: RecoveryStrategy.aggressiveCleanup,
        duration: Duration(milliseconds: 150),
        succeeded: true,
      );

      expect(result.memoryFreedMB, equals(25));
      expect(result.strategy, equals(RecoveryStrategy.aggressiveCleanup));
      expect(result.duration, equals(Duration(milliseconds: 150)));
      expect(result.succeeded, isTrue);
      expect(result.error, isNull);
    });

    test('should create failed result with error', () {
      const result = RecoveryResult(
        memoryFreedMB: 0,
        strategy: RecoveryStrategy.lightCleanup,
        duration: Duration(milliseconds: 50),
        succeeded: false,
        error: 'Recovery failed',
      );

      expect(result.succeeded, isFalse);
      expect(result.error, equals('Recovery failed'));
    });
  });
}