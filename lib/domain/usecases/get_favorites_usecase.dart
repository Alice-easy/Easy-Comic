import 'package:easy_comic/core/error/failures.dart';
import 'package:dartz/dartz.dart';
import 'package:easy_comic/domain/entities/favorite.dart';
import 'package:easy_comic/domain/repositories/favorite_repository.dart';

class GetFavoritesUseCase {
  final FavoriteRepository repository;

  GetFavoritesUseCase(this.repository);

  Future<Either<Failure, List<Favorite>>> call() async {
    try {
      final favorites = await repository.getFavorites();
      return Right(favorites);
    } catch (e) {
      return Left(ServerFailure(e.toString()));
    }
  }
}