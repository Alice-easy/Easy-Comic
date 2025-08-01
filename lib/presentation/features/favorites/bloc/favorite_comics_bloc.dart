import 'package:bloc/bloc.dart';
import 'package:easy_comic/core/services/logging_service.dart';
import 'package:easy_comic/domain/entities/comic.dart';
import 'package:easy_comic/domain/usecases/get_favorite_comics_usecase.dart';
import 'package:easy_comic/domain/usecases/remove_comic_from_favorite_usecase.dart';
import 'package:equatable/equatable.dart';

part 'favorite_comics_event.dart';
part 'favorite_comics_state.dart';

class FavoriteComicsBloc extends Bloc<FavoriteComicsEvent, FavoriteComicsState> {
  final GetFavoriteComicsUseCase getFavoriteComicsUseCase;
  final RemoveComicFromFavoriteUseCase removeComicFromFavoriteUseCase;
  final LoggingService loggingService;

  FavoriteComicsBloc({
    required this.getFavoriteComicsUseCase,
    required this.removeComicFromFavoriteUseCase,
    required this.loggingService,
  }) : super(FavoriteComicsInitial()) {
    on<LoadFavoriteComics>(_onLoadFavoriteComics);
    on<SearchFavoriteComics>(_onSearchFavoriteComics);
    on<SortFavoriteComics>(_onSortFavoriteComics);
    on<RemoveComicFromFavorite>(_onRemoveComicFromFavorite);
    on<ToggleComicSelection>(_onToggleComicSelection);
    on<SelectAllComics>(_onSelectAllComics);
    on<ClearSelection>(_onClearSelection);
    on<RemoveSelectedComics>(_onRemoveSelectedComics);
    on<ToggleSelectionMode>(_onToggleSelectionMode);
  }

  Future<void> _onLoadFavoriteComics(
      LoadFavoriteComics event, Emitter<FavoriteComicsState> emit) async {
    try {
      emit(FavoriteComicsLoading());
      final failureOrComics = await getFavoriteComicsUseCase(event.favoriteId);
      failureOrComics.fold(
        (failure) {
          final errorMessage = 'Failed to load favorite comics: $failure';
          loggingService.error(errorMessage);
          emit(FavoriteComicsError('无法加载收藏夹中的漫画。'));
        },
        (comics) {
          emit(FavoriteComicsLoaded(
            favoriteId: event.favoriteId,
            allComics: comics,
            displayedComics: comics,
            searchQuery: '',
            currentSortType: FavoriteComicsSortType.addTime,
            selectedComics: {},
            isSelectionMode: false,
          ));
        },
      );
    } catch (e, s) {
      loggingService.error('Unhandled error in _onLoadFavoriteComics', e, s);
      emit(FavoriteComicsError('发生未知错误。'));
    }
  }

  Future<void> _onSearchFavoriteComics(
      SearchFavoriteComics event, Emitter<FavoriteComicsState> emit) async {
    if (state is FavoriteComicsLoaded) {
      final currentState = state as FavoriteComicsLoaded;
      final filteredComics = _filterComics(currentState.allComics, event.query);
      emit(currentState.copyWith(
        displayedComics: filteredComics,
        searchQuery: event.query,
      ));
    }
  }

  Future<void> _onSortFavoriteComics(
      SortFavoriteComics event, Emitter<FavoriteComicsState> emit) async {
    if (state is FavoriteComicsLoaded) {
      final currentState = state as FavoriteComicsLoaded;
      final sortedComics = _sortComics(currentState.displayedComics, event.sortType);
      emit(currentState.copyWith(
        displayedComics: sortedComics,
        currentSortType: event.sortType,
      ));
    }
  }

  Future<void> _onRemoveComicFromFavorite(
      RemoveComicFromFavorite event, Emitter<FavoriteComicsState> emit) async {
    if (state is FavoriteComicsLoaded) {
      final currentState = state as FavoriteComicsLoaded;
      try {
        final failureOrRemove = await removeComicFromFavoriteUseCase(
            currentState.favoriteId, event.comicId);
        failureOrRemove.fold(
          (failure) {
            final errorMessage = 'Failed to remove comic from favorite: $failure';
            loggingService.error(errorMessage);
            emit(FavoriteComicsError('无法从收藏夹移除漫画。'));
          },
          (_) {
            // 重新加载漫画列表
            add(LoadFavoriteComics(currentState.favoriteId));
          },
        );
      } catch (e, s) {
        loggingService.error('Unhandled error in _onRemoveComicFromFavorite', e, s);
        emit(FavoriteComicsError('发生未知错误。'));
      }
    }
  }

