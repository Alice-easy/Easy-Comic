part of 'favorite_comics_bloc.dart';

abstract class FavoriteComicsState extends Equatable {
  const FavoriteComicsState();

  @override
  List<Object?> get props => [];
}

class FavoriteComicsInitial extends FavoriteComicsState {}

class FavoriteComicsLoading extends FavoriteComicsState {}

class FavoriteComicsLoaded extends FavoriteComicsState {
  final int favoriteId;
  final List<Comic> allComics;
  final List<Comic> displayedComics;
  final String searchQuery;
  final FavoriteComicsSortType currentSortType;
  final Set<String> selectedComics;
  final bool isSelectionMode;

  const FavoriteComicsLoaded({
    required this.favoriteId,
    required this.allComics,
    required this.displayedComics,
    required this.searchQuery,
    required this.currentSortType,
    required this.selectedComics,
    required this.isSelectionMode,
  });

  FavoriteComicsLoaded copyWith({
    int? favoriteId,
    List<Comic>? allComics,
    List<Comic>? displayedComics,
    String? searchQuery,
    FavoriteComicsSortType? currentSortType,
    Set<String>? selectedComics,
    bool? isSelectionMode,
  }) {
    return FavoriteComicsLoaded(
      favoriteId: favoriteId ?? this.favoriteId,
      allComics: allComics ?? this.allComics,
      displayedComics: displayedComics ?? this.displayedComics,
      searchQuery: searchQuery ?? this.searchQuery,
      currentSortType: currentSortType ?? this.currentSortType,
      selectedComics: selectedComics ?? this.selectedComics,
      isSelectionMode: isSelectionMode ?? this.isSelectionMode,
    );
  }

  @override
  List<Object?> get props => [
        favoriteId,
        allComics,
        displayedComics,
        searchQuery,
        currentSortType,
        selectedComics,
        isSelectionMode,
      ];
}

class FavoriteComicsError extends FavoriteComicsState {
  final String message;

  const FavoriteComicsError(this.message);

  @override
  List<Object> get props => [message];
}