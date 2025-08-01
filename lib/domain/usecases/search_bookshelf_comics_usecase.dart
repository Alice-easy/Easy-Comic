import 'package:dartz/dartz.dart';
import 'package:easy_comic/core/error/failures.dart';
import 'package:easy_comic/domain/entities/comic.dart';
import 'package:easy_comic/domain/repositories/bookshelf_repository.dart';

class SearchBookshelfComicsUsecase {
  final BookshelfRepository repository;

  SearchBookshelfComicsUsecase(this.repository);

  Future<Either<Failure, List<Comic>>> call(
      String bookshelfId, String query) async {
    return await repository.searchComicsInBookshelf(bookshelfId, query);
  }
}