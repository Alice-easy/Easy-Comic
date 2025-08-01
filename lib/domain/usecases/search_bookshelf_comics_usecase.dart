import 'package:dartz/dartz.dart';
import 'package:easy_comic/core/error/failures.dart';
import 'package:easy_comic/domain/entities/comic.dart';
import 'package:easy_comic/domain/repositories/comic_repository.dart';

class SearchBookshelfComicsUsecase {
  final ComicRepository repository;

  SearchBookshelfComicsUsecase(this.repository);

  Future<Either<Failure, List<Comic>>> call(
      String bookshelfId, String query) async {
    final int id = int.tryParse(bookshelfId) ?? 1;
    return await repository.searchComicsInBookshelf(id, query);
  }
}