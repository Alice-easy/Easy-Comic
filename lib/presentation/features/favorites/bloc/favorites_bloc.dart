import 'package:bloc/bloc.dart';
import 'package:easy_comic/core/services/logging_service.dart';
import 'package:easy_comic/domain/entities/favorite.dart';
import 'package:easy_comic/domain/usecases/add_comic_to_favorite_usecase.dart';
import 'package:easy_comic/domain/usecases/create_favorite_usecase.dart';
import 'package:easy_comic/domain/usecases/delete_favorite_usecase.dart';
import 'package:easy_comic/domain/usecases/get_favorites_usecase.dart';
import 'package:easy_comic/domain/usecases/get_favorite_comics_usecase.dart';
import 'package:easy_comic/domain/usecases/remove_comic_from_favorite_usecase.dart';
import 'package:equatable/equatable.dart';

part 'favorites_event.dart';
part 'favorites_state.dart';

class FavoritesBloc extends Bloc<FavoritesEvent, FavoritesState> {
  final CreateFavoriteUseCase createFavoriteUseCase;
  final GetFavoritesUseCase getFavoritesUseCase;
  final GetFavoriteComicsUseCase getFavoriteComicsUseCase;
  final AddComicToFavoriteUseCase addComicToFavoriteUseCase;
  final RemoveComicFromFavoriteUseCase removeComicFromFavoriteUseCase;
  final DeleteFavoriteUseCase deleteFavoriteUseCase;
  final LoggingService loggingService;

  FavoritesBloc({
    required this.createFavoriteUseCase,
    required this.getFavoritesUseCase,
    required this.getFavoriteComicsUseCase,
    required this.addComicToFavoriteUseCase,
    required this.removeComicFromFavoriteUseCase,
    required this.deleteFavoriteUseCase,
    required this.loggingService,
  }) : super(FavoritesInitial()) {
    on<LoadFavorites>(_onLoadFavorites);
    on<CreateFavorite>(_onCreateFavorite);
    on<DeleteFavorite>(_onDeleteFavorite);
    on<AddComicToFavorite>(_onAddComicToFavorite);
    on<RemoveComicFromFavorite>(_onRemoveComicFromFavorite);
    on<RenameFavorite>(_onRenameFavorite);
    on<SortFavorites>(_onSortFavorites);
    on<ValidateFavoriteName>(_onValidateFavoriteName);
  }

  Future<void> _onLoadFavorites(
      LoadFavorites event, Emitter<FavoritesState> emit) async {
    try {
      emit(FavoritesLoading());
      final failureOrFavorites = await getFavoritesUseCase();
      failureOrFavorites.fold(
        (failure) {
          final errorMessage = 'Failed to load favorites: $failure';
          loggingService.error(errorMessage);
          emit(FavoritesError('无法加载收藏夹。'));
        },
        (favorites) {
          // 加载收藏夹的漫画数量
          _loadComicCounts(favorites, emit);
          emit(FavoritesLoaded(favorites));
        },
      );
    } catch (e, s) {
      loggingService.error('Unhandled error in _onLoadFavorites', e, s);
      emit(FavoritesError('发生未知错误。'));
    }
  }

  Future<void> _loadComicCounts(List<Favorite> favorites, Emitter<FavoritesState> emit) async {
    final comicCounts = <int, int>{};
    for (final favorite in favorites) {
      try {
        final failureOrComics = await getFavoriteComicsUseCase.call(favorite.id);
        failureOrComics.fold(
          (failure) => comicCounts[favorite.id] = 0,
          (comics) => comicCounts[favorite.id] = comics.length,
        );
      } catch (e) {
        comicCounts[favorite.id] = 0;
      }
    }
    
    if (state is FavoritesLoaded) {
      emit((state as FavoritesLoaded).copyWith(comicCounts: comicCounts));
    }
  }

  Future<void> _onCreateFavorite(
      CreateFavorite event, Emitter<FavoritesState> emit) async {
    try {
      final failureOrCreate = await createFavoriteUseCase(event.name);
      failureOrCreate.fold(
        (failure) {
          final errorMessage = 'Failed to create favorite: $failure';
          loggingService.error(errorMessage);
          emit(FavoritesError('无法创建收藏夹。'));
        },
        (_) => add(LoadFavorites()),
      );
    } catch (e, s) {
      loggingService.error('Unhandled error in _onCreateFavorite', e, s);
      emit(FavoritesError('发生未知错误。'));
    }
  }

  Future<void> _onDeleteFavorite(
      DeleteFavorite event, Emitter<FavoritesState> emit) async {
    try {
      final failureOrDelete = await deleteFavoriteUseCase(event.id);
      failureOrDelete.fold(
        (failure) {
          final errorMessage = 'Failed to delete favorite: $failure';
          loggingService.error(errorMessage);
          emit(FavoritesError('无法删除收藏夹。'));
        },
        (_) => add(LoadFavorites()),
      );
    } catch (e, s) {
      loggingService.error('Unhandled error in _onDeleteFavorite', e, s);
      emit(FavoritesError('发生未知错误。'));
    }
  }

  Future<void> _onAddComicToFavorite(
      AddComicToFavorite event, Emitter<FavoritesState> emit) async {
    try {
      final failureOrAdd =
          await addComicToFavoriteUseCase(event.favoriteId, event.comicId);
      failureOrAdd.fold(
        (failure) {
          final errorMessage = 'Failed to add comic to favorite: $failure';
          loggingService.error(errorMessage);
          emit(FavoritesError('无法添加到收藏夹。'));
        },
        (_) => add(LoadFavorites()), // Or a more specific success state
      );
    } catch (e, s) {
      loggingService.error('Unhandled error in _onAddComicToFavorite', e, s);
      emit(FavoritesError('发生未知错误。'));
    }
  }

