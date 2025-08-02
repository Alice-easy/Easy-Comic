import 'package:equatable/equatable.dart';

abstract class Failure extends Equatable {
  final String message;
  final List properties;

  const Failure(this.message, [this.properties = const <dynamic>[]]);

  @override
  List<Object> get props => [message, ...properties];
}

// General failures
class ServerFailure extends Failure {
  const ServerFailure(String message) : super(message);
}

class CacheFailure extends Failure {
  const CacheFailure(String message) : super(message);
}

class DatabaseFailure extends Failure {
  const DatabaseFailure(String message) : super(message);
}

class NotFoundFailure extends Failure {
  const NotFoundFailure(String message) : super(message);
}

class ConfigurationFailure extends Failure {
  const ConfigurationFailure(String message) : super(message);
}

class UnknownFailure extends Failure {
  const UnknownFailure(String message) : super(message);
}

class DatabaseException implements Exception {
  final String message;
  DatabaseException(this.message);
}

class CacheException implements Exception {
  final String message;
  CacheException(this.message);
}