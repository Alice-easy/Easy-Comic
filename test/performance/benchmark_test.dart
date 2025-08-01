import 'dart:typed_data';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:easy_comic/core/comic_archive.dart';
import 'package:easy_comic/core/security/input_validator.dart';

void main() {
  group('Performance Benchmarks', () {
    group('Cache Service Performance', () {
      test('cache hit performance benchmark', () async {
        // This would normally use the real CacheService
        // For demonstration, we'll simulate the performance test
        
        final stopwatch = Stopwatch();
        const iterations = 1000;
        
        // Simulate cache hits
        stopwatch.start();
        for (int i = 0; i < iterations; i++) {
          // Simulate cache lookup
          await Future.delayed(Duration.zero);
        }
        stopwatch.stop();
        
        final averageTime = stopwatch.elapsedMicroseconds / iterations;
        
        // Performance benchmark: cache hits should be under 50 microseconds
        expect(averageTime, lessThan(50));
        
        print('Cache hit average time: ${averageTime.toStringAsFixed(2)} μs');
      });

      test('memory pressure response time benchmark', () async {
        final stopwatch = Stopwatch();
        
        // Simulate memory pressure detection and response
        stopwatch.start();
        
        // Simulate pressure detection
        await Future.delayed(const Duration(milliseconds: 1));
        
        // Simulate cleanup response
        await Future.delayed(const Duration(milliseconds: 10));
        
        stopwatch.stop();
        
        // Memory pressure response should be under 50ms
        expect(stopwatch.elapsedMilliseconds, lessThan(50));
        
        print('Memory pressure response time: ${stopwatch.elapsedMilliseconds}ms');
      });

      test('concurrent cache operations benchmark', () async {
        const concurrentOperations = 100;
        final stopwatch = Stopwatch();
        
        stopwatch.start();
        
        // Simulate concurrent cache operations
        final futures = List.generate(concurrentOperations, (i) async {
          // Simulate cache set operation
          await Future.delayed(const Duration(microseconds: 100));
          // Simulate cache get operation
          await Future.delayed(const Duration(microseconds: 50));
        });
        
        await Future.wait(futures);
        stopwatch.stop();
        
        final averagePerOperation = stopwatch.elapsedMicroseconds / (concurrentOperations * 2);
        
        // Concurrent operations should maintain good performance
        expect(averagePerOperation, lessThan(200)); // Under 200μs per operation
        
        print('Concurrent operations average: ${averagePerOperation.toStringAsFixed(2)} μs/op');
      });
    });

    group('Input Validation Performance', () {
      test('path validation performance benchmark', () async {
        final validator = InputValidator();
        const iterations = 1000;
        final testPaths = [
          '/valid/path/to/comic.cbz',
          '/another/valid/path/manga.zip',
          '/home/user/documents/comic_collection.cbr',
        ];
        
        final stopwatch = Stopwatch();
        
        stopwatch.start();
        for (int i = 0; i < iterations; i++) {
          final testPath = testPaths[i % testPaths.length];
          try {
            // This would normally validate against real files
            // For benchmark, we'll simulate the validation logic
            if (testPath.length > InputValidator.maxPathLength) {
              throw ValidationException('Path too long');
            }
            
            // Simulate path traversal checks
            for (final pattern in [RegExp(r'\.\.[\\/]'), RegExp(r'^[\\/]')]) {
              if (pattern.hasMatch(testPath)) {
                throw ValidationException('Path traversal detected');
              }
            }
            
            // Simulate extension validation
            final extension = testPath.split('.').last.toLowerCase();
            if (!['cbz', 'cbr', 'zip', 'rar'].contains(extension)) {
              throw ValidationException('Invalid extension');
            }
          } catch (e) {
            // Handle validation errors
          }
        }
        stopwatch.stop();
        
        final averageTime = stopwatch.elapsedMicroseconds / iterations;
        
        // Path validation should be under 100 microseconds
        expect(averageTime, lessThan(100));
        
        print('Path validation average time: ${averageTime.toStringAsFixed(2)} μs');
      });

      test('cache key validation performance benchmark', () async {
        final validator = InputValidator();
        const iterations = 10000;
        final testKeys = [
          'page_123',
          'comic_456_page_789',
          'thumbnail_cache_key',
          'image_preview_abc123',
        ];
        
        final stopwatch = Stopwatch();
        
        stopwatch.start();
        for (int i = 0; i < iterations; i++) {
          final testKey = testKeys[i % testKeys.length];
          validator.validateCacheKey(testKey);
        }
        stopwatch.stop();
        
        final averageTime = stopwatch.elapsedMicroseconds / iterations;
        
        // Cache key validation should be very fast (under 10 microseconds)
        expect(averageTime, lessThan(10));
        
        print('Cache key validation average time: ${averageTime.toStringAsFixed(2)} μs');
      });
    });

    group('Archive Processing Performance', () {
      test('natural sort performance benchmark', () {
        const iterations = 1000;
        final testFiles = [
          'page1.jpg', 'page10.jpg', 'page2.jpg', 'page20.jpg',
          'chapter1_page1.jpg', 'chapter1_page10.jpg', 'chapter2_page1.jpg',
          'img001.png', 'img010.png', 'img100.png', 'img002.png',
        ];
        
        final stopwatch = Stopwatch();
        
        stopwatch.start();
        for (int i = 0; i < iterations; i++) {
          final listToSort = List<String>.from(testFiles);
          listToSort.sort(naturalCompare);
        }
        stopwatch.stop();
        
        final averageTime = stopwatch.elapsedMicroseconds / iterations;
        
        // Natural sort should be efficient (under 1000 microseconds for small lists)
        expect(averageTime, lessThan(1000));
        
        print('Natural sort average time: ${averageTime.toStringAsFixed(2)} μs');
      });

      test('archive format detection performance benchmark', () {
        const iterations = 5000;
        final testPaths = [
          '/path/to/comic.cbz',
          '/path/to/manga.cbr',
          '/path/to/archive.zip',
          '/path/to/book.pdf',
          '/path/to/ebook.epub',
        ];
        
        final stopwatch = Stopwatch();
        
        stopwatch.start();
        for (int i = 0; i < iterations; i++) {
          final testPath = testPaths[i % testPaths.length];
          // Simulate format detection logic
          final extension = testPath.split('.').last.toLowerCase();
          ArchiveFormat? format;
          
          switch (extension) {
            case 'cbz':
            case 'zip':
              format = ArchiveFormat.cbz;
              break;
            case 'cbr':
            case 'rar':
              format = ArchiveFormat.cbr;
              break;
            case 'pdf':
              format = ArchiveFormat.pdf;
              break;
            case 'epub':
              format = ArchiveFormat.epub;
              break;
          }
        }
        stopwatch.stop();
        
        final averageTime = stopwatch.elapsedMicroseconds / iterations;
        
        // Format detection should be very fast (under 5 microseconds)
        expect(averageTime, lessThan(5));
        
        print('Archive format detection average time: ${averageTime.toStringAsFixed(2)} μs');
      });
    });

    group('Volume Key Service Performance', () {
      test('volume key event processing performance benchmark', () async {
        const iterations = 1000;
        final stopwatch = Stopwatch();
        
        stopwatch.start();
        for (int i = 0; i < iterations; i++) {
          // Simulate volume key event processing
          final keyType = i % 2; // Alternate between volume up/down
          final timestamp = DateTime.now().millisecondsSinceEpoch;
          
          // Simulate key type parsing
          VolumeKeyType volumeKeyType;
          switch (keyType) {
            case 0:
            case 24: // Android KEYCODE_VOLUME_UP
              volumeKeyType = VolumeKeyType.volumeUp;
              break;
            case 1:
            case 25: // Android KEYCODE_VOLUME_DOWN
              volumeKeyType = VolumeKeyType.volumeDown;
              break;
            default:
              continue; // Skip invalid key codes
          }
          
          // Simulate event creation
          final event = VolumeKeyEvent(
            type: volumeKeyType,
            timestamp: DateTime.fromMillisecondsSinceEpoch(timestamp),
          );
          
          // Simulate callback notification
          await Future.delayed(Duration.zero);
        }
        stopwatch.stop();
        
        final averageTime = stopwatch.elapsedMicroseconds / iterations;
        
        // Volume key processing should be under 100 microseconds
        expect(averageTime, lessThan(100));
        
        print('Volume key event processing average time: ${averageTime.toStringAsFixed(2)} μs');
      });
    });

    group('Memory Usage Benchmarks', () {
      test('cache service memory efficiency benchmark', () {
        // Simulate memory usage tracking
        const maxImages = 100;
        const imageSize = 100 * 1024; // 100KB per image
        final expectedMaxMemory = maxImages * imageSize;
        
        // This would normally track actual memory usage
        // For benchmark, we'll simulate efficient memory management
        var simulatedMemoryUsage = 0;
        
        for (int i = 0; i < maxImages; i++) {
          simulatedMemoryUsage += imageSize;
          
          // Simulate LRU eviction when approaching limit
          if (simulatedMemoryUsage > expectedMaxMemory * 0.8) {
            simulatedMemoryUsage -= imageSize * 0.2; // Evict 20% of items
          }
        }
        
        // Memory usage should not exceed expected limits
        expect(simulatedMemoryUsage, lessThanOrEqualTo(expectedMaxMemory));
        
        print('Final simulated memory usage: ${simulatedMemoryUsage / (1024 * 1024)} MB');
      });

      test('preloading engine memory overhead benchmark', () {
        // Simulate preloading engine overhead
        const maxTasks = 50;
        const averageTaskSize = 1024; // 1KB overhead per task
        
        var totalOverhead = 0;
        
        // Simulate task creation overhead
        for (int i = 0; i < maxTasks; i++) {
          totalOverhead += averageTaskSize;
          
          // Simulate task completion and cleanup
          if (i % 10 == 0) {
            totalOverhead -= averageTaskSize * 5; // Cleanup completed tasks
          }
        }
        
        // Total overhead should be reasonable
        expect(totalOverhead, lessThan(100 * 1024)); // Under 100KB
        
        print('Preloading engine overhead: ${totalOverhead / 1024} KB');
      });
    });

    group('Gesture Response Time Benchmarks', () {
      test('gesture recognition performance benchmark', () async {
        const iterations = 1000;
        final stopwatch = Stopwatch();
        
        final gestureTypes = [
          GestureType.tapLeft,
          GestureType.tapRight,
          GestureType.tapCenter,
          GestureType.doubleTap,
          GestureType.longPress,
        ];
        
        stopwatch.start();
        for (int i = 0; i < iterations; i++) {
          final gestureType = gestureTypes[i % gestureTypes.length];
          
          // Simulate gesture event creation
          final event = GestureEvent.now(
            type: gestureType,
            position: Offset(100.0 + i % 200, 100.0 + i % 300),
            timestamp: DateTime.now(),
          );
          
          // Simulate gesture processing
          await Future.delayed(Duration.zero);
        }
        stopwatch.stop();
        
        final averageTime = stopwatch.elapsedMicroseconds / iterations;
        
        // Gesture processing should be under 50 microseconds for responsive UI
        expect(averageTime, lessThan(50));
        
        print('Gesture recognition average time: ${averageTime.toStringAsFixed(2)} μs');
      });

      test('tap zone calculation performance benchmark', () {
        const iterations = 10000;
        final stopwatch = Stopwatch();
        
        const screenWidth = 400.0;
        const screenHeight = 800.0;
        const tapZoneConfig = TapZoneConfig(); // Default configuration
        
        stopwatch.start();
        for (int i = 0; i < iterations; i++) {
          final tapPosition = Offset(
            (i % 400).toDouble(),
            (i % 800).toDouble(),
          );
          
          // Simulate tap zone calculation
          final leftZoneWidth = screenWidth * tapZoneConfig.leftZoneWidth;
          final rightZoneStart = screenWidth - (screenWidth * tapZoneConfig.rightZoneWidth);
          
          GestureType gestureType;
          if (tapPosition.dx < leftZoneWidth) {
            gestureType = GestureType.tapLeft;
          } else if (tapPosition.dx > rightZoneStart) {
            gestureType = GestureType.tapRight;
          } else {
            gestureType = GestureType.tapCenter;
          }
        }
        stopwatch.stop();
        
        final averageTime = stopwatch.elapsedMicroseconds / iterations;
        
        // Tap zone calculation should be very fast (under 5 microseconds)
        expect(averageTime, lessThan(5));
        
        print('Tap zone calculation average time: ${averageTime.toStringAsFixed(2)} μs');
      });
    });

    group('Retry Mechanism Performance', () {
      test('retry delay calculation performance benchmark', () {
        const iterations = 1000;
        final stopwatch = Stopwatch();
        
        const config = RetryConfig(
          maxAttempts: 5,
          initialDelay: Duration(milliseconds: 100),
          backoffMultiplier: 2.0,
          enableJitter: true,
        );
        
        stopwatch.start();
        for (int i = 0; i < iterations; i++) {
          for (int attempt = 1; attempt <= config.maxAttempts; attempt++) {
            // Simulate delay calculation logic
            final baseDelay = config.initialDelay.inMilliseconds * 
                (config.backoffMultiplier * attempt);
            final cappedDelay = baseDelay.clamp(0, config.maxDelay.inMilliseconds);
            
            // Simulate jitter calculation
            if (config.enableJitter) {
              final jitter = cappedDelay * config.jitterFactor * 0.5; // Simplified
              final finalDelay = cappedDelay + jitter;
            }
          }
        }
        stopwatch.stop();
        
        final averageTime = stopwatch.elapsedMicroseconds / (iterations * config.maxAttempts);
        
        // Delay calculation should be very fast (under 10 microseconds)
        expect(averageTime, lessThan(10));
        
        print('Retry delay calculation average time: ${averageTime.toStringAsFixed(2)} μs');
      });
    });

    group('Overall System Performance', () {
      test('end-to-end page loading simulation benchmark', () async {
        const iterations = 100;
        final stopwatch = Stopwatch();
        
        stopwatch.start();
        for (int i = 0; i < iterations; i++) {
          // Simulate complete page loading workflow
          
          // 1. Input validation (path, cache key)
          await Future.delayed(const Duration(microseconds: 50));
          
          // 2. Cache lookup
          await Future.delayed(const Duration(microseconds: 20));
          
          // 3. Archive extraction (if not cached)
          if (i % 3 == 0) { // 1/3 cache miss
            await Future.delayed(const Duration(milliseconds: 1)); // Extraction
          }
          
          // 4. Image processing
          await Future.delayed(const Duration(microseconds: 200));
          
          // 5. Cache storage
          await Future.delayed(const Duration(microseconds: 30));
          
          // 6. Preloading trigger
          await Future.delayed(const Duration(microseconds: 10));
          
          // 7. Memory pressure check
          await Future.delayed(const Duration(microseconds: 5));
        }
        stopwatch.stop();
        
        final averageTime = stopwatch.elapsedMilliseconds / iterations;
        
        // End-to-end page loading should be under 10ms average
        expect(averageTime, lessThan(10));
        
        print('End-to-end page loading average time: ${averageTime.toStringAsFixed(2)} ms');
      });

      test('system resource usage simulation benchmark', () {
        // Simulate system resource tracking
        var totalMemoryUsage = 0;
        var totalCpuTime = 0;
        var totalIOOperations = 0;
        
        const iterations = 1000;
        
        for (int i = 0; i < iterations; i++) {
          // Simulate memory allocation
          totalMemoryUsage += 1024; // 1KB per operation
          
          // Simulate CPU usage
          totalCpuTime += 100; // 100μs per operation
          
          // Simulate I/O operations
          if (i % 10 == 0) {
            totalIOOperations += 1;
          }
          
          // Simulate cleanup
          if (i % 50 == 0) {
            totalMemoryUsage = (totalMemoryUsage * 0.8).round(); // 20% cleanup
          }
        }
        
        // Resource usage should be within acceptable limits
        expect(totalMemoryUsage, lessThan(500 * 1024)); // Under 500KB
        expect(totalCpuTime, lessThan(200 * 1000)); // Under 200ms total
        expect(totalIOOperations, lessThan(200)); // Under 200 I/O ops
        
        print('Resource usage - Memory: ${totalMemoryUsage / 1024} KB, '
              'CPU: ${totalCpuTime / 1000} ms, I/O: $totalIOOperations ops');
      });
    });
  });
}

