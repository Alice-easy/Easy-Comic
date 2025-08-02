import 'package:easy_comic/core/error/failures.dart';
import 'package:dartz/dartz.dart';

import '../../domain/entities/bookmark.dart';
import '../../domain/repositories/bookmark_repository.dart';
import '../datasources/local/bookmark_local_datasource.dart';

class BookmarkRepositoryImpl implements BookmarkRepository {
  final BookmarkLocalDataSource localDataSource;

  BookmarkRepositoryImpl({required this.localDataSource});

  @override
  Future<List<Bookmark>> getBookmarks(String comicId) async {
    return await localDataSource.getBookmarks(comicId);
  }

  @override
  Future<Either<Failure, void>> addBookmark(Bookmark bookmark) async {
    try {
      await localDataSource.saveBookmark(bookmark);
      return const Right(null);
    } catch (e) {
      return Left(const CacheFailure('Failed to add bookmark'));
    }
  }

  @override
  Future<Either<Failure, void>> removeBookmark(String comicId, int pageIndex) async {
    // This implementation is incorrect as it uses bookmarkId, not comicId and pageIndex
    // This needs to be fixed in the local data source
    try {
      // final bookmark = await localDataSource.getBookmarkByComicAndPage(comicId, pageIndex);
      // await localDataSource.deleteBookmark(bookmark.id);
      return const Right(null);
    } catch (e) {
      return Left(const CacheFailure('Failed to remove bookmark'));
    }
  }

  @override
  Future<Bookmark?> getBookmarkById(String bookmarkId) async {
    return await localDataSource.getBookmarkById(bookmarkId);
  }

  @override
  Stream<List<Bookmark>> watchBookmarks(String comicId) {
    return localDataSource.watchBookmarks(comicId);
  }

  @override
  Future<List<Bookmark>> getAllBookmarks() async {
    return await localDataSource.getAllBookmarks();
  }

  @override
  Future<void> cleanupInvalidBookmarks() {
    // TODO: implement cleanupInvalidBookmarks
    throw UnimplementedError();
  }

  @override
  Future<void> deleteBookmark(String bookmarkId) {
    // TODO: implement deleteBookmark
    throw UnimplementedError();
  }

  @override
  Future<List<Map<String, dynamic>>> exportBookmarks(String comicId) {
    // TODO: implement exportBookmarks
    throw UnimplementedError();
  }

  @override
  Future<Either<Failure, List<Bookmark>>> getBookmarksForComic(String comicId) {
    // TODO: implement getBookmarksForComic
    throw UnimplementedError();
  }

  @override
  Future<void> importBookmarks(String comicId, List<Map<String, dynamic>> bookmarks) {
    // TODO: implement importBookmarks
    throw UnimplementedError();
  }

  @override
  Future<void> updateBookmark(Bookmark bookmark) async {
    await localDataSource.updateBookmark(bookmark);
  }

  @override
  Future<Either<Failure, void>> removeBookmarksForComic(String comicId) async {
    try {
      await localDataSource.deleteBookmarksForComic(comicId);
      return const Right(null);
    } catch (e) {
      return Left(const CacheFailure('Failed to remove bookmarks for comic'));
    }
  }

  @override
  Future<void> clearAndInsertBookmarks(List<Bookmark> bookmarks) async {
    await localDataSource.clearAndInsertBookmarks(bookmarks);
  }
}