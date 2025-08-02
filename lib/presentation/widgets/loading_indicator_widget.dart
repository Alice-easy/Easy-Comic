import 'package:flutter/material.dart';

/// A customizable loading indicator widget with progress and message support
class LoadingIndicatorWidget extends StatelessWidget {
  final double? progress;
  final String? message;
  final String? operation;
  final bool showPercentage;
  final Color? color;
  final double size;

  const LoadingIndicatorWidget({
    super.key,
    this.progress,
    this.message,
    this.operation,
    this.showPercentage = true,
    this.color,
    this.size = 48.0,
  });

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          // Progress indicator
          SizedBox(
            width: size,
            height: size,
            child: progress != null
                ? CircularProgressIndicator(
                    value: progress! / 100,
                    color: color,
                    strokeWidth: 4.0,
                  )
                : CircularProgressIndicator(
                    color: color,
                    strokeWidth: 4.0,
                  ),
          ),
          
          const SizedBox(height: 16),
          
          // Progress percentage
          if (progress != null && showPercentage)
            Text(
              '${progress!.toStringAsFixed(0)}%',
              style: const TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.bold,
              ),
            ),
          
          // Operation text
          if (operation != null) ...[
            const SizedBox(height: 8),
            Text(
              operation!,
              style: const TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.w500,
              ),
            ),
          ],
          
          // Message text
          if (message != null) ...[
            const SizedBox(height: 8),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 32),
              child: Text(
                message!,
                style: const TextStyle(color: Colors.grey),
                textAlign: TextAlign.center,
                maxLines: 3,
                overflow: TextOverflow.ellipsis,
              ),
            ),
          ],
        ],
      ),
    );
  }
}

/// A shimmer loading effect for placeholders
class ShimmerLoadingWidget extends StatefulWidget {
  final Widget child;
  final Color? baseColor;
  final Color? highlightColor;
  final Duration duration;

  const ShimmerLoadingWidget({
    super.key,
    required this.child,
    this.baseColor,
    this.highlightColor,
    this.duration = const Duration(milliseconds: 1500),
  });

  @override
  State<ShimmerLoadingWidget> createState() => _ShimmerLoadingWidgetState();
}

class _ShimmerLoadingWidgetState extends State<ShimmerLoadingWidget>
    with SingleTickerProviderStateMixin {
  late AnimationController _animationController;
  late Animation<double> _animation;

  @override
  void initState() {
    super.initState();
    _animationController = AnimationController(
      duration: widget.duration,
      vsync: this,
    );
    _animation = Tween<double>(begin: -2.0, end: 2.0).animate(
      CurvedAnimation(parent: _animationController, curve: Curves.easeInOut),
    );
    _animationController.repeat();
  }

  @override
  void dispose() {
    _animationController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final baseColor = widget.baseColor ?? Colors.grey[300]!;
    final highlightColor = widget.highlightColor ?? Colors.grey[100]!;

    return AnimatedBuilder(
      animation: _animation,
      builder: (context, child) {
        return ShaderMask(
          shaderCallback: (bounds) {
            return LinearGradient(
              colors: [baseColor, highlightColor, baseColor],
              stops: const [0.0, 0.5, 1.0],
              begin: Alignment(-1.0 + _animation.value, 0.0),
              end: Alignment(1.0 + _animation.value, 0.0),
            ).createShader(bounds);
          },
          child: widget.child,
        );
      },
    );
  }
}