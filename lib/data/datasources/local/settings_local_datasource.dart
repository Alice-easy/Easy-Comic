// lib/data/datasources/local/settings_local_datasource.dart
import 'package:easy_comic/core/services/settings_service.dart';
import 'package:easy_comic/domain/entities/reader_settings.dart';

abstract class ISettingsLocalDataSource {
  Future<ReaderSettings> getReaderSettings();
  Future<void> saveReaderSettings(ReaderSettings settings);
}

class SettingsLocalDataSource implements ISettingsLocalDataSource {
  final SettingsService _settingsService;

  SettingsLocalDataSource({required SettingsService settingsService})
      : _settingsService = settingsService;

  @override
  Future<ReaderSettings> getReaderSettings() async {
    // This is a mock implementation. In a real app, you would load this from shared_preferences or a database.
    return const ReaderSettings();
  }

  @override
  Future<void> saveReaderSettings(ReaderSettings settings) async {
    // This is a mock implementation. In a real app, you would save this to shared_preferences or a database.
  }
}