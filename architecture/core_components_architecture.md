# Flutter漫画阅读器核心组件架构图

**版本**: 1.0  
**作者**: 架构师模式  
**日期**: 2025-07-31  
**项目**: Easy-Comic Flutter漫画阅读器核心组件详细设计  

## 1. ReaderBloc状态管理核心架构

### 1.1 ReaderBloc组件结构图

```mermaid
classDiagram
    class ReaderBloc {
        -comicRepository: IComicRepository
        -settingsRepository: ISettingsRepository
        -bookmarkRepository: IBookmarkRepository
        -navigationService: INavigationService
        -zoomService: IZoomService
        -cacheManager: ICacheManager
        -autoPageTimer: Timer?
        -progressSaveTimer: Timer?
        -currentComicId: String?
        
        +ReaderBloc(dependencies...)
        +add(event: ReaderEvent): void
        +close(): Future~void~
        -_onLoadComic(event: LoadComicEvent, emit: Emitter): Future~void~
        -_onPageChanged(event: PageChangedEvent, emit: Emitter): Future~void~
        -_onZoomChanged(event: ZoomChangedEvent, emit: Emitter): Future~void~
        -_onToggleUiVisibility(event: ToggleUiVisibilityEvent, emit: Emitter): void
        -_onUpdateSetting(event: UpdateSettingEvent, emit: Emitter): Future~void~
        -_onBookmarkEvent(event: BookmarkEvent, emit: Emitter): Future~void~
        -_onAutoPageEvent(event: AutoPageEvent, emit: Emitter): void
        -_startImagePreloading(pages: List, currentPage: int): void
        -_scheduleProgressSave(): void
        -_startAutoPage(interval: Duration): void
        -_stopAutoPage(): void
    }
    
    class ReaderState {
        +comic: Comic?
        +pages: List~ComicPage~
        +currentPage: int
        +totalPages: int
        +isLoading: bool
        +error: String?
        +isUiVisible: bool
        +zoomScale: double
        +settings: ReaderSettings
        +bookmarks: List~Bookmark~
        +thumbnailCache: Map~int, String~
        +customPageOrder: List~int~
        
        +copyWith(Map~String, Any~ updates): ReaderState
        +get isValid(): bool
        +get progress(): double
    }
    
    class ReaderEvent {
        <<abstract>>
    }
    
    class LoadComicEvent {
        +comicId: String
        +initialPage: int
        +restoreState: bool
    }
    
    class PageChangedEvent {
        +newPage: int
        +source: PageChangeSource
        +saveProgress: bool
    }
    
    class ZoomChangedEvent {
        +newScale: double
        +focalPoint: Offset?
        +animated: bool
    }
    
    class ToggleUiVisibilityEvent {
        +forceVisible: bool?
    }
    
    class UpdateSettingEvent {
        +settingType: SettingType
        +value: Any
        +persist: bool
    }
    
    class BookmarkEvent {
        +action: BookmarkAction
        +bookmarkId: String?
        +pageIndex: int?
        +label: String?
    }
    
    class AutoPageEvent {
        +action: AutoPageAction
        +interval: Duration?
    }
    
    ReaderBloc --> ReaderState : emits
    ReaderBloc --> ReaderEvent : handles
    ReaderEvent <|-- LoadComicEvent
    ReaderEvent <|-- PageChangedEvent
    ReaderEvent <|-- ZoomChangedEvent
    ReaderEvent <|-- ToggleUiVisibilityEvent
    ReaderEvent <|-- UpdateSettingEvent
    ReaderEvent <|-- BookmarkEvent
    ReaderEvent <|-- AutoPageEvent
```

### 1.2 状态转换流程图

