part of 'favorites_bloc.dart';

abstract class FavoritesEvent extends Equatable {
  const FavoritesEvent();

  @override
  List<Object?> get props => [];
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

class RenameFavorite extends FavoritesEvent {
  final int id;
  final String newName;

  const RenameFavorite(this.id, this.newName);

  @override
  List<Object> get props => [id, newName];
}

class SortFavorites extends FavoritesEvent {
  final FavoriteSortType sortType;

  const SortFavorites(this.sortType);

  @override
  List<Object> get props => [sortType];
}

class ValidateFavoriteName extends FavoritesEvent {
  final String name;
  final int? excludeId; // 排除指定ID，用于重命名时的验证

  const ValidateFavoriteName(this.name, {this.excludeId});

  @override
  List<Object?> get props => [name, excludeId];
}

enum FavoriteSortType {
  nameAsc,
  nameDesc,
  createTimeAsc,
  createTimeDesc,
}