import 'package:easy_comic/core/error/failures.dart';
import '../../drift_db.dart';

abstract class FavoriteLocalDataSource {
  Stream<List<FavoriteModel>> watchAllFavorites();
  Future<void> addComicToFavorite(String comicId, int favoriteId);
  Future<List<FavoriteModel>> getAllFavorites();
  Future<void> clearAndInsertFavorites(List<FavoritesCompanion> favorites);
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
}