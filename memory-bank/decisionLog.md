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
## 2025-08-01

**Decision:**
- Initiate release APK build using `flutter build apk --release`.

**Rationale:**
- A new release build is required as per the user's request.

**Follow-up Actions:**
- Monitor the build process for success or failure.
- Report the location of the generated APK upon successful completion.
---
### 决策 (代码)
[2025-08-01 06:42:29Z] - 系统地修复了由于 BLoC/存储库重构而导致的大量编译错误。

**理由:**
为了使项目恢复到可编译状态，必须解决实体、存储库接口、服务实现和 BLoC 之间的不一致。

**详情:**
*   统一了 `Comic.id` 为 `String` 类型。
*   更新了所有存储库接口以匹配 BLoC 中的调用。
*   实现了缺失的方法存根并修复了服务和存储库实现中的方法签名。
*   解决了 `AutoPageEvent` 的不一致用法。
*   修复了依赖注入容器中的多个问题。
*   修复了 `CacheFailure` 构造函数的不正确调用。