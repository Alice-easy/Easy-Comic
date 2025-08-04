# System Patterns *Optional*

This file documents recurring patterns and standards used in the project.
It is optional, but recommended to be updated as the project evolves.
2025-07-30 09:09:44 - Log of updates made.

*

## Coding Patterns

*   

## Architectural Patterns

*   

## Testing Patterns

*
---
### Architectural Patterns
[2025-07-30 01:39:22Z] - **BLoC (Business Logic Component)**
*   **Description**: BLoC is used to separate business logic from the UI. It takes a stream of events, processes them, and outputs a stream of states. For the reader feature, this means the UI dispatches events like `PageSwiped` or `ZoomChanged`, and the `ReaderBloc` handles the logic and emits a new `ReaderState` that the UI rebuilds against.
*   **Rationale**: Chosen for its clear separation of concerns, testability, and predictable state management, which is ideal for complex, interactive UIs like the comic reader. It directly maps to the modular design outlined in the `reader_optimization_spec.pseudo`.
*   **Implementation**:
    *   **Events**: Plain Dart classes representing user actions (e.g., `LoadComic`, `BookmarkButtonPressed`).
    *   **States**: Immutable classes representing the UI state (e.g., `ReaderState` with properties like `currentPage`, `isLoading`, `bookmarks`).
    *   **Bloc**: The central component that transforms events into states and interacts with data repositories.
    *   **UI**: `StatelessWidget`s that use `BlocBuilder` to react to state changes and `context.read<ReaderBloc>().add()` to dispatch events.
---
### Architectural Patterns
[2025-07-30T04:08:12Z] - **Reader v2 Architecture (BLoC with Repository)**
*   **Description**: The new reader architecture solidifies the use of the BLoC pattern combined with the Repository pattern, creating a clear three-tier structure: UI, Business Logic (BLoC), and Data. The UI layer is composed of stateless widgets that react to state changes from the BLoC. The BLoC contains all business logic, processing events from the UI and interacting with repositories. The Data layer, consisting of repositories (`ComicRepository`, `SettingsRepository`), abstracts data sources (Drift, SharedPreferences, file system), ensuring the BLoC is decoupled from data implementation details.
*   **Rationale**: This layered approach, detailed in [`architecture/reader_v2_architecture.md`](../architecture/reader_v2_architecture.md), enhances modularity, testability, and maintainability. It provides a robust and scalable foundation for the complex interactive features of the comic reader, directly implementing the `reader_optimization_spec.pseudo`.
*   **Implementation**:
    *   **UI**: Widgets like `ReaderScreen` listen to the `ReaderBloc` and dispatch events.
    *   **BLoC**: `ReaderBloc` manages `ReaderState` and orchestrates data flow by calling repository methods.
    *   **Repositories**: `ComicRepository` and `SettingsRepository` provide clean APIs for data access, hiding the underlying data sources.
---
### Caching Patterns
[2025-07-31T19:06:32Z] - **Multi-Level Intelligent Caching**
*   **Description**: A sophisticated three-tier caching system: Memory Cache (LRU-based for immediate access), Disk Cache (persistent storage for frequently accessed images), and Preload Queue (predictive loading based on reading patterns). The system includes memory pressure monitoring that triggers adaptive cache cleanup at 80%, 90%, and 95% memory thresholds.
*   **Rationale**: Comic readers require instant image loading for smooth reading experience while managing memory efficiently on mobile devices. The multi-level approach balances performance with resource constraints.
*   **Implementation**:
    *   **Memory Cache**: LRU algorithm with configurable size limits
    *   **Disk Cache**: Persistent storage with intelligent cleanup policies
    *   **Preload Queue**: Priority-based preloading (next 3 pages high priority, next 5 pages medium priority)
    *   **Memory Monitoring**: Adaptive strategies based on system memory pressure

---
### Performance Patterns
[2025-07-31T19:06:32Z] - **Lazy Loading with Viewport Optimization**
*   **Description**: Images are loaded only when approaching the viewport, with different loading strategies for different reading modes (single page, continuous scroll, double page). Includes image size optimization and progressive loading for large images.
*   **Rationale**: Reduces initial loading time and memory usage while maintaining smooth user experience across different reading patterns.
*   **Implementation**:
    *   **Viewport Detection**: Load images 1-2 pages ahead of current position
    *   **Progressive Loading**: Show low-res placeholder while high-res loads
    *   **Reading Mode Adaptation**: Different preload strategies for each mode
    *   **Image Optimization**: Automatic resizing based on device capabilities

