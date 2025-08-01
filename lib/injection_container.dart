import 'package:easy_comic/data/datasources/local/bookshelf_local_datasource.dart';
import 'package:easy_comic/data/datasources/local/bookmark_local_datasource.dart';
import 'package:easy_comic/data/datasources/local/comic_local_datasource.dart';
import 'package:easy_comic/data/datasources/local/favorite_local_datasource.dart';
import 'package:easy_comic/data/datasources/local/settings_local_datasource.dart';
import 'package:easy_comic/data/services/webdav_service_impl.dart';
import 'package:easy_comic/domain/services/webdav_service.dart';
import 'package:easy_comic/domain/usecases/backup_data_to_webdav_usecase.dart';
import 'package:easy_comic/domain/usecases/restore_data_from_webdav_usecase.dart';
import 'package:easy_comic/core/services/cache_service.dart';
import 'package:easy_comic/presentation/features/settings/webdav/bloc/webdav_bloc.dart';
import 'package:easy_comic/core/services/logging_service.dart';
import 'package:easy_comic/core/services/error_handler_service.dart';
import 'package:easy_comic/core/services/message_service.dart';
import 'package:easy_comic/core/services/network_service.dart';
import 'package:easy_comic/core/services/global_error_handler.dart';
import 'package:easy_comic/core/services/global_state_manager.dart';
import 'package:easy_comic/core/services/real_time_notification_service.dart';
import 'package:easy_comic/core/services/data_flow_optimizer.dart';
import 'package:easy_comic/core/services/unified_cache_manager.dart';
import 'package:easy_comic/core/services/webdav_sync_state_manager.dart';
import 'package:easy_comic/core/services/offline_queue_manager.dart';
import 'package:easy_comic/data/services/archive_service_impl.dart';
import 'package:easy_comic/domain/services/archive_service.dart';
import 'package:easy_comic/data/services/auto_page_service_impl.dart';
import 'package:easy_comic/domain/services/auto_page_service.dart';
import 'package:easy_comic/presentation/features/reader/bloc/reader_bloc.dart';
import 'package:easy_comic/core/comic_archive.dart';
import 'package:easy_comic/data/drift_db.dart';
import 'package:easy_comic/data/repositories/bookshelf_repository_impl.dart';
import 'package:easy_comic/data/repositories/bookmark_repository_impl.dart';
import 'package:easy_comic/data/repositories/comic_repository_impl.dart';
import 'package:easy_comic/data/repositories/favorite_repository_impl.dart';
import 'package:easy_comic/data/repositories/settings_repository_impl.dart';
import 'package:easy_comic/domain/repositories/bookshelf_repository.dart';
import 'package:easy_comic/domain/repositories/bookmark_repository.dart';
import 'package:easy_comic/domain/repositories/comic_repository.dart';
import 'package:easy_comic/domain/repositories/favorite_repository.dart';
import 'package:easy_comic/domain/repositories/settings_repository.dart';
import 'package:easy_comic/domain/usecases/add_comic_to_favorite_usecase.dart';
import 'package:easy_comic/domain/usecases/create_favorite_usecase.dart';
import 'package:easy_comic/domain/usecases/delete_favorite_usecase.dart';
import 'package:easy_comic/domain/usecases/get_bookshelf_comics_usecase.dart';
import 'package:easy_comic/domain/usecases/get_favorite_comics_usecase.dart';
import 'package:easy_comic/domain/usecases/get_favorites_usecase.dart';
import 'package:easy_comic/domain/usecases/import_comic_from_file_usecase.dart';
import 'package:easy_comic/domain/usecases/remove_comic_from_favorite_usecase.dart';
import 'package:easy_comic/domain/usecases/search_bookshelf_comics_usecase.dart';
import 'package:easy_comic/domain/usecases/sort_bookshelf_comics_usecase.dart';
import 'package:easy_comic/presentation/features/bookshelf/bloc/bookshelf_bloc.dart';
import 'package:easy_comic/data/services/theme_service_impl.dart';
import 'package:easy_comic/domain/services/theme_service.dart';
import 'package:easy_comic/presentation/features/favorites/bloc/favorites_bloc.dart';
import 'package:easy_comic/presentation/features/favorites/bloc/favorite_comics_bloc.dart';
import 'package:easy_comic/presentation/features/settings/theme/bloc/theme_bloc.dart';
import 'package:easy_comic/presentation/features/settings/general/bloc/settings_bloc.dart';
import 'package:get_it/get_it.dart';
import 'package:shared_preferences/shared_preferences.dart';

final sl = GetIt.instance;

