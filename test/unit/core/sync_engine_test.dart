import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'dart:convert';
import 'dart:typed_data';
import 'package:mockito/annotations.dart';
import 'package:dartz/dartz.dart';
import 'package:easy_comic/core/error/failures.dart';
import 'package:easy_comic/domain/entities/reader_settings.dart';
import 'package:easy_comic/domain/entities/webdav_config.dart';
import 'package:easy_comic/domain/entities/comic_progress.dart';
import 'package:easy_comic/domain/entities/favorite.dart';
import 'package:easy_comic/domain/entities/bookmark.dart';
import 'package:easy_comic/core/sync_engine.dart';
import 'package:easy_comic/domain/repositories/settings_repository.dart';
import 'package:easy_comic/domain/repositories/comic_repository.dart';
import 'package:easy_comic/domain/repositories/bookmark_repository.dart';
import 'package:easy_comic/domain/repositories/favorite_repository.dart';
import 'package:easy_comic/domain/services/webdav_service.dart';
import 'package:easy_comic/models/sync_models.dart';

import 'sync_engine_test.mocks.dart';

@GenerateMocks([
  WebDAVService,
  SettingsRepository,
  ComicRepository,
  BookmarkRepository,
  FavoriteRepository,
])
void main() {
  late SyncEngine syncEngine;
  late MockWebDAVService mockWebDavService;
  late MockSettingsRepository mockSettingsRepository;
  late MockComicRepository mockComicRepository;
  late MockBookmarkRepository mockBookmarkRepository;
  late MockFavoriteRepository mockFavoriteRepository;

  setUp(() {
    mockWebDavService = MockWebDAVService();
    mockSettingsRepository = MockSettingsRepository();
    mockComicRepository = MockComicRepository();
    mockBookmarkRepository = MockBookmarkRepository();
    mockFavoriteRepository = MockFavoriteRepository();

    syncEngine = SyncEngine(
      webDAVService: mockWebDavService,
      settingsRepository: mockSettingsRepository,
      comicRepository: mockComicRepository,
      bookmarkRepository: mockBookmarkRepository,
      favoriteRepository: mockFavoriteRepository,
    );
  });

  group('SyncEngine', () {
    test('should build and upload local package when server is empty (first sync)', () async {
      // Arrange
      final tConfig = const WebDAVConfig(uri: 'http://test.com', username: 'user', password: 'password');
      
      // 1. Setup WebDAV config
      when(mockSettingsRepository.getWebDAVConfig()).thenAnswer((_) async => tConfig);
      
      // 2. Mock remote fetch to simulate not found
      when(mockWebDavService.restore(config: anyNamed('config'), fileName: anyNamed('fileName')))
          .thenAnswer((_) async => Left(const NotFoundFailure('File not found')));
          
      // 3. Mock local data fetching
      when(mockSettingsRepository.getReaderSettings()).thenAnswer((_) async => const Right(ReaderSettings()));
      when(mockComicRepository.getAllProgress()).thenAnswer((_) async => []);
      when(mockFavoriteRepository.getFavorites()).thenAnswer((_) async => []);
      when(mockBookmarkRepository.getAllBookmarks()).thenAnswer((_) async => []);
      
      // 4. Mock upload success
      when(mockWebDavService.backup(config: anyNamed('config'), fileName: anyNamed('fileName'), data: anyNamed('data')))
          .thenAnswer((_) async => const Right(unit));
          
      // 5. Mock local application
      when(mockSettingsRepository.saveReaderSettings(any)).thenAnswer((_) async => const Right(unit));
      when(mockComicRepository.applySyncChanges(any)).thenAnswer((_) async => const Right(unit));
      when(mockFavoriteRepository.clearAndInsertFavorites(any)).thenAnswer((_) async => const Right(unit));
      when(mockBookmarkRepository.clearAndInsertBookmarks(any)).thenAnswer((_) async => const Right(unit));

      // Act
      final result = await syncEngine.performSync();

      // Assert
      expect(result, const Right(unit));
      verify(mockWebDavService.backup(config: tConfig, fileName: 'easy_comic_sync.json', data: anyNamed('data'))).called(1);
      verify(mockSettingsRepository.saveReaderSettings(any)).called(1);
      verify(mockComicRepository.applySyncChanges(any)).called(1);
      verify(mockFavoriteRepository.clearAndInsertFavorites(any)).called(1);
      verify(mockBookmarkRepository.clearAndInsertBookmarks(any)).called(1);
    });
    test('should download, merge (preferring remote), and apply remote package when server has newer data', () async {
      // Arrange
      final tNow = DateTime.now();
      final tRemotePackage = SyncPackage(
        lastModified: tNow.add(const Duration(hours: 1)), // Set to future to ensure it's newer
        settings: const ReaderSettings(appTheme: AppTheme.Dark), // Different from local
        progress: const [],
        favorites: const [],
        bookmarks: const [],
      );
      final tLocalPackage = SyncPackage(
        lastModified: tNow, // Older than remote
        settings: const ReaderSettings(appTheme: AppTheme.Light),
        progress: const [],
        favorites: const [],
        bookmarks: const [],
      );
      final tConfig = const WebDAVConfig(uri: 'http://test.com', username: 'user', password: 'password');
      final tRemoteData = Uint8List.fromList(utf8.encode(jsonEncode(tRemotePackage.toJson())));

      // 1. Setup WebDAV config
      when(mockSettingsRepository.getWebDAVConfig()).thenAnswer((_) async => tConfig);

      // 2. Mock remote fetch
      when(mockWebDavService.restore(config: anyNamed('config'), fileName: anyNamed('fileName')))
          .thenAnswer((_) async => Right(tRemoteData));

      // 3. Mock local data fetching
      when(mockSettingsRepository.getReaderSettings()).thenAnswer((_) async => Right(tLocalPackage.settings));
      when(mockComicRepository.getAllProgress()).thenAnswer((_) async => tLocalPackage.progress);
      when(mockFavoriteRepository.getFavorites()).thenAnswer((_) async => tLocalPackage.favorites);
      when(mockBookmarkRepository.getAllBookmarks()).thenAnswer((_) async => tLocalPackage.bookmarks);

      // 4. Mock upload success
      when(mockWebDavService.backup(config: anyNamed('config'), fileName: anyNamed('fileName'), data: anyNamed('data')))
          .thenAnswer((_) async => const Right(unit));

      // 5. Mock local application
      when(mockSettingsRepository.saveReaderSettings(any)).thenAnswer((_) async => const Right(unit));
      when(mockComicRepository.applySyncChanges(any)).thenAnswer((_) async => const Right(unit));
      when(mockFavoriteRepository.clearAndInsertFavorites(any)).thenAnswer((_) async => const Right(unit));
      when(mockBookmarkRepository.clearAndInsertBookmarks(any)).thenAnswer((_) async => const Right(unit));

      // Act
      final result = await syncEngine.performSync();

      // Assert
      expect(result, const Right(unit));
      
      // Verify that the merged package (which should be the remote one) is applied locally
      final verificationResult = verify(mockSettingsRepository.saveReaderSettings(captureAny));
      expect(verificationResult.captured.single.appTheme, AppTheme.Dark);
      
      verify(mockWebDavService.backup(config: tConfig, fileName: 'easy_comic_sync.json', data: anyNamed('data'))).called(1);
    });
    test('should merge (preferring local) and upload local package when local data is newer', () async {
      // Arrange
      final tNow = DateTime.now();
      final tLocalPackage = SyncPackage(
        lastModified: tNow, // Newer than remote
        settings: const ReaderSettings(appTheme: AppTheme.Light), // Different from remote
        progress: const [],
        favorites: const [],
        bookmarks: const [],
      );
      final tRemotePackage = SyncPackage(
        lastModified: tNow.subtract(const Duration(hours: 1)), // Older than local
        settings: const ReaderSettings(appTheme: AppTheme.Dark),
        progress: const [],
        favorites: const [],
        bookmarks: const [],
      );
      final tConfig = const WebDAVConfig(uri: 'http://test.com', username: 'user', password: 'password');
      final tRemoteData = Uint8List.fromList(utf8.encode(jsonEncode(tRemotePackage.toJson())));

      // 1. Setup WebDAV config
      when(mockSettingsRepository.getWebDAVConfig()).thenAnswer((_) async => tConfig);

      // 2. Mock remote fetch
      when(mockWebDavService.restore(config: anyNamed('config'), fileName: anyNamed('fileName')))
          .thenAnswer((_) async => Right(tRemoteData));

      // 3. Mock local data fetching
      when(mockSettingsRepository.getReaderSettings()).thenAnswer((_) async => Right(tLocalPackage.settings));
      when(mockComicRepository.getAllProgress()).thenAnswer((_) async => tLocalPackage.progress);
      when(mockFavoriteRepository.getFavorites()).thenAnswer((_) async => tLocalPackage.favorites);
      when(mockBookmarkRepository.getAllBookmarks()).thenAnswer((_) async => tLocalPackage.bookmarks);

      // 4. Mock upload success
      when(mockWebDavService.backup(config: anyNamed('config'), fileName: anyNamed('fileName'), data: anyNamed('data')))
          .thenAnswer((_) async => const Right(unit));

      // 5. Mock local application
      when(mockSettingsRepository.saveReaderSettings(any)).thenAnswer((_) async => const Right(unit));
      when(mockComicRepository.applySyncChanges(any)).thenAnswer((_) async => const Right(unit));
      when(mockFavoriteRepository.clearAndInsertFavorites(any)).thenAnswer((_) async => const Right(unit));
      when(mockBookmarkRepository.clearAndInsertBookmarks(any)).thenAnswer((_) async => const Right(unit));

      // Act
      final result = await syncEngine.performSync();

      // Assert
      expect(result, const Right(unit));
      
      // Verify that the uploaded package is the local one
      final backupVerification = verify(mockWebDavService.backup(config: tConfig, fileName: 'easy_comic_sync.json', data: captureAnyNamed('data')));
      final uploadedData = backupVerification.captured.single as Uint8List;
      final uploadedPackage = SyncPackage.fromJson(jsonDecode(utf8.decode(uploadedData)));
      
      // The merged package will have a new timestamp, so we only compare the content
      expect(uploadedPackage.settings.appTheme, AppTheme.Light);
    });
    test('should return a Failure when downloading from server fails', () async {
      // Arrange
      final tConfig = const WebDAVConfig(uri: 'http://test.com', username: 'user', password: 'password');
      final tFailure = const ServerFailure('Network Error');

      // 1. Setup WebDAV config
      when(mockSettingsRepository.getWebDAVConfig()).thenAnswer((_) async => tConfig);

      // 2. Mock remote fetch to throw an error
      when(mockWebDavService.restore(config: anyNamed('config'), fileName: anyNamed('fileName')))
          .thenAnswer((_) async => Left(tFailure));

      // Act
      final result = await syncEngine.performSync();

      // Assert
      expect(result, Left(tFailure));
      verifyNever(mockWebDavService.backup(config: anyNamed('config'), fileName: anyNamed('fileName'), data: anyNamed('data')));
    });

    test('should return a Failure when uploading to server fails', () async {
      // Arrange
      final tConfig = const WebDAVConfig(uri: 'http://test.com', username: 'user', password: 'password');
      final tFailure = const ServerFailure('Network Error');

      // 1. Setup WebDAV config
      when(mockSettingsRepository.getWebDAVConfig()).thenAnswer((_) async => tConfig);

      // 2. Mock remote fetch to simulate not found (to proceed to upload)
      when(mockWebDavService.restore(config: anyNamed('config'), fileName: anyNamed('fileName')))
          .thenAnswer((_) async => Left(const NotFoundFailure('File not found')));

      // 3. Mock local data fetching
      when(mockSettingsRepository.getReaderSettings()).thenAnswer((_) async => const Right(ReaderSettings()));
      when(mockComicRepository.getAllProgress()).thenAnswer((_) async => []);
      when(mockFavoriteRepository.getFavorites()).thenAnswer((_) async => []);
      when(mockBookmarkRepository.getAllBookmarks()).thenAnswer((_) async => []);

      // 4. Mock upload to throw an error
      when(mockWebDavService.backup(config: anyNamed('config'), fileName: anyNamed('fileName'), data: anyNamed('data')))
          .thenAnswer((_) async => Left(tFailure));

      // Act
      final result = await syncEngine.performSync();

      // Assert
      expect(result, Left(tFailure));
      verifyNever(mockSettingsRepository.saveReaderSettings(any));
    });
  });

  group('buildLocalSyncPackage', () {
    test('should correctly build a sync package from local repositories', () async {
      // Arrange
      final tSettings = const ReaderSettings(appTheme: AppTheme.Dark);
      final tProgress = [ComicProgress(id: '1', comicId: '1', currentPage: 1, totalPages: 100, lastUpdated: DateTime.now(), isCompleted: false, syncStatus: const SyncStatus.pending(), readingTimeSeconds: 0, metadata: {})];
      final tFavorites = [Favorite(id: 1, name: 'fav1', createTime: DateTime.now())];
      final tBookmarks = [Bookmark(comicId: '1', pageIndex: 10, createdAt: DateTime.now())];

      when(mockSettingsRepository.getReaderSettings()).thenAnswer((_) async => Right(tSettings));
      when(mockComicRepository.getAllProgress()).thenAnswer((_) async => tProgress);
      when(mockFavoriteRepository.getFavorites()).thenAnswer((_) async => tFavorites);
      when(mockBookmarkRepository.getAllBookmarks()).thenAnswer((_) async => tBookmarks);

      // Act
      final result = await syncEngine.buildLocalSyncPackage();

      // Assert
      expect(result.settings, tSettings);
      expect(result.progress, tProgress);
      expect(result.favorites, tFavorites);
      expect(result.bookmarks, tBookmarks);
      expect(result.lastModified, isA<DateTime>());
    });
  });

  group('applySyncPackageLocally', () {
    test('should correctly call repository methods to apply a sync package', () async {
      // This test indirectly tests the private _applyMergedPackage method
      // by running a successful sync cycle.

      // Arrange
      final tPackage = SyncPackage(
        lastModified: DateTime.now(),
        settings: const ReaderSettings(appTheme: AppTheme.Dark),
        progress: [ComicProgress(id: '1', comicId: '1', currentPage: 1, totalPages: 100, lastUpdated: DateTime.now(), isCompleted: false, syncStatus: const SyncStatus.pending(), readingTimeSeconds: 0, metadata: {})],
        favorites: [Favorite(id: 1, name: 'fav1', createTime: DateTime.now())],
        bookmarks: [Bookmark(comicId: '1', pageIndex: 10, createdAt: DateTime.now())],
      );
      final tConfig = const WebDAVConfig(uri: 'http://test.com', username: 'user', password: 'password');
      
      // Setup a sync cycle that results in applying tPackage
      when(mockSettingsRepository.getWebDAVConfig()).thenAnswer((_) async => tConfig);
      when(mockWebDavService.restore(config: anyNamed('config'), fileName: anyNamed('fileName')))
          .thenAnswer((_) async => Left(const NotFoundFailure('File not found')));
      when(mockSettingsRepository.getReaderSettings()).thenAnswer((_) async => Right(tPackage.settings));
      when(mockComicRepository.getAllProgress()).thenAnswer((_) async => tPackage.progress);
      when(mockFavoriteRepository.getFavorites()).thenAnswer((_) async => tPackage.favorites);
      when(mockBookmarkRepository.getAllBookmarks()).thenAnswer((_) async => tPackage.bookmarks);
      when(mockWebDavService.backup(config: anyNamed('config'), fileName: anyNamed('fileName'), data: anyNamed('data')))
          .thenAnswer((_) async => const Right(unit));
          
      // Mock the application methods that we want to verify
      when(mockSettingsRepository.saveReaderSettings(any)).thenAnswer((_) async => const Right(unit));
      when(mockComicRepository.applySyncChanges(any)).thenAnswer((_) async => const Right(unit));
      when(mockFavoriteRepository.clearAndInsertFavorites(any)).thenAnswer((_) async => const Right(unit));
      when(mockBookmarkRepository.clearAndInsertBookmarks(any)).thenAnswer((_) async => const Right(unit));

      // Act
      await syncEngine.performSync();

      // Assert
      verify(mockSettingsRepository.saveReaderSettings(tPackage.settings)).called(1);
      verify(mockComicRepository.applySyncChanges(tPackage.progress)).called(1);
      verify(mockFavoriteRepository.clearAndInsertFavorites(tPackage.favorites)).called(1);
      verify(mockBookmarkRepository.clearAndInsertBookmarks(tPackage.bookmarks)).called(1);
    });
  });
}