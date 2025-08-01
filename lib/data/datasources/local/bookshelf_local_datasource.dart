import 'package:drift/drift.dart';
import 'package:easy_comic/core/error/failures.dart';
import '../../drift_db.dart';

abstract class BookshelfLocalDataSource {
  Stream<List<BookshelfModel>> watchAllBookshelves();
  Future<int> addBookshelf(BookshelvesCompanion bookshelf);
  Future<void> updateBookshelf(BookshelvesCompanion bookshelf);
  Future<void> deleteBookshelf(int id);
}

class BookshelfLocalDataSourceImpl implements BookshelfLocalDataSource {
  final AppDatabase db;

  BookshelfLocalDataSourceImpl({required this.db});

  @override
  Stream<List<BookshelfModel>> watchAllBookshelves() {
    try {
      return db.bookshelvesDao.watchAllBookshelves();
    } catch (e) {
      throw DatabaseException(e.toString());
    }
  }

  @override
  Future<int> addBookshelf(BookshelvesCompanion bookshelf) async {
    try {
      return await db.bookshelvesDao.addBookshelf(bookshelf);
    } catch (e) {
      throw DatabaseException(e.toString());
    }
  }

  @override
  Future<void> updateBookshelf(BookshelvesCompanion bookshelf) async {
    try {
      await db.bookshelvesDao.updateBookshelf(bookshelf);
    } catch (e) {
      throw DatabaseException(e.toString());
    }
  }

  @override
  Future<void> deleteBookshelf(int id) async {
    try {
      await db.bookshelvesDao.deleteBookshelf(id);
    } catch (e) {
      throw DatabaseException(e.toString());
    }
  }
}