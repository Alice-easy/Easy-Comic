import 'package:dartz/dartz.dart';
import 'package:easy_comic/core/failures/failures.dart';
import 'package:easy_comic/data/datasources/local/settings_local_data_source.dart';
import 'package:easy_comic/domain/entities/app_settings.dart';
import 'package:easy_comic/domain/repositories/settings_repository.dart';

class SettingsRepositoryImpl implements ISettingsRepository {
  final SettingsLocalDataSource localDataSource;

  SettingsRepositoryImpl({required this.localDataSource});

  @override
  Future<Either<Failure, AppSettings>> getSettings() async {
    try {
      final settings = await localDataSource.getSettings();
      return Right(settings);
    } catch (e) {
      return Left(DatabaseFailure('Failed to get settings'));
    }
  }

  @override
  Future<Either<Failure, Unit>> saveSettings(AppSettings settings) async {
    try {
      await localDataSource.saveSettings(settings);
      return Right(unit);
    } catch (e) {
      return Left(DatabaseFailure('Failed to save settings'));
    }
  }
}