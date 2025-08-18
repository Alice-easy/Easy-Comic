# Android项目代码瘦身优化工具

🧹 一个专为Android项目设计的智能代码清理工具，帮助您快速清理测试文件、废弃代码和未使用的依赖项。

## ✨ 主要功能

- 🗑️ **智能测试文件清理** - 自动识别并清理所有测试目录和测试文件
- 📄 **废弃代码检测** - 清理过期文件、备份文件、临时文件和日志文件
- 🖼️ **未使用资源清理** - 检测并清理未使用的资源文件
- 📦 **依赖项优化** - 分析并移除未使用的Gradle依赖项
- 🛡️ **安全备份机制** - 自动创建备份，支持一键恢复
- 📊 **详细清理报告** - 生成HTML格式的详细清理报告
- 🔍 **风险评估** - 智能识别高风险操作，确保项目安全

## 🚀 快速开始

### 安装要求

- Python 3.8+
- Android项目（支持Gradle构建系统）

### 基本使用

1. **清理当前目录的Android项目**
```bash
python main.py
```

2. **指定项目路径**
```bash
python main.py --project-path /path/to/your/android/project
```

3. **干运行模式（仅分析不删除）**
```bash
python main.py --dry-run
```

4. **使用自定义配置**
```bash
python main.py --config custom_config.json
```

5. **详细输出模式**
```bash
python main.py --verbose
```

### 命令行参数

| 参数 | 简写 | 说明 | 默认值 |
|------|------|------|--------|
| `--project-path` | `-p` | Android项目根目录路径 | 当前目录 |
| `--config` | `-c` | 自定义配置文件路径 | 使用默认配置 |
| `--dry-run` | `-d` | 仅分析不执行删除操作 | False |
| `--verbose` | `-v` | 详细输出模式 | False |
| `--output` | `-o` | 清理报告输出文件名 | cleanup_report.html |

## 📋 配置说明

工具支持通过JSON配置文件自定义清理规则。参考 `config/example_config.json`：

### 测试文件配置
```json
{
  "test_directories": [
    "src/test",
    "src/androidTest",
    "test",
    "androidTest"
  ],
  "test_file_patterns": [
    "*Test.kt",
    "*Test.java",
    "*Tests.kt",
    "*Tests.java"
  ]
}
```

### 过期文件配置
```json
{
  "obsolete_file_patterns": [
    "*.tmp",
    "*.bak",
    "*.old",
    "*.backup",
    ".DS_Store",
    "Thumbs.db"
  ]
}
```

### 安全检查配置
```json
{
  "safety_checks": {
    "require_confirmation": true,
    "create_backup": true,
    "backup_directory": ".cleanup_backup",
    "max_files_per_operation": 1000
  }
}
```

### 受保护文件配置
```json
{
  "protected_directories": [
    "src/main",
    ".git",
    ".gradle",
    "gradle"
  ],
  "protected_files": [
    "build.gradle",
    "build.gradle.kts",
    "settings.gradle",
    "AndroidManifest.xml"
  ]
}
```

## 🛡️ 安全机制

### 自动备份
- 清理前自动创建完整备份
- 备份包含所有待删除文件
- 生成备份清单文件
- 支持手动恢复

### 多重安全检查
- 文件数量限制检查
- 重要文件保护
- 高风险文件识别
- 项目完整性验证

### 风险评估
- 智能识别高风险操作
- 大文件删除警告
- 重要目录保护
- 用户确认机制

## 📊 清理报告

工具会生成详细的HTML格式清理报告，包含：

- 📈 **清理统计** - 删除文件数量、释放空间等
- 📋 **详细清单** - 所有删除文件的完整列表
- 📦 **依赖变更** - 移除的依赖项记录
- ⏱️ **操作时间线** - 完整的操作历史
- 🔍 **风险评估** - 高风险操作和警告信息

## 🏗️ 项目结构

```
code_cleaner/
├── main.py                 # 主入口文件
├── config/
│   ├── settings.py         # 配置管理
│   └── example_config.json # 配置示例
├── core/
│   ├── project_scanner.py  # 项目扫描器
│   ├── file_analyzer.py    # 文件分析器
│   ├── dependency_analyzer.py # 依赖分析器
│   └── cleaner_engine.py   # 清理执行引擎
├── models/
│   └── scan_result.py      # 数据模型
├── ui/
│   └── cli_interface.py    # 命令行界面
├── utils/
│   └── logger.py           # 日志工具
└── README.md
```

## 🎯 支持的项目类型

- ✅ **标准Android项目** - 单模块和多模块项目
- ✅ **Kotlin项目** - 完整支持Kotlin代码和KTS构建脚本
- ✅ **Java项目** - 支持传统Java Android项目
- ✅ **混合项目** - Kotlin + Java混合项目
- ✅ **Gradle构建** - 支持Groovy和Kotlin DSL

## 📝 使用示例

### 示例1：基本清理
```bash
# 清理当前目录的Android项目
python main.py

# 输出示例：
# 🔍 发现的模块数量: 5
# 🗑️ 测试文件: 23 个 (1.2 MB)
# 📄 过期文件: 8 个 (0.5 MB)
# 📦 未使用依赖: 3 个
# 💾 可释放空间: 1.7 MB
```

### 示例2：干运行模式
```bash
# 仅分析不删除，查看会清理哪些文件
python main.py --dry-run --verbose

# 输出详细的分析结果和文件列表
```

### 示例3：自定义配置
```bash
# 使用自定义配置文件
python main.py --config my_config.json --output my_report.html
```

## ⚠️ 注意事项

1. **首次使用建议先执行干运行模式**，确认清理内容
2. **重要项目请手动备份**，虽然工具有自动备份功能
3. **检查清理报告**，确认所有操作都符合预期
4. **保留备份文件**，直到确认项目运行正常

## 🤝 贡献指南

欢迎提交Issue和Pull Request来改进这个工具！

## 📄 许可证

MIT License - 详见 LICENSE 文件

## 🔗 相关链接

- [Android开发官方文档](https://developer.android.com/)
- [Gradle构建工具](https://gradle.org/)
- [Kotlin语言](https://kotlinlang.org/)

---

💡 **提示**: 定期清理项目可以显著减少项目大小，提高构建速度，让您的开发环境更加整洁高效！