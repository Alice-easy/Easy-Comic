import 'package:easy_comic/core/error/failures.dart';
import 'package:easy_comic/core/utils/either.dart';
import 'package:easy_comic/domain/entities/bookshelf.dart';

abstract class BookshelfRepository {
  Stream<Either<Failure, List<Bookshelf>>> watchAllBookshelves();
  Future<Either<Failure, int>> addBookshelf(String name);
  Future<Either<Failure, void>> updateBookshelf(Bookshelf bookshelf);
  Future<Either<Failure, void>> deleteBookshelf(int id);
}