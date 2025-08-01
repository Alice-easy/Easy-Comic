import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:easy_comic/presentation/widgets/reader_core.dart';
import 'package:easy_comic/domain/entities/comic.dart';
import 'package:easy_comic/domain/entities/comic_page.dart';
import 'package:easy_comic/domain/entities/reader_settings.dart';
import 'package:easy_comic/domain/entities/bookmark.dart';
import 'package:easy_comic/domain/entities/gesture_types.dart';
import 'package:easy_comic/domain/services/auto_page_service.dart';
import 'package:easy_comic/domain/services/cache_service.dart';
import 'package:easy_comic/domain/services/volume_key_service.dart';
import 'package:easy_comic/domain/services/gesture_config_service.dart';

// Mock classes
class MockAutoPageService extends Mock implements AutoPageService {}
class MockCacheService extends Mock implements ICacheService {}
class MockVolumeKeyService extends Mock implements IVolumeKeyService {}
class MockGestureConfigService extends Mock implements IGestureConfigService {}

void main() {
  group('ReaderCore Widget Tests', () {
    late MockAutoPageService mockAutoPageService;
    late MockCacheService mockCacheService;
    late MockVolumeKeyService mockVolumeKeyService;
    late MockGestureConfigService mockGestureConfigService;

    // Test data
    final testComic = Comic(
      id: 'test_comic_1',
      title: 'Test Comic',
      filePath: '/test/path/comic.cbz',
      totalPages: 3,
      createdAt: DateTime.now(),
    );

    final testPages = [
      ComicPage(
        id: 'page_0',
        imagePath: 'page_0.jpg',
        pageNumber: 0,
        imageData: List<int>.filled(100, 0),
      ),
      ComicPage(
        id: 'page_1',
        imagePath: 'page_1.jpg',
        pageNumber: 1,
        imageData: List<int>.filled(100, 1),
      ),
      ComicPage(
        id: 'page_2',
        imagePath: 'page_2.jpg',
        pageNumber: 2,
        imageData: List<int>.filled(100, 2),
      ),
    ];

    const testSettings = ReaderSettings(
      readingMode: ReadingMode.leftToRight,
      enableFullscreen: true,
      brightness: 0.8,
      enableVolumeKeys: true,
    );

    final testBookmarks = [
      Bookmark(
        comicId: 'test_comic_1',
        pageIndex: 1,
        createdAt: DateTime.now(),
      ),
    ];

    setUpAll(() {
      registerFallbackValue(GestureEvent.now(type: GestureType.tapCenter));
      registerFallbackValue(testBookmarks.first);
    });

    setUp(() {
      mockAutoPageService = MockAutoPageService();
      mockCacheService = MockCacheService();
      mockVolumeKeyService = MockVolumeKeyService();
      mockGestureConfigService = MockGestureConfigService();

      // Setup default mock responses
      when(() => mockAutoPageService.isAutoPageActive).thenReturn(false);
      when(() => mockAutoPageService.isAutoPagePaused).thenReturn(false);
      when(() => mockAutoPageService.watchAutoPageState())
          .thenAnswer((_) => Stream.value(AutoPageState.stopped));
      when(() => mockVolumeKeyService.keyEventStream)
          .thenAnswer((_) => Stream.empty());
      
      // Mock gesture config service
      when(() => mockGestureConfigService.currentConfig)
          .thenReturn(const GestureConfig());
      when(() => mockGestureConfigService.tapZoneConfig)
          .thenReturn(const TapZoneConfig());
    });

    Widget createTestWidget({
      int currentPageIndex = 0,
      ReaderSettings? settings,
      VoidCallback? onPageChanged,
      ValueChanged<GestureEvent>? onGesture,
      ValueChanged<double>? onZoomChanged,
      VoidCallback? onAutoPageToggle,
      Function(Bookmark)? onBookmarkCreate,
    }) {
      return MaterialApp(
        home: Scaffold(
          body: ReaderCore(
            comic: testComic,
            pages: testPages,
            currentPageIndex: currentPageIndex,
            settings: settings ?? testSettings,
            autoPageService: mockAutoPageService,
            cacheService: mockCacheService,
            volumeKeyService: mockVolumeKeyService,
            gestureConfigService: mockGestureConfigService,
            onPageChanged: (index) => onPageChanged?.call(),
            onGesture: onGesture ?? (gesture) {},
            onZoomChanged: onZoomChanged ?? (zoom) {},
            onAutoPageToggle: onAutoPageToggle,
            onBookmarkCreate: onBookmarkCreate,
          ),
        ),
      );
    }

    group('Widget Rendering', () {
      testWidgets('should render with pages', (WidgetTester tester) async {
        await tester.pumpWidget(createTestWidget());
        await tester.pumpAndSettle();

        expect(find.byType(ReaderCore), findsOneWidget);
      });

      testWidgets('should render empty state when no pages', (WidgetTester tester) async {
        await tester.pumpWidget(MaterialApp(
          home: Scaffold(
            body: ReaderCore(
              comic: testComic,
              pages: [], // Empty pages
              currentPageIndex: 0,
              settings: testSettings,
              autoPageService: mockAutoPageService,
              cacheService: mockCacheService,
              volumeKeyService: mockVolumeKeyService,
              gestureConfigService: mockGestureConfigService,
              onPageChanged: (index) {},
              onGesture: (gesture) {},
              onZoomChanged: (zoom) {},
            ),
          ),
        ));
        await tester.pumpAndSettle();

        expect(find.text('没有可显示的页面'), findsOneWidget);
      });

      testWidgets('should show auto-page progress indicator when enabled', (WidgetTester tester) async {
        const settingsWithAutoPage = ReaderSettings(
          readingMode: ReadingMode.leftToRight,
          autoPageConfig: AutoPageConfig(showProgressIndicator: true),
        );

        await tester.pumpWidget(createTestWidget(settings: settingsWithAutoPage));
        await tester.pumpAndSettle();

        expect(find.byType(AutoPageProgressIndicator), findsOneWidget);
      });

      testWidgets('should not show auto-page progress indicator when disabled', (WidgetTester tester) async {
        const settingsWithoutAutoPage = ReaderSettings(
          readingMode: ReadingMode.leftToRight,
          autoPageConfig: AutoPageConfig(showProgressIndicator: false),
        );

        await tester.pumpWidget(createTestWidget(settings: settingsWithoutAutoPage));
        await tester.pumpAndSettle();

        expect(find.byType(AutoPageProgressIndicator), findsNothing);
      });
    });

    group('Gesture Handling', () {
      testWidgets('should handle tap gestures', (WidgetTester tester) async {
        GestureEvent? capturedGesture;
        
        await tester.pumpWidget(createTestWidget(
          onGesture: (gesture) => capturedGesture = gesture,
        ));
        await tester.pumpAndSettle();

        // Tap in the center of the widget
        await tester.tap(find.byType(ReaderCore));
        await tester.pumpAndSettle();

        expect(capturedGesture, isNotNull);
        expect(capturedGesture!.type, equals(GestureType.tapCenter));
      });

      testWidgets('should handle scale gestures', (WidgetTester tester) async {
        bool pauseCalled = false;
        when(() => mockAutoPageService.pauseForUserInteraction())
            .thenAnswer((_) async => pauseCalled = true);

        await tester.pumpWidget(createTestWidget());
        await tester.pumpAndSettle();

        // Simulate scale gesture
        final center = tester.getCenter(find.byType(ReaderCore));
        final gesture = await tester.startGesture(center);
        await gesture.moveBy(const Offset(50, 0));
        await gesture.up();
        await tester.pumpAndSettle();

        // Verify auto-page pause was called
        verify(() => mockAutoPageService.pauseForUserInteraction()).called(greaterThanOrEqualTo(1));
      });
    });

    group('Volume Key Handling', () {
      testWidgets('should listen to volume key events', (WidgetTester tester) async {
        final volumeKeyController = StreamController<VolumeKeyEvent>();
        when(() => mockVolumeKeyService.keyEventStream)
            .thenAnswer((_) => volumeKeyController.stream);

        GestureEvent? capturedGesture;
        
        await tester.pumpWidget(createTestWidget(
          onGesture: (gesture) => capturedGesture = gesture,
        ));
        await tester.pumpAndSettle();

        // Simulate volume up key
        volumeKeyController.add(VolumeKeyEvent(
          type: VolumeKeyType.volumeUp,
          timestamp: DateTime.now(),
        ));
        await tester.pumpAndSettle();

        expect(capturedGesture, isNotNull);
        expect(capturedGesture!.type, equals(GestureType.volumeUp));

        volumeKeyController.close();
      });

      testWidgets('should ignore volume keys when disabled', (WidgetTester tester) async {
        const settingsWithoutVolumeKeys = ReaderSettings(
          readingMode: ReadingMode.leftToRight,
          enableVolumeKeys: false,
        );

        final volumeKeyController = StreamController<VolumeKeyEvent>();
        when(() => mockVolumeKeyService.keyEventStream)
            .thenAnswer((_) => volumeKeyController.stream);

        GestureEvent? capturedGesture;
        
        await tester.pumpWidget(createTestWidget(
          settings: settingsWithoutVolumeKeys,
          onGesture: (gesture) => capturedGesture = gesture,
        ));
        await tester.pumpAndSettle();

        // Simulate volume up key
        volumeKeyController.add(VolumeKeyEvent(
          type: VolumeKeyType.volumeUp,
          timestamp: DateTime.now(),
        ));
        await tester.pumpAndSettle();

        expect(capturedGesture, isNull);

        volumeKeyController.close();
      });
    });

    group('Reading Mode Rendering', () {
      testWidgets('should create horizontal renderer for left-to-right mode', (WidgetTester tester) async {
        const settings = ReaderSettings(readingMode: ReadingMode.leftToRight);
        
        await tester.pumpWidget(createTestWidget(settings: settings));
        await tester.pumpAndSettle();

        expect(find.byType(ReaderCore), findsOneWidget);
        // Note: Internal renderer type cannot be directly tested in widget tests
        // This would need integration tests or testing the widget's internal state
      });

      testWidgets('should create horizontal renderer for right-to-left mode', (WidgetTester tester) async {
        const settings = ReaderSettings(readingMode: ReadingMode.rightToLeft);
        
        await tester.pumpWidget(createTestWidget(settings: settings));
        await tester.pumpAndSettle();

        expect(find.byType(ReaderCore), findsOneWidget);
      });

      testWidgets('should create vertical renderer for vertical mode', (WidgetTester tester) async {
        const settings = ReaderSettings(readingMode: ReadingMode.vertical);
        
        await tester.pumpWidget(createTestWidget(settings: settings));
        await tester.pumpAndSettle();

        expect(find.byType(ReaderCore), findsOneWidget);
      });

      testWidgets('should create webtoon renderer for webtoon mode', (WidgetTester tester) async {
        const settings = ReaderSettings(readingMode: ReadingMode.webtoon);
        
        await tester.pumpWidget(createTestWidget(settings: settings));
        await tester.pumpAndSettle();

        expect(find.byType(ReaderCore), findsOneWidget);
      });
    });

    group('Auto Page Integration', () {
      testWidgets('should pause auto-page on user interaction', (WidgetTester tester) async {
        when(() => mockAutoPageService.isAutoPageActive).thenReturn(true);
        when(() => mockAutoPageService.pauseForUserInteraction())
            .thenAnswer((_) async {});

        await tester.pumpWidget(createTestWidget());
        await tester.pumpAndSettle();

        // Simulate user tap
        await tester.tap(find.byType(ReaderCore));
        await tester.pumpAndSettle();

        verify(() => mockAutoPageService.pauseForUserInteraction()).called(1);
      });

      testWidgets('should handle auto-page indicator tap', (WidgetTester tester) async {
        const settingsWithAutoPage = ReaderSettings(
          readingMode: ReadingMode.leftToRight,
          autoPageConfig: AutoPageConfig(showProgressIndicator: true),
        );

        when(() => mockAutoPageService.isAutoPageActive).thenReturn(true);
        when(() => mockAutoPageService.isAutoPagePaused).thenReturn(false);
        when(() => mockAutoPageService.pauseAutoPage()).thenAnswer((_) async {});

        await tester.pumpWidget(createTestWidget(settings: settingsWithAutoPage));
        await tester.pumpAndSettle();

        // Tap on auto-page indicator
        await tester.tap(find.byType(AutoPageProgressIndicator));
        await tester.pumpAndSettle();

        verify(() => mockAutoPageService.pauseAutoPage()).called(1);
      });

      testWidgets('should resume auto-page when tapped while paused', (WidgetTester tester) async {
        const settingsWithAutoPage = ReaderSettings(
          readingMode: ReadingMode.leftToRight,
          autoPageConfig: AutoPageConfig(showProgressIndicator: true),
        );

        when(() => mockAutoPageService.isAutoPageActive).thenReturn(true);
        when(() => mockAutoPageService.isAutoPagePaused).thenReturn(true);
        when(() => mockAutoPageService.resumeAutoPage()).thenAnswer((_) async {});

        await tester.pumpWidget(createTestWidget(settings: settingsWithAutoPage));
        await tester.pumpAndSettle();

        // Tap on auto-page indicator while paused
        await tester.tap(find.byType(AutoPageProgressIndicator));
        await tester.pumpAndSettle();

        verify(() => mockAutoPageService.resumeAutoPage()).called(1);
      });

      testWidgets('should toggle auto-page when inactive', (WidgetTester tester) async {
        const settingsWithAutoPage = ReaderSettings(
          readingMode: ReadingMode.leftToRight,
          autoPageConfig: AutoPageConfig(showProgressIndicator: true),
        );

        when(() => mockAutoPageService.isAutoPageActive).thenReturn(false);

        bool toggleCalled = false;
        
        await tester.pumpWidget(createTestWidget(
          settings: settingsWithAutoPage,
          onAutoPageToggle: () => toggleCalled = true,
        ));
        await tester.pumpAndSettle();

        // Tap on auto-page indicator when inactive
        await tester.tap(find.byType(AutoPageProgressIndicator));
        await tester.pumpAndSettle();

        expect(toggleCalled, isTrue);
      });
    });

    group('Page Change Handling', () {
      testWidgets('should update page controller when current page changes', (WidgetTester tester) async {
        await tester.pumpWidget(createTestWidget(currentPageIndex: 0));
        await tester.pumpAndSettle();

        // Update to page 1
        await tester.pumpWidget(createTestWidget(currentPageIndex: 1));
        await tester.pumpAndSettle();

        expect(find.byType(ReaderCore), findsOneWidget);
      });

      testWidgets('should reinitialize renderer when reading mode changes', (WidgetTester tester) async {
        const initialSettings = ReaderSettings(readingMode: ReadingMode.leftToRight);
        const newSettings = ReaderSettings(readingMode: ReadingMode.vertical);

        await tester.pumpWidget(createTestWidget(settings: initialSettings));
        await tester.pumpAndSettle();

        // Change reading mode
        await tester.pumpWidget(createTestWidget(settings: newSettings));
        await tester.pumpAndSettle();

        expect(find.byType(ReaderCore), findsOneWidget);
      });
    });

    group('Focus and Keyboard Handling', () {
      testWidgets('should handle keyboard events for volume keys', (WidgetTester tester) async {
        GestureEvent? capturedGesture;
        
        await tester.pumpWidget(createTestWidget(
          onGesture: (gesture) => capturedGesture = gesture,
        ));
        await tester.pumpAndSettle();

        // Simulate volume up key press
        await tester.sendKeyDownEvent(LogicalKeyboardKey.audioVolumeUp);
        await tester.pumpAndSettle();

        expect(capturedGesture, isNotNull);
        expect(capturedGesture!.type, equals(GestureType.volumeUp));
      });

      testWidgets('should ignore keyboard events when volume keys disabled', (WidgetTester tester) async {
        const settingsWithoutVolumeKeys = ReaderSettings(
          readingMode: ReadingMode.leftToRight,
          enableVolumeKeys: false,
        );

        GestureEvent? capturedGesture;
        
        await tester.pumpWidget(createTestWidget(
          settings: settingsWithoutVolumeKeys,
          onGesture: (gesture) => capturedGesture = gesture,
        ));
        await tester.pumpAndSettle();

        // Simulate volume up key press
        await tester.sendKeyDownEvent(LogicalKeyboardKey.audioVolumeUp);
        await tester.pumpAndSettle();

        expect(capturedGesture, isNull);
      });

      testWidgets('should handle volume down key', (WidgetTester tester) async {
        GestureEvent? capturedGesture;
        
        await tester.pumpWidget(createTestWidget(
          onGesture: (gesture) => capturedGesture = gesture,
        ));
        await tester.pumpAndSettle();

        // Simulate volume down key press
        await tester.sendKeyDownEvent(LogicalKeyboardKey.audioVolumeDown);
        await tester.pumpAndSettle();

        expect(capturedGesture, isNotNull);
        expect(capturedGesture!.type, equals(GestureType.volumeDown));
      });
    });

    group('Performance Tests', () {
      testWidgets('should render quickly with many pages', (WidgetTester tester) async {
        // Create a large number of pages
        final manyPages = List.generate(100, (index) => ComicPage(
          id: 'page_$index',
          imagePath: 'page_$index.jpg',
          pageNumber: index,
          imageData: List<int>.filled(100, index % 256),
        ));

        final largeComic = Comic(
          id: 'large_comic',
          title: 'Large Comic',
          filePath: '/test/path/large_comic.cbz',
          totalPages: manyPages.length,
          createdAt: DateTime.now(),
        );

        final stopwatch = Stopwatch()..start();

        await tester.pumpWidget(MaterialApp(
          home: Scaffold(
            body: ReaderCore(
              comic: largeComic,
              pages: manyPages,
              currentPageIndex: 0,
              settings: testSettings,
              autoPageService: mockAutoPageService,
              cacheService: mockCacheService,
              volumeKeyService: mockVolumeKeyService,
              gestureConfigService: mockGestureConfigService,
              onPageChanged: (index) {},
              onGesture: (gesture) {},
              onZoomChanged: (zoom) {},
            ),
          ),
        ));
        await tester.pumpAndSettle();

        stopwatch.stop();

        expect(find.byType(ReaderCore), findsOneWidget);
        expect(stopwatch.elapsedMilliseconds, lessThan(1000)); // Should render quickly
      });

      testWidgets('should handle rapid gesture events', (WidgetTester tester) async {
        final gestureEvents = <GestureEvent>[];
        
        await tester.pumpWidget(createTestWidget(
          onGesture: (gesture) => gestureEvents.add(gesture),
        ));
        await tester.pumpAndSettle();

        final stopwatch = Stopwatch()..start();

        // Simulate rapid taps
        for (int i = 0; i < 10; i++) {
          await tester.tap(find.byType(ReaderCore));
          await tester.pump(const Duration(milliseconds: 10));
        }

        stopwatch.stop();

        expect(gestureEvents.length, equals(10));
        expect(stopwatch.elapsedMilliseconds, lessThan(500));
      });
    });

    group('Error Handling', () {
      testWidgets('should handle null callbacks gracefully', (WidgetTester tester) async {
        await tester.pumpWidget(MaterialApp(
          home: Scaffold(
            body: ReaderCore(
              comic: testComic,
              pages: testPages,
              currentPageIndex: 0,
              settings: testSettings,
              autoPageService: mockAutoPageService,
              cacheService: mockCacheService,
              volumeKeyService: mockVolumeKeyService,
              gestureConfigService: mockGestureConfigService,
              onPageChanged: (index) {},
              onGesture: (gesture) {},
              onZoomChanged: (zoom) {},
              // onAutoPageToggle and onBookmarkCreate are null
            ),
          ),
        ));
        await tester.pumpAndSettle();

        expect(find.byType(ReaderCore), findsOneWidget);
      });

      testWidgets('should handle stream errors gracefully', (WidgetTester tester) async {
        final errorController = StreamController<VolumeKeyEvent>();
        when(() => mockVolumeKeyService.keyEventStream)
            .thenAnswer((_) => errorController.stream);

        await tester.pumpWidget(createTestWidget());
        await tester.pumpAndSettle();

        // Add error to stream
        errorController.addError(Exception('Stream error'));
        await tester.pumpAndSettle();

        // Widget should still be rendered
        expect(find.byType(ReaderCore), findsOneWidget);

        errorController.close();
      });
    });

    group('Edge Cases', () {
      testWidgets('should handle empty comic title', (WidgetTester tester) async {
        final emptyTitleComic = Comic(
          id: 'empty_title',
          title: '',
          filePath: '/test/path/empty.cbz',
          totalPages: testPages.length,
          createdAt: DateTime.now(),
        );

        await tester.pumpWidget(MaterialApp(
          home: Scaffold(
            body: ReaderCore(
              comic: emptyTitleComic,
              pages: testPages,
              currentPageIndex: 0,
              settings: testSettings,
              autoPageService: mockAutoPageService,
              cacheService: mockCacheService,
              volumeKeyService: mockVolumeKeyService,
              gestureConfigService: mockGestureConfigService,
              onPageChanged: (index) {},
              onGesture: (gesture) {},
              onZoomChanged: (zoom) {},
            ),
          ),
        ));
        await tester.pumpAndSettle();

        expect(find.byType(ReaderCore), findsOneWidget);
      });

      testWidgets('should handle invalid current page index', (WidgetTester tester) async {
        await tester.pumpWidget(createTestWidget(currentPageIndex: 999)); // Invalid index
        await tester.pumpAndSettle();

        expect(find.byType(ReaderCore), findsOneWidget);
      });

      testWidgets('should handle single page comic', (WidgetTester tester) async {
        final singlePageComic = Comic(
          id: 'single_page',
          title: 'Single Page Comic',
          filePath: '/test/path/single.cbz',
          totalPages: 1,
          createdAt: DateTime.now(),
        );

        final singlePage = [testPages.first];

        await tester.pumpWidget(MaterialApp(
          home: Scaffold(
            body: ReaderCore(
              comic: singlePageComic,
              pages: singlePage,
              currentPageIndex: 0,
              settings: testSettings,
              autoPageService: mockAutoPageService,
              cacheService: mockCacheService,
              volumeKeyService: mockVolumeKeyService,
              gestureConfigService: mockGestureConfigService,
              onPageChanged: (index) {},
              onGesture: (gesture) {},
              onZoomChanged: (zoom) {},
            ),
          ),
        ));
        await tester.pumpAndSettle();

        expect(find.byType(ReaderCore), findsOneWidget);
      });
    });
  });
}