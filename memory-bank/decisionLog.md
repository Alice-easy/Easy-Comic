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
---
### 决策 (架构)
[2025-08-01T16:15:24Z] - **采纳并文档化最终的应用架构**

**决策:**
- 正式采纳并创建了详细的架构蓝图 (`architecture/FINAL_ARCHITECTURE_BLUEPRINT.md`)，该蓝图基于整洁架构（Clean Architecture），并明确了技术选型，包括 BLoC、GetIt、Drift 和 UseCases 模式。

**理由:**
- 随着项目功能的复杂化（如 `specs/comprehensive_feature_spec.pseudo` 所述），团队需要一个统一、权威的架构指南来确保代码质量、可维护性和可扩展性。
- 此前的开发周期中出现了架构不一致和实现混乱的问题，这份最终蓝图旨在解决这些问题，为所有开发者提供一个清晰的“单一事实来源”。
- 确立的架构模式（特别是 Clean Architecture 和 BLoC）是业界处理复杂 Flutter 应用的最佳实践，能够有效隔离关注点，提高可测试性。

**后续行动:**
- 所有未来的功能开发和重构工作都必须遵循 [`architecture/FINAL_ARCHITECTURE_BLUEPRINT.md`](../architecture/FINAL_ARCHITECTURE_BLUEPRINT.md) 中定义的原则和模式。
- 开发团队应首先熟悉该蓝图，再开始具体的编码任务。
- `systemPatterns.md` 已同步更新，以反映这一最终架构决策。
---
### 决策 (部署)
[2025-08-01T18:00:05Z] - **推送到 GitHub**

**决策:**
- 将本地所有已提交的更改推送到远程 GitHub 仓库的 `main` 分支。

**理由:**
- 在完成了大量功能开发、架构重构和文档更新后，需要将这些成果同步到中央代码仓库，以作备份并方便团队协作。

**后续行动:**
- 确认推送成功。
- 更新 `progress.md` 和 `activeContext.md` 以反映部署状态。
---
### Decision (Code)
[2025-08-01 19:03:01] - **书架页面交互功能架构设计决策**

**理由:**
为了实现一流的用户体验，在书架页面的交互功能实现中做出了几个关键设计决策：

**详情:**
1. **状态管理策略**: 扩展BookshelfLoaded状态来包含搜索和排序信息(isSearching, searchQuery, currentSortType, originalComics)，确保搜索和排序状态的持久化和一致性。
2. **搜索体验优化**: 实现了关键词高亮(_HighlightedText组件)和智能的搜索状态管理，包括搜索中指示器和无结果提示。
3. **长按菜单设计**: 使用ModalBottomSheet替代简单对话框，提供更丰富的漫画操作选项（收藏、详情、编辑、删除），改善移动端用户体验。
4. **排序界面**: 设计带有视觉反馈的排序选项(_SortOption组件)，清晰显示当前排序状态和可用选项。

文件路径: `lib/presentation/features/bookshelf/`
---
### Decision (Code)
[2025-08-01T19:35:00Z] - 综合设置系统架构设计与实现

**理由:**
为了提供一流的用户体验，设计并实现了完整的设置管理系统，采用了模块化架构和Clean Architecture原则。主要决策包括：1) 采用BLoC模式进行复杂状态管理；2) 使用卡片式UI布局提高可用性；3) 实现通用的更新模式支持50+设置项；4) 提供完整的导入导出功能。

**详情:**
- **实体扩展**: ReaderSettings从4个属性扩展到50+属性，涵盖阅读、显示、文件、同步、调试等六大类别
- **状态管理**: SettingsBloc包含80+事件类型，采用通用更新模式_updateSetting提高代码复用性
- **UI组件**: 创建8个专业卡片组件(ReadingPreferencesCard, DisplaySettingsCard等)，采用Material Design
- **用户体验**: 实现搜索、导入导出、重置等高级功能，支持实时预览和错误处理
- **架构集成**: 完整的依赖注入配置，遵循Clean Architecture分层原则

文件路径: `lib/domain/entities/reader_settings.dart`, `lib/presentation/features/settings/general/bloc/`, `lib/presentation/features/settings/general/widgets/`
---
### Decision (Code)
[2025-08-01 19:57:00] - 错误处理和用户反馈系统架构设计决策

**理由:**
为了提供一流的用户体验，设计并实现了完整的错误处理和用户反馈机制。主要决策包括：1) 采用分层的错误处理策略（全局、服务、BLoC层）；2) 实现统一的消息服务支持多种反馈类型；3) 集成网络状态监控和智能重试机制；4) 建立标准化的BLoC错误处理模式；5) 创建丰富的加载状态指示器组件。

**详情:**
- **ErrorHandlerService**: 统一错误处理入口，支持分类处理、用户消息显示、重试机制
- **MessageService**: 单例模式消息管理，支持Toast、Dialog、BottomSheet等多种展示方式
- **NetworkService**: 实时网络状态监控，支持连接质量评估和使用建议
- **GlobalErrorHandler**: 全局异常捕获，包括Flutter框架、平台和Isolate错误
- **BaseBloc**: 标准化BLoC错误处理模式，提供统一的错误状态和恢复机制
- **LoadingIndicators**: 完整的加载状态组件库，支持多种样式和进度显示

文件路径: `lib/core/services/`, `lib/presentation/bloc/`, `lib/presentation/widgets/common/`
---
## APK构建部署决策记录 (2025-08-01T21:27)

**部署策略决策**:
1. **多架构APK构建**: 选择生成4个架构的APK文件，确保最大设备兼容性
   - ARM64-v8a (主流现代设备)
   - ARMv7a (较老Android设备)
   - x86_64/x86 (模拟器和x86设备)

2. **Release模式构建**: 使用 `flutter build apk --release` 进行生产级优化
   - 代码混淆和压缩
   - 性能优化
   - 去除调试符号

3. **错误修复策略**: 委托Code模式系统性修复而非逐一修补
   - 效率更高的批量修复
   - 确保架构一致性
   - 减少遗漏风险

4. **构建验证**: 验证所有APK文件大小和完整性
   - 确保构建产物可用性
   - 文件大小合理性检查

**基础设施决策**:
- 使用本地Flutter环境进行构建
- Windows 11开发环境兼容性确认
- Android SDK版本36.0.0支持