```mermaid
stateDiagram-v2
    [*] --> Initial : 初始化ReaderBloc
    
    Initial --> Loading : LoadComicEvent
    
    Loading --> Processing : 开始加载漫画数据
    Processing --> LoadingPages : 漫画基本信息加载完成
    LoadingPages --> CacheCheck : 检查页面缓存
    
    CacheCheck --> CacheHit : 缓存命中
    CacheCheck --> CacheMiss : 缓存未命中
    
    CacheHit --> Loaded : 从缓存加载页面
    CacheMiss --> Extracting : 解压原始文档
    Extracting --> Processing_Images : 处理图像数据
    Processing_Images --> Caching : 存储到缓存
    Caching --> Loaded : 页面加载完成
    
    Loading --> Error : 加载失败
    Processing --> Error : 处理失败
    Extracting --> Error : 解压失败
    
    Loaded --> Navigating : PageChangedEvent
    Navigating --> PreloadNext : 预加载相邻页面
    PreloadNext --> Loaded : 导航完成
    
    Loaded --> Zooming : ZoomChangedEvent
    Zooming --> Loaded : 缩放完成
    
    Loaded --> SettingsUpdating : UpdateSettingEvent
    SettingsUpdating --> Loaded : 设置更新完成
    
    Loaded --> BookmarkManaging : BookmarkEvent(ADD/DELETE)
    BookmarkManaging --> Loaded : 书签操作完成
    
    Loaded --> BookmarkJumping : BookmarkEvent(JUMP_TO)
    BookmarkJumping --> Navigating : 跳转到书签页面
    
    Loaded --> AutoPaging : AutoPageEvent(START)
    AutoPaging --> AutoPageTimer : 启动定时器
    AutoPageTimer --> Navigating : 定时翻页
    AutoPaging --> Loaded : AutoPageEvent(STOP)
    
    Error --> Loading : 重试加载
    
    note right of Loaded
        核心状态包含:
        - Comic基本信息
        - Pages完整列表
        - 当前页码索引
        - UI显示状态
        - 缩放级别
        - 书签列表
        - 用户设置
        - 缓存状态
    end note
```

### 1.3 事件处理数据流

```mermaid
sequenceDiagram
    participant UI as UI Widget
    participant Bloc as ReaderBloc
    participant UseCase as Use Cases
    participant Repo as Repository
    participant Cache as Cache Manager
    participant DB as Database
    
    UI->>Bloc: LoadComicEvent(comicId)
    activate Bloc
    
    Bloc->>Bloc: emit(Loading State)
    
    par 并行加载数据
        Bloc->>Repo: getComic(comicId)
        activate Repo
        Repo->>DB: 查询漫画信息
        DB-->>Repo: Comic信息
        deactivate Repo
    and
        Bloc->>Repo: getPages(comicId)
        activate Repo
        
        Repo->>Cache: 检查页面缓存
        Cache-->>Repo: 缓存状态
        
        alt 缓存命中
            Cache-->>Repo: 返回缓存页面
        else 缓存未命中
            Repo->>Repo: 解压和处理图像
            Repo->>Cache: 存储处理后的页面
        end
        
        Repo-->>Bloc: Pages列表
        deactivate Repo
    and
        Bloc->>Repo: getBookmarks(comicId)
        Repo-->>Bloc: 书签列表
    and
        Bloc->>Repo: getSettings()
        Repo-->>Bloc: 用户设置
    end
    
    Bloc->>Bloc: emit(Loaded State)
    Bloc-->>UI: 状态更新通知
    deactivate Bloc
    
    Note over Cache: 后台预加载相邻页面
    Cache->>Cache: 异步预加载(priority queue)
```

## 2. Repository模式数据访问层架构

### 2.1 Repository层组件关系图

