// lib/presentation/features/reader/bloc/reader_bloc.dart
import 'dart:async';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:easy_comic/domain/repositories/comic_repository.dart';
import 'package:easy_comic/domain/repositories/settings_repository.dart';
import 'package:easy_comic/domain/repositories/bookmark_repository.dart';
import 'package:easy_comic/domain/services/auto_page_service.dart';
import 'package:easy_comic/domain/services/cache_service.dart';
import 'package:easy_comic/domain/entities/comic.dart';
import 'package:easy_comic/domain/entities/comic_page.dart';
import 'package:easy_comic/domain/entities/reader_settings.dart';
import 'package:easy_comic/domain/entities/bookmark.dart';
import 'package:easy_comic/core/comic_archive.dart';
import '../../../widgets/auto_page/auto_page_manager.dart';
import 'reader_event.dart';
import 'reader_state.dart';

class ReaderBloc extends Bloc<ReaderEvent, ReaderState> {
  final ComicRepository comicRepository;
  final SettingsRepository settingsRepository;
  final BookmarkRepository bookmarkRepository;
  final AutoPageService autoPageService;
  final CacheService cacheService;
  final ComicArchive comicArchive;

  late AutoPageManager _autoPageManager;
  StreamSubscription<AutoPageEvent>? _autoPageSubscription;
  Timer? _progressSaveTimer;
  
  ReaderBloc({
    required this.comicRepository,
    required this.settingsRepository,
    required this.bookmarkRepository,
    required this.autoPageService,
    required this.cacheService,
    required this.comicArchive,
  }) : super(ReaderInitial()) {
    _autoPageManager = AutoPageManager();
    _setupAutoPageSubscription();
    
    // Register event handlers
    on<LoadComic>(_onLoadComic);
    on<PageChanged>(_onPageChanged);
    on<PreviousPage>(_onPreviousPage);
    on<NextPage>(_onNextPage);
    on<ToggleUIVisibility>(_onToggleUIVisibility);
    on<ToggleAutoPageTurn>(_onToggleAutoPageTurn);
    on<UpdateAutoPageInterval>(_onUpdateAutoPageInterval);
    on<ChangeReadingMode>(_onChangeReadingMode);
    on<ToggleFullscreen>(_onToggleFullscreen);
    on<UpdateBrightness>(_onUpdateBrightness);
    on<ZoomChanged>(_onZoomChanged);
    on<HandleGesture>(_onHandleGesture);
    on<SaveProgress>(_onSaveProgress);
    on<AddBookmark>(_onAddBookmark);
    on<RemoveBookmark>(_onRemoveBookmark);
    on<LoadSettings>(_onLoadSettings);
    on<UpdateSettings>(_onUpdateSettings);
  }

  void _setupAutoPageSubscription() {
    _autoPageSubscription = _autoPageManager.autoPageEventStream.listen((event) {
      switch (event.type) {
        case AutoPageEventType.nextPage:
          add(NextPage());
          break;
        case AutoPageEventType.pause:
        case AutoPageEventType.resume:
        case AutoPageEventType.stop:
          // Handle UI updates for auto-page state changes
          _emitCurrentStateWithAutoPageUpdate();
          break;
        default:
          break;
      }
    });
  }

