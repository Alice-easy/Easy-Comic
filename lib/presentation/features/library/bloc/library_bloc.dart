import 'package:bloc/bloc.dart';
import 'package:easy_comic/core/services/file_picker_service.dart';
import 'package:easy_comic/core/usecases/usecase.dart';
import 'package:easy_comic/domain/entities/manga.dart';
import 'package:easy_comic/domain/usecases/delete_manga_usecase.dart';
import 'package:easy_comic/domain/usecases/get_library_mangas_usecase.dart';
import 'package:easy_comic/domain/usecases/import_manga_usecase.dart';
import 'package:easy_comic/domain/usecases/toggle_favorite_usecase.dart';
import 'package:equatable/equatable.dart';

part 'library_event.dart';
part 'library_state.dart';

class LibraryBloc extends Bloc<LibraryEvent, LibraryState> {
  final GetLibraryMangasUseCase getLibraryMangas;
  final ImportMangaUseCase importManga;
  final DeleteMangaUseCase deleteMangaUseCase;
  final ToggleFavoriteUseCase toggleFavoriteUseCase;
  final FilePickerService filePickerService;

  LibraryBloc({
    required this.getLibraryMangas,
    required this.importManga,
    required this.deleteMangaUseCase,
    required this.toggleFavoriteUseCase,
    required this.filePickerService,
  }) : super(LibraryInitial()) {
    on<LoadLibrary>(_onLoadLibrary);
    on<ImportManga>(_onImportManga);
    on<SearchQueryChanged>(_onSearchQueryChanged);
    on<SortOrderChanged>(_onSortOrderChanged);
    on<DeleteManga>(_onDeleteManga);
    on<ToggleFavorite>(_onToggleFavorite);
  }

  Future<void> _onLoadLibrary(LoadLibrary event, Emitter<LibraryState> emit) async {
    emit(LibraryLoading());
    final failureOrMangas = await getLibraryMangas(NoParams());
    failureOrMangas.fold(
      (failure) => emit(LibraryError(message: failure.toString())),
      (mangas) => emit(LibraryLoaded(allMangas: mangas, displayedMangas: mangas)),
    );
  }

  Future<void> _onImportManga(ImportManga event, Emitter<LibraryState> emit) async {
    final file = await filePickerService.pickFile();
    if (file != null) {
      emit(LibraryLoading());
      final failureOrManga = await importManga(ImportMangaParams(file: file));
      failureOrManga.fold(
        (failure) => emit(LibraryError(message: failure.toString())),
        (manga) => add(LoadLibrary()),
      );
    }
  }

  void _onSearchQueryChanged(SearchQueryChanged event, Emitter<LibraryState> emit) {
    if (state is LibraryLoaded) {
      final currentState = state as LibraryLoaded;
      final query = event.query.toLowerCase();
      final filteredMangas = currentState.allMangas
          .where((manga) => manga.title.toLowerCase().contains(query))
          .toList();
      emit(currentState.copyWith(displayedMangas: filteredMangas, searchQuery: query));
    }
  }

  void _onSortOrderChanged(SortOrderChanged event, Emitter<LibraryState> emit) {
    if (state is LibraryLoaded) {
      final currentState = state as LibraryLoaded;
      final sortedMangas = List<Manga>.from(currentState.displayedMangas);
      if (event.sortType == SortType.title) {
        sortedMangas.sort((a, b) => a.title.compareTo(b.title));
      } else {
        sortedMangas.sort((a, b) => b.dateAdded.compareTo(a.dateAdded));
      }
      emit(currentState.copyWith(displayedMangas: sortedMangas, sortType: event.sortType));
    }
  }

  Future<void> _onDeleteManga(DeleteManga event, Emitter<LibraryState> emit) async {
    final failureOrSuccess = await deleteMangaUseCase(event.mangaId);
    failureOrSuccess.fold(
      (failure) => emit(LibraryError(message: failure.toString())),
      (_) => add(LoadLibrary()),
    );
  }

  Future<void> _onToggleFavorite(ToggleFavorite event, Emitter<LibraryState> emit) async {
    final failureOrSuccess = await toggleFavoriteUseCase(event.mangaId);
    failureOrSuccess.fold(
      (failure) => emit(LibraryError(message: failure.toString())),
      (_) => add(LoadLibrary()),
    );
  }
}