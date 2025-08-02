import 'package:flutter/material.dart';
import 'package:photo_view/photo_view.dart';
import '../../../domain/entities/reader_settings.dart';

class PinchZoomController extends ChangeNotifier {
  final PhotoViewController _photoViewController;
  final GestureConfig _gestureConfig;
  
  double _currentScale = 1.0;
  double _minScale = 0.8;
  double _maxScale = 3.0;
  
  PinchZoomController({
    required PhotoViewController photoViewController,
    required GestureConfig gestureConfig,
  }) : _photoViewController = photoViewController,
       _gestureConfig = gestureConfig {
    _photoViewController.addListener(_onPhotoViewControllerChanged);
  }
  
  double get currentScale => _currentScale;
  double get minScale => _minScale;
  double get maxScale => _maxScale;
  
  void _onPhotoViewControllerChanged() {
    final newScale = _photoViewController.scale ?? 1.0;
    if (newScale != _currentScale) {
      _currentScale = newScale;
      notifyListeners();
    }
  }
  
  /// Handle pinch gesture with sensitivity adjustment
  void handlePinchGesture(ScaleUpdateDetails details) {
    final adjustedScale = _calculateAdjustedScale(details.scale);
    final constrainedScale = _constrainScale(adjustedScale);
    
    _photoViewController.scale = constrainedScale;
    
    // Optional haptic feedback
    if (_gestureConfig.enableHapticFeedback) {
      _triggerHapticFeedback(constrainedScale);
    }
  }
  
  double _calculateAdjustedScale(double rawScale) {
    // Apply sensitivity adjustment
    final sensitivity = _gestureConfig.pinchSensitivity;
    return _currentScale * (1.0 + (rawScale - 1.0) * sensitivity);
  }
  
  double _constrainScale(double scale) {
    return scale.clamp(_minScale, _maxScale);
  }
  
  void _triggerHapticFeedback(double scale) {
    // Provide haptic feedback at scale boundaries
    if (scale <= _minScale || scale >= _maxScale) {
      // Light haptic feedback at boundaries
      // Note: Would implement platform-specific haptic feedback
    }
  }
  
  /// Set zoom level with smooth animation
  Future<void> setZoomLevel(double targetScale, {Duration? duration}) async {
    final constrainedScale = _constrainScale(targetScale);
    
    if (duration != null) {
      // Animate to target scale
      await _animateToScale(constrainedScale, duration);
    } else {
      _photoViewController.scale = constrainedScale;
    }
  }
  
  Future<void> _animateToScale(double targetScale, Duration duration) async {
    final startScale = _currentScale;
    // Animation temporarily disabled due to missing TickerProvider context
    // Would need to be passed from widget that has access to TickerProvider
    _photoViewController.scale = targetScale;
  }
  
  /// Reset zoom to fit container
  void resetZoom() {
    _photoViewController.scale = PhotoViewComputedScale.contained;
  }
  
  /// Zoom to fit width
  void zoomToWidth() {
    _photoViewController.scale = PhotoViewComputedScale.covered;
  }
  
  /// Check if currently at minimum zoom
  bool get isAtMinZoom => _currentScale <= _minScale + 0.01;
  
  /// Check if currently at maximum zoom  
  bool get isAtMaxZoom => _currentScale >= _maxScale - 0.01;
  
  /// Update scale constraints
  void updateScaleConstraints({double? minScale, double? maxScale}) {
    if (minScale != null) _minScale = minScale;
    if (maxScale != null) _maxScale = maxScale;
    
    // Ensure current scale is within new constraints
    if (_currentScale < _minScale || _currentScale > _maxScale) {
      final constrainedScale = _constrainScale(_currentScale);
      _photoViewController.scale = constrainedScale;
    }
  }
  
  @override
  void dispose() {
    _photoViewController.removeListener(_onPhotoViewControllerChanged);
    super.dispose();
  }
}

/// Extension to provide computed scales
extension PhotoViewScaleExtension on PhotoViewComputedScale {
  static PhotoViewComputedScale get contained => PhotoViewComputedScale.contained;
  static PhotoViewComputedScale get covered => PhotoViewComputedScale.covered;
  
  static PhotoViewComputedScale scaleFor(double multiplier) {
    return PhotoViewComputedScale.covered * multiplier;
  }
}

/// Mixin for providing TickerProvider (would need to be implemented in actual usage)
mixin TickerProvider {
  static TickerProvider? of(BuildContext context) {
    return context.findAncestorStateOfType<TickerProviderStateMixin>();
  }
}