import 'package:equatable/equatable.dart';

enum WebDAVOperation { backup, restore }

abstract class WebDAVState extends Equatable {
  const WebDAVState();

  @override
  List<Object> get props => [];
}

class WebDAVInitial extends WebDAVState {}

class WebDAVInProgress extends WebDAVState {
  final WebDAVOperation operation;

  const WebDAVInProgress(this.operation);

  @override
  List<Object> get props => [operation];
}

class WebDAVSuccess extends WebDAVState {
  final String message;

  const WebDAVSuccess(this.message);

  @override
  List<Object> get props => [message];
}

class WebDAVFailure extends WebDAVState {
  final String message;

  const WebDAVFailure(this.message);

  @override
  List<Object> get props => [message];
}