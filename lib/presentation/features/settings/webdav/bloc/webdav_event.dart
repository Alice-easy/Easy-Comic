import 'package:equatable/equatable.dart';

abstract class WebDAVEvent extends Equatable {
  const WebDAVEvent();

  @override
  List<Object> get props => [];
}

class BackupDataEvent extends WebDAVEvent {}

class RestoreDataEvent extends WebDAVEvent {}

class LoginEvent extends WebDAVEvent {
  final String serverUrl;
  final String username;
  final String password;

  const LoginEvent({required this.serverUrl, required this.username, required this.password});

  @override
  List<Object> get props => [serverUrl, username, password];
}

class LogoutEvent extends WebDAVEvent {}

class SyncDataEvent extends WebDAVEvent {}

class UpdateAvatarEvent extends WebDAVEvent {
  final String newAvatarPath;

  const UpdateAvatarEvent(this.newAvatarPath);

  @override
  List<Object> get props => [newAvatarPath];
}