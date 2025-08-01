import 'package:easy_comic/core/error/failures.dart';
import 'package:dartz/dartz.dart';
import 'package:easy_comic/domain/entities/comic.dart';

enum SortType { dateAdded, title, author }

abstract class ComicRepository {
  Future<Either<Failure, List<Comic>>> getComicsInBookshelf(int bookshelfId, {int limit, int offset});
  Stream<Either<Failure, List<Comic>>> watchComicsInBookshelf(int bookshelfId);
  Future<Either<Failure, Comic>> getComic(String id);
  Future<Either<Failure, void>> addComic(Comic comic);
  Future<Either<Failure, void>> updateComic(Comic comic);
  Future<Either<Failure, void>> deleteComic(String id);
  Future<List<Comic>> getAllComics();
  Future<void> clearAndInsertComics(List<Comic> comics);
  Future<Either<Failure, List<Comic>>> searchComicsInBookshelf(int bookshelfId, String query);
  Future<Either<Failure, List<Comic>>> sortComicsInBookshelf(int bookshelfId, SortType sortType);
}