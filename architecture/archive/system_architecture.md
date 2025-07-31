# Flutter 漫画阅读器 - 系统架构设计

**版本**: 2.0
**日期**: 2025-07-31
**作者**: 架构师

## 1. 概述

本文档基于 `specs/flutter_comic_reader_functional_spec.md` 的功能需求，并结合项目已采纳的 `BLoC + Repository + Service` 架构决策，为 Flutter 漫画阅读器提供一个统一、清晰且可扩展的系统架构。

该架构旨在实现以下目标：
- **高内聚、低耦合**: 各层级和模块职责分明。
- **可测试性**: UI、业务逻辑和数据层可独立测试。
- **可维护性**: 清晰的结构便于理解、修改和扩展。
- **性能**: 保证流畅的用户体验，特别是在图像加载和渲染方面。

## 2. 核心架构：分层设计

我们将遵循清晰的三层架构，并辅以一个核心服务层。

```mermaid
graph TD
    A[表现层 (Presentation)] --> B[领域层 (Domain)];
    B --> C[数据层 (Data)];
    A --> D[核心服务层 (Core/Services)];
    B --> D;
    C --> D;

    subgraph 表现层 (Presentation)
        direction LR
        A1[视图 (Views/Pages)]
        A2[状态管理 (BLoC)]
    end

    subgraph 领域层 (Domain)
        direction LR
        B1[实体 (Entities)]
        B2[仓库接口 (Repository Interfaces)]
        B3[用例 (UseCases - 可选)]
    end

    subgraph 数据层 (Data)
        direction LR
        C1[仓库实现 (Repository Implementations)]
        C2[数据源 (DataSources)]
        C3[数据传输对象 (DTOs - 可选)]
    end

    subgraph 核心服务层 (Core/Services)
        direction LR
        D1[ArchiveService]
        D2[CacheService]
        D3[SettingsService]
        D4[NavigationService]
    end
```

### 2.1. 表现层 (Presentation)

- **职责**: 处理用户界面和用户交互。
- **技术栈**: Flutter Widgets, `flutter_bloc`。
- **结构**:
    - **`pages` / `features`**: 每个主要功能（如书架、阅读器、设置）都是一个独立的模块。
    - **`widgets`**: 可复用的 UI 组件。
    - **`bloc`**: 包含 `Bloc`、`Event` 和 `State` 文件，负责管理页面的状态。

### 2.2. 领域层 (Domain)

- **职责**: 包含核心业务逻辑和业务对象，不依赖任何具体框架。
- **结构**:
    - **`entities`**: 定义核心业务对象（如 `Comic`, `ComicPage`, `Bookmark`）。这些是纯 Dart 对象。
    - **`repositories`**: 定义数据仓库的抽象接口（如 `IComicRepository`, `ISettingsRepository`）。表现层和数据层都依赖于这些接口。
    - **`usecases`**: (可选) 封装复杂或可复用的业务逻辑。例如，`GetComicDetailsUseCase` 可以组合来自多个仓库的数据。

### 2.3. 数据层 (Data)

- **职责**: 数据的获取、存储和管理。
- **结构**:
    - **`repositories`**: `Repository` 接口的具体实现。它们负责协调一个或多个数据源。
    - **`datasources`**:
        - **`local`**: 本地数据源，如 `Drift` (SQLite), `SharedPreferences`, 文件系统。
        - **`remote`**: (如果需要) 远程数据源，如与 WebDAV 或其他 API 交互的客户端。
    - **`models` / `dtos`**: (可选) 用于数据解析的数据模型，特别是与外部 API 交互时，可以将网络数据模型与领域实体分开。

### 2.4. 核心服务层 (Core/Services)

- **职责**: 提供跨应用的核心、底层功能。
- **结构**:
    - **`services`**: 封装具体功能的类，如 `ArchiveService` (处理 .cbz/.zip), `CacheService` (图像缓存), `SettingsService` (应用设置)。
    - **`utils`**: 通用工具函数。
    - **`constants`**: 应用范围内的常量。

## 3. 目录结构

根据上述分层设计，项目将采用以下目录结构：

