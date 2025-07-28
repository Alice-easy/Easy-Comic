import 'dart:convert';
import 'dart:typed_data';
import 'package:crypto/crypto.dart';
import 'package:firebase_analytics/firebase_analytics.dart';
import 'package:firebase_crashlytics/firebase_crashlytics.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:photo_view/photo_view.dart';
import 'package:photo_view/photo_view_gallery.dart';

import '../core/comic_archive.dart';
import '../data/drift_db.dart';
import '../home/home_page.dart';

class ReaderPage extends ConsumerStatefulWidget {
  const ReaderPage({
    required this.comicArchive,
    this.initialPage = 0,
    super.key,
  });

  final ComicArchive comicArchive;
  final int initialPage;

  @override
  ConsumerState<ReaderPage> createState() => _ReaderPageState();
}

class _ReaderPageState extends ConsumerState<ReaderPage> {
  late PageController _pageController;
  List<Uint8List>? _pages;
  bool _isLoading = true;
  String? _error;
  int _currentPage = 0;
  late DateTime _startTime;

  @override
  void initState() {
    super.initState();
    _startTime = DateTime.now();
    _currentPage = widget.initialPage;
    _pageController = PageController(initialPage: widget.initialPage);
    _loadPages();
  }

  @override
  void dispose() {
    _pageController.dispose();
    _saveReadingSession();
    super.dispose();
  }

  Future<void> _saveReadingSession() async {
    final db = ref.read(dbProvider);
    final endTime = DateTime.now();
    final duration = endTime.difference(_startTime);
    final fileHash = sha1
        .convert(utf8.encode(widget.comicArchive.path!))
        .toString();
    await db.addReadingSession(
      ReadingSession(
        id: 0,
        fileHash: fileHash,
        startTime: _startTime,
        endTime: endTime,
        durationInSeconds: duration.inSeconds,
      ),
    );
  }

  Future<void> _loadPages() async {
    try {
      final pages = await widget.comicArchive.listPages();
      // 设置 fileHash 自定义键
      final fileHash = widget.comicArchive.path?.split('/').last;
      if (fileHash != null) {
        await FirebaseCrashlytics.instance.setCustomKey('fileHash', fileHash);
      }
      if (mounted) {
        setState(() {
          _pages = pages;
          _isLoading = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _error = e.toString();
          _isLoading = false;
        });
      }
    }
  }

  void _onPageChanged(int index) {
    setState(() {
      _currentPage = index;
    });
    FirebaseAnalytics.instance.logEvent(
      name: 'page_flipped',
      parameters: {'page': index},
    );
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    backgroundColor: Colors.black,
    appBar: AppBar(
      backgroundColor: Colors.black.withAlpha((255 * 0.7).round()),
      foregroundColor: Colors.white,
      title: _pages != null
          ? Text('${_currentPage + 1} / ${_pages!.length}')
          : const Text('漫画阅读器'),
      actions: [if (_pages != null) _buildInfoButton()],
    ),
    body: _buildBody(),
  );

  Widget _buildInfoButton() => IconButton(
    icon: const Icon(Icons.info_outline),
    onPressed: () {
      showDialog<void>(
        context: context,
        builder: (context) => AlertDialog(
          title: const Text('漫画信息'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('总页数: ${_pages!.length}'),
              Text('当前页: ${_currentPage + 1}'),
              Text('文件: ${widget.comicArchive.path?.split('/').last ?? 'N/A'}'),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(),
              child: const Text('关闭'),
            ),
          ],
        ),
      );
    },
  );

  Widget _buildBody() {
    if (_isLoading) {
      return const Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            CircularProgressIndicator(color: Colors.white),
            SizedBox(height: 16),
            Text('正在加载漫画...', style: TextStyle(color: Colors.white)),
          ],
        ),
      );
    }

    if (_error != null) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.error_outline, color: Colors.red, size: 64),
            const SizedBox(height: 16),
            Text(
              '加载失败: $_error',
              style: const TextStyle(color: Colors.white),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 16),
            ElevatedButton(onPressed: _retryLoad, child: const Text('重试')),
          ],
        ),
      );
    }

    if (_pages == null || _pages!.isEmpty) {
      return const Center(
        child: Text('没有找到漫画页面', style: TextStyle(color: Colors.white)),
      );
    }

    return LayoutBuilder(
      builder: (context, constraints) {
        if (constraints.maxWidth > 600) {
          // 双页视图
          return PhotoViewGallery.builder(
            scrollPhysics: const BouncingScrollPhysics(),
            builder: (BuildContext context, int index) {
              final firstPageIndex = index * 2;
              final secondPageIndex = firstPageIndex + 1;
              final hasSecondPage = secondPageIndex < _pages!.length;

              return PhotoViewGalleryPageOptions.customChild(
                child: Row(
                  children: [
                    Expanded(
                      child: PhotoView(
                        imageProvider: MemoryImage(_pages![firstPageIndex]),
                        initialScale: PhotoViewComputedScale.contained,
                        minScale: PhotoViewComputedScale.contained * 0.5,
                        maxScale: PhotoViewComputedScale.covered * 3.0,
                        heroAttributes: PhotoViewHeroAttributes(
                          tag: 'comic_page_$firstPageIndex',
                        ),
                      ),
                    ),
                    if (hasSecondPage)
                      Expanded(
                        child: PhotoView(
                          imageProvider: MemoryImage(_pages![secondPageIndex]),
                          initialScale: PhotoViewComputedScale.contained,
                          minScale: PhotoViewComputedScale.contained * 0.5,
                          maxScale: PhotoViewComputedScale.covered * 3.0,
                          heroAttributes: PhotoViewHeroAttributes(
                            tag: 'comic_page_$secondPageIndex',
                          ),
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
            itemCount: (_pages!.length / 2).ceil(),
            loadingBuilder: (context, event) => const Center(
              child: CircularProgressIndicator(color: Colors.white),
            ),
            pageController: _pageController,
            onPageChanged: (index) => _onPageChanged(index * 2),
            backgroundDecoration: const BoxDecoration(color: Colors.black),
          );
        } else {
          // 单页视图
          return PhotoViewGallery.builder(
            scrollPhysics: const BouncingScrollPhysics(),
            builder: (BuildContext context, int index) =>
                PhotoViewGalleryPageOptions(
                  imageProvider: MemoryImage(_pages![index]),
                  initialScale: PhotoViewComputedScale.contained,
                  minScale: PhotoViewComputedScale.contained * 0.5,
                  maxScale: PhotoViewComputedScale.covered * 3.0,
                  heroAttributes: PhotoViewHeroAttributes(
                    tag: 'comic_page_$index',
                  ),
                ),
            itemCount: _pages!.length,
            loadingBuilder: (context, event) => const Center(
              child: CircularProgressIndicator(color: Colors.white),
            ),
            pageController: _pageController,
            onPageChanged: _onPageChanged,
            backgroundDecoration: const BoxDecoration(color: Colors.black),
          );
        }
      },
    );
  }

  void _retryLoad() {
    setState(() {
      _isLoading = true;
      _error = null;
    });
    _loadPages();
  }
}