  Future<void> _onLoadComic(LoadComic event, Emitter<ReaderState> emit) async {
    try {
      emit(ReaderLoading());
      
      // Load comic data
      Comic? comic;
      List<ComicPage> pages = [];
      
      if (event.comicId != null) {
        // Load from database
        final comicResult = await comicRepository.getComicById(event.comicId!.toString());
        comic = comicResult.fold(
          (failure) => throw Exception('Failed to load comic: ${failure.toString()}'),
          (loadedComic) => loadedComic,
        );
      } else if (event.filePath != null) {
        // Load from file
        final archive = ComicArchive(path: event.filePath!);
        pages = await archive.extractPages();
        comic = Comic(
          id: event.filePath!,
          title: _extractTitleFromPath(event.filePath!),
          author: 'Unknown Author', // Default author
          path: event.filePath!,
          filePath: event.filePath!,
          fileName: _extractTitleFromPath(event.filePath!),
          coverPath: '',
          pageCount: pages.length,
          addTime: DateTime.now(),
          lastReadTime: DateTime.now(),
          progress: 0,
          bookshelfId: 0,
          isFavorite: false,
          tags: [],
          metadata: {},
          pages: pages,
          totalPages: pages.length,
          currentPage: 0,
        );
      } else {
        throw Exception('No comic ID or file path provided');
      }
      
      if (comic == null) {
        emit(const ReaderError(message: 'Failed to load comic'));
        return;
      }
      
      // Load reader settings
      final settingsResult = await settingsRepository.getReaderSettings();
      final settings = settingsResult.fold(
        (failure) => const ReaderSettings(), // Use defaults
        (loadedSettings) => loadedSettings,
      );
      
      // Load bookmarks
      final bookmarksResult = await bookmarkRepository.getBookmarksForComic(comic.id);
      final bookmarks = bookmarksResult.fold(
        (failure) => <Bookmark>[],
        (loadedBookmarks) => loadedBookmarks,
      );
      
      // Get current reading progress
      int currentPageIndex = 0;
      // TODO: Load progress from repository
      
      // Preload initial pages
      await _preloadPages(comic.id, currentPageIndex, comic.pages);
      
      emit(ReaderLoaded(
        comic: comic,
        currentPageIndex: currentPageIndex,
        settings: settings,
        bookmarks: bookmarks,
        isCurrentPageBookmarked: _isPageBookmarked(currentPageIndex, bookmarks),
      ));
      
      // Start periodic progress saving
      _startProgressSaveTimer();
      
    } catch (e) {
      emit(ReaderError(message: e.toString()));
    }
  }

  Future<void> _onPageChanged(PageChanged event, Emitter<ReaderState> emit) async {
    final currentState = state;
    if (currentState is! ReaderLoaded) return;
    
    // Check bounds
    if (event.newIndex < 0 || event.newIndex >= (currentState.comic.totalPages ?? 0)) {
      return;
    }
    
    // Pause auto-page if user manually changed page
    if (_autoPageManager.isAutoPageActive) {
      await _autoPageManager.pauseForUserInteraction();
    }
    
    // Preload adjacent pages
    await _preloadAdjacentPages(currentState.comic.id, event.newIndex);
    
    emit(currentState.copyWith(
      currentPageIndex: event.newIndex,
      isCurrentPageBookmarked: _isPageBookmarked(event.newIndex, currentState.bookmarks),
    ));
    
    // Check if reached last page for auto-page
    if (event.newIndex >= (currentState.comic.totalPages ?? 1) - 1) {
      await _autoPageManager.handleLastPageReached();
    }
  }

  Future<void> _onPreviousPage(PreviousPage event, Emitter<ReaderState> emit) async {
    final currentState = state;
    if (currentState is! ReaderLoaded) return;
    
    final newIndex = currentState.currentPageIndex - 1;
    if (newIndex >= 0) {
      add(PageChanged(newIndex));
    }
  }

  Future<void> _onNextPage(NextPage event, Emitter<ReaderState> emit) async {
    final currentState = state;
    if (currentState is! ReaderLoaded) return;
    
    final newIndex = currentState.currentPageIndex + 1;
    if (newIndex < (currentState.comic.totalPages ?? 0)) {
      add(PageChanged(newIndex));
    }
  }

  Future<void> _onToggleUIVisibility(ToggleUIVisibility event, Emitter<ReaderState> emit) async {
    final currentState = state;
    if (currentState is! ReaderLoaded) return;
    
    emit(currentState.copyWith(isUIVisible: !currentState.isUIVisible));
  }

  Future<void> _onToggleAutoPageTurn(ToggleAutoPageTurn event, Emitter<ReaderState> emit) async {
    final currentState = state;
    if (currentState is! ReaderLoaded) return;
    
    if (_autoPageManager.isAutoPageActive) {
      await _autoPageManager.stopAutoPage();
    } else {
      await _autoPageManager.startAutoPage(currentState.settings.autoPageInterval);
    }
    
    emit(currentState.copyWith(
      isAutoPageTurnEnabled: _autoPageManager.isAutoPageActive,
    ));
  }