```mermaid
classDiagram
    class IComicRepository {
        <<interface>>
        +getComic(comicId: String): Future~Comic~
        +getPages(comicId: String): Future~List~ComicPage~~
        +saveProgress(comicId: String, page: int): Future~void~
        +getProgress(comicId: String): Future~ReadingProgress?~
        +getCustomPageOrder(comicId: String): Future~List~int~~
        +setCustomPageOrder(comicId: String, order: List~int~): Future~void~
        +clearCustomPageOrder(comicId: String): Future~void~
    }
    
    class ComicRepository {
        -database: DriftDatabase
        -archiveService: IArchiveService
        -cacheManager: ICacheManager
        -fileService: IFileService
        -errorHandler: ErrorHandler
        
        +getComic(comicId: String): Future~Comic~
        +getPages(comicId: String): Future~List~ComicPage~~
        +saveProgress(comicId: String, page: int): Future~void~
        +getProgress(comicId: String): Future~ReadingProgress?~
        +getCustomPageOrder(comicId: String): Future~List~int~~
        +setCustomPageOrder(comicId: String, order: List~int~): Future~void~
        -_processPageImage(imageData: Uint8List, index: int): Future~ComicPage~
        -_optimizeImage(data: Uint8List, info: ImageInfo): Future~Uint8List~
        -_shouldOptimize(info: ImageInfo): bool
        -_getTotalPages(comicId: String): Future~int~
        -_updateReadingSession(comicId: String, page: int): Future~void~
    }
    
    class ISettingsRepository {
        <<interface>>
        +getSettings(): Future~ReaderSettings~
        +saveSettings(settings: ReaderSettings): Future~void~
        +saveSetting(type: SettingType, value: Any): Future~void~
        +watchSettings(): Stream~ReaderSettings~
        +resetToDefaults(): Future~void~
    }
    
    class SettingsRepository {
        -sharedPrefs: SharedPreferences
        -database: DriftDatabase
        -settingsCache: Map~String, Any~
        -cacheExpiry: DateTime?
        -_settingsStreamController: StreamController
        
        +getSettings(): Future~ReaderSettings~
        +saveSettings(settings: ReaderSettings): Future~void~
        +saveSetting(type: SettingType, value: Any): Future~void~
        +watchSettings(): Stream~ReaderSettings~
        +resetToDefaults(): Future~void~
        -_loadAllSettings(): Future~Map~String, Any~~
        -_validateSettingValue(type: SettingType, value: Any): Any
        -_isCacheValid(): bool
        -_updateCache(data: Map~String, Any~): void
        -_notifySettingChanged(type: SettingType, value: Any): void
    }
    
    class IBookmarkRepository {
        <<interface>>
        +getBookmarks(comicId: String): Future~List~Bookmark~~
        +addBookmark(comicId: String, page: int, label: String?): Future~Bookmark~
        +deleteBookmark(bookmarkId: String): Future~void~
        +updateBookmark(bookmark: Bookmark): Future~void~
        +getBookmarkThumbnail(bookmarkId: String): Future~String?~
    }
    
    class BookmarkRepository {
        -database: DriftDatabase
        -fileService: IFileService
        -thumbnailService: IThumbnailService
        
        +getBookmarks(comicId: String): Future~List~Bookmark~~
        +addBookmark(comicId: String, page: int, label: String?): Future~Bookmark~
        +deleteBookmark(bookmarkId: String): Future~void~
        +updateBookmark(bookmark: Bookmark): Future~void~
        +getBookmarkThumbnail(bookmarkId: String): Future~String?~
        -_generateBookmarkThumbnail(comicId: String, page: int): Future~String~
        -_notifyBookmarkAdded(bookmark: Bookmark): void
        -_notifyBookmarkDeleted(bookmark: Bookmark): void
    }
    
    IComicRepository <|-- ComicRepository
    ISettingsRepository <|-- SettingsRepository
    IBookmarkRepository <|-- BookmarkRepository
    
    ComicRepository --> IArchiveService
    ComicRepository --> ICacheManager
    ComicRepository --> IFileService
    ComicRepository --> DriftDatabase
    
    SettingsRepository --> SharedPreferences
    SettingsRepository --> DriftDatabase
    
    BookmarkRepository --> DriftDatabase
    BookmarkRepository --> IFileService
    BookmarkRepository --> IThumbnailService
```

### 2.2 数据访问流程图

