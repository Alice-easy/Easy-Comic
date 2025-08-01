import 'package:easy_comic/core/error/failures.dart';
import 'package:easy_comic/core/utils/either.dart';
import 'package:easy_comic/domain/repositories/favorite_repository.dart';

class AddComicToFavoriteUseCase {
  final FavoriteRepository repository;

  AddComicToFavoriteUseCase(this.repository);

  Future<Either<Failure, void>> call(int favoriteId, String comicId) async {
    return await repository.addComicToFavorite(favoriteId, comicId);
  }
}