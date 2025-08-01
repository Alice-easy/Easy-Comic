import 'package:easy_comic/core/error/failures.dart';
import 'package:easy_comic/core/utils/either.dart';
import 'package:easy_comic/domain/entities/comic.dart';
import 'package:easy_comic/domain/repositories/favorite_repository.dart';

class GetFavoriteComicsUseCase {
  final FavoriteRepository repository;

  GetFavoriteComicsUseCase(this.repository);

  Future<Either<Failure, List<Comic>>> call(int favoriteId) async {
    return await repository.getComicsInFavorite(favoriteId);
  }
}