import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';
import 'package:crypto/crypto.dart';
import 'package:drift/drift.dart' as drift;
import 'package:firebase_analytics/firebase_analytics.dart';
import 'package:firebase_crashlytics/firebase_crashlytics.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:photo_view/photo_view.dart';
import 'package:photo_view/photo_view_gallery.dart';

import '../core/comic_archive.dart';
import '../data/drift_db.dart';
import '../home/home_page.dart';
import '../main.dart';

/// Riverpod Provider for managing bookmarks by comic ID
final bookmarksProvider =
    FutureProvider.family<List<Bookmark>, int>((ref, comicId) async {
  final db = ref.watch(dbProvider);
  return (db.select(db.bookmarks)..where((tbl) => tbl.comicId.equals(comicId)))
      .get();
});

/// 漫画阅读器页面
/// 
/// 提供以下功能：
/// - 显示漫画页面（支持单页和双页模式）
/// - 翻页和缩放操作
/// - 书签管理（添加、删除、跳转）
/// - 阅读进度追踪和保存
/// - 阅读时长统计
/// - Firebase Analytics 事件记录
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
  
  // 缓存文件哈希以避免重复计算
  String? _cachedFileHash;
  
  // 用于批量更新进度的定时器
  Timer? _progressUpdateTimer;
  bool _hasPendingProgressUpdate = false;

  @override
  void initState() {
    super.initState();
    _startTime = DateTime.now();
    _currentPage = widget.initialPage;
    _pageController = PageController(initialPage: widget.initialPage);
    _loadPages();
    
    // 每2秒批量更新一次进度，避免频繁的数据库写入
    _progressUpdateTimer = Timer.periodic(const Duration(seconds: 2), (_) {
      if (_hasPendingProgressUpdate) {
        _updateProgress(force: true);
        _hasPendingProgressUpdate = false;
      }
    });
  }

  @override
  void dispose() {
    _pageController.dispose();
    _progressUpdateTimer?.cancel();
    _saveReadingSession();
    _updateProgress(isDisposing: true, force: true);
    super.dispose();
  }
  
  /// 获取缓存的文件哈希
  String get _fileHash {
    _cachedFileHash ??= sha1
        .convert(utf8.encode(widget.comicArchive.path!))
        .toString();
    return _cachedFileHash!;
  }

  /// 保存阅读会话到数据库
  /// 
  /// 记录本次阅读的开始时间、结束时间和总时长
  Future<void> _saveReadingSession() async {
    final db = ref.read(dbProvider);
    final endTime = DateTime.now();
    final duration = endTime.difference(_startTime);
    await db.addReadingSession(
      ReadingSession(
        id: 0,
        fileHash: _fileHash,
        startTime: _startTime,
        endTime: endTime,
        durationInSeconds: duration.inSeconds,
      ),
    );
  }

  /// 加载漫画页面数据
  /// 
  /// 从 ComicArchive 中提取所有页面图片，并设置 Firebase Crashlytics 自定义键
  Future<void> _loadPages() async {
    try {
      final pages = await widget.comicArchive.listPages();
      // 设置 fileHash 自定义键
      final fileName = widget.comicArchive.path?.split('/').last;
      if (fileName != null) {
        await FirebaseCrashlytics.instance.setCustomKey('fileName', fileName);
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

  /// 更新阅读进度
  /// 
  /// 支持批量更新模式以减少数据库写入频率。
  /// - [isDisposing]: 是否正在销毁组件，如果是则强制更新
  /// - [force]: 是否强制立即更新，否则只标记待更新
  Future<void> _updateProgress({bool isDisposing = false, bool force = false}) async {
    if (_pages == null || _pages!.isEmpty) return;
    if (!force && !isDisposing) {
      // 标记有待处理的进度更新，而不是立即执行
      _hasPendingProgressUpdate = true;
      return;
    }

    final db = ref.read(dbProvider);
    final progress = (_currentPage + 1) / _pages!.length;

    // Find the comic id from file path
    final comic = await (db.select(db.comics)
          ..where((tbl) => tbl.filePath.equals(widget.comicArchive.path!)))
        .getSingleOrNull();

    if (comic != null) {
      await (db.update(db.comics)..where((tbl) => tbl.id.equals(comic.id)))
          .write(
        ComicsCompanion(
          progress: drift.Value(progress),
          lastReadAt: drift.Value(DateTime.now()),
        ),
      );
    }
    if (!isDisposing) {
      ref.invalidate(comicListProvider);
    }
  }

  /// 处理页面切换事件
  /// 
  /// 更新当前页面状态，触发进度更新，并记录 Firebase Analytics 事件
  void _onPageChanged(int index) {
    setState(() {
      _currentPage = index;
    });
    _updateProgress();
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
      actions: [
        if (_pages != null)
          IconButton(
            icon: const Icon(Icons.bookmark_add_outlined),
            tooltip: '添加书签',
            onPressed: _showAddBookmarkDialog,
          ),
        if (_pages != null)
          IconButton(
            icon: const Icon(Icons.bookmarks_outlined),
            tooltip: '书签列表',
            onPressed: _showBookmarks,
          ),
        if (_pages != null) _buildInfoButton()
      ],
    ),
    body: _buildBody(),
  );

  /// 显示添加书签对话框
  /// 
  /// 允许用户为当前页面添加书签，可选择提供标签描述
  Future<void> _showAddBookmarkDialog() async {
    final db = ref.read(dbProvider);
    final comic = await (db.select(db.comics)
          ..where((tbl) => tbl.filePath.equals(widget.comicArchive.path!)))
        .getSingleOrNull();
    if (comic == null) return;

    if (!mounted) return;

    final textController = TextEditingController();
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('添加书签'),
        content: TextField(
          controller: textController,
          decoration: const InputDecoration(hintText: '书签描述 (可选)'),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('取消'),
          ),
          TextButton(
            onPressed: () => Navigator.of(context).pop(true),
            child: const Text('添加'),
          ),
        ],
      ),
    );

    if (confirmed == true && mounted) {
      await db.into(db.bookmarks).insert(
            BookmarksCompanion(
              comicId: drift.Value(comic.id),
              pageIndex: drift.Value(_currentPage),
              label: drift.Value(textController.text),
              createdAt: drift.Value(DateTime.now()),
            ),
          );
      ref.invalidate(bookmarksProvider(comic.id));
    }
    textController.dispose();
  }

  /// 显示书签列表底部弹出菜单
  /// 
  /// 用户可以查看所有书签，点击跳转到对应页面，或删除书签
  Future<void> _showBookmarks() async {
    final db = ref.read(dbProvider);
    final comic = await (db.select(db.comics)
          ..where((tbl) => tbl.filePath.equals(widget.comicArchive.path!)))
        .getSingleOrNull();
    if (comic == null) return;

    final bookmarks = ref.watch(bookmarksProvider(comic.id));

    if (!mounted) return;

    await showModalBottomSheet<void>(
      context: context,
      builder: (context) => bookmarks.when(
        data: (data) => ListView.builder(
          itemCount: data.length,
          itemBuilder: (context, index) {
            final bookmark = data[index];
            return ListTile(
              title: Text('第 ${bookmark.pageIndex + 1} 页'),
              subtitle: bookmark.label != null ? Text(bookmark.label!) : null,
              onTap: () {
                _pageController.jumpToPage(bookmark.pageIndex);
                if (mounted) {
                  Navigator.of(context).pop();
                }
              },
              trailing: IconButton(
                icon: const Icon(Icons.delete_outline),
                onPressed: () async {
                  await (db.delete(db.bookmarks)..where((tbl) => tbl.id.equals(bookmark.id))).go();
                  ref.invalidate(bookmarksProvider(comic.id));
                  if (mounted) {
                    Navigator.of(context).pop();
                  }
                },
              ),
            );
          },
        ),
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (err, stack) => Center(child: Text('Error: $err')),
      ),
    );
  }

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
