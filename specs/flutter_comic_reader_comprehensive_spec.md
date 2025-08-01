# Flutter 漫画阅读器 - 全面功能规格文档

**版本:** 1.0
**日期:** 2025-08-01

## 1. 简介

本文档为 "Easy-Comic" Flutter 漫画阅读器应用提供了全面、模块化的功能规格。它基于现有的架构（Clean Architecture, BLoC, Repository）进行扩展，整合了新的核心功能，如书架、收藏夹和 WebDAV 同步。本文档旨在作为后续架构设计、开发和测试的主要依据。

---

## 2. 核心功能模块

应用被分解为以下九个核心功能模块，以实现高度内聚和低耦合的设计。

| 模块 ID | 模块名称 | 核心职责 |
| :--- | :--- | :--- |
| **MOD-01** | **应用核心 (App Core)** | 应用生命周期管理、启动流程、依赖注入和全局配置。 |
| **MOD-02** | **数据持久化 (Persistence)** | 管理本地数据库（漫画、设置、书签、收藏），确保数据一致性和完整性。 |
| **MOD-03** | **文件管理 (File System)** | 处理本地文件系统操作，包括漫画文件导入、访问和元数据提取。 |
| **MOD-04** | **书架 (Bookshelf)** | 管理用户漫画库，提供分组、排序和搜索功能。 |
| **MOD-05** | **阅读器 (Reader)** | 提供核心漫画阅读体验，包括多种阅读模式、手势交互和 UI 控制。 |
| **MOD-06** | **收藏夹 (Favorites)** | 管理用户收藏的漫画，独立于书架。 |
| **MOD-07** | **WebDAV 同步 (Sync)** | 处理与 WebDAV 服务器的数据同步，包括阅读进度、书签和设置。 |
| **MOD-08** | **用户界面 (UI)** | 提供统一的视觉风格、主题管理和可重用的 UI 组件。 |
| **MOD-09** | **服务层 (Services)** | 封装核心业务逻辑，如错误处理、性能监控和后台任务。 |

---

## 3. 数据结构定义 (Data Structures)

为确保数据模型的一致性，定义以下核心实体。

### 3.1 `Comic` - 漫画实体

代表一本漫画的核心信息。

```
CLASS Comic
  STRING id (Primary Key, e.g., UUID or SHA256 hash of file path)
  STRING filePath (本地文件绝对路径)
  STRING title (漫画标题, from metadata or filename)
  STRING coverImagePath (封面图片路径, может быть извлечена из архива)
  INTEGER pageCount (总页数)
  INTEGER currentPage (当前阅读页码, default: 0)
  DOUBLE readingProgress (阅读进度, 0.0 to 1.0)
  DATETIME lastOpened (上次打开时间)
  DATETIME addedToLibrary (添加到库的时间)
  BOOLEAN isFavorite (是否已收藏)
  STRING bookshelfId (所属书架ID, Foreign Key)
END CLASS
```

### 3.2 `Bookshelf` - 书架实体

代表一个用户定义的漫画分组。

```
CLASS Bookshelf
  STRING id (Primary Key, e.g., 'default', 'reading', UUID)
  STRING name (书架名称, e.g., "我的最爱", "计划阅读")
  INTEGER comicCount (书架中的漫画数量)
  DATETIME createdDate (创建日期)
END CLASS
```

### 3.3 `Bookmark` - 书签实体

代表在漫画中添加的书签。

```
CLASS Bookmark
  STRING id (Primary Key)
  STRING comicId (Foreign Key to Comic)
  INTEGER pageIndex (书签所在页码)
  STRING note (书签备注, optional)
  DATETIME createdDate
END CLASS
```

### 3.4 `AppSettings` - 应用设置实体

存储应用的全局配置。

```
CLASS AppSettings
  STRING id ('singleton_settings')
  STRING themeMode ('light', 'dark', 'system')
  STRING webdavUrl
  STRING webdavUsername
  STRING webdavPassword (Encrypted)
  BOOLEAN isWebdavEnabled
  DATETIME lastSyncTimestamp
END CLASS
```

### 3.5 `ReaderSettings` - 阅读器设置实体

存储与阅读器界面相关的个性化设置。

