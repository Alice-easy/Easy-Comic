// lib/presentation/features/reader/bloc/reader_bloc.dart
import 'dart:async';
import 'dart:convert';
import 'dart:developer' as developer;
import 'dart:typed_data';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:easy_comic/domain/repositories/comic_repository.dart';
import 'package:easy_comic/domain/repositories/settings_repository.dart';
import 'package:easy_comic/domain/repositories/bookmark_repository.dart';
import 'package:easy_comic/domain/services/auto_page_service.dart';
import 'package:easy_comic/domain/services/cache_service.dart';
import 'package:easy_comic/domain/services/progress_persistence_manager.dart';
import 'package:easy_comic/core/services/enhanced_cache_service.dart';
import 'package:easy_comic/core/services/page_preloading_service.dart';
import 'package:easy_comic/core/services/unified_manga_importer_service.dart';
import 'package:easy_comic/domain/entities/comic.dart';
import 'package:easy_comic/domain/entities/comic_page.dart';
import 'package:easy_comic/domain/entities/reader_settings.dart';
import 'package:easy_comic/domain/entities/bookmark.dart';
import 'package:easy_comic/core/services/global_error_handler.dart';
import 'package:easy_comic/core/error/retry_mechanism.dart';
import 'package:fpdart/fpdart.dart';
import '../../../widgets/auto_page/auto_page_manager.dart';
import 'reader_event.dart';
import 'reader_state.dart';
import '../../../../core/error/failures.dart';
import '../../../../domain/services/archive_service.dart';

class ReaderBloc extends Bloc<ReaderEvent, ReaderState> {
  final ComicRepository comicRepository;
  final SettingsRepository settingsRepository;
  final BookmarkRepository bookmarkRepository;
  final AutoPageService autoPageService;
  final CacheService cacheService;
  final UnifiedMangaImporterService mangaImporter;
  final IProgressPersistenceManager progressPersistenceManager;

  late AutoPageManager _autoPageManager;
  StreamSubscription<AutoPageEvent>? _autoPageSubscription;
  Timer? _progressSaveTimer;
  Timer? _readingTimeTracker;
  DateTime? _sessionStartTime;
  
