// lib/presentation/features/bookshelf/bloc/bookshelf_event.dart

import 'package:easy_comic/domain/usecases/sort_bookshelf_comics_usecase.dart';
import 'package:easy_comic/domain/repositories/comic_repository.dart';
import 'package:equatable/equatable.dart';

abstract class BookshelfEvent extends Equatable {
  const BookshelfEvent();

  @override
  List<Object> get props => [];
}

class LoadBookshelf extends BookshelfEvent {
  final String bookshelfId;

  const LoadBookshelf(this.bookshelfId);

  @override
  List<Object> get props => [bookshelfId];
}

class SearchComics extends BookshelfEvent {
  final String query;

  const SearchComics(this.query);

  @override
  List<Object> get props => [query];
}

class SortComics extends BookshelfEvent {
  final SortType sortType;

  const SortComics(this.sortType);

  @override
  List<Object> get props => [sortType];
}

class ImportComicEvent extends BookshelfEvent {
  final String bookshelfId;

  const ImportComicEvent(this.bookshelfId);

  @override
  List<Object> get props => [bookshelfId];
}

class LoadMoreComics extends BookshelfEvent {
  const LoadMoreComics();
}