---
### Coding Patterns
[2025-07-31T19:06:32Z] - **Event-Driven State Management**
*   **Description**: All user interactions are modeled as events (PageSwipeEvent, ZoomChangeEvent, BookmarkToggleEvent) that flow through the BLoC, ensuring predictable state transitions and enabling comprehensive logging and analytics.
*   **Rationale**: Provides clear debugging capabilities, enables undo/redo functionality, and creates a single source of truth for all state changes.
*   **Implementation**:
    *   **Event Classes**: Immutable objects representing user actions
    *   **State Classes**: Immutable state objects with copyWith methods
    *   **Event Handlers**: Pure functions that transform events into state changes
    *   **Side Effects**: Handled through separate services called by BLoC
---
### Architectural Patterns
[2025-07-31T20:57:10Z] - **Clean Architecture (DDD-inspired)**
*   **Description**: A formal three-tier architecture composed of Presentation, Domain, and Data layers, supplemented by a Core/Services layer. This pattern enforces a strict dependency rule where all dependencies point inwards, with the Domain layer at the core, completely independent of any other layer.
    *   **Presentation Layer**: Contains UI (Widgets) and state management (BLoC). It depends only on the Domain layer.
    *   **Domain Layer**: Contains business logic (Entities, Repository Interfaces, UseCases). It has no dependencies on other layers.
    *   **Data Layer**: Contains data sources (local/remote) and repository implementations. It depends on the Domain layer to implement its interfaces.
    *   **Core/Services Layer**: Provides shared, low-level functionalities (e.g., caching, logging, navigation) and can be accessed by other layers.
*   **Rationale**: This pattern was chosen for its exceptional separation of concerns, which leads to highly modular, testable, and maintainable code. It protects the core business logic from changes in frameworks and infrastructure, ensuring the application's longevity and adaptability. It provides a clear roadmap for developers, making the codebase easier to navigate and scale.
*   **Implementation**:
    *   **Directory Structure**: The `lib` directory is organized into `presentation`, `domain`, `data`, and `core` folders, directly reflecting the architectural layers.
    *   **Dependency Injection**: `GetIt` is used as a service locator to decouple classes and manage dependencies across layers, configured in a central `injection_container.dart`.
    *   **State Management**: The `flutter_bloc` library is used within the Presentation layer to manage state, responding to user events and interacting with the Domain layer (typically via UseCases or Repositories).

---
### Architectural Patterns
[2025-08-01T15:41:06Z] - **Clean Architecture with BLoC and UseCases**
*   **Description**: The application architecture is formally defined as a Clean Architecture variant. It promotes a clear separation of concerns into three primary layers: Presentation, Domain, and Data. The Presentation layer uses the BLoC pattern for state management. The Domain layer introduces UseCases (Interactors) to encapsulate specific business logic, which are called by the BLoCs. The Data layer implements repository interfaces defined in the Domain layer.
*   **Rationale**: This structured approach enhances modularity, testability, and maintainability. UseCases provide a finer-grained control over business logic, preventing BLoCs from becoming bloated and ensuring that each component has a single, well-defined responsibility. This pattern is ideal for the complexity outlined in `specs/flutter_comic_reader_comprehensive_spec.md`.
*   **Implementation**:
    *   **Data Flow**: UI (Widget) -> BLoC -> UseCase -> Repository -> DataSource.
    *   **Directory Structure**: The `lib` directory is physically separated into `presentation`, `domain`, and `data` folders.
    *   **Dependencies**: All dependencies point inwards, towards the `domain` layer.

---
### Dependency Injection Patterns
[2025-08-01T15:41:06Z] - **Service Locator with GetIt**
*   **Description**: Dependency Injection is managed using the `get_it` package, which acts as a Service Locator. All dependencies (Repositories, UseCases, BLoCs, DataSources, external services like `SharedPreferences`) are registered in a centralized `injection_container.dart` file at application startup.
*   **Rationale**: Using `get_it` provides a fast, simple, and flexible way to decouple classes across all layers of the application. It simplifies testing by allowing mock dependencies to be easily injected in test environments.
*   **Implementation**:
    *   **Registration**: Dependencies are registered with different lifecycles: `registerLazySingleton` for single instances (Repositories, UseCases), and `registerFactory` for new instances each time (BLoCs).
    *   **Access**: Dependencies are retrieved in code by calling `sl<MyType>()`.
    *   **Initialization**: All dependencies are registered by calling a single `init()` function from `main.dart` before the app runs.