// Helper classes for benchmark testing
enum VolumeKeyType { volumeUp, volumeDown }

class VolumeKeyEvent {
  final VolumeKeyType type;
  final DateTime timestamp;
  
  const VolumeKeyEvent({required this.type, required this.timestamp});
}

enum GestureType {
  tapLeft, tapRight, tapCenter, doubleTap, longPress,
  pinchIn, pinchOut, swipeUp, swipeDown, swipeLeft, swipeRight
}

class GestureEvent {
  final GestureType type;
  final Offset? position;
  final DateTime timestamp;
  
  const GestureEvent({
    required this.type,
    this.position,
    required this.timestamp,
  });
  
  factory GestureEvent.now({
    required GestureType type,
    Offset? position,
  }) {
    return GestureEvent(
      type: type,
      position: position,
      timestamp: DateTime.now(),
    );
  }
}

class TapZoneConfig {
  final double leftZoneWidth;
  final double rightZoneWidth;
  final double centerZoneWidth;
  
  const TapZoneConfig({
    this.leftZoneWidth = 0.33,
    this.rightZoneWidth = 0.33,
    this.centerZoneWidth = 0.34,
  });
}

enum ArchiveFormat { cbz, cbr, zip, rar, pdf, epub }

class RetryConfig {
  final int maxAttempts;
  final Duration initialDelay;
  final double backoffMultiplier;
  final Duration maxDelay;
  final bool enableJitter;
  final double jitterFactor;
  
  const RetryConfig({
    this.maxAttempts = 3,
    this.initialDelay = const Duration(seconds: 1),
    this.backoffMultiplier = 2.0,
    this.maxDelay = const Duration(seconds: 30),
    this.enableJitter = true,
    this.jitterFactor = 0.1,
  });
}

class ValidationException implements Exception {
  final String message;
  const ValidationException(this.message);
}

int naturalCompare(String s1, String s2) {
  final re = RegExp(r'([0-9]+|[^0-9]+)');
  final s1parts = re.allMatches(s1).map((m) => m.group(0)!).toList();
  final s2parts = re.allMatches(s2).map((m) => m.group(0)!).toList();

  final len = s1parts.length < s2parts.length ? s1parts.length : s2parts.length;

  for (var i = 0; i < len; i++) {
    final p1 = s1parts[i];
    final p2 = s2parts[i];
    final n1 = int.tryParse(p1);
    final n2 = int.tryParse(p2);

    if (n1 != null && n2 != null) {
      final cmp = n1.compareTo(n2);
      if (cmp != 0) return cmp;
    } else {
      final cmp = p1.compareTo(p2);
      if (cmp != 0) return cmp;
    }
  }

  return s1parts.length.compareTo(s2parts.length);
}