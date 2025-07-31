// lib/presentation/features/reader/bloc/reader_state.dart
import 'package:equatable/equatable.dart';
import 'package:easy_comic/domain/entities/comic.dart';

abstract class ReaderState extends Equatable {
  const ReaderState();

  @override
  List<Object> get props => [];
}

class ReaderInitial extends ReaderState {}

class ReaderLoading extends ReaderState {}

class ReaderLoaded extends ReaderState {
  final Comic comic;
  final int currentPageIndex;
  final bool isUIVisible;
  final bool isReversed;

  const ReaderLoaded({
    required this.comic,
    required this.currentPageIndex,
    this.isUIVisible = true,
    this.isReversed = false,
  });

  @override
  List<Object> get props => [comic, currentPageIndex, isUIVisible, isReversed];
}

class ReaderError extends ReaderState {
  final String message;

  const ReaderError({required this.message});

  @override
  List<Object> get props => [message];
}