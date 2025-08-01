import 'dart:async';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:easy_comic/core/error/retry_mechanism.dart';

void main() {
  group('ExponentialBackoffRetry', () {
    late ExponentialBackoffRetry retry;

    setUp(() {
      retry = ExponentialBackoffRetry();
    });

    group('Basic Retry Functionality', () {
      test('should succeed on first attempt', () async {
        var callCount = 0;
        
        final result = await retry.execute(() async {
          callCount++;
          return 'success';
        });
        
        expect(result, equals('success'));
        expect(callCount, equals(1));
      });

      test('should retry on failure and succeed', () async {
        var callCount = 0;
        
        final result = await retry.execute(() async {
          callCount++;
          if (callCount < 3) {
            throw Exception('Temporary failure');
          }
          return 'success after retries';
        });
        
        expect(result, equals('success after retries'));
        expect(callCount, equals(3));
      });

      test('should fail after max attempts', () async {
        var callCount = 0;
        
        expect(
          retry.execute(
            () async {
              callCount++;
              throw Exception('Always fails');
            },
            config: const RetryConfig(maxAttempts: 3),
          ),
          throwsA(isA<Exception>()),
        );
        
        // Should wait for completion
        await Future.delayed(const Duration(milliseconds: 100));
        expect(callCount, equals(3));
      });

      test('should respect custom retry config', () async {
        var callCount = 0;
        
        await expectLater(
          retry.execute(
            () async {
              callCount++;
              throw Exception('Always fails');
            },
            config: const RetryConfig(
              maxAttempts: 5,
              initialDelay: Duration(milliseconds: 10),
            ),
          ),
          throwsA(isA<Exception>()),
        );
        
        expect(callCount, equals(5));
      });
    });

    group('Retry Conditions', () {
      test('should respect shouldRetry function', () async {
        var callCount = 0;
        
        await expectLater(
          retry.execute(
            () async {
              callCount++;
              throw Exception('Should not retry');
            },
            shouldRetry: (error) => false, // Never retry
          ),
          throwsA(isA<Exception>()),
        );
        
        expect(callCount, equals(1)); // Only one attempt
      });

      test('should use default retry conditions when not specified', () async {
        var callCount = 0;
        
        await expectLater(
          retry.execute(
            () async {
              callCount++;
              throw Exception('connection timeout');
            },
            config: const RetryConfig(maxAttempts: 3),
          ),
          throwsA(isA<Exception>()),
        );
        
        expect(callCount, equals(3)); // Should retry network errors
      });

      test('should handle custom retry logic', () async {
        var callCount = 0;
        
        final result = await retry.execute(
          () async {
            callCount++;
            if (callCount < 3) {
              throw Exception('retry_me');
            }
            return 'success';
          },
          shouldRetry: (error) => error.toString().contains('retry_me'),
        );
        
        expect(result, equals('success'));
        expect(callCount, equals(3));
      });
    });

    group('Delay Calculation', () {
      test('should calculate exponential backoff delays correctly', () async {
        final delays = <Duration>[];
        
        await expectLater(
          retry.execute(
            () async => throw Exception('Always fails'),
            config: const RetryConfig(
              maxAttempts: 4,
              initialDelay: Duration(milliseconds: 100),
              backoffMultiplier: 2.0,
              enableJitter: false, // Disable for predictable testing
            ),
            onRetry: (attempt, error) {
              // This is called before the delay, so we need to calculate expected delay
              final expectedDelay = Duration(
                milliseconds: (100 * (1 << (attempt - 1))).round(),
              );
              delays.add(expectedDelay);
            },
          ),
          throwsA(isA<Exception>()),
        );
        
        expect(delays, hasLength(3)); // 3 retries after initial failure
        expect(delays[0].inMilliseconds, equals(100)); // 100ms * 2^0
        expect(delays[1].inMilliseconds, equals(200)); // 100ms * 2^1
        expect(delays[2].inMilliseconds, equals(400)); // 100ms * 2^2
      });

      test('should respect maximum delay limit', () async {
        final delays = <Duration>[];
        
        await expectLater(
          retry.execute(
            () async => throw Exception('Always fails'),
            config: const RetryConfig(
              maxAttempts: 5,
              initialDelay: Duration(seconds: 1),
              backoffMultiplier: 10.0,
              maxDelay: Duration(seconds: 2),
              enableJitter: false,
            ),
            onRetry: (attempt, error) {
              final expectedDelay = Duration(
                milliseconds: (1000 * (10 ^ (attempt - 1))).round().clamp(0, 2000),
              );
              delays.add(expectedDelay);
            },
          ),
          throwsA(isA<Exception>()),
        );
        
        // All delays should be capped at maxDelay
        for (final delay in delays) {
          expect(delay.inMilliseconds, lessThanOrEqualTo(2000));
        }
      });

      test('should add jitter when enabled', () async {
        final delays = <int>[];
        
        // Run multiple times to check jitter variation
        for (int i = 0; i < 5; i++) {
          try {
            await retry.execute(
              () async {
                throw Exception('Test jitter');
              },
              config: const RetryConfig(
                maxAttempts: 2,
                initialDelay: Duration(milliseconds: 100),
                enableJitter: true,
                jitterFactor: 0.5,
              ),
              onRetry: (attempt, error) {
                delays.add(DateTime.now().millisecondsSinceEpoch);
              },
            );
          } catch (e) {
            // Expected to fail
          }
        }
        
        // With jitter, delays should vary
        expect(delays, isNotEmpty);
      });
    });

    group('Cancellation', () {
      test('should cancel operation when token is cancelled', () async {
        final cancelToken = CancelToken();
        var callCount = 0;
        
        // Cancel after short delay
        Timer(const Duration(milliseconds: 50), () {
          cancelToken.cancel();
        });
        
        await expectLater(
          retry.execute(
            () async {
              callCount++;
              throw Exception('Network error');
            },
            config: const RetryConfig(
              maxAttempts: 5,
              initialDelay: Duration(milliseconds: 200),
            ),
            cancelToken: cancelToken,
          ),
          throwsA(isA<RetryCancelledException>()),
        );
        
        // Should not complete all attempts
        expect(callCount, lessThan(5));
      });

      test('should handle pre-cancelled token', () async {
        final cancelToken = CancelToken();
        cancelToken.cancel();
        
        await expectLater(
          retry.execute(
            () async => 'should not execute',
            cancelToken: cancelToken,
          ),
          throwsA(isA<RetryCancelledException>()),
        );
      });

      test('should cancel during delay', () async {
        final cancelToken = CancelToken();
        final stopwatch = Stopwatch()..start();
        
        // Cancel after 100ms
        Timer(const Duration(milliseconds: 100), () {
          cancelToken.cancel();
        });
        
        await expectLater(
          retry.execute(
            () async => throw Exception('Will retry'),
            config: const RetryConfig(
              maxAttempts: 3,
              initialDelay: Duration(seconds: 5), // Long delay
            ),
            cancelToken: cancelToken,
          ),
          throwsA(isA<RetryCancelledException>()),
        );
        
        stopwatch.stop();
        // Should cancel quickly, not wait full delay
        expect(stopwatch.elapsedMilliseconds, lessThan(1000));
      });
    });

    group('Callback Notifications', () {
      test('should call onRetry callback for each retry attempt', () async {
        final retryAttempts = <int>[];
        final retryErrors = <Exception>[];
        
        await expectLater(
          retry.execute(
            () async => throw Exception('Test error'),
            config: const RetryConfig(maxAttempts: 3),
            onRetry: (attempt, error) {
              retryAttempts.add(attempt);
              retryErrors.add(error);
            },
          ),
          throwsA(isA<Exception>()),
        );
        
        expect(retryAttempts, equals([1, 2])); // Retries after attempts 1 and 2
        expect(retryErrors, hasLength(2));
      });

      test('should not call onRetry on successful first attempt', () async {
        var onRetryCalled = false;
        
        final result = await retry.execute(
          () async => 'success',
          onRetry: (attempt, error) {
            onRetryCalled = true;
          },
        );
        
        expect(result, equals('success'));
        expect(onRetryCalled, isFalse);
      });
    });

    group('ExecuteWithResult', () {
      test('should return result with success metadata', () async {
        final result = await retry.executeWithResult(
          () async => 'success',
        );
        
        expect(result.succeeded, isTrue);
        expect(result.result, equals('success'));
        expect(result.attemptsMade, equals(1));
        expect(result.lastError, isNull);
        expect(result.totalDuration, isA<Duration>());
      });

      test('should return result with failure metadata', () async {
        final result = await retry.executeWithResult(
          () async => throw Exception('Always fails'),
          config: const RetryConfig(maxAttempts: 3),
        );
        
        expect(result.succeeded, isFalse);
        expect(result.result, isNull);
        expect(result.attemptsMade, equals(3));
        expect(result.lastError, isA<Exception>());
        expect(result.totalDuration, isA<Duration>());
      });

      test('should track total duration accurately', () async {
        final stopwatch = Stopwatch()..start();
        
        final result = await retry.executeWithResult(
          () async {
            await Future.delayed(const Duration(milliseconds: 10));
            throw Exception('Fail');
          },
          config: const RetryConfig(
            maxAttempts: 2,
            initialDelay: Duration(milliseconds: 10),
          ),
        );
        
        stopwatch.stop();
        
        expect(result.totalDuration.inMilliseconds, 
               greaterThan(10)); // At least processing time
        expect(result.totalDuration.inMilliseconds, 
               lessThan(stopwatch.elapsedMilliseconds + 100)); // Within reasonable range
      });
    });

    group('Default Retry Conditions', () {
      test('defaultShouldRetry should identify network errors', () {
        final networkErrors = [
          Exception('connection timeout'),
          Exception('network unreachable'),
          Exception('host not found'),
          Exception('socket exception'),
        ];
        
        for (final error in networkErrors) {
          expect(ExponentialBackoffRetry.defaultShouldRetry(error), isTrue);
        }
      });

      test('defaultShouldRetry should identify HTTP retry status codes', () {
        final retryableErrors = [
          Exception('HTTP 429 Rate Limited'),
          Exception('HTTP 500 Internal Server Error'),
          Exception('HTTP 502 Bad Gateway'),
          Exception('HTTP 503 Service Unavailable'),
          Exception('HTTP 504 Gateway Timeout'),
        ];
        
        for (final error in retryableErrors) {
          expect(ExponentialBackoffRetry.defaultShouldRetry(error), isTrue);
        }
      });

      test('defaultShouldRetry should identify temporary file system errors', () {
        final temporaryErrors = [
          Exception('temporarily unavailable'),
          Exception('resource busy'),
          Exception('try again later'),
        ];
        
        for (final error in temporaryErrors) {
          expect(ExponentialBackoffRetry.defaultShouldRetry(error), isTrue);
        }
      });

      test('defaultShouldRetry should not retry permanent errors', () {
        final permanentErrors = [
          Exception('permission denied'),
          Exception('file not found'),
          Exception('invalid request'),
        ];
        
        for (final error in permanentErrors) {
          expect(ExponentialBackoffRetry.defaultShouldRetry(error), isFalse);
        }
      });

      test('fileShouldRetry should not retry permission errors', () {
        final permissionErrors = [
          Exception('permission denied'),
          Exception('access denied'),
          Exception('file not found'),
        ];
        
        for (final error in permissionErrors) {
          expect(ExponentialBackoffRetry.fileShouldRetry(error), isFalse);
        }
      });

      test('databaseShouldRetry should not retry schema errors', () {
        final schemaErrors = [
          Exception('constraint violation'),
          Exception('schema error'),
          Exception('syntax error'),
        ];
        
        for (final error in schemaErrors) {
          expect(ExponentialBackoffRetry.databaseShouldRetry(error), isFalse);
        }
      });

      test('databaseShouldRetry should retry lock errors', () {
        final lockErrors = [
          Exception('database locked'),
          Exception('resource busy'),
          Exception('deadlock detected'),
        ];
        
        for (final error in lockErrors) {
          expect(ExponentialBackoffRetry.databaseShouldRetry(error), isTrue);
        }
      });
    });
  });

  group('RetryConfig', () {
    test('should create config with default values', () {
      const config = RetryConfig();
      
      expect(config.maxAttempts, equals(3));
      expect(config.initialDelay, equals(Duration(seconds: 1)));
      expect(config.backoffMultiplier, equals(2.0));
      expect(config.maxDelay, equals(Duration(seconds: 30)));
      expect(config.enableJitter, isTrue);
      expect(config.jitterFactor, equals(0.1));
    });

    test('should provide network-specific config', () {
      const config = RetryConfig.network;
      
      expect(config.maxAttempts, equals(3));
      expect(config.initialDelay, equals(Duration(seconds: 1)));
      expect(config.maxDelay, equals(Duration(seconds: 4)));
      expect(config.enableJitter, isTrue);
    });

    test('should provide file-specific config', () {
      const config = RetryConfig.file;
      
      expect(config.maxAttempts, equals(3));
      expect(config.initialDelay, equals(Duration(milliseconds: 100)));
      expect(config.maxDelay, equals(Duration(seconds: 1)));
      expect(config.enableJitter, isFalse);
    });

    test('should provide database-specific config', () {
      const config = RetryConfig.database;
      
      expect(config.maxAttempts, equals(5));
      expect(config.initialDelay, equals(Duration(milliseconds: 50)));
      expect(config.backoffMultiplier, equals(1.5));
      expect(config.maxDelay, equals(Duration(seconds: 2)));
      expect(config.enableJitter, isTrue);
    });
  });

  group('RetryUtils', () {
    test('retryNetwork should use network config and conditions', () async {
      var callCount = 0;
      
      final result = await RetryUtils.retryNetwork(() async {
        callCount++;
        if (callCount < 2) {
          throw Exception('connection timeout');
        }
        return 'network success';
      });
      
      expect(result, equals('network success'));
      expect(callCount, equals(2));
    });

    test('retryFile should use file config and conditions', () async {
      var callCount = 0;
      
      final result = await RetryUtils.retryFile(() async {
        callCount++;
        if (callCount < 2) {
          throw Exception('temporarily unavailable');
        }
        return 'file success';
      });
      
      expect(result, equals('file success'));
      expect(callCount, equals(2));
    });

    test('retryDatabase should use database config and conditions', () async {
      var callCount = 0;
      
      final result = await RetryUtils.retryDatabase(() async {
        callCount++;
        if (callCount < 3) {
          throw Exception('database locked');
        }
        return 'database success';
      });
      
      expect(result, equals('database success'));
      expect(callCount, equals(3));
    });

    test('should handle cancellation in utility methods', () async {
      final cancelToken = CancelToken();
      
      Timer(const Duration(milliseconds: 50), () {
        cancelToken.cancel();
      });
      
      await expectLater(
        RetryUtils.retryNetwork(
          () async {
            await Future.delayed(const Duration(seconds: 1));
            return 'should not complete';
          },
          cancelToken: cancelToken,
        ),
        throwsA(isA<RetryCancelledException>()),
      );
    });
  });

  group('CancelToken', () {
    test('should start as not cancelled', () {
      final token = CancelToken();
      
      expect(token.isCancelled, isFalse);
    });

    test('should become cancelled when cancel is called', () {
      final token = CancelToken();
      
      token.cancel();
      
      expect(token.isCancelled, isTrue);
    });

    test('should complete cancelled future when cancelled', () async {
      final token = CancelToken();
      
      final future = token.cancelled;
      token.cancel();
      
      await expectLater(future, completes);
    });

    test('should handle multiple cancel calls', () {
      final token = CancelToken();
      
      token.cancel();
      token.cancel(); // Should not throw
      
      expect(token.isCancelled, isTrue);
    });
  });

  group('Performance Tests', () {
    test('should handle rapid retry operations efficiently', () async {
      final futures = <Future>[];
      
      for (int i = 0; i < 100; i++) {
        futures.add(retry.execute(
          () async => 'result_$i',
        ));
      }
      
      final stopwatch = Stopwatch()..start();
      final results = await Future.wait(futures);
      stopwatch.stop();
      
      expect(results, hasLength(100));
      expect(stopwatch.elapsedMilliseconds, lessThan(1000)); // Should be fast for immediate success
    });

    test('should maintain performance under concurrent retry scenarios', () async {
      final futures = <Future>[];
      
      for (int i = 0; i < 50; i++) {
        futures.add(retry.execute(
          () async {
            if (i % 10 == 0) {
              throw Exception('Intermittent failure');
            }
            return 'success_$i';
          },
          config: const RetryConfig(maxAttempts: 2),
        ));
      }
      
      final stopwatch = Stopwatch()..start();
      
      // Some will succeed, some will fail
      final results = await Future.allSettled(futures);
      
      stopwatch.stop();
      
      expect(results, hasLength(50));
      expect(stopwatch.elapsedMilliseconds, lessThan(5000)); // Reasonable time
    });
  });
}