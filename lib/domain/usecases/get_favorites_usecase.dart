import 'package:easy_comic/core/error/failures.dart';
import 'package:easy_comic/core/utils/either.dart';
import 'package:easy_comic/domain/entities/favorite.dart';
import 'package:easy_comic/domain/repositories/favorite_repository.dart';

class GetFavoritesUseCase {
  final FavoriteRepository repository;

  GetFavoritesUseCase(this.repository);

  Future<Either<Failure, List<Favorite>>> call() async {
    return await repository.getFavorites();
  }
}