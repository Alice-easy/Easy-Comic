// lib/domain/repositories/settings_repository.dart
import 'package:flutter/material.dart';
import 'package:easy_comic/core/error/failures.dart';
import 'package:dartz/dartz.dart';
import 'package:easy_comic/domain/entities/reader_settings.dart';
import 'package:easy_comic/domain/entities/webdav_config.dart';

abstract class SettingsRepository {
  Future<Either<Failure, ReaderSettings>> getReaderSettings();
  Future<Either<Failure, void>> saveReaderSettings(ReaderSettings settings);
  Future<WebDAVConfig> getWebDAVConfig();
  Future<void> saveWebDAVConfig(WebDAVConfig config);

  // Theme settings
  Future<ThemeMode> getThemeMode();
  Future<void> setThemeMode(ThemeMode mode);

  // Brightness settings
  Future<double> getBrightness();
  Future<void> setBrightness(double brightness);
}