// lib/domain/repositories/settings_repository.dart
import 'package:easy_comic/core/services/settings_service.dart';

abstract class SettingsRepository {
  Future<ReadingMode> getReadingMode();
  Future<void> setReadingMode(ReadingMode mode);

  Future<ReadingDirection> getReadingDirection();
  Future<void> setReadingDirection(ReadingDirection direction);

  Future<AppTheme> getAppTheme();
  Future<void> setAppTheme(AppTheme theme);

  Future<int> getAutoPageInterval();
  Future<void> setAutoPageInterval(int interval);
}