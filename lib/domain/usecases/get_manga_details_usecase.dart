import 'package:dartz/dartz.dart';
import 'package:easy_comic/core/failures/failures.dart';
import 'package:easy_comic/core/usecases/usecase.dart';
import 'package:easy_comic/domain/entities/manga.dart';
import 'package:easy_comic/domain/repositories/manga_repository.dart';

class GetMangaDetailsUseCase implements UseCase<Manga, String> {
  final MangaRepository repository;

  GetMangaDetailsUseCase(this.repository);

  @override
  Future<Either<Failure, Manga>> call(String params) {
    return repository.getMangaDetails(params);
  }
}