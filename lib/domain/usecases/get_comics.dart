// lib/domain/usecases/get_comics.dart

import 'package:easy_comic/core/error/failures.dart';
import 'package:dartz/dartz.dart';
import 'package:easy_comic/domain/entities/comic.dart';
import 'package:easy_comic/domain/repositories/comic_repository.dart';

class GetComics {
  final ComicRepository repository;

  GetComics(this.repository);

  Future<Either<Failure, List<Comic>>> call() async {
    return await repository.getComics();
  }
}