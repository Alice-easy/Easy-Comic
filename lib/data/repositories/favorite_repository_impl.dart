import 'dart:convert';
import 'package:easy_comic/core/error/failures.dart';
import 'package:dartz/dartz.dart';
import 'package:easy_comic/data/datasources/local/favorite_local_datasource.dart';
import 'package:easy_comic/domain/entities/comic.dart';
import 'package:easy_comic/domain/entities/favorite.dart';
import 'package:drift/drift.dart';
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

  @override
  Future<Either<Failure, int>> createFavorite(String name) async {
    try {
      final companion = db.FavoritesCompanion(
        name: Value(name),
        createTime: Value(DateTime.now()),
      );
      final id = await localDataSource.createFavorite(companion);
      return Right(id);
    } on DatabaseException catch (e) {
      return Left(DatabaseFailure(e.message));
    }
  }

  @override
  Future<Either<Failure, void>> deleteFavorite(int id) async {
    try {
      await localDataSource.deleteFavorite(id);
      return const Right(null);
    } on DatabaseException catch (e) {
      return Left(DatabaseFailure(e.message));
    }
  }

  @override
  Future<Either<Failure, void>> removeComicFromFavorite(String comicId, int favoriteId) async {
    try {
      await localDataSource.removeComicFromFavorite(comicId, favoriteId);
      return const Right(null);
    } on DatabaseException catch (e) {
      return Left(DatabaseFailure(e.message));
    }
  }

  @override
  Future<Either<Failure, List<Comic>>> getComicsInFavorite(int favoriteId) async {
    try {
      final models = await localDataSource.getComicsInFavorite(favoriteId);
      final entities = models.map((model) => Comic(
        id: model.id,
        title: model.fileName.split('.').first,
        author: 'Unknown Author', // Default author
        path: model.filePath,
        filePath: model.filePath,
        fileName: model.fileName,
        coverPath: model.coverPath,
        pageCount: model.pageCount,
        addTime: model.addTime,
        lastReadTime: model.lastReadTime,
        progress: model.progress,
        bookshelfId: model.bookshelfId,
        isFavorite: model.isFavorite,
        tags: List<String>.from(jsonDecode(model.tags)),
        metadata: Map<String, dynamic>.from(jsonDecode(model.metadata)),
        // Additional required properties
        addedAt: model.addTime,
        lastReadAt: model.lastReadTime ?? DateTime.now(),
        currentPage: model.progress,
        totalPages: model.pageCount,
        pages: const [],
      )).toList();
      return Right(entities);
    } on DatabaseException catch (e) {
      return Left(DatabaseFailure(e.message));
    }
  }
}