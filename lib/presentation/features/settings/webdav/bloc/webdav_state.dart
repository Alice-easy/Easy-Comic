import 'package:equatable/equatable.dart';

enum WebDAVOperation { backup, restore, login, logout, sync }
enum WebDavStatus { loggedIn, loggedOut, syncing, syncSuccess, syncFailure }

abstract class WebDAVState extends Equatable {
  final WebDavStatus status;
  final String? username;
  final String? avatarPath;
  final DateTime? lastSyncTime;
  final String? errorMessage;

  const WebDAVState({
    this.status = WebDavStatus.loggedOut,
    this.username,
    this.avatarPath,
    this.lastSyncTime,
    this.errorMessage,
  });

  @override
  List<Object?> get props => [status, username, avatarPath, lastSyncTime, errorMessage];
}

class WebDAVInitial extends WebDAVState {}

class WebDAVAuthInProgress extends WebDAVState {
  final WebDAVOperation operation;

  const WebDAVAuthInProgress(this.operation) : super(status: WebDavStatus.loggedOut);

  @override
  List<Object> get props => [operation];
}

class WebDAVSyncInProgress extends WebDAVState {
    const WebDAVSyncInProgress({String? username, String? avatarPath, DateTime? lastSyncTime})
      : super(status: WebDavStatus.syncing, username: username, avatarPath: avatarPath, lastSyncTime: lastSyncTime);
}

class WebDAVSuccess extends WebDAVState {
  final String message;

  const WebDAVSuccess(this.message, {WebDavStatus status = WebDavStatus.loggedOut, String? username, String? avatarPath})
      : super(status: status, username: username, avatarPath: avatarPath);

  @override
  List<Object?> get props => [message, status, username, avatarPath];
}

class WebDAVFailure extends WebDAVState {
  const WebDAVFailure({
    required String message,
    WebDavStatus status = WebDavStatus.loggedOut,
    String? username,
    String? avatarPath,
    DateTime? lastSyncTime,
  }) : super(
          status: status,
          username: username,
          avatarPath: avatarPath,
          lastSyncTime: lastSyncTime,
          errorMessage: message,
        );
}

class WebDAVLoggedIn extends WebDAVState {
  const WebDAVLoggedIn({required String username, String? avatarPath, DateTime? lastSyncTime})
      : super(status: WebDavStatus.loggedIn, username: username, avatarPath: avatarPath, lastSyncTime: lastSyncTime);
}

class WebDAVLoggedOut extends WebDAVState {
  const WebDAVLoggedOut() : super(status: WebDavStatus.loggedOut);
}