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