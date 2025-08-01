# Easy Comic Reader - Design Document

## 1. Overview

Easy Comic Reader is a Flutter-based cross-platform application implementing Clean Architecture principles with BLoC state management, Drift ORM for data persistence, and WebDAV synchronization. The application provides a comprehensive comic reading experience with intelligent caching, multi-device synchronization, and extensive customization options.

### 1.1 Architecture Philosophy
- **Local-First Design**: Prioritizes offline functionality with background synchronization
- **Clean Architecture**: Separates concerns into distinct layers (Presentation, Domain, Data)
- **Reactive Programming**: Uses streams and BLoC pattern for state management
- **Cross-Platform**: Single codebase supporting Android, iOS, and Windows

## 2. System Architecture

### 2.1 Layer Architecture

```
┌─────────────────────────────────────────┐
│           Presentation Layer            │
│  (UI Components, BLoCs, Pages, Widgets) │
├─────────────────────────────────────────┤
│            Domain Layer                 │
│   (Entities, Repositories, Services)    │
├─────────────────────────────────────────┤
│             Data Layer                  │
│  (Repositories, DataSources, Models)    │
├─────────────────────────────────────────┤
│             Core Layer                  │
│   (Services, Utils, Constants)          │
└─────────────────────────────────────────┘
```

### 2.2 Dependency Flow
- **Presentation** depends on **Domain**
- **Data** implements **Domain** interfaces
- **Core** provides shared utilities across all layers
- **Domain** has no dependencies on other layers

### 2.3 Key Architectural Patterns
- **Repository Pattern**: Abstracts data access logic
- **Service Locator**: Dependency injection using GetIt
- **BLoC Pattern**: Reactive state management
- **Factory Pattern**: Creating specialized services and handlers

## 3. Core Components and Interfaces

### 3.1 Database Schema Design

#### 3.1.1 Core Tables
```sql
-- Comics table: Primary comic metadata
Comics (
  id INTEGER PRIMARY KEY,
  filePath TEXT UNIQUE NOT NULL,
  fileName TEXT NOT NULL,
  coverImage TEXT NULL,
  addedAt DATETIME NOT NULL,
  isFavorite BOOLEAN DEFAULT FALSE,
  lastReadAt DATETIME NULL,
  progress REAL DEFAULT 0.0
)

-- Reading progress with sync support  
ComicProgress (
  id INTEGER PRIMARY KEY,
  fileHash TEXT UNIQUE NOT NULL,
  currentPage INTEGER NOT NULL,
  totalPages INTEGER NOT NULL,
  updatedAt DATETIME NOT NULL,
  etag TEXT NULL -- WebDAV sync ETag
)

-- Visual bookmarks with thumbnails
Bookmarks (
  id INTEGER PRIMARY KEY,
  comicId INTEGER REFERENCES Comics(id),
  pageIndex INTEGER NOT NULL,
  label TEXT NULL,
  createdAt DATETIME NOT NULL
)

-- Reader settings with sync capability
ReaderSettings (
  id INTEGER PRIMARY KEY,
  userId TEXT NULL,
  readingMode TEXT DEFAULT 'single',
  navigationDirection TEXT DEFAULT 'horizontal',
  backgroundTheme TEXT DEFAULT 'black',
  transitionType TEXT DEFAULT 'none',
  brightness REAL DEFAULT 1.0,
  showThumbnails BOOLEAN DEFAULT TRUE,
  updatedAt DATETIME NOT NULL,
  etag TEXT NULL -- WebDAV sync ETag
)
```

