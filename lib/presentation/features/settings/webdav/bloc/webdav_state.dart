import 'package:equatable/equatable.dart';

abstract class WebDAVState extends Equatable {
  const WebDAVState();

  @override
  List<Object> get props => [];
}

class WebDAVInitial extends WebDAVState {}

class WebDAVInProgress extends WebDAVState {}

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