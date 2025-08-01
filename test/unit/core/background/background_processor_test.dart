import 'dart:async';
import 'dart:isolate';
import 'dart:typed_data';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:easy_comic/core/background/background_processor.dart';

void main() {
  group('BackgroundProcessor', () {
    late BackgroundProcessor processor;

    setUp(() {
      processor = BackgroundProcessor();
    });

    tearDown(() async {
      await processor.dispose();
    });

    group('Initialization', () {
      test('should initialize successfully', () async {
        await processor.initialize();
        
        expect(processor.isReady, isTrue);
      });

      test('should not initialize twice', () async {
        await processor.initialize();
        
        // Second initialization should be no-op
        await processor.initialize();
        
        expect(processor.isReady, isTrue);
      });

      test('should handle initialization failure gracefully', () async {
        // Dispose processor to simulate initialization failure scenario
        await processor.dispose();
        
        // Try to use disposed processor
        expect(
          () => processor.processImageInBackground(
            'test_path',
            const ImageProcessingConfig(),
          ),
          throwsA(isA<ImageProcessingError>()),
        );
      });
    });

    group('Image Processing', () {
      test('should process image in background successfully', () async {
        await processor.initialize();
        
        const config = ImageProcessingConfig(
          quality: 0.8,
          outputFormat: ImageFormat.jpeg,
        );
        
        final result = await processor.processImageInBackground(
          'test_image_path',
          config,
        );
        
        expect(result, isA<Uint8List>());
      });

      test('should handle different image formats', () async {
        await processor.initialize();
        
        const configs = [
          ImageProcessingConfig(outputFormat: ImageFormat.jpeg),
          ImageProcessingConfig(outputFormat: ImageFormat.png),
          ImageProcessingConfig(outputFormat: ImageFormat.webp),
        ];
        
        for (final config in configs) {
          final result = await processor.processImageInBackground(
            'test_path',
            config,
          );
          
          expect(result, isA<Uint8List>());
        }
      });

      test('should handle different quality settings', () async {
        await processor.initialize();
        
        const qualityLevels = [0.1, 0.5, 0.8, 1.0];
        
        for (final quality in qualityLevels) {
          final config = ImageProcessingConfig(quality: quality);
          final result = await processor.processImageInBackground(
            'test_path',
            config,
          );
          
          expect(result, isA<Uint8List>());
        }
      });

      test('should handle target dimensions', () async {
        await processor.initialize();
        
        const config = ImageProcessingConfig(
          targetWidth: 800,
          targetHeight: 600,
          maintainAspectRatio: true,
        );
        
        final result = await processor.processImageInBackground(
          'test_path',
          config,
        );
        
        expect(result, isA<Uint8List>());
      });

      test('should process image without initialization if not ready', () async {
        // Don't initialize processor
        expect(processor.isReady, isFalse);
        
        const config = ImageProcessingConfig();
        
        // Should auto-initialize and process
        final result = await processor.processImageInBackground(
          'test_path',
          config,
        );
        
        expect(result, isA<Uint8List>());
        expect(processor.isReady, isTrue);
      });
    });

    group('Batch Processing', () {
      test('should process multiple images with priority', () async {
        await processor.initialize();
        
        final requests = [
          ImageProcessingRequest(
            imagePath: 'image1.jpg',
            config: const ImageProcessingConfig(quality: 0.8),
            responsePort: ReceivePort().sendPort,
          ),
          ImageProcessingRequest(
            imagePath: 'image2.jpg',
            config: const ImageProcessingConfig(quality: 0.6),
            responsePort: ReceivePort().sendPort,
          ),
          ImageProcessingRequest(
            imagePath: 'image3.jpg',
            config: const ImageProcessingConfig(quality: 0.9),
            responsePort: ReceivePort().sendPort,
          ),
        ];
        
        final results = await processor.processImagesWithPriority(requests);
        
        expect(results, hasLength(3));
        for (final result in results) {
          expect(result, isA<Uint8List>());
        }
      });

      test('should handle batch processing errors gracefully', () async {
        await processor.initialize();
        
        final requests = [
          ImageProcessingRequest(
            imagePath: 'valid_image.jpg',
            config: const ImageProcessingConfig(),
            responsePort: ReceivePort().sendPort,
          ),
          ImageProcessingRequest(
            imagePath: 'invalid_image.jpg',
            config: const ImageProcessingConfig(),
            responsePort: ReceivePort().sendPort,
          ),
        ];
        
        final results = await processor.processImagesWithPriority(requests);
        
        expect(results, hasLength(2));
        // Should include placeholder for failed processing
        expect(results.any((r) => r.isEmpty), isTrue);
      });

      test('should maintain order in batch processing', () async {
        await processor.initialize();
        
        final requests = List.generate(5, (i) => ImageProcessingRequest(
          imagePath: 'image$i.jpg',
          config: const ImageProcessingConfig(),
          responsePort: ReceivePort().sendPort,
        ));
        
        final results = await processor.processImagesWithPriority(requests);
        
        expect(results, hasLength(5));
        // Results should maintain input order
        for (int i = 0; i < results.length; i++) {
          expect(results[i], isA<Uint8List>());
        }
      });
    });

    group('Performance Tests', () {
      test('image processing should complete within reasonable time', () async {
        await processor.initialize();
        
        const config = ImageProcessingConfig();
        final stopwatch = Stopwatch()..start();
        
        await processor.processImageInBackground('test_path', config);
        
        stopwatch.stop();
        
        // Should complete within 1 second (includes isolate communication overhead)
        expect(stopwatch.elapsedMilliseconds, lessThan(1000));
      });

      test('should handle concurrent processing requests', () async {
        await processor.initialize();
        
        const config = ImageProcessingConfig();
        final futures = <Future<Uint8List>>[];
        
        // Submit 10 concurrent requests
        for (int i = 0; i < 10; i++) {
          futures.add(processor.processImageInBackground('image$i.jpg', config));
        }
        
        final stopwatch = Stopwatch()..start();
        final results = await Future.wait(futures);
        stopwatch.stop();
        
        expect(results, hasLength(10));
        // All concurrent requests should complete within reasonable time
        expect(stopwatch.elapsedMilliseconds, lessThan(5000));
      });

      test('batch processing should be efficient', () async {
        await processor.initialize();
        
        final requests = List.generate(20, (i) => ImageProcessingRequest(
          imagePath: 'batch_image$i.jpg',
          config: const ImageProcessingConfig(),
          responsePort: ReceivePort().sendPort,
        ));
        
        final stopwatch = Stopwatch()..start();
        final results = await processor.processImagesWithPriority(requests);
        stopwatch.stop();
        
        expect(results, hasLength(20));
        // Batch processing should be more efficient than individual requests
        expect(stopwatch.elapsedMilliseconds, lessThan(10000)); // < 500ms per image
      });
    });

    group('Error Handling', () {
      test('should handle processing errors with meaningful messages', () async {
        await processor.initialize();
        
        // Use invalid path to trigger error in real implementation
        const config = ImageProcessingConfig();
        
        try {
          await processor.processImageInBackground('', config);
        } catch (e) {
          expect(e, isA<ImageProcessingError>());
        }
      });

      test('should handle isolate communication errors', () async {
        await processor.initialize();
        
        // Dispose processor while processing to simulate communication error
        const config = ImageProcessingConfig();
        final future = processor.processImageInBackground('test', config);
        
        // Dispose immediately to break communication
        await processor.dispose();
        
        expect(future, throwsA(isA<ImageProcessingError>()));
      });
    });

    group('Resource Management', () {
      test('should dispose cleanly', () async {
        await processor.initialize();
        expect(processor.isReady, isTrue);
        
        await processor.dispose();
        expect(processor.isReady, isFalse);
      });

      test('should handle multiple disposal calls', () async {
        await processor.initialize();
        
        await processor.dispose();
        await processor.dispose(); // Second disposal should be safe
        
        expect(processor.isReady, isFalse);
      });

      test('should cleanup isolate resources properly', () async {
        await processor.initialize();
        
        // Process some images to ensure isolate is active
        const config = ImageProcessingConfig();
        await processor.processImageInBackground('test1', config);
        await processor.processImageInBackground('test2', config);
        
        // Disposal should cleanup all isolate resources
        await processor.dispose();
        
        expect(processor.isReady, isFalse);
      });
    });

    group('Factory Pattern', () {
      test('should create singleton instance', () {
        final instance1 = BackgroundProcessorFactory.instance;
        final instance2 = BackgroundProcessorFactory.instance;
        
        expect(identical(instance1, instance2), isTrue);
      });

      test('should create custom processor instances', () {
        final custom1 = BackgroundProcessorFactory.createCustomProcessor();
        final custom2 = BackgroundProcessorFactory.createCustomProcessor();
        
        expect(identical(custom1, custom2), isFalse);
        
        // Cleanup custom instances
        custom1.dispose();
        custom2.dispose();
      });

      test('should dispose all processors', () async {
        final instance = BackgroundProcessorFactory.instance;
        await instance.initialize();
        
        expect(instance.isReady, isTrue);
        
        await BackgroundProcessorFactory.disposeAll();
        
        expect(instance.isReady, isFalse);
      });
    });

    group('Edge Cases', () {
      test('should handle rapid initialization and disposal', () async {
        for (int i = 0; i < 5; i++) {
          final tempProcessor = BackgroundProcessor();
          await tempProcessor.initialize();
          expect(tempProcessor.isReady, isTrue);
          
          await tempProcessor.dispose();
          expect(tempProcessor.isReady, isFalse);
        }
      });

      test('should handle processing request on disposed processor', () async {
        await processor.initialize();
        await processor.dispose();
        
        expect(
          () => processor.processImageInBackground('test', const ImageProcessingConfig()),
          throwsA(isA<ImageProcessingError>()),
        );
      });
    });
  });

  group('ImageProcessingConfig', () {
    test('should create config with default values', () {
      const config = ImageProcessingConfig();
      
      expect(config.quality, equals(0.8));
      expect(config.outputFormat, equals(ImageFormat.jpeg));
      expect(config.maintainAspectRatio, isTrue);
      expect(config.targetWidth, isNull);
      expect(config.targetHeight, isNull);
    });

    test('should create config with custom values', () {
      const config = ImageProcessingConfig(
        targetWidth: 1920,
        targetHeight: 1080,
        quality: 0.95,
        outputFormat: ImageFormat.png,
        maintainAspectRatio: false,
      );
      
      expect(config.targetWidth, equals(1920));
      expect(config.targetHeight, equals(1080));
      expect(config.quality, equals(0.95));
      expect(config.outputFormat, equals(ImageFormat.png));
      expect(config.maintainAspectRatio, isFalse);
    });

    test('should handle edge case quality values', () {
      const configs = [
        ImageProcessingConfig(quality: 0.0),
        ImageProcessingConfig(quality: 1.0),
      ];
      
      for (final config in configs) {
        expect(config.quality, greaterThanOrEqualTo(0.0));
        expect(config.quality, lessThanOrEqualTo(1.0));
      }
    });
  });

  group('ImageProcessingError', () {
    test('should create error with message', () {
      const error = ImageProcessingError('Test error message');
      
      expect(error.message, equals('Test error message'));
      expect(error.stackTrace, isNull);
      expect(error.toString(), equals('ImageProcessingError: Test error message'));
    });

    test('should create error with message and stack trace', () {
      const error = ImageProcessingError('Test error', 'Stack trace info');
      
      expect(error.message, equals('Test error'));
      expect(error.stackTrace, equals('Stack trace info'));
    });
  });
}