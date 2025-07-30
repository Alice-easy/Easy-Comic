import 'dart:async';

import 'package:flutter_bloc/flutter_bloc.dart';

import '../../data/comic_repository.dart';
import '../../data/settings_repository.dart';
import 'reader_event.dart';
import 'reader_state.dart';

class ReaderBloc extends Bloc<ReaderEvent, ReaderState> {
  final ComicRepository _comicRepository;
  final SettingsRepository _settingsRepository;

  ReaderBloc({
    required ComicRepository comicRepository,
    required SettingsRepository settingsRepository,
  })  : _comicRepository = comicRepository,
        _settingsRepository = settingsRepository,
        super(const ReaderState()) {
    on<LoadComicEvent>(_onLoadComic);
    on<PageChangedEvent>(_onPageChanged);
    on<ZoomChangedEvent>(_onZoomChanged);
    on<ToggleUiVisibilityEvent>(_onToggleUiVisibility);
    on<UpdateSettingEvent>(_onUpdateSetting);
    on<AddBookmarkEvent>(_onAddBookmark);
    on<DeleteBookmarkEvent>(_onDeleteBookmark);
  }

  Future<void> _onLoadComic(
      LoadComicEvent event, Emitter<ReaderState> emit) async {
    emit(state.copyWith(status: ReaderStatus.loading));
    try {
      final comicDetail = await _comicRepository.getComicDetail(event.comicId);
      final pages = await _comicRepository.getComicPages(event.comicId);
      final settings = await _settingsRepository.getReaderSettings();

      emit(state.copyWith(
        status: ReaderStatus.success,
        comicDetail: comicDetail,
        pages: pages,
        settings: settings,
        currentPage: comicDetail.lastReadPage,
      ));
    } catch (e) {
      emit(state.copyWith(
        status: ReaderStatus.failure, 
        errorMessage: e.toString(),
      ));
    }
  }

  void _onPageChanged(PageChangedEvent event, Emitter<ReaderState> emit) {
    if (state.comicDetail == null) {
      return;
    }

    emit(state.copyWith(currentPage: event.newPage));

    _comicRepository.updateReadingProgress(
      comicId: state.comicDetail!.id,
      page: event.newPage,
    );
  }

  void _onZoomChanged(ZoomChangedEvent event, Emitter<ReaderState> emit) {
    emit(state.copyWith(zoomScale: event.newScale));
  }

  void _onToggleUiVisibility(
      ToggleUiVisibilityEvent event, Emitter<ReaderState> emit) {
    emit(state.copyWith(isUiVisible: !state.isUiVisible));
  }

  void _onUpdateSetting(UpdateSettingEvent event, Emitter<ReaderState> emit) {
    emit(state.copyWith(settings: event.newSettings));
    _settingsRepository.saveReaderSettings(event.newSettings);
  }

  Future<void> _onAddBookmark(
      AddBookmarkEvent event, Emitter<ReaderState> emit) async {
    if (state.comicDetail == null) {
      return;
    }
    try {
      await _comicRepository.saveBookmark(
        comicId: state.comicDetail!.id,
        page: state.currentPage,
        label: event.label,
      );
      final bookmarks = await _comicRepository.getBookmarks(state.comicDetail!.id);
      emit(state.copyWith(bookmarks: bookmarks));
    } catch (e) {
      // Optionally, handle the error by emitting a specific error state
    }
  }

  Future<void> _onDeleteBookmark(
      DeleteBookmarkEvent event, Emitter<ReaderState> emit) async {
    if (state.comicDetail == null) {
      return;
    }
    try {
      await _comicRepository.deleteBookmark(event.bookmarkId);
      final bookmarks = await _comicRepository.getBookmarks(state.comicDetail!.id);
      emit(state.copyWith(bookmarks: bookmarks));
    } catch (e) {
      // Optionally, handle the error by emitting a specific error state
    }
  }
}