  ReaderBloc({
    required this.comicRepository,
    required this.settingsRepository,
    required this.bookmarkRepository,
    required this.autoPageService,
    required this.cacheService,
    required this.mangaImporter,
    required this.progressPersistenceManager,
  }) : super(ReaderInitial()) {
    _autoPageManager = AutoPageManager();
    _setupAutoPageSubscription();
    
    // Register event handlers
    on<LoadMangaEvent>(_onLoadManga);
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
    final loadStartTime = DateTime.now();
    developer.log('Starting comic load', name: 'ReaderBloc', error: {'comicId': event.comicId, 'filePath': event.filePath});

    try {
      emit(ReaderLoading());

      Comic comic;

      if (event.filePath != null) {
        // Priority to file path: always re-import for a fresh state
        developer.log('Importing comic from file path: ${event.filePath}', name: 'ReaderBloc');
        final pagesData = await mangaImporter.importFromPath(event.filePath!);
        final pages = pagesData.mapWithIndex((data, index) => ComicPage(pageIndex: index, imageData: data, path: 'page_$index.jpg')).toList();
        comic = Comic(
          id: event.filePath!,
          title: _extractTitleFromPath(event.filePath!),
          path: event.filePath!,
          filePath: event.filePath!,
          fileName: event.filePath!.split('/').last,
          coverPath: '',
          pageCount: pages.length,
          addTime: DateTime.now(),
          lastReadTime: DateTime.now(),
          progress: 0,
          bookshelfId: 0,
          isFavorite: false,
          tags: [],
          metadata: {},
          author: 'Unknown',
          pages: pages,
        );
      } else if (event.comicId != null) {
        developer.log('Loading comic from database, ID: ${event.comicId}', name: 'ReaderBloc');
        final comicResult = await comicRepository.getComicById(event.comicId!);
        
        Comic? dbComic = comicResult.fold(
          (failure) => null,
          (comic) => comic,
        );

        if (dbComic == null) {
          throw Exception('Comic with ID ${event.comicId} not found in the database.');
        }

        // If pages are missing, re-import from the source file path
        if (dbComic.pages == null || dbComic.pages!.isEmpty) {
          developer.log('Comic from DB is missing pages. Re-importing from: ${dbComic.filePath}', name: 'ReaderBloc');
          if (dbComic.filePath.isEmpty) {
            throw Exception('Comic has no pages and no file path to re-import from.');
          }
          final pagesData = await mangaImporter.importFromPath(dbComic.filePath);
          final pages = pagesData.mapWithIndex((data, index) => ComicPage(pageIndex: index, imageData: data, path: 'page_$index.jpg')).toList();
          comic = dbComic.copyWith(
            pages: pages,
            pageCount: pages.length,
          );
          // Here you might want to update the comic in the repository
          // await comicRepository.addOrUpdateComic(comic);
        } else {
          comic = dbComic;
        }
      } else {
        throw Exception('LoadComic event must have either a comicId or a filePath.');
      }

      // Final validation
      if (comic.pages == null || comic.pages!.isEmpty) {
        throw Exception('Failed to load comic pages.');
      }

      developer.log('Loading settings and bookmarks for comic: ${comic.title}', name: 'ReaderBloc');
      final settingsResult = await settingsRepository.getReaderSettings();
      final bookmarksResult = await bookmarkRepository.getBookmarksForComic(comic.id);

      final settings = settingsResult.fold((l) => const ReaderSettings(), (r) => r);
      final bookmarks = bookmarksResult.fold((l) => <Bookmark>[], (r) => r);

      int currentPageIndex = 0;
      final progressResult = await progressPersistenceManager.loadProgress(comic.id);
      progressResult.when(
        success: (progress) {
          currentPageIndex = progress.currentPage.clamp(0, comic.pages!.length - 1);
          developer.log('Progress restored for comic ${comic.id}: page $currentPageIndex', name: 'ReaderBloc');
        },
        failure: (error) {
          developer.log('Failed to load progress for comic ${comic.id}: ${error.userMessage}. Starting from page 0.', name: 'ReaderBloc', level: 900);
        },
      );

      _sessionStartTime = DateTime.now();
      _startReadingTimeTracker(comic.id);

      await _preloadPages(comic.id, currentPageIndex, comic.pages!);

      final loadDuration = DateTime.now().difference(loadStartTime);
      developer.log('Comic loading completed successfully in ${loadDuration.inMilliseconds}ms', name: 'ReaderBloc');

      emit(ReaderLoaded(
        comic: comic,
        currentPageIndex: currentPageIndex,
        settings: settings,
        bookmarks: bookmarks,
        isCurrentPageBookmarked: _isPageBookmarked(currentPageIndex, bookmarks),
        loadDuration: loadDuration,
        diagnostics: {'loadTime': loadDuration.inMilliseconds, 'pageCount': comic.pages!.length},
      ));

      _startProgressSaveTimer();

    } catch (e, stackTrace) {
      final loadDuration = DateTime.now().difference(loadStartTime);
      developer.log('Comic loading failed', name: 'ReaderBloc', level: 1000, error: e, stackTrace: stackTrace);
      GlobalErrorHandler.reportError(e, stackTrace: stackTrace, context: 'ReaderBloc._onLoadComic', additionalInfo: {
        'comicId': event.comicId,
        'filePath': event.filePath,
        'loadDuration': loadDuration.inMilliseconds,
      });
      emit(ReaderError(
        message: 'Failed to load comic: ${_getUserFriendlyError(e)}',
        errorType: ReaderErrorType.fileError, // Generalize for now
        canRetry: _canRetryError(e),
        originalError: e,
      ));
    }
  }

