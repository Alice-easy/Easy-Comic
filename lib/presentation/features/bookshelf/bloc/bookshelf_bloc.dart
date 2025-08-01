import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:easy_comic/domain/usecases/import_comic_from_file_usecase.dart';
import 'package:file_picker/file_picker.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:easy_comic/core/services/logging_service.dart';
import 'package:easy_comic/domain/entities/comic.dart';
import 'package:easy_comic/domain/usecases/get_bookshelf_comics_usecase.dart';
import 'package:easy_comic/domain/usecases/search_bookshelf_comics_usecase.dart';
import 'package:easy_comic/domain/usecases/sort_bookshelf_comics_usecase.dart';
import 'bookshelf_event.dart';
import 'bookshelf_state.dart';

class BookshelfBloc extends Bloc<BookshelfEvent, BookshelfState> {
  final GetBookshelfComicsUsecase getBookshelfComics;
  final SearchBookshelfComicsUsecase searchBookshelfComics;
  final SortBookshelfComicsUsecase sortBookshelfComics;
  final ImportComicFromFileUsecase importComicFromFile;
  final LoggingService loggingService;

  // Store the current bookshelfId to be used by search and sort
  String _currentBookshelfId = '';

  BookshelfBloc({
    required this.getBookshelfComics,
    required this.searchBookshelfComics,
    required this.sortBookshelfComics,
    required this.importComicFromFile,
    required this.loggingService,
  }) : super(BookshelfInitial()) {
    on<LoadBookshelf>(_onLoadBookshelf);
    on<SearchComics>(_onSearchComics);
    on<SortComics>(_onSortComics);
    on<ImportComicEvent>(_onImportComic);
    on<LoadMoreComics>(_onLoadMoreComics);
  }

  Future<void> _onLoadBookshelf(
      LoadBookshelf event, Emitter<BookshelfState> emit) async {
    try {
      emit(BookshelfLoading());
      _currentBookshelfId = event.bookshelfId;
      final failureOrComics = await getBookshelfComics(
          Params(bookshelfId: _currentBookshelfId, limit: 20, offset: 0));
      failureOrComics.fold(
        (failure) {
          final errorMessage = 'Failed to load bookshelf: $failure';
          loggingService.error(errorMessage);
          emit(BookshelfError(message: '无法加载书架，请稍后重试。'));
        },
        (comics) => emit(BookshelfLoaded(comics: comics, hasReachedMax: comics.length < 20)),
      );
    } catch (e, s) {
      loggingService.error('Unhandled error in _onLoadBookshelf', e, s);
      emit(BookshelfError(message: '发生未知错误。'));
    }
  }

  Future<void> _onLoadMoreComics(
      LoadMoreComics event, Emitter<BookshelfState> emit) async {
    if (state is BookshelfLoaded && !(state as BookshelfLoaded).hasReachedMax) {
      final currentState = state as BookshelfLoaded;
      try {
        final failureOrComics = await getBookshelfComics(Params(
            bookshelfId: _currentBookshelfId,
            limit: 20,
            offset: currentState.comics.length));

        failureOrComics.fold(
          (failure) {
            final errorMessage = 'Failed to load more comics: $failure';
            loggingService.error(errorMessage);
            // Optionally, we could emit an error state that doesn't clear existing comics
          },
          (newComics) {
            if (newComics.isEmpty) {
              emit(currentState.copyWith(hasReachedMax: true));
            } else {
              emit(currentState.copyWith(
                comics: currentState.comics + newComics,
                hasReachedMax: newComics.length < 20,
              ));
            }
          },
        );
      } catch (e, s) {
        loggingService.error('Unhandled error in _onLoadMoreComics', e, s);
        // Handle error without clearing the screen
      }
    }
  }

  Future<void> _onSearchComics(
      SearchComics event, Emitter<BookshelfState> emit) async {
    try {
      emit(BookshelfLoading());
      final failureOrComics =
          await searchBookshelfComics(_currentBookshelfId, event.query);
      failureOrComics.fold(
        (failure) {
          final errorMessage = 'Failed to search comics: $failure';
          loggingService.error(errorMessage);
          emit(BookshelfError(message: '搜索失败，请稍后重试。'));
        },
        (comics) => emit(BookshelfLoaded(comics: comics)),
      );
    } catch (e, s) {
      loggingService.error('Unhandled error in _onSearchComics', e, s);
      emit(BookshelfError(message: '发生未知错误。'));
    }
  }

  Future<void> _onSortComics(
      SortComics event, Emitter<BookshelfState> emit) async {
    try {
      emit(BookshelfLoading());
      final failureOrComics =
          await sortBookshelfComics(_currentBookshelfId, event.sortType);
      failureOrComics.fold(
        (failure) {
          final errorMessage = 'Failed to sort comics: $failure';
          loggingService.error(errorMessage);
          emit(BookshelfError(message: '排序失败，请稍后重试。'));
        },
        (comics) => emit(BookshelfLoaded(comics: comics)),
      );
    } catch (e, s) {
      loggingService.error('Unhandled error in _onSortComics', e, s);
      emit(BookshelfError(message: '发生未知错误。'));
    }
  }

  Future<void> _onImportComic(
      ImportComicEvent event, Emitter<BookshelfState> emit) async {
    try {
      final result = await FilePicker.platform.pickFiles(
        type: FileType.custom,
        allowedExtensions: ['zip', 'cbz'],
      );

      if (result != null && result.files.single.path != null) {
        final filePath = result.files.single.path!;
        final int bookshelfId = int.tryParse(event.bookshelfId) ?? 1;
        final failureOrSuccess =
            await importComicFromFile(filePath, bookshelfId);

        failureOrSuccess.fold(
          (failure) {
            final errorMessage = 'Failed to import comic: $failure';
            loggingService.error(errorMessage);
            emit(BookshelfError(message: '导入失败，请检查文件格式。'));
          },
          (_) => add(LoadBookshelf(event.bookshelfId)),
        );
      }
    } catch (e, s) {
      loggingService.error('Unhandled error in _onImportComic', e, s);
      emit(BookshelfError(message: '发生未知错误。'));
    }
  }
}