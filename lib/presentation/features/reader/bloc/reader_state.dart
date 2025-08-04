part of 'reader_bloc.dart';

enum ReadingMode { SinglePage, DoublePage, LongStrip }

abstract class ReaderState extends Equatable {
  const ReaderState();

  @override
  List<Object> get props => [];
}

class ReaderInitial extends ReaderState {}

class ReaderLoading extends ReaderState {}

class ReaderLoaded extends ReaderState {
  final Manga manga;
  final int currentPage;
  final ReadingMode readingMode;
  final double scale;
  final bool isOverlayVisible;

  const ReaderLoaded({
    required this.manga,
    required this.currentPage,
    this.readingMode = ReadingMode.SinglePage,
    this.scale = 1.0,
    this.isOverlayVisible = true,
  });

  @override
  List<Object> get props => [manga, currentPage, readingMode, scale, isOverlayVisible];
}

class ReaderError extends ReaderState {
  final String message;

  const ReaderError(this.message);

  @override
  List<Object> get props => [message];
}