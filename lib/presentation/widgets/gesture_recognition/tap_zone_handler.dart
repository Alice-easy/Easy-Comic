import 'package:flutter/material.dart';
import '../../../domain/entities/reader_settings.dart';

enum TapZone {
  left,
  right,
  center,
}

class TapZoneHandler {
  final TapZoneConfig config;
  
  const TapZoneHandler({required this.config});
  
  TapZone getTapZone(Offset globalPosition, Size screenSize) {
    final tapX = globalPosition.dx;
    final screenWidth = screenSize.width;
    
    final leftZoneWidth = screenWidth * config.leftZoneSize;
    final rightZoneWidth = screenWidth * config.rightZoneSize;
    
    if (tapX < leftZoneWidth) {
      return TapZone.left;
    } else if (tapX > screenWidth - rightZoneWidth) {
      return TapZone.right;
    } else {
      return TapZone.center;
    }
  }
  
  bool shouldHandleTap(TapZone zone) {
    return config.enableTapToFlip || zone == TapZone.center;
  }
  
  /// Convert tap zone to gesture type based on reading mode
  GestureType tapZoneToGesture(TapZone zone, ReadingMode readingMode) {
    switch (zone) {
      case TapZone.left:
        return readingMode == ReadingMode.rightToLeft 
          ? GestureType.tapRight 
          : GestureType.tapLeft;
      case TapZone.right:
        return readingMode == ReadingMode.rightToLeft 
          ? GestureType.tapLeft 
          : GestureType.tapRight;
      case TapZone.center:
        return GestureType.tapCenter;
    }
  }
}

/// Widget that visualizes tap zones for debugging
class TapZoneDebugOverlay extends StatelessWidget {
  final TapZoneConfig config;
  final bool showZones;
  
  const TapZoneDebugOverlay({
    Key? key,
    required this.config,
    this.showZones = false,
  }) : super(key: key);
  
  @override
  Widget build(BuildContext context) {
    if (!showZones) return const SizedBox.shrink();
    
    return IgnorePointer(
      child: Container(
        width: double.infinity,
        height: double.infinity,
        child: CustomPaint(
          painter: TapZonePainter(config),
        ),
      ),
    );
  }
}

class TapZonePainter extends CustomPainter {
  final TapZoneConfig config;
  
  TapZonePainter(this.config);
  
  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..style = PaintingStyle.fill
      ..color = Colors.red.withOpacity(0.2);
    
    final leftZoneWidth = size.width * config.leftZoneSize;
    final rightZoneWidth = size.width * config.rightZoneSize;
    
    // Draw left zone
    canvas.drawRect(
      Rect.fromLTWH(0, 0, leftZoneWidth, size.height),
      paint,
    );
    
    // Draw right zone
    canvas.drawRect(
      Rect.fromLTWH(size.width - rightZoneWidth, 0, rightZoneWidth, size.height),
      paint,
    );
    
    // Draw center zone outline
    paint.style = PaintingStyle.stroke;
    paint.color = Colors.blue.withOpacity(0.5);
    paint.strokeWidth = 2.0;
    
    canvas.drawRect(
      Rect.fromLTWH(leftZoneWidth, 0, size.width - leftZoneWidth - rightZoneWidth, size.height),
      paint,
    );
  }
  
  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

enum GestureType {
  tapLeft,
  tapRight,
  tapCenter,
  doubleTap,
  longPress,
  swipeLeft,
  swipeRight,
  swipeUp,
  swipeDown,
  pinchIn,
  pinchOut,
}