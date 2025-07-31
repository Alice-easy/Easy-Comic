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