```mermaid
flowchart TD
    A[数据访问请求] --> B{请求类型}
    
    B -->|获取漫画| C[ComicRepository.getComic]
    B -->|获取页面| D[ComicRepository.getPages]
    B -->|获取设置| E[SettingsRepository.getSettings]
    B -->|获取书签| F[BookmarkRepository.getBookmarks]
    
    C --> G{数据库查询}
    G -->|存在| H[返回漫画信息]
    G -->|不存在| I[抛出NotFound异常]
    
    D --> J{缓存检查}
    J -->|命中| K[返回缓存页面]
    J -->|未命中| L[Archive解压]
    L --> M[图像处理优化]
    M --> N[存储到缓存]
    N --> O[返回页面列表]
    
    E --> P{缓存检查}
    P -->|有效| Q[返回缓存设置]
    P -->|过期| R[加载持久化设置]
    R --> S[更新缓存]
    S --> T[返回设置对象]
    
    F --> U[数据库查询书签]
    U --> V[加载缩略图路径]
    V --> W[排序并返回]
    
    subgraph "错误处理"
        X[异常捕获] --> Y{异常类型}
        Y -->|网络错误| Z[重试机制]
        Y -->|文件错误| AA[降级处理]
        Y -->|数据错误| BB[默认值返回]
        Y -->|系统错误| CC[错误日志记录]
    end
    
    I --> X
    L --> X
    M --> X
    R --> X
    U --> X
```

## 3. Service层组件关系图

### 3.1 核心服务架构

```mermaid
classDiagram
    class INavigationService {
        <<interface>>
        +goToPage(page: int, saveToHistory: bool): bool
        +goToNextPage(): bool
        +goToPreviousPage(): bool
        +goBack(): bool
        +canGoNext(): bool
        +canGoPrevious(): bool
        +canGoBack(): bool
        +jumpToBookmark(bookmark: Bookmark): bool
        +getCurrentPage(): int
        +getTotalPages(): int
        +getNavigationHistory(): List~int~
    }
    
    class NavigationService {
        -currentPage: int
        -totalPages: int
        -navigationHistory: List~int~
        -maxHistorySize: int
        -_pageChangeStreamController: StreamController
        
        +goToPage(page: int, saveToHistory: bool): bool
        +goToNextPage(): bool
        +goToPreviousPage(): bool
        +goBack(): bool
        +canGoNext(): bool
        +canGoPrevious(): bool
        +canGoBack(): bool
        +jumpToBookmark(bookmark: Bookmark): bool
        +updateCurrentPage(page: int): void
        +updateTotalPages(total: int): void
        -_addToHistory(page: int): void
        -_notifyPageChanged(oldPage: int, newPage: int): void
    }
    
    class IZoomService {
        <<interface>>
        +setZoom(scale: double, focalPoint: Offset?, animated: bool): void
        +zoomIn(): void
        +zoomOut(): void
        +resetZoom(): void
        +fitToScreen(): void
        +fitToWidth(): void
        +fitToHeight(): void
        +getCurrentZoom(): double
        +getMinZoom(): double
        +getMaxZoom(): double
    }
    
    class ZoomService {
        -currentZoom: double
        -minZoom: double
        -maxZoom: double
        -defaultZoom: double
        -_zoomChangeStreamController: StreamController
        
        +setZoom(scale: double, focalPoint: Offset?, animated: bool): void
        +zoomIn(): void
        +zoomOut(): void
        +resetZoom(): void
        +fitToScreen(): void
        +fitToWidth(): void
        +fitToHeight(): void
        +getCurrentZoom(): double
        -_clampZoom(scale: double): double
        -_notifyZoomChanged(oldZoom: double, newZoom: double): void
    }
    
    class IAutoPageService {
        <<interface>>
        +start(interval: Duration, onPageChange: Function()): void
        +stop(): void
        +pause(): void
        +resume(): void
        +updateInterval(interval: Duration): void
        +isRunning(): bool
        +isPaused(): bool
        +getCurrentInterval(): Duration
    }
    
    class AutoPageService {
        -timer: Timer?
        -interval: Duration
        -isRunning: bool
        -isPaused: bool
        -onPageChange: Function()?
        
        +start(interval: Duration, onPageChange: Function()): void
        +stop(): void
        +pause(): void
        +resume(): void
        +updateInterval(interval: Duration): void
        +isRunning(): bool
        +isPaused(): bool
        -_startTimer(): void
    }
    
    class IThemeService {
        <<interface>>
        +getCurrentTheme(): BackgroundTheme
        +setTheme(theme: BackgroundTheme): void
        +getBrightness(): double
        +setBrightness(brightness: double): void
        +getAvailableThemes(): List~BackgroundTheme~
        +watchThemeChanges(): Stream~BackgroundTheme~
    }
    
    class ThemeService {
        -currentTheme: BackgroundTheme
        -currentBrightness: double
        -_themeChangeStreamController: StreamController
        
        +getCurrentTheme(): BackgroundTheme
        +setTheme(theme: BackgroundTheme): void
        +getBrightness(): double
        +setBrightness(brightness: double): void
        +getAvailableThemes(): List~BackgroundTheme~
        +watchThemeChanges(): Stream~BackgroundTheme~
        -_notifyThemeChanged(oldTheme: BackgroundTheme, newTheme: BackgroundTheme): void
    }
    
    INavigationService <|-- NavigationService
    IZoomService <|-- ZoomService
    IAutoPageService <|-- AutoPageService
    IThemeService <|-- ThemeService
```

