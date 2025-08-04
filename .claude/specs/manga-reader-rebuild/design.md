# Manga Reader Rebuild - Design Specification

## Overview

This document presents the comprehensive design for rebuilding the Flutter manga reader application using Clean Architecture principles. The design emphasizes scalability, maintainability, and performance while supporting cross-platform deployment and handling 1000+ manga collections efficiently.

## Architecture Overview

### Clean Architecture Implementation

The application follows Clean Architecture with three distinct layers:

```
┌─────────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                           │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐   │
│  │     Pages       │ │     Widgets     │ │      BLoC       │   │
│  │   (Screens)     │ │  (Components)   │ │ (State Mgmt)    │   │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                               │
                        ┌─────────────┐
                        │   Events    │
                        │     │       │
                        │   States    │
                        └─────────────┘
                               │
┌─────────────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                               │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐   │
│  │    Entities     │ │   Use Cases     │ │  Repository     │   │
│  │  (Models)       │ │  (Business)     │ │  Interfaces     │   │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                               │
┌─────────────────────────────────────────────────────────────────┐
│                       DATA LAYER                                │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐   │
│  │  Repositories   │ │   Data Sources  │ │    Services     │   │
│  │(Implementation) │ │  (Local/Remote) │ │   (External)    │   │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

### Dependency Injection Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        GetIt Service Locator                    │
│                                                                 │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐         │
│  │  Singletons │    │  Factories  │    │   Lazy      │         │
│  │             │    │             │    │ Singletons  │         │
│  │ • Database  │    │ • BLoCs     │    │ • Services  │         │
│  │ • Cache     │    │ • Use Cases │    │ • Managers  │         │
│  │ • WebDAV    │    │             │    │             │         │
│  └─────────────┘    └─────────────┘    └─────────────┘         │
└─────────────────────────────────────────────────────────────────┘
```

## Database Design

### Drift SQLite Schema

The database uses Drift ORM with the following schema:

