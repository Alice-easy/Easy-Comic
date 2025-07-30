import '../models/reader_models.dart';

class SettingsRepository {
  Future<ReaderSettings> getReaderSettings() async {
    // In a future task, this will load from SharedPreferences
    return ReaderSettings();
  }

  Future<void> saveReaderSettings(ReaderSettings settings) async {
    // In a future task, this will save to SharedPreferences
  }
}