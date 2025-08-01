import 'package:easy_comic/core/error/failures.dart';
import 'package:dartz/dartz.dart';
import 'package:easy_comic/domain/entities/comic.dart';
import 'package:easy_comic/domain/entities/favorite.dart';

abstract class FavoriteRepository {
  Stream<Either<Failure, List<Favorite>>> watchAllFavorites();
  Future<Either<Failure, void>> addComicToFavorite(String comicId, int favoriteId);
  Future<List<Favorite>> getFavorites();
  Future<void> clearAndInsertFavorites(List<Favorite> favorites);
  Future<Either<Failure, int>> createFavorite(String name);
  Future<Either<Failure, void>> deleteFavorite(int id);
  Future<Either<Failure, void>> removeComicFromFavorite(String comicId, int favoriteId);
  Future<Either<Failure, List<Comic>>> getComicsInFavorite(int favoriteId);
}