import 'package:dartz/dartz.dart';
import 'package:easy_comic/core/failures/failures.dart';
import 'package:easy_comic/core/usecases/usecase.dart';
import 'package:easy_comic/domain/repositories/manga_repository.dart';

class ToggleFavoriteUseCase implements UseCase<void, String> {
  final MangaRepository repository;

  ToggleFavoriteUseCase(this.repository);

  @override
  Future<Either<Failure, void>> call(String params) async {
    return await repository.toggleMangaFavoriteStatus(params);
  }
}