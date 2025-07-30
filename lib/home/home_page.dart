import 'dart:typed_data';

import 'package:drift/drift.dart' as drift;
import 'package:file_picker/file_picker.dart';
import 'package:firebase_analytics/firebase_analytics.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:palette_generator/palette_generator.dart';
import 'package:path/path.dart' as p;

import '../core/comic_archive.dart';
import '../data/drift_db.dart';
import '../main.dart';
import '../reader/reader_page.dart';
import '../settings/settings_page.dart';
import 'sync_provider.dart';

/// 漫画排序方式枚举
enum SortOrder { byName, byDate, byProgress }

/// 封面图片缓存Provider
/// 
/// 使用 FutureProvider.family 缓存每个漫画文件的封面图片，
/// 避免重复读取和提高UI性能
final coverImageProvider = FutureProvider.family<Uint8List?, String>((ref, filePath) async {
  try {
    final comicArchive = ComicArchive(path: filePath);
    return await comicArchive.getCoverImage();
  } catch (e) {
    return null;
  }
});

final sortOrderProvider = StateProvider<SortOrder>((ref) => SortOrder.byName);
final favoritesFilterProvider = StateProvider<bool>((ref) => false);

final comicListProvider = FutureProvider<List<Comic>>((ref) async {
  final db = ref.watch(dbProvider);
  final sortOrder = ref.watch(sortOrderProvider);
  final showFavoritesOnly = ref.watch(favoritesFilterProvider);

  final query = db.select(db.comics);

  if (showFavoritesOnly) {
    query.where((tbl) => tbl.isFavorite.equals(true));
  }

  switch (sortOrder) {
    case SortOrder.byName:
      query.orderBy([(tbl) => drift.OrderingTerm(expression: tbl.fileName)]);
      break;
    case SortOrder.byDate:
      query.orderBy([(tbl) => drift.OrderingTerm(expression: tbl.lastReadAt, mode: drift.OrderingMode.desc)]);
      break;
    case SortOrder.byProgress:
      query.orderBy([(tbl) => drift.OrderingTerm(expression: tbl.progress, mode: drift.OrderingMode.desc)]);
      break;
  }

  return query.get();
});

/// 主页组件
/// 
/// 显示漫画库，包含以下功能：
/// - 漫画文件管理（添加、删除、收藏）
/// - 多种排序方式（按名称、日期、进度）
/// - 同步功能
/// - 设置和关于页面导航
class HomePage extends ConsumerWidget {
  const HomePage({super.key});

  /// 选择并添加漫画文件
  /// 
  /// 使用 FilePicker 选择 CBZ/ZIP 文件，支持多选，
  /// 并将选中的文件添加到数据库中
  Future<void> _pickComicFile(WidgetRef ref) async {
    final result = await FilePicker.platform.pickFiles(
      type: FileType.custom,
      allowedExtensions: ['cbz', 'zip'],
      allowMultiple: true,
    );

    if (result != null) {
      final db = ref.read(dbProvider);
      for (final file in result.files) {
        if (file.path != null) {
          await db
              .into(db.comics)
              .insert(
                ComicsCompanion(
                  filePath: drift.Value(file.path!),
                  fileName: drift.Value(p.basename(file.path!)),
                  addedAt: drift.Value(DateTime.now()),
                ),
              );
        }
      }
      ref.invalidate(comicListProvider);
    }
  }

  /// 打开漫画阅读器
  /// 
  /// 提取封面图片生成主题色彩，然后导航到阅读器页面
  /// 同时记录 Firebase Analytics 事件
  Future<void> _openComic(
    BuildContext context,
    WidgetRef ref,
    Comic comic,
  ) async {
    await FirebaseAnalytics.instance.logEvent(name: 'read_start');
    final comicArchive = ComicArchive(path: comic.filePath);
    final coverImage = await comicArchive.getCoverImage();
    if (coverImage != null) {
      final paletteGenerator = await PaletteGenerator.fromImageProvider(
        MemoryImage(coverImage),
      );
      final dominantColor = paletteGenerator.dominantColor?.color;
      if (dominantColor != null) {
        ref.read(seedColorProvider.notifier).state = dominantColor;
      }
    }

    if (context.mounted) {
      // ignore: unawaited_futures
      Navigator.push<void>(
        context,
        MaterialPageRoute(
          builder: (context) => ReaderPage(comicArchive: comicArchive),
        ),
      );
    }
  }

