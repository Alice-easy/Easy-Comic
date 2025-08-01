# Comic Reader Viewer Component - Design Document

## Overview

This design document outlines the refined architecture and implementation strategy for the Flutter-based comic reader application's core viewer component. Based on quality validation feedback identifying critical interface integration gaps and tight coupling issues, this design provides precise interface definitions, dependency injection patterns, and architectural solutions to achieve professional-grade reading experience.

The component serves as a loosely coupled, testable reading interface that integrates seamlessly with existing services through well-defined contracts while maintaining 60fps performance and comprehensive error recovery.

## Architecture

### High-Level Architecture with Interface Abstractions

The enhanced comic reader maintains Clean Architecture principles with strict interface-based dependency injection:

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
├─────────────────────────────────────────────────────────────┤
│  • ReaderScreen (DI: IReaderBloc, INavigationService)     │
│  • ReaderCore (DI: IAutoPageService, IVolumeKeyService)   │
│  • ReaderBloc (DI: ICacheService, ISettingsRepository)   │
│  • GestureDetectionLayer (DI: IGestureConfigService)      │
│  • AutoPageController (DI: IAutoPageService)              │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                 Domain Layer (Interfaces)                  │
├─────────────────────────────────────────────────────────────┤
│  • ICacheService (preloadPages, setImageQuality methods)  │
│  • IAutoPageService (startTimer, pauseOnInteraction)      │
│  • IVolumeKeyService (registerCallbacks, handleKeyEvent)  │
│  • INavigationService (navigateToPage, handleModeSwitch)  │
│  • IReaderSettingsRepository (save, load, validate)       │
│  • IGestureConfigService (updateTapZones, sensitivity)    │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│              Data Layer (Implementations)                  │
├─────────────────────────────────────────────────────────────┤
│  • CacheService implements ICacheService                   │
│  • AutoPageService implements IAutoPageService             │
│  • VolumeKeyService implements IVolumeKeyService           │
│  • NavigationService implements INavigationService         │
│  • ReaderSettingsRepository implements ISettingsRepo      │
└─────────────────────────────────────────────────────────────┘
```

### Dependency Injection Configuration

Service registration with GetIt service locator:

```dart
// Core dependency injection setup
void setupServiceLocator() {
  // Interface implementations
  GetIt.instance.registerLazySingleton<ICacheService>(
    () => CacheService(),
  );
  
  GetIt.instance.registerLazySingleton<IAutoPageService>(
    () => AutoPageService(
      settingsRepository: GetIt.instance<IReaderSettingsRepository>(),
    ),
  );
  
  GetIt.instance.registerLazySingleton<IVolumeKeyService>(
    () => VolumeKeyService(),
  );
  
  // BLoC with interface dependencies
  GetIt.instance.registerFactory<IReaderBloc>(
    () => ReaderBloc(
      cacheService: GetIt.instance<ICacheService>(),
      settingsRepository: GetIt.instance<IReaderSettingsRepository>(),
      navigationService: GetIt.instance<INavigationService>(),
    ),
  );
}
```

## Components and Interfaces

### 1. Enhanced ICacheService Interface

**Purpose**: Complete interface contract for image caching with preloading and quality management.

**Critical Methods Addressing Validation Gaps**:
```dart
abstract class ICacheService {
  // Existing methods
  Future<Uint8List?> getImage(String key);
  Future<void> cacheImage(String key, Uint8List imageData);
  Future<void> clearCache();
  Stream<CacheStatistics> watchCacheStats();
  
  // MISSING METHODS - CRITICAL FIXES
  /// Preloads multiple pages with priority-based queue management
  Future<void> preloadPages(List<String> pageKeys, int currentPageIndex);
  
  /// Sets image quality for progressive loading
  Future<void> setImageQuality(String pageKey, ImageQuality quality);
  
  /// Background isolate cleanup without UI blocking
  Future<void> cleanupCacheAsync();
  
  /// Hardware acceleration configuration
  Future<void> configureHardwareAcceleration(bool enabled);
}

