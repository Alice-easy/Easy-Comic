import 'dart:io';

import 'package:bloc/bloc.dart';
import 'package:easy_comic/domain/entities/webdav_config.dart';
import 'package:easy_comic/domain/usecases/webdav/download_avatar_usecase.dart';
import 'package:easy_comic/domain/usecases/webdav/login_to_webdav_usecase.dart';
import 'package:easy_comic/domain/usecases/webdav/sync_data_usecase.dart';
import 'package:easy_comic/domain/usecases/webdav/upload_avatar_usecase.dart';
import 'package:equatable/equatable.dart';

part 'webdav_event.dart';
part 'webdav_state.dart';

class WebdavBloc extends Bloc<WebdavEvent, WebdavState> {
  final LoginToWebdavUseCase loginToWebdavUseCase;
  final UploadAvatarUseCase uploadAvatarUseCase;
  final DownloadAvatarUseCase downloadAvatarUseCase;
  final SyncDataUseCase syncDataUseCase;

  WebdavBloc({
    required this.loginToWebdavUseCase,
    required this.uploadAvatarUseCase,
    required this.downloadAvatarUseCase,
    required this.syncDataUseCase,
  }) : super(const WebdavState()) {
    on<LoginButtonPressed>(_onLoginButtonPressed);
    on<LogoutButtonPressed>(_onLogoutButtonPressed);
    on<SyncNow>(_onSyncNow);
    on<UploadAvatarButtonPressed>(_onUploadAvatarButtonPressed);
    on<DownloadAvatar>(_onDownloadAvatar);
  }

  Future<void> _onLoginButtonPressed(
    LoginButtonPressed event,
    Emitter<WebdavState> emit,
  ) async {
    emit(state.copyWith(status: WebdavStatus.loading));
    final config = WebDAVConfig(
      serverUrl: event.serverUrl,
      username: event.username,
      password: event.password,
      autoSync: false, // Default value
      avatarPath: '', // Default value
    );
    final result = await loginToWebdavUseCase(config);
    result.fold(
      (failure) => emit(state.copyWith(status: WebdavStatus.failure, failureMessage: failure.message)),
      (_) => emit(state.copyWith(status: WebdavStatus.success, config: config)),
    );
  }

  Future<void> _onLogoutButtonPressed(
    LogoutButtonPressed event,
    Emitter<WebdavState> emit,
  ) async {
    emit(const WebdavState());
  }

  Future<void> _onSyncNow(
    SyncNow event,
    Emitter<WebdavState> emit,
  ) async {
    if (state.config == null) return;
    emit(state.copyWith(status: WebdavStatus.syncing));
    final result = await syncDataUseCase(state.config!);
    result.fold(
      (failure) => emit(state.copyWith(status: WebdavStatus.failure, failureMessage: failure.message)),
      (_) => emit(state.copyWith(status: WebdavStatus.success)),
    );
  }

  Future<void> _onUploadAvatarButtonPressed(
    UploadAvatarButtonPressed event,
    Emitter<WebdavState> emit,
  ) async {
    if (state.config == null) return;
    emit(state.copyWith(status: WebdavStatus.loading));
    const remotePath = '/avatar.png';
    final result = await uploadAvatarUseCase(UploadAvatarParams(config: state.config!, file: event.avatarFile, remotePath: remotePath));
    result.fold(
      (failure) => emit(state.copyWith(status: WebdavStatus.failure, failureMessage: failure.message)),
      (_) {
        emit(state.copyWith(status: WebdavStatus.success, avatarPath: event.avatarFile.path));
      },
    );
  }

  Future<void> _onDownloadAvatar(
    DownloadAvatar event,
    Emitter<WebdavState> emit,
  ) async {
    if (state.config == null) return;
    emit(state.copyWith(status: WebdavStatus.loading));
    // TODO: Define local path properly
    const localPath = 'avatar.png';
    const remotePath = '/avatar.png';
    final result = await downloadAvatarUseCase(DownloadAvatarParams(config: state.config!, remotePath: remotePath, localPath: localPath));
    result.fold(
      (failure) => emit(state.copyWith(status: WebdavStatus.failure, failureMessage: failure.message)),
      (file) => emit(state.copyWith(status: WebdavStatus.success, avatarPath: file.path)),
    );
  }
}