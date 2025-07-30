import 'package:equatable/equatable.dart';

import '../../models/reader_models.dart';

// Reader status enum
enum ReaderStatus { initial, loading, success, failure }

class ReaderState extends Equatable {
  final ComicInfo? comic;
  final ComicInfo? comicDetail;
  final List<ComicPage> pages;
  final int currentPage;
  final int totalPages;
  final List<Bookmark> bookmarks;
  final ReaderStatus status;
  final bool isLoading;
  final String? error;
  final String? errorMessage;
  final ReaderSettings settings;
  final bool isUiVisible;
  final double zoomScale;

  const ReaderState({
    this.comic,
    this.comicDetail,
    this.pages = const [],
    this.currentPage = 0,
    this.totalPages = 0,
    this.bookmarks = const [],
    this.status = ReaderStatus.initial,
    this.isLoading = true,
    this.error,
    this.errorMessage,
    this.settings = const ReaderSettings(),
    this.isUiVisible = true,
    this.zoomScale = 1.0,
  });

  ReaderState copyWith({
    ComicInfo? comic,
    ComicInfo? comicDetail,
    List<ComicPage>? pages,
    int? currentPage,
    int? totalPages,
    List<Bookmark>? bookmarks,
    ReaderStatus? status,
    bool? isLoading,
    String? error,
    String? errorMessage,
    ReaderSettings? settings,
    bool? isUiVisible,
    double? zoomScale,
  }) => ReaderState(
        comic: comic ?? this.comic,
        comicDetail: comicDetail ?? this.comicDetail,
        pages: pages ?? this.pages,
        currentPage: currentPage ?? this.currentPage,
        totalPages: totalPages ?? this.totalPages,
        bookmarks: bookmarks ?? this.bookmarks,
        status: status ?? this.status,
        isLoading: isLoading ?? this.isLoading,
        error: error ?? this.error,
        errorMessage: errorMessage ?? this.errorMessage,
        settings: settings ?? this.settings,
        isUiVisible: isUiVisible ?? this.isUiVisible,
        zoomScale: zoomScale ?? this.zoomScale,
      );

  @override
  List<Object?> get props => [
        comic,
        comicDetail,
        pages,
        currentPage,
        totalPages,
        bookmarks,
        status,
        isLoading,
        error,
        errorMessage,
        settings,
        isUiVisible,
        zoomScale,
      ];
}