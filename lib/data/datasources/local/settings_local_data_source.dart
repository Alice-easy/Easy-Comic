import 'package:easy_comic/domain/entities/app_settings.dart';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

abstract class SettingsLocalDataSource {
  Future<AppSettings> getSettings();
  Future<void> saveSettings(AppSettings settings);
}

const THEME_MODE = 'THEME_MODE';

class SettingsLocalDataSourceImpl implements SettingsLocalDataSource {
  final SharedPreferences sharedPreferences;

  SettingsLocalDataSourceImpl({required this.sharedPreferences});

  @override
  Future<AppSettings> getSettings() {
    final themeModeString = sharedPreferences.getString(THEME_MODE);
    
    ThemeMode themeMode;
    if (themeModeString == 'ThemeMode.light') {
      themeMode = ThemeMode.light;
    } else if (themeModeString == 'ThemeMode.dark') {
      themeMode = ThemeMode.dark;
    } else {
      themeMode = ThemeMode.system;
    }

    // In a real app, you would load all settings.
    // For this task, we only focus on themeMode.
    return Future.value(AppSettings(themeMode: themeMode));
  }

  @override
  Future<void> saveSettings(AppSettings settings) {
    // In a real app, you would save all settings.
    // For this task, we only focus on themeMode.
    return sharedPreferences.setString(THEME_MODE, settings.themeMode.toString());
  }
}