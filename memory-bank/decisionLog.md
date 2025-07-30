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