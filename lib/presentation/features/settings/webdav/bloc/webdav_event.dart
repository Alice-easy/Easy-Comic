part of 'webdav_bloc.dart';

abstract class WebdavEvent extends Equatable {
  const WebdavEvent();

  @override
  List<Object> get props => [];
}

class LoginButtonPressed extends WebdavEvent {
  final String serverUrl;
  final String username;
  final String password;

  const LoginButtonPressed({
    required this.serverUrl,
    required this.username,
    required this.password,
  });

  @override
  List<Object> get props => [serverUrl, username, password];
}

class LogoutButtonPressed extends WebdavEvent {}

class SyncNow extends WebdavEvent {}

class UploadAvatarButtonPressed extends WebdavEvent {
  final File avatarFile;

  const UploadAvatarButtonPressed({required this.avatarFile});

  @override
  List<Object> get props => [avatarFile];
}

class DownloadAvatar extends WebdavEvent {}