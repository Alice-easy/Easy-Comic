part of 'library_bloc.dart';

abstract class LibraryEvent extends Equatable {
  const LibraryEvent();

  @override
  List<Object> get props => [];
}

class LoadLibrary extends LibraryEvent {}

class ImportManga extends LibraryEvent {}

class SearchQueryChanged extends LibraryEvent {
  final String query;

  const SearchQueryChanged(this.query);

  @override
  List<Object> get props => [query];
}

class SortOrderChanged extends LibraryEvent {
  final SortType sortType;

  const SortOrderChanged(this.sortType);

  @override
  List<Object> get props => [sortType];
}

class DeleteManga extends LibraryEvent {
  final String mangaId;

  const DeleteManga(this.mangaId);

  @override
  List<Object> get props => [mangaId];
}

class ToggleFavorite extends LibraryEvent {
  final String mangaId;

  const ToggleFavorite(this.mangaId);

  @override
  List<Object> get props => [mangaId];
}