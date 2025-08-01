import 'package:easy_comic/core/error/failures.dart';
import 'package:easy_comic/core/utils/either.dart';
import 'package:easy_comic/domain/entities/favorite.dart';

abstract class FavoriteRepository {
  Stream<Either<Failure, List<Favorite>>> watchAllFavorites();
  Future<Either<Failure, void>> addComicToFavorite(String comicId, int favoriteId);
  Future<List<Favorite>> getFavorites();
  Future<void> clearAndInsertFavorites(List<Favorite> favorites);
}