// lib/injection_container.dart
import 'package:easy_comic/core/comic_archive.dart';
import 'package:easy_comic/core/services/archive_service.dart';
import 'package:easy_comic/data/datasources/local/bookmark_local_datasource.dart';
import 'package:easy_comic/data/repositories/bookmark_repository_impl.dart';
import 'package:easy_comic/data/services/auto_page_service_impl.dart';
import 'package:easy_comic/data/services/cache_service_impl.dart';
import 'package:easy_comic/domain/repositories/bookmark_repository.dart';
import 'package:easy_comic/domain/services/auto_page_service.dart';
import 'package:easy_comic/domain/services/cache_service.dart';
import 'package:easy_comic/core/services/settings_service.dart';
import 'package:easy_comic/data/datasources/local/settings_local_datasource.dart';
import 'package:easy_comic/data/repositories/comic_repository_impl.dart';
import 'package:easy_comic/data/repositories/settings_repository_impl.dart';
import 'package:easy_comic/domain/repositories/comic_repository.dart';
import 'package:easy_comic/domain/repositories/settings_repository.dart';
import 'package:easy_comic/presentation/features/reader/bloc/reader_bloc.dart';
import 'package:get_it/get_it.dart';
import 'package:shared_preferences/shared_preferences.dart';

final sl = GetIt.instance;

Future<void> init() async {
  // Blocs
  sl.registerFactory(() => ReaderBloc(
        comicRepository: sl(),
        settingsRepository: sl(),
        bookmarkRepository: sl(),
        autoPageService: sl(),
        cacheService: sl(),
        comicArchive: sl(),
      ));

  // Repositories
  sl.registerLazySingleton<ComicRepository>(
      () => ComicRepositoryImpl(archiveService: sl()));
  sl.registerLazySingleton<SettingsRepository>(
      () => SettingsRepositoryImpl(localDataSource: sl()));
  sl.registerLazySingleton<BookmarkRepository>(
      () => BookmarkRepositoryImpl(localDataSource: sl()));

  // DataSources
  sl.registerLazySingleton<BookmarkLocalDataSource>(
      () => BookmarkLocalDataSourceImpl(database: sl())); // Assuming Drift is used
  sl.registerLazySingleton<ISettingsLocalDataSource>(
      () => SettingsLocalDataSource(settingsService: sl()));

  // Core Services
  sl.registerLazySingleton<ArchiveService>(() => ArchiveServiceImpl());
  sl.registerLazySingleton<CacheService>(() => CacheServiceImpl());
  sl.registerLazySingleton<AutoPageService>(() => AutoPageServiceImpl());
  sl.registerLazySingleton<SettingsService>(() => SettingsServiceImpl(sl()));
  sl.registerFactory(() => ComicArchive());

  // External
  final sharedPreferences = await SharedPreferences.getInstance();
  sl.registerLazySingleton(() => sharedPreferences);
  // sl.registerLazySingleton(() => AppDatabase()); // Assuming Drift AppDatabase
}