import 'dart:convert';
import 'dart:typed_data';

import 'package:dartz/dartz.dart';
import 'package:easy_comic/core/error/failures.dart';
import 'package:easy_comic/domain/entities/webdav_config.dart';
import 'package:easy_comic/domain/repositories/comic_repository.dart';
import 'package:easy_comic/domain/repositories/favorite_repository.dart';
import 'package:easy_comic/domain/repositories/settings_repository.dart';
import 'package:easy_comic/domain/services/webdav_service.dart';

class BackupDataToWebdavUseCase {
  final WebDAVService webDAVService;
  final ComicRepository comicRepository;
  final FavoriteRepository favoriteRepository;
  final SettingsRepository settingsRepository;

  BackupDataToWebdavUseCase({
    required this.webDAVService,
    required this.comicRepository,
    required this.favoriteRepository,
    required this.settingsRepository,
  });

  Future<Either<Failure, Unit>> call(WebDAVConfig config) async {
    try {
      final comics = await comicRepository.getAllComics();
      final favorites = await favoriteRepository.getFavorites();
      
      // NOTE: We'll likely want to expand what is backed up in the future.
      final dataToBackup = {
        'comics': comics.map((c) => c.toJson()).toList(),
        'favorites': favorites.map((f) => f.toJson()).toList(),
      };

      final jsonString = jsonEncode(dataToBackup);
      final data = Uint8List.fromList(utf8.encode(jsonString));

      return await webDAVService.backup(
        config: config,
        fileName: 'easy_comic_backup.json',
        data: data,
      );
    } catch (e) {
      return Left(CacheFailure('Failed to create backup file.'));
    }
  }
}