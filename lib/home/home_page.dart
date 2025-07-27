import 'dart:convert';
import 'package:crypto/crypto.dart';
import 'package:drift/drift.dart' as drift;
import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:path/path.dart' as p;

import '../core/comic_archive.dart';
import '../data/drift_db.dart';
import '../reader/reader_page.dart';
import '../settings/settings_page.dart';
import 'sync_provider.dart';

final dbProvider = Provider((ref) => DriftDb());

final comicListProvider = FutureProvider<List<Comic>>((ref) async {
  final db = ref.watch(dbProvider);
  return db.select(db.comics).get();
});

final comicProgressProvider =
    FutureProvider.family<ComicProgressData?, String>((ref, fileHash) async {
  final db = ref.watch(dbProvider);
  return db.getProgress(fileHash);
});

class HomePage extends ConsumerWidget {
  const HomePage({super.key});

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
          await db.into(db.comics).insert(
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

  void _openComic(BuildContext context, Comic comic) {
    final comicArchive = ComicArchive(comic.filePath);
    Navigator.push<void>(
      context,
      MaterialPageRoute(
        builder: (context) => ReaderPage(comicArchive: comicArchive),
      ),
    );
  }

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
              }
            },
            itemBuilder: (BuildContext context) => <PopupMenuEntry<String>>[
              const PopupMenuItem<String>(value: 'settings', child: Text('设置')),
              const PopupMenuItem<String>(value: 'about', child: Text('关于')),
            ],
          ),
        ],
      ),
      body: comicListAsync.when(
        data: (comics) =>
            comics.isEmpty ? _buildEmptyState(ref) : _buildComicGrid(ref, comics),
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (err, stack) => Center(child: Text('Error: $err')),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: syncState.isLoading ? null : () => _performSync(ref, context),
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
          final fileHash =
              sha1.convert(utf8.encode(comic.filePath)).toString();

          return Card(
            elevation: 4,
            child: InkWell(
              onTap: () => _openComic(context, comic),
              borderRadius: BorderRadius.circular(12),
              child: Stack(
                children: [
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      Expanded(
                        child: Container(
                          decoration: BoxDecoration(
                            borderRadius: const BorderRadius.vertical(
                              top: Radius.circular(12),
                            ),
                            color: Colors.grey[200],
                          ),
                          child:
                              const Icon(Icons.book, size: 80, color: Colors.grey),
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
                  Consumer(
                    builder: (context, ref, child) {
                      final progress = ref.watch(comicProgressProvider(fileHash));
                      return progress.when(
                        data: (data) {
                          if (data == null || data.totalPages == 0) {
                            return const SizedBox.shrink();
                          }
                          return Positioned(
                            bottom: 0,
                            left: 0,
                            right: 0,
                            child: LinearProgressIndicator(
                              value: data.currentPage / data.totalPages,
                              backgroundColor: Colors.grey[300],
                              valueColor:
                                  const AlwaysStoppedAnimation<Color>(Colors.deepPurple),
                            ),
                          );
                        },
                        loading: () => const SizedBox.shrink(),
                        error: (err, stack) => const SizedBox.shrink(),
                      );
                    },
                  ),
                ],
              ),
            ),
          );
        },
      );

  Future<void> _performSync(WidgetRef ref, BuildContext context) async {
    await ref.read(syncProvider.notifier).sync();

    if (context.mounted) {
      ref.read(syncProvider).when(
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
}
