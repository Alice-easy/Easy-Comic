part of 'webdav_bloc.dart';

enum WebdavStatus { initial, loading, success, failure, syncing }

class WebdavState extends Equatable {
  final WebdavStatus status;
  final WebDAVConfig? config;
  final String? failureMessage;
  final String? avatarPath;

  const WebdavState({
    this.status = WebdavStatus.initial,
    this.config,
    this.failureMessage,
    this.avatarPath,
  });

  WebdavState copyWith({
    WebdavStatus? status,
    WebDAVConfig? config,
    String? failureMessage,
    String? avatarPath,
  }) {
    return WebdavState(
      status: status ?? this.status,
      config: config ?? this.config,
      failureMessage: failureMessage ?? this.failureMessage,
      avatarPath: avatarPath ?? this.avatarPath,
    );
  }

  @override
  List<Object?> get props => [status, config, failureMessage, avatarPath];
}