Future<void> init() async {
  // #region Features
  // BLoCs
  sl.registerFactory(() => BookshelfBloc(
        getBookshelfComics: sl(),
        searchBookshelfComics: sl(),
        sortBookshelfComics: sl(),
        importComicFromFile: sl(),
        loggingService: sl(),
      ));
  sl.registerFactory(() => FavoritesBloc(
        createFavoriteUseCase: sl(),
        getFavoritesUseCase: sl(),
        getFavoriteComicsUseCase: sl(),
        addComicToFavoriteUseCase: sl(),
        removeComicFromFavoriteUseCase: sl(),
        deleteFavoriteUseCase: sl(),
        loggingService: sl(),
      ));
  sl.registerFactory(() => FavoriteComicsBloc(
        getFavoriteComicsUseCase: sl(),
        removeComicFromFavoriteUseCase: sl(),
        loggingService: sl(),
      ));
  sl.registerFactory(() => WebDAVBloc(
        backupDataToWebdavUseCase: sl(),
        restoreDataFromWebdavUseCase: sl(),
        settingsRepository: sl(),
        loggingService: sl(),
      ));
  sl.registerFactory(() => ThemeBloc(themeService: sl()));
  sl.registerFactory(() => SettingsBloc(
        settingsRepository: sl(),
        cacheService: sl(),
        loggingService: sl(),
      ));
  
  // ReaderBloc - needs special dependencies
  sl.registerFactory(() => ReaderBloc(
        comicRepository: sl(),
        settingsRepository: sl(),
        bookmarkRepository: sl(),
        autoPageService: sl(),
        cacheService: sl(),
        comicArchive: sl(),
      ));
  // #endregion

  // #region Domain
  // UseCases
  sl.registerLazySingleton(() => GetBookshelfComicsUsecase(sl<ComicRepository>()));
  sl.registerLazySingleton(() => SearchBookshelfComicsUsecase(sl()));
  sl.registerLazySingleton(() => SortBookshelfComicsUsecase(sl()));
  sl.registerLazySingleton(() => ImportComicFromFileUsecase(sl(), sl()));
  sl.registerLazySingleton(() => CreateFavoriteUseCase(sl()));
  sl.registerLazySingleton(() => GetFavoritesUseCase(sl()));
  sl.registerLazySingleton(() => AddComicToFavoriteUseCase(sl()));
  sl.registerLazySingleton(() => RemoveComicFromFavoriteUseCase(sl()));
  sl.registerLazySingleton(() => DeleteFavoriteUseCase(sl()));
  sl.registerLazySingleton(() => GetFavoriteComicsUseCase(sl()));
  sl.registerLazySingleton(() => BackupDataToWebdavUseCase(
        webDAVService: sl(),
        comicRepository: sl(),
        favoriteRepository: sl(),
        settingsRepository: sl(),
      ));
  sl.registerLazySingleton(() => RestoreDataFromWebdavUseCase(
        webDAVService: sl(),
        comicRepository: sl(),
        favoriteRepository: sl(),
      ));

  // Repositories
  sl.registerLazySingleton<ComicRepository>(
      () => ComicRepositoryImpl(localDataSource: sl()));
  sl.registerLazySingleton<BookshelfRepository>(
      () => BookshelfRepositoryImpl(localDataSource: sl()));
  sl.registerLazySingleton<FavoriteRepository>(
      () => FavoriteRepositoryImpl(localDataSource: sl()));
  sl.registerLazySingleton<SettingsRepository>(
      () => SettingsRepositoryImpl(localDataSource: sl()));
  sl.registerLazySingleton<BookmarkRepository>(
      () => BookmarkRepositoryImpl(localDataSource: sl()));
  
  // Services
  sl.registerLazySingleton<WebDAVService>(() => WebDAVServiceImpl());
  sl.registerLazySingleton<ThemeService>(() => ThemeServiceImpl(settingsRepository: sl()));
  sl.registerLazySingleton<CacheService>(() => CacheService());
  sl.registerLazySingleton<ArchiveService>(() => ArchiveServiceImpl());
  sl.registerLazySingleton<AutoPageService>(() => AutoPageServiceImpl());
  sl.registerFactory(() => ComicArchive());
  // #endregion

  // #region Data
  // DataSources
  sl.registerLazySingleton<ComicLocalDataSource>(
      () => ComicLocalDataSourceImpl(db: sl()));
  sl.registerLazySingleton<BookshelfLocalDataSource>(
      () => BookshelfLocalDataSourceImpl(db: sl()));
  sl.registerLazySingleton<FavoriteLocalDataSource>(
      () => FavoriteLocalDataSourceImpl(db: sl()));
  sl.registerLazySingleton<SettingsLocalDataSource>(
      () => SettingsLocalDataSourceImpl(prefs: sl()));
  sl.registerLazySingleton<BookmarkLocalDataSource>(
      () => BookmarkLocalDataSourceImpl(database: sl()));

  // Database
  sl.registerLazySingleton<AppDatabase>(() => AppDatabase());
  sl.registerLazySingleton(() => sl<AppDatabase>().comicsDao);
  sl.registerLazySingleton(() => sl<AppDatabase>().bookshelvesDao);
  sl.registerLazySingleton(() => sl<AppDatabase>().favoritesDao);
  // #endregion

  // #region Core
  // Services
  sl.registerLazySingleton(() => LoggingService());
  sl.registerLazySingleton(() => MessageService());
  sl.registerLazySingleton(() => NetworkService(
        messageService: sl(),
        loggingService: sl(),
      ));
  sl.registerLazySingleton(() => ErrorHandlerService(
        loggingService: sl(),
        messageService: sl(),
      ));
sl.registerLazySingleton(() => OfflineQueueManager.instance);
sl.registerLazySingleton(() => DataFlowOptimizer.instance);
  sl.registerLazySingleton(() => GlobalErrorHandler.instance);
  // #endregion

  // #region External
  final sharedPreferences = await SharedPreferences.getInstance();
  sl.registerLazySingleton(() => sharedPreferences);
  // #endregion
}