```
CLASS ReaderSettings
  STRING id ('singleton_reader_settings')
  STRING readingDirection ('left_to_right', 'right_to_left')
  STRING readingMode ('single_page', 'dual_page', 'webtoon')
  BOOLEAN lockOrientation
  BOOLEAN screenOn
END CLASS
```

### 3.6 `SyncState` - 同步状态实体 (非持久化)

用于在 UI 层反映当前的同步状态。

```
CLASS SyncState
  ENUM status ('idle', 'syncing', 'success', 'failed')
  STRING message (e.g., "同步中...", "同步成功", "错误信息")
  DOUBLE progress (0.0 to 1.0, for sync progress indication)
END CLASS
```

---

## 4. 关键逻辑伪代码 (Pseudocode)

### 4.1 WebDAV 同步逻辑 (`SyncService`)

```
CLASS SyncService
  PROPERTY webdavClient
  PROPERTY databaseService
  PROPERTY stateController

  FUNCTION syncAllData()
    IF stateController.isSyncing() THEN RETURN

    stateController.update(status: 'syncing', message: '开始同步...')

    isSuccess = webdavClient.connect(AppSettings.getCredentials())
    IF NOT isSuccess THEN
      stateController.update(status: 'failed', message: 'WebDAV 连接失败')
      RETURN
    END IF

    remoteSyncFilePath = "/easy_comic/sync_data.json"
    remoteJson = webdavClient.download(remoteSyncFilePath)
    
    localComics = databaseService.getAllComics()
    localBookmarks = databaseService.getAllBookmarks()

    merger = new DataMerger(remoteJson, localComics, localBookmarks)
    mergedResult = merger.merge()

    databaseService.updateComics(mergedResult.comicsToUpdate)
    databaseService.updateBookmarks(mergedResult.bookmarksToUpdate)

    uploadSuccess = webdavClient.upload(remoteSyncFilePath, mergedResult.fullSyncPackage)
    IF NOT uploadSuccess THEN
      stateController.update(status: 'failed', message: '数据上传失败')
      RETURN
    END IF

    AppSettings.setLastSyncTimestamp(now())
    stateController.update(status: 'success', message: '同步完成')
  END FUNCTION
END CLASS
```

### 4.2 文件导入与书架管理逻辑 (`BookshelfBloc`)

```
CLASS BookshelfBloc
  PROPERTY comicRepository
  PROPERTY bookshelfRepository
  PROPERTY archiveService
  PROPERTY stateController

  FUNCTION handleEvent(event)
    SWITCH event.type
      CASE 'ImportFile':
        importFile(event.filePath)
      CASE 'LoadBookshelf':
        loadBookshelf(event.bookshelfId)
      CASE 'MoveComic':
        moveComic(event.comicId, event.targetBookshelfId)
      CASE 'CreateBookshelf':
        createBookshelf(event.name)
    END SWITCH
  END FUNCTION

  PRIVATE FUNCTION importFile(filePath)
    stateController.emit(State.Loading("正在解析漫画..."))
    
    parsedResult = archiveService.parse(filePath)
    IF parsedResult.hasError() THEN
      stateController.emit(State.Error("文件解析失败"))
      RETURN
    END IF

    newComic = Comic.fromParsingResult(parsedResult, filePath)
    comicRepository.addComic(newComic)
    
    loadBookshelf(AppConstants.defaultBookshelfId)
  END FUNCTION

  PRIVATE FUNCTION loadBookshelf(bookshelfId)
    stateController.emit(State.Loading())
    comics = comicRepository.getComicsByBookshelf(bookshelfId)
    bookshelves = bookshelfRepository.getAll()
    stateController.emit(State.Loaded(comics, bookshelves))
  END FUNCTION

  PRIVATE FUNCTION moveComic(comicId, targetBookshelfId)
    comic = comicRepository.getComicById(comicId)
    comic.bookshelfId = targetBookshelfId
    comicRepository.updateComic(comic)
    loadBookshelf(stateController.currentBookshelfId)
  END FUNCTION

  PRIVATE FUNCTION createBookshelf(name)
    newBookshelf = new Bookshelf(name: name, id: generateUUID())
    bookshelfRepository.add(newBookshelf)
    loadBookshelf(stateController.currentBookshelfId)
  END FUNCTION
END CLASS
```
---
## 5. 模块详细规格

