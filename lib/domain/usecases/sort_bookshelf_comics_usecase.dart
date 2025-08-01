import 'package:dartz/dartz.dart';
import 'package:easy_comic/core/error/failures.dart';
import 'package:easy_comic/domain/entities/comic.dart';
import 'package:easy_comic/domain/repositories/bookshelf_repository.dart';

enum SortType { dateAdded, title, author }

class SortBookshelfComicsUsecase {
  final BookshelfRepository repository;

  SortBookshelfComicsUsecase(this.repository);

  Future<Either<Failure, List<Comic>>> call(
      String bookshelfId, SortType sortType) async {
    return await repository.sortComicsInBookshelf(bookshelfId, sortType);
  }
}