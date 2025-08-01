import 'package:easy_comic/core/error/failures.dart';
import 'package:dartz/dartz.dart';
import 'package:easy_comic/domain/repositories/favorite_repository.dart';

class DeleteFavoriteUseCase {
  final FavoriteRepository repository;

  DeleteFavoriteUseCase(this.repository);

  Future<Either<Failure, void>> call(int id) async {
    return await repository.deleteFavorite(id);
  }
}