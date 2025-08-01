import 'package:dartz/dartz.dart';
import 'package:easy_comic/core/error/failures.dart';
import 'package:easy_comic/domain/entities/comic.dart';
import 'package:easy_comic/domain/repositories/comic_repository.dart';
import 'package:equatable/equatable.dart';

class GetBookshelfComicsUsecase {
  final ComicRepository repository;

  GetBookshelfComicsUsecase(this.repository);

  Future<Either<Failure, List<Comic>>> call(Params params) async {
    final int bookshelfId = int.tryParse(params.bookshelfId) ?? 1; // Default to 1, a common default ID
    return await repository.getComicsInBookshelf(bookshelfId, limit: params.limit, offset: params.offset);
  }
}

class Params extends Equatable {
  final String bookshelfId;
  final int limit;
  final int offset;

  const Params({required this.bookshelfId, this.limit = 20, this.offset = 0});

  @override
  List<Object> get props => [bookshelfId, limit, offset];
}