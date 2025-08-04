part of 'library_bloc.dart';

enum SortType { title, dateAdded }

abstract class LibraryState extends Equatable {
  const LibraryState();

  @override
  List<Object> get props => [];
}

class LibraryInitial extends LibraryState {}

class LibraryLoading extends LibraryState {}

class LibraryLoaded extends LibraryState {
  final List<Manga> allMangas;
  final List<Manga> displayedMangas;
  final SortType sortType;
  final String? searchQuery;

  const LibraryLoaded({
    required this.allMangas,
    required this.displayedMangas,
    this.sortType = SortType.dateAdded,
    this.searchQuery,
  });

  @override
  List<Object?> get props => [allMangas, displayedMangas, sortType, searchQuery];

  LibraryLoaded copyWith({
    List<Manga>? allMangas,
    List<Manga>? displayedMangas,
    SortType? sortType,
    String? searchQuery,
  }) {
    return LibraryLoaded(
      allMangas: allMangas ?? this.allMangas,
      displayedMangas: displayedMangas ?? this.displayedMangas,
      sortType: sortType ?? this.sortType,
      searchQuery: searchQuery ?? this.searchQuery,
    );
  }
}

class LibraryError extends LibraryState {
  final String message;

  const LibraryError({required this.message});

  @override
  List<Object> get props => [message];
}