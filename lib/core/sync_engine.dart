import 'dart:convert';
import 'dart:typed_data';

import 'package:dartz/dartz.dart';
import 'package:easy_comic/core/error/failures.dart';
import 'package:easy_comic/domain/entities/bookmark.dart';
import 'package:easy_comic/domain/entities/comic.dart';
import 'package:easy_comic/domain/entities/comic_progress.dart';
import 'package:easy_comic/domain/entities/favorite.dart';
import 'package:easy_comic/domain/entities/webdav_config.dart';
import 'package:easy_comic/domain/entities/reader_settings.dart';
import 'package:easy_comic/domain/repositories/bookmark_repository.dart';
import 'package:easy_comic/domain/repositories/comic_repository.dart';
import 'package:easy_comic/domain/repositories/favorite_repository.dart';
import 'package:easy_comic/domain/repositories/settings_repository.dart';
import 'package:easy_comic/domain/services/webdav_service.dart';
import 'package:easy_comic/models/sync_models.dart';

const _syncFileName = 'easy_comic_sync.json';

class SyncEngine {
  final WebDAVService _webDAVService;
  final SettingsRepository _settingsRepository;
  final ComicRepository _comicRepository;
  final FavoriteRepository _favoriteRepository;
  final BookmarkRepository _bookmarkRepository;

  SyncEngine({
    required WebDAVService webDAVService,
    required SettingsRepository settingsRepository,
    required ComicRepository comicRepository,
    required FavoriteRepository favoriteRepository,
    required BookmarkRepository bookmarkRepository,
  })  : _webDAVService = webDAVService,
        _settingsRepository = settingsRepository,
        _comicRepository = comicRepository,
        _favoriteRepository = favoriteRepository,
        _bookmarkRepository = bookmarkRepository;

  Future<Either<Failure, Unit>> performSync() async {
    try {
      // 1. Get WebDAV config
      final config = await _settingsRepository.getWebDAVConfig();
      if (config.uri.isEmpty) {
        return Left(ConfigurationFailure('WebDAV is not configured.'));
      }

      // 2. Fetch remote package
      final remotePackageResult = await _webDAVService.restore(
        config: config,
        fileName: _syncFileName,
      );

      SyncPackage? remotePackage;
      remotePackageResult.fold(
        (failure) {
          if (failure is! NotFoundFailure) {
            // It's a real error, propagate it
            throw failure;
          }
          // If it's NotFoundFailure, it's okay, we'll just upload a new file.
        },
        (data) {
          remotePackage = SyncPackage.fromJson(jsonDecode(utf8.decode(data)));
        },
      );

      // 3. Build local package
      final localPackage = await buildLocalSyncPackage();

      // 4. Merge packages
      final mergedPackage = _mergePackages(localPackage, remotePackage);

      // 5. Upload merged package
      final uploadData = Uint8List.fromList(utf8.encode(jsonEncode(mergedPackage.toJson())));
      final uploadResult = await _webDAVService.backup(
        config: config,
        fileName: _syncFileName,
        data: uploadData,
      );

      return await uploadResult.fold(
        (failure) => Left(failure),
        (_) async {
          // 6. Apply merged data locally
          await _applyMergedPackage(mergedPackage);
          return const Right(unit);
        },
      );
    } on Failure catch (e) {
      return Left(e);
    } catch (e) {
      return Left(UnknownFailure(e.toString()));
    }
  }

  SyncPackage _mergePackages(SyncPackage local, SyncPackage? remote) {
    if (remote == null) {
      return local;
    }

    // "Last write wins" based on timestamp
    if (local.lastModified.isAfter(remote.lastModified)) {
      return local.copyWith(lastModified: DateTime.now());
    } else {
      return remote.copyWith(lastModified: DateTime.now());
    }
  }

  Future<void> _applyMergedPackage(SyncPackage package) async {
    await _settingsRepository.saveReaderSettings(package.settings);
    await _comicRepository.applySyncChanges(package.progress);
    await _favoriteRepository.clearAndInsertFavorites(package.favorites);
    await _bookmarkRepository.clearAndInsertBookmarks(package.bookmarks);
  }

  Future<SyncPackage> buildLocalSyncPackage() async {
    final settingsResult = await _settingsRepository.getReaderSettings();
    final settings = settingsResult.getOrElse(() => const ReaderSettings());

    final progress = await _comicRepository.getAllProgress();
    final favorites = await _favoriteRepository.getFavorites();
    final bookmarks = await _bookmarkRepository.getAllBookmarks();

    return SyncPackage(
      lastModified: DateTime.now(),
      settings: settings,
      progress: progress,
      favorites: favorites,
      bookmarks: bookmarks,
    );
  }
}