  Future<void> _onRemoveComicFromFavorite(
      RemoveComicFromFavorite event, Emitter<FavoritesState> emit) async {
    try {
      final failureOrRemove = await removeComicFromFavoriteUseCase(
          event.favoriteId, event.comicId);
      failureOrRemove.fold(
        (failure) {
          final errorMessage = 'Failed to remove comic from favorite: $failure';
          loggingService.error(errorMessage);
          emit(FavoritesError('无法从收藏夹移除。'));
        },
        (_) => add(LoadFavorites()), // Or a more specific success state
      );
    } catch (e, s) {
      loggingService.error('Unhandled error in _onRemoveComicFromFavorite', e, s);
      emit(FavoritesError('发生未知错误。'));
    }
  }

  Future<void> _onRenameFavorite(
      RenameFavorite event, Emitter<FavoritesState> emit) async {
    try {
      // 首先验证新名称
      final isValid = await _validateFavoriteName(event.newName, excludeId: event.id);
      if (!isValid.isValid) {
        emit(FavoritesError(isValid.errorMessage ?? '收藏夹名称无效。'));
        return;
      }

      // TODO: 需要实现RenameFavoriteUseCase
      // final failureOrRename = await renameFavoriteUseCase(event.id, event.newName);
      // failureOrRename.fold(
      //   (failure) {
      //     final errorMessage = 'Failed to rename favorite: $failure';
      //     loggingService.error(errorMessage);
      //     emit(FavoritesError('无法重命名收藏夹。'));
      //   },
      //   (_) {
      //     emit(FavoriteOperationSuccess(
      //       message: '收藏夹已重命名',
      //       operationType: FavoriteOperationType.rename,
      //     ));
      //     add(LoadFavorites());
      //   },
      // );
      
      // 临时实现：直接刷新收藏夹列表
      emit(FavoriteOperationSuccess(
        message: '收藏夹重命名功能正在开发中',
        operationType: FavoriteOperationType.rename,
      ));
      add(LoadFavorites());
    } catch (e, s) {
      loggingService.error('Unhandled error in _onRenameFavorite', e, s);
      emit(FavoritesError('发生未知错误。'));
    }
  }

  Future<void> _onSortFavorites(
      SortFavorites event, Emitter<FavoritesState> emit) async {
    if (state is FavoritesLoaded) {
      final currentState = state as FavoritesLoaded;
      final sortedFavorites = _sortFavoritesList(currentState.favorites, event.sortType);
      emit(currentState.copyWith(
        favorites: sortedFavorites,
        currentSortType: event.sortType,
      ));
    }
  }

  Future<void> _onValidateFavoriteName(
      ValidateFavoriteName event, Emitter<FavoritesState> emit) async {
    final validation = await _validateFavoriteName(event.name, excludeId: event.excludeId);
    emit(validation);
  }

  Future<FavoriteNameValidation> _validateFavoriteName(String name, {int? excludeId}) async {
    // 基本验证
    if (name.trim().isEmpty) {
      return const FavoriteNameValidation(
        isValid: false,
        errorMessage: '收藏夹名称不能为空',
        validatedName: '',
      );
    }

    if (name.trim().length > 50) {
      return FavoriteNameValidation(
        isValid: false,
        errorMessage: '收藏夹名称不能超过50个字符',
        validatedName: name.trim(),
      );
    }

    // 检查重名
    try {
      final failureOrFavorites = await getFavoritesUseCase();
      return failureOrFavorites.fold(
        (failure) => FavoriteNameValidation(
          isValid: true, // 如果无法获取收藏夹列表，就假设名称有效
          validatedName: name.trim(),
        ),
        (favorites) {
          final trimmedName = name.trim();
          final isDuplicate = favorites.any((favorite) =>
              favorite.name.toLowerCase() == trimmedName.toLowerCase() &&
              (excludeId == null || favorite.id != excludeId));

          if (isDuplicate) {
            return FavoriteNameValidation(
              isValid: false,
              errorMessage: '该收藏夹名称已存在',
              validatedName: trimmedName,
            );
          }

          return FavoriteNameValidation(
            isValid: true,
            validatedName: trimmedName,
          );
        },
      );
    } catch (e) {
      loggingService.error('Error validating favorite name', e);
      return FavoriteNameValidation(
        isValid: true, // 如果验证失败，就假设名称有效
        validatedName: name.trim(),
      );
    }
  }

  List<Favorite> _sortFavoritesList(List<Favorite> favorites, FavoriteSortType sortType) {
    final List<Favorite> sortedList = List.from(favorites);
    
    switch (sortType) {
      case FavoriteSortType.nameAsc:
        sortedList.sort((a, b) => a.name.toLowerCase().compareTo(b.name.toLowerCase()));
        break;
      case FavoriteSortType.nameDesc:
        sortedList.sort((a, b) => b.name.toLowerCase().compareTo(a.name.toLowerCase()));
        break;
      case FavoriteSortType.createTimeAsc:
        sortedList.sort((a, b) => a.createTime.compareTo(b.createTime));
        break;
      case FavoriteSortType.createTimeDesc:
        sortedList.sort((a, b) => b.createTime.compareTo(a.createTime));
        break;
    }
    
    return sortedList;
  }
}