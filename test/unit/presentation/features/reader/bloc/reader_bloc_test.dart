import 'dart:async';
import 'dart:typed_data';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:easy_comic/presentation/features/reader/bloc/reader_bloc.dart';
import 'package:easy_comic/presentation/features/reader/bloc/reader_event.dart';
import 'package:easy_comic/presentation/features/reader/bloc/reader_state.dart';
import 'package:easy_comic/domain/repositories/comic_repository.dart';
import 'package:easy_comic/domain/repositories/settings_repository.dart';
import 'package:easy_comic/domain/repositories/bookmark_repository.dart';
import 'package:easy_comic/domain/services/auto_page_service.dart';
import 'package:easy_comic/domain/services/cache_service.dart';
import 'package:easy_comic/domain/entities/comic.dart';
import 'package:easy_comic/domain/entities/comic_page.dart';
import 'package:easy_comic/domain/entities/reader_settings.dart';
import 'package:easy_comic/domain/entities/bookmark.dart';
import 'package:easy_comic/domain/entities/gesture_types.dart';
import 'package:easy_comic/core/comic_archive.dart';
import 'package:easy_comic/core/utils/either.dart';
import 'package:easy_comic/core/error/failures.dart';

// Mock classes
class MockComicRepository extends Mock implements ComicRepository {}
class MockSettingsRepository extends Mock implements SettingsRepository {}
class MockBookmarkRepository extends Mock implements BookmarkRepository {}
class MockAutoPageService extends Mock implements AutoPageService {}
class MockCacheService extends Mock implements CacheService {}
class MockComicArchive extends Mock implements ComicArchive {}