  Future<void> _onLoadManga(LoadMangaEvent event, Emitter<ReaderState> emit) async {
    emit(ReaderLoading());
    try {
      final pagesData = await mangaImporter.importFromPath(event.path);
      if (pagesData.isEmpty) {
        throw Exception('No images found in the provided path.');
      }
      final pages = pagesData.mapWithIndex((data, index) => ComicPage(pageIndex: index, imageData: data, path: 'page_$index.jpg')).toList();
      final comic = Comic(
        id: event.path,
        title: _extractTitleFromPath(event.path),
        path: event.path,
        filePath: event.path,
        fileName: event.path.split('/').last,
        coverPath: '',
        pageCount: pages.length,
        addTime: DateTime.now(),
        lastReadTime: DateTime.now(),
        progress: 0,
        bookshelfId: 0,
        isFavorite: false,
        tags: [],
        metadata: {},
        author: 'Unknown',
        pages: pages,
      );
      final settings = await settingsRepository.getReaderSettings().then((res) => res.getOrElse(() => const ReaderSettings()));
      emit(ReaderLoaded(
        comic: comic,
        currentPageIndex: 0,
        settings: settings,
        bookmarks: const [],
      ));
    } catch (e, stackTrace) {
      GlobalErrorHandler.reportError(e, stackTrace: stackTrace, context: 'ReaderBloc._onLoadManga');
      emit(ReaderError(message: 'Failed to load manga: ${_getUserFriendlyError(e)}'));
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
    
    // Automatically save progress when page changes
    await _autoSaveProgress(currentState.comic.id ?? '', event.newIndex, currentState.comic.totalPages ?? 0);
    
    // Check if reached last page for auto-page
    if (event.newIndex >= (currentState.comic.totalPages ?? 1) - 1) {
      await _autoPageManager.handleLastPageReached();
    }
  }

  /// Automatically save progress with batching (non-blocking)
  Future<void> _autoSaveProgress(String comicId, int currentPage, int totalPages) async {
    try {
      // Use non-blocking progress save with batching
      progressPersistenceManager.saveProgress(
        comicId,
        currentPage,
        totalPages: totalPages,
        forceImmediate: false, // Use batching for performance
        isCompleted: currentPage >= totalPages - 1,
        metadata: {
          'autoSaved': true,
          'timestamp': DateTime.now().toIso8601String(),
        },
      ).catchError((e) {
        // Log but don't rethrow to avoid blocking UI
        developer.log(
          'Auto-save progress failed for comic $comicId: $e',
          name: 'ReaderBloc',
          level: 900, // Warning level
        );
      });
    } catch (e) {
      // Log but don't block UI for auto-save failures
      developer.log(
        'Auto-save progress failed for comic $comicId: $e',
        name: 'ReaderBloc',
        level: 900, // Warning level
      );
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
    
    try {
      developer.log(
        'Saving progress for comic ${currentState.comic.id} at page ${currentState.currentPageIndex}',
        name: 'ReaderBloc',
      );

      // Save reading progress using the progress persistence manager
      final result = await progressPersistenceManager.saveProgress(
        currentState.comic.id ?? '',
        currentState.currentPageIndex,
        totalPages: currentState.comic.pages?.length ?? 0,
        forceImmediate: event.forceImmediate,
        isCompleted: currentState.currentPageIndex >= (currentState.comic.pages?.length ?? 1) - 1,
        metadata: {
          'lastPageViewed': DateTime.now().toIso8601String(),
          'readingMode': currentState.readingMode.toString(),
          'zoomLevel': currentState.zoomScale,
        },
      );

      // Handle the result
      result.when(
        success: (progress) {
          developer.log(
            'Progress saved successfully for comic ${currentState.comic.id}',
            name: 'ReaderBloc',
          );
          
          // Update the state with the latest progress information
          emit(currentState.copyWith(
            lastSavedProgress: progress.currentPage,
            progressSaveTime: DateTime.now(),
          ));

          // Also update the old-style progress in the comic repository for backward compatibility
          _updateLegacyProgress(currentState.comic.id ?? '', currentState.currentPageIndex);
        },
        failure: (error) {
          developer.log(
            'Failed to save progress for comic ${currentState.comic.id}: ${error.userMessage}',
            name: 'ReaderBloc',
            level: 1000,
          );
          
          // For non-critical errors, try to retry after a delay
          if (error.isRetryable) {
            _scheduleProgressRetry(currentState.comic.id ?? '', currentState.currentPageIndex);
          }
          
          // Emit error state if this was a force immediate save (user-initiated)
          if (event.forceImmediate) {
            emit(ReaderError(message: '保存进度失败: ${error.userMessage}'));
            // Return to the previous loaded state after showing error
            Timer(const Duration(seconds: 3), () {
              if (state is ReaderError) {
                emit(currentState);
              }
            });
          }
        },
      );
      
      // Update reading time if session is active
      if (_sessionStartTime != null) {
        final sessionDuration = DateTime.now().difference(_sessionStartTime!).inSeconds;
        if (sessionDuration > 5) { // Only track sessions longer than 5 seconds
          await progressPersistenceManager.updateReadingTime(
            currentState.comic.id ?? '',
            sessionDuration,
          );
          _sessionStartTime = DateTime.now(); // Reset session start time
        }
      }
      
    } catch (e, stackTrace) {
      developer.log(
        'Unexpected error saving progress for comic ${currentState.comic.id}',
        name: 'ReaderBloc',
        error: e,
        stackTrace: stackTrace,
      );
      
      if (event.forceImmediate) {
        emit(ReaderError(message: '保存进度时发生意外错误'));
        // Return to the previous loaded state after showing error
        Timer(const Duration(seconds: 3), () {
          if (state is ReaderError) {
            emit(currentState);
          }
        });
      }
    }
  }

  /// Schedule a retry for progress saving after a delay
  void _scheduleProgressRetry(String comicId, int currentPage) {
    _progressSaveTimer?.cancel();
    _progressSaveTimer = Timer(const Duration(seconds: 5), () {
      add(SaveProgress((state as ReaderLoaded).currentPageIndex, forceImmediate: false));
    });
  }

  /// Update legacy progress for backward compatibility
  Future<void> _updateLegacyProgress(String comicId, int currentPage) async {
    try {
      // This would update the old progress field in the Comics table
      // Implementation depends on the existing comic repository structure
      developer.log(
        'Updating legacy progress for comic $comicId to page $currentPage',
        name: 'ReaderBloc',
      );
    } catch (e) {
      developer.log(
        'Failed to update legacy progress: $e',
        name: 'ReaderBloc',
        level: 900, // Warning level
      );
    }
  }

  /// Start tracking reading time for the current session
  void _startReadingTimeTracker(String comicId) {
    _readingTimeTracker?.cancel();
    _readingTimeTracker = Timer.periodic(const Duration(seconds: 30), (timer) {
      // Update reading time every 30 seconds
      if (_sessionStartTime != null) {
        final sessionDuration = DateTime.now().difference(_sessionStartTime!).inSeconds;
        if (sessionDuration >= 30) { // Only track meaningful reading sessions
          progressPersistenceManager.updateReadingTime(comicId, 30).catchError((e) {
            developer.log(
              'Failed to update reading time: $e',
              name: 'ReaderBloc',
              level: 900,
            );
          });
          _sessionStartTime = DateTime.now(); // Reset for next interval
        }
      }
    });
  }

  /// Stop reading time tracking
  void _stopReadingTimeTracker() {
    _readingTimeTracker?.cancel();
    _readingTimeTracker = null;
    _sessionStartTime = null;
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

  // Enhanced helper methods
  String _extractTitleFromPath(String filePath) {
    try {
      return filePath.split('/').last.split('.').first;
    } catch (e) {
      developer.log('Failed to extract title from path', name: 'ReaderBloc', level: 900, error: e);
      return 'Unknown Comic';
    }
  }

  bool _isPageBookmarked(int pageIndex, List<Bookmark> bookmarks) {
    return bookmarks.any((bookmark) => bookmark.pageIndex == pageIndex);
  }
  
  /// Validate image data to ensure it can be displayed
  bool _validateImageData(Uint8List imageData) {
    if (imageData.isEmpty) {
      return false;
    }
    
    // Basic image format validation based on headers
    // JPEG: FF D8 FF
    if (imageData.length >= 3 && 
        imageData[0] == 0xFF && imageData[1] == 0xD8 && imageData[2] == 0xFF) {
      return true;
    }
    
    // PNG: 89 50 4E 47 0D 0A 1A 0A
    if (imageData.length >= 8 &&
        imageData[0] == 0x89 && imageData[1] == 0x50 && imageData[2] == 0x4E && imageData[3] == 0x47 &&
        imageData[4] == 0x0D && imageData[5] == 0x0A && imageData[6] == 0x1A && imageData[7] == 0x0A) {
      return true;
    }
    
    // GIF: GIF87a or GIF89a
    if (imageData.length >= 6 &&
        imageData[0] == 0x47 && imageData[1] == 0x49 && imageData[2] == 0x46 &&
        imageData[3] == 0x38 && (imageData[4] == 0x37 || imageData[4] == 0x38) && imageData[5] == 0x61) {
      return true;
    }
    
    // BMP: BM
    if (imageData.length >= 2 && imageData[0] == 0x42 && imageData[1] == 0x4D) {
      return true;
    }
    
    // WebP: RIFF...WEBP
    if (imageData.length >= 12 &&
        imageData[0] == 0x52 && imageData[1] == 0x49 && imageData[2] == 0x46 && imageData[3] == 0x46 &&
        imageData[8] == 0x57 && imageData[9] == 0x45 && imageData[10] == 0x42 && imageData[11] == 0x50) {
      return true;
    }
    
    return false;
  }
  
  /// Get user-friendly error message
  String _getUserFriendlyError(dynamic error) {
    if (error == null) return 'Unknown error';
    
    final errorString = error.toString().toLowerCase();
    
    if (errorString.contains('permission')) {
      return 'Permission denied. Please check file access permissions.';
    }
    
    if (errorString.contains('not found') || errorString.contains('does not exist')) {
      return 'File not found or has been moved.';
    }
    
    if (errorString.contains('network') || errorString.contains('connection')) {
      return 'Network connection error. Please check your internet connection.';
    }
    
    if (errorString.contains('timeout')) {
      return 'Operation timed out. Please try again.';
    }
    
    if (errorString.contains('memory') || errorString.contains('out of memory')) {
      return 'Not enough memory. Please close other apps and try again.';
    }
    
    if (errorString.contains('storage') || errorString.contains('disk full')) {
      return 'Not enough storage space. Please free up some space.';
    }
    
    
    // For other errors, return the original message but limit length
    final message = error.toString();
    return message.length > 100 ? '${message.substring(0, 100)}...' : message;
  }
  
  /// Determine if an error can be retried
  bool _canRetryError(dynamic error) {
    if (error == null) return false;
    
    final errorString = error.toString().toLowerCase();
    
    // Don't retry permission, format, or file not found errors
    if (errorString.contains('permission') ||
        errorString.contains('not found') ||
        errorString.contains('unsupported format') ||
        errorString.contains('invalid')) {
      return false;
    }
    
    // Retry network, timeout, or temporary errors
    if (errorString.contains('network') ||
        errorString.contains('timeout') ||
        errorString.contains('connection') ||
        errorString.contains('temporarily')) {
      return true;
    }
    
    
    return true; // Default to retryable
  }

  Future<void> _preloadPages(String comicId, int currentPage, List<ComicPage> pages) async {
    try {
      // Only preload if we have a preloading service integrated
      // For now, we'll use a simple preloading strategy
      final preloadingService = PagePreloadingService(cacheService as IEnhancedCacheService);
      
      await preloadingService.startPreloading(
        comicId,
        currentPage,
        pages,
        PreloadingStrategy.adaptive, // Use adaptive strategy based on memory pressure
      );
      
      developer.log(
        'Started preloading for comic $comicId from page $currentPage',
        name: 'ReaderBloc',
      );
    } catch (e) {
      developer.log(
        'Failed to start preloading for comic $comicId: $e',
        name: 'ReaderBloc',
        level: 900,
      );
    }
  }

  Future<void> _preloadAdjacentPages(String comicId, int currentPage) async {
    try {
      if (cacheService is IEnhancedCacheService) {
        final enhancedCache = cacheService as IEnhancedCacheService;
        
        // Preload next 3 pages with high priority
        for (int i = 1; i <= 3; i++) {
          final pageIndex = currentPage + i;
          if (pageIndex < (state as ReaderLoaded).comic.totalPages!) {
            await enhancedCache.preloadPage(comicId, pageIndex, PreloadPriority.high);
          }
        }
        
        // Preload next 2 pages after that with medium priority
        for (int i = 4; i <= 5; i++) {
          final pageIndex = currentPage + i;
          if (pageIndex < (state as ReaderLoaded).comic.totalPages!) {
            await enhancedCache.preloadPage(comicId, pageIndex, PreloadPriority.medium);
          }
        }
        
        developer.log(
          'Preloaded adjacent pages for comic $comicId from page $currentPage',
          name: 'ReaderBloc',
        );
      } else {
        // Fallback for basic cache service
        developer.log(
          'Basic cache service detected, skipping advanced preloading',
          name: 'ReaderBloc',
          level: 500,
        );
      }
    } catch (e) {
      developer.log(
        'Failed to preload adjacent pages for comic $comicId: $e',
        name: 'ReaderBloc',
        level: 900,
      );
    }
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
        add(SaveProgress(currentState.currentPageIndex, forceImmediate: false));
      }
    });
  }

  @override
  Future<void> close() async {
    // Save any pending progress before closing
    try {
      await progressPersistenceManager.flushBatchBuffer();
    } catch (e) {
      developer.log(
        'Error flushing progress buffer on close: $e',
        name: 'ReaderBloc',
        level: 900,
      );
    }
    
    // Clean up timers and subscriptions
    _autoPageSubscription?.cancel();
    _progressSaveTimer?.cancel();
    _readingTimeTracker?.cancel();
    _stopReadingTimeTracker();
    _autoPageManager.dispose();
    
    return super.close();
  }
}