```
lib/
├── app.dart                 # App根组件，配置主题、路由等
├── main.dart                # 应用入口
│
├── core/                    # 核心服务与工具
│   ├── services/
│   │   ├── archive_service.dart
│   │   ├── cache_service.dart
│   │   ├── settings_service.dart
│   │   └── navigation_service.dart
│   ├── utils/
│   │   └── ...
│   └── constants/
│       └── ...
│
├── data/                    # 数据层
│   ├── datasources/
│   │   ├── local/
│   │   │   ├── comic_local_datasource.dart
│   │   │   └── settings_local_datasource.dart
│   │   └── remote/
│   │       └── webdav_client.dart # 示例
│   ├── repositories/
│   │   ├── comic_repository_impl.dart
│   │   └── settings_repository_impl.dart
│   └── models/              # 数据传输对象 (可选)
│       └── comic_dto.dart
│
├── domain/                  # 领域层
│   ├── entities/
│   │   ├── comic.dart
│   │   ├── comic_page.dart
│   │   └── bookmark.dart
│   ├── repositories/
│   │   ├── comic_repository.dart
│   │   └── settings_repository.dart
│   └── usecases/            # (可选)
│       └── get_comic_details.dart
│
├── presentation/            # 表现层
│   ├── features/
│   │   ├── reader/
│   │   │   ├── bloc/
│   │   │   │   ├── reader_bloc.dart
│   │   │   │   ├── reader_event.dart
│   │   │   │   └── reader_state.dart
│   │   │   ├── view/
│   │   │   │   └── reader_page.dart
│   │   │   └── widgets/
│   │   │       ├── reader_app_bar.dart
│   │   │       └── reader_bottom_bar.dart
│   │   └── library/
│   │       └── ...
│   └── widgets/             # 全局可复用Widgets
│       └── loading_indicator.dart
│
└── injection_container.dart # 依赖注入配置 (GetIt)
```

## 4. 状态管理 (BLoC)

- **全局状态**: 对于像用户设置、主题这样的全局状态，可以通过一个 `SettingsBloc` 来管理，并在应用顶层注入。
- **页面/功能状态**: 每个复杂页面或功能（如阅读器）将拥有自己的 `Bloc`（如 `ReaderBloc`）。这个 `Bloc` 的生命周期与页面绑定。
- **UI 状态**: 简单的、临时的 UI 状态（如动画控制、控件可见性）可以直接在 `StatefulWidget` 中管理，或者为了保持一致性，也可以通过 `Bloc` 的状态属性来控制。

## 5. 依赖注入 (GetIt)

我们将使用 `get_it` 配合 `injectable` 来实现依赖注入，以解耦各个组件。

- **`injection_container.dart`**: 这是配置所有依赖项的中心文件。
- **注入方式**:
    - **Singleton**: 对于像 `SettingsService` 这样的全局单例服务。
    - **Lazy Singleton**: 默认的单例模式，在第一次使用时才初始化。
    - **Factory**: 对于像 `ReaderBloc` 这样每次需要新实例的组件。

**示例 (`injection_container.dart`)**:
```dart
final sl = GetIt.instance;

Future<void> init() async {
  // Blocs
  sl.registerFactory(() => ReaderBloc(comicRepository: sl(), settingsRepository: sl()));

  // UseCases (可选)

  // Repositories
  sl.registerLazySingleton<IComicRepository>(() => ComicRepositoryImpl(localDataSource: sl(), archiveService: sl()));
  sl.registerLazySingleton<ISettingsRepository>(() => SettingsRepositoryImpl(localDataSource: sl()));

  // DataSources
  sl.registerLazySingleton<IComicLocalDataSource>(() => ComicLocalDataSourceImpl(database: sl()));
  // ...

  // Core Services
  sl.registerLazySingleton(() => ArchiveService());
  sl.registerLazySingleton(() => CacheService());
}
```

## 6. 总结

这个架构为构建一个健壮、可维护的漫画阅读器应用提供了坚实的基础。它通过明确的分层和关注点分离，使得团队能够高效协作，并轻松地适应未来的需求变化。