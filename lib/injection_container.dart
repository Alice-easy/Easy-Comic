// lib/injection_container.dart
import 'package:easy_comic/core/services/archive_service.dart';
import 'package:easy_comic/core/services/cache_service.dart';
import 'package:easy_comic/core/services/settings_service.dart';
import 'package:easy_comic/data/datasources/local/comic_local_datasource.dart';
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
  sl.registerFactory(() => ReaderBloc(comicRepository: sl(), settingsRepository: sl()));

  // Repositories
  sl.registerLazySingleton<ComicRepository>(
      () => ComicRepositoryImpl(archiveService: sl()));
  sl.registerLazySingleton<SettingsRepository>(
      () => SettingsRepositoryImpl(localDataSource: sl()));

  // DataSources
  // sl.registerLazySingleton<ComicLocalDataSource>(() => ComicLocalDataSourceImpl());
  // sl.registerLazySingleton<SettingsLocalDataSource>(
  //     () => SettingsLocalDataSourceImpl(settingsService: sl()));

  // Core Services
  sl.registerLazySingleton<ArchiveService>(() => ArchiveServiceImpl());
  sl.registerLazySingleton<CacheService>(() => CacheService());
  sl.registerLazySingleton<SettingsService>(() => SettingsServiceImpl(sl()));

  // External
  final sharedPreferences = await SharedPreferences.getInstance();
  sl.registerLazySingleton(() => sharedPreferences);
}