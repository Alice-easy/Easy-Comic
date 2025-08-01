import 'package:bloc/bloc.dart';
import 'package:easy_comic/core/services/logging_service.dart';
import 'package:easy_comic/domain/entities/favorite.dart';
import 'package:easy_comic/domain/usecases/add_comic_to_favorite_usecase.dart';
import 'package:easy_comic/domain/usecases/create_favorite_usecase.dart';
import 'package:easy_comic/domain/usecases/delete_favorite_usecase.dart';
import 'package:easy_comic/domain/usecases/get_favorites_usecase.dart';
import 'package:easy_comic/domain/usecases/remove_comic_from_favorite_usecase.dart';
import 'package:equatable/equatable.dart';

part 'favorites_event.dart';
part 'favorites_state.dart';

class FavoritesBloc extends Bloc<FavoritesEvent, FavoritesState> {
  final CreateFavoriteUseCase createFavoriteUseCase;
  final GetFavoritesUseCase getFavoritesUseCase;
  final AddComicToFavoriteUseCase addComicToFavoriteUseCase;
  final RemoveComicFromFavoriteUseCase removeComicFromFavoriteUseCase;
  final DeleteFavoriteUseCase deleteFavoriteUseCase;
  final LoggingService loggingService;

  FavoritesBloc({
    required this.createFavoriteUseCase,
    required this.getFavoritesUseCase,
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
        (favorites) => emit(FavoritesLoaded(favorites)),
      );
    } catch (e, s) {
      loggingService.error('Unhandled error in _onLoadFavorites', e, s);
      emit(FavoritesError('发生未知错误。'));
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
}