class CacheService implements ICacheService {
  final LRUCache<String, Uint8List> _memoryCache;
  final DiskCache _diskCache;
  final PreloadingEngine _preloader;
  final BackgroundProcessor _backgroundProcessor;
  
  @override
  Future<void> preloadPages(List<String> pageKeys, int currentPageIndex) async {
    await _preloader.enqueuePreloading(pageKeys, currentPageIndex);
  }
  
  @override
  Future<void> setImageQuality(String pageKey, ImageQuality quality) async {
    await _backgroundProcessor.adjustImageQuality(pageKey, quality);
  }
  
  @override
  Future<void> cleanupCacheAsync() async {
    await compute(_performCacheCleanup, _memoryCache.size);
  }
}
```

### 2. IAutoPageService Interface with Complete Contract

**Purpose**: Auto-page functionality with user interaction awareness and timer management.

```dart
abstract class IAutoPageService {
  Stream<AutoPageEvent> get eventStream;
  AutoPageState get currentState;
  
  Future<void> startAutoPage(Duration interval);
  Future<void> pauseForUserInteraction();
  Future<void> resumeAfterDelay(Duration delay);
  Future<void> stop();
  void handleUserInteraction(UserInteractionType type);
  
  // Configuration methods
  void updateInterval(Duration newInterval);
  void setEndOfComicBehavior(EndOfComicAction action);
}

class AutoPageService implements IAutoPageService {
  final IReaderSettingsRepository _settingsRepository;
  final StreamController<AutoPageEvent> _eventController;
  Timer? _autoPageTimer;
  Timer? _resumeTimer;
  
  AutoPageService({
    required IReaderSettingsRepository settingsRepository,
  }) : _settingsRepository = settingsRepository;
  
  @override
  Future<void> startAutoPage(Duration interval) async {
    _autoPageTimer?.cancel();
    _autoPageTimer = Timer.periodic(interval, _onTimerTick);
    _eventController.add(AutoPageEvent.started(interval));
  }
  
  @override
  void handleUserInteraction(UserInteractionType type) {
    if (_autoPageTimer?.isActive == true) {
      pauseForUserInteraction();
      _scheduleResume();
    }
  }
}
```

### 3. ReadingModeRenderer Base Class Implementation

**Purpose**: Abstract base class for different reading modes with consistent interface.

```dart
abstract class ReadingModeRenderer {
  final ReaderSettings settings;
  final INavigationService navigationService;
  
  ReadingModeRenderer({
    required this.settings,
    required this.navigationService,
  });
  
  // Abstract methods that must be implemented
  Widget buildView(List<ComicPage> pages, int currentPageIndex);
  Future<void> handlePageNavigation(NavigationDirection direction);
  Future<void> preserveReadingPosition(int currentPage, double scrollOffset);
  
  // Shared functionality
  void onPageChanged(int pageIndex) {
    navigationService.updateCurrentPage(pageIndex);
  }
}

class HorizontalModeRenderer extends ReadingModeRenderer {
  HorizontalModeRenderer({
    required super.settings,
    required super.navigationService,
  });
  
  @override
  Widget buildView(List<ComicPage> pages, int currentPageIndex) {
    return PhotoViewGallery.builder(
      itemCount: pages.length,
      builder: (context, index) => PhotoViewGalleryPageOptions(
        imageProvider: NetworkImage(pages[index].imagePath),
        minScale: PhotoViewComputedScale.contained,
        maxScale: PhotoViewComputedScale.covered * 3.0,
        heroAttributes: PhotoViewHeroAttributes(tag: pages[index].id),
      ),
      onPageChanged: onPageChanged,
      reverse: settings.readingMode == ReadingMode.rightToLeft,
    );
  }
  
  @override
  Future<void> handlePageNavigation(NavigationDirection direction) async {
    final isRTL = settings.readingMode == ReadingMode.rightToLeft;
    final shouldGoNext = (direction == NavigationDirection.forward && !isRTL) ||
                        (direction == NavigationDirection.backward && isRTL);
    
    if (shouldGoNext) {
      await navigationService.navigateToNextPage();
    } else {
      await navigationService.navigateToPreviousPage();
    }
  }
  
