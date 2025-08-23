# 代码质量检查工具指南

Easy Comic 项目集成了完整的代码质量检查工具链，确保代码质量、安全性和一致性。

## 🛠️ 集成的工具

### 1. Detekt - 静态代码分析

**用途**: Kotlin 静态代码分析，检测代码异味、复杂度、潜在错误等

**运行命令**:

```bash
# 运行Detekt分析
./gradlew detekt

# 查看报告
open app/build/reports/detekt/detekt.html
```

**配置文件**: `detekt.yml`

- 自定义规则配置
- 复杂度阈值设置
- 代码风格检查
- 性能优化建议

### 2. ktlint - 代码格式化

**用途**: Kotlin 代码格式标准化，确保代码风格一致性

**运行命令**:

```bash
# 检查代码格式
./gradlew ktlintCheck

# 自动修复格式问题
./gradlew ktlintFormat

# 或者使用自定义任务
./gradlew formatCode
```

**配置文件**: `.editorconfig`

- 缩进和换行规则
- 最大行长度: 120 字符
- Android 项目特定设置
- Compose 函数命名规则

### 3. OWASP 依赖安全扫描

**用途**: 检测第三方依赖的已知安全漏洞

**运行命令**:

```bash
# 运行依赖安全扫描
./gradlew dependencyCheckAnalyze

# 查看报告
open build/reports/dependency-check-report.html
```

**配置文件**: `config/dependency-check-suppressions.xml`

- 误报抑制配置
- 已知安全问题的例外处理
- CVE 白名单管理

### 4. Gradle 依赖更新检查

**用途**: 检查过期的依赖版本

**运行命令**:

```bash
# 检查依赖更新
./gradlew dependencyUpdates

# 查看报告
open build/reports/dependencyUpdates/report.html
```

### 5. SonarQube (可选)

**用途**: 综合代码质量分析平台

**配置**: 在根目录 `build.gradle.kts` 中已预配置，需要 SonarQube 服务器

## 🚀 快速开始

### 运行所有代码质量检查

```bash
# 运行完整的代码质量检查套件
./gradlew codeQualityCheck
```

这个命令会依次执行：

- ✅ Detekt 静态分析
- ✅ ktlint 代码格式检查
- ✅ 依赖安全扫描
- ✅ Jacoco 测试覆盖率
- ✅ Android Lint 检查

### 修复代码格式问题

```bash
# 自动修复代码格式
./gradlew formatCode
```

### 提交前检查

```bash
# 模拟pre-commit检查
./gradlew preCommitCheck
```

## 📋 Pre-commit Hooks

项目支持 [pre-commit](https://pre-commit.com/) 钩子，在提交前自动运行质量检查。

### 安装 pre-commit

```bash
# 安装pre-commit工具
pip install pre-commit

# 安装项目钩子
pre-commit install

# 手动运行所有钩子
pre-commit run --all-files
```

### 钩子配置

配置文件: `.pre-commit-config.yaml`

**提交时检查**:

- ktlint 代码格式
- Detekt 静态分析
- Android Lint
- TODO/FIXME 注释检查
- 调试代码检查
- 硬编码字符串检查

**推送时检查**:

- 单元测试
- 依赖安全扫描

## 📊 质量指标

### 代码覆盖率目标

- **单元测试覆盖率**: ≥ 90%
- **分支覆盖率**: ≥ 85%
- **行覆盖率**: ≥ 90%

### 代码质量标准

- **Detekt 问题**: 0 个严重问题
- **ktlint 违规**: 0 个格式问题
- **Android Lint**: 0 个错误，≤ 5 个警告
- **安全漏洞**: 0 个高危和严重漏洞

### 复杂度限制

- **函数复杂度**: ≤ 15
- **类复杂度**: ≤ 600
- **方法行数**: ≤ 60
- **参数个数**: ≤ 6

## 🔧 IDE 集成

### Android Studio 设置

1. **ktlint 插件**:

   - 安装 "ktlint" 插件
   - 配置自动格式化快捷键

2. **Detekt 插件**:

   - 安装 "Detekt" 插件
   - 配置实时检查

3. **代码风格**:
   - 导入项目的 `.editorconfig` 设置
   - 设置行长度为 120 字符

### VS Code 设置

```json
{
  "kotlin.languageServer.enabled": true,
  "editor.rulers": [120],
  "editor.formatOnSave": true
}
```

## 📈 代码质量监控

代码质量检查已集成到本地开发流程中：

### 自动运行时机

- **本地开发**: 手动运行代码质量检查
- **提交前**: 手动检查 ktlint、Detekt、Lint
- **发布分支**: 包含依赖安全扫描

### 失败策略

- **ktlint 违规**: 构建失败 ❌
- **Detekt 严重问题**: 构建失败 ❌
- **单元测试失败**: 构建失败 ❌
- **依赖安全漏洞**: 警告但继续 ⚠️

## 🔍 报告和分析

### 查看检查报告

```bash
# 生成所有质量报告
./gradlew codeQualityCheck

# 报告位置
echo "📊 质量检查报告:"
echo "  - Detekt: app/build/reports/detekt/detekt.html"
echo "  - 测试覆盖率: app/build/reports/jacoco/jacocoTestReport/html/index.html"
echo "  - Android Lint: app/build/reports/lint-results.html"
echo "  - 依赖安全: build/reports/dependency-check-report.html"
```

## 🛡️ 安全最佳实践

### 依赖管理

- 定期更新依赖版本
- 关注安全公告
- 使用官方维护的库
- 避免使用废弃的依赖

### 代码安全

- 避免硬编码敏感信息
- 使用安全的加密方法
- 输入验证和边界检查
- 权限最小化原则

## ❓ 常见问题

### Q: ktlint 检查失败怎么办？

A: 运行 `./gradlew ktlintFormat` 自动修复大部分格式问题

### Q: Detekt 报告太多问题？

A: 可以在 `detekt.yml` 中调整规则阈值，但建议逐步修复而不是降低标准

### Q: 依赖安全扫描有误报？

A: 在 `config/dependency-check-suppressions.xml` 中添加抑制规则

### Q: 如何跳过某个检查？

A: 不建议跳过，但紧急情况下可以添加 `-x` 参数：

```bash
./gradlew build -x detekt -x ktlintCheck
```

## 📚 相关文档

- [Detekt 官方文档](https://detekt.dev/)
- [ktlint 官方文档](https://github.com/pinterest/ktlint)
- [OWASP 依赖检查](https://owasp.org/www-project-dependency-check/)
- [Pre-commit 配置](https://pre-commit.com/)
- [Android 代码质量指南](https://developer.android.com/guide/code-quality)

---

**提示**: 代码质量检查不是限制开发的枷锁，而是提升代码质量、减少 bug、提高可维护性的有效工具。养成良好的编码习惯，让质量检查成为开发流程的自然组成部分。
