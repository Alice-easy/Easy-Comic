import 'dart:async';
import 'package:flutter/material.dart';
import 'package:photo_view/photo_view.dart';
import '../../../domain/entities/reader_settings.dart';

enum ZoomLevel {
  fitToScreen,
  twoX,
  threeX,
}

class DoubleTapZoomController extends ChangeNotifier {
  final PhotoViewController _photoViewController;
  final GestureConfig _gestureConfig;
  final AnimationController _animationController;
  
  ZoomLevel _currentZoomLevel = ZoomLevel.fitToScreen;
  Timer? _doubleTapTimer;
  int _tapCount = 0;
  
  DoubleTapZoomController({
    required PhotoViewController photoViewController,
    required GestureConfig gestureConfig,
    required TickerProvider tickerProvider,
  }) : _photoViewController = photoViewController,
       _gestureConfig = gestureConfig,
       _animationController = AnimationController(
         duration: const Duration(milliseconds: 300),
         vsync: tickerProvider,
       );
  
  ZoomLevel get currentZoomLevel => _currentZoomLevel;
  
  /// Handle tap events for double-tap detection
  void handleTap(TapUpDetails details) {
    _tapCount++;
    
    if (_tapCount == 1) {
      // Start timer for double-tap detection
      _doubleTapTimer = Timer(_gestureConfig.doubleTapTimeout, () {
        // Single tap timeout - reset counter
        _tapCount = 0;
      });
    } else if (_tapCount == 2) {
      // Double tap detected
      _doubleTapTimer?.cancel();
      _tapCount = 0;
      _handleDoubleTap(details);
    }
  }
  
  void _handleDoubleTap(TapUpDetails details) {
    if (!_gestureConfig.tapZoneConfig.enableTapToFlip) return;
    
    // Cycle to next zoom level
    final nextZoomLevel = _getNextZoomLevel();
    _zoomToLevel(nextZoomLevel, details.globalPosition);
    
    // Haptic feedback if enabled
    if (_gestureConfig.enableHapticFeedback) {
      _triggerHapticFeedback();
    }
  }
  
  ZoomLevel _getNextZoomLevel() {
    switch (_currentZoomLevel) {
      case ZoomLevel.fitToScreen:
        return ZoomLevel.twoX;
      case ZoomLevel.twoX:
        return ZoomLevel.threeX;
      case ZoomLevel.threeX:
        return ZoomLevel.fitToScreen;
    }
  }
  
  Future<void> _zoomToLevel(ZoomLevel targetLevel, Offset? focalPoint) async {
    final targetScale = _getScaleForZoomLevel(targetLevel);
    
    // Create zoom animation
    final animation = Tween<double>(
      begin: _photoViewController.scale ?? 1.0,
      end: targetScale,
    ).animate(CurvedAnimation(
      parent: _animationController,
      curve: Curves.easeInOut,
    ));
    
    // Handle focal point for zoom
    if (focalPoint != null && targetLevel != ZoomLevel.fitToScreen) {
      _setZoomFocalPoint(focalPoint);
    }
    
    animation.addListener(() {
      _photoViewController.scale = animation.value;
    });
    
    _animationController.reset();
    await _animationController.forward();
    
    _currentZoomLevel = targetLevel;
    notifyListeners();
  }
  
  double _getScaleForZoomLevel(ZoomLevel level) {
    switch (level) {
      case ZoomLevel.fitToScreen:
        return PhotoViewComputedScale.contained.multiplier;
      case ZoomLevel.twoX:
        return PhotoViewComputedScale.covered.multiplier * 2.0;
      case ZoomLevel.threeX:
        return PhotoViewComputedScale.covered.multiplier * 3.0;
    }
  }
  
  void _setZoomFocalPoint(Offset focalPoint) {
    // Convert screen coordinates to image coordinates for proper focal point
    // This would require additional calculation based on current transform
    // For now, we'll use the center as focal point
    _photoViewController.position = Offset.zero;
  }
  
  void _triggerHapticFeedback() {
    // Platform-specific haptic feedback implementation would go here
    // For now, this is a placeholder
  }
  
  /// Manually set zoom level
  Future<void> setZoomLevel(ZoomLevel level, {Offset? focalPoint}) async {
    if (level != _currentZoomLevel) {
      await _zoomToLevel(level, focalPoint);
    }
  }
  
  /// Check if double-tap zoom is enabled
  bool get isDoubleTapZoomEnabled => _gestureConfig.tapZoneConfig.enableTapToFlip;
  
  /// Get zoom level name for UI display
  String get currentZoomLevelName {
    switch (_currentZoomLevel) {
      case ZoomLevel.fitToScreen:
        return '适应屏幕';
      case ZoomLevel.twoX:
        return '2倍缩放'; 
      case ZoomLevel.threeX:
        return '3倍缩放';
    }
  }
  
  /// Reset to fit screen
  Future<void> resetZoom() async {
    await setZoomLevel(ZoomLevel.fitToScreen);
  }
  
  @override
  void dispose() {
    _doubleTapTimer?.cancel();
    _animationController.dispose();
    super.dispose();
  }
}

/// Extension for PhotoViewComputedScale multiplier access
extension PhotoViewComputedScaleExtension on PhotoViewComputedScale {
  double get multiplier {
    // This would need to be implemented based on PhotoView internals
    // For now, returning reasonable defaults
    if (this == PhotoViewComputedScale.contained) return 1.0;
    if (this == PhotoViewComputedScale.covered) return 1.5;
    return 1.0;
  }
}