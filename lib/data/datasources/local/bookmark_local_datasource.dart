import '../../../data/drift_db.dart' hide Bookmark;
import '../../../domain/entities/bookmark.dart';

abstract class BookmarkLocalDataSource {
  Future<List<Bookmark>> getBookmarks(String comicId);
  Future<void> saveBookmark(Bookmark bookmark);
  Future<void> deleteBookmark(String bookmarkId);
  Future<Bookmark?> getBookmarkById(String bookmarkId);
  Stream<List<Bookmark>> watchBookmarks(String comicId);
}

class BookmarkLocalDataSourceImpl implements BookmarkLocalDataSource {
  final AppDatabase database;
  
  BookmarkLocalDataSourceImpl({required this.database});

  @override
  Future<List<Bookmark>> getBookmarks(String comicId) async {
    // For now, return empty list
    // In a full implementation, this would query database
    return [];
  }

  @override
  Future<void> saveBookmark(Bookmark bookmark) async {
    // For now, this is a placeholder
    // In a full implementation, this would save to database
  }

  @override
  Future<void> deleteBookmark(String bookmarkId) async {
    // For now, this is a placeholder
    // In a full implementation, this would delete from database
  }

  @override
  Future<Bookmark?> getBookmarkById(String bookmarkId) async {
    // For now, return null
    // In a full implementation, this would query database
    return null;
  }

  @override
  Stream<List<Bookmark>> watchBookmarks(String comicId) {
    // For now, return empty stream
    // In a full implementation, this would watch database changes
    return Stream.value([]);
  }
}