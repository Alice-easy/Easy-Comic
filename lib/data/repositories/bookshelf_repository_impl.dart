import 'package:drift/drift.dart';
import 'package:easy_comic/core/error/failures.dart';
import 'package:easy_comic/core/utils/either.dart';
import 'package:easy_comic/data/datasources/local/bookshelf_local_datasource.dart';
import 'package:easy_comic/domain/entities/bookshelf.dart';
import 'package:easy_comic/domain/repositories/bookshelf_repository.dart';
import '../drift_db.dart' as db;

class BookshelfRepositoryImpl implements BookshelfRepository {
  final BookshelfLocalDataSource localDataSource;

  BookshelfRepositoryImpl({required this.localDataSource});

  @override
  Stream<Either<Failure, List<Bookshelf>>> watchAllBookshelves() {
    return localDataSource.watchAllBookshelves().map(
      (models) {
        final entities = models.map(_modelToEntity).toList();
        return Right<Failure, List<Bookshelf>>(entities);
      },
    ).handleError((error) {
      return Left<Failure, List<Bookshelf>>(DatabaseFailure(error.toString()));
    });
  }

  @override
  Future<Either<Failure, int>> addBookshelf(String name) async {
    try {
      final companion = db.BookshelvesCompanion(
        name: Value(name),
        createTime: Value(DateTime.now()),
      );
      final id = await localDataSource.addBookshelf(companion);
      return Right(id);
    } on DatabaseException catch (e) {
      return Left(DatabaseFailure(e.message));
    }
  }

  @override
  Future<Either<Failure, void>> updateBookshelf(Bookshelf bookshelf) async {
    try {
      final companion = _entityToCompanion(bookshelf);
      await localDataSource.updateBookshelf(companion);
      return const Right(null);
    } on DatabaseException catch (e) {
      return Left(DatabaseFailure(e.message));
    }
  }

  @override
  Future<Either<Failure, void>> deleteBookshelf(int id) async {
    try {
      await localDataSource.deleteBookshelf(id);
      return const Right(null);
    } on DatabaseException catch (e) {
      return Left(DatabaseFailure(e.message));
    }
  }

  Bookshelf _modelToEntity(db.BookshelfModel model) {
    return Bookshelf(
      id: model.id,
      name: model.name,
      coverImage: model.coverImage,
      createTime: model.createTime,
    );
  }

  db.BookshelvesCompanion _entityToCompanion(Bookshelf entity) {
    return db.BookshelvesCompanion(
      id: Value(entity.id),
      name: Value(entity.name),
      coverImage: Value(entity.coverImage),
      createTime: Value(entity.createTime),
    );
  }
}