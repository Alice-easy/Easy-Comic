import 'dart:typed_data';
import 'package:bloc_test/bloc_test.dart';
import 'package:easy_comic/core/error/failures.dart';
import 'package:easy_comic/core/services/unified_manga_importer_service.dart';
import 'package:easy_comic/domain/entities/bookmark.dart';
import 'package:easy_comic/domain/entities/comic.dart';
import 'package:easy_comic/domain/entities/comic_page.dart';
import 'package:easy_comic/domain/entities/reader_settings.dart';
import 'package:easy_comic/domain/entities/comic_progress.dart';
import 'package:easy_comic/domain/repositories/bookmark_repository.dart';
import 'package:easy_comic/domain/repositories/comic_repository.dart';
import 'package:easy_comic/domain/repositories/settings_repository.dart';
import 'package:easy_comic/domain/services/auto_page_service.dart';
import 'package:easy_comic/domain/services/cache_service.dart';
import 'package:easy_comic/domain/services/progress_persistence_manager.dart';
import 'package:easy_comic/presentation/features/reader/bloc/reader_bloc.dart';
import 'package:easy_comic/presentation/features/reader/bloc/reader_event.dart';
import 'package:easy_comic/presentation/features/reader/bloc/reader_state.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:dartz/dartz.dart' as dartz;
import 'package:mocktail/mocktail.dart';

// Mocks
class MockComicRepository extends Mock implements ComicRepository {}
class MockSettingsRepository extends Mock implements SettingsRepository {}
class MockBookmarkRepository extends Mock implements BookmarkRepository {}
class MockAutoPageService extends Mock implements AutoPageService {}
class MockCacheService extends Mock implements CacheService {}
class MockUnifiedMangaImporterService extends Mock implements UnifiedMangaImporterService {}
class MockProgressPersistenceManager extends Mock implements IProgressPersistenceManager {}

void main() {
  group('ReaderBloc', () {
    late ReaderBloc readerBloc;
    late MockComicRepository mockComicRepository;
    late MockSettingsRepository mockSettingsRepository;
    late MockBookmarkRepository mockBookmarkRepository;
    late MockAutoPageService mockAutoPageService;
    late MockCacheService mockCacheService;
    late MockUnifiedMangaImporterService mockMangaImporter;
    late MockProgressPersistenceManager mockProgressManager;

    final testComic = Comic(
      id: 'test_comic_1',
      title: 'Test Comic',
      path: '/test/path/comic.cbz',
      filePath: '/test/path/comic.cbz',
      fileName: 'comic.cbz',
      coverPath: '',
      pageCount: 10,
      addTime: DateTime.now(),
      lastReadTime: DateTime.now(),
      progress: 0,
      bookshelfId: 1,
      isFavorite: false,
      tags: [],
      metadata: {},
      author: 'Test Author',
    );

    const testSettings = ReaderSettings();

    setUp(() {
      mockComicRepository = MockComicRepository();
      mockSettingsRepository = MockSettingsRepository();
      mockBookmarkRepository = MockBookmarkRepository();
      mockAutoPageService = MockAutoPageService();
      mockCacheService = MockCacheService();
      mockMangaImporter = MockUnifiedMangaImporterService();
      mockProgressManager = MockProgressPersistenceManager();

      when(() => mockSettingsRepository.getReaderSettings())
          .thenAnswer((_) async => const dartz.Right(testSettings));
      when(() => mockBookmarkRepository.getBookmarksForComic(any()))
          .thenAnswer((_) async => const dartz.Right([]));
      when(() => mockProgressManager.loadProgress(any()))
          .thenAnswer((_) async => const ProgressResult.failure(ProgressError.loadFailed('Not found')));

      readerBloc = ReaderBloc(
        comicRepository: mockComicRepository,
        settingsRepository: mockSettingsRepository,
        bookmarkRepository: mockBookmarkRepository,
        autoPageService: mockAutoPageService,
        cacheService: mockCacheService,
        mangaImporter: mockMangaImporter,
        progressPersistenceManager: mockProgressManager,
      );
    });


    group('LoadMangaEvent', () {
      blocTest<ReaderBloc, ReaderState>(
        'emits [ReaderLoading, ReaderLoaded] when LoadMangaEvent is added and import is successful.',
        build: () {
          when(() => mockMangaImporter.importFromPath(any())).thenAnswer((_) async => [Uint8List(1)]);
          return readerBloc;
        },
        act: (bloc) => bloc.add(const LoadMangaEvent('/fake/path.zip')),
        expect: () => [
          isA<ReaderLoading>(),
          isA<ReaderLoaded>(),
        ],
        verify: (_) {
          verify(() => mockMangaImporter.importFromPath(any())).called(1);
        },
      );

      blocTest<ReaderBloc, ReaderState>(
        'emits [ReaderLoading, ReaderError] when UnifiedMangaImporter throws an exception.',
        build: () {
          when(() => mockMangaImporter.importFromPath(any())).thenThrow(Exception('Import failed'));
          return readerBloc;
        },
        act: (bloc) => bloc.add(const LoadMangaEvent('/fake/path.zip')),
        expect: () => [
          isA<ReaderLoading>(),
          isA<ReaderError>().having((e) => e.message, 'message', contains('Import failed')),
        ],
      );

      blocTest<ReaderBloc, ReaderState>(
        'triggers re-import when loaded comic from DB has no pages.',
        build: () {
          final comicWithNoPages = testComic.copyWith(pages: []);
          when(() => mockComicRepository.getComicById(any())).thenAnswer((_) async => dartz.Right(comicWithNoPages));
          when(() => mockMangaImporter.importFromPath(any())).thenAnswer((_) async => [Uint8List(1)]);
          return readerBloc;
        },
        act: (bloc) => bloc.add(const LoadComic(comicId: 'test_comic_1')),
        expect: () => [
          isA<ReaderLoading>(),
          isA<ReaderLoaded>(),
        ],
        verify: (_) {
          verify(() => mockMangaImporter.importFromPath(testComic.filePath)).called(1);
        },
      );
    });
  });
}