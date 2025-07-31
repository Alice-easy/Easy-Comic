# Flutter 漫画阅读器功能规范文档

**版本**: 1.0
**日期**: 2025-07-31
**作者**: 规范编写器

## 1. 概述

本文档旨在详细定义 Flutter 漫画阅读器的功能需求，将用户需求分解为清晰的功能模块，并为每个模块提供核心组件、职责和伪代码实现。本文档将作为开发团队后续实现的技术蓝图。

## 2. 核心架构与状态管理

### 2.1. 状态管理

- **方案**: 采用 `Riverpod` 进行状态管理，确保应用状态的一致性和可维护性。
- **核心 Provider**:
    - `readerSettingsProvider`: 管理阅读器相关的持久化设置。
    - `readerUIStateProvider`: 管理阅读器界面的临时 UI 状态。
    - `comicStateProvider`: 管理当前打开漫画的状态，包括页面列表、当前页码等。

### 2.2. 架构模式

- **数据层**: 使用 `Repository` 模式处理数据逻辑，分离业务逻辑和 UI。
- **服务层**: 构建 `Service` 层处理文件操作、缓存管理和设置存储等核心功能。

---

## 3. 功能模块详解

### 3.1. 核心显示与导航 (Core Display & Navigation)

#### 3.1.1. 组件与职责

- **`ReaderCore` (Widget)**:
    - **职责**: 作为核心视图容器，根据阅读模式（PageView 或连续滚动）展示漫画页面。
    - **组件**:
        - `PageView`: 用于分页阅读模式。
        - `CustomScrollView` / `ListView`: 用于连续滚动阅读模式。
        - `GestureDetector`: 用于处理翻页手势。

- **`NavigationService` (Service)**:
    - **职责**: 管理导航逻辑，包括上一页、下一页、跳转等。

- **`PageIndicator` (Widget)**:
    - **职责**: 显示当前阅读进度，如 `E1:P1`。

- **`AutoPageService` (Service)**:
    - **职责**: 实现定时自动翻页功能。

#### 3.1.2. 伪代码

```pseudo
// ReaderCore Widget
class ReaderCore extends Widget {
  build(context) {
    final settings = context.watch(readerSettingsProvider);
    final comicState = context.watch(comicStateProvider);

    if (settings.readingMode == ReadingMode.PageView) {
      return PageView.builder(
        controller: pageController,
        itemCount: comicState.pages.length,
        itemBuilder: (context, index) => ComicPageWidget(page: comicState.pages[index]),
        onPageChanged: (index) => context.read(comicStateProvider.notifier).goToPage(index),
      );
    } else { // Continuous Scroll
      return ListView.builder(
        itemCount: comicState.pages.length,
        itemBuilder: (context, index) => ComicPageWidget(page: comicState.pages[index]),
      );
    }
  }
}

// NavigationService
class NavigationService {
  function goToNextPage() {
    if (canGoNext) {
      comicState.currentPage++;
    }
  }

  function goToPreviousPage() {
    if (canGoPrevious) {
      comicState.currentPage--;
    }
  }
}

// AutoPageService
class AutoPageService {
  Timer? timer;

  function start(interval) {
    timer = Timer.periodic(interval, (_) {
      navigationService.goToNextPage();
    });
  }

  function stop() {
    timer?.cancel();
  }
}
```

### 3.2. 阅读模式与屏幕配置 (Reading Mode & Screen Configuration)

#### 3.2.1. 组件与职责

- **`SettingsService` (Service)**:
    - **职责**: 管理屏幕方向、阅读方向（LTR/RTL）等设置。
- **`SystemChrome` (Flutter Service)**:
    - **职责**: 用于设置屏幕方向和全屏模式。
- **`OrientationBuilder` (Widget)**:
    - **职责**: 根据屏幕方向动态调整布局。
- **`Wakelock` (Plugin)**:
    - **职责**: 保持屏幕常亮。

#### 3.2.2. 伪代码

