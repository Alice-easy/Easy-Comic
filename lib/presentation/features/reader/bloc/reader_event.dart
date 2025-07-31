// lib/presentation/features/reader/bloc/reader_event.dart
import 'package:equatable/equatable.dart';

abstract class ReaderEvent extends Equatable {
  const ReaderEvent();

  @override
  List<Object> get props => [];
}

class LoadComic extends ReaderEvent {
  final String? filePath;
  final int? comicId;

  const LoadComic({this.filePath, this.comicId});

  @override
  List<Object> get props => [filePath ?? '', comicId ?? 0];
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