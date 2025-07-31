// lib/data/repositories/settings_repository_impl.dart
import 'package:easy_comic/core/services/settings_service.dart';
import 'package:easy_comic/domain/repositories/settings_repository.dart';
import 'package:easy_comic/data/datasources/local/settings_local_datasource.dart';

class SettingsRepositoryImpl implements SettingsRepository {
  final ISettingsLocalDataSource localDataSource;

  SettingsRepositoryImpl({required this.localDataSource});

  @override
  Future<ReadingMode> getReadingMode() => localDataSource.getReadingMode();

  @override
  Future<void> setReadingMode(ReadingMode mode) =>
      localDataSource.setReadingMode(mode);

  @override
  Future<ReadingDirection> getReadingDirection() =>
      localDataSource.getReadingDirection();

  @override
  Future<void> setReadingDirection(ReadingDirection direction) =>
      localDataSource.setReadingDirection(direction);

  @override
  Future<AppTheme> getAppTheme() => localDataSource.getAppTheme();

  @override
  Future<void> setAppTheme(AppTheme theme) =>
      localDataSource.setAppTheme(theme);

  @override
  Future<int> getAutoPageInterval() => localDataSource.getAutoPageInterval();

  @override
  Future<void> setAutoPageInterval(int interval) =>
      localDataSource.setAutoPageInterval(interval);
}