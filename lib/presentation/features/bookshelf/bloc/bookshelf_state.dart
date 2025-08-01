// lib/presentation/features/bookshelf/bloc/bookshelf_state.dart

import 'package:easy_comic/domain/entities/comic.dart';
import 'package:equatable/equatable.dart';

abstract class BookshelfState extends Equatable {
  const BookshelfState();

  @override
  List<Object> get props => [];
}

class BookshelfInitial extends BookshelfState {}

class BookshelfLoading extends BookshelfState {}

class BookshelfLoaded extends BookshelfState {
  final List<Comic> comics;
  final bool hasReachedMax;

  const BookshelfLoaded({
    required this.comics,
    this.hasReachedMax = false,
  });

  BookshelfLoaded copyWith({
    List<Comic>? comics,
    bool? hasReachedMax,
  }) {
    return BookshelfLoaded(
      comics: comics ?? this.comics,
      hasReachedMax: hasReachedMax ?? this.hasReachedMax,
    );
  }

  @override
  List<Object> get props => [comics, hasReachedMax];
}

class BookshelfError extends BookshelfState {
  final String message;

  const BookshelfError({required this.message});

  @override
  List<Object> get props => [message];
}