### MOD-01: 应用核心 (App Core)
- **职责**:
  - 管理应用的启动过程，包括初始化服务和加载配置。
  - 使用 `GetIt` 实现依赖注入，注册所有服务和仓库。
  - 处理应用的生命周期事件（暂停、恢复、终止）。
- **主要组件**:
  - `main.dart`: 应用入口。
  - `injection_container.dart`: 依赖注入配置。
  - `AppLifeCycleObserver.dart`: 监听应用生命周期。

### MOD-02: 数据持久化 (Persistence)
- **职责**:
  - 使用 `Drift` 实现本地数据库。
  - 定义数据库表结构（Comics, Bookshelves, Bookmarks, Settings）。
  - 提供 Repository 对数据进行增删改查。
  - 处理数据库迁移。
- **主要组件**:
  - `drift_db.dart`: 数据库定义和连接。
  - `ComicRepository.dart`, `SettingsRepository.dart`: 数据仓库接口和实现。

### MOD-03: 文件管理 (File System)
- **职责**:
  - 提供文件选择器来导入本地漫画文件（.cbz, .zip, .rar）。
  - 使用 `archive` 库解压漫画存档并读取页面列表。
  - 从漫画文件中提取元数据（如封面、标题）。
  - 管理应用缓存目录。
- **主要组件**:
  - `ArchiveService.dart`: 封装 `archive` 库的实现。
  - `FilePickerService.dart`: 封装 `file_picker` 的功能。

### MOD-04: 书架 (Bookshelf)
- **职责**:
  - 以网格或列表形式展示漫画库。
  - 支持创建、重命名、删除书架。
  - 支持将漫画在不同书架间移动。
  - 提供漫画排序（按标题、添加日期、上次阅读）和搜索功能。
- **主要组件**:
  - `HomeScreen.dart`: 书架主页面 UI。
  - `BookshelfBloc.dart`: 书架页面的状态管理。

### MOD-05: 阅读器 (Reader)
- **职责**:
  - 显示漫画页面，支持多种阅读模式。
  - 处理用户手势（翻页、缩放、平移）。
  - 管理阅读器 UI 叠加层（顶部/底部菜单栏）。
  - 自动记录阅读进度。
- **主要组件**:
  - `ReaderScreen.dart`: 阅读器主页面 UI。
  - `ReaderBloc.dart`: 阅读器状态管理。
  - `ReaderCore.dart`: 核心图像显示和交互组件。

### MOD-06: 收藏夹 (Favorites)
- **职责**:
  - 单独的页面显示所有标记为 "isFavorite" 的漫画。
  - 允许用户在书架或阅读器中快速添加/移除收藏。
  - 收藏夹的数据与书架解耦，仅通过 `Comic.isFavorite` 属性关联。
- **主要组件**:
  - `FavoritesScreen.dart`: 收藏夹 UI。
  - `FavoritesBloc.dart`: 收藏夹状态管理。

### MOD-07: WebDAV 同步 (Sync)
- **职责**:
  - 提供 UI 界面配置 WebDAV 服务器信息。
  - 在后台执行同步任务。
  - 实现双向合并逻辑，解决数据冲突。
  - 在 UI 上显示同步状态和结果。
- **主要组件**:
  - `SyncService.dart`: 核心同步逻辑。
  - `WebdavClient.dart`: 封装 WebDAV 通信。
  - `SettingsScreen.dart`: 提供同步设置入口。

### MOD-08: 用户界面 (UI)
- **职责**:
  - 定义应用的整体主题（颜色、字体、图标）。
  - 提供亮色/暗色模式切换。
  - 创建可重用的 UI 组件（如自定义按钮、对话框、加载指示器）。
- **主要组件**:
  - `theme.dart`: 应用主题定义。
  - `widgets/` 目录: 存放可重用的 Widget。

### MOD-09: 服务层 (Services)
- **职责**:
  - 提供统一的错误处理机制和日志记录。
  - 封装后台任务执行逻辑。
  - 缓存服务 (`CacheService.dart`)。
  - 导航服务 (`NavigationService.dart`)。
- **主要组件**:
  - `ErrorHandler.dart`: 全局错误处理器。
  - `BackgroundTaskManager.dart`: 管理后台任务。