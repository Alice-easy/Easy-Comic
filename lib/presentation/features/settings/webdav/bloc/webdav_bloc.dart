import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:easy_comic/domain/entities/webdav_config.dart';
import 'package:easy_comic/core/services/logging_service.dart';
import 'package:easy_comic/domain/repositories/settings_repository.dart';
import 'package:easy_comic/domain/usecases/backup_data_to_webdav_usecase.dart';
import 'package:easy_comic/domain/usecases/restore_data_from_webdav_usecase.dart';

import 'webdav_event.dart';
import 'webdav_state.dart';

class WebDAVBloc extends Bloc<WebDAVEvent, WebDAVState> {
  final BackupDataToWebdavUseCase backupDataToWebdavUseCase;
  final RestoreDataFromWebdavUseCase restoreDataFromWebdavUseCase;
  final SettingsRepository settingsRepository;
  final LoggingService loggingService;

  WebDAVBloc({
    required this.backupDataToWebdavUseCase,
    required this.restoreDataFromWebdavUseCase,
    required this.settingsRepository,
    required this.loggingService,
  }) : super(WebDAVInitial()) {
    on<BackupDataEvent>(_onBackupData);
    on<RestoreDataEvent>(_onRestoreData);
  }

  Future<void> _onBackupData(BackupDataEvent event, Emitter<WebDAVState> emit) async {
    try {
      emit(WebDAVInProgress(WebDAVOperation.backup));
      final config = await settingsRepository.getWebDAVConfig();
      final result = await backupDataToWebdavUseCase(config);
      result.fold(
        (failure) {
          final errorMessage = 'Failed to backup data: $failure';
          loggingService.error(errorMessage);
          emit(WebDAVFailure('备份失败，请检查网络和配置。'));
        },
        (_) => emit(const WebDAVSuccess('备份成功')),
      );
    } catch (e, s) {
      loggingService.error('Unhandled error in _onBackupData', e, s);
      emit(WebDAVFailure('发生未知错误。'));
    }
  }

  Future<void> _onRestoreData(RestoreDataEvent event, Emitter<WebDAVState> emit) async {
    try {
      emit(WebDAVInProgress(WebDAVOperation.restore));
      final config = await settingsRepository.getWebDAVConfig();
      final result = await restoreDataFromWebdavUseCase(config);
      result.fold(
        (failure) {
          final errorMessage = 'Failed to restore data: $failure';
          loggingService.error(errorMessage);
          emit(WebDAVFailure('恢复失败，请检查网络和配置。'));
        },
        (_) => emit(const WebDAVSuccess('恢复成功')),
      );
    } catch (e, s) {
      loggingService.error('Unhandled error in _onRestoreData', e, s);
      emit(WebDAVFailure('发生未知错误。'));
    }
  }
}