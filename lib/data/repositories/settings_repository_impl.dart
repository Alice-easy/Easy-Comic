// lib/data/repositories/settings_repository_impl.dart
import 'package:easy_comic/core/services/settings_service.dart';
import 'package:easy_comic/domain/repositories/settings_repository.dart';
import 'package:easy_comic/data/datasources/local/settings_local_datasource.dart';

import 'package:easy_comic/core/error/failures.dart';
import 'package:easy_comic/core/utils/either.dart';
import 'package:easy_comic/domain/entities/reader_settings.dart';

class SettingsRepositoryImpl implements SettingsRepository {
  final ISettingsLocalDataSource localDataSource;

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
}