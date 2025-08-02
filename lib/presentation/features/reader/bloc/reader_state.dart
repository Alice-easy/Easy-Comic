// lib/presentation/features/reader/bloc/reader_state.dart
import 'package:equatable/equatable.dart';
import 'package:easy_comic/domain/entities/comic.dart';
import 'package:easy_comic/domain/entities/reader_settings.dart';
import 'package:easy_comic/domain/entities/bookmark.dart';

/// Enhanced error types for better error handling
enum ReaderErrorType {
  unknown,
  fileError,
  databaseError,
  networkError,
  memoryError,
  unsupportedFormat,
  corruptedFile,
  noValidImages,
  fileTooLarge,
  permissionDenied,
  invalidRequest,
}

abstract class ReaderState extends Equatable {
  const ReaderState();

  @override
  List<Object?> get props => [];
}

class ReaderInitial extends ReaderState {}

/// Enhanced loading state with progress information
class ReaderLoading extends ReaderState {
  final double? progress;
  final String? message;
  final String? operation;
  final Map<String, dynamic>? diagnostics;
  
  const ReaderLoading({
    this.progress,
    this.message,
    this.operation,
    this.diagnostics,
  });
  
  @override
  List<Object?> get props => [progress, message, operation, diagnostics];
}

/// Enhanced loaded state with diagnostic information
class ReaderLoaded extends ReaderState {
  final Comic comic;
  final int currentPageIndex;
  final bool isUIVisible;
  final bool isReversed;
  final bool isAutoPageTurnEnabled;
  final Duration autoPageInterval;
  final bool isFullscreen;
  final double brightness;
  final double zoomLevel;
  final ReaderSettings settings;
  final List<Bookmark> bookmarks;
  final bool isCurrentPageBookmarked;
  final Duration? loadDuration;
  final Map<String, dynamic>? diagnostics;
  final Map<String, dynamic>? performanceMetrics;
  
  // 新增缺失的属性
  final ReadingMode readingMode;
  final double zoomScale;
  final int? lastSavedProgress;
  final DateTime? progressSaveTime;

  const ReaderLoaded({
    required this.comic,
    required this.currentPageIndex,
    required this.settings,
    required this.bookmarks,
    this.isUIVisible = true,
    this.isReversed = false,
    this.isAutoPageTurnEnabled = false,
    this.autoPageInterval = const Duration(seconds: 5),
    this.isFullscreen = false,
    this.brightness = 1.0,
    this.zoomLevel = 1.0,
    this.isCurrentPageBookmarked = false,
    this.loadDuration,
    this.diagnostics,
    this.performanceMetrics,
    this.readingMode = ReadingMode.SinglePage,
    this.zoomScale = 1.0,
    this.lastSavedProgress,
    this.progressSaveTime,
  });

  ReaderLoaded copyWith({
    Comic? comic,
    int? currentPageIndex,
    bool? isUIVisible,
    bool? isReversed,
    bool? isAutoPageTurnEnabled,
    Duration? autoPageInterval,
    bool? isFullscreen,
    double? brightness,
    double? zoomLevel,
    ReaderSettings? settings,
    List<Bookmark>? bookmarks,
    bool? isCurrentPageBookmarked,
    Duration? loadDuration,
    Map<String, dynamic>? diagnostics,
    Map<String, dynamic>? performanceMetrics,
    ReadingMode? readingMode,
    double? zoomScale,
    int? lastSavedProgress,
    DateTime? progressSaveTime,
  }) {
    return ReaderLoaded(
      comic: comic ?? this.comic,
      currentPageIndex: currentPageIndex ?? this.currentPageIndex,
      isUIVisible: isUIVisible ?? this.isUIVisible,
      isReversed: isReversed ?? this.isReversed,
      isAutoPageTurnEnabled: isAutoPageTurnEnabled ?? this.isAutoPageTurnEnabled,
      autoPageInterval: autoPageInterval ?? this.autoPageInterval,
      isFullscreen: isFullscreen ?? this.isFullscreen,
      brightness: brightness ?? this.brightness,
      zoomLevel: zoomLevel ?? this.zoomLevel,
      settings: settings ?? this.settings,
      bookmarks: bookmarks ?? this.bookmarks,
      isCurrentPageBookmarked: isCurrentPageBookmarked ?? this.isCurrentPageBookmarked,
      loadDuration: loadDuration ?? this.loadDuration,
      diagnostics: diagnostics ?? this.diagnostics,
      performanceMetrics: performanceMetrics ?? this.performanceMetrics,
      readingMode: readingMode ?? this.readingMode,
      zoomScale: zoomScale ?? this.zoomScale,
      lastSavedProgress: lastSavedProgress ?? this.lastSavedProgress,
      progressSaveTime: progressSaveTime ?? this.progressSaveTime,
    );
  }

  @override
  List<Object?> get props => [
        comic,
        currentPageIndex,
        isUIVisible,
        isReversed,
        isAutoPageTurnEnabled,
        autoPageInterval,
        isFullscreen,
        brightness,
        zoomLevel,
        settings,
        bookmarks,
        isCurrentPageBookmarked,
        loadDuration,
        diagnostics,
        performanceMetrics,
        readingMode,
        zoomScale,
        lastSavedProgress,
        progressSaveTime,
      ];
}

/// Enhanced error state with detailed error information
class ReaderError extends ReaderState {
  final String message;
  final ReaderErrorType errorType;
  final bool canRetry;
  final String? details;
  final dynamic originalError;
  final Map<String, dynamic>? diagnostics;
  final List<String>? suggestedActions;

  const ReaderError({
    required this.message,
    this.errorType = ReaderErrorType.unknown,
    this.canRetry = false,
    this.details,
    this.originalError,
    this.diagnostics,
    this.suggestedActions,
  });
  
  /// Create error with recovery suggestions
  ReaderError.withSuggestions({
    required this.message,
    required this.errorType,
    required List<String> suggestions,
    this.canRetry = true,
    this.details,
    this.originalError,
    this.diagnostics,
  }) : suggestedActions = suggestions;

  @override
  List<Object?> get props => [message, errorType, canRetry, details, originalError, diagnostics, suggestedActions];
}