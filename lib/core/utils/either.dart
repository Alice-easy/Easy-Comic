import 'package:equatable/equatable.dart';
import 'package:easy_comic/core/error/failures.dart';

abstract class Either<L, R> extends Equatable {
  const Either();

  B fold<B>(B Function(L l) ifLeft, B Function(R r) ifRight);

  @override
  List<Object> get props => [];
}

class Left<L, R> extends Either<L, R> {
  final L value;

  const Left(this.value);

  @override
  B fold<B>(B Function(L l) ifLeft, B Function(R r) ifRight) => ifLeft(value);

  @override
  List<Object> get props => [value as Object];
}

class Right<L, R> extends Either<L, R> {
  final R value;

  const Right(this.value);

  @override
  B fold<B>(B Function(L l) ifLeft, B Function(R r) ifRight) => ifRight(value);

  @override
  List<Object> get props => [value as Object];
}