  @override
  Future<void> preserveReadingPosition(int currentPage, double scrollOffset) async {
    await navigationService.saveReadingPosition(currentPage, scrollOffset);
  }
}
```

### 4. Unified GestureType Enum in Domain Layer

**Purpose**: Single source of truth for gesture types across all components.

```dart
// Domain layer: lib/domain/entities/gesture_types.dart
enum GestureType {
  tapLeft,
  tapRight,
  tapCenter,
  pinchZoomIn,
  pinchZoomOut,
  panStart,
  panUpdate,
  panEnd,
  doubleTapZoom,
  volumeUp,
  volumeDown,
  longPress,
  swipeUp,
  swipeDown,
  swipeLeft,
  swipeRight,
}

class GestureEvent {
  final GestureType type;
  final Offset? position;
  final double? velocity;
  final double? scale;
  final DateTime timestamp;
  
  const GestureEvent({
    required this.type,
    this.position,
    this.velocity,
    this.scale,
    required this.timestamp,
  });
}
```

### 5. ReaderCore with Proper Dependency Injection

**Purpose**: Central reader component with loosely coupled dependencies.

```dart
class ReaderCore extends StatefulWidget {
  final Comic comic;
  final List<ComicPage> pages;
  final int currentPageIndex;
  final ReaderSettings settings;
  
  // Interface dependencies - NO concrete classes
  final IAutoPageService autoPageService;
  final ICacheService cacheService;
  final IVolumeKeyService volumeKeyService;
  final IGestureConfigService gestureConfigService;
  
  final ValueChanged<int> onPageChanged;
  final ValueChanged<GestureEvent> onGesture;
  final ValueChanged<double> onZoomChanged;
  
  const ReaderCore({
    Key? key,
    required this.comic,
    required this.pages,
    required this.currentPageIndex,
    required this.settings,
    required this.autoPageService,
    required this.cacheService,
    required this.volumeKeyService,
    required this.gestureConfigService,
    required this.onPageChanged,
    required this.onGesture,
    required this.onZoomChanged,
  }) : super(key: key);
  
  @override
  State<ReaderCore> createState() => _ReaderCoreState();
}

class _ReaderCoreState extends State<ReaderCore> {
  late ReadingModeRenderer _modeRenderer;
  late GestureDetector _gestureDetector;
  
  @override
  void initState() {
    super.initState();
    _initializeModeRenderer();
    _initializeGestureDetection();
  }
  
  void _initializeModeRenderer() {
    switch (widget.settings.readingMode) {
      case ReadingMode.leftToRight:
      case ReadingMode.rightToLeft:
        _modeRenderer = HorizontalModeRenderer(
          settings: widget.settings,
          navigationService: GetIt.instance<INavigationService>(),
        );
        break;
      case ReadingMode.vertical:
        _modeRenderer = VerticalModeRenderer(
          settings: widget.settings,
          navigationService: GetIt.instance<INavigationService>(),
        );
        break;
      case ReadingMode.webtoon:
        _modeRenderer = WebtoonModeRenderer(
          settings: widget.settings,
          navigationService: GetIt.instance<INavigationService>(),
        );
        break;
    }
  }
}
```

### 6. Background Processing with Isolates

**Purpose**: Heavy operations in background isolates to maintain UI performance.

```dart
class BackgroundProcessor {
  static const String _isolateName = 'ImageProcessorIsolate';
  late Isolate _processingIsolate;
  late ReceivePort _receivePort;
  late SendPort _sendPort;
  
  Future<void> initialize() async {
    _receivePort = ReceivePort();
    _processingIsolate = await Isolate.spawn(
      _imageProcessingEntryPoint,
      _receivePort.sendPort,
      debugName: _isolateName,
    );
    
    _sendPort = await _receivePort.first as SendPort;
  }
  