```sql
-- Core manga metadata table
CREATE TABLE manga (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    author TEXT,
    description TEXT,
    file_path TEXT NOT NULL UNIQUE,
    file_size INTEGER NOT NULL,
    page_count INTEGER NOT NULL,
    cover_image_path TEXT,
    is_favorite BOOLEAN NOT NULL DEFAULT FALSE,
    date_added DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_read_date DATETIME,
    tags TEXT, -- JSON array of tags
    metadata TEXT, -- JSON for extensible metadata
    sync_status TEXT NOT NULL DEFAULT 'local', -- local, synced, conflict
    etag TEXT, -- for WebDAV sync conflict detection
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Reading progress tracking
CREATE TABLE reading_progress (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    manga_id INTEGER NOT NULL,
    current_page INTEGER NOT NULL DEFAULT 0,
    total_pages INTEGER NOT NULL,
    progress_percentage REAL NOT NULL DEFAULT 0.0,
    reading_mode TEXT NOT NULL DEFAULT 'horizontal', -- horizontal, vertical, webtoon
    zoom_level REAL NOT NULL DEFAULT 1.0,
    scroll_position_x REAL NOT NULL DEFAULT 0.0,
    scroll_position_y REAL NOT NULL DEFAULT 0.0,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    completion_date DATETIME,
    last_read_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reading_time_seconds INTEGER NOT NULL DEFAULT 0,
    sync_status TEXT NOT NULL DEFAULT 'local',
    etag TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (manga_id) REFERENCES manga (id) ON DELETE CASCADE
);

-- Bookmarks for specific pages
CREATE TABLE bookmarks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    manga_id INTEGER NOT NULL,
    page_number INTEGER NOT NULL,
    title TEXT,
    note TEXT,
    thumbnail_path TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (manga_id) REFERENCES manga (id) ON DELETE CASCADE,
    UNIQUE(manga_id, page_number)
);

-- Reading sessions for statistics
CREATE TABLE reading_sessions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    manga_id INTEGER NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME,
    duration_seconds INTEGER,
    pages_read INTEGER NOT NULL DEFAULT 0,
    session_type TEXT NOT NULL DEFAULT 'normal', -- normal, binge, quick
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (manga_id) REFERENCES manga (id) ON DELETE CASCADE
);

-- User settings with sync support
CREATE TABLE user_settings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    category TEXT NOT NULL, -- reading, display, library, sync, etc.
    key TEXT NOT NULL,
    value TEXT NOT NULL, -- JSON-serialized value
    data_type TEXT NOT NULL, -- string, number, boolean, object
    sync_status TEXT NOT NULL DEFAULT 'local',
    etag TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(category, key)
);

-- Custom page ordering for drag-and-drop
CREATE TABLE page_custom_order (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    manga_id INTEGER NOT NULL,
    original_page_number INTEGER NOT NULL,
    custom_page_number INTEGER NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (manga_id) REFERENCES manga (id) ON DELETE CASCADE,
    UNIQUE(manga_id, original_page_number),
    UNIQUE(manga_id, custom_page_number)
);

-- User profile and statistics
CREATE TABLE user_profile (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    display_name TEXT NOT NULL,
    avatar_path TEXT,
    total_reading_time INTEGER NOT NULL DEFAULT 0,
    total_manga_read INTEGER NOT NULL DEFAULT 0,
    current_streak_days INTEGER NOT NULL DEFAULT 0,
    longest_streak_days INTEGER NOT NULL DEFAULT 0,
    preferred_reading_mode TEXT NOT NULL DEFAULT 'horizontal',
    join_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_active_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sync_status TEXT NOT NULL DEFAULT 'local',
    etag TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Cache management for images and metadata
CREATE TABLE cache_entries (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    cache_key TEXT NOT NULL UNIQUE,
    file_path TEXT NOT NULL,
    file_size INTEGER NOT NULL,
    cache_type TEXT NOT NULL, -- thumbnail, page, cover, metadata
    access_count INTEGER NOT NULL DEFAULT 1,
    last_accessed DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance optimization
CREATE INDEX idx_manga_title ON manga(title);
CREATE INDEX idx_manga_author ON manga(author);
CREATE INDEX idx_manga_last_read ON manga(last_read_date);
CREATE INDEX idx_manga_favorite ON manga(is_favorite);
CREATE INDEX idx_reading_progress_manga ON reading_progress(manga_id);
CREATE INDEX idx_reading_progress_last_read ON reading_progress(last_read_date);
CREATE INDEX idx_bookmarks_manga ON bookmarks(manga_id);
CREATE INDEX idx_reading_sessions_manga ON reading_sessions(manga_id);
CREATE INDEX idx_reading_sessions_start_time ON reading_sessions(start_time);
CREATE INDEX idx_settings_category ON user_settings(category);
CREATE INDEX idx_cache_type ON cache_entries(cache_type);
CREATE INDEX idx_cache_last_accessed ON cache_entries(last_accessed);
```

### Database Migration Strategy

```dart
// Migration versioning strategy
class DatabaseMigrations {
  static const int currentVersion = 1;
  
  static final List<Migration> migrations = [
    // Version 1: Initial schema
    Migration(1, 2, (Migrator m) async {
      // Add new columns or tables
      await m.addColumn(manga, manga.etag);
    }),
    // Future migrations...
  ];
}
```

## Component Architecture

### Core Components

#### 1. File Management System