### 3.2 服务协作关系图

```mermaid
graph TB
    subgraph "Service Layer Collaboration"
        A[ReaderBloc<br/>状态管理中心] --> B[INavigationService<br/>导航服务]
        A --> C[IZoomService<br/>缩放服务]
        A --> D[IAutoPageService<br/>自动翻页服务]
        A --> E[IThemeService<br/>主题服务]
        A --> F[ICacheManager<br/>缓存管理服务]
        A --> G[IAnalyticsService<br/>分析服务]
        
        B --> H[页面导航逻辑<br/>历史记录管理]
        B --> I[书签跳转功能<br/>快速定位]
        
        C --> J[缩放级别控制<br/>手势缩放支持]
        C --> K[智能缩放算法<br/>适配不同屏幕]
        
        D --> L[定时翻页功能<br/>自动播放]
        D --> M[暂停/恢复控制<br/>用户交互感知]
        
        E --> N[主题切换逻辑<br/>亮度调节]
        E --> O[视觉效果管理<br/>用户体验优化]
        
        F --> P[多级缓存策略<br/>性能优化]
        F --> Q[预加载管理<br/>流畅体验]
        
        G --> R[用户行为统计<br/>使用模式分析]
        G --> S[性能指标收集<br/>优化指导]
    end
    
    subgraph "Service Communication"
        T[服务间通信] --> U[Event Bus<br/>事件总线]
        T --> V[Stream Communication<br/>流式通信]
        T --> W[Callback Pattern<br/>回调模式]
        
        U --> X[NavigationChanged Event<br/>页面变更事件]
        U --> Y[ZoomChanged Event<br/>缩放变更事件]
        U --> Z[ThemeChanged Event<br/>主题变更事件]
        
        V --> AA[Page Stream<br/>页面变更流]
        V --> BB[Zoom Stream<br/>缩放变更流]
        V --> CC[Settings Stream<br/>设置变更流]
    end
```

## 4. 缓存系统架构 (LRU + 异步加载)

### 4.1 缓存管理器详细架构

