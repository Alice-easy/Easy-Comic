# 🤝 Easy Comic 贡献指南

感谢您对 Easy Comic 项目的关注！我们欢迎各种形式的贡献，包括但不限于 Bug 报告、功能建议、代码提交和文档改进。

## 🌟 贡献方式

### 🐛 报告 Bug

1. 在提交 Issue 前，请先搜索是否已有相似的问题
2. 使用我们提供的 Bug 报告模板
3. 提供详细的复现步骤和环境信息
4. 如果可能，请附上相关的错误日志

### 💡 功能建议

1. 在 GitHub Discussions 中讨论您的想法
2. 描述功能的使用场景和预期效果
3. 考虑功能对现有架构的影响
4. 等待维护者和社区的反馈

### 🔧 代码贡献

1. Fork 本仓库到您的 GitHub 账户
2. 创建一个新的功能分支 (`git checkout -b feature/amazing-feature`)
3. 进行您的修改
4. 提交更改 (`git commit -m 'Add some amazing feature'`)
5. 推送到分支 (`git push origin feature/amazing-feature`)
6. 创建 Pull Request

## 📋 开发环境设置

### 环境要求

- **Android Studio**: Hedgehog | 2023.1.1+
- **JDK**: 17 (建议使用 Temurin/AdoptOpenJDK)
- **Android SDK**: API 24+ (Android 7.0+)
- **Build Tools**: 34.0.0+
- **Target SDK**: 35 (Android 15)

### 克隆和构建

```bash
# 1. 克隆仓库
git clone https://github.com/Alice-easy/Easy-Comic.git
cd Easy-Comic

# 2. 检查环境
./gradlew --version
./gradlew clean

# 3. 构建Debug版本
./gradlew assembleDebug

# 4. 运行测试
./gradlew testDebugUnitTest
```

## 🏗️ 代码规范

### 架构原则

- **严格遵循 Clean Architecture**: 保持层次分离，避免跨层直接依赖
- **SOLID 原则**: 单一职责、开闭原则、依赖倒置等
- **响应式编程**: 使用 Kotlin Flow 进行数据流管理
- **依赖注入**: 使用 Koin 进行依赖管理

### 代码风格

