// lib/domain/repositories/settings_repository.dart
import 'package:easy_comic/core/error/failures.dart';
import 'package:easy_comic/core/utils/either.dart';
import 'package:easy_comic/domain/entities/reader_settings.dart';

abstract class SettingsRepository {
  Future<Either<Failure, ReaderSettings>> getReaderSettings();
  Future<Either<Failure, void>> saveReaderSettings(ReaderSettings settings);
}