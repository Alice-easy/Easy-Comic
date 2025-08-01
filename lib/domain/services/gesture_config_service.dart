import '../entities/gesture_types.dart';

/// Tap zone configuration for gesture recognition
class TapZoneConfig {
  final double leftZoneWidth;    // Percentage of screen width (0.0-1.0)
  final double rightZoneWidth;   // Percentage of screen width (0.0-1.0)
  final double centerZoneWidth;  // Percentage of screen width (0.0-1.0)
  final double topZoneHeight;    // Percentage of screen height (0.0-1.0)
  final double bottomZoneHeight; // Percentage of screen height (0.0-1.0)
  
  const TapZoneConfig({
    this.leftZoneWidth = 0.33,
    this.rightZoneWidth = 0.33,
    this.centerZoneWidth = 0.34,
    this.topZoneHeight = 0.2,
    this.bottomZoneHeight = 0.2,
  });
  
  TapZoneConfig copyWith({
    double? leftZoneWidth,
    double? rightZoneWidth,
    double? centerZoneWidth,
    double? topZoneHeight,
    double? bottomZoneHeight,
  }) {
    return TapZoneConfig(
      leftZoneWidth: leftZoneWidth ?? this.leftZoneWidth,
      rightZoneWidth: rightZoneWidth ?? this.rightZoneWidth,
      centerZoneWidth: centerZoneWidth ?? this.centerZoneWidth,
      topZoneHeight: topZoneHeight ?? this.topZoneHeight,
      bottomZoneHeight: bottomZoneHeight ?? this.bottomZoneHeight,
    );
  }
}

/// Service for managing gesture configuration and recognition
abstract class IGestureConfigService {
  /// Get current gesture configuration
  GestureConfig get currentConfig;
  
  /// Update gesture configuration
  Future<void> updateConfig(GestureConfig config);
  
  /// Get current tap zone configuration
  TapZoneConfig get tapZoneConfig;
  
  /// Update tap zone configuration
  Future<void> updateTapZoneConfig(TapZoneConfig config);
  
  /// Update gesture sensitivity
  Future<void> updateSensitivity(double tapSensitivity, double zoomSensitivity);
  
  /// Enable or disable specific gesture types
  Future<void> setGestureEnabled(GestureType gestureType, bool enabled);
  
  /// Check if a gesture type is enabled
  bool isGestureEnabled(GestureType gestureType);
  
  /// Reset to default configuration
  Future<void> resetToDefaults();
  
  /// Watch for configuration changes
  Stream<GestureConfig> watchConfigChanges();
}