  Future<void> _onUpdateAutoPageInterval(UpdateAutoPageInterval event, Emitter<ReaderState> emit) async {
    final currentState = state;
    if (currentState is! ReaderLoaded) return;
    
    final intervalSeconds = event.interval.inSeconds;
    await _autoPageManager.setInterval(intervalSeconds);
    
    final newSettings = currentState.settings.copyWith(autoPageInterval: intervalSeconds);
    await settingsRepository.saveReaderSettings(newSettings);
    
    emit(currentState.copyWith(
      settings: newSettings,
      autoPageInterval: event.interval,
    ));
  }

  Future<void> _onChangeReadingMode(ChangeReadingMode event, Emitter<ReaderState> emit) async {
    final currentState = state;
    if (currentState is! ReaderLoaded) return;
    
    final newSettings = currentState.settings.copyWith(readingMode: event.mode);
    await settingsRepository.saveReaderSettings(newSettings);
    
    emit(currentState.copyWith(settings: newSettings));
  }

  Future<void> _onToggleFullscreen(ToggleFullscreen event, Emitter<ReaderState> emit) async {
    final currentState = state;
    if (currentState is! ReaderLoaded) return;
    
    final newSettings = currentState.settings.copyWith(enableFullscreen: !currentState.settings.enableFullscreen);
    await settingsRepository.saveReaderSettings(newSettings);
    
    emit(currentState.copyWith(
      settings: newSettings,
      isFullscreen: newSettings.enableFullscreen,
    ));
  }

  Future<void> _onUpdateBrightness(UpdateBrightness event, Emitter<ReaderState> emit) async {
    final currentState = state;
    if (currentState is! ReaderLoaded) return;
    
    final newSettings = currentState.settings.copyWith(brightness: event.brightness);
    await settingsRepository.saveReaderSettings(newSettings);
    
    emit(currentState.copyWith(
      settings: newSettings,
      brightness: event.brightness,
    ));
  }

  Future<void> _onZoomChanged(ZoomChanged event, Emitter<ReaderState> emit) async {
    final currentState = state;
    if (currentState is! ReaderLoaded) return;
    
    emit(currentState.copyWith(zoomLevel: event.zoomLevel));
  }

  Future<void> _onHandleGesture(HandleGesture event, Emitter<ReaderState> emit) async {
    final currentState = state;
    if (currentState is! ReaderLoaded) return;
    
    switch (event.gestureType) {
      case GestureType.tapLeft:
        add(PreviousPage());
        break;
      case GestureType.tapRight:
        add(NextPage());
        break;
      case GestureType.tapCenter:
        add(ToggleUIVisibility());
        break;
      case GestureType.doubleTap:
        // Handle double-tap zoom
        break;
      case GestureType.longPress:
        // Handle long press actions
        break;
      case GestureType.swipeLeft:
      case GestureType.swipeRight:
      case GestureType.swipeUp:
      case GestureType.swipeDown:
        // Handle swipe gestures
        break;
      case GestureType.pinchIn:
      case GestureType.pinchOut:
        // Handle pinch gestures
        break;
    }
    
    // Pause auto-page on user interaction
    if (_autoPageManager.isAutoPageActive) {
      await _autoPageManager.pauseForUserInteraction();
    }
  }

  Future<void> _onSaveProgress(SaveProgress event, Emitter<ReaderState> emit) async {
    final currentState = state;
    if (currentState is! ReaderLoaded) return;
    
    // Save reading progress to repository
    // TODO: Implement progress saving
  }