```
┌─────────────────────────────────────────────────────────────────┐
│                    File Management Architecture                  │
│                                                                 │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────┐ │
│  │   File Import   │    │  Archive        │    │   Cache     │ │
│  │   Service       │────│  Processor      │────│  Manager    │ │
│  │                 │    │                 │    │             │ │
│  │ • ZIP/CBZ/CBR   │    │ • Image Extract │    │ • LRU Cache │ │
│  │ • Validation    │    │ • Metadata      │    │ • Size Mgmt │ │
│  │ • Progress      │    │ • Error Handle  │    │ • Cleanup   │ │
│  └─────────────────┘    └─────────────────┘    └─────────────┘ │
│           │                       │                       │    │
│           └───────────────────────┼───────────────────────┘    │
│                                   │                            │
│  ┌─────────────────────────────────┼─────────────────────────┐  │
│  │            Storage Layer         │                        │  │
│  │                                  │                        │  │
│  │  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐   │  │
│  │  │   Memory    │    │    Disk     │    │   Database  │   │  │
│  │  │   Cache     │    │   Cache     │    │  Metadata   │   │  │
│  │  └─────────────┘    └─────────────┘    └─────────────┘   │  │
│  └─────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

#### 2. Reading Engine

```
┌─────────────────────────────────────────────────────────────────┐
│                      Reading Engine Architecture                │
│                                                                 │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────┐ │
│  │   Page View     │    │   Gesture       │    │  Preloader  │ │
│  │   Controller    │────│   Handler       │────│   Engine    │ │
│  │                 │    │                 │    │             │ │
│  │ • View Modes    │    │ • Zoom/Pan     │    │ • Priority  │ │
│  │ • Transitions   │    │ • Navigation   │    │ • Buffering │ │
│  │ • State Mgmt    │    │ • Shortcuts    │    │ • Memory    │ │
│  └─────────────────┘    └─────────────────┘    └─────────────┘ │
│           │                       │                       │    │
│           └───────────────────────┼───────────────────────┘    │
│                                   │                            │
│  ┌─────────────────────────────────┼─────────────────────────┐  │
│  │           Image Rendering        │                        │  │
│  │                                  │                        │  │
│  │  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐   │  │
│  │  │   Photo     │    │   Custom    │    │   Memory    │   │  │
│  │  │    View     │    │  Renderer   │    │  Manager    │   │  │
│  │  └─────────────┘    └─────────────┘    └─────────────┘   │  │
│  └─────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

#### 3. Synchronization System

```
┌─────────────────────────────────────────────────────────────────┐
│                   WebDAV Synchronization Architecture          │
│                                                                 │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────┐ │
│  │   Sync Engine   │    │   Conflict      │    │  Background │ │
│  │                 │────│   Resolver      │────│   Scheduler │ │
│  │ • Change Detect │    │                 │    │             │ │
│  │ • ETag Compare  │    │ • Strategies    │    │ • WorkMgr   │ │
│  │ • Queue Mgmt    │    │ • User Choice   │    │ • Retry     │ │
│  │ • Error Handle  │    │ • Auto-merge    │    │ • Intervals │ │
│  └─────────────────┘    └─────────────────┘    └─────────────┘ │
│           │                       │                       │    │
│           └───────────────────────┼───────────────────────┘    │
│                                   │                            │
│  ┌─────────────────────────────────┼─────────────────────────┐  │
│  │              Network Layer       │                        │  │
│  │                                  │                        │  │
│  │  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐   │  │
│  │  │   WebDAV    │    │   HTTP      │    │   Retry     │   │  │
│  │  │   Client    │    │   Service   │    │   Policy    │   │  │
│  │  └─────────────┘    └─────────────┘    └─────────────┘   │  │
│  └─────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

## Data Models and Entities

### Core Domain Entities

```dart
// Domain entity for Manga
@freezed
class Manga with _$Manga {
  const factory Manga({
    required String id,
    required String title,
    String? author,
    String? description,
    required String filePath,
    required int fileSize,
    required int pageCount,
    String? coverImagePath,
    required bool isFavorite,
    required DateTime dateAdded,
    required DateTime dateModified,
    DateTime? lastReadDate,
    @Default([]) List<String> tags,
    @Default({}) Map<String, dynamic> metadata,
    @Default(SyncStatus.local) SyncStatus syncStatus,
    String? etag,
  }) = _Manga;
  
  factory Manga.fromJson(Map<String, dynamic> json) => _$MangaFromJson(json);
}

// Reading progress entity
@freezed
class ReadingProgress with _$ReadingProgress {
  const factory ReadingProgress({
    required String id,
    required String mangaId,
    required int currentPage,
    required int totalPages,
    required double progressPercentage,
    @Default(ReadingMode.horizontal) ReadingMode readingMode,
    @Default(1.0) double zoomLevel,
    @Default(0.0) double scrollPositionX,
    @Default(0.0) double scrollPositionY,
    required bool isCompleted,
    DateTime? completionDate,
    required DateTime lastReadDate,
    @Default(0) int readingTimeSeconds,
    @Default(SyncStatus.local) SyncStatus syncStatus,
    String? etag,
  }) = _ReadingProgress;
  
  factory ReadingProgress.fromJson(Map<String, dynamic> json) => 
    _$ReadingProgressFromJson(json);
}

// User settings entity
@freezed
class UserSetting with _$UserSetting {
  const factory UserSetting({
    required String category,
    required String key,
    required dynamic value,
    required SettingDataType dataType,
    @Default(SyncStatus.local) SyncStatus syncStatus,
    String? etag,
  }) = _UserSetting;
  
