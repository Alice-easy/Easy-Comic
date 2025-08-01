# Comic Reader Viewer Component - Implementation Tasks

This document provides a comprehensive implementation roadmap for enhancing the Flutter-based comic reader application's core viewer component. Based on quality validation feedback identifying critical interface integration gaps, tight coupling issues, and performance bottlenecks, this task list provides specific, actionable implementation steps to achieve professional-grade functionality.

## Phase 1: Critical Interface Fixes (HIGH PRIORITY)

### 1. Fix Missing ICacheService Interface Methods

- [ ] 1.1 Add missing preloadPages method to ICacheService interface
  - Open `lib/domain/repositories/cache_service.dart` (or create if doesn't exist)
  - Add `Future<void> preloadPages(List<String> pageKeys, int currentPageIndex)` method signature to ICacheService abstract class
  - Add comprehensive documentation explaining priority-based queue management (next 3 pages high priority, next 5 medium)
  - Ensure method returns Future<void> to allow for asynchronous background processing
  - Addresses requirements 1.1, 8.3

- [ ] 1.2 Add missing setImageQuality method to ICacheService interface
  - Add `Future<void> setImageQuality(String pageKey, ImageQuality quality)` method signature to ICacheService interface
  - Define ImageQuality enum with values: thumbnail, medium, high, original
  - Add documentation explaining progressive loading strategy (thumbnail → medium → full resolution)
  - Ensure method supports background processing without UI thread blocking
  - Addresses requirements 1.2, 8.5

- [ ] 1.3 Add missing cleanupCacheAsync method to ICacheService interface
  - Add `Future<void> cleanupCacheAsync()` method signature to ICacheService interface
  - Add documentation specifying this method must run in background isolate using compute() function
  - Ensure method handles memory pressure cleanup without blocking UI thread
  - Add return type documentation for proper error handling
  - Addresses requirements 3.3, 8.4

- [ ] 1.4 Implement missing methods in concrete CacheService class
  - Open `lib/core/services/cache_service.dart`
  - Implement all missing interface methods with proper @override annotations
  - Create PreloadingEngine class with priority-based queue system
  - Implement BackgroundProcessor class for isolate-based image processing
  - Add proper error handling with try-catch blocks and specific exception types
  - Addresses requirements 1.1, 1.2, 3.3

### 2. Eliminate Architecture Coupling Issues

- [ ] 2.1 Remove direct AutoPageManager instantiation from ReaderCore
  - Open `lib/presentation/widgets/reader_core.dart`
  - Remove any direct instantiation of AutoPageManager class (e.g., `AutoPageManager()`)
  - Add IAutoPageService parameter to ReaderCore constructor
  - Update ReaderCore constructor to accept IAutoPageService autoPageService parameter
  - Replace all AutoPageManager references with widget.autoPageService calls
  - Addresses requirements 2.1

- [ ] 2.2 Fix ReaderCore constructor with proper dependency injection
  - Update ReaderCore constructor to include all required interface dependencies:
    - `required IAutoPageService autoPageService`
    - `required ICacheService cacheService`
    - `required IVolumeKeyService volumeKeyService`
    - `required IGestureConfigService gestureConfigService`
  - Remove any GetIt.instance calls from within widget build methods
  - Ensure all service access goes through constructor-injected interfaces only
  - Addresses requirements 2.4

- [ ] 2.3 Update ReaderScreen to provide dependencies to ReaderCore
  - Open `lib/presentation/pages/reader_screen.dart`
  - Update ReaderCore instantiation to pass all required service interfaces:
    ```dart
    ReaderCore(
      autoPageService: GetIt.instance<IAutoPageService>(),
      cacheService: GetIt.instance<ICacheService>(),
      volumeKeyService: GetIt.instance<IVolumeKeyService>(),
      gestureConfigService: GetIt.instance<IGestureConfigService>(),
      // ... other parameters
    )
    ```
  - Ensure GetIt calls are only at the dependency injection boundary (widget creation)
  - Addresses requirements 2.1, 2.4

### 3. Create ReadingModeRenderer Base Class

- [ ] 3.1 Define ReadingModeRenderer abstract base class
  - Create `lib/presentation/renderers/reading_mode_renderer.dart`
  - Define abstract class with required constructor parameters:
    - `required ReaderSettings settings`
    - `required INavigationService navigationService`
  - Define abstract methods that must be implemented:
    - `Widget buildView(List<ComicPage> pages, int currentPageIndex)`
    - `Future<void> handlePageNavigation(NavigationDirection direction)`
    - `Future<void> preserveReadingPosition(int currentPage, double scrollOffset)`
  - Add shared functionality method: `void onPageChanged(int pageIndex)`
  - Addresses requirements 2.2

- [ ] 3.2 Implement HorizontalModeRenderer extending ReadingModeRenderer
  - Create `lib/presentation/renderers/horizontal_mode_renderer.dart`
  - Implement buildView method using PhotoViewGallery.builder with proper configuration:
    - Set reverse: true for RTL mode based on settings.readingMode
    - Configure minScale: PhotoViewComputedScale.contained
    - Configure maxScale: PhotoViewComputedScale.covered * 3.0
    - Add proper onPageChanged callback
  - Implement handlePageNavigation with RTL logic handling
  - Implement preserveReadingPosition using navigationService
  - Addresses requirements 2.2

- [ ] 3.3 Implement VerticalModeRenderer extending ReadingModeRenderer
  - Create `lib/presentation/renderers/vertical_mode_renderer.dart`
  - Implement buildView method using PageView with vertical scrolling:
    - Set scrollDirection: Axis.vertical
    - Configure proper page transitions
    - Add zoom capabilities for individual pages
  - Implement handlePageNavigation for vertical navigation
  - Implement preserveReadingPosition with scroll offset tracking
  - Addresses requirements 2.2

- [ ] 3.4 Implement WebtoonModeRenderer extending ReadingModeRenderer
  - Create `lib/presentation/renderers/webtoon_mode_renderer.dart`
  - Implement buildView method using CustomScrollView with SliverList:
    - Create continuous vertical scrolling without page breaks
    - Implement proper image sizing for webtoon format
    - Add scroll position tracking for reading progress
  - Implement handlePageNavigation for smooth scrolling to pages
  - Implement preserveReadingPosition with precise scroll offset
  - Addresses requirements 2.2

### 4. Eliminate GestureType Enum Duplication

- [ ] 4.1 Create single GestureType enum in domain layer
  - Create `lib/domain/entities/gesture_types.dart`
  - Define comprehensive GestureType enum with all gesture types:
    - tapLeft, tapRight, tapCenter
    - pinchZoomIn, pinchZoomOut
    - panStart, panUpdate, panEnd
    - doubleTapZoom, volumeUp, volumeDown
    - longPress, swipeUp, swipeDown, swipeLeft, swipeRight
  - Create GestureEvent class with proper data structure
  - Addresses requirements 2.3

- [ ] 4.2 Remove duplicate GestureType definitions across files
  - Search all files for GestureType enum definitions using `flutter analyze` or grep
  - Remove duplicate enum definitions in presentation layer files
  - Update all imports to use the single domain layer GestureType enum
  - Update all gesture handling code to use consistent enum values
  - Ensure no compilation errors after consolidation
  - Addresses requirements 2.3

## Phase 2: Background Processing and Performance (HIGH PRIORITY)

### 5. Implement Isolate-Based Background Processing

- [ ] 5.1 Create BackgroundProcessor class for image processing
  - Create `lib/core/background/background_processor.dart`
  - Implement Isolate.spawn() for dedicated image processing isolate:
    - Define _imageProcessingEntryPoint static method
    - Set up ReceivePort and SendPort communication channels
    - Add proper isolate naming for debugging ('ImageProcessorIsolate')
  - Create ImageProcessingRequest and ImageProcessingResponse data classes
  - Implement proper isolate lifecycle management (start, stop, restart on errors)
  - Addresses requirements 3.1, 8.5

- [ ] 5.2 Implement priority-based preloading engine
  - Create `lib/core/preloading/preloading_engine.dart`
  - Implement priority queue system with separate queues:
    - High priority queue for next 3 pages
    - Medium priority queue for next 5 pages
    - Low priority queue for remaining pages
  - Add background isolate integration for processing queued pages
  - Implement cancellation support for queue items when page changes rapidly
  - Add queue size limits and memory pressure handling
  - Addresses requirements 3.2, 8.3

- [ ] 5.3 Implement hardware acceleration configuration
  - Update CacheService implementation to include configureHardwareAcceleration method
  - Add Flutter rendering pipeline configuration for hardware acceleration:
    - Configure RenderObject settings for GPU acceleration
    - Set appropriate FilterQuality for hardware-accelerated rendering
    - Add platform-specific optimizations (Android hardware layers, iOS Metal)
  - Create HardwareAccelerationConfig class with platform detection
  - Addresses requirements 3.5

### 6. Platform Channel Integration for Volume Keys

- [ ] 6.1 Create Android platform channel implementation
  - Open `android/app/src/main/kotlin/MainActivity.kt`
  - Add MethodChannel for 'comic_reader/volume_keys'
  - Implement volume key event interception using KeyEvent.KEYCODE_VOLUME_UP/DOWN
  - Add proper key event handling with debouncing (minimum 200ms between events)
  - Implement volume key navigation enable/disable functionality
  - Add error handling for platform-specific exceptions
  - Addresses requirements 4.1, 9.4

- [ ] 6.2 Create iOS platform channel implementation
  - Open `ios/Runner/AppDelegate.swift`
  - Add MethodChannel for 'comic_reader/volume_keys'
  - Configure AVAudioSession category for volume key interception
  - Implement volume key event handling with proper iOS key codes
  - Add debouncing logic to prevent multiple rapid key presses
  - Handle iOS-specific volume key behavior and system integration
  - Addresses requirements 4.2, 9.4

- [ ] 6.3 Implement Flutter-side VolumeKeyService
  - Create `lib/core/services/volume_key_service.dart`
  - Implement IVolumeKeyService interface with all required methods:
    - `Stream<VolumeKeyEvent> get keyEventStream`
    - `Future<void> enableVolumeKeyNavigation(bool enabled)`
    - `Future<bool> get isVolumeKeyNavigationSupported`
  - Add proper MethodChannel integration with error handling
  - Implement VolumeKeyEvent data class with timestamp and key type
  - Add platform-specific exception handling and fallback behavior
  - Addresses requirements 4.3, 4.5

### 7. Input Validation and Security

- [ ] 7.1 Implement file path validation and security
  - Create `lib/core/security/file_path_validator.dart`
  - Add directory traversal attack prevention:
    - Validate file paths don't contain '../' sequences
    - Ensure paths are within allowed application directories
    - Add whitelist of allowed file extensions for comic files
  - Implement path normalization before processing
  - Add file size validation to prevent memory exhaustion attacks
  - Create comprehensive error messages for validation failures
  - Addresses requirements 4.4

- [ ] 7.2 Add comprehensive error handling for platform channels
  - Update VolumeKeyService with proper PlatformException handling
  - Add specific error codes for different failure scenarios:
    - Platform not supported (return false gracefully)
    - Permission denied (provide user guidance)
    - Hardware not available (fallback to touch navigation)
  - Implement retry mechanisms for transient platform errors
  - Add logging for debugging platform integration issues
  - Addresses requirements 4.5, 5.1

## Phase 3: Error Recovery and Retry Mechanisms (HIGH PRIORITY)

### 8. Implement Comprehensive Error Recovery

- [ ] 8.1 Create exponential backoff retry mechanism
  - Create `lib/core/error/retry_mechanism.dart`
  - Implement ExponentialBackoffRetry class with configurable parameters:
    - Maximum 3 attempts for network operations
    - Increasing delays: 1s, 2s, 4s between attempts
    - Jitter addition to prevent thundering herd
  - Add specific retry logic for different error types (network, file system, memory)
  - Implement CancelToken support for user-initiated cancellation
  - Addresses requirements 5.1

- [ ] 8.2 Implement database corruption recovery
  - Create `lib/core/error/database_recovery.dart`
  - Add database integrity checking using `PRAGMA integrity_check`
  - Implement automatic database repair for recoverable corruption:
    - Recreate corrupted tables with schema migration
    - Restore from backup if available
    - Reset to defaults with user notification if repair fails
  - Add database backup creation before risky operations
  - Create comprehensive error reporting for database issues
  - Addresses requirements 5.4

- [ ] 8.3 Implement memory pressure recovery
  - Update CacheService with automatic memory pressure handling:
    - Monitor device memory usage using platform-specific APIs
    - Implement automatic cache cleanup when memory usage > 80%
    - Reduce image quality automatically during memory pressure
    - Cancel non-essential background tasks when memory is low
  - Add graceful degradation instead of app crashes
  - Create user notification for memory-related performance reductions
  - Addresses requirements 5.3, 8.4

### 9. Archive Format Support Enhancement

- [ ] 9.1 Enhance archive validation and recovery
  - Update `lib/core/comic_archive.dart` with comprehensive validation:
    - Verify archive headers before extraction
    - Detect common corruption patterns (truncated files, invalid headers)
    - Implement partial extraction for corrupted archives
  - Add specific error messages with recovery suggestions:
    - "Archive appears truncated - try re-downloading"
    - "Password-protected archive detected - enter password"
    - "Unsupported compression method - try different tool"
  - Create progress reporting through Stream<ExtractionProgress>
  - Addresses requirements 7.3, 7.4

- [ ] 9.2 Implement proper archive progress reporting
  - Add CancelToken support to archive extraction operations
  - Create ExtractionProgress data class with percentage and current file info
  - Implement background isolate extraction with progress updates
  - Add user-friendly progress UI with cancellation option
  - Handle extraction cancellation gracefully without corrupting cache
  - Addresses requirements 7.4, 9.5

## Phase 4: Testing Implementation (MEDIUM PRIORITY)

### 10. Unit Testing with Interface Mocks

- [ ] 10.1 Create comprehensive service interface mocks
  - Create `test/mocks/mock_services.dart`
  - Implement mock classes for all service interfaces:
    - MockCacheService extends Mock implements ICacheService
    - MockAutoPageService extends Mock implements IAutoPageService
    - MockVolumeKeyService extends Mock implements IVolumeKeyService
    - MockNavigationService extends Mock implements INavigationService
  - Add proper when/verify setup for all interface methods
  - Addresses requirements 6.1

- [ ] 10.2 Write unit tests for ReaderBloc with interface dependencies
  - Create `test/presentation/bloc/reader_bloc_test.dart`
  - Test ReaderBloc with injected mock services:
    - Verify preloadPages is called on page navigation
    - Test auto-page functionality with mock timer events
    - Verify volume key events trigger proper navigation
    - Test error handling with mock service failures
  - Achieve minimum 90% test coverage for ReaderBloc
  - Addresses requirements 6.1

- [ ] 10.3 Write unit tests for background processing
  - Create `test/core/background/background_processor_test.dart`
  - Test isolate lifecycle management (start, stop, restart)
  - Test image processing request/response communication
  - Test error handling in background isolate
  - Test memory cleanup operations
  - Verify isolate cancellation works properly
  - Addresses requirements 6.1

### 11. Performance Testing Implementation

- [ ] 11.1 Create performance monitoring infrastructure
  - Create `test/performance/performance_monitor.dart`
  - Implement PerformanceMonitor class with frame rate tracking:
    - Monitor rendering performance during page transitions
    - Track memory usage patterns during extended reading
    - Measure cache hit/miss ratios and response times
  - Add performance regression detection with baseline comparisons
  - Create automated performance test reporting
  - Addresses requirements 6.3

- [ ] 11.2 Implement memory usage testing
  - Create `test/performance/memory_test.dart`
  - Test memory usage during extended reading sessions:
    - Load 100+ pages and verify memory stays under 100MB
    - Test memory pressure handling and automatic cleanup
    - Verify no memory leaks during repeated page navigation
  - Add memory profiling for identifying optimization opportunities
  - Test background isolate memory isolation
  - Addresses requirements 6.3

- [ ] 11.3 Create frame rate consistency testing
  - Create `test/performance/frame_rate_test.dart`
  - Test page transition smoothness:
    - Verify 60fps maintenance during rapid page changes
    - Test zoom animation performance under load
    - Measure gesture response times (target < 16ms)
  - Add automated frame drop detection and reporting
  - Test performance under various device specifications
  - Addresses requirements 6.3, 9.1

## Phase 5: Accessibility and Platform Integration (MEDIUM PRIORITY)

### 12. Accessibility Implementation

- [ ] 12.1 Add comprehensive screen reader support
  - Update all reader UI components with proper Semantics widgets:
    - Add meaningful descriptions for comic pages ("Page 5 of 25", "Chapter 1")
    - Provide reading progress announcements ("Progress: 20%")
    - Add navigation action descriptions ("Next page", "Previous page")
  - Implement page change announcements for assistive technologies
  - Add support for reading mode announcements ("Switched to manga mode")
  - Test with TalkBack (Android) and VoiceOver (iOS)
  - Addresses requirements 12.1

- [ ] 12.2 Implement keyboard navigation support
  - Add keyboard shortcut handling to ReaderCore:
    - Arrow keys for page navigation (left/right, up/down based on mode)
    - Plus/minus keys for zoom control
    - Tab key for UI control focus management
    - Space bar for auto-page toggle
  - Add focus management for proper keyboard navigation flow
  - Create visual focus indicators for keyboard users
  - Test keyboard navigation across all reading modes
  - Addresses requirements 12.2

### 13. Platform-Specific Optimizations

- [ ] 13.1 Enhance Android platform integration
  - Improve native brightness control in `android/app/src/main/kotlin/`:
    - Add proper brightness permission handling
    - Implement smooth brightness transitions
    - Add system brightness vs overlay brightness options
  - Enhance volume key integration with proper hardware key priority
  - Implement Android immersive mode for true fullscreen:
    - Hide system navigation bar and status bar
    - Add proper gesture navigation support
    - Handle Android version differences gracefully
  - Addresses design document Android optimizations

- [ ] 13.2 Implement iOS platform enhancements
  - Create Swift brightness control implementation:
    - Add proper iOS brightness API integration
    - Handle iOS brightness limitations and permissions
    - Implement smooth brightness animation matching iOS standards
  - Add iOS-style navigation gestures and transitions:
    - Implement swipe-back gesture for page navigation
    - Add iOS-style zoom animations and bounce effects
    - Integrate with iOS system gesture recognizers properly
  - Addresses design document iOS optimizations

## Implementation Guidelines

### Code Generation Requirements
After implementing database schema changes, freezed models, or interface modifications:
```bash
flutter packages pub run build_runner build --delete-conflicting-outputs
```

### Testing Requirements
- Achieve minimum 90% unit test coverage for all service implementations
- All interface methods must have corresponding test coverage
- Performance tests must verify 60fps maintenance and memory limits
- Integration tests must cover complete user workflows
- Error handling tests must verify recovery mechanisms

### Quality Metrics
- **Performance**: 60fps during transitions, <100MB memory usage, <16ms gesture response
- **Architecture**: Zero direct service instantiations in widgets, 100% interface-based dependencies
- **Error Handling**: All failure scenarios must have recovery mechanisms with user feedback
- **Platform Integration**: All platform channels must include proper error handling and validation

### Development Workflow
1. Fix critical interface issues (Phase 1) - these are blocking for other work
2. Implement background processing and performance optimizations (Phase 2)
3. Add comprehensive error recovery (Phase 3)
4. Ensure thorough testing coverage (Phase 4)
5. Polish with accessibility and platform features (Phase 5)

Each task must be completed with proper error handling, comprehensive testing, and documentation. Interface implementations must be validated against their contracts, and all dependencies must be properly injected rather than directly instantiated.

This refined implementation roadmap addresses all critical gaps identified in the quality validation feedback and provides specific, actionable steps to achieve professional-grade comic reader functionality.