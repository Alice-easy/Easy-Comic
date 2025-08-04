import 'package:dartz/dartz.dart';
import 'package:easy_comic/core/failures/failures.dart';
import 'package:easy_comic/core/usecases/usecase.dart';
import 'package:easy_comic/domain/entities/manga.dart';
import 'package:easy_comic/domain/repositories/manga_repository.dart';

class GetLibraryMangasUseCase implements UseCase<List<Manga>, NoParams> {
  final MangaRepository repository;

  GetLibraryMangasUseCase(this.repository);

  @override
  Future<Either<Failure, List<Manga>>> call(NoParams params) async {
    return await repository.getLibraryMangas();
  }
}