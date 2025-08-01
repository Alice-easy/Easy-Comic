import 'package:flutter/material.dart';
import '../../domain/entities/comic_page.dart';
import '../../domain/entities/reader_settings.dart';
import '../../domain/services/navigation_service.dart';
import 'reading_mode_renderer.dart';

/// Webtoon reading mode renderer for continuous vertical scrolling
class WebtoonModeRenderer extends ReadingModeRenderer {
  final ScrollController _scrollController;
  
  WebtoonModeRenderer({
    required super.settings,
    required super.navigationService,
    required ScrollController scrollController,
  }) : _scrollController = scrollController;

  @override
  Widget buildView(List<ComicPage> pages, int currentPageIndex) {
    return NotificationListener<ScrollNotification>(
      onNotification: (notification) {
        if (notification is ScrollUpdateNotification) {
          _handleScrollUpdate(notification, pages.length);
        }
        return false;
      },
      child: CustomScrollView(
        controller: _scrollController,
        slivers: [
          SliverList(
            delegate: SliverChildBuilderDelegate(
              (context, index) => _buildWebtoonPage(pages[index], index),
              childCount: pages.length,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildWebtoonPage(ComicPage page, int index) {
    return Container(
      width: double.infinity,
      color: settings.backgroundTheme.color,
      child: Image(
        image: _getImageProvider(page),
        fit: BoxFit.fitWidth,
        filterQuality: _getFilterQuality(),
        errorBuilder: (context, error, stackTrace) {
          return Container(
            height: 200,
            color: Colors.grey[900],
            child: const Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.error, color: Colors.white, size: 48),
                  SizedBox(height: 8),
                  Text(
                    '图片加载失败',
                    style: TextStyle(color: Colors.white),
                  ),
                ],
              ),
            ),
          );
        },
        loadingBuilder: (context, child, loadingProgress) {
          if (loadingProgress == null) return child;
          return Container(
            height: 200,
            color: Colors.grey[900],
            child: Center(
              child: CircularProgressIndicator(
                value: loadingProgress.expectedTotalBytes != null
                    ? loadingProgress.cumulativeBytesLoaded /
                        loadingProgress.expectedTotalBytes!
                    : null,
                color: Colors.white,
              ),
            ),
          );
        },
      ),
    );
  }

  @override
  Future<void> handlePageNavigation(NavigationDirection direction) async {
    // For webtoon mode, navigate by scrolling to specific positions
    final viewportHeight = _scrollController.position.viewportDimension;
    final currentOffset = _scrollController.offset;
    
    if (direction == NavigationDirection.forward) {
      // Scroll down by viewport height
      final targetOffset = currentOffset + viewportHeight;
      final maxOffset = _scrollController.position.maxScrollExtent;
      
      await _scrollController.animateTo(
        targetOffset > maxOffset ? maxOffset : targetOffset,
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeInOut,
      );
    } else {
      // Scroll up by viewport height
      final targetOffset = currentOffset - viewportHeight;
      
      await _scrollController.animateTo(
        targetOffset < 0 ? 0 : targetOffset,
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeInOut,
      );
    }
  }

  @override
  Future<void> preserveReadingPosition(int currentPage, double scrollOffset) async {
    // For webtoon mode, the scroll offset is the primary position indicator
    await navigationService.saveReadingPosition(currentPage, _scrollController.offset);
  }

  void _handleScrollUpdate(ScrollUpdateNotification notification, int totalPages) {
    // Calculate current page based on scroll position
    // This is a simplified calculation - in reality would need page heights
    final scrollPercentage = notification.metrics.pixels / notification.metrics.maxScrollExtent;
    final currentPage = (scrollPercentage * totalPages).floor().clamp(0, totalPages - 1);
    
    onPageChanged(currentPage);
  }

  ImageProvider _getImageProvider(ComicPage page) {
    if (page.imagePath.startsWith('http')) {
      return NetworkImage(page.imagePath);
    } else {
      return AssetImage(page.imagePath);
    }
  }

  FilterQuality _getFilterQuality() {
    // Map reader settings to Flutter FilterQuality
    switch (settings.cacheConfig?.defaultQuality) {
      case ImageQuality.thumbnail:
        return FilterQuality.low;
      case ImageQuality.medium:
        return FilterQuality.medium;
      case ImageQuality.high:
      case ImageQuality.original:
        return FilterQuality.high;
      default:
        return FilterQuality.medium;
    }
  }
}