  factory UserSetting.fromJson(Map<String, dynamic> json) => 
    _$UserSettingFromJson(json);
}

// Enums for type safety
enum SyncStatus { local, synced, conflict, pending }
enum ReadingMode { horizontal, vertical, webtoon }
enum SettingDataType { string, number, boolean, object, array }
```

## State Management Design

### BLoC Architecture Pattern

```dart
// Manga Library BLoC
class MangaLibraryBloc extends Bloc<MangaLibraryEvent, MangaLibraryState> {
  final GetMangaLibraryUseCase _getMangaLibrary;
  final SearchMangaUseCase _searchManga;
  final UpdateMangaUseCase _updateManga;
  
  MangaLibraryBloc({
    required GetMangaLibraryUseCase getMangaLibrary,
    required SearchMangaUseCase searchManga,
    required UpdateMangaUseCase updateManga,
  }) : _getMangaLibrary = getMangaLibrary,
       _searchManga = searchManga,
       _updateManga = updateManga,
       super(const MangaLibraryState.initial()) {
    
    on<MangaLibraryEvent.loadLibrary>(_onLoadLibrary);
    on<MangaLibraryEvent.searchManga>(_onSearchManga);
    on<MangaLibraryEvent.filterManga>(_onFilterManga);
    on<MangaLibraryEvent.sortManga>(_onSortManga);
    on<MangaLibraryEvent.toggleFavorite>(_onToggleFavorite);
  }
  
  Future<void> _onLoadLibrary(
    MangaLibraryLoadLibrary event,
    Emitter<MangaLibraryState> emit,
  ) async {
    emit(const MangaLibraryState.loading());
    
    final result = await _getMangaLibrary(GetMangaLibraryParams(
      page: event.page,
      limit: event.limit,
      sortBy: event.sortBy,
      sortOrder: event.sortOrder,
    ));
    
    result.fold(
      (failure) => emit(MangaLibraryState.error(failure.message)),
      (mangaList) => emit(MangaLibraryState.loaded(
        manga: mangaList,
        hasReachedMax: mangaList.length < event.limit,
      )),
    );
  }
}

// Reading BLoC for manga reading experience
class ReadingBloc extends Bloc<ReadingEvent, ReadingState> {
  final GetMangaPagesUseCase _getMangaPages;
  final UpdateReadingProgressUseCase _updateProgress;
  final PreloadPagesUseCase _preloadPages;
  
  // Implementation with page management, progress tracking, etc.
}

// Sync BLoC for WebDAV synchronization
class SyncBloc extends Bloc<SyncEvent, SyncState> {
  final SyncDataUseCase _syncData;
  final ResolveSyncConflictUseCase _resolveConflict;
  
  // Implementation with sync operations, conflict resolution, etc.
}
```

### State Definitions

```dart
// Manga Library States
@freezed
class MangaLibraryState with _$MangaLibraryState {
  const factory MangaLibraryState.initial() = _Initial;
  const factory MangaLibraryState.loading() = _Loading;
  const factory MangaLibraryState.loaded({
    required List<Manga> manga,
    required bool hasReachedMax,
    @Default('') String searchQuery,
    @Default(MangaFilter.all) MangaFilter filter,
    @Default(MangaSortBy.title) MangaSortBy sortBy,
    @Default(SortOrder.ascending) SortOrder sortOrder,
  }) = _Loaded;
  const factory MangaLibraryState.error(String message) = _Error;
}