  Future<Uint8List> processImageInBackground(
    String imagePath,
    ImageProcessingConfig config,
  ) async {
    final completer = Completer<Uint8List>();
    final responsePort = ReceivePort();
    
    _sendPort.send(ImageProcessingRequest(
      imagePath: imagePath,
      config: config,
      responsePort: responsePort.sendPort,
    ));
    
    responsePort.listen((result) {
      if (result is Uint8List) {
        completer.complete(result);
      } else if (result is ImageProcessingError) {
        completer.completeError(result);
      }
      responsePort.close();
    });
    
    return completer.future;
  }
  
  static void _imageProcessingEntryPoint(SendPort sendPort) {
    final receivePort = ReceivePort();
    sendPort.send(receivePort.sendPort);
    
    receivePort.listen((message) async {
      if (message is ImageProcessingRequest) {
        try {
          final processedImage = await _processImage(message);
          message.responsePort.send(processedImage);
        } catch (e) {
          message.responsePort.send(ImageProcessingError(e.toString()));
        }
      }
    });
  }
}
```

### 7. Platform Channel Integration with Error Handling

**Purpose**: Volume key navigation with proper platform integration and validation.

```dart
abstract class IVolumeKeyService {
  Stream<VolumeKeyEvent> get keyEventStream;
  Future<void> enableVolumeKeyNavigation(bool enabled);
  Future<bool> get isVolumeKeyNavigationSupported;
}

class VolumeKeyService implements IVolumeKeyService {
  static const MethodChannel _methodChannel = MethodChannel('comic_reader/volume_keys');
  final StreamController<VolumeKeyEvent> _keyEventController = StreamController.broadcast();
  
  VolumeKeyService() {
    _methodChannel.setMethodCallHandler(_handleMethodCall);
  }
  
  @override
  Stream<VolumeKeyEvent> get keyEventStream => _keyEventController.stream;
  
  @override
  Future<void> enableVolumeKeyNavigation(bool enabled) async {
    try {
      await _methodChannel.invokeMethod('enableVolumeKeyNavigation', {
        'enabled': enabled,
      });
    } on PlatformException catch (e) {
      throw VolumeKeyServiceException('Failed to enable volume key navigation: ${e.message}');
    }
  }
  
  @override
  Future<bool> get isVolumeKeyNavigationSupported async {
    try {
      return await _methodChannel.invokeMethod<bool>('isSupported') ?? false;
    } on PlatformException catch (e) {
      return false; // Assume not supported if platform call fails
    }
  }
  
  Future<dynamic> _handleMethodCall(MethodCall call) async {
    switch (call.method) {
      case 'onVolumeKeyPressed':
        final arguments = call.arguments as Map<String, dynamic>;
        final keyType = VolumeKeyType.values[arguments['keyType'] as int];
        _keyEventController.add(VolumeKeyEvent(
          type: keyType,
          timestamp: DateTime.now(),
        ));
        break;
      default:
        throw UnimplementedError('Method ${call.method} not implemented');
    }
  }
}
```

## Data Models

### Enhanced ReaderSettings with Complete Configuration

```dart
@freezed
class ReaderSettings with _$ReaderSettings {
  const factory ReaderSettings({
    required ReadingMode readingMode,
    required NavigationDirection navigationDirection,
    required BackgroundTheme backgroundTheme,
    required TransitionType transitionType,
    required double brightness,
    required bool enableAutoPage,
    required int autoPageInterval,
    required bool enableWakelock,
    required bool showProgress,
    required bool showPageInfo,
    required bool enableVolumeKeys,
    required TapZoneConfig tapZoneConfig,
    required double zoomSensitivity,
    required bool enableDoubleTapZoom,
    required bool enableFullscreen,
    
    // Enhanced configuration objects
    required AutoPageConfig autoPageConfig,
    required CacheConfig cacheConfig,
    required GestureConfig gestureConfig,
    required PerformanceConfig performanceConfig,
  }) = _ReaderSettings;
  
  factory ReaderSettings.fromJson(Map<String, dynamic> json) =>
      _$ReaderSettingsFromJson(json);
}

