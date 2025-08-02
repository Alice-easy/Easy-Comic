import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:easy_comic/domain/entities/webdav_config.dart';
import 'package:easy_comic/core/services/logging_service.dart';
import 'package:easy_comic/domain/repositories/settings_repository.dart';
import 'package:easy_comic/domain/usecases/backup_data_to_webdav_usecase.dart';
import 'package:easy_comic/domain/usecases/restore_data_from_webdav_usecase.dart';
import 'package:easy_comic/core/services/avatar_manager.dart';
import 'package:easy_comic/core/sync_engine.dart';

import 'webdav_event.dart';
import 'webdav_state.dart';

class WebDAVBloc extends Bloc<WebDAVEvent, WebDAVState> {
  final BackupDataToWebdavUseCase backupDataToWebdavUseCase;
  final RestoreDataFromWebdavUseCase restoreDataFromWebdavUseCase;
  final SettingsRepository settingsRepository;
  final LoggingService loggingService;
  final AvatarManager avatarManager;
  final SyncEngine syncEngine;

  WebDAVBloc({
    required this.backupDataToWebdavUseCase,
    required this.restoreDataFromWebdavUseCase,
    required this.settingsRepository,
    required this.loggingService,
    required this.avatarManager,
    required this.syncEngine,
  }) : super(WebDAVInitial()) {
    on<BackupDataEvent>(_onBackupData);
    on<RestoreDataEvent>(_onRestoreData);
    on<LoginEvent>(_onLogin);
    on<LogoutEvent>(_onLogout);
    on<UpdateAvatarEvent>(_onUpdateAvatar);
    on<SyncDataEvent>(_onSyncData);
  }

  Future<void> _onLogin(LoginEvent event, Emitter<WebDAVState> emit) async {
    emit(const WebDAVAuthInProgress(WebDAVOperation.login));
    try {
      final config = WebDAVConfig(
        uri: event.serverUrl,
        username: event.username,
        password: event.password,
      );
      // Here you would typically verify the credentials with the WebDAV server.
      // For this example, we'll assume they are correct and save them.
      await settingsRepository.saveWebDAVConfig(config);
      emit(WebDAVLoggedIn(username: event.username));
    } catch (e) {
      emit(WebDAVFailure(message: '登录失败: ${e.toString()}'));
    }
  }

  Future<void> _onLogout(LogoutEvent event, Emitter<WebDAVState> emit) async {
    emit(const WebDAVAuthInProgress(WebDAVOperation.logout));
    try {
      await settingsRepository.saveWebDAVConfig(const WebDAVConfig(uri: '', username: '', password: ''));
      emit(const WebDAVLoggedOut());
    } catch (e) {
      emit(WebDAVFailure(message: '登出失败: ${e.toString()}'));
    }
  }

  Future<void> _onUpdateAvatar(UpdateAvatarEvent event, Emitter<WebDAVState> emit) async {
    if (state is WebDAVLoggedIn) {
      final currentState = state as WebDAVLoggedIn;
      try {
        if (event.newAvatarPath.isEmpty) {
          throw Exception('Invalid avatar path');
        }
        // In a real app, you might want to upload the avatar to a server
        // and get a URL. For this example, we just use the local path.
        // The AvatarManager already saved the file locally.
        emit(WebDAVLoggedIn(username: currentState.username!, avatarPath: event.newAvatarPath, lastSyncTime: currentState.lastSyncTime));
      } catch (e) {
        // Handle potential errors during avatar update
        emit(WebDAVFailure(message: "Failed to update avatar.", status: currentState.status, username: currentState.username, avatarPath: currentState.avatarPath, lastSyncTime: currentState.lastSyncTime));
      }
    }
  }

  Future<void> _onSyncData(SyncDataEvent event, Emitter<WebDAVState> emit) async {
    final currentState = state;
    emit(WebDAVSyncInProgress(username: currentState.username, avatarPath: currentState.avatarPath, lastSyncTime: currentState.lastSyncTime));
    try {
      final result = await syncEngine.performSync();
      result.fold(
        (failure) {
          emit(WebDAVFailure(
            message: '同步失败: ${failure.toString()}',
            status: WebDavStatus.syncFailure,
            username: currentState.username,
            avatarPath: currentState.avatarPath,
            lastSyncTime: currentState.lastSyncTime,
          ));
        },
        (_) {
          emit(WebDAVLoggedIn(
            username: currentState.username!,
            avatarPath: currentState.avatarPath,
            lastSyncTime: DateTime.now(),
          ));
        },
      );
    } catch (e) {
      emit(WebDAVFailure(
        message: '同步时发生未知错误: ${e.toString()}',
        status: WebDavStatus.syncFailure,
        username: currentState.username,
        avatarPath: currentState.avatarPath,
        lastSyncTime: currentState.lastSyncTime,
      ));
    }
  }

  Future<void> _onBackupData(BackupDataEvent event, Emitter<WebDAVState> emit) async {
    try {
      emit(const WebDAVAuthInProgress(WebDAVOperation.backup));
      final config = await settingsRepository.getWebDAVConfig();
      final result = await backupDataToWebdavUseCase(config);
      result.fold(
        (failure) {
          final errorMessage = 'Failed to backup data: $failure';
          loggingService.error(errorMessage);
          emit(const WebDAVFailure(message: '备份失败，请检查网络和配置。'));
        },
        (_) => emit(const WebDAVSuccess('备份成功')),
      );
    } catch (e, s) {
      loggingService.error('Unhandled error in _onBackupData', e, s);
      emit(const WebDAVFailure(message: '发生未知错误。'));
    }
  }

  Future<void> _onRestoreData(RestoreDataEvent event, Emitter<WebDAVState> emit) async {
    try {
      emit(const WebDAVAuthInProgress(WebDAVOperation.restore));
      final config = await settingsRepository.getWebDAVConfig();
      final result = await restoreDataFromWebdavUseCase(config);
      result.fold(
        (failure) {
          final errorMessage = 'Failed to restore data: $failure';
          loggingService.error(errorMessage);
          emit(const WebDAVFailure(message: '恢复失败，请检查网络和配置。'));
        },
        (_) => emit(const WebDAVSuccess('恢复成功')),
      );
    } catch (e, s) {
      loggingService.error('Unhandled error in _onRestoreData', e, s);
      emit(const WebDAVFailure(message: '发生未知错误。'));
    }
  }
}