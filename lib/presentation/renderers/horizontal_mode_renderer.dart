import 'package:flutter/material.dart';
import 'package:photo_view/photo_view.dart';
import 'package:photo_view/photo_view_gallery.dart';
import '../../domain/entities/comic_page.dart';
import '../../domain/entities/reader_settings.dart';
import '../../domain/services/navigation_service.dart';
import 'reading_mode_renderer.dart';

/// Horizontal reading mode renderer for left-to-right and right-to-left reading
class HorizontalModeRenderer extends ReadingModeRenderer {
  final PageController _pageController;
  final bool reverse;
  
  HorizontalModeRenderer({
    required super.settings,
    required super.navigationService,
    required PageController pageController,
  }) : _pageController = pageController,
       reverse = settings.readingMode == ReadingMode.rightToLeft;

  @override
  Widget buildView(List<ComicPage> pages, int currentPageIndex) {
    return PhotoViewGallery.builder(
      pageController: _pageController,
      itemCount: pages.length,
      reverse: reverse,
      scrollDirection: Axis.horizontal,
      onPageChanged: onPageChanged,
      builder: (context, index) => PhotoViewGalleryPageOptions(
        imageProvider: _getImageProvider(pages[index]),
        minScale: PhotoViewComputedScale.contained,
        maxScale: PhotoViewComputedScale.covered * 3.0,
        initialScale: PhotoViewComputedScale.contained,
        heroAttributes: PhotoViewHeroAttributes(tag: pages[index].id),
        filterQuality: _getFilterQuality(),
      ),
      backgroundDecoration: BoxDecoration(
        color: settings.backgroundTheme.color,
      ),
      gaplessPlayback: true,
      allowImplicitScrolling: true,
    );
  }

  @override
  Future<void> handlePageNavigation(NavigationDirection direction) async {
    if (shouldNavigateForward(direction)) {
      await navigationService.navigateToNextPage();
    } else {
      await navigationService.navigateToPreviousPage();
    }
  }

  @override
  Future<void> preserveReadingPosition(int currentPage, double scrollOffset) async {
    // For horizontal mode, the page index is the primary position indicator
    await navigationService.saveReadingPosition(currentPage, 0.0);
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