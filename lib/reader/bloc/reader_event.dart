import 'package:equatable/equatable.dart';
import '../../models/reader_models.dart'; // Assuming models are defined here

abstract class ReaderEvent extends Equatable {
  const ReaderEvent();

  @override
  List<Object?> get props => [];
}

class LoadComicEvent extends ReaderEvent {
  final String comicId;

  const LoadComicEvent({required this.comicId});

  @override
  List<Object?> get props => [comicId];
}

class PageChangedEvent extends ReaderEvent {
  final int newPage;

  const PageChangedEvent({required this.newPage});

  @override
  List<Object?> get props => [newPage];
}

class ZoomChangedEvent extends ReaderEvent {
  final double newScale;

  const ZoomChangedEvent({required this.newScale});

  @override
  List<Object?> get props => [newScale];
}

class ToggleUiVisibilityEvent extends ReaderEvent {}

class UpdateSettingEvent extends ReaderEvent {
  final ReaderSettings newSettings;

  const UpdateSettingEvent({required this.newSettings});

  @override
  List<Object?> get props => [newSettings];
}

class AddBookmarkEvent extends ReaderEvent {
  final String? label;

  const AddBookmarkEvent({this.label});

  @override
  List<Object?> get props => [label];
}

class DeleteBookmarkEvent extends ReaderEvent {
  final int bookmarkId;

  const DeleteBookmarkEvent({required this.bookmarkId});

  @override
  List<Object?> get props => [bookmarkId];
}