// Reading States
@freezed
class ReadingState with _$ReadingState {
  const factory ReadingState.initial() = _Initial;
  const factory ReadingState.loading() = _Loading;
  const factory ReadingState.reading({
    required Manga manga,
    required List<String> pages,
    required ReadingProgress progress,
    required ReadingMode mode,
    @Default(1.0) double zoomLevel,
    @Default([]) List<String> preloadedPages,
    @Default(false) bool isFullscreen,
    @Default(false) bool showControls,
  }) = _Reading;
  const factory ReadingState.error(String message) = _Error;
}
```

## User Interface Design

### Navigation Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        App Navigation                           │
│                                                                 │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────┐ │
│  │     Bottom      │    │      App        │    │   Modal     │ │
│  │   Navigation    │────│      Bar        │────│   Routes    │ │
│  │                 │    │                 │    │             │ │
│  │ • Library       │    │ • Back/Forward  │    │ • Settings  │ │
│  │ • Reading       │    │ • Search        │    │ • Profile   │ │
│  │ • Bookmarks     │    │ • Actions       │    │ • Import    │ │
│  │ • Profile       │    │ • Title         │    │ • Details   │ │
│  └─────────────────┘    └─────────────────┘    └─────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### Key UI Components

#### 1. Library Grid/List View

```dart
class MangaLibraryView extends StatelessWidget {
  const MangaLibraryView({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<MangaLibraryBloc, MangaLibraryState>(
      builder: (context, state) {
        return state.when(
          initial: () => const Center(child: Text('Welcome to your library')),
          loading: () => const Center(child: CircularProgressIndicator()),
          loaded: (manga, hasReachedMax, query, filter, sortBy, order) => 
            CustomScrollView(
              slivers: [
                SliverAppBar(
                  title: Text('Library (${manga.length})'),
                  actions: [
                    IconButton(
                      icon: const Icon(Icons.search),
                      onPressed: () => _showSearch(context),
                    ),
                    IconButton(
                      icon: const Icon(Icons.filter_list),
                      onPressed: () => _showFilter(context),
                    ),
                  ],
                ),
                SliverGrid(
                  gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                    crossAxisCount: 2,
                    childAspectRatio: 0.7,
                    crossAxisSpacing: 8,
                    mainAxisSpacing: 8,
                  ),
                  delegate: SliverChildBuilderDelegate(
                    (context, index) => MangaCard(manga: manga[index]),
                    childCount: manga.length,
                  ),
                ),
              ],
            ),
          error: (message) => Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(Icons.error, size: 64, color: Colors.red),
                SizedBox(height: 16),
                Text(message),
                ElevatedButton(
                  onPressed: () => context.read<MangaLibraryBloc>()
                    .add(const MangaLibraryEvent.loadLibrary()),
                  child: const Text('Retry'),
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}
```

#### 2. Reading Interface

```dart
class ReadingView extends StatelessWidget {
  const ReadingView({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<ReadingBloc, ReadingState>(
      builder: (context, state) {
        return state.when(
          initial: () => const SizedBox.shrink(),
          loading: () => const Center(child: CircularProgressIndicator()),
          reading: (manga, pages, progress, mode, zoom, preloaded, fullscreen, showControls) =>
            Scaffold(
              body: Stack(
                children: [
                  // Main reading area
                  _buildReadingArea(context, pages, progress, mode, zoom),
                  
                  // Controls overlay
                  if (showControls) ...[
                    _buildTopControls(context, manga, progress),
                    _buildBottomControls(context, progress, mode),
                  ],
                  
                  // Gesture detector for tap zones
                  _buildGestureOverlay(context),
                ],
              ),
            ),
          error: (message) => Center(child: Text('Error: $message')),
        );
      },
    );
  }
  
  Widget _buildReadingArea(BuildContext context, List<String> pages, 
      ReadingProgress progress, ReadingMode mode, double zoom) {
    switch (mode) {
      case ReadingMode.horizontal:
        return PageView.builder(
          controller: PageController(initialPage: progress.currentPage),
          itemCount: pages.length,
          onPageChanged: (page) => _updateProgress(context, page),
          itemBuilder: (context, index) => PhotoView(
            imageProvider: FileImage(File(pages[index])),
            minScale: PhotoViewComputedScale.contained,
            maxScale: PhotoViewComputedScale.covered * 3.0,
            initialScale: PhotoViewComputedScale.contained * zoom,
          ),
        );
      case ReadingMode.vertical:
        return ListView.builder(
          controller: ScrollController(
            initialScrollOffset: progress.scrollPositionY,
          ),
          itemCount: pages.length,
          itemBuilder: (context, index) => Image.file(
            File(pages[index]),
            fit: BoxFit.fitWidth,
          ),
        );
      case ReadingMode.webtoon:
        return SingleChildScrollView(
          controller: ScrollController(
            initialScrollOffset: progress.scrollPositionY,
          ),
          child: Column(
            children: pages.map((page) => Image.file(
              File(page),
              fit: BoxFit.fitWidth,
            )).toList(),
          ),
        );
    }
  }
}
```

## Performance Optimization Strategy

### Memory Management

```dart
class ImageCacheManager {
  static const int maxMemoryCacheSize = 100 * 1024 * 1024; // 100MB
  static const int maxDiskCacheSize = 500 * 1024 * 1024; // 500MB
  static const int preloadDistance = 3; // pages to preload
  
  final LRUMap<String, Uint8List> _memoryCache = LRUMap(maxMemoryCacheSize);
  final DiskCache _diskCache = DiskCache(maxDiskCacheSize);
  
  Future<Uint8List?> getImage(String path) async {
    // Check memory cache first
    final cached = _memoryCache[path];
    if (cached != null) return cached;
    
    // Check disk cache
    final diskCached = await _diskCache.get(path);
    if (diskCached != null) {
      _memoryCache[path] = diskCached;
      return diskCached;
    }
    
    // Load from file and cache
    final file = File(path);
    if (await file.exists()) {
      final bytes = await file.readAsBytes();
      _memoryCache[path] = bytes;
      await _diskCache.put(path, bytes);
      return bytes;
    }
    
    return null;
  }
  
  void preloadPages(List<String> pages, int currentIndex) {
    final startIndex = math.max(0, currentIndex - 1);
    final endIndex = math.min(pages.length, currentIndex + preloadDistance + 1);
    
    for (int i = startIndex; i < endIndex; i++) {
      if (i != currentIndex && !_memoryCache.containsKey(pages[i])) {
        getImage(pages[i]); // Preload in background
      }
    }
  }
}
```

### Database Query Optimization

```dart
class MangaRepository {
  Future<List<Manga>> getMangaWithPagination({
    required int page,
    required int limit,
    String? searchQuery,
    MangaFilter? filter,
    MangaSortBy? sortBy,
    SortOrder? sortOrder,
  }) async {
    final query = select(manga).limit(limit, offset: page * limit);
    
    // Add search filter
    if (searchQuery != null && searchQuery.isNotEmpty) {
      query.where((m) => m.title.contains(searchQuery) |
                          m.author.contains(searchQuery));
    }
    
    // Add status filter
    switch (filter) {
      case MangaFilter.favorites:
        query.where((m) => m.isFavorite.equals(true));
        break;
      case MangaFilter.unread:
        query.where((m) => m.lastReadDate.isNull());
        break;
      case MangaFilter.reading:
        query.where((m) => m.lastReadDate.isNotNull());
        break;
      default:
        break;
    }
    
    // Add sorting
    switch (sortBy) {
      case MangaSortBy.title:
        if (sortOrder == SortOrder.ascending) {
          query.orderBy([(m) => OrderingTerm.asc(m.title)]);
        } else {
          query.orderBy([(m) => OrderingTerm.desc(m.title)]);
        }
        break;
      case MangaSortBy.dateAdded:
        if (sortOrder == SortOrder.ascending) {
          query.orderBy([(m) => OrderingTerm.asc(m.dateAdded)]);
        } else {
          query.orderBy([(m) => OrderingTerm.desc(m.dateAdded)]);
        }
        break;
      case MangaSortBy.lastRead:
        query.orderBy([(m) => OrderingTerm.desc(m.lastReadDate)]);
        break;
    }
    
    return await query.get();
  }
}
```

## Testing Strategy

### Unit Testing Architecture

```dart
// Repository tests
class MangaRepositoryTest {
  late MangaRepository repository;
  late Database database;
  
  setUp() async {
    database = Database.memory();
    repository = MangaRepository(database);
  }
  
  group('MangaRepository Tests', () {
    test('should insert manga successfully', () async {
      final manga = TestData.sampleManga();
      await repository.insertManga(manga);
      
      final retrieved = await repository.getMangaById(manga.id);
      expect(retrieved, equals(manga));
    });
    
    test('should handle pagination correctly', () async {
      // Insert test data
      final mangaList = List.generate(25, (i) => TestData.sampleManga());
      for (final manga in mangaList) {
        await repository.insertManga(manga);
      }
      
      // Test pagination
      final page1 = await repository.getMangaWithPagination(page: 0, limit: 10);
      final page2 = await repository.getMangaWithPagination(page: 1, limit: 10);
      final page3 = await repository.getMangaWithPagination(page: 2, limit: 10);
      
      expect(page1.length, equals(10));
      expect(page2.length, equals(10));
      expect(page3.length, equals(5));
    });
  });
}

// BLoC tests
class MangaLibraryBlocTest {
  late MangaLibraryBloc bloc;
  late MockMangaRepository mockRepository;
  
  setUp() {
    mockRepository = MockMangaRepository();
    bloc = MangaLibraryBloc(
      getMangaLibrary: GetMangaLibraryUseCase(mockRepository),
      searchManga: SearchMangaUseCase(mockRepository),
      updateManga: UpdateMangaUseCase(mockRepository),
    );
  }
  
  blocTest<MangaLibraryBloc, MangaLibraryState>(
    'emits loaded state when library loads successfully',
    build: () {
      when(() => mockRepository.getMangaWithPagination(any()))
        .thenAnswer((_) async => TestData.sampleMangaList());
      return bloc;
    },
    act: (bloc) => bloc.add(const MangaLibraryEvent.loadLibrary()),
    expect: () => [
      const MangaLibraryState.loading(),
      MangaLibraryState.loaded(
        manga: TestData.sampleMangaList(),
        hasReachedMax: false,
      ),
    ],
  );
}
```

### Integration Testing

```dart
class AppIntegrationTest {
  testWidgets('complete manga reading flow', (WidgetTester tester) async {
    // Launch app
    await tester.pumpWidget(MyApp());
    await tester.pumpAndSettle();
    
    // Navigate to library
    expect(find.text('Library'), findsOneWidget);
    
    // Import a test manga
    await tester.tap(find.byIcon(Icons.add));
    await tester.pumpAndSettle();
    
    // Select manga to read
    await tester.tap(find.byType(MangaCard).first);
    await tester.pumpAndSettle();
    
    // Verify reading interface
    expect(find.byType(PhotoView), findsOneWidget);
    
    // Test page navigation
    await tester.drag(find.byType(PhotoView), const Offset(-300, 0));
    await tester.pumpAndSettle();
    
    // Verify progress update
    expect(find.text('Page 2'), findsOneWidget);
  });
}
```

## Error Handling Strategy

### Centralized Error Management

```dart
abstract class Failure {
  const Failure(this.message);
  final String message;
}

class DatabaseFailure extends Failure {
  const DatabaseFailure(String message) : super(message);
}

class NetworkFailure extends Failure {
  const NetworkFailure(String message) : super(message);
}

class FileSystemFailure extends Failure {
  const FileSystemFailure(String message) : super(message);
}

class ValidationFailure extends Failure {
  const ValidationFailure(String message) : super(message);
}

// Global error handler
class ErrorHandler {
  static void handleError(Object error, StackTrace stackTrace) {
    // Log to Firebase Crashlytics
    FirebaseCrashlytics.instance.recordError(error, stackTrace);
    
    // Log locally for debugging
    logger.error('Unhandled error: $error', error, stackTrace);
    
    // Show user-friendly message
    if (error is Failure) {
      ShowSnackBar.error(error.message);
    } else {
      ShowSnackBar.error('An unexpected error occurred');
    }
  }
}
```

## Security Considerations

### Data Protection

```dart
class SecurityManager {
  // Encrypt sensitive data
  static Future<String> encryptData(String data) async {
    final key = await _getEncryptionKey();
    final encrypted = await AESCrypt.encrypt(data, key);
    return encrypted;
  }
  
  // Secure WebDAV credentials
  static Future<void> storeWebDAVCredentials(
    String username, 
    String password
  ) async {
    final storage = FlutterSecureStorage();
    await storage.write(key: 'webdav_username', value: username);
    await storage.write(key: 'webdav_password', value: password);
  }
  
  // Validate file integrity
  static Future<bool> validateArchiveFile(String filePath) async {
    try {
      final file = File(filePath);
      final bytes = await file.readAsBytes();
      
      // Check file signature for ZIP/CBZ/CBR
      if (bytes.length < 4) return false;
      
      final signature = bytes.sublist(0, 4);
      return _isValidArchiveSignature(signature);
    } catch (e) {
      return false;
    }
  }
}
```

This comprehensive design document provides the architectural foundation for implementing a scalable, maintainable Flutter manga reader application with clean architecture principles, efficient performance, and robust error handling.