import 'package:equatable/equatable.dart';

abstract class WebDAVEvent extends Equatable {
  const WebDAVEvent();

  @override
  List<Object> get props => [];
}

class BackupDataEvent extends WebDAVEvent {}

class RestoreDataEvent extends WebDAVEvent {}