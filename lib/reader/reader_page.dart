import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:photo_view/photo_view.dart';
import 'package:photo_view/photo_view_gallery.dart';

import '../core/comic_archive.dart';

class ReaderPage extends StatefulWidget {
  const ReaderPage({
    required this.comicArchive,
    this.initialPage = 0,
    super.key,
  });

  final ComicArchive comicArchive;
  final int initialPage;

  @override
  State<ReaderPage> createState() => _ReaderPageState();
}

class _ReaderPageState extends State<ReaderPage> {
  late PageController _pageController;
  List<Uint8List>? _pages;
  bool _isLoading = true;
  String? _error;
  int _currentPage = 0;

  @override
  void initState() {
    super.initState();
    _currentPage = widget.initialPage;
    _pageController = PageController(initialPage: widget.initialPage);
    _loadPages();
  }

  @override
  void dispose() {
    _pageController.dispose();
    super.dispose();
  }

  Future<void> _loadPages() async {
    try {
      final pages = await widget.comicArchive.listPages();
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
                  Text('文件: ${widget.comicArchive.path.split('/').last}'),
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

    return PhotoViewGallery.builder(
      scrollPhysics: const BouncingScrollPhysics(),
      builder: (BuildContext context, int index) =>
          PhotoViewGalleryPageOptions(
        imageProvider: MemoryImage(_pages![index]),
        initialScale: PhotoViewComputedScale.contained,
        minScale: PhotoViewComputedScale.contained * 0.5,
        maxScale: PhotoViewComputedScale.covered * 3.0,
        heroAttributes: PhotoViewHeroAttributes(tag: 'comic_page_$index'),
      ),
      itemCount: _pages!.length,
      loadingBuilder: (context, event) =>
          const Center(child: CircularProgressIndicator(color: Colors.white)),
      pageController: _pageController,
      onPageChanged: _onPageChanged,
      backgroundDecoration: const BoxDecoration(color: Colors.black),
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
