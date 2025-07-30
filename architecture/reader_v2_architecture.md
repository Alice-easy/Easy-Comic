# 漫画阅读器 v2 - BLoC 架构设计

**作者**: 架构师模式
**日期**: 2025-07-30
**规范文档**: [`specs/reader_optimization_spec.pseudo`](../specs/reader_optimization_spec.pseudo)

## 1. 概述

本文档基于 `reader_optimization_spec.pseudo` 规范，为漫画阅读器 v2 功能设计了一套全新的、遵循 BLoC (Business Logic Component) 模式的架构。该架构旨在实现关注点分离，提高代码的可测试性、可维护性和可扩展性。

## 2. 整体架构图

下图展示了新架构的层次和组件间的关系。

```mermaid
graph TD
    subgraph "UI Layer"
        A[ReaderScreen] --> B{ReaderGestureDetector};
        A --> C[PageViewComponent];
        A --> D[TopMenuBar];
        A --> E[BottomProgressBar];
        A --> F[SettingsPanel];
    end

    subgraph "Business Logic Layer"
        G[ReaderBloc]
    end

    subgraph "Data Layer"
        H[ComicRepository]
        I[SettingsRepository]
    end

    subgraph "Data Sources"
        J[Drift Database]
        K[Comic Archive (ZIP/RAR)]
        L[SharedPreferences]
    end

    %% Interactions
    B -- "Dispatches Events (e.g., ToggleUiVisibilityEvent)" --> G;
    C -- "Dispatches Events (e.g., PageChangedEvent)" --> G;
    F -- "Dispatches Events (e.g., UpdateSettingEvent)" --> G;
    
    A -- "Listens to States" --> G;
    G -- "Emits States (ReaderState)" --> A;

    G -- "Requests Data" --> H;
    G -- "Requests/Saves Settings" --> I;

    H -- "Accesses Comic Data" --> J;
    H -- "Accesses Comic Files" --> K;
    I -- "Accesses User Preferences" --> L;
```

## 3. 组件职责详述

### 3.1. UI Layer (视图层)

UI 层完全由无状态 (Stateless) 或状态极简的 Widgets 组成。它们唯一的职责是根据 `ReaderState` 渲染 UI，并将用户交互转换为 `ReaderEvent` 发送给 `ReaderBloc`。

-   **`ReaderScreen`**: 顶层组件，通过 `BlocProvider` 注入 `ReaderBloc`，并使用 `BlocBuilder` 监听 `ReaderState` 的变化来构建整个屏幕的 UI。
-   **`ReaderGestureDetector`**: 包装在 `PageViewComponent` 外层，负责捕获全局手势，如单击（切换 UI 可见性）、双击（缩放）和捏合缩放，并分发相应的 `ReaderEvent`。
-   **`PageViewComponent`**: 核心内容展示区。根据 `ReaderState.settings.pageTurnMode` 动态选择渲染方式（水平 `PhotoViewGallery` 或垂直 `ListView`）。它负责展示漫画页面，并在页面切换时分发 `PageChangedEvent`。
-   **`TopMenuBar` / `BottomProgressBar`**: 根据 `ReaderState.isUiVisible` 控制自身的显隐。它们展示阅读进度、书签等信息，并提供交互按钮（如设置、添加书签）。
-   **`SettingsPanel`**: 一个模态框或底部工作表，允许用户修改阅读设置。用户的每次修改都会触发一个 `UpdateSettingEvent`。

### 3.2. Business Logic Layer (业务逻辑层)

-   **`ReaderBloc`**: 架构的核心。
    -   **输入**: 接收来自 UI 层的 `ReaderEvent`。
    -   **处理**:
        1.  响应 `LoadComicEvent`：通过 `ComicRepository` 和 `SettingsRepository` 加载漫画数据和用户设置，计算初始状态。在加载过程中发出 `isLoading: true` 的状态。
        2.  响应 `PageChangedEvent`：更新状态中的 `currentPage`，并调用 `ComicRepository` 保存阅读进度。
        3.  响应 `UpdateSettingEvent`：调用 `SettingsRepository` 保存新设置，并更新状态以使 UI 做出相应变化。
        4.  响应 `AddBookmarkEvent` / `DeleteBookmarkEvent`：调用 `ComicRepository` 更新书签数据。
        5.  管理 `isUiVisible` 和 `zoomScale` 等 UI 状态。
    -   **输出**: 发出一个新的、不可变的 `ReaderState` 流。UI 层监听到这个流就会重建。

### 3.3. Data Layer (数据层)

数据层将业务逻辑与数据来源完全解耦。`ReaderBloc` 不关心数据是来自数据库、文件还是网络。

-   **`ComicRepository`**: 负责所有与**单本漫画**相关的数据操作。
    -   从 `DriftDatabase` 获取漫画元数据、书签列表、阅读历史。
    -   与 `ComicArchive` 服务交互，解压并读取漫画图片文件。
    -   提供统一的接口（如 `getComicPages`, `saveBookmark`）给 BLoC 调用。
-   **`SettingsRepository`**: 负责所有与**全局阅读设置**相关的数据操作。
    -   使用 `SharedPreferences` 或类似的键值存储来持久化 `ReaderSettings`（如亮度、背景色、翻页模式）。
    -   提供 `getReaderSettings` 和 `saveReaderSettings` 接口。

## 4. 数据流（以“更改背景色”为例）

1.  **用户操作**: 用户在 `SettingsPanel` 中点击“夜间模式”按钮。
2.  **Event 分发**: `SettingsPanel` widget 调用 `context.read<ReaderBloc>().add(UpdateSettingEvent(newSettings))`，其中 `newSettings` 是一个包含了新背景色 `NIGHT` 的 `ReaderSettings` 对象。
3.  **BLoC 处理**:
    -   `ReaderBloc` 接收到 `UpdateSettingEvent`。
    -   它调用 `SettingsRepository.saveReaderSettings(event.newSettings)` 来持久化这个设置。
    -   `ReaderBloc` 使用 `emit()` 方法发出一个新的 `ReaderState` 副本，该副本的 `settings` 属性已更新为 `event.newSettings`。
4.  **UI 重建**:
    -   `ReaderScreen` 中的 `BlocBuilder` 接收到新的 `ReaderState`。
    -   `BlocBuilder` 的 `builder` 函数被触发，重建 UI。
    -   `PageViewComponent` 和其他相关组件根据新的 `state.settings.backgroundColor` 调整其外观，屏幕背景变为夜间模式。

## 5. 总结

该架构通过 BLoC 模式将 UI、业务逻辑和数据访问清晰地分离开来。这种分离使得：
-   **高内聚，低耦合**: 每个组件都有明确的单一职责。
-   **易于测试**: `ReaderBloc` 可以独立于 UI 进行单元测试。Repositories 也可以通过 Mocking 进行测试。
-   **状态可预测**: 所有状态变化都遵循 `Event -> BLoC -> State` 的单向数据流，易于调试和追踪。