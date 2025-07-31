import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// Unified service for managing app-specific and system brightness control
/// Provides hybrid brightness control with overlay and system-level options
class BrightnessService {
  static const MethodChannel _channel = MethodChannel('com.easycomic.brightness');
  static const String _brightnessKey = 'reader_brightness';
  static const double _defaultBrightness = 1.0;
  static const double _minBrightness = 0.1;
  static const double _maxBrightness = 1.0;

  double? _originalBrightness;
  double _currentBrightness = 1.0;
  bool _isInitialized = false;
  bool _isSupported = false;
  bool _hasWritePermission = false;

  /// Initialize the brightness service
  Future<void> initialize() async {
    if (_isInitialized) return;
    
    try {
      // Check if brightness control is supported
      _isSupported = await _checkSupport();
      
      if (_isSupported) {
        // Get current system brightness
        _originalBrightness = await _getSystemBrightness();
        _currentBrightness = _originalBrightness ?? 1.0;
        
        // Check write permissions on Android
        if (Platform.isAndroid) {
          _hasWritePermission = await _checkWritePermission();
        } else {
          _hasWritePermission = true; // iOS doesn't require special permissions
        }
      } else {
        // Fallback for unsupported platforms - use stored brightness
        _originalBrightness = await getBrightness();
        _currentBrightness = _originalBrightness ?? 1.0;
        _hasWritePermission = false;
      }
      
      _isInitialized = true;
    } catch (e) {
      // Initialize with default values on error
      _originalBrightness = await getBrightness();
      _currentBrightness = _originalBrightness ?? 1.0;
      _isInitialized = true;
      _isSupported = false;
      _hasWritePermission = false;
    }
  }

  /// Get current brightness setting from storage
  static Future<double> getBrightness() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      return prefs.getDouble(_brightnessKey) ?? _defaultBrightness;
    } catch (e) {
      return _defaultBrightness;
    }
  }

  /// Set brightness with both overlay and optional system control
  static Future<void> setBrightness(double brightness, {bool systemLevel = false}) async {
    brightness = brightness.clamp(_minBrightness, _maxBrightness);
    
    try {
      // Always store the preference
      final prefs = await SharedPreferences.getInstance();
      await prefs.setDouble(_brightnessKey, brightness);
      
      // Optionally set system brightness if supported and requested
      if (systemLevel) {
        final service = BrightnessService();
        if (!service._isInitialized) await service.initialize();
        
        if (service._isSupported && service._hasWritePermission) {
          await service._setSystemBrightness(brightness);
          service._currentBrightness = brightness;
        }
      }
    } catch (e) {
      // Silently fail if unable to persist or set system brightness
    }
  }

  /// Get current system brightness level
  double get currentBrightness => _currentBrightness;

  /// Get original system brightness
  double? get originalBrightness => _originalBrightness;

  /// Check if brightness control is supported
  bool get isSupported => _isSupported;

  /// Check if write permission is granted (Android only)
  bool get hasWritePermission => _hasWritePermission;

  /// Reset brightness to default
  static Future<void> resetBrightness() async {
    await setBrightness(_defaultBrightness);
  }

  /// Validate brightness value
  static double validateBrightness(double brightness) {
    return brightness.clamp(_minBrightness, _maxBrightness);
  }

  /// Check if brightness is at minimum
  static bool isMinBrightness(double brightness) {
    return brightness <= _minBrightness;
  }

  /// Check if brightness is at maximum
  static bool isMaxBrightness(double brightness) {
    return brightness >= _maxBrightness;
  }

  /// Increase brightness by step amount
  Future<void> increaseBrightness({double step = 0.1}) async {
    final newBrightness = (_currentBrightness + step).clamp(0.0, 1.0);
    await setBrightness(newBrightness, systemLevel: _isSupported);
  }

  /// Decrease brightness by step amount
  Future<void> decreaseBrightness({double step = 0.1}) async {
    final newBrightness = (_currentBrightness - step).clamp(0.0, 1.0);
    await setBrightness(newBrightness, systemLevel: _isSupported);
  }

  /// Reset brightness to original system level
  Future<void> resetToOriginal() async {
    if (_originalBrightness != null) {
      await setBrightness(_originalBrightness!, systemLevel: _isSupported);
    }
  }

  /// Apply reading-optimized brightness based on theme
  Future<void> applyReadingBrightness({
    required bool isDarkTheme,
    double? customBrightness,
  }) async {
    if (customBrightness != null) {
      await setBrightness(customBrightness, systemLevel: _isSupported);
      return;
    }

    // Optimize brightness for reading
    if (isDarkTheme) {
      // Lower brightness for dark themes to reduce eye strain
      await setBrightness(0.3, systemLevel: _isSupported);
    } else {
      // Higher brightness for light themes
      await setBrightness(0.8, systemLevel: _isSupported);
    }
  }

  /// Request write settings permission on Android
  Future<bool> requestWritePermission() async {
    if (!Platform.isAndroid) return true;
    
    try {
      final granted = await _channel.invokeMethod<bool>('requestWriteSettingsPermission');
      _hasWritePermission = granted ?? false;
      return _hasWritePermission;
    } catch (e) {
      return false;
    }
  }

  /// Check if brightness control is supported on this platform
  Future<bool> _checkSupport() async {
    try {
      final supported = await _channel.invokeMethod<bool>('isSupported');
      return supported ?? false;
    } catch (e) {
      return false;
    }
  }

  /// Check write settings permission on Android
  Future<bool> _checkWritePermission() async {
    if (!Platform.isAndroid) return true;
    
    try {
      final granted = await _channel.invokeMethod<bool>('checkWriteSettingsPermission');
      return granted ?? false;
    } catch (e) {
      return false;
    }
  }

  /// Get platform-specific system brightness
  Future<double> _getSystemBrightness() async {
    try {
      final result = await _channel.invokeMethod<double>('getSystemBrightness');
      return result ?? 1.0;
    } catch (e) {
      return 1.0;
    }
  }

  /// Set platform-specific system brightness
  Future<void> _setSystemBrightness(double brightness) async {
    try {
      await _channel.invokeMethod('setSystemBrightness', {'brightness': brightness});
    } catch (e) {
      // Silently handle system brightness errors
    }
  }

  /// Get brightness level description for UI
  String getBrightnessDescription(double brightness) {
    if (brightness <= 0.2) {
      return 'Very Low';
    } else if (brightness <= 0.4) {
      return 'Low';
    } else if (brightness <= 0.6) {
      return 'Medium';
    } else if (brightness <= 0.8) {
      return 'High';
    } else {
      return 'Very High';
    }
  }

  /// Get recommended brightness for different times of day
  double getRecommendedBrightness() {
    final now = DateTime.now();
    final hour = now.hour;

    // Night mode (9 PM - 6 AM)
    if (hour >= 21 || hour <= 6) {
      return 0.2;
    }
    // Morning/Evening (6 AM - 9 AM, 6 PM - 9 PM)
    else if ((hour >= 6 && hour <= 9) || (hour >= 18 && hour <= 21)) {
      return 0.5;
    }
    // Daytime (9 AM - 6 PM)
    else {
      return 0.8;
    }
  }

  /// Auto-adjust brightness based on time of day
  Future<void> autoAdjustBrightness() async {
    final recommended = getRecommendedBrightness();
    await setBrightness(recommended, systemLevel: _isSupported);
  }

  /// Dispose and cleanup
  Future<void> dispose() async {
    if (_originalBrightness != null && _isSupported) {
      try {
        await resetToOriginal();
      } catch (e) {
        // Ignore errors during disposal
      }
    }
  }
}