void main() {
  group('ReaderBloc', () {
    late ReaderBloc readerBloc;
    late MockComicRepository mockComicRepository;
    late MockSettingsRepository mockSettingsRepository;
    late MockBookmarkRepository mockBookmarkRepository;
    late MockAutoPageService mockAutoPageService;
    late MockCacheService mockCacheService;
    late MockComicArchive mockComicArchive;

    // Test data
    final testComic = Comic(
      id: 'test_comic_1',
      title: 'Test Comic',
      filePath: '/test/path/comic.cbz',
      totalPages: 10,
      createdAt: DateTime.now(),
    );

    final testPages = List.generate(10, (index) => ComicPage(
      id: 'page_$index',
      imagePath: 'page_$index.jpg',
      pageNumber: index,
      imageData: Uint8List.fromList([index]),
    ));

    const testSettings = ReaderSettings(
      readingMode: ReadingMode.leftToRight,
      enableFullscreen: true,
      brightness: 0.8,
      autoPageInterval: 3,
    );

    final testBookmarks = [
      Bookmark(
        comicId: 'test_comic_1',
        pageIndex: 2,
        createdAt: DateTime.now(),
      ),
      Bookmark(
        comicId: 'test_comic_1',
        pageIndex: 5,
        createdAt: DateTime.now(),
      ),
    ];

    setUpAll(() {
      registerFallbackValue(testComic);
      registerFallbackValue(testSettings);
      registerFallbackValue(testBookmarks.first);
    });

    setUp(() {
      mockComicRepository = MockComicRepository();
      mockSettingsRepository = MockSettingsRepository();
      mockBookmarkRepository = MockBookmarkRepository();
      mockAutoPageService = MockAutoPageService();
      mockCacheService = MockCacheService();
      mockComicArchive = MockComicArchive();

      // Setup default mock responses
      when(() => mockComicRepository.getComicById(any()))
          .thenAnswer((_) async => Right(testComic));
      when(() => mockSettingsRepository.getReaderSettings())
          .thenAnswer((_) async => Right(testSettings));
      when(() => mockBookmarkRepository.getBookmarksForComic(any()))
          .thenAnswer((_) async => Right(testBookmarks));
      when(() => mockCacheService.preloadPages(any(), any(), any()))
          .thenAnswer((_) async {});
      when(() => mockComicArchive.extractPages(any()))
          .thenAnswer((_) async => testPages);

      readerBloc = ReaderBloc(
        comicRepository: mockComicRepository,
        settingsRepository: mockSettingsRepository,
        bookmarkRepository: mockBookmarkRepository,
        autoPageService: mockAutoPageService,
        cacheService: mockCacheService,
        comicArchive: mockComicArchive,
      );
    });

    tearDown(() {
      readerBloc.close();
    });

    group('LoadComic', () {
      test('should emit ReaderLoaded when comic loads successfully by ID', () async {
        // Arrange
        const comicId = 'test_comic_1';

        // Act
        readerBloc.add(LoadComic(comicId: comicId));

        // Assert
        await expectLater(
          readerBloc.stream,
          emitsInOrder([
            isA<ReaderLoading>(),
            isA<ReaderLoaded>()
                .having((state) => state.comic.id, 'comic.id', comicId)
                .having((state) => state.settings, 'settings', testSettings)
                .having((state) => state.bookmarks, 'bookmarks', testBookmarks),
          ]),
        );

        verify(() => mockComicRepository.getComicById(comicId)).called(1);
        verify(() => mockSettingsRepository.getReaderSettings()).called(1);
        verify(() => mockBookmarkRepository.getBookmarksForComic(comicId)).called(1);
      });

      test('should emit ReaderLoaded when comic loads successfully by file path', () async {
        // Arrange
        const filePath = '/test/path/comic.cbz';

        // Act
        readerBloc.add(LoadComic(filePath: filePath));

        // Assert
        await expectLater(
          readerBloc.stream,
          emitsInOrder([
            isA<ReaderLoading>(),
            isA<ReaderLoaded>()
                .having((state) => state.comic.filePath, 'comic.filePath', filePath),
          ]),
        );

        verify(() => mockComicArchive.extractPages(filePath)).called(1);
        verify(() => mockSettingsRepository.getReaderSettings()).called(1);
      });

      test('should emit ReaderError when comic repository fails', () async {
        // Arrange
        when(() => mockComicRepository.getComicById(any()))
            .thenAnswer((_) async => Left(Failure('Comic not found')));

        // Act
        readerBloc.add(LoadComic(comicId: 'invalid_id'));

        // Assert
        await expectLater(
          readerBloc.stream,
          emitsInOrder([
            isA<ReaderLoading>(),
            isA<ReaderError>()
                .having((state) => state.message, 'message', contains('Comic not found')),
          ]),
        );
      });

      test('should emit ReaderError when no comic ID or file path provided', () async {
        // Act
        readerBloc.add(LoadComic());

        // Assert
        await expectLater(
          readerBloc.stream,
          emitsInOrder([
            isA<ReaderLoading>(),
            isA<ReaderError>()
                .having((state) => state.message, 'message', contains('No comic ID or file path')),
          ]),
        );
      });

      test('should use default settings when settings repository fails', () async {
        // Arrange
        when(() => mockSettingsRepository.getReaderSettings())
            .thenAnswer((_) async => Left(Failure('Settings not found')));

        // Act
        readerBloc.add(LoadComic(comicId: 'test_comic_1'));

        // Assert
        await expectLater(
          readerBloc.stream,
          emitsInOrder([
            isA<ReaderLoading>(),
            isA<ReaderLoaded>()
                .having((state) => state.settings, 'settings', const ReaderSettings()),
          ]),
        );
      });

      test('should handle empty bookmarks when bookmark repository fails', () async {
        // Arrange
        when(() => mockBookmarkRepository.getBookmarksForComic(any()))
            .thenAnswer((_) async => Left(Failure('Bookmarks not found')));

        // Act
        readerBloc.add(LoadComic(comicId: 'test_comic_1'));

        // Assert
        await expectLater(
          readerBloc.stream,
          emitsInOrder([
            isA<ReaderLoading>(),
            isA<ReaderLoaded>()
                .having((state) => state.bookmarks, 'bookmarks', isEmpty),
          ]),
        );
      });
    });

    group('Page Navigation', () {
      test('should navigate to next page', () async {
        // Arrange - Load comic first
        readerBloc.add(LoadComic(comicId: 'test_comic_1'));
        await readerBloc.stream.firstWhere((state) => state is ReaderLoaded);

        // Act
        readerBloc.add(NextPage());

        // Assert
        await expectLater(
          readerBloc.stream,
          emits(isA<ReaderLoaded>()
              .having((state) => state.currentPageIndex, 'currentPageIndex', 1)),
        );
      });

      test('should navigate to previous page', () async {
        // Arrange - Load comic and set current page to 2
        readerBloc.add(LoadComic(comicId: 'test_comic_1'));
        await readerBloc.stream.firstWhere((state) => state is ReaderLoaded);
        readerBloc.add(PageChanged(2));
        await readerBloc.stream.firstWhere(
          (state) => state is ReaderLoaded && state.currentPageIndex == 2);

        // Act
        readerBloc.add(PreviousPage());

        // Assert
        await expectLater(
          readerBloc.stream,
          emits(isA<ReaderLoaded>()
              .having((state) => state.currentPageIndex, 'currentPageIndex', 1)),
        );
      });

      test('should not navigate beyond first page', () async {
        // Arrange - Load comic (starts at page 0)
        readerBloc.add(LoadComic(comicId: 'test_comic_1'));
        await readerBloc.stream.firstWhere((state) => state is ReaderLoaded);

        // Act
        readerBloc.add(PreviousPage());

        // Assert - should remain at page 0
        await Future.delayed(const Duration(milliseconds: 100));
        final currentState = readerBloc.state as ReaderLoaded;
        expect(currentState.currentPageIndex, equals(0));
      });

      test('should not navigate beyond last page', () async {
        // Arrange - Load comic and go to last page
        readerBloc.add(LoadComic(comicId: 'test_comic_1'));
        await readerBloc.stream.firstWhere((state) => state is ReaderLoaded);
        readerBloc.add(PageChanged(9)); // Last page (10 pages total)
        await readerBloc.stream.firstWhere(
          (state) => state is ReaderLoaded && state.currentPageIndex == 9);

        // Act
        readerBloc.add(NextPage());

        // Assert - should remain at page 9
        await Future.delayed(const Duration(milliseconds: 100));
        final currentState = readerBloc.state as ReaderLoaded;
        expect(currentState.currentPageIndex, equals(9));
      });

      test('should handle direct page change', () async {
        // Arrange - Load comic first
        readerBloc.add(LoadComic(comicId: 'test_comic_1'));
        await readerBloc.stream.firstWhere((state) => state is ReaderLoaded);

        // Act
        readerBloc.add(PageChanged(5));

        // Assert
        await expectLater(
          readerBloc.stream,
          emits(isA<ReaderLoaded>()
              .having((state) => state.currentPageIndex, 'currentPageIndex', 5)),
        );
      });

      test('should validate page bounds on direct page change', () async {
        // Arrange - Load comic first
        readerBloc.add(LoadComic(comicId: 'test_comic_1'));
        await readerBloc.stream.firstWhere((state) => state is ReaderLoaded);

        // Act - Try to navigate to invalid page
        readerBloc.add(PageChanged(15)); // Beyond total pages

        // Assert - should ignore invalid page change
        await Future.delayed(const Duration(milliseconds: 100));
        final currentState = readerBloc.state as ReaderLoaded;
        expect(currentState.currentPageIndex, equals(0)); // Should remain at initial page
      });
    });

    group('UI State Management', () {
      test('should toggle UI visibility', () async {
        // Arrange - Load comic first
        readerBloc.add(LoadComic(comicId: 'test_comic_1'));
        await readerBloc.stream.firstWhere((state) => state is ReaderLoaded);

        // Act
        readerBloc.add(ToggleUIVisibility());

        // Assert
        await expectLater(
          readerBloc.stream,
          emits(isA<ReaderLoaded>()
              .having((state) => state.isUIVisible, 'isUIVisible', false)),
        );
      });

      test('should toggle fullscreen mode', () async {
        // Arrange - Load comic first
        readerBloc.add(LoadComic(comicId: 'test_comic_1'));
        await readerBloc.stream.firstWhere((state) => state is ReaderLoaded);

        // Act
        readerBloc.add(ToggleFullscreen());

        // Assert
        await expectLater(
          readerBloc.stream,
          emits(isA<ReaderLoaded>()
              .having((state) => state.isFullscreen, 'isFullscreen', false)),
        );

        verify(() => mockSettingsRepository.saveReaderSettings(any())).called(1);
      });

      test('should update brightness', () async {
        // Arrange - Load comic first
        readerBloc.add(LoadComic(comicId: 'test_comic_1'));
        await readerBloc.stream.firstWhere((state) => state is ReaderLoaded);

        // Act
        readerBloc.add(UpdateBrightness(0.5));

        // Assert
        await expectLater(
          readerBloc.stream,
          emits(isA<ReaderLoaded>()
              .having((state) => state.brightness, 'brightness', 0.5)),
        );

        verify(() => mockSettingsRepository.saveReaderSettings(any())).called(1);
      });

      test('should update zoom level', () async {
        // Arrange - Load comic first
        readerBloc.add(LoadComic(comicId: 'test_comic_1'));
        await readerBloc.stream.firstWhere((state) => state is ReaderLoaded);

        // Act
        readerBloc.add(ZoomChanged(2.0));

        // Assert
        await expectLater(
          readerBloc.stream,
          emits(isA<ReaderLoaded>()
              .having((state) => state.zoomLevel, 'zoomLevel', 2.0)),
        );
      });
    });

    group('Reading Mode', () {
      test('should change reading mode', () async {
        // Arrange - Load comic first
        readerBloc.add(LoadComic(comicId: 'test_comic_1'));
        await readerBloc.stream.firstWhere((state) => state is ReaderLoaded);

        // Act
        readerBloc.add(ChangeReadingMode(ReadingMode.rightToLeft));

        // Assert
        await expectLater(
          readerBloc.stream,
          emits(isA<ReaderLoaded>()
              .having((state) => state.settings.readingMode, 'readingMode', ReadingMode.rightToLeft)),
        );

        verify(() => mockSettingsRepository.saveReaderSettings(any())).called(1);
      });

      test('should persist reading mode changes', () async {
        // Arrange
        when(() => mockSettingsRepository.saveReaderSettings(any()))
            .thenAnswer((_) async => Right(null));

        readerBloc.add(LoadComic(comicId: 'test_comic_1'));
        await readerBloc.stream.firstWhere((state) => state is ReaderLoaded);

        // Act
        readerBloc.add(ChangeReadingMode(ReadingMode.vertical));

        // Assert
        await expectLater(
          readerBloc.stream,
          emits(isA<ReaderLoaded>()),
        );

        final captured = verify(() => mockSettingsRepository.saveReaderSettings(captureAny())).captured;
        final savedSettings = captured.first as ReaderSettings;
        expect(savedSettings.readingMode, equals(ReadingMode.vertical));
      });
    });

    group('Auto Page Turn', () {
      test('should toggle auto page turn', () async {
        // Arrange
        when(() => mockAutoPageService.startAutoPage(any())).thenAnswer((_) async {});
        when(() => mockAutoPageService.stopAutoPage()).thenAnswer((_) async {});
        when(() => mockAutoPageService.isAutoPageActive).thenReturn(false);

        readerBloc.add(LoadComic(comicId: 'test_comic_1'));
        await readerBloc.stream.firstWhere((state) => state is ReaderLoaded);

        // Act
        readerBloc.add(ToggleAutoPageTurn());

        // Assert
        await expectLater(
          readerBloc.stream,
          emits(isA<ReaderLoaded>()
              .having((state) => state.isAutoPageTurnEnabled, 'isAutoPageTurnEnabled', false)),
        );
      });

      test('should update auto page interval', () async {
        // Arrange
        when(() => mockAutoPageService.setInterval(any())).thenAnswer((_) async {});
        when(() => mockSettingsRepository.saveReaderSettings(any()))
            .thenAnswer((_) async => Right(null));

        readerBloc.add(LoadComic(comicId: 'test_comic_1'));
        await readerBloc.stream.firstWhere((state) => state is ReaderLoaded);

        // Act
        const newInterval = Duration(seconds: 5);
        readerBloc.add(UpdateAutoPageInterval(newInterval));

        // Assert
        await expectLater(
          readerBloc.stream,
          emits(isA<ReaderLoaded>()
              .having((state) => state.autoPageInterval, 'autoPageInterval', newInterval)),
        );

        verify(() => mockAutoPageService.setInterval(5)).called(1);
        verify(() => mockSettingsRepository.saveReaderSettings(any())).called(1);
      });
    });

    group('Gesture Handling', () {
      test('should handle tap left gesture for previous page', () async {
        // Arrange - Load comic and set to page 2
        readerBloc.add(LoadComic(comicId: 'test_comic_1'));
        await readerBloc.stream.firstWhere((state) => state is ReaderLoaded);
        readerBloc.add(PageChanged(2));
        await readerBloc.stream.firstWhere(
          (state) => state is ReaderLoaded && state.currentPageIndex == 2);

        // Act
        readerBloc.add(HandleGesture(GestureType.tapLeft));

        // Assert
        await expectLater(
          readerBloc.stream,
          emits(isA<ReaderLoaded>()
              .having((state) => state.currentPageIndex, 'currentPageIndex', 1)),
        );
      });

      test('should handle tap right gesture for next page', () async {
        // Arrange - Load comic
        readerBloc.add(LoadComic(comicId: 'test_comic_1'));
        await readerBloc.stream.firstWhere((state) => state is ReaderLoaded);

        // Act
        readerBloc.add(HandleGesture(GestureType.tapRight));

        // Assert
        await expectLater(
          readerBloc.stream,
          emits(isA<ReaderLoaded>()
              .having((state) => state.currentPageIndex, 'currentPageIndex', 1)),
        );
      });

      test('should handle tap center gesture for UI toggle', () async {
        // Arrange - Load comic
        readerBloc.add(LoadComic(comicId: 'test_comic_1'));
        await readerBloc.stream.firstWhere((state) => state is ReaderLoaded);

        // Act
        readerBloc.add(HandleGesture(GestureType.tapCenter));

        // Assert
        await expectLater(
          readerBloc.stream,
          emits(isA<ReaderLoaded>()
              .having((state) => state.isUIVisible, 'isUIVisible', false)),
        );
      });

      test('should handle other gesture types', () async {
        // Arrange - Load comic
        readerBloc.add(LoadComic(comicId: 'test_comic_1'));
        await readerBloc.stream.firstWhere((state) => state is ReaderLoaded);

        final gestures = [
          GestureType.doubleTap,
          GestureType.longPress,
          GestureType.swipeLeft,
          GestureType.swipeRight,
          GestureType.swipeUp,
          GestureType.swipeDown,
          GestureType.pinchIn,
          GestureType.pinchOut,
        ];

        // Act & Assert - Should not crash on any gesture
        for (final gesture in gestures) {
          readerBloc.add(HandleGesture(gesture));
          await Future.delayed(const Duration(milliseconds: 10));
        }

        expect(readerBloc.state, isA<ReaderLoaded>());
      });
    });

    group('Bookmark Management', () {
      test('should add bookmark', () async {
        // Arrange
        when(() => mockBookmarkRepository.addBookmark(any()))
            .thenAnswer((_) async => Right(null));

        readerBloc.add(LoadComic(comicId: 'test_comic_1'));
        await readerBloc.stream.firstWhere((state) => state is ReaderLoaded);
        readerBloc.add(PageChanged(3)); // Go to page not currently bookmarked
        await readerBloc.stream.firstWhere(
          (state) => state is ReaderLoaded && state.currentPageIndex == 3);

        // Act
        readerBloc.add(AddBookmark(3));

        // Assert
        await expectLater(
          readerBloc.stream,
          emits(isA<ReaderLoaded>()
              .having((state) => state.isCurrentPageBookmarked, 'isCurrentPageBookmarked', true)),
        );

        verify(() => mockBookmarkRepository.addBookmark(any())).called(1);
      });

      test('should remove bookmark', () async {
        // Arrange
        when(() => mockBookmarkRepository.removeBookmark(any(), any()))
            .thenAnswer((_) async => Right(null));

        readerBloc.add(LoadComic(comicId: 'test_comic_1'));
        await readerBloc.stream.firstWhere((state) => state is ReaderLoaded);
        readerBloc.add(PageChanged(2)); // Go to bookmarked page
        await readerBloc.stream.firstWhere(
          (state) => state is ReaderLoaded && state.currentPageIndex == 2);

        // Act
        readerBloc.add(RemoveBookmark(2));

        // Assert
        await expectLater(
          readerBloc.stream,
          emits(isA<ReaderLoaded>()
              .having((state) => state.isCurrentPageBookmarked, 'isCurrentPageBookmarked', false)),
        );

        verify(() => mockBookmarkRepository.removeBookmark('test_comic_1', 2)).called(1);
      });

      test('should handle bookmark add failure', () async {
        // Arrange
        when(() => mockBookmarkRepository.addBookmark(any()))
            .thenAnswer((_) async => Left(Failure('Failed to add bookmark')));

        readerBloc.add(LoadComic(comicId: 'test_comic_1'));
        await readerBloc.stream.firstWhere((state) => state is ReaderLoaded);

        // Act
        readerBloc.add(AddBookmark(1));

        // Assert
        await expectLater(
          readerBloc.stream,
          emits(isA<ReaderError>()
              .having((state) => state.message, 'message', contains('Failed to add bookmark'))),
        );
      });

      test('should handle bookmark remove failure', () async {
        // Arrange
        when(() => mockBookmarkRepository.removeBookmark(any(), any()))
            .thenAnswer((_) async => Left(Failure('Failed to remove bookmark')));

        readerBloc.add(LoadComic(comicId: 'test_comic_1'));
        await readerBloc.stream.firstWhere((state) => state is ReaderLoaded);

        // Act
        readerBloc.add(RemoveBookmark(2)); // Try to remove existing bookmark

        // Assert
        await expectLater(
          readerBloc.stream,
          emits(isA<ReaderError>()
              .having((state) => state.message, 'message', contains('Failed to remove bookmark'))),
        );
      });
    });

    group('Settings Management', () {
      test('should load settings', () async {
        // Arrange
        const newSettings = ReaderSettings(
          readingMode: ReadingMode.rightToLeft,
          brightness: 0.5,
        );
        when(() => mockSettingsRepository.getReaderSettings())
            .thenAnswer((_) async => Right(newSettings));

        readerBloc.add(LoadComic(comicId: 'test_comic_1'));
        await readerBloc.stream.firstWhere((state) => state is ReaderLoaded);

        // Act
        readerBloc.add(LoadSettings());

        // Assert
        await expectLater(
          readerBloc.stream,
          emits(isA<ReaderLoaded>()
              .having((state) => state.settings, 'settings', newSettings)),
        );
      });

      test('should update settings', () async {
        // Arrange
        when(() => mockSettingsRepository.saveReaderSettings(any()))
            .thenAnswer((_) async => Right(null));

        readerBloc.add(LoadComic(comicId: 'test_comic_1'));
        await readerBloc.stream.firstWhere((state) => state is ReaderLoaded);

        const newSettings = ReaderSettings(
          readingMode: ReadingMode.webtoon,
          brightness: 0.3,
        );

        // Act
        readerBloc.add(UpdateSettings(newSettings));

        // Assert
        await expectLater(
          readerBloc.stream,
          emits(isA<ReaderLoaded>()
              .having((state) => state.settings, 'settings', newSettings)),
        );

        verify(() => mockSettingsRepository.saveReaderSettings(newSettings)).called(1);
      });

      test('should handle settings load failure', () async {
        // Arrange
        when(() => mockSettingsRepository.getReaderSettings())
            .thenAnswer((_) async => Left(Failure('Settings not found')));

        readerBloc.add(LoadComic(comicId: 'test_comic_1'));
        await readerBloc.stream.firstWhere((state) => state is ReaderLoaded);

        // Act
        readerBloc.add(LoadSettings());

        // Assert
        await expectLater(
          readerBloc.stream,
          emits(isA<ReaderError>()
              .having((state) => state.message, 'message', contains('Failed to load settings'))),
        );
      });

      test('should handle settings save failure', () async {
        // Arrange
        when(() => mockSettingsRepository.saveReaderSettings(any()))
            .thenAnswer((_) async => Left(Failure('Failed to save settings')));

        readerBloc.add(LoadComic(comicId: 'test_comic_1'));
        await readerBloc.stream.firstWhere((state) => state is ReaderLoaded);

        // Act
        readerBloc.add(UpdateSettings(testSettings));

        // Assert
        await expectLater(
          readerBloc.stream,
          emits(isA<ReaderError>()
              .having((state) => state.message, 'message', contains('Failed to save settings'))),
        );
      });
    });

    group('Performance Tests', () {
      test('should handle rapid page changes efficiently', () async {
        // Arrange
        readerBloc.add(LoadComic(comicId: 'test_comic_1'));
        await readerBloc.stream.firstWhere((state) => state is ReaderLoaded);

        final stopwatch = Stopwatch()..start();

        // Act - Rapid page changes
        for (int i = 0; i < 10; i++) {
          readerBloc.add(PageChanged(i % 10));
          await Future.delayed(const Duration(milliseconds: 10));
        }

        stopwatch.stop();

        // Assert
        expect(stopwatch.elapsedMilliseconds, lessThan(500));
        expect(readerBloc.state, isA<ReaderLoaded>());
      });

      test('should handle multiple rapid gestures efficiently', () async {
        // Arrange
        readerBloc.add(LoadComic(comicId: 'test_comic_1'));
        await readerBloc.stream.firstWhere((state) => state is ReaderLoaded);

        final stopwatch = Stopwatch()..start();

        // Act - Rapid gestures
        final gestures = [
          GestureType.tapLeft,
          GestureType.tapRight,
          GestureType.tapCenter,
        ];

        for (int i = 0; i < 30; i++) {
          readerBloc.add(HandleGesture(gestures[i % gestures.length]));
          await Future.delayed(const Duration(milliseconds: 5));
        }

        stopwatch.stop();

        // Assert
        expect(stopwatch.elapsedMilliseconds, lessThan(1000));
        expect(readerBloc.state, isA<ReaderLoaded>());
      });
    });

    group('Edge Cases', () {
      test('should handle events when not loaded', () async {
        // Act - Try to navigate before loading
        readerBloc.add(NextPage());
        readerBloc.add(PreviousPage());
        readerBloc.add(PageChanged(5));

        // Assert - Should not crash
        await Future.delayed(const Duration(milliseconds: 100));
        expect(readerBloc.state, isA<ReaderInitial>());
      });

      test('should handle bookmark removal for non-existent bookmark', () async {
        // Arrange
        readerBloc.add(LoadComic(comicId: 'test_comic_1'));
        await readerBloc.stream.firstWhere((state) => state is ReaderLoaded);

        // Act - Try to remove bookmark that doesn't exist
        expect(
          () => readerBloc.add(RemoveBookmark(100)), // Non-existent page
          returnsNormally,
        );
      });

      test('should handle concurrent state changes', () async {
        // Arrange
        readerBloc.add(LoadComic(comicId: 'test_comic_1'));
        await readerBloc.stream.firstWhere((state) => state is ReaderLoaded);

        // Act - Multiple concurrent changes
        readerBloc.add(UpdateBrightness(0.5));
        readerBloc.add(ToggleFullscreen());
        readerBloc.add(PageChanged(3));
        readerBloc.add(ZoomChanged(1.5));

        // Wait for all changes to process
        await Future.delayed(const Duration(milliseconds: 200));

        // Assert - Should end up in a valid state
        final state = readerBloc.state;
        expect(state, isA<ReaderLoaded>());
      });
    });
  });
}