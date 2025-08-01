import 'dart:convert';
import 'dart:typed_data';

import 'package:dartz/dartz.dart';
import 'package:easy_comic/core/error/failures.dart';
import 'package:easy_comic/domain/entities/comic.dart';
import 'package:easy_comic/domain/entities/favorite.dart';
import 'package:easy_comic/domain/entities/webdav_config.dart';
import 'package:easy_comic/domain/repositories/comic_repository.dart';
import 'package:easy_comic/domain/repositories/favorite_repository.dart';
import 'package:easy_comic/domain/services/webdav_service.dart';

class RestoreDataFromWebdavUseCase {
  final WebDAVService webDAVService;
  final ComicRepository comicRepository;
  final FavoriteRepository favoriteRepository;

  RestoreDataFromWebdavUseCase({
    required this.webDAVService,
    required this.comicRepository,
    required this.favoriteRepository,
  });

  Future<Either<Failure, Unit>> call(WebDAVConfig config) async {
    try {
      final backupDataResult = await webDAVService.restore(
        config: config,
        fileName: 'easy_comic_backup.json',
      );

      return backupDataResult.fold(
        (failure) => Left(failure),
        (data) async {
          try {
            final jsonString = utf8.decode(data);
            final decodedData = jsonDecode(jsonString);

            final comics = (decodedData['comics'] as List)
                .map((item) => Comic.fromJson(item))
                .toList();
            
            final favorites = (decodedData['favorites'] as List)
                .map((item) => Favorite.fromJson(item))
                .toList();

            await comicRepository.clearAndInsertComics(comics);
            await favoriteRepository.clearAndInsertFavorites(favorites);

            return const Right(unit);
          } catch (e) {
            return Left(CacheFailure(message: 'Failed to parse backup file.'));
          }
        },
      );
    } catch (e) {
      return Left(ServerFailure(message: 'Failed to restore data.'));
    }
  }
}