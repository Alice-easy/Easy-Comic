import 'dart:typed_data';
import 'package:flutter/material.dart';
import '../../core/thumbnail_service.dart';

/// Enhanced progress bar with thumbnail previews
/// Shows thumbnail preview when user drags the slider
class ThumbnailProgressBar extends StatefulWidget {
  final double value;
  final double min;
  final double max;
  final ValueChanged<double>? onChanged;
  final ValueChanged<double>? onChangeEnd;
  final List<Uint8List>? pages;
  final Color? activeColor;
  final Color? inactiveColor;
  final bool showThumbnail;

  const ThumbnailProgressBar({
    super.key,
    required this.value,
    this.min = 0.0,
    this.max = 1.0,
    this.onChanged,
    this.onChangeEnd,
    this.pages,
    this.activeColor,
    this.inactiveColor,
    this.showThumbnail = true,
  });

  @override
  State<ThumbnailProgressBar> createState() => _ThumbnailProgressBarState();
}

class _ThumbnailProgressBarState extends State<ThumbnailProgressBar>
    with TickerProviderStateMixin {
  OverlayEntry? _overlayEntry;
  bool _isDragging = false;
  double _dragValue = 0.0;
  String? _thumbnailPath;
  bool _thumbnailLoading = false;

  late AnimationController _fadeController;
  late Animation<double> _fadeAnimation;

  @override
  void initState() {
    super.initState();
    _fadeController = AnimationController(
      duration: const Duration(milliseconds: 200),
      vsync: this,
    );
    _fadeAnimation = Tween<double>(begin: 0.0, end: 1.0).animate(
      CurvedAnimation(parent: _fadeController, curve: Curves.easeInOut),
    );
  }

  @override
  void dispose() {
    _removeOverlay();
    _fadeController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return SliderTheme(
      data: SliderTheme.of(context).copyWith(
        trackHeight: 4.0,
        thumbShape: const RoundSliderThumbShape(enabledThumbRadius: 8.0),
        overlayShape: const RoundSliderOverlayShape(overlayRadius: 16.0),
        activeTrackColor: widget.activeColor,
        inactiveTrackColor: widget.inactiveColor,
        thumbColor: widget.activeColor,
        overlayColor: widget.activeColor?.withOpacity(0.2),
      ),
      child: Slider(
        value: widget.value.clamp(widget.min, widget.max),
        min: widget.min,
        max: widget.max,
        onChanged: _handleSliderChange,
        onChangeStart: _handleSliderStart,
        onChangeEnd: _handleSliderEnd,
      ),
    );
  }

  void _handleSliderStart(double value) {
    if (!widget.showThumbnail || widget.pages == null) return;
    
    setState(() {
      _isDragging = true;
      _dragValue = value;
    });
    
    _showThumbnailOverlay();
    _fadeController.forward();
  }

  void _handleSliderChange(double value) {
    widget.onChanged?.call(value);
    
    if (_isDragging && widget.showThumbnail) {
      setState(() {
        _dragValue = value;
      });
      _updateThumbnail();
    }
  }

  void _handleSliderEnd(double value) {
    setState(() {
      _isDragging = false;
    });
    
    _fadeController.reverse().then((_) {
      _removeOverlay();
    });
    
    widget.onChangeEnd?.call(value);
  }

  void _showThumbnailOverlay() {
    if (_overlayEntry != null) return;
    
    _overlayEntry = OverlayEntry(
      builder: (context) => Positioned(
        left: 0,
        right: 0,
        bottom: 120, // Position above the slider
        child: Center(
          child: FadeTransition(
            opacity: _fadeAnimation,
            child: _buildThumbnailPreview(),
          ),
        ),
      ),
    );
    
    Overlay.of(context).insert(_overlayEntry!);
  }

  void _removeOverlay() {
    _overlayEntry?.remove();
    _overlayEntry = null;
  }

  Widget _buildThumbnailPreview() {
    return Container(
      padding: const EdgeInsets.all(12.0),
      decoration: BoxDecoration(
        color: Colors.black.withOpacity(0.8),
        borderRadius: BorderRadius.circular(8.0),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.3),
            blurRadius: 8.0,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          SizedBox(
            width: 120,
            height: 160,
            child: _buildThumbnailImage(),
          ),
          const SizedBox(height: 8),
          Text(
            '第 ${_getCurrentPageNumber()} 页',
            style: const TextStyle(
              color: Colors.white,
              fontSize: 12,
              fontWeight: FontWeight.w500,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildThumbnailImage() {
    if (_thumbnailLoading) {
      return const Center(
        child: CircularProgressIndicator(
          color: Colors.white,
          strokeWidth: 2.0,
        ),
      );
    }

    if (_thumbnailPath != null) {
      return ThumbnailImage(
        thumbnailKey: 'page_${_getCurrentPageIndex()}',
        width: 120,
        height: 160,
        fit: BoxFit.contain,
      );
    }

    // Show original image if thumbnail not available
    final pageIndex = _getCurrentPageIndex();
    if (widget.pages != null && 
        pageIndex >= 0 && 
        pageIndex < widget.pages!.length) {
      return Image.memory(
        widget.pages![pageIndex],
        width: 120,
        height: 160,
        fit: BoxFit.contain,
        errorBuilder: (context, error, stackTrace) {
          return const Center(
            child: Icon(
              Icons.image_not_supported,
              color: Colors.white54,
              size: 32,
            ),
          );
        },
      );
    }

    return const Center(
      child: Icon(
        Icons.image,
        color: Colors.white54,
        size: 32,
      ),
    );
  }

  int _getCurrentPageIndex() {
    if (widget.max <= widget.min) return 0;
    
    final progress = (_dragValue - widget.min) / (widget.max - widget.min);
    final pageIndex = (progress * (widget.max - widget.min)).round();
    return pageIndex.clamp(0, widget.max.toInt());
  }

  int _getCurrentPageNumber() => _getCurrentPageIndex() + 1;

  void _updateThumbnail() {
    if (!widget.showThumbnail || widget.pages == null) return;
    
    final pageIndex = _getCurrentPageIndex();
    if (pageIndex < 0 || pageIndex >= widget.pages!.length) return;
    
    final thumbnailKey = 'page_$pageIndex';
    
    setState(() {
      _thumbnailLoading = true;
    });
    
    // Try to get cached thumbnail first
    ThumbnailService.getCachedThumbnail(thumbnailKey).then((cachedPath) {
      if (cachedPath != null) {
        if (mounted) {
          setState(() {
            _thumbnailPath = cachedPath;
            _thumbnailLoading = false;
          });
        }
      } else {
        // Generate thumbnail in background
        ThumbnailService.generateThumbnail(
          widget.pages![pageIndex],
          thumbnailKey,
        ).then((generatedPath) {
          if (mounted) {
            setState(() {
              _thumbnailPath = generatedPath;
              _thumbnailLoading = false;
            });
          }
        }).catchError((_) {
          if (mounted) {
            setState(() {
              _thumbnailPath = null;
              _thumbnailLoading = false;
            });
          }
        });
      }
    }).catchError((_) {
      if (mounted) {
        setState(() {
          _thumbnailPath = null;
          _thumbnailLoading = false;
        });
      }
    });
  }
}

/// Simple progress bar without thumbnail preview
/// For cases where thumbnails are not needed or available
class SimpleProgressBar extends StatelessWidget {
  final double value;
  final double min;
  final double max;
  final ValueChanged<double>? onChanged;
  final ValueChanged<double>? onChangeEnd;
  final Color? activeColor;
  final Color? inactiveColor;
  final bool showPageNumbers;

  const SimpleProgressBar({
    super.key,
    required this.value,
    this.min = 0.0,
    this.max = 1.0,
    this.onChanged,
    this.onChangeEnd,
    this.activeColor,
    this.inactiveColor,
    this.showPageNumbers = true,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        if (showPageNumbers)
          Text(
            '${(value + 1).toInt()}',
            style: TextStyle(
              color: activeColor ?? Theme.of(context).primaryColor,
              fontSize: 12,
            ),
          ),
        if (showPageNumbers) const SizedBox(width: 8),
        Expanded(
          child: SliderTheme(
            data: SliderTheme.of(context).copyWith(
              trackHeight: 4.0,
              thumbShape: const RoundSliderThumbShape(enabledThumbRadius: 8.0),
              overlayShape: const RoundSliderOverlayShape(overlayRadius: 16.0),
              activeTrackColor: activeColor,
              inactiveTrackColor: inactiveColor,
              thumbColor: activeColor,
              overlayColor: activeColor?.withOpacity(0.2),
            ),
            child: Slider(
              value: value.clamp(min, max),
              min: min,
              max: max,
              onChanged: onChanged,
              onChangeEnd: onChangeEnd,
            ),
          ),
        ),
        if (showPageNumbers) const SizedBox(width: 8),
        if (showPageNumbers)
          Text(
            '${(max + 1).toInt()}',
            style: TextStyle(
              color: inactiveColor ?? Theme.of(context).disabledColor,
              fontSize: 12,
            ),
          ),
      ],
    );
  }
}