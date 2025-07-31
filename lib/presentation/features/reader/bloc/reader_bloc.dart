// lib/presentation/features/reader/bloc/reader_bloc.dart
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:easy_comic/domain/repositories/comic_repository.dart';
import 'package:easy_comic/domain/repositories/settings_repository.dart';
import 'reader_event.dart';
import 'reader_state.dart';

class ReaderBloc extends Bloc<ReaderEvent, ReaderState> {
  final ComicRepository comicRepository;
  final SettingsRepository settingsRepository;

  ReaderBloc({required this.comicRepository, required this.settingsRepository}) : super(ReaderInitial()) {
    on<LoadComic>(_onLoadComic);
  }

  Future<void> _onLoadComic(LoadComic event, Emitter<ReaderState> emit) async {
    // TODO: Implement comic loading logic
  }
}