```pseudo
// Settings Panel Widget
class SettingsPanel extends Widget {
  build(context) {
    final settings = context.watch(readerSettingsProvider);

    // Button to toggle reading direction
    Button(
      onClick: () {
        final newDirection = settings.direction == RTL ? LTR : RTL;
        context.read(readerSettingsProvider.notifier).setDirection(newDirection);
      },
      child: Text("Toggle Reading Direction"),
    );

    // Button to lock orientation
    Button(
      onClick: () {
        SystemChrome.setPreferredOrientations([DeviceOrientation.landscapeLeft]);
      },
      child: Text("Lock Landscape"),
    );
  }
}

// ReaderScreen Widget
class ReaderScreen extends Widget {
  initState() {
    Wakelock.enable(); // Enable wakelock
  }

  dispose() {
    Wakelock.disable(); // Disable wakelock
  }

  build(context) {
    return OrientationBuilder(
      builder: (context, orientation) {
        if (orientation == Orientation.portrait) {
          return PortraitLayout();
        } else {
          return LandscapeLayout();
        }
      }
    );
  }
}
```

### 3.3. 缩放与交互控制 (Zoom & Interaction Control)

#### 3.3.1. 组件与职责

- **`InteractiveViewer` (Widget)**:
    - **职责**: 提供内置的平移和捏合缩放功能。
- **`GestureDetector` (Widget)**:
    - **职责**: 监听双击事件以实现智能缩放，并处理点击区域翻页。
- **`HardwareButtons` (Plugin)**:
    - **职责**: 监听音量键事件以实现翻页。

#### 3.3.2. 伪代码

```pseudo
// ComicPageWidget
class ComicPageWidget extends Widget {
  build(context) {
    return GestureDetector(
      onDoubleTap: () => zoomService.toggleZoom(),
      onTapDown: (details) => handleTapNavigation(details.globalPosition),
      child: InteractiveViewer(
        transformationController: zoomController,
        child: Image.memory(page.imageData),
      ),
    );
  }

  function handleTapNavigation(position) {
    final screenWidth = context.screenWidth;
    final tapZone = screenWidth * 0.25;

    if (position.dx < tapZone) {
      navigationService.goToPreviousPage();
    } else if (position.dx > screenWidth - tapZone) {
      navigationService.goToNextPage();
    } else {
      uiState.toggleControls();
    }
  }
}

// ReaderScreen Widget
class ReaderScreen extends Widget {
  initState() {
    // Listen to volume button events
    hardwareButtons.volumeButtonEvents.listen((event) {
      if (event == VolumeButtonEvent.volumeUp) {
        navigationService.goToPreviousPage();
      } else if (event == VolumeButtonEvent.volumeDown) {
        navigationService.goToNextPage();
      }
    });
  }
}
```

### 3.4. 视觉效果与主题 (Visual Effects & Theme)

#### 3.4.1. 组件与职责

- **`ThemeService` (Service)**:
    - **职责**: 管理应用的主题（暗色/亮色），并提供动态调整亮度的滤镜。
- **`SystemChrome` (Flutter Service)**:
    - **职责**: 用于进入和退出全屏模式。
- **`AnimationController` (Flutter Class)**:
    - **职责**: 为所有交互提供平滑的动画效果。

#### 3.4.2. 伪代码

```pseudo
// ThemeService
class ThemeService {
  ThemeData currentTheme = Themes.light;

  function toggleTheme() {
    currentTheme = (currentTheme == Themes.light) ? Themes.dark : Themes.light;
    notifyListeners();
  }

  function getImageColorFilter(brightness) {
    // Returns a ColorFilter to adjust image brightness
    return ColorFilter.matrix([
      brightness, 0, 0, 0, 0,
      0, brightness, 0, 0, 0,
      0, 0, brightness, 0, 0,
      0, 0, 0, 1, 0,
    ]);
  }
}

// ReaderScreen Widget
class ReaderScreen extends Widget {
  function toggleFullscreen() {
    if (isFullscreen) {
      SystemChrome.setEnabledSystemUIMode(SystemUiMode.edgeToEdge);
    } else {
      SystemChrome.setEnabledSystemUIMode(SystemUiMode.immersive);
    }
    isFullscreen = !isFullscreen;
  }
}
```

### 3.5. 文件处理与性能优化 (File Handling & Performance)

#### 3.5.1. 组件与职责

- **`ArchiveService` (Service)**:
    - **职责**: 使用 `archive` 插件处理压缩文件（CBZ, CBR, ZIP, RAR）。
- **`CacheService` (Service)**:
    - **职责**: 实现基于 LRU 算法的图像缓存系统，管理内存和磁盘缓存。
- **`ComicRepository` (Repository)**:
    - **职责**: 协调异步图像加载，防止 UI 线程阻塞。

