# 决策日志

## 2025-07-31

**决策:**
- **确认重构方向:** 审查确认了应用从 Riverpod 到 GetIt + BLoC 的迁移仍在进行中。`main.dart` 需要彻底清理 Riverpod 的残留代码。
- **规划 `HomeScreen` 重构:** 确定了 `HomeScreen` 的核心功能（漫画网格、排序、收藏），并设计了相应的 BLoC 结构（Events, States, Bloc）。
- **识别数据库冗余:** 发现 `drift_db.dart` 中的 `ComicProgress` 表已过时，其功能被 `Comics` 表中的 `progress` 字段取代，计划将其移除。

**理由:**
- `main.dart` 中混合使用 Riverpod 和 GetIt 会导致依赖管理混乱和潜在的运行时错误。统一使用 GetIt 是架构迁移的关键步骤。
- 旧的 `home_page.dart` 已被删除，必须通过分析数据模型来重新构建 `HomeScreen` 的功能，BLoC 是实现响应式 UI 和分离业务逻辑的理想模式。
- 移除冗余的数据库表可以简化数据模型，减少维护成本，并消除潜在的数据不一致问题。

**后续行动:**
- 执行伪代码计划，重构 `main.dart`。
- 扩展 `ComicRepository` 接口以支持 `HomeScreen` 的功能需求。
- 实现 `HomeBloc` 和新的 `HomeScreen` UI。
- 在数据库迁移中移除 `ComicProgress` 表。