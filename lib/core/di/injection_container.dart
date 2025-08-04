import 'package:easy_comic/core/services/file_picker_service.dart';
import 'package:easy_comic/core/services/file_processing_service.dart';
import 'package:easy_comic/data/datasources/local/manga_local_data_source.dart';
import 'package:easy_comic/data/datasources/local/manga_local_data_source_impl.dart';
import 'package:easy_comic/data/repositories/manga_repository_impl.dart';
import 'package:easy_comic/domain/repositories/manga_repository.dart';
import 'package:easy_comic/domain/usecases/get_library_mangas_usecase.dart';
import 'package:easy_comic/domain/usecases/get_manga_details_usecase.dart';
import 'package:easy_comic/domain/usecases/import_manga_usecase.dart';
import 'package:easy_comic/presentation/features/library/bloc/library_bloc.dart';
import 'package:easy_comic/data/datasources/local/settings_local_data_source.dart';
import 'package:easy_comic/data/repositories/settings_repository_impl.dart';
import 'package:easy_comic/domain/repositories/settings_repository.dart';
import 'package:easy_comic/presentation/features/reader/bloc/reader_bloc.dart';
import 'package:easy_comic/data/repositories/webdav_repository_impl.dart';
import 'package:easy_comic/domain/repositories/webdav_repository.dart';
import 'package:easy_comic/domain/usecases/webdav/download_avatar_usecase.dart';
import 'package:easy_comic/domain/usecases/webdav/login_to_webdav_usecase.dart';
import 'package:easy_comic/domain/usecases/webdav/sync_data_usecase.dart';
import 'package:easy_comic/domain/usecases/webdav/upload_avatar_usecase.dart';
import 'package:easy_comic/presentation/features/settings/bloc/theme/theme_bloc.dart';
import 'package:easy_comic/presentation/features/settings/webdav/bloc/webdav_bloc.dart';
import 'package:get_it/get_it.dart';
import 'package:easy_comic/data/datasources/local/app_database.dart';
import 'package:shared_preferences/shared_preferences.dart';

final sl = GetIt.instance;

Future<void> init() async {
  // Features - Settings
  // BLoC
  sl.registerFactory(() => ThemeBloc(settingsRepository: sl()));
  sl.registerFactory(() => WebdavBloc(
        loginToWebdavUseCase: sl(),
        uploadAvatarUseCase: sl(),
        downloadAvatarUseCase: sl(),
        syncDataUseCase: sl(),
      ));

  // Use cases
  sl.registerLazySingleton(() => LoginToWebdavUseCase(sl()));
  sl.registerLazySingleton(() => UploadAvatarUseCase(sl()));
  sl.registerLazySingleton(() => DownloadAvatarUseCase(sl()));
  sl.registerLazySingleton(() => SyncDataUseCase(sl()));

  // Repository
  sl.registerLazySingleton<SettingsRepository>(
      () => SettingsRepositoryImpl(localDataSource: sl()));
  sl.registerLazySingleton<WebdavRepository>(() => WebdavRepositoryImpl());

  // Data sources
  sl.registerLazySingleton<SettingsLocalDataSource>(
      () => SettingsLocalDataSourceImpl(sharedPreferences: sl()));

  // Features - Library
  // BLoC
  sl.registerFactory(
    () => LibraryBloc(
      getLibraryMangas: sl(),
      importManga: sl(),
      filePickerService: sl(),
    ),
  );
  sl.registerFactory(() => ReaderBloc(
        getMangaDetailsUseCase: sl(),
        settingsRepository: sl(),
      ));

  // Use cases
  sl.registerLazySingleton(() => GetLibraryMangasUseCase(sl()));
  sl.registerLazySingleton(() => ImportMangaUseCase(sl()));
  sl.registerLazySingleton(() => GetMangaDetailsUseCase(sl()));
  sl.registerLazySingleton(() => DeleteMangaUseCase(sl()));
  sl.registerLazySingleton(() => ToggleFavoriteUseCase(sl()));
  sl.registerLazySingleton(() => DeleteMangaUseCase(sl()));

  // Repository
  sl.registerLazySingleton<MangaRepository>(
    () => MangaRepositoryImpl(
      localDataSource: sl(),
      fileProcessingService: sl(),
    ),
  );

  // Data sources
  sl.registerLazySingleton<MangaLocalDataSource>(
    () => MangaLocalDataSourceImpl(database: sl()),
  );

  // Core
  sl.registerLazySingleton<FilePickerService>(() => FilePickerServiceImpl());
  sl.registerLazySingleton<FileProcessingService>(() => FileProcessingServiceImpl());


  // External
  // Database
  sl.registerLazySingleton(() => AppDatabase());
  final sharedPreferences = await SharedPreferences.getInstance();
  sl.registerLazySingleton(() => sharedPreferences);
}