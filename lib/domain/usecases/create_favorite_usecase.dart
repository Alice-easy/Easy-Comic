import 'package:easy_comic/core/error/failures.dart';
import 'package:easy_comic/core/utils/either.dart';
import 'package:easy_comic/domain/entities/favorite.dart';
import 'package:easy_comic/domain/repositories/favorite_repository.dart';

class CreateFavoriteUseCase {
  final FavoriteRepository repository;

  CreateFavoriteUseCase(this.repository);

  Future<Either<Failure, void>> call(String name) async {
    return await repository.createFavorite(name);
  }
}