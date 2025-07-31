# Reader Enhancements Design Document

## Overview

This design document outlines the technical architecture for implementing enhanced reading features in the Easy Comic Flutter application. The design extends the existing ReaderPage architecture while maintaining compatibility with the current Drift database, Riverpod state management, and WebDAV sync infrastructure.

## Architecture

### High-Level Architecture

The reader enhancement architecture follows a layered approach:

```
┌─────────────────────────────────────────────────────────┐
│                    UI Layer                             │
├─────────────────────────────────────────────────────────┤
│  ReaderPage │ ReaderSettings │ PageManager │ Controls    │
├─────────────────────────────────────────────────────────┤
│                 State Management                        │
├─────────────────────────────────────────────────────────┤
│ ReaderStateNotifier │ SettingsProvider │ BookmarkProvider│
├─────────────────────────────────────────────────────────┤
│                Service Layer                            │
├─────────────────────────────────────────────────────────┤
│ ReaderModeService │BrightnessService │ ThumbnailService  │
├─────────────────────────────────────────────────────────┤
│                 Data Layer                              │
├─────────────────────────────────────────────────────────┤
│   DriftDb + New Tables │ ReaderSettings │ PageCache      │
└─────────────────────────────────────────────────────────┘
```

### Strategy Pattern Implementation

Reading modes will be implemented using the Strategy pattern to ensure clean separation of concerns:

```dart
abstract class ReadingStrategy {
  Widget buildReader(List<Uint8List> pages, ReaderState state);
  void handlePageChange(int page);
  void handleGesture(GestureDetails details);
}

class SinglePageStrategy extends ReadingStrategy { ... }
class DualPageStrategy extends ReadingStrategy { ... }
class VerticalScrollStrategy extends ReadingStrategy { ... }
```

## Components and Interfaces

### 1. Extended Database Schema

**New Tables**:

```dart
// Reader settings table
class ReaderSettings extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get userId => text().nullable()(); // For future multi-user support
  TextColumn get readingMode => text()(); // 'single', 'dual', 'vertical'
  TextColumn get backgroundColor => text()();
  TextColumn get transitionType => text()();
  RealColumn get brightness => real()();
  BoolColumn get showThumbnails => boolean()();
  DateTimeColumn get updatedAt => dateTime()();
  TextColumn get etag => text().nullable()(); // For WebDAV sync
}

// Page custom ordering
class PageCustomOrder extends Table {
  IntColumn get id => integer().autoIncrement()();
  IntColumn get comicId => integer().references(Comics, #id)();
  IntColumn get originalIndex => integer()();
  IntColumn get customIndex => integer()();
  DateTimeColumn get createdAt => dateTime()();
}

// Enhanced reading history
class ReadingHistory extends Table {
  IntColumn get id => integer().autoIncrement()();
  IntColumn get comicId => integer().references(Comics, #id)();
  IntColumn get lastPageRead => integer()();
  DateTimeColumn get lastReadAt => dateTime()();
  IntColumn get totalTimeSeconds => integer()();
  TextColumn get sessionId => text()(); // For session grouping
}

// Enhanced bookmarks with thumbnails
class BookmarkThumbnails extends Table {
  IntColumn get id => integer().autoIncrement()();
  IntColumn get bookmarkId => integer().references(Bookmarks, #id)();
  TextColumn get thumbnailPath => text()(); // Local file path
  DateTimeColumn get createdAt => dateTime()();
}
```

### 2. Reader State Management

**ReaderStateNotifier**:
```dart
class ReaderState {
  final ReadingMode mode;
  final NavigationDirection direction;
  final Color backgroundColor;
  final TransitionType transitionType;
  final double brightness;
  final bool showThumbnails;
  final List<int> customPageOrder;
  final Map<int, String> thumbnailCache;
}

class ReaderStateNotifier extends StateNotifier<ReaderState> {
  final DriftDb db;
  final int comicId;
  
  Future<void> setReadingMode(ReadingMode mode);
  Future<void> setBackgroundColor(Color color);
  Future<void> setBrightness(double brightness);
  Future<void> updateCustomPageOrder(List<int> order);
  Future<void> generateThumbnails();
}
```

### 3. Service Layer

**ReaderModeService**:
```dart
class ReaderModeService {
  ReadingStrategy getStrategy(ReadingMode mode) {
    switch (mode) {
      case ReadingMode.single:
        return SinglePageStrategy();
      case ReadingMode.dual:
        return DualPageStrategy();
      case ReadingMode.vertical:
        return VerticalScrollStrategy();
    }
  }
  
  Widget buildReaderWidget(ReadingStrategy strategy, List<Uint8List> pages, ReaderState state);
}
```

**BrightnessService**:
```dart
class BrightnessService {
  static const String _brightnessKey = 'reader_brightness';
  
  Future<void> setBrightness(double brightness);
  Future<double> getBrightness();
  void applyBrightnessOverlay(Widget child, double brightness);
}
```

**ThumbnailService**:
```dart
class ThumbnailService {
  final String _cacheDir;
  final int _maxCacheSize = 50 * 1024 * 1024; // 50MB
  
  Future<String?> generateThumbnail(Uint8List imageData, String key);
  Future<void> cleanupCache();
  Future<String?> getCachedThumbnail(String key);
}
```

### 4. Enhanced UI Components

