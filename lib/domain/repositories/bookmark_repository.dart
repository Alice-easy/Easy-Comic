import 'package:easy_comic/core/error/failures.dart';
import 'package:dartz/dartz.dart';
import '../entities/bookmark.dart';

abstract class BookmarkRepository {
  /// 获取指定漫画的所有书签
  Future<Either<Failure, List<Bookmark>>> getBookmarksForComic(String comicId);

  /// 添加书签
  Future<Either<Failure, void>> addBookmark(Bookmark bookmark);

  /// 删除书签
  Future<Either<Failure, void>> removeBookmark(String comicId, int pageIndex);

  /// 获取指定漫画的所有书签
  Future<List<Bookmark>> getBookmarks(String comicId);

  /// 删除书签
  Future<void> deleteBookmark(String bookmarkId);

  /// 更新书签
  Future<void> updateBookmark(Bookmark bookmark);

  /// 根据ID获取书签
  Future<Bookmark?> getBookmarkById(String bookmarkId);

  /// 监听书签变化
  Stream<List<Bookmark>> watchBookmarks(String comicId);

  /// 获取所有书签
  Future<List<Bookmark>> getAllBookmarks();

  /// 清理无效书签（对应的漫画已删除）
  Future<void> cleanupInvalidBookmarks();

  /// 导出书签
  Future<List<Map<String, dynamic>>> exportBookmarks(String comicId);

  /// 导入书签
  Future<void> importBookmarks(String comicId, List<Map<String, dynamic>> bookmarks);
}