```mermaid
classDiagram
    class ICacheManager {
        <<interface>>
        +getCachedPages(comicId: String): Future~List~ComicPage?~
        +cachePages(comicId: String, pages: List~ComicPage~): Future~void~
        +preloadImage(page: ComicPage, priority: int): Future~void~
        +clearCache(type: CacheType): Future~void~
        +getCacheStats(): CacheStats
        +optimizeCache(): Future~void~
    }
    
    class CacheManager {
        -memoryCache: LRUCache~String, ComicPage~
        -diskCache: DiskLRUCache
        -preloadQueue: PriorityQueue~PreloadRequest~
        -maxMemorySize: int
        -maxDiskSize: int
        -isProcessingQueue: bool
        -memoryPressureMonitor: MemoryPressureMonitor
        
        +getCachedPages(comicId: String): Future~List~ComicPage?~
        +cachePages(comicId: String, pages: List~ComicPage~): Future~void~
        +preloadImage(page: ComicPage, priority: int): Future~void~
        +clearCache(type: CacheType): Future~void~
        +getCacheStats(): CacheStats
        +optimizeCache(): Future~void~
        -_processPreloadQueue(): Future~void~
        -_optimizeImageForDisplay(imageData: Uint8List): Future~Uint8List~
        -_calculateOptimalImageSize(width: int, height: int, screenSize: Size): Size
        -_isPageCached(page: ComicPage): bool
        -_cacheProcessedImage(pageIndex: int, data: Uint8List): Future~void~
        -_cachePagesOnDisk(comicId: String, pages: List~ComicPage~): Future~void~
        -_deserializePages(data: Uint8List): Future~List~ComicPage~~
        -_initializeCleanupTimer(): void
    }
    
    class LRUCache {
        -maxSize: int
        -cache: LinkedHashMap~K, CacheEntry~V~~
        -currentSize: int
        
        +get(key: K): V?
        +put(key: K, value: V, size: int): void
        +remove(key: K): V?
        +clear(): void
        +getCurrentSize(): int
        +getMaxSize(): int
        -_ensureCapacity(requiredSize: int): void
    }
    
    class CacheEntry {
        +value: V
        +size: int
        +accessTime: DateTime
        +hitCount: int
    }
    
    class DiskLRUCache {
        -cacheDirectory: Directory
        -maxSize: int
        -currentSize: int
        -journal: CacheJournal
        
        +get(key: String): Future~Uint8List?~
        +put(key: String, data: Uint8List): Future~void~
        +remove(key: String): Future~void~
        +clear(): Future~void~
        +size(): int
        -_evictLeastRecentlyUsed(): Future~void~
        -_updateJournal(): Future~void~
    }
    
    class PriorityQueue {
        -heap: List~PreloadRequest~
        
        +add(request: PreloadRequest): void
        +removeFirst(): PreloadRequest
        +isEmpty(): bool
        +length(): int
        -_bubbleUp(index: int): void
        -_bubbleDown(index: int): void
    }
    
    class PreloadRequest {
        +page: ComicPage
        +priority: int
        +timestamp: DateTime
        +retryCount: int
    }
    
    class MemoryPressureMonitor {
        -warningThreshold: double
        -criticalThreshold: double
        -_memoryWarningStreamController: StreamController
        
        +startMonitoring(): void
        +stopMonitoring(): void
        +getCurrentMemoryUsage(): double
        +getAvailableMemory(): int
        +watchMemoryPressure(): Stream~MemoryPressureLevel~
        -_checkMemoryPressure(): Future~void~
        -_handleMemoryWarning(level: MemoryPressureLevel): void
    }
    
    ICacheManager <|-- CacheManager
    CacheManager --> LRUCache
    CacheManager --> DiskLRUCache
    CacheManager --> PriorityQueue
    CacheManager --> MemoryPressureMonitor
    LRUCache --> CacheEntry
    PriorityQueue --> PreloadRequest
```

### 4.2 缓存策略流程图