#### 3.5.2. 伪代码

```pseudo
// ArchiveService
class ArchiveService {
  function extractPages(filePath) {
    final file = File(filePath);
    final bytes = file.readAsBytesSync();
    
    if (filePath.endsWith('.zip') || filePath.endsWith('.cbz')) {
      final archive = ZipDecoder().decodeBytes(bytes);
      return archive.files.where((f) => f.isFile).map((f) => f.content).toList();
    } else if (filePath.endsWith('.rar') || filePath.endsWith('.cbr')) {
      // Use a suitable RAR library implementation
      // ...
    }
    return [];
  }
}

// CacheService
class CacheService {
  LRUCache<String, Uint8List> memoryCache;
  DiskCache diskCache;

  function getImage(pageUrl) {
    if (memoryCache.contains(pageUrl)) {
      return memoryCache.get(pageUrl);
    }
    
    final diskData = await diskCache.get(pageUrl);
    if (diskData != null) {
      memoryCache.put(pageUrl, diskData);
      return diskData;
    }

    // If not in cache, load from source and cache it
    final imageData = await comicRepository.loadImage(pageUrl);
    memoryCache.put(pageUrl, imageData);
    await diskCache.put(pageUrl, imageData);
    return imageData;
  }
}
```

### 3.6. 用户界面组件 (UI Components)

#### 3.6.1. 组件与职责

- **`ReaderBottomBar` (Widget)**:
    - **职责**: 提供播放、暂停、翻页等控制按钮。
- **`SettingsPage` (Widget)**:
    - **职责**: 提供用户配置界面，使用 `SharedPreferences` 持久化设置。
- **`ChapterSlider` (Widget)**:
    - **职责**: 允许用户在章节内快速导航。
- **`BookmarkService` (Service)**:
    - **职责**: 使用 `SQLite` (via `drift`) 管理书签和阅读进度。

#### 3.6.2. 伪代码

```pseudo
// ReaderBottomBar Widget
class ReaderBottomBar extends Widget {
  build(context) {
    return BottomAppBar(
      child: Row(
        children: [
          IconButton(icon: Icon(Icons.skip_previous), onPressed: () => navigationService.goToPreviousPage()),
          IconButton(icon: Icon(Icons.play_arrow), onPressed: () => autoPageService.start()),
          IconButton(icon: Icon(Icons.skip_next), onPressed: () => navigationService.goToNextPage()),
          ChapterSlider(),
        ],
      ),
    );
  }
}

// SettingsService
class SettingsService {
  SharedPreferences prefs;

  function saveSetting(key, value) {
    prefs.setString(key, value);
  }

  function getSetting(key) {
    return prefs.getString(key);
  }
}

// BookmarkService
class BookmarkService {
  DriftDatabase db;

  function addBookmark(comicId, pageIndex) {
    db.insert(bookmarksTable, {comicId: comicId, page: pageIndex});
  }

d
  function getBookmarks(comicId) {
    return db.select(bookmarksTable).where((b) => b.comicId.equals(comicId)).get();
  }
}
```

---

## 4. 第三方库依赖

根据功能需求，推荐使用以下第三方库：

- **状态管理**:
    - `flutter_riverpod`: 用于应用范围内的状态管理。
- **数据持久化**:
    - `shared_preferences`: 用于存储简单的键值对设置。
    - `drift`: 用于构建类型安全的 SQLite 数据库，管理书签和阅读进度。
- **文件处理**:
    - `archive`: 用于解压 CBZ/ZIP 等格式的漫画文件。
    - `file_picker`: 用于让用户从设备中选择漫画文件。
- **UI & 交互**:
    - `interactive_viewer`: Flutter 内置，用于图像的缩放和平移。
    - `photo_view`: 功能更强大的图像查看器，可作为 `InteractiveViewer` 的替代或补充。
    - `wakelock`: 保持屏幕常亮。
    - `hardware_buttons`: 监听音量键等物理按键事件。
    - `flutter_bloc`: (备选) 如果团队更熟悉 BLoC 模式，可作为 Riverpod 的替代方案。
- **系统与硬件**:
    - `path_provider`: 获取设备上的标准目录路径。
    - `permission_handler`: 用于请求文件读写等权限。
- **图像**:
    - `cached_network_image`: 如果需要支持网络漫画，此库能提供强大的缓存功能。对于本地文件，自定义缓存方案更佳。