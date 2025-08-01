part of 'favorite_comics_bloc.dart';

abstract class FavoriteComicsEvent extends Equatable {
  const FavoriteComicsEvent();

  @override
  List<Object> get props => [];
}

class LoadFavoriteComics extends FavoriteComicsEvent {
  final int favoriteId;

  const LoadFavoriteComics(this.favoriteId);

  @override
  List<Object> get props => [favoriteId];
}

class SearchFavoriteComics extends FavoriteComicsEvent {
  final String query;

  const SearchFavoriteComics(this.query);

  @override
  List<Object> get props => [query];
}

class SortFavoriteComics extends FavoriteComicsEvent {
  final FavoriteComicsSortType sortType;

  const SortFavoriteComics(this.sortType);

  @override
  List<Object> get props => [sortType];
}

class RemoveComicFromFavorite extends FavoriteComicsEvent {
  final String comicId;

  const RemoveComicFromFavorite(this.comicId);

  @override
  List<Object> get props => [comicId];
}

class ToggleComicSelection extends FavoriteComicsEvent {
  final String comicId;

  const ToggleComicSelection(this.comicId);

  @override
  List<Object> get props => [comicId];
}

class SelectAllComics extends FavoriteComicsEvent {}

class ClearSelection extends FavoriteComicsEvent {}

class RemoveSelectedComics extends FavoriteComicsEvent {}

class ToggleSelectionMode extends FavoriteComicsEvent {}

enum FavoriteComicsSortType {
  title,
  addTime,
  lastRead,
  progress,
}