part of 'favorites_bloc.dart';

abstract class FavoritesEvent extends Equatable {
  const FavoritesEvent();

  @override
  List<Object> get props => [];
}

class LoadFavorites extends FavoritesEvent {}

class CreateFavorite extends FavoritesEvent {
  final String name;

  const CreateFavorite(this.name);

  @override
  List<Object> get props => [name];
}

class DeleteFavorite extends FavoritesEvent {
  final int id;

  const DeleteFavorite(this.id);

  @override
  List<Object> get props => [id];
}

class AddComicToFavorite extends FavoritesEvent {
  final int favoriteId;
  final String comicId;

  const AddComicToFavorite(this.favoriteId, this.comicId);

  @override
  List<Object> get props => [favoriteId, comicId];
}

class RemoveComicFromFavorite extends FavoritesEvent {
  final int favoriteId;
  final String comicId;

  const RemoveComicFromFavorite(this.favoriteId, this.comicId);

  @override
  List<Object> get props => [favoriteId, comicId];
}