@freezed
class AutoPageConfig with _$AutoPageConfig {
  const factory AutoPageConfig({
    required int defaultIntervalSeconds,
    required bool pauseOnUserInteraction,
    required bool pauseOnAppBackground,
    required bool showProgressIndicator,
    required bool stopAtLastPage,
    required int interactionPauseDelaySeconds,
    required EndOfComicAction endOfComicAction,
  }) = _AutoPageConfig;
  
  factory AutoPageConfig.fromJson(Map<String, dynamic> json) =>
      _$AutoPageConfigFromJson(json);
}

@freezed
class CacheConfig with _$CacheConfig {
  const factory CacheConfig({
    required int memoryCacheSizeMB,
    required int diskCacheSizeMB,
    required int preloadDistance,
    required ImageQuality defaultQuality,
    required bool enableAdaptiveQuality,
    required bool enableHardwareAcceleration,
  }) = _CacheConfig;
  
  factory CacheConfig.fromJson(Map<String, dynamic> json) =>
      _$CacheConfigFromJson(json);
}
```

### Comprehensive Error Handling Models

```dart
@freezed
class ReaderError with _$ReaderError {
  const factory ReaderError.fileSystem({
    required String message,
    required String filePath,
    required FileSystemErrorType type,
    String? stackTrace,
    DateTime? timestamp,
  }) = FileSystemError;
  
  const factory ReaderError.memory({
    required String message,
    required int memoryUsageMB,
    required MemoryErrorType type,
    String? stackTrace,
    DateTime? timestamp,
  }) = MemoryError;
  
  const factory ReaderError.platform({
    required String message,
    required String platformMethod,
    required PlatformErrorType type,
    String? stackTrace,
    DateTime? timestamp,
  }) = PlatformError;
  
  const factory ReaderError.archive({
    required String message,
    required String archivePath,
    required ArchiveErrorType type,
    String? stackTrace,
    DateTime? timestamp,
  }) = ArchiveError;
}

enum FileSystemErrorType {
  corruption,
  permissions,
  diskSpace,
  notFound,
  networkError,
}

enum MemoryErrorType {
  outOfMemory,
  cacheOverflow,
  imageProcessingFailure,
  isolateError,
}

class ErrorRecoveryService {
  static Future<bool> attemptRecovery(ReaderError error) async {
    return error.when(
      fileSystem: _recoverFileSystemError,
      memory: _recoverMemoryError,
      platform: _recoverPlatformError,
      archive: _recoverArchiveError,
    );
  }
  
  static Future<bool> _recoverMemoryError(
    String message,
    int memoryUsageMB,
    MemoryErrorType type,
    String? stackTrace,
    DateTime? timestamp,
  ) async {
    switch (type) {
      case MemoryErrorType.outOfMemory:
        await GetIt.instance<ICacheService>().cleanupCacheAsync();
        return true;
      case MemoryErrorType.cacheOverflow:
        await GetIt.instance<ICacheService>().clearCache();
        return true;
      default:
        return false;
    }
  }
}
```

## Error Handling

### Comprehensive Error Management with Recovery

**Error Handling Strategy**:
1. **Automatic Recovery**: Memory cleanup, cache reset, quality degradation
2. **Retry Mechanisms**: Exponential backoff for network operations
3. **Graceful Degradation**: Reduce quality/performance instead of crashing
4. **User Notification**: Clear error messages with recovery suggestions

```dart
class ReaderErrorHandler {
  final ICacheService _cacheService;
  final IReaderSettingsRepository _settingsRepository;
  final StreamController<ReaderError> _errorController = StreamController.broadcast();
  
  Stream<ReaderError> get errorStream => _errorController.stream;
  
  Future<void> handleError(ReaderError error) async {
    _errorController.add(error);
    
    final recovered = await ErrorRecoveryService.attemptRecovery(error);
    if (!recovered) {
      await _reportCriticalError(error);
    }
  }
  