/// Widget that applies brightness overlay to its child
class BrightnessOverlay extends StatelessWidget {
  final Widget child;
  final double brightness;
  final Duration animationDuration;

  const BrightnessOverlay({
    super.key,
    required this.child,
    required this.brightness,
    this.animationDuration = const Duration(milliseconds: 200),
  });

  @override
  Widget build(BuildContext context) {
    final validBrightness = BrightnessService.validateBrightness(brightness);
    
    if (validBrightness >= 1.0) {
      return child;
    }

    return AnimatedContainer(
      duration: animationDuration,
      child: Stack(
        children: [
          child,
          IgnorePointer(
            child: Container(
              color: Colors.black.withOpacity(1.0 - validBrightness),
            ),
          ),
        ],
      ),
    );
  }
}

/// Brightness control slider widget
class BrightnessSlider extends StatefulWidget {
  final double brightness;
  final ValueChanged<double> onChanged;
  final ValueChanged<double>? onChangeEnd;
  final Color? activeColor;
  final Color? inactiveColor;
  final bool showLabel;

  const BrightnessSlider({
    super.key,
    required this.brightness,
    required this.onChanged,
    this.onChangeEnd,
    this.activeColor,
    this.inactiveColor,
    this.showLabel = true,
  });

  @override
  State<BrightnessSlider> createState() => _BrightnessSliderState();
}

class _BrightnessSliderState extends State<BrightnessSlider> {
  double _currentBrightness = 1.0;

  @override
  void initState() {
    super.initState();
    _currentBrightness = widget.brightness;
  }

  @override
  void didUpdateWidget(BrightnessSlider oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.brightness != oldWidget.brightness) {
      _currentBrightness = widget.brightness;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        if (widget.showLabel)
          Padding(
            padding: const EdgeInsets.only(bottom: 8.0),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text('亮度'),
                Text(
                  '${(_currentBrightness * 100).round()}%',
                  style: Theme.of(context).textTheme.bodySmall,
                ),
              ],
            ),
          ),
        Row(
          children: [
            Icon(
              Icons.brightness_low,
              size: 20,
              color: Theme.of(context).iconTheme.color?.withOpacity(0.7),
            ),
            Expanded(
              child: SliderTheme(
                data: SliderTheme.of(context).copyWith(
                  activeTrackColor: widget.activeColor,
                  inactiveTrackColor: widget.inactiveColor,
                  thumbColor: widget.activeColor,
                  overlayColor: widget.activeColor?.withOpacity(0.2),
                ),
                child: Slider(
                  value: _currentBrightness,
                  min: BrightnessService.validateBrightness(0.0),
                  max: BrightnessService.validateBrightness(1.0),
                  divisions: 20,
                  onChanged: (value) {
                    setState(() {
                      _currentBrightness = value;
                    });
                    widget.onChanged(value);
                    // Provide haptic feedback
                    HapticFeedback.selectionClick();
                  },
                  onChangeEnd: widget.onChangeEnd,
                ),
              ),
            ),
            Icon(
              Icons.brightness_high,
              size: 20,
              color: Theme.of(context).iconTheme.color?.withOpacity(0.7),
            ),
          ],
        ),
      ],
    );
  }
}