#### 3.1.2 Extended Tables
```sql
-- Custom page ordering for drag-and-drop
PageCustomOrder (
  comicId INTEGER REFERENCES Comics(id),
  originalIndex INTEGER NOT NULL,
  customIndex INTEGER NOT NULL,
  createdAt DATETIME NOT NULL,
  PRIMARY KEY (comicId, originalIndex)
)

-- Reading session tracking
ReadingSessions (
  id INTEGER PRIMARY KEY,
  fileHash TEXT NOT NULL,
  startTime DATETIME NOT NULL,
  endTime DATETIME NOT NULL,
  durationInSeconds INTEGER NOT NULL
)

-- Enhanced reading history
ReadingHistory (
  id INTEGER PRIMARY KEY,
  comicId INTEGER REFERENCES Comics(id),
  lastPageRead INTEGER NOT NULL,
  lastReadAt DATETIME NOT NULL,
  totalTimeSeconds INTEGER NOT NULL,
  sessionId TEXT NOT NULL
)

-- Bookmark visual thumbnails
BookmarkThumbnails (
  id INTEGER PRIMARY KEY,
  bookmarkId INTEGER REFERENCES Bookmarks(id),
  thumbnailPath TEXT NOT NULL,
  createdAt DATETIME NOT NULL
)
```

### 3.2 State Management Architecture

#### 3.2.1 BLoC Structure
```dart
// Main Reader BLoC
class ReaderBloc extends Bloc<ReaderEvent, ReaderState> {
  final ComicRepository comicRepository;
  final SettingsRepository settingsRepository;
  final BookmarkRepository bookmarkRepository;
  final AutoPageService autoPageService;
  final CacheService cacheService;
  final ComicArchive comicArchive;
}

// Reader Events
abstract class ReaderEvent extends Equatable {}
class LoadComicEvent extends ReaderEvent {}
class NavigateToPageEvent extends ReaderEvent {}
class ZoomChangedEvent extends ReaderEvent {}
class SettingsChangedEvent extends ReaderEvent {}
class BookmarkAddedEvent extends ReaderEvent {}

// Reader States  
abstract class ReaderState extends Equatable {}
class ReaderInitial extends ReaderState {}
class ReaderLoading extends ReaderState {}
class ReaderLoaded extends ReaderState {}
class ReaderError extends ReaderState {}
```

#### 3.2.2 Additional BLoCs
- **BookshelfBloc**: Comic library management
- **SyncBloc**: WebDAV synchronization state
- **SettingsBloc**: Application settings management
- **BookmarkBloc**: Bookmark operations

### 3.3 Repository Interfaces

#### 3.3.1 Comic Repository
```dart
abstract class ComicRepository {
  Future<Either<Failure, List<Comic>>> getComics();
  Future<Either<Failure, Comic>> getComicById(int id);
  Future<Either<Failure, void>> addComic(Comic comic);
  Future<Either<Failure, void>> updateComic(Comic comic);
  Future<Either<Failure, void>> deleteComic(int id);
  Future<Either<Failure, List<ComicPage>>> getComicPages(String filePath);
  Stream<List<Comic>> watchComics();
}
```

#### 3.3.2 Settings Repository
```dart
abstract class SettingsRepository {
  Future<Either<Failure, ReaderSettings>> getReaderSettings();
  Future<Either<Failure, void>> updateReaderSettings(ReaderSettings settings);
  Future<Either<Failure, Map<String, dynamic>>> getAppSettings();
  Future<Either<Failure, void>> updateAppSetting(String key, dynamic value);
  Stream<ReaderSettings> watchReaderSettings();
}
```

#### 3.3.3 Bookmark Repository
```dart
abstract class BookmarkRepository {
  Future<Either<Failure, List<Bookmark>>> getBookmarksByComic(int comicId);
  Future<Either<Failure, Bookmark>> addBookmark(Bookmark bookmark);
  Future<Either<Failure, void>> deleteBookmark(int bookmarkId);
  Future<Either<Failure, void>> updateBookmark(Bookmark bookmark);
  Stream<List<Bookmark>> watchBookmarks(int comicId);
}
```

## 4. WebDAV Synchronization Engine

### 4.1 Sync Architecture
```dart
class SyncEngine {
  final WebDAVService webdavService;
  final DriftDb database;
  
  // Core sync methods
  Future<SyncResult> syncAll();
  Future<SyncResult> syncProgress();
  Future<SyncResult> syncSettings();
  Future<SyncResult> syncBookmarks();
  
  // Conflict resolution
  Future<T> resolveConflict<T>(ConflictData<T> conflict);
}
```

### 4.2 ETag-Based Conflict Detection
```dart
class ETageManager {
  Future<bool> hasRemoteChanges(String resource, String? localETag);
  Future<String?> getRemoteETag(String resource);
  Future<void> updateLocalETag(String resource, String etag);
}
```

