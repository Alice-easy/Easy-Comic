import 'package:freezed_annotation/freezed_annotation.dart';
import '../core/brightness_errors.dart';
import '../core/brightness_service.dart';

part 'brightness_models.g.dart';
part 'brightness_models.freezed.dart';

/// Brightness control mode
enum BrightnessMode {
  /// Manual brightness control
  manual('manual', 'Manual'),
  
  /// Auto-adjust based on time of day
  timeBasedAuto('time_auto', 'Time-based Auto'),
  
  /// Auto-adjust based on ambient light (requires sensor)
  ambientAuto('ambient_auto', 'Ambient Auto');

  const BrightnessMode(this.value, this.displayName);
  final String value;
  final String displayName;

  static BrightnessMode fromString(String value) {
    return BrightnessMode.values.firstWhere(
      (mode) => mode.value == value,
      orElse: () => BrightnessMode.manual,
    );
  }
}

/// Brightness state for UI management
@freezed
class BrightnessState with _$BrightnessState {
  const factory BrightnessState({
    @Default(1.0) double currentBrightness,
    @Default(1.0) double originalBrightness,
    @Default(false) bool isSupported,
    @Default(false) bool hasWritePermission,
    @Default(false) bool autoAdjustEnabled,
    @Default(BrightnessMode.manual) BrightnessMode mode,
    @Default(false) bool isInitialized,
    @Default(false) bool isLoading,
    @BrightnessErrorConverter() BrightnessError? error
  }) = _BrightnessState;

  factory BrightnessState.fromJson(Map<String, dynamic> json) =>
      _$BrightnessStateFromJson(json);
}

/// Enhanced brightness state with additional UI properties
@freezed
class EnhancedBrightnessState with _$EnhancedBrightnessState {
  const factory EnhancedBrightnessState({
    required BrightnessState brightness,
    @Default(false) bool showBrightnessSlider,
    @Default(false) bool showPermissionDialog,
    @Default(false) bool overlayEnabled,
    @Default(null) double? customOverlayBrightness,
    @Default([]) List<double> presetLevels,
    String? lastErrorMessage,
  }) = _EnhancedBrightnessState;

  factory EnhancedBrightnessState.fromJson(Map<String, dynamic> json) =>
      _$EnhancedBrightnessStateFromJson(json);
}