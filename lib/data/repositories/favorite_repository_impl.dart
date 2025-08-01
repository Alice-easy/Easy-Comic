import 'package:easy_comic/core/error/failures.dart';
import 'package:easy_comic/core/utils/either.dart';
import 'package:easy_comic/data/datasources/local/favorite_local_datasource.dart';
import 'package:easy_comic/domain/entities/favorite.dart';
import 'package:easy_comic/domain/repositories/favorite_repository.dart';
import '../drift_db.dart' as db;

class FavoriteRepositoryImpl implements FavoriteRepository {
  final FavoriteLocalDataSource localDataSource;

  FavoriteRepositoryImpl({required this.localDataSource});

  @override
  Stream<Either<Failure, List<Favorite>>> watchAllFavorites() {
    return localDataSource.watchAllFavorites().map(
      (models) {
        final entities = models.map(_modelToEntity).toList();
        return Right<Failure, List<Favorite>>(entities);
      },
    ).handleError((error) {
      return Left<Failure, List<Favorite>>(DatabaseFailure(error.toString()));
    });
  }

  @override
  Future<Either<Failure, void>> addComicToFavorite(String comicId, int favoriteId) async {
    try {
      await localDataSource.addComicToFavorite(comicId, favoriteId);
      return const Right(null);
    } on DatabaseException catch (e) {
      return Left(DatabaseFailure(e.message));
    }
  }

  Favorite _modelToEntity(db.FavoriteModel model) {
    return Favorite(
      id: model.id,
      name: model.name,
      parentId: model.parentId,
      description: model.description,
      createTime: model.createTime,
    );
  }

  db.FavoritesCompanion _entityToCompanion(Favorite entity) {
    return db.FavoritesCompanion(
      id: Value(entity.id),
      name: Value(entity.name),
      parentId: Value(entity.parentId),
      description: Value(entity.description),
      createTime: Value(entity.createTime),
    );
  }

  @override
  Future<List<Favorite>> getFavorites() async {
    final models = await localDataSource.getAllFavorites();
    return models.map(_modelToEntity).toList();
  }

  @override
  Future<void> clearAndInsertFavorites(List<Favorite> favorites) async {
    final companions = favorites.map(_entityToCompanion).toList();
    await localDataSource.clearAndInsertFavorites(companions);
  }
}