  Future<void> _onAddBookmark(AddBookmark event, Emitter<ReaderState> emit) async {
    final currentState = state;
    if (currentState is! ReaderLoaded) return;
    
    final bookmark = Bookmark(
      comicId: currentState.comic.id,
      pageIndex: event.pageIndex,
      createdAt: DateTime.now(),
    );
    
    final result = await bookmarkRepository.addBookmark(bookmark);
    result.fold(
      (failure) => emit(ReaderError(message: 'Failed to add bookmark: ${failure.toString()}')),
      (_) {
        final updatedBookmarks = [...currentState.bookmarks, bookmark];
        emit(currentState.copyWith(
          bookmarks: updatedBookmarks,
          isCurrentPageBookmarked: _isPageBookmarked(currentState.currentPageIndex, updatedBookmarks),
        ));
      },
    );
  }

  Future<void> _onRemoveBookmark(RemoveBookmark event, Emitter<ReaderState> emit) async {
    final currentState = state;
    if (currentState is! ReaderLoaded) return;
    
    final bookmarkToRemove = currentState.bookmarks.firstWhere(
      (bookmark) => bookmark.pageIndex == event.pageIndex,
      orElse: () => throw Exception('Bookmark not found'),
    );
    
    final result = await bookmarkRepository.removeBookmark(bookmarkToRemove.comicId, bookmarkToRemove.pageIndex);
    result.fold(
      (failure) => emit(ReaderError(message: 'Failed to remove bookmark: ${failure.toString()}')),
      (_) {
        final updatedBookmarks = currentState.bookmarks
            .where((bookmark) => bookmark.pageIndex != event.pageIndex)
            .toList();
        emit(currentState.copyWith(
          bookmarks: updatedBookmarks,
          isCurrentPageBookmarked: _isPageBookmarked(currentState.currentPageIndex, updatedBookmarks),
        ));
      },
    );
  }

  Future<void> _onLoadSettings(LoadSettings event, Emitter<ReaderState> emit) async {
    final settingsResult = await settingsRepository.getReaderSettings();
    settingsResult.fold(
      (failure) => emit(ReaderError(message: 'Failed to load settings: ${failure.toString()}')),
      (settings) {
        final currentState = state;
        if (currentState is ReaderLoaded) {
          emit(currentState.copyWith(settings: settings));
        }
      },
    );
  }

  Future<void> _onUpdateSettings(UpdateSettings event, Emitter<ReaderState> emit) async {
    final result = await settingsRepository.saveReaderSettings(event.settings);
    result.fold(
      (failure) => emit(ReaderError(message: 'Failed to save settings: ${failure.toString()}')),
      (_) {
        final currentState = state;
        if (currentState is ReaderLoaded) {
          emit(currentState.copyWith(settings: event.settings));
        }
      },
    );
  }

  // Helper methods
  String _extractTitleFromPath(String filePath) {
    return filePath.split('/').last.split('.').first;
  }

  bool _isPageBookmarked(int pageIndex, List<Bookmark> bookmarks) {
    return bookmarks.any((bookmark) => bookmark.pageIndex == pageIndex);
  }

  Future<void> _preloadPages(String comicId, int currentPage, List<ComicPage> pages) async {
    // Preload current and adjacent pages
    // await cacheService.preloadPages(comicId, currentPage, 3);
  }

  Future<void> _preloadAdjacentPages(String comicId, int currentPage) async {
    // Preload next few pages
    // await cacheService.preloadPages(comicId, currentPage + 1, 2);
  }

  void _emitCurrentStateWithAutoPageUpdate() {
    final currentState = state;
    if (currentState is ReaderLoaded) {
      // This would trigger a rebuild with current auto-page state
      // For now, we'll just emit the same state to trigger listeners
      emit(currentState.copyWith(
        isAutoPageTurnEnabled: _autoPageManager.isAutoPageActive,
      ));
    }
  }

  void _startProgressSaveTimer() {
    _progressSaveTimer?.cancel();
    _progressSaveTimer = Timer.periodic(const Duration(seconds: 10), (timer) {
      final currentState = state;
      if (currentState is ReaderLoaded) {
        add(SaveProgress(currentState.currentPageIndex));
      }
    });
  }

  @override
  Future<void> close() {
    _autoPageSubscription?.cancel();
    _progressSaveTimer?.cancel();
    _autoPageManager.dispose();
    return super.close();
  }
}