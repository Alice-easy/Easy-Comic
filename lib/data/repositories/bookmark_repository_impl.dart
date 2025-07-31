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
  Future<void> addBookmark(Bookmark bookmark) async {
    await localDataSource.saveBookmark(bookmark);
  }

  @override
  Future<void> removeBookmark(String bookmarkId) async {
    await localDataSource.deleteBookmark(bookmarkId);
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
  Future<bool> isBookmarked(String comicId, int pageIndex) async {
    final bookmarks = await getBookmarks(comicId);
    return bookmarks.any((bookmark) => bookmark.pageIndex == pageIndex);
  }

  @override
  Future<List<Bookmark>> getAllBookmarks() async {
    // For now, return empty list
    // In a full implementation, this would get all bookmarks across all comics
    return [];
  }
}