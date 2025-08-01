import 'dart:math';
import 'package:flutter/material.dart';
import '../../../domain/services/auto_page_service.dart';

class AutoPageProgressIndicator extends StatefulWidget {
  final AutoPageState state;
  final VoidCallback? onTap;
  final bool showProgressIndicator;
  
  const AutoPageProgressIndicator({
    Key? key,
    required this.state,
    this.onTap,
    this.showProgressIndicator = true,
  }) : super(key: key);
  
  @override
  State<AutoPageProgressIndicator> createState() => _AutoPageProgressIndicatorState();
}

class _AutoPageProgressIndicatorState extends State<AutoPageProgressIndicator> 
    with SingleTickerProviderStateMixin {
  late AnimationController _animationController;
  late Animation<double> _progressAnimation;
  
  @override
  void initState() {
    super.initState();
    _animationController = AnimationController(
      duration: const Duration(milliseconds: 100),
      vsync: this,
    );
    _updateProgressAnimation();
  }
  
  @override
  void didUpdateWidget(AutoPageProgressIndicator oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.state.remainingSeconds != widget.state.remainingSeconds ||
        oldWidget.state.intervalSeconds != widget.state.intervalSeconds) {
      _updateProgressAnimation();
    }
  }
  
  void _updateProgressAnimation() {
    final progress = widget.state.intervalSeconds > 0
        ? (widget.state.intervalSeconds - widget.state.remainingSeconds) / widget.state.intervalSeconds
        : 0.0;
    
    _progressAnimation = Tween<double>(
      begin: _progressAnimation?.value ?? 0.0,
      end: progress,
    ).animate(CurvedAnimation(
      parent: _animationController,
      curve: Curves.easeInOut,
    ));
    
    _animationController.reset();
    _animationController.forward();
  }
  
  @override
  void dispose() {
    _animationController.dispose();
    super.dispose();
  }
  
  @override
  Widget build(BuildContext context) {
    if (!widget.showProgressIndicator || !widget.state.isActive) {
      return const SizedBox.shrink();
    }
    
    return Positioned(
      bottom: 20,
      right: 20,
      child: GestureDetector(
        onTap: widget.onTap,
        child: Container(
          width: 60,
          height: 60,
          decoration: BoxDecoration(
            color: Colors.black.withOpacity(0.7),
            borderRadius: BorderRadius.circular(30),
            border: Border.all(
              color: Colors.white.withOpacity(0.3),
              width: 1,
            ),
          ),
          child: Stack(
            alignment: Alignment.center,
            children: [
              // Progress ring
              AnimatedBuilder(
                animation: _progressAnimation,
                builder: (context, child) {
                  return CustomPaint(
                    size: const Size(60, 60),
                    painter: CircularProgressPainter(
                      progress: _progressAnimation.value,
                      isPaused: widget.state.isPaused,
                    ),
                  );
                },
              ),
              // Center content
              Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(
                    widget.state.isPaused ? Icons.play_arrow : Icons.pause,
                    color: Colors.white,
                    size: 20,
                  ),
                  const SizedBox(height: 2),
                  Text(
                    '${widget.state.remainingSeconds}s',
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 10,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class CircularProgressPainter extends CustomPainter {
  final double progress;
  final bool isPaused;
  
  CircularProgressPainter({
    required this.progress,
    required this.isPaused,
  });
  
  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final radius = size.width / 2 - 4;
    
    // Background circle
    final backgroundPaint = Paint()
      ..color = Colors.white.withOpacity(0.2)
      ..strokeWidth = 3
      ..style = PaintingStyle.stroke;
    
    canvas.drawCircle(center, radius, backgroundPaint);
    
    // Progress arc
    final progressPaint = Paint()
      ..color = isPaused ? Colors.orange : Colors.green
      ..strokeWidth = 3
      ..style = PaintingStyle.stroke
      ..strokeCap = StrokeCap.round;
    
    final startAngle = -pi / 2; // Start from top
    final sweepAngle = 2 * pi * progress;
    
    canvas.drawArc(
      Rect.fromCircle(center: center, radius: radius),
      startAngle,
      sweepAngle,
      false,
      progressPaint,
    );
  }
  
  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) {
    return oldDelegate is CircularProgressPainter &&
           (oldDelegate.progress != progress || oldDelegate.isPaused != isPaused);
  }
}

/// Compact version for smaller spaces
class CompactAutoPageIndicator extends StatelessWidget {
  final AutoPageState state;
  final VoidCallback? onTap;
  
  const CompactAutoPageIndicator({
    Key? key,
    required this.state,
    this.onTap,
  }) : super(key: key);
  
  @override
  Widget build(BuildContext context) {
    if (!state.isActive) return const SizedBox.shrink();
    
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
        decoration: BoxDecoration(
          color: Colors.black.withOpacity(0.7),
          borderRadius: BorderRadius.circular(12),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              state.isPaused ? Icons.play_arrow : Icons.pause,
              color: Colors.white,
              size: 16,
            ),
            const SizedBox(width: 4),
            Text(
              '${state.remainingSeconds}s',
              style: const TextStyle(
                color: Colors.white,
                fontSize: 12,
                fontWeight: FontWeight.bold,
              ),
            ),
          ],
        ),
      ),
    );
  }
}