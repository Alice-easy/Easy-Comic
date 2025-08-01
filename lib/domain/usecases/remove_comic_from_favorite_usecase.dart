import 'package:easy_comic/core/error/failures.dart';
import 'package:easy_comic/core/utils/either.dart';
import 'package:easy_comic/domain/repositories/favorite_repository.dart';

class RemoveComicFromFavoriteUseCase {
  final FavoriteRepository repository;

  RemoveComicFromFavoriteUseCase(this.repository);

  Future<Either<Failure, void>> call(int favoriteId, String comicId) async {
    return await repository.removeComicFromFavorite(favoriteId, comicId);
  }
}