# 当前上下文

**当前焦点:** `HomeScreen` 重构和应用初始化清理。

**任务:**
1.  **清理 `main.dart`:** 移除所有 Riverpod 相关的 Provider 和初始化逻辑，确保 `GetIt` 是唯一的依赖注入源。
2.  **实现 `HomeScreen` BLoC:** 根据设计的伪代码，创建 `HomeBloc`、`HomeState` 和 `HomeEvent`。
3.  **扩展 `ComicRepository`:** 添加获取漫画列表、排序和过滤功能的方法。
4.  **构建 `HomeScreen` UI:** 创建一个新的 `HomeScreen` widget，使用 `BlocProvider` 和 `BlocBuilder` 来连接 `HomeBloc` 并展示漫画列表。
5.  **数据库迁移:** 准备一个新的数据库迁移脚本，用于安全地删除 `ComicProgress` 表。

**后续步骤:**
- 在完成 `HomeScreen` 后，下一步将是审查和重构 `ReaderScreen`，并处理与阅读器相关的数据库表（如 `PageCustomOrder`, `ReadingHistory` 等）。
**Current Focus:** Building the release APK.

**Task:**
1. Execute `flutter build apk --release`.
2. Monitor build status.
3. Report APK path on success.
**Current Focus:** Resolving build errors.

**Problem:**
- The `flutter build apk --release` command failed with numerous compilation errors. The errors point to issues in the BLoC layer, data repositories, and entity definitions. It seems like a major refactoring was left incomplete.

**Next Steps:**
1.  Analyze the error log in detail.
2.  Delegate the fixing of these errors to the `code` mode.
* [2025-08-01 06:41:56Z] - 解决了与 BLoC/存储库重构相关的所有 Dart 编译错误。项目现在可以成功编译，但 Firebase Crashlytics 的网络问题阻止了最终的 APK 组装。