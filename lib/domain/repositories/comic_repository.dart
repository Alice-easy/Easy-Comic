import 'package:easy_comic/core/error/failures.dart';
import 'package:easy_comic/core/utils/either.dart';
import 'package:easy_comic/domain/entities/comic.dart';

abstract class ComicRepository {
  Future<Either<Failure, List<Comic>>> getComicsInBookshelf(int bookshelfId, {int limit, int offset});
  Stream<Either<Failure, List<Comic>>> watchComicsInBookshelf(int bookshelfId);
  Future<Either<Failure, Comic>> getComic(String id);
  Future<Either<Failure, void>> addComic(Comic comic);
  Future<Either<Failure, void>> updateComic(Comic comic);
  Future<Either<Failure, void>> deleteComic(String id);
  Future<List<Comic>> getAllComics();
  Future<void> clearAndInsertComics(List<Comic> comics);
}