### 4.3 Sync Data Models
```dart
@freezed
class SyncProgressData with _$SyncProgressData {
  const factory SyncProgressData({
    required String fileHash,
    required int currentPage,
    required int totalPages,
    required DateTime updatedAt,
    String? etag,
  }) = _SyncProgressData;
}

@freezed
class SyncSettingsData with _$SyncSettingsData {
  const factory SyncSettingsData({
    required String readingMode,
    required String navigationDirection,
    required String backgroundTheme,
    required double brightness,
    required DateTime updatedAt,
    String? etag,
  }) = _SyncSettingsData;
}
```

## 5. Image Caching and Loading Strategy

### 5.1 Multi-Level Cache Architecture
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Memory Cache  │───▶│    Disk Cache   │───▶│  Archive File   │
│   (LRU, 100MB)  │    │   (LRU, 1GB)    │    │   (Original)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### 5.2 Cache Service Interface
```dart
abstract class CacheService {
  Future<Uint8List?> getImage(String key);
  Future<void> putImage(String key, Uint8List data);
  Future<void> preloadPages(List<String> pageKeys);
  Future<void> clearCache();
  Stream<CacheStatistics> get cacheStats;
}
```

### 5.3 Preloading Strategy
- **Priority Levels**: Current (immediate), Next 3 (high), Next 5 (medium)
- **Background Loading**: Use isolates for image decompression
- **Memory Management**: LRU eviction with memory pressure monitoring

## 6. Comic Archive Processing

### 6.1 Archive Service Design
```dart
class ComicArchive {
  Future<List<String>> getPageList(String archivePath);
  Future<Uint8List> getPageImage(String archivePath, int pageIndex);
  Future<Uint8List?> getCoverImage(String archivePath);
  Future<ArchiveMetadata> getMetadata(String archivePath);
}

class ArchiveMetadata {
  final int pageCount;
  final List<String> pageNames;
  final int totalSize;
  final String format;
}
```

### 6.2 Supported Formats
- **CBZ/ZIP**: Primary support with full feature set
- **CBR/RAR**: Read-only support via native libraries
- **PDF**: Future enhancement for digital comics
- **Image Folders**: Direct folder reading capability

## 7. Reading Mode Renderers

### 7.1 Renderer Architecture
```dart
abstract class ReadingModeRenderer {
  Widget buildPage(BuildContext context, ComicPage page, ReaderState state);
  void handleGesture(GestureDetails gesture);
  bool canNavigate(NavigationDirection direction);
}

class HorizontalModeRenderer extends ReadingModeRenderer {
  // Horizontal page-turning implementation
}

class VerticalModeRenderer extends ReadingModeRenderer {
  // Vertical scrolling implementation
}

class WebtoonModeRenderer extends ReadingModeRenderer {
  // Continuous vertical scrolling for webtoons
}
```

### 7.2 Gesture Recognition System
```dart
class TapZoneHandler {
  final List<TapZone> zones;
  TapZoneAction? handleTap(Offset position, Size screenSize);
}

class PinchZoomController {
  final TransformationController controller;
  void handleScaleStart(ScaleStartDetails details);
  void handleScaleUpdate(ScaleUpdateDetails details);
  void handleScaleEnd(ScaleEndDetails details);
}
```

## 8. Background Processing

### 8.1 Background Task Architecture
```dart
class BackgroundTaskManager {
  static Future<void> initialize();
  static Future<void> registerTask(BackgroundTask task);
  static Future<void> executeTask(String taskName);
}

abstract class BackgroundTask {
  String get name;
  Duration get interval;
  Future<void> execute();
}

class SyncTask extends BackgroundTask {
  // Periodic sync execution
}

class CacheCleanupTask extends BackgroundTask {
  // Cache maintenance and cleanup
}
```

### 8.2 WorkManager Integration
```dart
class WorkManagerService {
  Future<void> schedulePeriodicSync();
  Future<void> scheduleCacheCleanup();
  Future<void> cancelAllTasks();
}
```

## 9. Error Handling Framework