---
### Architectural Patterns
[2025-08-01T16:15:03Z] - **最终确定的整洁架构 (Finalized Clean Architecture)**
*   **描述**: 正式确立应用的核心架构为整洁架构（Clean Architecture），严格划分表示层（Presentation）、领域层（Domain）和数据层（Data）。
    *   **表示层**: 使用 BLoC 进行状态管理，负责 UI 渲染和用户交互。
    *   **领域层**: 包含业务实体（Entities）、用例（UseCases）和仓库接口（Repository Interfaces），是应用的核心，独立于任何框架。
    *   **数据层**: 通过实现仓库接口来提供数据，管理数据源（Drift 数据库、WebDAV、SharedPreferences）。
*   **理由**: 此架构提供了极致的关注点分离，最大化了代码的可测试性、可维护性和长期可扩展性。它将核心业务逻辑与外部依赖完全隔离，能够灵活应对未来的技术和需求变化。该设计是对 `specs/comprehensive_feature_spec.pseudo` 中所有功能需求的直接响应。
*   **实现**:
    *   **权威文档**: 完整的架构定义、组件职责和数据流图见 [`architecture/FINAL_ARCHITECTURE_BLUEPRINT.md`](../architecture/FINAL_ARCHITECTURE_BLUEPRINT.md)。
    *   **依赖注入**: 使用 `GetIt` 作为服务定位器，在 `injection_container.dart` 中统一管理所有依赖。
    *   **数据流**: 严格遵循 UI -> BLoC -> UseCase -> Repository 的单向数据流。
---
### Architectural Patterns
[2025-08-02T01:14:55Z] - **复合 BLoC 模式 (Composite BLoC Pattern)**
*   **描述**: 在复杂的 UI 界面（如设置屏幕）中，使用一个主 Widget（`SettingsScreen`）通过 `MultiBlocProvider` 来提供和管理多个独立的、功能特定的子 BLoC（如 `SettingsBloc`, `WebDavBloc`）。每个子 BLoC 只负责其自己的功能领域，而 UI 则由多个独立的子 Widget 组成，每个子 Widget 只监听其所需的 BLoC。
*   **理由**: 这种模式可以有效分解复杂性。它避免了创建一个巨大而臃肿的“上帝 BLoC”，而是将状态管理逻辑分散到多个更小、更易于维护的 BLoC 中。这提高了模块化程度、可测试性和代码的可读性。
*   **实现**:
    *   **UI**: 使用 `MultiBlocProvider` 在顶层提供所有需要的 BLoC。子 Widget 使用 `BlocBuilder` 或 `context.watch<MyBloc>()` 来监听特定 BLoC 的状态。
    *   **BLoCs**: 每个 BLoC 都是独立的，并处理自己的一组事件和状态。

---
### Architectural Patterns
[2025-08-02T01:14:55Z] - **协调器服务模式 (Coordinator Service Pattern)**
*   **描述**: 定义一个高级服务（“协调器”），其主要职责不是执行底层的 I/O 或业务计算，而是编排和协调多个其他低级服务和仓库来完成一个复杂的、多步骤的业务流程。`SyncEngine` 就是这个模式的一个例子。
*   **理由**: 对于像数据同步这样涉及多个数据源和多个操作步骤的复杂流程，将编排逻辑封装在一个专门的协调器服务中，可以使流程更清晰、更易于管理和测试。它将高层的“做什么”（同步）与低层的“怎么做”（API 调用、数据库读写）分离开来。
*   **实现**:
    *   **协调器 (`SyncEngine`)**: 依赖于多个低级服务/仓库的接口。其公共方法（如 `performSync()`）定义了业务流程的各个步骤。
    *   **调用者 (`WebDavBloc`)**: 只需调用协调器的一个方法，而无需关心其内部复杂的实现细节。
---
### Architectural Patterns
[2025-08-03T13:10:51.036Z] - **Refactor Clean Architecture**
*   **Description**: The refactoring effort formalizes the application's structure based on a strict Clean Architecture. This involves three distinct layers (Presentation, Domain, Data) with a unidirectional dependency flow towards the Domain layer. BLoC is used for state management in the Presentation layer, and UseCases in the Domain layer encapsulate specific business logic.
*   **Rationale**: This pattern is chosen to maximize modularity, testability, and maintainability, providing a robust foundation for all features outlined in the `specs/refactor_spec.pseudo`. It ensures that the core business logic is independent of UI and data source implementations.
*   **Implementation**:
    *   **Directory Structure**: `lib/` is organized into `presentation`, `domain`, `data`, and `core`.
    *   **Dependency Injection**: `GetIt` is used as a service locator, configured in `injection_container.dart`.
    *   **Data Flow**: UI (Widget) -> BLoC -> UseCase -> Repository Interface -> Repository Implementation -> DataSource.
    *   **Documentation**: The complete architecture is detailed in [`architecture/refactor_architecture.md`](../architecture/refactor_architecture.md).