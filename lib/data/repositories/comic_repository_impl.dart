import 'dart:convert';
import 'package:drift/drift.dart';
import 'package:easy_comic/core/error/failures.dart';
import 'package:dartz/dartz.dart';
import 'package:easy_comic/data/datasources/local/comic_local_datasource.dart';
import 'package:easy_comic/domain/entities/comic.dart';
import 'package:easy_comic/domain/repositories/comic_repository.dart';
import '../drift_db.dart' as db;

class ComicRepositoryImpl implements ComicRepository {
  final ComicLocalDataSource localDataSource;

  ComicRepositoryImpl({required this.localDataSource});

  @override
  Future<Either<Failure, List<Comic>>> getComicsInBookshelf(int bookshelfId, {int limit = 20, int offset = 0}) async {
    try {
      final models = await localDataSource.getComicsInBookshelf(bookshelfId, limit: limit, offset: offset);
      final entities = models.map(_modelToEntity).toList();
      return Right(entities);
    } on DatabaseException catch (e) {
      return Left(DatabaseFailure(e.message));
    }
  }

  @override
  Stream<Either<Failure, List<Comic>>> watchComicsInBookshelf(int bookshelfId) {
    return localDataSource.watchComicsInBookshelf(bookshelfId).map(
      (models) {
        final entities = models.map(_modelToEntity).toList();
        return Right<Failure, List<Comic>>(entities);
      },
    ).handleError((error) {
      return Left<Failure, List<Comic>>(DatabaseFailure(error.toString()));
    });
  }

  @override
  Future<Either<Failure, Comic>> getComic(String id) async {
    try {
      final model = await localDataSource.getComic(id);
      if (model != null) {
        return Right(_modelToEntity(model));
      } else {
        return Left(NotFoundFailure('Comic not found.'));
      }
    } on DatabaseException catch (e) {
      return Left(DatabaseFailure(e.message));
    }
  }

  @override
  Future<Either<Failure, void>> addComic(Comic comic) async {
    try {
      final companion = _entityToCompanion(comic);
      await localDataSource.addComic(companion);
      return const Right(null);
    } on DatabaseException catch (e) {
      return Left(DatabaseFailure(e.message));
    }
  }

  @override
  Future<Either<Failure, void>> updateComic(Comic comic) async {
    try {
      final companion = _entityToCompanion(comic);
      await localDataSource.updateComic(companion);
      return const Right(null);
    } on DatabaseException catch (e) {
      return Left(DatabaseFailure(e.message));
    }
  }

  @override
  Future<Either<Failure, void>> deleteComic(String id) async {
    try {
      await localDataSource.deleteComic(id);
      return const Right(null);
    } on DatabaseException catch (e) {
      return Left(DatabaseFailure(e.message));
    }
  }

  // --- Mappers ---

  Comic _modelToEntity(db.ComicModel model) {
    return Comic(
      id: model.id,
      title: model.fileName.split('.').first,
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
    );
  }

  db.ComicsCompanion _entityToCompanion(Comic entity) {
    return db.ComicsCompanion(
      id: Value(entity.id),
      // title and path are not in the database model, so they are not mapped here.
      filePath: Value(entity.filePath),
      fileName: Value(entity.fileName),
      coverPath: Value(entity.coverPath),
      pageCount: Value(entity.pageCount),
      addTime: Value(entity.addTime),
      lastReadTime: Value(entity.lastReadTime),
      progress: Value(entity.progress),
      bookshelfId: Value(entity.bookshelfId),
      isFavorite: Value(entity.isFavorite),
      tags: Value(jsonEncode(entity.tags)),
      metadata: Value(jsonEncode(entity.metadata)),
    );
  }

  @override
  Future<List<Comic>> getAllComics() async {
    final models = await localDataSource.getAllComics();
    return models.map(_modelToEntity).toList();
  }

  @override
  Future<void> clearAndInsertComics(List<Comic> comics) async {
    final companions = comics.map(_entityToCompanion).toList();
    await localDataSource.clearAndInsertComics(companions);
  }

  @override
  Future<Either<Failure, List<Comic>>> searchComicsInBookshelf(int bookshelfId, String query) async {
    // TODO: Implement search in LocalDataSource and DAO
    return Right([]);
  }

  @override
  Future<Either<Failure, List<Comic>>> sortComicsInBookshelf(int bookshelfId, SortType sortType) async {
    // TODO: Implement sort in LocalDataSource and DAO
    return Right([]);
  }
}