  /// 从库中移除漫画
  /// 
  /// 从数据库中删除漫画记录并刷新列表
  void _removeComic(WidgetRef ref, Comic comic) {
    final db = ref.read(dbProvider);
    (db.delete(db.comics)..where((tbl) => tbl.id.equals(comic.id))).go();
    ref.invalidate(comicListProvider);
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final syncState = ref.watch(syncProvider);
    final comicListAsync = ref.watch(comicListProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Easy Comic'),
        elevation: 4,
        actions: [
          IconButton(
            icon: const Icon(Icons.add),
            onPressed: () => _pickComicFile(ref),
            tooltip: '添加漫画',
          ),
          PopupMenuButton<SortOrder>(
            icon: const Icon(Icons.sort),
            tooltip: '排序方式',
            onSelected: (SortOrder result) {
              ref.read(sortOrderProvider.notifier).state = result;
            },
            itemBuilder: (BuildContext context) => <PopupMenuEntry<SortOrder>>[
              const PopupMenuItem<SortOrder>(
                value: SortOrder.byName,
                child: Text('按名称'),
              ),
              const PopupMenuItem<SortOrder>(
                value: SortOrder.byDate,
                child: Text('按最近阅读'),
              ),
              const PopupMenuItem<SortOrder>(
                value: SortOrder.byProgress,
                child: Text('按进度'),
              ),
            ],
          ),
          PopupMenuButton<String>(
            onSelected: (value) {
              if (value == 'settings') {
                Navigator.push<void>(
                  context,
                  MaterialPageRoute(builder: (context) => const SettingsPage()),
                );
              } else if (value == 'about') {
                showAboutDialog(
                  context: context,
                  applicationName: 'Easy Comic',
                  applicationVersion: '1.0.0',
                  applicationLegalese: '© 2024 The Easy Comic Authors',
                );
              } else if (value == 'toggle_favorites') {
                final current = ref.read(favoritesFilterProvider);
                ref.read(favoritesFilterProvider.notifier).state = !current;
              }
            },
            itemBuilder: (BuildContext context) => <PopupMenuEntry<String>>[
              CheckedPopupMenuItem<String>(
                value: 'toggle_favorites',
                checked: ref.watch(favoritesFilterProvider),
                child: const Text('仅显示收藏'),
              ),
              const PopupMenuDivider(),
              const PopupMenuItem<String>(value: 'settings', child: Text('设置')),
              const PopupMenuItem<String>(value: 'about', child: Text('关于')),
            ],
          ),
        ],
      ),
      body: comicListAsync.when(
        data: (comics) => comics.isEmpty
            ? _buildEmptyState(ref)
            : _buildComicGrid(ref, comics),
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (err, stack) => Center(child: Text('Error: $err')),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: syncState.isLoading
            ? null
            : () => _performSync(ref, context),
        tooltip: '同步数据',
        child: syncState.isLoading
            ? const SizedBox(
                width: 20,
                height: 20,
                child: CircularProgressIndicator(
                  strokeWidth: 2,
                  valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                ),
              )
            : const Icon(Icons.sync),
      ),
    );
  }

  Widget _buildEmptyState(WidgetRef ref) => Center(
    child: Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Icon(Icons.library_books, size: 100, color: Colors.grey[400]),
        const SizedBox(height: 16),
        Text('暂无漫画', style: TextStyle(fontSize: 18, color: Colors.grey[600])),
        const SizedBox(height: 8),
        Text(
          '点击右上角的 + 按钮添加漫画文件',
          style: TextStyle(fontSize: 14, color: Colors.grey[500]),
        ),
        const SizedBox(height: 24),
        FilledButton.icon(
          onPressed: () => _pickComicFile(ref),
          icon: const Icon(Icons.add),
          label: const Text('添加漫画'),
        ),
      ],
    ),
  );

  Widget _buildComicGrid(WidgetRef ref, List<Comic> comics) => GridView.builder(
    padding: const EdgeInsets.all(16),
    gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
      crossAxisCount: 2,
      childAspectRatio: 0.7,
      crossAxisSpacing: 16,
      mainAxisSpacing: 16,
    ),
    itemCount: comics.length,
    itemBuilder: (context, index) {
      final comic = comics[index];

      return Card(
        elevation: 4,
        child: InkWell(
          onTap: () => _openComic(context, ref, comic),
          borderRadius: BorderRadius.circular(12),
          child: Stack(
            children: [
              Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  Expanded(
                    child: Consumer(
                      builder: (context, ref, child) {
                        final coverAsync = ref.watch(coverImageProvider(comic.filePath));
                        return coverAsync.when(
                          data: (coverData) {
                            if (coverData != null) {
                              return Image.memory(
                                coverData,
                                fit: BoxFit.cover,
                              );
                            }
                            return Container(
                              decoration: BoxDecoration(
                                borderRadius: const BorderRadius.vertical(
                                  top: Radius.circular(12),
                                ),
                                color: Colors.grey[200],
                              ),
                              child: const Icon(
                                Icons.book,
                                size: 80,
                                color: Colors.grey,
                              ),
                            );
                          },
                          loading: () => Container(
                            decoration: BoxDecoration(
                              borderRadius: const BorderRadius.vertical(
                                top: Radius.circular(12),
                              ),
                              color: Colors.grey[200],
                            ),
                            child: const Center(
                              child: CircularProgressIndicator(),
                            ),
                          ),
                          error: (_, __) => Container(
                            decoration: BoxDecoration(
                              borderRadius: const BorderRadius.vertical(
                                top: Radius.circular(12),
                              ),
                              color: Colors.grey[200],
                            ),
                            child: const Icon(
                              Icons.book,
                              size: 80,
                              color: Colors.grey,
                            ),
                          ),
                        );
                      },
                    ),
                  ),
                  Padding(
                    padding: const EdgeInsets.all(8),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          comic.fileName,
                          style: const TextStyle(
                            fontWeight: FontWeight.bold,
                            fontSize: 14,
                          ),
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                        ),
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            const Expanded(
                              child: Text(
                                'CBZ/ZIP',
                                style: TextStyle(
                                  fontSize: 12,
                                  color: Colors.grey,
                                ),
                              ),
                            ),
                            IconButton(
                              icon: Icon(
                                comic.isFavorite ? Icons.star : Icons.star_border,
                                color: comic.isFavorite ? Colors.amber : Colors.grey,
                                size: 20,
                              ),
                              onPressed: () async {
                                final db = ref.read(dbProvider);
                                await (db.update(db.comics)..where((tbl) => tbl.id.equals(comic.id))).write(
                                  ComicsCompanion(isFavorite: drift.Value(!comic.isFavorite)),
                                );
                                ref.invalidate(comicListProvider);
                              },
                              padding: EdgeInsets.zero,
                              constraints: const BoxConstraints(),
                            ),
                            IconButton(
                              icon: const Icon(Icons.delete, size: 16),
                              onPressed: () => _removeComic(ref, comic),
                              padding: EdgeInsets.zero,
                              constraints: const BoxConstraints(),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                ],
              ),
              if (comic.progress > 0 && comic.progress < 1)
                Positioned(
                  bottom: 0,
                  left: 0,
                  right: 0,
                  child: LinearProgressIndicator(
                    value: comic.progress,
                    backgroundColor: Colors.grey[300],
                    valueColor: const AlwaysStoppedAnimation<Color>(
                      Colors.deepPurple,
                    ),
                  ),
                ),
            ],
          ),
        ),
      );
    },
  );

  Future<void> _performSync(WidgetRef ref, BuildContext context) async {
    await ref.read(syncProvider.notifier).sync();

    if (!context.mounted) return;

    ref
        .read(syncProvider)
        .when(
          data: (result) {
            ScaffoldMessenger.of(context)
              ..hideCurrentSnackBar()
              ..showSnackBar(
                SnackBar(
                  content: Text(
                    '同步完成: 上传${result.uploaded}个，下载${result.downloaded}个',
                  ),
                  backgroundColor: Colors.green,
                ),
              );
          },
          error: (error, stackTrace) {
            ScaffoldMessenger.of(context)
              ..hideCurrentSnackBar()
              ..showSnackBar(
                SnackBar(
                  content: Text('同步失败: $error'),
                  backgroundColor: Colors.red,
                  action: SnackBarAction(
                    label: 'Retry',
                    onPressed: () => ref.read(syncProvider.notifier).sync(),
                  ),
                ),
              );
          },
          loading: () {},
        );
  }
}
