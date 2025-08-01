import 'package:drift/drift.dart';
import 'package:easy_comic/core/error/failures.dart';
import 'package:easy_comic/domain/repositories/comic_repository.dart';
import '../../drift_db.dart';

abstract class ComicLocalDataSource {
  Future<List<ComicModel>> getComicsInBookshelf(int bookshelfId, {int limit, int offset});
  Stream<List<ComicModel>> watchComicsInBookshelf(int bookshelfId);
  Future<ComicModel?> getComic(String id);
  Future<void> addComic(ComicsCompanion comic);
  Future<void> updateComic(ComicsCompanion comic);
  Future<void> deleteComic(String id);
  Future<List<ComicModel>> getAllComics();
  Future<void> clearAndInsertComics(List<ComicsCompanion> comics);
  Future<List<ComicModel>> searchComicsInBookshelf(int bookshelfId, String query);
  Future<List<ComicModel>> sortComicsInBookshelf(int bookshelfId, SortType sortType);
}

class ComicLocalDataSourceImpl implements ComicLocalDataSource {
  final AppDatabase db;

  ComicLocalDataSourceImpl({required this.db});

  @override
  Future<List<ComicModel>> getComicsInBookshelf(int bookshelfId, {int limit = 20, int offset = 0}) async {
    try {
      return await db.comicsDao.getComicsInBookshelf(bookshelfId, limit: limit, offset: offset);
    } catch (e) {
      throw DatabaseException(e.toString());
    }
  }

  @override
  Stream<List<ComicModel>> watchComicsInBookshelf(int bookshelfId) {
    try {
      return db.comicsDao.watchComicsInBookshelf(bookshelfId);
    } catch (e) {
      throw DatabaseException(e.toString());
    }
  }

  @override
  Future<ComicModel?> getComic(String id) async {
    try {
      return await db.comicsDao.getComic(id);
    } catch (e) {
      throw DatabaseException(e.toString());
    }
  }

  @override
  Future<void> addComic(ComicsCompanion comic) async {
    try {
      await db.comicsDao.addComic(comic);
    } catch (e) {
      throw DatabaseException(e.toString());
    }
  }

  @override
  Future<void> updateComic(ComicsCompanion comic) async {
    try {
      await db.comicsDao.updateComic(comic);
    } catch (e) {
      throw DatabaseException(e.toString());
    }
  }

  @override
  Future<void> deleteComic(String id) async {
    try {
      await db.comicsDao.deleteComic(id);
    } catch (e) {
      throw DatabaseException(e.toString());
    }
  }

  @override
  Future<List<ComicModel>> getAllComics() async {
    try {
      return await db.comicsDao.getAllComics();
    } catch (e) {
      throw DatabaseException(e.toString());
    }
  }

  @override
  Future<void> clearAndInsertComics(List<ComicsCompanion> comics) async {
    try {
      await db.comicsDao.clearAndInsertComics(comics);
    } catch (e) {
      throw DatabaseException(e.toString());
    }
  }

  @override
  Future<List<ComicModel>> searchComicsInBookshelf(int bookshelfId, String query) async {
    try {
      return await db.comicsDao.searchComicsInBookshelf(bookshelfId, query);
    } catch (e) {
      throw DatabaseException(e.toString());
    }
  }

  @override
  Future<List<ComicModel>> sortComicsInBookshelf(int bookshelfId, SortType sortType) async {
    try {
      return await db.comicsDao.sortComicsInBookshelf(bookshelfId, sortType);
    } catch (e) {
      throw DatabaseException(e.toString());
    }
  }
}