import 'dart:convert';

import 'package:easy_comic/domain/entities/reader_settings.dart';
import 'package:easy_comic/domain/entities/webdav_config.dart';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

abstract class SettingsLocalDataSource {
  Future<ReaderSettings> getReaderSettings();
  Future<void> saveReaderSettings(ReaderSettings settings);
  Future<WebDAVConfig> getWebDAVConfig();
  Future<void> saveWebDAVConfig(WebDAVConfig config);
  Future<ThemeMode> getThemeMode();
  Future<void> saveThemeMode(ThemeMode mode);
  Future<double> getBrightness();
  Future<void> saveBrightness(double brightness);
}

class SettingsLocalDataSourceImpl implements SettingsLocalDataSource {
  final SharedPreferences _prefs;
  static const String _settingsKey = 'reader_settings';
  static const String _webDAVConfigKey = 'webdav_config';
  static const String _themeModeKey = 'theme_mode';
  static const String _brightnessKey = 'brightness';

  SettingsLocalDataSourceImpl({required SharedPreferences prefs}) : _prefs = prefs;

  @override
  Future<ReaderSettings> getReaderSettings() async {
    final jsonString = _prefs.getString(_settingsKey);
    if (jsonString != null) {
      return ReaderSettings.fromJson(jsonDecode(jsonString));
    } else {
      // Return default settings if none are saved
      return const ReaderSettings();
    }
  }

  @override
  Future<void> saveReaderSettings(ReaderSettings settings) async {
    final jsonString = jsonEncode(settings.toJson());
    await _prefs.setString(_settingsKey, jsonString);
  }

  @override
  Future<WebDAVConfig> getWebDAVConfig() async {
    final jsonString = _prefs.getString(_webDAVConfigKey);
    if (jsonString != null) {
      return WebDAVConfig.fromJson(jsonDecode(jsonString));
    } else {
      return const WebDAVConfig(uri: '', username: '', password: '');
    }
  }

  @override
  Future<void> saveWebDAVConfig(WebDAVConfig config) async {
    final jsonString = jsonEncode(config.toJson());
    await _prefs.setString(_webDAVConfigKey, jsonString);
  }

  @override
  Future<ThemeMode> getThemeMode() async {
    final themeString = _prefs.getString(_themeModeKey);
    switch (themeString) {
      case 'dark':
        return ThemeMode.dark;
      case 'light':
        return ThemeMode.light;
      default:
        return ThemeMode.system;
    }
  }

  @override
  Future<void> saveThemeMode(ThemeMode mode) async {
    await _prefs.setString(_themeModeKey, mode.name);
  }

  @override
  Future<double> getBrightness() async {
    return _prefs.getDouble(_brightnessKey) ?? 0.5;
  }

  @override
  Future<void> saveBrightness(double brightness) async {
    await _prefs.setDouble(_brightnessKey, brightness);
  }
}