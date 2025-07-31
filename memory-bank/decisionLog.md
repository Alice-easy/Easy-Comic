# Decision Log

This file records architectural and implementation decisions using a list format.
2025-07-30 09:09:24 - Log of updates made.

*
      
## Decision

* [2025-07-30 09:19:54] - Configure Gradle to use Aliyun's Maven mirror to resolve network issues during dependency download.
      
## Rationale 

* The initial APK build failed due to network timeouts when fetching dependencies from `storage.googleapis.com` and `firebasecrashlyticssymbols.googleapis.com`. Using a domestic mirror is expected to provide a more stable and faster connection for developers in China.

## Implementation Details

* Added `maven("https://maven.aliyun.com/repository/public")` to the `repositories` block in both `android/build.gradle.kts` and `android/settings.gradle.kts`.
---
### Decision
[2025-07-30 01:39:22Z] - Adopt BLoC pattern for the Reader v2 feature refactor.

**Rationale:**
The existing `ReaderPage` is a large, monolithic `StatefulWidget` that is difficult to maintain and test. The BLoC pattern was chosen to refactor the reader feature because it enforces a strong separation between business logic and the UI layer. This aligns perfectly with the modular structure defined in `reader_optimization_spec.pseudo`. The key benefits include improved testability (logic can be tested independently of the UI), predictable state management (all changes flow through the BLoC), and better code organization, which will make future development and debugging significantly easier.

**Implications/Details:**
*   **New Components**: This requires creating new classes for Events (`ReaderEvent`), States (`ReaderState`), and the BLoC itself (`ReaderBloc`).
*   **Data Layer**: Data access logic will be abstracted into `ComicRepository` and `SettingsRepository`, which the `ReaderBloc` will depend on. This isolates data sources (Drift, SharedPreferences, ComicArchive) from the business logic.
*   **UI Refactoring**: The `ReaderPage` will be converted into a `StatelessWidget` that listens to the `ReaderBloc` via a `BlocBuilder` and dispatches events based on user interaction.
*   **Dependencies**: The `flutter_bloc` package will need to be added to the project.
- [2025-07-30T11:10:41Z] Disabled Crashlytics native symbol upload in `android/app/build.gradle.kts` to work around network issues during APK build.
---
### Decision
[2025-07-30 13:05:26Z] - 采用分层架构实现手动页面排序功能，通过扩展 Drift 数据库、更新 Repository 和 BLoC 来支持该功能。

**Rationale:**
为了实现手动页面排序并保持代码的整洁性和可维护性，需要一个能够持久化用户偏好的解决方案。直接在现有架构上进行扩展是最自然的选择：
1.  **数据持久化**: 使用 Drift 数据库可以确保排序顺序在应用会话之间得以保留，符合用户的期望。
2.  **关注点分离**: 将数据库逻辑封装在 `ComicRepository` 中，使得 `ReaderBloc` 无需关心数据存储的具体实现，这遵循了项目已建立的 Repository 模式。
3.  **可测试性**: 业务逻辑（如加载和重新排序）保留在 `ReaderBloc` 中，可以独立于UI和数据层进行单元测试。
4.  **模块化UI**: 创建一个专门的 `ReorderPage` 页面来处理排序交互，可以使 `ReaderPage` 保持简洁，专注于阅读体验本身。

**Implications/Details:**
*   **数据库 (`drift_db.dart`)**: 需要新增一个 `PageOrders` 表，包含 `comicId` 和 `order` 字段。
*   **数据仓库 (`comic_repository.dart`)**: 需要添加 `savePageOrder` 和 `getPageOrder` 方法。
*   **BLoC (`reader_bloc.dart`)**:
    *   `LoadComicEvent` 的处理逻辑需要更新，以在加载时检查并应用手动排序。
    *   需要添加新的事件 `UpdatePageOrder` 和 `ResetPageOrder` 及其处理逻辑。
*   **UI**:
    *   需要创建一个新的 `ReorderPage`，使用 `ReorderableListView` 或类似组件。
    *   `ReaderPage` 中需要添加一个入口点来导航到 `ReorderPage`。