import 'dart:typed_data';

import 'package:drift/drift.dart';

import '../core/comic_archive.dart';
import '../models/reader_models.dart' as models;
import 'drift_db.dart';

/// ComicRepository 抽象了与漫画内容和元数据相关的数据操作。
///
/// 它负责:
/// - 从 [ComicArchive] 加载漫画页面。
/// - 通过 [DriftDb] 数据库加载/保存阅读历史、会话和书签。
class ComicRepository {
  final DriftDb _db;

  ComicRepository(this._db);

  /// 从指定的 [ComicArchive] 加载所有页面。
  Future<List<Uint8List>> getPages(ComicArchive comicArchive) => comicArchive.listPages();

  /// 获取漫画详情
  Future<models.ComicInfo> getComicDetail(String comicId) async {
    final comic = await (_db.select(_db.comics)..where((c) => c.id.equals(int.parse(comicId)))).getSingleOrNull();
    if (comic == null) {
      throw Exception('Comic not found');
    }
    return models.ComicInfo(id: comic.id.toString(), name: comic.fileName);
  }

  /// 获取漫画页面
  Future<List<models.ComicPage>> getComicPages(String comicId) => Future.value(<models.ComicPage>[]);

  /// 更新阅读进度
  Future<void> updateReadingProgress({required String comicId, required int page}) async {
    final id = int.parse(comicId);
    await (_db.update(_db.comics)..where((tbl) => tbl.id.equals(id)))
        .write(ComicsCompanion(
          lastReadAt: Value(DateTime.now()),
          progress: Value(page.toDouble()),
        ));
  }

  /// 保存书签
  Future<void> saveBookmark({required String comicId, required int page, String? label}) async {
    await _db.into(_db.bookmarks).insert(BookmarksCompanion(
      comicId: Value(int.parse(comicId)),
      pageIndex: Value(page),
      label: Value(label),
      createdAt: Value(DateTime.now()),
    ));
  }

  /// 获取书签列表
  Future<List<models.Bookmark>> getBookmarks(String comicId) async {
    final bookmarks = await (_db.select(_db.bookmarks)
          ..where((b) => b.comicId.equals(int.parse(comicId))))
        .get();
    return bookmarks.map((b) => models.Bookmark(id: b.id, page: b.pageIndex)).toList();
  }

  /// 删除书签
  Future<void> deleteBookmark(int bookmarkId) async {
    await (_db.delete(_db.bookmarks)..where((b) => b.id.equals(bookmarkId))).go();
  }
}