### 9.1 Error Types
```dart
abstract class Failure extends Equatable {
  final String message;
  final String? code;
  final dynamic originalError;
}

class NetworkFailure extends Failure {}
class DatabaseFailure extends Failure {}
class FileSystemFailure extends Failure {}
class SyncFailure extends Failure {}
class ValidationFailure extends Failure {}
```

### 9.2 Error Handler
```dart
class GlobalErrorHandler {
  static void initialize();
  static void handleError(dynamic error, StackTrace stackTrace);
  static void handleBlocError(BlocBase bloc, Object error, StackTrace stackTrace);
  static void logError(String message, [dynamic error, StackTrace? stackTrace]);
}
```

### 9.3 Retry Mechanism
```dart
class RetryMechanism {
  static Future<T> retry<T>(
    Future<T> Function() operation, {
    int maxAttempts = 3,
    Duration delay = const Duration(seconds: 1),
    bool Function(dynamic error)? retryIf,
  });
}
```

## 10. Platform-Specific Implementations

### 10.1 Brightness Control
```dart
// Abstract interface
abstract class BrightnessService {
  Future<Either<BrightnessFailure, double>> getBrightness();
  Future<Either<BrightnessFailure, void>> setBrightness(double brightness);
  Stream<double> get brightnessStream;
}

// Platform implementations
class AndroidBrightnessService implements BrightnessService {
  // Android-specific implementation via MethodChannel
}

class IOSBrightnessService implements BrightnessService {
  // iOS-specific implementation via MethodChannel  
}
```

### 10.2 File System Access
```dart
class PlatformFileService {
  Future<String> getApplicationDocumentsDirectory();
  Future<String> getApplicationCacheDirectory();
  Future<bool> hasStoragePermission();
  Future<void> requestStoragePermission();
}
```

## 11. Testing Strategy

### 11.1 Unit Testing
- **Repository Tests**: Mock data sources, test business logic
- **BLoC Tests**: Event handling, state transitions
- **Service Tests**: Core functionality, error handling
- **Utility Tests**: Helper functions, extensions

### 11.2 Integration Testing
- **Database Tests**: Schema migrations, DAO operations
- **Sync Tests**: End-to-end WebDAV synchronization
- **Cache Tests**: Multi-level caching behavior
- **Archive Tests**: Comic file processing

### 11.3 Widget Testing
- **Reader UI Tests**: Reading interface, gesture handling
- **Bookshelf Tests**: Comic library management
- **Settings Tests**: Configuration interface

### 11.4 Performance Testing
- **Memory Tests**: Memory usage under load
- **Loading Tests**: Comic and page loading performance
- **Cache Tests**: Cache hit rates and performance
- **Sync Tests**: Synchronization performance

## 12. Security Considerations

### 12.1 Data Protection
- WebDAV credentials stored in platform keychain
- Database encryption for sensitive data
- Secure HTTP communications (HTTPS preferred)
- Input validation and sanitization

### 12.2 File System Security
- Sandbox compliance on all platforms
- Proper file permission handling
- Secure temporary file management
- Archive bomb protection

### 12.3 Network Security
- Certificate pinning for WebDAV connections
- Timeout and retry limits for network operations
- Secure credential storage and transmission
- Network request validation

## 13. Performance Optimization

### 13.1 Memory Management
- Image memory pooling and reuse
- Garbage collection optimization
- Memory pressure monitoring and response
- Efficient data structure usage

### 13.2 Rendering Optimization
- GPU-accelerated image rendering
- Efficient widget rebuilding
- Smooth animations and transitions
- Responsive UI during background operations

### 13.3 Storage Optimization
- Efficient database indexing
- Compressed cache storage
- Automatic cleanup of unused data
- Smart preloading based on reading patterns

## 14. Monitoring and Analytics

### 14.1 Firebase Integration
- Crash reporting with Crashlytics
- Performance monitoring
- User analytics and usage patterns
- Custom event tracking

### 14.2 Local Logging
- Structured logging with levels
- Performance metrics collection
- Error tracking and reporting
- Debug information for development

This design document provides the foundation for implementing a robust, scalable, and maintainable comic reader application that meets all specified requirements while following best practices for Flutter development and Clean Architecture principles.