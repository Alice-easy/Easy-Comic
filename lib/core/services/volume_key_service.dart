import 'dart:async';
import 'package:flutter/services.dart';
import '../../domain/services/volume_key_service.dart';

/// Exception thrown when volume key service operations fail
class VolumeKeyServiceException implements Exception {
  final String message;
  final String? code;
  
  const VolumeKeyServiceException(this.message, [this.code]);
  
  @override
  String toString() => 'VolumeKeyServiceException: $message${code != null ? ' (code: $code)' : ''}';
}

/// Concrete implementation of volume key service using platform channels
class VolumeKeyService implements IVolumeKeyService {
  static const MethodChannel _methodChannel = MethodChannel('comic_reader/volume_keys');
  
  final StreamController<VolumeKeyEvent> _keyEventController = 
      StreamController<VolumeKeyEvent>.broadcast();
  
  bool _isListening = false;
  bool _isNavigationEnabled = false;
  final List<void Function(VolumeKeyEvent)> _callbacks = [];
  
  /// Constructor sets up method call handler
  VolumeKeyService() {
    _methodChannel.setMethodCallHandler(_handleMethodCall);
  }
  
  @override
  Stream<VolumeKeyEvent> get keyEventStream => _keyEventController.stream;
  
  @override
  Future<void> enableVolumeKeyNavigation(bool enabled) async {
    try {
      _isNavigationEnabled = enabled;
      
      if (enabled && !_isListening) {
        await _startListening();
      } else if (!enabled && _isListening) {
        await _stopListening();
      }
      
      await _methodChannel.invokeMethod('enableVolumeKeyNavigation', {
        'enabled': enabled,
      });
      
    } on PlatformException catch (e) {
      throw VolumeKeyServiceException(
        'Failed to enable volume key navigation: ${e.message}',
        e.code,
      );
    }
  }
  
  @override
  Future<bool> get isVolumeKeyNavigationSupported async {
    try {
      return await _methodChannel.invokeMethod<bool>('isSupported') ?? false;
    } on PlatformException catch (e) {
      // If platform method fails, assume not supported
      return false;
    }
  }
  
  @override
  void registerVolumeKeyCallback(void Function(VolumeKeyEvent) callback) {
    _callbacks.add(callback);
  }
  
  @override
  void unregisterVolumeKeyCallback() {
    _callbacks.clear();
  }
  
  /// Handle method calls from platform
  Future<dynamic> _handleMethodCall(MethodCall call) async {
    switch (call.method) {
      case 'onVolumeKeyPressed':
        await _handleVolumeKeyPressed(call.arguments);
        break;
      case 'onVolumeKeyReleased':
        await _handleVolumeKeyReleased(call.arguments);
        break;
      default:
        throw UnimplementedError('Method ${call.method} not implemented');
    }
  }
  
  Future<void> _handleVolumeKeyPressed(Map<String, dynamic> arguments) async {
    try {
      final keyType = _parseKeyType(arguments['keyType'] as int);
      final timestamp = DateTime.fromMillisecondsSinceEpoch(
        arguments['timestamp'] as int? ?? DateTime.now().millisecondsSinceEpoch,
      );
      
      final event = VolumeKeyEvent(
        type: keyType,
        timestamp: timestamp,
      );
      
      // Add to stream
      _keyEventController.add(event);
      
      // Notify callbacks
      for (final callback in _callbacks) {
        try {
          callback(event);
        } catch (e) {
          // Ignore callback errors to prevent breaking other listeners
        }
      }
      
    } catch (e) {
      // Log error but don't throw to prevent breaking platform channel
    }
  }
  
  Future<void> _handleVolumeKeyReleased(Map<String, dynamic> arguments) async {
    // Handle volume key release if needed for debouncing or special behaviors
    // For now, we only handle key press events
  }
  
  VolumeKeyType _parseKeyType(int keyCode) {
    switch (keyCode) {
      case 0: // Volume up
      case 24: // Android KEYCODE_VOLUME_UP
        return VolumeKeyType.volumeUp;
      case 1: // Volume down  
      case 25: // Android KEYCODE_VOLUME_DOWN
        return VolumeKeyType.volumeDown;
      default:
        throw VolumeKeyServiceException('Unknown volume key code: $keyCode');
    }
  }
  
  Future<void> _startListening() async {
    if (_isListening) return;
    
    try {
      await _methodChannel.invokeMethod('startListening');
      _isListening = true;
    } on PlatformException catch (e) {
      throw VolumeKeyServiceException(
        'Failed to start volume key listening: ${e.message}',
        e.code,
      );
    }
  }
  
  Future<void> _stopListening() async {
    if (!_isListening) return;
    
    try {
      await _methodChannel.invokeMethod('stopListening');
      _isListening = false;
    } on PlatformException catch (e) {
      throw VolumeKeyServiceException(
        'Failed to stop volume key listening: ${e.message}',
        e.code,
      );
    }
  }
  
  /// Check if volume key navigation is currently enabled
  bool get isEnabled => _isNavigationEnabled;
  
  /// Check if service is currently listening for volume key events
  bool get isListening => _isListening;
  
  /// Dispose the service and clean up resources
  Future<void> dispose() async {
    try {
      await _stopListening();
    } catch (e) {
      // Ignore disposal errors
    }
    
    _callbacks.clear();
    await _keyEventController.close();
  }
}