part of 'reader_bloc.dart';

abstract class ReaderEvent extends Equatable {
  const ReaderEvent();

  @override
  List<Object> get props => [];
}

class LoadMangaEvent extends ReaderEvent {
  final String mangaId;

  const LoadMangaEvent(this.mangaId);

  @override
  List<Object> get props => [mangaId];
}

class PageChangedEvent extends ReaderEvent {
  final int pageIndex;

  const PageChangedEvent(this.pageIndex);

  @override
  List<Object> get props => [pageIndex];
}

class ToggleOverlayEvent extends ReaderEvent {}

class ZoomChangedEvent extends ReaderEvent {
  final double scale;

  const ZoomChangedEvent(this.scale);

  @override
  List<Object> get props => [scale];
}

class ReadingModeChangedEvent extends ReaderEvent {
  final ReadingMode readingMode;

  const ReadingModeChangedEvent(this.readingMode);

  @override
  List<Object> get props => [readingMode];
}