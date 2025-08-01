part of 'favorites_bloc.dart';

abstract class FavoritesState extends Equatable {
  const FavoritesState();

  @override
  List<Object?> get props => [];
}

class FavoritesInitial extends FavoritesState {}

class FavoritesLoading extends FavoritesState {}

class FavoritesLoaded extends FavoritesState {
  final List<Favorite> favorites;
  final FavoriteSortType currentSortType;
  final Map<int, int> comicCounts; // 收藏夹ID -> 漫画数量

  const FavoritesLoaded(
    this.favorites, {
    this.currentSortType = FavoriteSortType.createTimeDesc,
    this.comicCounts = const {},
  });

  FavoritesLoaded copyWith({
    List<Favorite>? favorites,
    FavoriteSortType? currentSortType,
    Map<int, int>? comicCounts,
  }) {
    return FavoritesLoaded(
      favorites ?? this.favorites,
      currentSortType: currentSortType ?? this.currentSortType,
      comicCounts: comicCounts ?? this.comicCounts,
    );
  }

  @override
  List<Object> get props => [favorites, currentSortType, comicCounts];
}

class FavoritesError extends FavoritesState {
  final String message;

  const FavoritesError(this.message);

  @override
  List<Object> get props => [message];
}

class FavoriteNameValidation extends FavoritesState {
  final bool isValid;
  final String? errorMessage;
  final String validatedName;

  const FavoriteNameValidation({
    required this.isValid,
    this.errorMessage,
    required this.validatedName,
  });

  @override
  List<Object?> get props => [isValid, errorMessage, validatedName];
}

class FavoriteOperationSuccess extends FavoritesState {
  final String message;
  final FavoriteOperationType operationType;

  const FavoriteOperationSuccess({
    required this.message,
    required this.operationType,
  });

  @override
  List<Object> get props => [message, operationType];
}

enum FavoriteOperationType {
  create,
  rename,
  delete,
  addComic,
  removeComic,
}