```mermaid
flowchart TD
    A[图像缓存请求] --> B{内存缓存检查}
    
    B -->|命中| C[直接返回<br/>更新访问时间]
    B -->|未命中| D{磁盘缓存检查}
    
    D -->|命中| E[异步加载到内存<br/>更新LRU顺序]
    D -->|未命中| F[添加到预加载队列<br/>按优先级排序]
    
    E --> G[内存缓存存储<br/>检查容量限制]
    F --> H[队列处理器<br/>后台异步处理]
    
    G --> I{内存容量检查}
    I -->|超出限制| J[LRU淘汰算法<br/>删除最少使用项]
    I -->|容量充足| K[存储完成]
    
    H --> L[从队列取出请求<br/>按优先级顺序]
    L --> M[图像解压和处理<br/>多线程并行]
    M --> N[图像优化<br/>尺寸和质量调整]
    N --> O[双重缓存存储<br/>内存+磁盘]
    
    J --> K
    O --> P[通知UI更新<br/>图像准备就绪]
    
    subgraph "预加载优先级策略"
        Q[当前页面] --> R[优先级: 10<br/>立即处理]
        S[相邻页面±1] --> T[优先级: 8<br/>高优先级]
        U[相邻页面±2] --> V[优先级: 6<br/>中优先级]
        W[书签页面] --> X[优先级: 4<br/>低优先级]
        Y[历史页面] --> Z[优先级: 2<br/>后台处理]
    end
    
    subgraph "内存压力监控"
        AA[内存监控器] --> BB{内存使用率}
        BB -->|< 80%| CC[正常运行<br/>全速预加载]
        BB -->|80-90%| DD[警告状态<br/>减少预加载]
        BB -->|90-95%| EE[危险状态<br/>暂停预加载]
        BB -->|> 95%| FF[紧急状态<br/>强制清理缓存]
        
        DD --> GG[降低图像质量<br/>减少缓存项目]
        EE --> HH[暂停队列处理<br/>清理非关键缓存]
        FF --> II[强制GC<br/>清空所有缓存]
    end
```

### 4.3 异步加载时序图

```mermaid
sequenceDiagram
    participant UI as UI Component
    participant Cache as Cache Manager
    participant Memory as Memory Cache
    participant Disk as Disk Cache
    participant Queue as Preload Queue
    participant Worker as Background Worker
    participant Archive as Archive Service
    
    UI->>Cache: 请求页面图像
    activate Cache
    
    Cache->>Memory: 检查内存缓存
    activate Memory
    Memory-->>Cache: 缓存未命中
    deactivate Memory
    
    Cache->>Disk: 检查磁盘缓存
    activate Disk
    Disk-->>Cache: 缓存未命中
    deactivate Disk
    
    Cache->>Queue: 添加预加载请求(高优先级)
    activate Queue
    Queue-->>Cache: 请求已排队
    deactivate Queue
    
    Cache-->>UI: 返回占位符/加载中状态
    deactivate Cache
    
    par 后台处理
        Queue->>Worker: 触发队列处理
        activate Worker
        
        Worker->>Archive: 解压图像数据
        activate Archive
        Archive-->>Worker: 原始图像数据
        deactivate Archive
        
        Worker->>Worker: 图像优化处理
        Note over Worker: 尺寸调整、压缩优化
        
        Worker->>Memory: 存储到内存缓存
        activate Memory
        Memory-->>Worker: 存储完成
        deactivate Memory
        
        Worker->>Disk: 存储到磁盘缓存
        activate Disk
        Disk-->>Worker: 存储完成
        deactivate Disk
        
        Worker-->>UI: 通知图像准备就绪
        deactivate Worker
    and 预加载相邻页面
        Cache->>Queue: 添加相邻页面请求(中优先级)
        Queue->>Worker: 后台预处理
        Worker->>Archive: 批量解压相邻页面
        Worker->>Memory: 批量缓存存储
    end
    
    UI->>Cache: 再次请求同一页面
    Cache->>Memory: 检查内存缓存
    Memory-->>Cache: 缓存命中
    Cache-->>UI: 直接返回缓存图像
```

## 5. 组件集成和通信机制

### 5.1 组件间通信架构

