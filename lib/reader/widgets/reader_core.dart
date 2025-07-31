import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:photo_view/photo_view.dart';
import 'package:photo_view/photo_view_gallery.dart';

import '../../models/reader_models.dart' as models;

/// Core comic reader widget that handles image display and interaction
/// 
/// This widget is responsible for:
/// - Displaying comic pages using PhotoView
/// - Supporting different reading modes (single, dual, continuous)
/// - Handling zoom, pan, and navigation gestures
/// - Managing page transitions and animations
class ReaderCore extends ConsumerWidget {
  const ReaderCore({
    super.key,
    required this.pages,
    required this.settings,
    required this.pageController,
    required this.currentPage,
    required this.onPageChanged,
    required this.onTap,
  });

  final List<Uint8List> pages;
  final models.ReaderSettings settings;
  final PageController pageController;
  final int currentPage;
  final void Function(int) onPageChanged;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    if (pages.isEmpty) {
      return Center(
        child: Text(
          '没有找到漫画页面',
          style: TextStyle(color: _getForegroundColor(settings.backgroundTheme)),
        ),
      );
    }

    return _buildReaderView();
  }

  /// Build reader view based on settings
  Widget _buildReaderView() {
    switch (settings.readingMode) {
      case models.ReadingMode.single:
        return _buildSinglePageView();
      case models.ReadingMode.dual:
        return _buildDualPageView();
      case models.ReadingMode.continuous:
        return _buildContinuousScrollView();
      case models.ReadingMode.vertical:
        return _buildContinuousScrollView(); // Alias for continuous
    }
  }

  /// Build single page view
  Widget _buildSinglePageView() {
    return PhotoViewGallery.builder(
      scrollPhysics: const BouncingScrollPhysics(),
      scrollDirection: settings.navigationDirection == models.NavigationDirection.vertical 
          ? Axis.vertical 
          : Axis.horizontal,
      reverse: settings.navigationDirection == models.NavigationDirection.rtl,
      builder: (BuildContext context, int index) =>
          PhotoViewGalleryPageOptions(
        imageProvider: MemoryImage(pages[index]),
        initialScale: PhotoViewComputedScale.contained,
        minScale: PhotoViewComputedScale.contained * 0.5,
        maxScale: PhotoViewComputedScale.covered * 3.0,
        heroAttributes: PhotoViewHeroAttributes(
          tag: 'comic_page_$index',
        ),
        onTapUp: (context, details, controllerValue) => onTap(),
      ),
      itemCount: pages.length,
      loadingBuilder: (context, event) => Center(
        child: CircularProgressIndicator(
          color: _getForegroundColor(settings.backgroundTheme),
        ),
      ),
      pageController: pageController,
      onPageChanged: onPageChanged,
      backgroundDecoration: BoxDecoration(
        color: settings.backgroundTheme.color,
      ),
    );
  }

  /// Build dual page view
  Widget _buildDualPageView() {
    return LayoutBuilder(
      builder: (context, constraints) {
        if (constraints.maxWidth < 600) {
          // Fall back to single page on narrow screens
          return _buildSinglePageView();
        }

        return PhotoViewGallery.builder(
          scrollPhysics: const BouncingScrollPhysics(),
          scrollDirection: settings.navigationDirection == models.NavigationDirection.vertical 
              ? Axis.vertical 
              : Axis.horizontal,
          reverse: settings.navigationDirection == models.NavigationDirection.rtl,
          builder: (BuildContext context, int index) {
            final firstPageIndex = index * 2;
            final secondPageIndex = firstPageIndex + 1;
            final hasSecondPage = secondPageIndex < pages.length;

            return PhotoViewGalleryPageOptions.customChild(
              child: Row(
                children: [
                  Expanded(
                    child: PhotoView(
                      imageProvider: MemoryImage(pages[firstPageIndex]),
                      initialScale: PhotoViewComputedScale.contained,
                      minScale: PhotoViewComputedScale.contained * 0.5,
                      maxScale: PhotoViewComputedScale.covered * 3.0,
                      heroAttributes: PhotoViewHeroAttributes(
                        tag: 'comic_page_$firstPageIndex',
                      ),
                      backgroundDecoration: BoxDecoration(
                        color: settings.backgroundTheme.color,
                      ),
                      onTapUp: (context, details, controllerValue) => onTap(),
                    ),
                  ),
                  if (hasSecondPage)
                    Expanded(
                      child: PhotoView(
                        imageProvider: MemoryImage(pages[secondPageIndex]),
                        initialScale: PhotoViewComputedScale.contained,
                        minScale: PhotoViewComputedScale.contained * 0.5,
                        maxScale: PhotoViewComputedScale.covered * 3.0,
                        heroAttributes: PhotoViewHeroAttributes(
                          tag: 'comic_page_$secondPageIndex',
                        ),
                        backgroundDecoration: BoxDecoration(
                          color: settings.backgroundTheme.color,
                        ),
                        onTapUp: (context, details, controllerValue) => onTap(),
                      ),
                    ),
                ],
              ),
              initialScale: PhotoViewComputedScale.contained,
              minScale: PhotoViewComputedScale.contained * 0.5,
              maxScale: PhotoViewComputedScale.covered * 3.0,
              heroAttributes: PhotoViewHeroAttributes(
                tag: 'dual_page_$index',
              ),
            );
          },
          itemCount: (pages.length / 2).ceil(),
          loadingBuilder: (context, event) => Center(
            child: CircularProgressIndicator(
              color: _getForegroundColor(settings.backgroundTheme),
            ),
          ),
          pageController: pageController,
          onPageChanged: (index) => onPageChanged(index * 2),
          backgroundDecoration: BoxDecoration(
            color: settings.backgroundTheme.color,
          ),
        );
      },
    );
  }

  /// Build continuous scroll view
  Widget _buildContinuousScrollView() {
    return ListView.builder(
      scrollDirection: settings.navigationDirection == models.NavigationDirection.vertical 
          ? Axis.vertical 
          : Axis.horizontal,
      reverse: settings.navigationDirection == models.NavigationDirection.rtl,
      itemCount: pages.length,
      itemBuilder: (context, index) {
        return Container(
          margin: const EdgeInsets.all(4.0),
          child: PhotoView(
            imageProvider: MemoryImage(pages[index]),
            initialScale: PhotoViewComputedScale.contained,
            minScale: PhotoViewComputedScale.contained * 0.5,
            maxScale: PhotoViewComputedScale.covered * 3.0,
            heroAttributes: PhotoViewHeroAttributes(
              tag: 'comic_page_$index',
            ),
            backgroundDecoration: BoxDecoration(
              color: settings.backgroundTheme.color,
            ),
            onTapUp: (context, details, controllerValue) => onTap(),
          ),
        );
      },
    );
  }

  /// Get foreground color based on background theme
  Color _getForegroundColor(models.BackgroundTheme theme) {
    switch (theme) {
      case models.BackgroundTheme.black:
      case models.BackgroundTheme.grey:
        return Colors.white;
      case models.BackgroundTheme.white:
      case models.BackgroundTheme.sepia:
        return Colors.black87;
    }
  }
}