**ReaderPage Enhancements**:
```dart
class _ReaderPageState extends ConsumerState<ReaderPage> {
  late ReadingStrategy _currentStrategy;
  Timer? _brightnessTimer;
  
  @override
  Widget build(BuildContext context) {
    final readerState = ref.watch(readerStateProvider(widget.comicId));
    
    return Scaffold(
      body: BrightnessOverlay(
        brightness: readerState.brightness,
        child: Container(
          color: readerState.backgroundColor,
          child: _buildReaderContent(readerState),
        ),
      ),
    );
  }
  
  Widget _buildReaderContent(ReaderState state) {
    return _currentStrategy.buildReader(_pages, state);
  }
}
```

**Enhanced Progress Bar**:
```dart
class ThumbnailProgressBar extends ConsumerWidget {
  Widget build(BuildContext context, WidgetRef ref) {
    return SliderTheme(
      data: SliderTheme.of(context).copyWith(
        thumbShape: ThumbnailSliderThumb(),
      ),
      child: Slider(
        value: currentPage.toDouble(),
        max: totalPages.toDouble() - 1,
        onChanged: _handleSliderChange,
        onChangeEnd: _handleSliderEnd,
      ),
    );
  }
}
```

**Page Manager Interface**:
```dart
class PageManagerDialog extends StatefulWidget {
  Widget build(BuildContext context) {
    return Dialog(
      child: Container(
        width: MediaQuery.of(context).size.width * 0.9,
        height: MediaQuery.of(context).size.height * 0.8,
        child: Column(
          children: [
            _buildHeader(),
            Expanded(child: _buildPageGrid()),
            _buildFooter(),
          ],
        ),
      ),
    );
  }
  
  Widget _buildPageGrid() {
    return ReorderableGridView.builder(
      onReorder: _handlePageReorder,
      itemBuilder: _buildPageTile,
      itemCount: pages.length,
    );
  }
}
```

## Data Models

### ReaderSettings Model

```dart
class ReaderSettingsData {
  final int id;
  final String? userId;
  final ReadingMode readingMode;
  final Color backgroundColor;
  final TransitionType transitionType;
  final double brightness;
  final bool showThumbnails;
  final DateTime updatedAt;
  final String? etag;
}

enum ReadingMode { single, dual, vertical }
enum TransitionType { none, slide, fade, curl }
enum NavigationDirection { horizontal, vertical }
```

### Enhanced Bookmark Model

```dart
class EnhancedBookmark {
  final int id;
  final int comicId;
  final int pageIndex;
  final String? label;
  final String? thumbnailPath;
  final DateTime createdAt;
}
```

### Page Order Model

```dart
class PageOrderEntry {
  final int originalIndex;
  final int customIndex;
  final String thumbnailKey;
}
```

## Error Handling

### Error Types and Handling

1. **Thumbnail Generation Errors**:
   - Graceful fallback to placeholder images
   - Background retry mechanism
   - Memory pressure handling

2. **Settings Persistence Errors**:
   - Local fallback when sync fails
   - Validation of corrupted settings
   - Default settings restoration

3. **Page Ordering Errors**:
   - Validation of custom order integrity
   - Fallback to filename-based ordering
   - Duplicate index resolution

### Error Recovery Strategies

```dart
class ReaderErrorHandler {
  static Future<T> withErrorHandling<T>(
    Future<T> Function() operation,
    T fallbackValue,
    String errorContext,
  ) async {
    try {
      return await operation();
    } catch (e, stackTrace) {
      await FirebaseCrashlytics.instance.recordError(
        e,
        stackTrace,
        context: errorContext,
      );
      return fallbackValue;
    }
  }
}
```

## Testing Strategy

### Unit Testing

1. **State Management Tests**:
   - ReaderStateNotifier state transitions
   - Settings persistence and restoration
   - Custom page order validation

2. **Service Layer Tests**:
   - Strategy pattern implementation
   - Thumbnail generation and caching
   - Brightness control functionality

3. **Database Tests**:
   - New table schema validation
   - Migration testing from existing schema
   - Data integrity checks

### Integration Testing

1. **Reader Flow Tests**:
   - End-to-end reading mode switching
   - Settings synchronization with WebDAV
   - Performance under different page counts

2. **UI Component Tests**:
   - Gesture recognition accuracy
   - Animation smoothness validation
   - Memory usage during thumbnail operations

### Widget Testing

1. **ReaderPage Widget Tests**:
   - Strategy switching behavior
   - UI state consistency
   - Error state handling

2. **Custom Widget Tests**:
   - ThumbnailProgressBar functionality
   - PageManagerDialog behavior
   - Settings panel interactions

## Performance Considerations

### Memory Management

1. **Thumbnail Caching**:
   - LRU cache with size limits
   - Automatic cleanup on memory pressure
   - Lazy loading of thumbnails

2. **Page Loading**:
   - Preload adjacent pages in background
   - Unload distant pages to free memory
   - Efficient image compression for thumbnails

### Database Optimization

1. **Query Optimization**:
   - Indexed columns for frequent queries
   - Batch operations for page reordering
   - Efficient joins for complex queries

2. **Sync Optimization**:
   - Incremental sync for settings changes
   - ETag-based conflict resolution
   - Background sync for non-critical data

## Security Considerations

1. **File System Access**:
   - Validate thumbnail file paths
   - Secure temporary file handling
   - Permission checks for cache directory

2. **Settings Validation**:
   - Input sanitization for custom settings
   - Range validation for numeric values
   - Type safety for enumerated values

3. **WebDAV Sync Security**:
   - Encrypted transmission of settings
   - Authentication token validation
   - Rate limiting for sync operations