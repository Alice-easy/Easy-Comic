// lib/data/repositories/settings_repository_impl.dart
import 'package:easy_comic/domain/entities/webdav_config.dart';
import 'package:easy_comic/domain/repositories/settings_repository.dart';
import 'package:easy_comic/data/datasources/local/settings_local_datasource.dart';
import 'package:flutter/material.dart';

import 'package:easy_comic/core/error/failures.dart';
import 'package:easy_comic/core/utils/either.dart';
import 'package:easy_comic/domain/entities/reader_settings.dart';

class SettingsRepositoryImpl implements SettingsRepository {
  final SettingsLocalDataSource localDataSource;

  SettingsRepositoryImpl({required this.localDataSource});

  @override
  Future<Either<Failure, ReaderSettings>> getReaderSettings() async {
    try {
      final settings = await localDataSource.getReaderSettings();
      return Right(settings);
    } catch (e) {
      return Left(const CacheFailure('Failed to get settings'));
    }
  }

  @override
  Future<Either<Failure, void>> saveReaderSettings(ReaderSettings settings) async {
    try {
      await localDataSource.saveReaderSettings(settings);
      return const Right(null);
    } catch (e) {
      return Left(const CacheFailure('Failed to save settings'));
    }
  }

  @override
  Future<WebDAVConfig> getWebDAVConfig() async {
    try {
      return await localDataSource.getWebDAVConfig();
    } catch (e) {
      throw CacheException('Failed to get WebDAV config');
    }
  }

  @override
  Future<void> saveWebDAVConfig(WebDAVConfig config) async {
    try {
      await localDataSource.saveWebDAVConfig(config);
    } catch (e) {
      throw CacheException('Failed to save WebDAV config');
    }
  }

  @override
  Future<ThemeMode> getThemeMode() async {
    try {
      return await localDataSource.getThemeMode();
    } catch (e) {
      throw CacheException('Failed to get theme mode');
    }
  }

  @override
  Future<void> setThemeMode(ThemeMode mode) async {
    try {
      await localDataSource.saveThemeMode(mode);
    } catch (e) {
      throw CacheException('Failed to save theme mode');
    }
  }

  @override
  Future<double> getBrightness() async {
    try {
      return await localDataSource.getBrightness();
    } catch (e) {
      throw CacheException('Failed to get brightness');
    }
  }

  @override
  Future<void> setBrightness(double brightness) async {
    try {
      await localDataSource.saveBrightness(brightness);
    } catch (e) {
      throw CacheException('Failed to save brightness');
    }
  }
}