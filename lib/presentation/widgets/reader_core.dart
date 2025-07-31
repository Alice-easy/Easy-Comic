import 'package:flutter/material.dart';
import 'package:photo_view/photo_view.dart';
import 'package:photo_view/photo_view_gallery.dart';
import '../../domain/entities/comic.dart';
import '../../domain/entities/comic_page.dart';
import '../../domain/entities/reader_settings.dart';
import '../../presentation/bloc/reader/reader_event.dart';

class ReaderCore extends StatefulWidget {
  final Comic comic;
  final List<ComicPage> pages;
  final int currentPageIndex;
  final ReaderSettings settings;
  final ValueChanged<int> onPageChanged;
  final ValueChanged<GestureType> onGesture;
  final ValueChanged<double> onZoomChanged;

  const ReaderCore({
    Key? key,
    required this.comic,
    required this.pages,
    required this.currentPageIndex,
    required this.settings,
    required this.onPageChanged,
    required this.onGesture,
    required this.onZoomChanged,
  }) : super(key: key);

  @override
  State<ReaderCore> createState() => _ReaderCoreState();
}

class _ReaderCoreState extends State<ReaderCore> {
  late PageController _pageController;
  late PhotoViewController _photoViewController;
  
  @override
  void initState() {
    super.initState();
    _pageController = PageController(initialPage: widget.currentPageIndex);
    _photoViewController = PhotoViewController();
  }

  @override
  void didUpdateWidget(ReaderCore oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.currentPageIndex != widget.currentPageIndex) {
      _pageController.animateToPage(
        widget.currentPageIndex,
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeInOut,
      );
    }
  }

  @override
  void dispose() {
    _pageController.dispose();
    _photoViewController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (widget.pages.isEmpty) {
      return const Center(
        child: Text(
          '没有可显示的页面',
          style: TextStyle(color: Colors.white, fontSize: 16),
        ),
      );
    }

    return _buildPageView();
  }

  Widget _buildPageView() {
    switch (widget.settings.readingMode) {
      case ReadingMode.leftToRight:
        return _buildHorizontalPageView(false);
      case ReadingMode.rightToLeft:
        return _buildHorizontalPageView(true);
      case ReadingMode.vertical:
        return _buildVerticalPageView();
      case ReadingMode.webtoon:
        return _buildWebtoonView();
    }
  }

  Widget _buildHorizontalPageView(bool reverse) {
    return PhotoViewGallery.builder(
      pageController: _pageController,
      itemCount: widget.pages.length,
      reverse: reverse,
      scrollDirection: Axis.horizontal,
      onPageChanged: (index) {
        widget.onPageChanged(index);
      },
      builder: (context, index) {
        return _buildPhotoViewItem(widget.pages[index]);
      },
      backgroundDecoration: const BoxDecoration(
        color: Colors.black,
      ),
      gaplessPlayback: true,
      allowImplicitScrolling: true,
    );
  }

  Widget _buildVerticalPageView() {
    return PhotoViewGallery.builder(
      pageController: _pageController,
      itemCount: widget.pages.length,
      scrollDirection: Axis.vertical,
      onPageChanged: (index) {
        widget.onPageChanged(index);
      },
      builder: (context, index) {
        return _buildPhotoViewItem(widget.pages[index]);
      },
      backgroundDecoration: const BoxDecoration(
        color: Colors.black,
      ),
      gaplessPlayback: true,
      allowImplicitScrolling: true,
    );
  }

  Widget _buildWebtoonView() {
    return CustomScrollView(
      slivers: [
        SliverList(
          delegate: SliverChildBuilderDelegate(
            (context, index) {
              return _buildWebtoonPage(widget.pages[index]);
            },
            childCount: widget.pages.length,
          ),
        ),
      ],
    );
  }

  Widget _buildPhotoViewItem(ComicPage page) {
    return PhotoViewGalleryPageOptions(
      imageProvider: _getImageProvider(page),
      minScale: PhotoViewComputedScale.contained,
      maxScale: PhotoViewComputedScale.covered * 3.0,
      initialScale: PhotoViewComputedScale.contained,
      heroAttributes: PhotoViewHeroAttributes(tag: page.id),
      onScaleEnd: (context, details, controllerValue) {
        widget.onZoomChanged(controllerValue.scale ?? 1.0);
      },
      gestureDetectorBehavior: HitTestBehavior.translucent,
      onTapUp: (context, details, controllerValue) {
        _handleTap(details.globalPosition);
      },
      filterQuality: FilterQuality.high,
    );
  }

  Widget _buildWebtoonPage(ComicPage page) {
    return GestureDetector(
      onTap: () {
        widget.onGesture(GestureType.tapCenter);
      },
      child: Container(
        width: double.infinity,
        color: Colors.black,
        child: Image(
          image: _getImageProvider(page),
          fit: BoxFit.contain,
          filterQuality: FilterQuality.high,
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
      ),
    );
  }

  ImageProvider _getImageProvider(ComicPage page) {
    if (page.imagePath.startsWith('http')) {
      return NetworkImage(page.imagePath);
    } else {
      return AssetImage(page.imagePath);
    }
  }

  void _handleTap(Offset globalPosition) {
    final screenWidth = MediaQuery.of(context).size.width;
    final tapX = globalPosition.dx;
    
    // Define tap zones based on settings
    final leftZoneWidth = screenWidth * widget.settings.tapZoneConfig.leftZoneSize;
    final rightZoneWidth = screenWidth * widget.settings.tapZoneConfig.rightZoneSize;
    
    if (tapX < leftZoneWidth) {
      // Left zone - previous page (or next page in RTL mode)
      widget.onGesture(widget.settings.readingMode == ReadingMode.rightToLeft 
        ? GestureType.tapRight 
        : GestureType.tapLeft);
    } else if (tapX > screenWidth - rightZoneWidth) {
      // Right zone - next page (or previous page in RTL mode)
      widget.onGesture(widget.settings.readingMode == ReadingMode.rightToLeft 
        ? GestureType.tapLeft 
        : GestureType.tapRight);
    } else {
      // Center zone - toggle UI
      widget.onGesture(GestureType.tapCenter);
    }
  }
}