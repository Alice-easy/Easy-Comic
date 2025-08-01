import 'dart:async';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:easy_comic/core/services/volume_key_service.dart';
import 'package:easy_comic/domain/services/volume_key_service.dart';

// Mock classes
class MockMethodChannel extends Mock implements MethodChannel {}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  group('VolumeKeyService', () {
    late VolumeKeyService volumeKeyService;
    late MockMethodChannel mockMethodChannel;

    setUpAll(() {
      registerFallbackValue(const MethodCall('test'));
    });

    setUp(() {
      volumeKeyService = VolumeKeyService();
      mockMethodChannel = MockMethodChannel();
    });

    test('should initialize with default state', () {
      expect(volumeKeyService.isEnabled, isFalse);
      expect(volumeKeyService.isListening, isFalse);
    });

    group('Volume Key Navigation', () {
      test('should enable volume key navigation successfully', () async {
        // Mock successful platform call
        when(() => mockMethodChannel.invokeMethod<void>(
          'enableVolumeKeyNavigation', 
          any(),
        )).thenAnswer((_) async {});
        
        when(() => mockMethodChannel.invokeMethod<void>(
          'startListening',
        )).thenAnswer((_) async {});

        await volumeKeyService.enableVolumeKeyNavigation(true);
        
        expect(volumeKeyService.isEnabled, isTrue);
      });

      test('should disable volume key navigation successfully', () async {
        // First enable it
        when(() => mockMethodChannel.invokeMethod<void>(
          'enableVolumeKeyNavigation', 
          any(),
        )).thenAnswer((_) async {});
        
        when(() => mockMethodChannel.invokeMethod<void>(
          'startListening',
        )).thenAnswer((_) async {});
        
        when(() => mockMethodChannel.invokeMethod<void>(
          'stopListening',
        )).thenAnswer((_) async {});

        await volumeKeyService.enableVolumeKeyNavigation(true);
        await volumeKeyService.enableVolumeKeyNavigation(false);
        
        expect(volumeKeyService.isEnabled, isFalse);
      });

      test('should handle platform exception during enable', () async {
        when(() => mockMethodChannel.invokeMethod<void>(
          'enableVolumeKeyNavigation', 
          any(),
        )).thenThrow(PlatformException(code: 'ERROR', message: 'Test error'));

        expect(
          () => volumeKeyService.enableVolumeKeyNavigation(true),
          throwsA(isA<VolumeKeyServiceException>()),
        );
      });
    });

    group('Volume Key Support Detection', () {
      test('should return true when platform supports volume keys', () async {
        when(() => mockMethodChannel.invokeMethod<bool>('isSupported'))
            .thenAnswer((_) async => true);

        final isSupported = await volumeKeyService.isVolumeKeyNavigationSupported;
        
        expect(isSupported, isTrue);
      });

      test('should return false when platform does not support volume keys', () async {
        when(() => mockMethodChannel.invokeMethod<bool>('isSupported'))
            .thenAnswer((_) async => false);

        final isSupported = await volumeKeyService.isVolumeKeyNavigationSupported;
        
        expect(isSupported, isFalse);
      });

      test('should return false when platform call fails', () async {
        when(() => mockMethodChannel.invokeMethod<bool>('isSupported'))
            .thenThrow(PlatformException(code: 'ERROR', message: 'Not supported'));

        final isSupported = await volumeKeyService.isVolumeKeyNavigationSupported;
        
        expect(isSupported, isFalse);
      });
    });

    group('Volume Key Events', () {
      test('should emit volume up events correctly', () async {
        final events = <VolumeKeyEvent>[];
        final subscription = volumeKeyService.keyEventStream.listen(events.add);

        // Simulate platform sending volume up event
        final methodCall = MethodCall('onVolumeKeyPressed', {
          'keyType': 0, // Volume up
          'timestamp': DateTime.now().millisecondsSinceEpoch,
        });

        await volumeKeyService._handleMethodCall(methodCall);

        expect(events, hasLength(1));
        expect(events.first.type, equals(VolumeKeyType.volumeUp));
        
        await subscription.cancel();
      });

      test('should emit volume down events correctly', () async {
        final events = <VolumeKeyEvent>[];
        final subscription = volumeKeyService.keyEventStream.listen(events.add);

        // Simulate platform sending volume down event
        final methodCall = MethodCall('onVolumeKeyPressed', {
          'keyType': 1, // Volume down
          'timestamp': DateTime.now().millisecondsSinceEpoch,
        });

        await volumeKeyService._handleMethodCall(methodCall);

        expect(events, hasLength(1));
        expect(events.first.type, equals(VolumeKeyType.volumeDown));
        
        await subscription.cancel();
      });

      test('should handle Android key codes correctly', () async {
        final events = <VolumeKeyEvent>[];
        final subscription = volumeKeyService.keyEventStream.listen(events.add);

        // Android KEYCODE_VOLUME_UP = 24
        final methodCall1 = MethodCall('onVolumeKeyPressed', {
          'keyType': 24,
          'timestamp': DateTime.now().millisecondsSinceEpoch,
        });

        // Android KEYCODE_VOLUME_DOWN = 25
        final methodCall2 = MethodCall('onVolumeKeyPressed', {
          'keyType': 25,
          'timestamp': DateTime.now().millisecondsSinceEpoch,
        });

        await volumeKeyService._handleMethodCall(methodCall1);
        await volumeKeyService._handleMethodCall(methodCall2);

        expect(events, hasLength(2));
        expect(events[0].type, equals(VolumeKeyType.volumeUp));
        expect(events[1].type, equals(VolumeKeyType.volumeDown));
        
        await subscription.cancel();
      });

      test('should handle invalid key codes gracefully', () async {
        final events = <VolumeKeyEvent>[];
        final subscription = volumeKeyService.keyEventStream.listen(events.add);

        // Invalid key code
        final methodCall = MethodCall('onVolumeKeyPressed', {
          'keyType': 999,
          'timestamp': DateTime.now().millisecondsSinceEpoch,
        });

        // Should not throw or emit events for invalid key codes
        await volumeKeyService._handleMethodCall(methodCall);

        expect(events, isEmpty);
        
        await subscription.cancel();
      });

      test('should handle missing timestamp gracefully', () async {
        final events = <VolumeKeyEvent>[];
        final subscription = volumeKeyService.keyEventStream.listen(events.add);

        final methodCall = MethodCall('onVolumeKeyPressed', {
          'keyType': 0, // Volume up
          // No timestamp provided
        });

        await volumeKeyService._handleMethodCall(methodCall);

        expect(events, hasLength(1));
        expect(events.first.type, equals(VolumeKeyType.volumeUp));
        expect(events.first.timestamp, isA<DateTime>());
        
        await subscription.cancel();
      });
    });

    group('Callback Registration', () {
      test('should register and call callbacks for volume events', () async {
        VolumeKeyEvent? receivedEvent;
        
        volumeKeyService.registerVolumeKeyCallback((event) {
          receivedEvent = event;
        });

        // Simulate volume key event
        final methodCall = MethodCall('onVolumeKeyPressed', {
          'keyType': 0,
          'timestamp': DateTime.now().millisecondsSinceEpoch,
        });

        await volumeKeyService._handleMethodCall(methodCall);

        expect(receivedEvent, isNotNull);
        expect(receivedEvent!.type, equals(VolumeKeyType.volumeUp));
      });

      test('should handle multiple callbacks', () async {
        final receivedEvents = <VolumeKeyEvent>[];
        
        volumeKeyService.registerVolumeKeyCallback((event) {
          receivedEvents.add(event);
        });
        
        volumeKeyService.registerVolumeKeyCallback((event) {
          receivedEvents.add(event);
        });

        final methodCall = MethodCall('onVolumeKeyPressed', {
          'keyType': 0,
          'timestamp': DateTime.now().millisecondsSinceEpoch,
        });

        await volumeKeyService._handleMethodCall(methodCall);

        expect(receivedEvents, hasLength(2));
      });

      test('should handle callback errors gracefully', () async {
        volumeKeyService.registerVolumeKeyCallback((event) {
          throw Exception('Callback error');
        });

        final methodCall = MethodCall('onVolumeKeyPressed', {
          'keyType': 0,
          'timestamp': DateTime.now().millisecondsSinceEpoch,
        });

        // Should not throw even if callback throws
        expect(
          () async => await volumeKeyService._handleMethodCall(methodCall),
          returnsNormally,
        );
      });

      test('should clear callbacks when unregistered', () async {
        final receivedEvents = <VolumeKeyEvent>[];
        
        volumeKeyService.registerVolumeKeyCallback((event) {
          receivedEvents.add(event);
        });

        volumeKeyService.unregisterVolumeKeyCallback();

        final methodCall = MethodCall('onVolumeKeyPressed', {
          'keyType': 0,
          'timestamp': DateTime.now().millisecondsSinceEpoch,
        });

        await volumeKeyService._handleMethodCall(methodCall);

        expect(receivedEvents, isEmpty);
      });
    });

    group('Method Call Handling', () {
      test('should handle unknown method calls', () async {
        final methodCall = MethodCall('unknownMethod', {});

        expect(
          () async => await volumeKeyService._handleMethodCall(methodCall),
          throwsA(isA<UnimplementedError>()),
        );
      });

      test('should handle onVolumeKeyReleased method', () async {
        final methodCall = MethodCall('onVolumeKeyReleased', {
          'keyType': 0,
          'timestamp': DateTime.now().millisecondsSinceEpoch,
        });

        // Should not throw for release events
        expect(
          () async => await volumeKeyService._handleMethodCall(methodCall),
          returnsNormally,
        );
      });
    });

    group('Performance Tests', () {
      test('volume key event processing should be fast', () async {
        final events = <VolumeKeyEvent>[];
        final subscription = volumeKeyService.keyEventStream.listen(events.add);

        final stopwatch = Stopwatch()..start();
        
        // Process multiple events rapidly
        for (int i = 0; i < 100; i++) {
          final methodCall = MethodCall('onVolumeKeyPressed', {
            'keyType': i % 2, // Alternate between volume up/down
            'timestamp': DateTime.now().millisecondsSinceEpoch,
          });
          
          await volumeKeyService._handleMethodCall(methodCall);
        }
        
        stopwatch.stop();

        // Should process 100 events in under 100ms (1ms per event average)
        expect(stopwatch.elapsedMilliseconds, lessThan(100));
        expect(events, hasLength(100));
        
        await subscription.cancel();
      });

      test('should handle rapid enable/disable cycles', () async {
        when(() => mockMethodChannel.invokeMethod<void>(any(), any()))
            .thenAnswer((_) async {});

        final stopwatch = Stopwatch()..start();
        
        // Rapid enable/disable cycles
        for (int i = 0; i < 50; i++) {
          await volumeKeyService.enableVolumeKeyNavigation(true);
          await volumeKeyService.enableVolumeKeyNavigation(false);
        }
        
        stopwatch.stop();

        // Should handle 100 operations in reasonable time
        expect(stopwatch.elapsedMilliseconds, lessThan(1000));
      });
    });

    group('Resource Management', () {
      test('should dispose cleanly', () async {
        when(() => mockMethodChannel.invokeMethod<void>('stopListening'))
            .thenAnswer((_) async {});

        // Enable first to have something to clean up
        when(() => mockMethodChannel.invokeMethod<void>(any(), any()))
            .thenAnswer((_) async {});
        
        await volumeKeyService.enableVolumeKeyNavigation(true);
        
        // Should dispose without throwing
        expect(() async => await volumeKeyService.dispose(), returnsNormally);
      });

      test('should handle disposal errors gracefully', () async {
        when(() => mockMethodChannel.invokeMethod<void>('stopListening'))
            .thenThrow(PlatformException(code: 'ERROR', message: 'Disposal error'));

        // Should not throw even if platform disposal fails
        expect(() async => await volumeKeyService.dispose(), returnsNormally);
      });
    });

    tearDown(() async {
      await volumeKeyService.dispose();
    });
  });
}