```mermaid
graph TB
    subgraph "Communication Patterns"
        A[BLoC Pattern<br/>状态管理通信] --> B[Event -> State<br/>单向数据流]
        A --> C[Stream Communication<br/>异步数据流]
        
        D[Repository Pattern<br/>数据访问通信] --> E[Interface Abstraction<br/>接口抽象]
        D --> F[Dependency Injection<br/>依赖注入]
        
        G[Service Layer<br/>业务服务通信] --> H[Callback Pattern<br/>回调模式]
        G --> I[Observer Pattern<br/>观察者模式]
        
        J[Platform Channel<br/>平台通信] --> K[Method Channel<br/>方法调用]
        J --> L[Event Channel<br/>事件流]
    end
    
    subgraph "Data Flow Integration"
        M[UI Layer] --> N[Event Dispatch<br/>事件分发]
        N --> O[BLoC Processing<br/>业务逻辑处理]
        O --> P[Repository Access<br/>数据访问]
        P --> Q[Service Coordination<br/>服务协调]
        Q --> R[Platform Integration<br/>平台集成]
        
        R --> S[Data Response<br/>数据响应]
        S --> T[State Update<br/>状态更新]
        T --> U[UI Rebuild<br/>界面重建]
    end
    
    subgraph "Error Propagation"
        V[Error Detection<br/>错误检测] --> W[Error Handling<br/>错误处理]
        W --> X[Error Recovery<br/>错误恢复]
        X --> Y[User Notification<br/>用户通知]
        
        W --> Z[Logging<br/>日志记录]
        W --> AA[Analytics<br/>分析报告]
        W --> BB[Crash Reporting<br/>崩溃报告]
    end
```

### 5.2 依赖注入和服务定位

```mermaid
classDiagram
    class ServiceLocator {
        -_services: Map~Type, dynamic~
        -_factories: Map~Type, Function~
        -_singletons: Map~Type, dynamic~
        
        +registerFactory~T~(factory: Function): void
        +registerSingleton~T~(instance: T): void
        +registerLazySingleton~T~(factory: Function): void
        +get~T~(): T
        +isRegistered~T~(): bool
        +unregister~T~(): void
        +reset(): void
    }
    
    class DependencyInjection {
        +init(): Future~void~
        -_registerRepositories(): void
        -_registerServices(): void
        -_registerPlatformServices(): void
        -_registerExternalServices(): void
    }
    
    class RepositoryModule {
        +register(locator: ServiceLocator): void
    }
    
    class ServiceModule {
        +register(locator: ServiceLocator): void
    }
    
    class PlatformModule {
        +register(locator: ServiceLocator): void
    }
    
    DependencyInjection --> ServiceLocator
    DependencyInjection --> RepositoryModule
    DependencyInjection --> ServiceModule
    DependencyInjection --> PlatformModule
    
    ServiceLocator --> IComicRepository
    ServiceLocator --> ISettingsRepository
    ServiceLocator --> IBookmarkRepository
    ServiceLocator --> ICacheManager
    ServiceLocator --> INavigationService
    ServiceLocator --> IZoomService
    ServiceLocator --> IBrightnessService
```

---

## 总结

本核心组件架构图详细展示了Flutter漫画阅读器的核心组件内部结构和交互关系：

### 关键架构特点

1. **ReaderBloc状态管理**
   - 完整的事件驱动状态机
   - 异步数据流处理
   - 错误处理和恢复机制
   - 自动化进度保存和预加载

2. **Repository模式数据层**
   - 清晰的接口抽象
   - 多数据源整合
   - 缓存策略集成
   - 错误处理和重试机制

3. **Service层业务逻辑**
   - 服务间松耦合协作
   - 流式通信机制
   - 职责明确分离
   - 可扩展的架构设计

4. **智能缓存系统**
   - 多级LRU缓存策略
   - 优先级预加载队列
   - 内存压力自适应
   - 异步处理优化

5. **组件集成机制**
   - 依赖注入管理
   - 统一通信模式
   - 错误传播处理
   - 平台服务集成

这个架构设计确保了系统的高性能、可维护性和扩展性，为后续的代码实现提供了详细的技术指导。