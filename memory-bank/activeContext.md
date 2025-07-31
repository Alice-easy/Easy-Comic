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