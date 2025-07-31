// lib/data/datasources/local/settings_local_datasource.dart
import 'package:easy_comic/core/services/settings_service.dart';

abstract class ISettingsLocalDataSource {
  Future<ReadingMode> getReadingMode();
  Future<void> setReadingMode(ReadingMode mode);

  Future<ReadingDirection> getReadingDirection();
  Future<void> setReadingDirection(ReadingDirection direction);

  Future<AppTheme> getAppTheme();
  Future<void> setAppTheme(AppTheme theme);

  Future<int> getAutoPageInterval();
  Future<void> setAutoPageInterval(int interval);
}

class SettingsLocalDataSource implements ISettingsLocalDataSource {
  final SettingsService _settingsService;

  SettingsLocalDataSource({required SettingsService settingsService})
      : _settingsService = settingsService;

  @override
  Future<ReadingMode> getReadingMode() => _settingsService.getReadingMode();

  @override
  Future<void> setReadingMode(ReadingMode mode) =>
      _settingsService.setReadingMode(mode);

  @override
  Future<ReadingDirection> getReadingDirection() =>
      _settingsService.getReadingDirection();

  @override
  Future<void> setReadingDirection(ReadingDirection direction) =>
      _settingsService.setReadingDirection(direction);

  @override
  Future<AppTheme> getAppTheme() => _settingsService.getAppTheme();

  @override
  Future<void> setAppTheme(AppTheme theme) => _settingsService.setAppTheme(theme);

  @override
  Future<int> getAutoPageInterval() => _settingsService.getAutoPageInterval();

  @override
  Future<void> setAutoPageInterval(int interval) =>
      _settingsService.setAutoPageInterval(interval);
}