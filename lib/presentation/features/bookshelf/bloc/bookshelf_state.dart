// lib/presentation/features/bookshelf/bloc/bookshelf_state.dart

import 'package:easy_comic/domain/entities/comic.dart';
import 'package:easy_comic/domain/repositories/comic_repository.dart';
import 'package:equatable/equatable.dart';

abstract class BookshelfState extends Equatable {
  const BookshelfState();

  @override
  List<Object?> get props => [];
}

class BookshelfInitial extends BookshelfState {}

class BookshelfLoading extends BookshelfState {}

class BookshelfLoaded extends BookshelfState {
  final List<Comic> comics;
  final bool hasReachedMax;
  final bool isSearching;
  final String searchQuery;
  final SortType? currentSortType;
  final List<Comic>? originalComics; // 用于搜索重置

  const BookshelfLoaded({
    required this.comics,
    this.hasReachedMax = false,
    this.isSearching = false,
    this.searchQuery = '',
    this.currentSortType,
    this.originalComics,
  });

  BookshelfLoaded copyWith({
    List<Comic>? comics,
    bool? hasReachedMax,
    bool? isSearching,
    String? searchQuery,
    SortType? currentSortType,
    List<Comic>? originalComics,
  }) {
    return BookshelfLoaded(
      comics: comics ?? this.comics,
      hasReachedMax: hasReachedMax ?? this.hasReachedMax,
      isSearching: isSearching ?? this.isSearching,
      searchQuery: searchQuery ?? this.searchQuery,
      currentSortType: currentSortType ?? this.currentSortType,
      originalComics: originalComics ?? this.originalComics,
    );
  }

  @override
  List<Object?> get props => [comics, hasReachedMax, isSearching, searchQuery, currentSortType, originalComics];
}

class BookshelfError extends BookshelfState {
  final String message;

  const BookshelfError({required this.message});

  @override
  List<Object> get props => [message];
}