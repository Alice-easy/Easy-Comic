import 'package:easy_comic/core/error/failures.dart';
import '../../drift_db.dart';

abstract class FavoriteLocalDataSource {
  Stream<List<FavoriteModel>> watchAllFavorites();
  Future<void> addComicToFavorite(String comicId, int favoriteId);
  Future<List<FavoriteModel>> getAllFavorites();
  Future<void> clearAndInsertFavorites(List<FavoritesCompanion> favorites);
  Future<int> createFavorite(FavoritesCompanion favorite);
  Future<void> deleteFavorite(int id);
  Future<void> removeComicFromFavorite(String comicId, int favoriteId);
  Future<List<ComicModel>> getComicsInFavorite(int favoriteId);
}

class FavoriteLocalDataSourceImpl implements FavoriteLocalDataSource {
  final AppDatabase db;

  FavoriteLocalDataSourceImpl({required this.db});

  @override
  Stream<List<FavoriteModel>> watchAllFavorites() {
    try {
      return db.favoritesDao.watchAllFavorites();
    } catch (e) {
      throw DatabaseException(e.toString());
    }
  }

  @override
  Future<void> addComicToFavorite(String comicId, int favoriteId) async {
    try {
      await db.favoritesDao.addComicToFavorite(comicId, favoriteId);
    } catch (e) {
      throw DatabaseException(e.toString());
    }
  }

  @override
  Future<List<FavoriteModel>> getAllFavorites() async {
    try {
      return await db.favoritesDao.getAllFavorites();
    } catch (e) {
      throw DatabaseException(e.toString());
    }
  }

  @override
  Future<void> clearAndInsertFavorites(List<FavoritesCompanion> favorites) async {
    try {
      await db.favoritesDao.clearAndInsertFavorites(favorites);
    } catch (e) {
      throw DatabaseException(e.toString());
    }
  }

  @override
  Future<int> createFavorite(FavoritesCompanion favorite) async {
    try {
      return await db.favoritesDao.createFavorite(favorite);
    } catch (e) {
      throw DatabaseException(e.toString());
    }
  }

  @override
  Future<void> deleteFavorite(int id) async {
    try {
      await db.favoritesDao.deleteFavorite(id);
    } catch (e) {
      throw DatabaseException(e.toString());
    }
  }

  @override
  Future<void> removeComicFromFavorite(String comicId, int favoriteId) async {
    try {
      await db.favoritesDao.removeComicFromFavorite(comicId, favoriteId);
    } catch (e) {
      throw DatabaseException(e.toString());
    }
  }

  @override
  Future<List<ComicModel>> getComicsInFavorite(int favoriteId) async {
    try {
      return await db.favoritesDao.getComicsInFavorite(favoriteId);
    } catch (e) {
      throw DatabaseException(e.toString());
    }
  }
}