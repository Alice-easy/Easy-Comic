import 'package:flutter/material.dart';

/// Domain layer gesture types - single source of truth for all gesture handling
enum GestureType {
  // Tap gestures
  tapLeft,
  tapRight,
  tapCenter,
  
  // Zoom gestures
  pinchZoomIn,
  pinchZoomOut,
  doubleTapZoom,
  
  // Pan gestures
  panStart,
  panUpdate,
  panEnd,
  
  // Volume key gestures
  volumeUp,
  volumeDown,
  
  // Other gestures
  longPress,
  swipeUp,
  swipeDown,
  swipeLeft,
  swipeRight,
  
  // Legacy gesture names for compatibility
  doubleTap,
  pinchIn,
  pinchOut,
}

/// Gesture event data with metadata
class GestureEvent {
  final GestureType type;
  final Offset? position;
  final double? velocity;
  final double? scale;
  final DateTime timestamp;
  
  const GestureEvent({
    required this.type,
    this.position,
    this.velocity,
    this.scale,
    required this.timestamp,
  });
  
  /// Create a gesture event with current timestamp
  factory GestureEvent.now({
    required GestureType type,
    Offset? position,
    double? velocity,
    double? scale,
  }) {
    return GestureEvent(
      type: type,
      position: position,
      velocity: velocity,
      scale: scale,
      timestamp: DateTime.now(),
    );
  }
}

/// Gesture configuration for different interaction scenarios
class GestureConfig {
  final bool enableTapNavigation;
  final bool enableVolumeKeyNavigation;
  final bool enablePinchZoom;
  final bool enableDoubleTapZoom;
  final double tapSensitivity;
  final double zoomSensitivity;
  final Duration doubleTapTimeout;
  final Duration longPressTimeout;
  
  const GestureConfig({
    this.enableTapNavigation = true,
    this.enableVolumeKeyNavigation = false,
    this.enablePinchZoom = true,
    this.enableDoubleTapZoom = true,
    this.tapSensitivity = 1.0,
    this.zoomSensitivity = 1.0,
    this.doubleTapTimeout = const Duration(milliseconds: 300),
    this.longPressTimeout = const Duration(milliseconds: 500),
  });
  
  GestureConfig copyWith({
    bool? enableTapNavigation,
    bool? enableVolumeKeyNavigation,
    bool? enablePinchZoom,
    bool? enableDoubleTapZoom,
    double? tapSensitivity,
    double? zoomSensitivity,
    Duration? doubleTapTimeout,
    Duration? longPressTimeout,
  }) {
    return GestureConfig(
      enableTapNavigation: enableTapNavigation ?? this.enableTapNavigation,
      enableVolumeKeyNavigation: enableVolumeKeyNavigation ?? this.enableVolumeKeyNavigation,
      enablePinchZoom: enablePinchZoom ?? this.enablePinchZoom,
      enableDoubleTapZoom: enableDoubleTapZoom ?? this.enableDoubleTapZoom,
      tapSensitivity: tapSensitivity ?? this.tapSensitivity,
      zoomSensitivity: zoomSensitivity ?? this.zoomSensitivity,
      doubleTapTimeout: doubleTapTimeout ?? this.doubleTapTimeout,
      longPressTimeout: longPressTimeout ?? this.longPressTimeout,
    );
  }
}