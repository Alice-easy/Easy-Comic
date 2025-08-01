// lib/presentation/features/reader/bloc/reader_state.dart
import 'package:equatable/equatable.dart';
import 'package:easy_comic/domain/entities/comic.dart';
import 'package:easy_comic/domain/entities/reader_settings.dart';
import 'package:easy_comic/domain/entities/bookmark.dart';

abstract class ReaderState extends Equatable {
  const ReaderState();

  @override
  List<Object> get props => [];
}

class ReaderInitial extends ReaderState {}

class ReaderLoading extends ReaderState {}

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
    );
  }

  @override
  List<Object> get props => [
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
      ];
}

class ReaderError extends ReaderState {
  final String message;

  const ReaderError({required this.message});

  @override
  List<Object> get props => [message];
}