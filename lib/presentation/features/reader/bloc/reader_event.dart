// lib/presentation/features/reader/bloc/reader_event.dart
import 'package:equatable/equatable.dart';
import 'package:easy_comic/domain/entities/reader_settings.dart';

abstract class ReaderEvent extends Equatable {
  const ReaderEvent();

  @override
  List<Object> get props => [];
}

class LoadComic extends ReaderEvent {
  final String? filePath;
  final String? comicId;

  const LoadComic({this.filePath, this.comicId});

  @override
  List<Object> get props => [filePath ?? '', comicId ?? ''];
}

class LoadMangaEvent extends ReaderEvent {
  final String path;

  const LoadMangaEvent(this.path);

  @override
  List<Object> get props => [path];
}

class PageChanged extends ReaderEvent {
  final int newIndex;

  const PageChanged(this.newIndex);

  @override
  List<Object> get props => [newIndex];
}

class PreviousPage extends ReaderEvent {}

class NextPage extends ReaderEvent {}

class ToggleUIVisibility extends ReaderEvent {}

class ToggleAutoPageTurn extends ReaderEvent {}

class UpdateAutoPageInterval extends ReaderEvent {
  final Duration interval;

  const UpdateAutoPageInterval(this.interval);

  @override
  List<Object> get props => [interval];
}

class ChangeReadingMode extends ReaderEvent {
  final ReadingMode mode;

  const ChangeReadingMode(this.mode);

  @override
  List<Object> get props => [mode];
}

class ToggleFullscreen extends ReaderEvent {}

class UpdateBrightness extends ReaderEvent {
  final double brightness;

  const UpdateBrightness(this.brightness);

  @override
  List<Object> get props => [brightness];
}

class ZoomChanged extends ReaderEvent {
  final double zoomLevel;

  const ZoomChanged(this.zoomLevel);

  @override
  List<Object> get props => [zoomLevel];
}

class HandleGesture extends ReaderEvent {
  final GestureType gestureType;

  const HandleGesture(this.gestureType);

  @override
  List<Object> get props => [gestureType];
}

class SaveProgress extends ReaderEvent {
  final int pageIndex;
  final bool forceImmediate;

  const SaveProgress(this.pageIndex, {this.forceImmediate = false});

  @override
  List<Object> get props => [pageIndex, forceImmediate];
}

class AddBookmark extends ReaderEvent {
  final int pageIndex;

  const AddBookmark(this.pageIndex);

  @override
  List<Object> get props => [pageIndex];
}

class RemoveBookmark extends ReaderEvent {
  final int pageIndex;

  const RemoveBookmark(this.pageIndex);

  @override
  List<Object> get props => [pageIndex];
}

class LoadSettings extends ReaderEvent {}

class UpdateSettings extends ReaderEvent {
  final ReaderSettings settings;

  const UpdateSettings(this.settings);

  @override
  List<Object> get props => [settings];
}

enum GestureType {
  tapLeft,
  tapRight,
  tapCenter,
  doubleTap,
  longPress,
  swipeLeft,
  swipeRight,
  swipeUp,
  swipeDown,
  pinchIn,
  pinchOut,
}