- **Kotlin 代码规范**: 遵循[Kotlin 官方代码风格指南](https://kotlinlang.org/docs/coding-conventions.html)
- **命名约定**: 使用有意义的变量名和函数名
- **文档注释**: 为公共 API 添加 KDoc 注释
- **代码格式**: 使用 Android Studio 默认格式化设置

### 示例代码结构

```kotlin
/**
 * 示例UseCase类，展示正确的代码结构
 *
 * @param repository 数据仓库接口
 */
class GetMangaListUseCase(
    private val repository: MangaRepository
) : BaseUseCase<GetMangaListUseCase.Params, List<Manga>>() {

    override suspend fun execute(params: Params): List<Manga> {
        return repository.getMangaList(
            query = params.query,
            sortBy = params.sortBy
        )
    }

    data class Params(
        val query: String = "",
        val sortBy: SortOrder = SortOrder.NAME
    )
}
```

## 🧪 测试要求

### 测试覆盖率

- **目标覆盖率**: 90%+ 单元测试覆盖率
- **必须测试**: 新增的 UseCase、Repository 实现、ViewModel
- **推荐测试**: UI 组件、工具类、扩展函数

### 测试框架

- **单元测试**: JUnit + MockK + Truth
- **协程测试**: Turbine + Coroutines Test
- **UI 测试**: Compose Test + Robolectric

### 测试示例

```kotlin
@Test
fun `getMangaList should return sorted manga list when query is provided`() = runTest {
    // Given
    val mockRepository = mockk<MangaRepository>()
    val expectedMangaList = listOf(/* test data */)
    coEvery { mockRepository.getMangaList(any(), any()) } returns expectedMangaList

    val useCase = GetMangaListUseCase(mockRepository)
    val params = GetMangaListUseCase.Params(
        query = "test query",
        sortBy = SortOrder.NAME
    )

    // When
    val result = useCase.execute(params)

    // Then
    assertThat(result).isEqualTo(expectedMangaList)
    coVerify { mockRepository.getMangaList("test query", SortOrder.NAME) }
}
```

## 📊 性能要求

### 性能基准

- **启动时间**: < 1500ms (目标: < 200ms)
- **翻页响应**: < 80ms (目标: < 50ms)
- **搜索响应**: < 300ms (目标: < 200ms)
- **内存使用**: < 120MB (目标: < 100MB)

### 性能测试

- 所有性能相关的修改必须通过基准测试验证
- 不允许任何性能回归
- 使用项目内置的 PerformanceTracker 进行监控

## 📝 文档要求

### 代码文档

- **KDoc 注释**: 为所有公共类、函数和属性添加文档
- **复杂逻辑**: 为复杂的业务逻辑添加详细注释
- **TODO 注释**: 使用 TODO 标记需要后续完善的代码

### 提交信息

```
type(scope): description

[optional body]

[optional footer]
```

**类型 (type):**

- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 文档更新
- `style`: 代码格式化
- `refactor`: 重构
- `test`: 添加测试
- `chore`: 构建工具或辅助工具的变动

**示例:**

```
feat(reader): add page zoom functionality

Implement double-tap zoom and pinch-to-zoom gestures
for better reading experience.

Closes #123
```

## 🔍 Pull Request 流程

### PR 检查清单

- [ ] 代码遵循项目规范
- [ ] 所有测试通过
- [ ] 测试覆盖率不低于 90%
- [ ] 性能基准测试通过
- [ ] 文档已更新（如需要）
- [ ] CHANGELOG.md 已更新（如需要）

### PR 模板

```markdown
## 🎯 变更描述

<!-- 描述此PR解决的问题或添加的功能 -->

## 🔗 相关 Issue

<!-- 引用相关的Issue，例如：Closes #123 -->

## 🧪 测试

<!-- 描述测试策略和测试结果 -->

- [ ] 单元测试
- [ ] 集成测试
- [ ] 手动测试

## 📊 性能影响

<!-- 描述对性能的影响，如有性能测试结果请附上 -->

## 📱 测试设备

<!-- 列出测试过的设备和Android版本 -->

## 📸 截图

<!-- 如果是UI相关变更，请提供前后对比截图 -->

## ✅ 检查清单

- [ ] 我已阅读并遵循贡献指南
- [ ] 代码遵循项目规范
- [ ] 所有测试通过
- [ ] 文档已更新
```

## 🚀 发布流程

### 版本号规范

遵循[语义化版本](https://semver.org/lang/zh-CN/)规范：

- **MAJOR**: 不兼容的 API 修改
- **MINOR**: 向下兼容的功能性新增
- **PATCH**: 向下兼容的问题修正

### 发布步骤

1. 更新版本号
2. 更新 CHANGELOG.md
3. 创建 Release PR
4. 合并后自动触发发布流程

## 🤔 获取帮助

### 联系方式

- **GitHub Issues**: 用于 Bug 报告和功能请求
- **GitHub Discussions**: 用于一般性讨论和问题咨询
- **项目邮箱**: easy@ea.cloudns.ch

### 常见问题

**Q: 如何设置开发环境？**
A: 请参考 README.md 中的快速开始部分。

**Q: 测试覆盖率不达标怎么办？**
A: 为新增代码添加充分的单元测试，使用`./gradlew jacocoTestReport`查看覆盖率报告。

**Q: 如何添加新的文件格式支持？**
A: 实现 ComicParser 接口，并在 ComicParserFactory 中注册新的解析器。

## 📜 行为准则

### 我们的承诺

为了营造一个开放友好的环境，我们承诺：

- 使用友好和包容的语言
- 尊重不同的观点和经验
- 优雅地接受建设性批评
- 专注于对社区最有利的事情
- 与社区成员保持友善

### 不可接受的行为

- 发布他人的私人信息
- 使用性暗示的言论或图像
- 恶意攻击、侮辱或人身攻击
- 公开或私下骚扰
- 其他在专业环境中不合适的行为

## 🎉 致谢

感谢所有为 Easy Comic 项目做出贡献的开发者！您的每一个贡献都让这个项目变得更好。

---

**开始贡献之前，请确保您已阅读并理解本指南的所有内容。如有疑问，随时通过 Issue 或 Discussion 与我们联系！**

_最后更新: 2024 年 12 月 19 日_
