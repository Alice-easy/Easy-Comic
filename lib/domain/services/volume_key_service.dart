import 'dart:async';
import '../entities/gesture_types.dart';

/// Volume key event data
class VolumeKeyEvent {
  final VolumeKeyType type;
  final DateTime timestamp;
  
  const VolumeKeyEvent({
    required this.type,
    required this.timestamp,
  });
}

/// Volume key types
enum VolumeKeyType {
  volumeUp,
  volumeDown,
}

/// Service for handling volume key navigation
abstract class IVolumeKeyService {
  /// Stream of volume key events
  Stream<VolumeKeyEvent> get keyEventStream;
  
  /// Enable or disable volume key navigation
  Future<void> enableVolumeKeyNavigation(bool enabled);
  
  /// Check if volume key navigation is supported on this platform
  Future<bool> get isVolumeKeyNavigationSupported;
  
  /// Register callback for volume key events
  void registerVolumeKeyCallback(void Function(VolumeKeyEvent) callback);
  
  /// Unregister volume key callback
  void unregisterVolumeKeyCallback();
}