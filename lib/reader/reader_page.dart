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
import '../models/reader_models.dart' as models;
import '../core/brightness_service.dart';
import 'widgets/reader_core.dart';
import 'mixins/navigation_handler.dart';
import 'mixins/settings_handler.dart';

/// Riverpod Provider for managing bookmarks by comic ID
final bookmarksProvider =
    FutureProvider.family<List<models.Bookmark>, int>((ref, comicId) async {
  final db = ref.watch(dbProvider);
  final bookmarksData = await (db.select(db.bookmarks)..where((tbl) => tbl.comicId.equals(comicId)))
      .get();
  
  return bookmarksData.map((data) => models.Bookmark.fromData(data)).toList();
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

class _ReaderPageState extends ConsumerState<ReaderPage> with NavigationHandler, SettingsHandler {
  late PageController _pageController;
  List<Uint8List>? _pages;
  List<Uint8List>? _orderedPages; // Pages with custom order applied
  bool _isLoading = true;
  String? _error;
  int _currentPage = 0;
  late DateTime _startTime;
  bool _showUi = true;
  String? _sessionId;
  int? _comicId;

  // NavigationHandler implementation
  @override
  int get currentPage => _currentPage;
  
  @override
  int get totalPages => _orderedPages?.length ?? 0;
  
  @override
  PageController get pageController => _pageController;
  
  @override
  void updateCurrentPage(int page) {
    setState(() {
      _currentPage = page;
    });
  }
  
  @override
  void updateProgress() {
    _updateProgress();
  }

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
    _sessionId = DateTime.now().millisecondsSinceEpoch.toString();
    _pageController = PageController(initialPage: widget.initialPage);
    
    // Initialize after first frame
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _initializeServices();
      _loadPages();
    });

    // 每2秒批量更新一次进度，避免频繁的数据库写入
    _progressUpdateTimer = Timer.periodic(const Duration(seconds: 2), (_) {
      if (_hasPendingProgressUpdate) {
        _updateProgress(force: true);
        _hasPendingProgressUpdate = false;
      }
    });
  }

  /// Initialize services
  Future<void> _initializeServices() async {
    final db = ref.read(dbProvider);
    
    // Find comic ID
    final comic = await (db.select(db.comics)
          ..where((tbl) => tbl.filePath.equals(widget.comicArchive.path!)))
        .getSingleOrNull();
    _comicId = comic?.id;
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
  /// 从 ComicArchive 中提取所有页面图片，应用自定义排序，并设置 Firebase Crashlytics 自定义键
  Future<void> _loadPages() async {
    try {
      final pages = await widget.comicArchive.listPages();
      
      // Apply custom page order if available
      List<Uint8List> orderedPages = pages;
      if (_comicId != null) {
        final db = ref.read(dbProvider);
        final customOrder = await db.getCustomPageOrder(_comicId!);
        if (customOrder.isNotEmpty) {
          // Apply custom ordering based on database records
          final Map<int, int> orderMap = {};
          for (final order in customOrder) {
            orderMap[order.originalIndex] = order.customIndex;
          }
          
          final List<Uint8List> reorderedPages = List.filled(pages.length, pages[0]);
          for (int i = 0; i < pages.length; i++) {
            final newIndex = orderMap[i] ?? i;
            if (newIndex < reorderedPages.length) {
              reorderedPages[newIndex] = pages[i];
            }
          }
          orderedPages = reorderedPages;
        }
      }
      
      // 设置 fileHash 自定义键
      final fileName = widget.comicArchive.path?.split('/').last;
      if (fileName != null) {
        await FirebaseCrashlytics.instance.setCustomKey('fileName', fileName);
      }
      
      if (mounted) {
        setState(() {
          _pages = pages;
          _orderedPages = orderedPages;
          _isLoading = false;
        });
        
        // Add to reading history
        if (_comicId != null && _sessionId != null) {
          final db = ref.read(dbProvider);
          await db.addToReadingHistory(_comicId!, _currentPage, _sessionId!);
        }
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


  @override
  Widget build(BuildContext context) {
    // Watch reader settings and UI state
    final readerSettings = ref.watch(readerSettingsProvider);
    final readerUIState = ref.watch(readerUIStateProvider);
    final brightness = ref.watch(brightnessProvider);
    
    return readerSettings.when(
      data: (settings) => _buildReader(context, settings, readerUIState, brightness),
      loading: () => const Scaffold(
        body: Center(child: CircularProgressIndicator()),
      ),
      error: (error, stack) => Scaffold(
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.error_outline, color: Colors.red, size: 64),
              const SizedBox(height: 16),
              Text('加载设置失败: $error'),
              const SizedBox(height: 16),
              ElevatedButton(
                onPressed: () => ref.invalidate(readerSettingsProvider),
                child: const Text('重试'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildReader(BuildContext context, models.ReaderSettings settings, models.ReaderUIState uiState, double brightness) {
    final backgroundColor = uiState.brightnessOverlayEnabled && uiState.customBrightness != null
        ? settings.backgroundTheme.color
        : settings.backgroundTheme.color;

    return Scaffold(
      backgroundColor: backgroundColor,
      appBar: PreferredSize(
        preferredSize: const Size.fromHeight(kToolbarHeight),
        child: AnimatedOpacity(
          opacity: uiState.showControls ? 1.0 : 0.0,
          duration: const Duration(milliseconds: 200),
          child: AppBar(
            backgroundColor: backgroundColor.withAlpha((255 * 0.7).round()),
            foregroundColor: _getForegroundColor(settings.backgroundTheme),
            title: _orderedPages != null
                ? Text('${_currentPage + 1} / ${_orderedPages!.length}')
                : const Text('漫画阅读器'),
            actions: [
              if (_orderedPages != null) buildModeButton(settings),
              if (_orderedPages != null) buildDirectionButton(settings),
              if (_orderedPages != null) buildBrightnessButton(settings, uiState),
              if (_orderedPages != null) buildBackgroundButton(settings),
              if (_orderedPages != null)
                IconButton(
                  icon: const Icon(Icons.bookmark_add_outlined),
                  tooltip: '添加书签',
                  onPressed: _showAddBookmarkDialog,
                ),
              if (_orderedPages != null)
                IconButton(
                  icon: const Icon(Icons.bookmarks_outlined),
                  tooltip: '书签列表',
                  onPressed: _showBookmarks,
                ),
              if (_orderedPages != null) _buildInfoButton(),
            ],
          ),
        ),
      ),
      body: Stack(
        children: [
          GestureDetector(
            onTap: () {
              ref.read(readerUIStateProvider.notifier).toggleControls();
            },
            child: _buildBody(settings, uiState),
          ),
          if (uiState.brightnessOverlayEnabled && uiState.customBrightness != null)
            _buildBrightnessOverlay(uiState.customBrightness!),
        ],
      ),
      bottomNavigationBar: AnimatedOpacity(
        opacity: uiState.showProgress && uiState.showControls ? 1.0 : 0.0,
        duration: const Duration(milliseconds: 200),
        child: _orderedPages == null || _orderedPages!.isEmpty
            ? null
            : BottomAppBar(
                color: backgroundColor.withAlpha((255 * 0.7).round()),
                child: Slider(
                  value: _currentPage.toDouble(),
                  min: 0,
                  max: (_orderedPages!.length - 1).toDouble(),
                  activeColor: _getForegroundColor(settings.backgroundTheme),
                  inactiveColor: _getForegroundColor(settings.backgroundTheme).withOpacity(0.3),
                  onChanged: (value) {
                    setState(() {
                      _currentPage = value.toInt();
                    });
                  },
                  onChangeEnd: (value) {
                    _pageController.jumpToPage(value.toInt());
                  },
                ),
              ),
      ),
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
        return Colors.black;
    }
  }

  /// Build reading mode button
  Widget _buildModeButton(models.ReaderSettings settings) {
    return PopupMenuButton<models.ReadingMode>(
      icon: Icon(_getModeIcon(settings.readingMode)),
      tooltip: '阅读模式',
      onSelected: (mode) => ref.read(readerSettingsProvider.notifier).updateReadingMode(mode),
      itemBuilder: (context) => [
        PopupMenuItem(
          value: models.ReadingMode.single,
          child: Row(
            children: [
              Icon(_getModeIcon(models.ReadingMode.single)),
              const SizedBox(width: 8),
              const Text('单页模式'),
            ],
          ),
        ),
        PopupMenuItem(
          value: models.ReadingMode.dual,
          child: Row(
            children: [
              Icon(_getModeIcon(models.ReadingMode.dual)),
              const SizedBox(width: 8),
              const Text('双页模式'),
            ],
          ),
        ),
        PopupMenuItem(
          value: models.ReadingMode.continuous,
          child: Row(
            children: [
              Icon(_getModeIcon(models.ReadingMode.continuous)),
              const SizedBox(width: 8),
              const Text('连续滚动'),
            ],
          ),
        ),
      ],
    );
  }

  /// Build navigation direction button
  Widget _buildDirectionButton(models.ReaderSettings settings) {
    return PopupMenuButton<models.NavigationDirection>(
      icon: Icon(getDirectionIcon(settings.navigationDirection)),
      tooltip: '阅读方向',
      onSelected: (direction) => ref.read(readerSettingsProvider.notifier).updateNavigationDirection(direction),
      itemBuilder: (context) => [
        PopupMenuItem(
          value: models.NavigationDirection.horizontal,
          child: Row(
            children: [
              Icon(getDirectionIcon(models.NavigationDirection.horizontal)),
              const SizedBox(width: 8),
              const Text('从左到右'),
            ],
          ),
        ),
        PopupMenuItem(
          value: models.NavigationDirection.rtl,
          child: Row(
            children: [
              Icon(getDirectionIcon(models.NavigationDirection.rtl)),
              const SizedBox(width: 8),
              const Text('从右到左'),
            ],
          ),
        ),
        PopupMenuItem(
          value: models.NavigationDirection.vertical,
          child: Row(
            children: [
              Icon(getDirectionIcon(models.NavigationDirection.vertical)),
              const SizedBox(width: 8),
              const Text('从上到下'),
            ],
          ),
        ),
      ],
    );
  }

  /// Build brightness button with slider
  Widget _buildBrightnessButton(models.ReaderSettings settings, models.ReaderUIState uiState) {
    return PopupMenuButton<void>(
      icon: const Icon(Icons.brightness_6),
      tooltip: '亮度调节',
      itemBuilder: (context) => [
        PopupMenuItem(
          enabled: false,
          child: SizedBox(
            width: 200,
            child: Column(
              children: [
                Text('亮度: ${(settings.brightness * 100).round()}%'),
                Slider(
                  value: settings.brightness,
                  onChanged: (value) => ref.read(readerSettingsProvider.notifier).updateBrightness(value),
                ),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    TextButton(
                      onPressed: () => ref.read(brightnessProvider.notifier).autoAdjustBrightness(),
                      child: const Text('自动'),
                    ),
                    TextButton(
                      onPressed: () => ref.read(readerUIStateProvider.notifier).toggleBrightnessOverlay(),
                      child: Text(uiState.brightnessOverlayEnabled ? '关闭覆盖' : '启用覆盖'),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }

  /// Build background theme button
  Widget _buildBackgroundButton(models.ReaderSettings settings) {
    return PopupMenuButton<models.BackgroundTheme>(
      icon: Icon(Icons.palette, color: settings.backgroundTheme.color == Colors.black ? Colors.white : Colors.black),
      tooltip: '背景主题',
      onSelected: (theme) => ref.read(readerSettingsProvider.notifier).updateBackgroundTheme(theme),
      itemBuilder: (context) => models.BackgroundTheme.values.map((theme) => PopupMenuItem(
        value: theme,
        child: Row(
          children: [
            Container(
              width: 20,
              height: 20,
              decoration: BoxDecoration(
                color: theme.color,
                border: Border.all(color: Colors.grey),
                borderRadius: BorderRadius.circular(4),
              ),
            ),
            const SizedBox(width: 8),
            Text(theme.displayName),
            if (settings.backgroundTheme == theme)
              const Padding(
                padding: EdgeInsets.only(left: 8),
                child: Icon(Icons.check, size: 16),
              ),
          ],
        ),
      )).toList(),
    );
  }

  /// Build brightness overlay
  Widget _buildBrightnessOverlay(double brightness) {
    return Positioned.fill(
      child: IgnorePointer(
        child: Container(
          decoration: BoxDecoration(
            color: Colors.black.withOpacity(1.0 - brightness),
          ),
        ),
      ),
    );
  }

  /// Get icon for reading mode
  IconData _getModeIcon(models.ReadingMode mode) {
    switch (mode) {
      case models.ReadingMode.single:
        return Icons.crop_portrait;
      case models.ReadingMode.dual:
        return Icons.crop_landscape;
      case models.ReadingMode.continuous:
        return Icons.view_day;
      case models.ReadingMode.vertical:
        return Icons.view_stream;
    }
  }

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
              title: Text('第 ${bookmark.page + 1} 页'),
              subtitle: bookmark.label != null && bookmark.label!.isNotEmpty ? Text(bookmark.label!) : null,
              onTap: () {
                _pageController.jumpToPage(bookmark.page);
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

  Widget _buildBody(models.ReaderSettings settings, models.ReaderUIState uiState) {
    if (_isLoading) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            CircularProgressIndicator(color: _getForegroundColor(settings.backgroundTheme)),
            const SizedBox(height: 16),
            Text(
              '正在加载漫画...',
              style: TextStyle(color: getForegroundColor(settings.backgroundTheme)),
            ),
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
              style: TextStyle(color: getForegroundColor(settings.backgroundTheme)),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 16),
            ElevatedButton(onPressed: _retryLoad, child: const Text('重试')),
          ],
        ),
      );
    }

    if (_orderedPages == null || _orderedPages!.isEmpty) {
      return Center(
        child: Text(
          '没有找到漫画页面',
          style: TextStyle(color: getForegroundColor(settings.backgroundTheme)),
        ),
      );
    }

    // Use reading mode and navigation direction from settings
    return ReaderCore(
      pages: _orderedPages!,
      settings: settings,
      pageController: _pageController,
      currentPage: _currentPage,
      onPageChanged: onPageChanged, // Use mixin method
      onTap: () => ref.read(readerUIStateProvider.notifier).toggleControls(),
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