  Future<void> _onToggleComicSelection(
      ToggleComicSelection event, Emitter<FavoriteComicsState> emit) async {
    if (state is FavoriteComicsLoaded) {
      final currentState = state as FavoriteComicsLoaded;
      final newSelectedComics = Set<String>.from(currentState.selectedComics);
      
      if (newSelectedComics.contains(event.comicId)) {
        newSelectedComics.remove(event.comicId);
      } else {
        newSelectedComics.add(event.comicId);
      }

      emit(currentState.copyWith(selectedComics: newSelectedComics));
    }
  }

  Future<void> _onSelectAllComics(
      SelectAllComics event, Emitter<FavoriteComicsState> emit) async {
    if (state is FavoriteComicsLoaded) {
      final currentState = state as FavoriteComicsLoaded;
      final allComicIds = currentState.displayedComics.map((comic) => comic.id).toSet();
      emit(currentState.copyWith(selectedComics: allComicIds));
    }
  }

  Future<void> _onClearSelection(
      ClearSelection event, Emitter<FavoriteComicsState> emit) async {
    if (state is FavoriteComicsLoaded) {
      final currentState = state as FavoriteComicsLoaded;
      emit(currentState.copyWith(selectedComics: <String>{}));
    }
  }

  Future<void> _onToggleSelectionMode(
      ToggleSelectionMode event, Emitter<FavoriteComicsState> emit) async {
    if (state is FavoriteComicsLoaded) {
      final currentState = state as FavoriteComicsLoaded;
      emit(currentState.copyWith(
        isSelectionMode: !currentState.isSelectionMode,
        selectedComics: <String>{}, // 清空选择
      ));
    }
  }

  Future<void> _onRemoveSelectedComics(
      RemoveSelectedComics event, Emitter<FavoriteComicsState> emit) async {
    if (state is FavoriteComicsLoaded) {
      final currentState = state as FavoriteComicsLoaded;
      try {
        for (final comicId in currentState.selectedComics) {
          await removeComicFromFavoriteUseCase(currentState.favoriteId, comicId);
        }
        
        // 重新加载漫画列表
        add(LoadFavoriteComics(currentState.favoriteId));
        add(ToggleSelectionMode()); // 退出选择模式
      } catch (e, s) {
        loggingService.error('Unhandled error in _onRemoveSelectedComics', e, s);
        emit(FavoriteComicsError('批量移除失败。'));
      }
    }
  }

  List<Comic> _filterComics(List<Comic> comics, String query) {
    if (query.trim().isEmpty) {
      return comics;
    }
    
    final lowercaseQuery = query.toLowerCase();
    return comics.where((comic) {
      return comic.title.toLowerCase().contains(lowercaseQuery) ||
             comic.fileName.toLowerCase().contains(lowercaseQuery);
    }).toList();
  }

  List<Comic> _sortComics(List<Comic> comics, FavoriteComicsSortType sortType) {
    final List<Comic> sortedList = List.from(comics);
    
    switch (sortType) {
      case FavoriteComicsSortType.title:
        sortedList.sort((a, b) => a.title.toLowerCase().compareTo(b.title.toLowerCase()));
        break;
      case FavoriteComicsSortType.addTime:
        sortedList.sort((a, b) => b.addTime.compareTo(a.addTime));
        break;
      case FavoriteComicsSortType.lastRead:
        sortedList.sort((a, b) {
          if (a.lastReadTime == null && b.lastReadTime == null) return 0;
          if (a.lastReadTime == null) return 1;
          if (b.lastReadTime == null) return -1;
          return b.lastReadTime!.compareTo(a.lastReadTime!);
        });
        break;
      case FavoriteComicsSortType.progress:
        sortedList.sort((a, b) {
          final aProgress = a.pageCount > 0 ? a.progress / a.pageCount : 0.0;
          final bProgress = b.pageCount > 0 ? b.progress / b.pageCount : 0.0;
          return bProgress.compareTo(aProgress);
        });
        break;
    }
    
    return sortedList;
  }
}