  Future<void> _reportCriticalError(ReaderError error) async {
    // Log to crash reporting service
    FirebaseCrashlytics.instance.recordError(
      error,
      null,
      fatal: error.when(
        fileSystem: (_, __, type, ___, ____) => 
          type == FileSystemErrorType.corruption,
        memory: (_, __, type, ___, ____) => 
          type == MemoryErrorType.outOfMemory,
        platform: (_, __, type, ___, ____) => false,
        archive: (_, __, type, ___, ____) => false,
      ),
    );
  }
}
```

## Testing Strategy

### Unit Testing with Interface Mocks

```dart
class MockCacheService extends Mock implements ICacheService {}
class MockAutoPageService extends Mock implements IAutoPageService {}
class MockVolumeKeyService extends Mock implements IVolumeKeyService {}

void main() {
  group('ReaderBloc Tests', () {
    late ReaderBloc readerBloc;
    late MockCacheService mockCacheService;
    late MockAutoPageService mockAutoPageService;
    
    setUp(() {
      mockCacheService = MockCacheService();
      mockAutoPageService = MockAutoPageService();
      
      readerBloc = ReaderBloc(
        cacheService: mockCacheService,
        autoPageService: mockAutoPageService,
        settingsRepository: mockSettingsRepository,
      );
    });
    
    test('should preload pages when navigation occurs', () async {
      // Arrange
      when(mockCacheService.preloadPages(any, any))
          .thenAnswer((_) async {});
      
      // Act
      readerBloc.add(const ReaderEvent.pageChanged(5));
      
      // Assert
      verify(mockCacheService.preloadPages(
        argThat(isA<List<String>>()),
        5,
      )).called(1);
    });
  });
}
```

### Performance Testing Requirements

```dart
void main() {
  group('Performance Tests', () {
    testWidgets('page transitions maintain 60fps', (tester) async {
      final performanceMonitor = PerformanceMonitor();
      
      await tester.pumpWidget(TestReaderApp());
      await performanceMonitor.startMonitoring();
      
      // Simulate rapid page navigation
      for (int i = 0; i < 10; i++) {
        await tester.tap(find.byType(NextPageButton));
        await tester.pump();
      }
      
      final metrics = await performanceMonitor.getMetrics();
      expect(metrics.averageFPS, greaterThanOrEqualTo(58.0));
    });
    
    testWidgets('memory usage stays within limits', (tester) async {
      final memoryMonitor = MemoryMonitor();
      
      await tester.pumpWidget(TestReaderApp());
      await memoryMonitor.startMonitoring();
      
      // Simulate extended reading session
      for (int i = 0; i < 100; i++) {
        await tester.tap(find.byType(NextPageButton));
        await tester.pump();
      }
      
      final memoryUsage = await memoryMonitor.getCurrentUsage();
      expect(memoryUsage.totalMB, lessThanOrEqualTo(100));
    });
  });
}
```

## Implementation Priorities

### Phase 1: Critical Interface Fixes (HIGH PRIORITY)
1. Fix missing `preloadPages` and `setImageQuality` methods in ICacheService
2. Implement proper dependency injection in ReaderCore constructor
3. Create ReadingModeRenderer base class with abstract methods
4. Eliminate GestureType enum duplication
5. Add background isolate processing for image operations

### Phase 2: Architecture Improvements (HIGH PRIORITY)
1. Remove tight coupling in ReaderCore (no direct AutoPageManager instantiation)
2. Implement comprehensive error recovery mechanisms
3. Add platform channel integration with proper validation
4. Create hardware acceleration configuration
5. Implement priority-based preloading engine

### Phase 3: Testing and Quality (MEDIUM PRIORITY)
1. Achieve 90% unit test coverage with interface mocks
2. Implement performance benchmarking with frame rate monitoring
3. Add accessibility testing with WCAG compliance verification
4. Create integration tests for complete workflows
5. Add automated performance regression detection

This refined design document addresses all critical gaps identified in the quality validation and provides precise implementation guidance for achieving professional-grade comic reader functionality with proper architecture patterns and interface contracts.