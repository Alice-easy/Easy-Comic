import 'package:bloc/bloc.dart';
import 'package:easy_comic/domain/entities/manga.dart';
import 'package:easy_comic/domain/repositories/settings_repository.dart';
import 'package:easy_comic/domain/usecases/get_manga_details_usecase.dart';
import 'package:equatable/equatable.dart';

part 'reader_event.dart';
part 'reader_state.dart';

class ReaderBloc extends Bloc<ReaderEvent, ReaderState> {
  final GetMangaDetailsUseCase getMangaDetailsUseCase;
  final SettingsRepository settingsRepository;

  ReaderBloc({
    required this.getMangaDetailsUseCase,
    required this.settingsRepository,
  }) : super(ReaderInitial()) {
    on<LoadMangaEvent>(_onLoadManga);
    on<PageChangedEvent>(_onPageChanged);
    on<ToggleOverlayEvent>(_onToggleOverlay);
    on<ZoomChangedEvent>(_onZoomChanged);
    on<ReadingModeChangedEvent>(_onReadingModeChanged);
  }

  Future<void> _onLoadManga(LoadMangaEvent event, Emitter<ReaderState> emit) async {
    emit(ReaderLoading());
    final settings = await settingsRepository.getSettings();
    final failureOrManga = await getMangaDetailsUseCase(event.mangaId);
    failureOrManga.fold(
      (failure) => emit(ReaderError(failure.toString())),
      (manga) => emit(ReaderLoaded(
        manga: manga,
        currentPage: manga.currentPage,
        readingMode: settings.readingMode,
      )),
    );
  }

  void _onPageChanged(PageChangedEvent event, Emitter<ReaderState> emit) {
    if (state is ReaderLoaded) {
      final currentState = state as ReaderLoaded;
      emit(currentState.copyWith(currentPage: event.pageIndex));
      // Here you would also update the database
    }
  }

  void _onToggleOverlay(ToggleOverlayEvent event, Emitter<ReaderState> emit) {
    if (state is ReaderLoaded) {
      final currentState = state as ReaderLoaded;
      emit(currentState.copyWith(isOverlayVisible: !currentState.isOverlayVisible));
    }
  }

  void _onZoomChanged(ZoomChangedEvent event, Emitter<ReaderState> emit) {
    if (state is ReaderLoaded) {
      final currentState = state as ReaderLoaded;
      emit(currentState.copyWith(scale: event.scale));
    }
  }

  void _onReadingModeChanged(ReadingModeChangedEvent event, Emitter<ReaderState> emit) {
    if (state is ReaderLoaded) {
      final currentState = state as ReaderLoaded;
      emit(currentState.copyWith(readingMode: event.readingMode));
    }
  }
}

extension ReaderLoadedExt on ReaderLoaded {
  ReaderLoaded copyWith({
    Manga? manga,
    int? currentPage,
    ReadingMode? readingMode,
    double? scale,
    bool? isOverlayVisible,
  }) {
    return ReaderLoaded(
      manga: manga ?? this.manga,
      currentPage: currentPage ?? this.currentPage,
      readingMode: readingMode ?? this.readingMode,
      scale: scale ?? this.scale,
      isOverlayVisible: isOverlayVisible ?? this.isOverlayVisible,
    );
  }
}