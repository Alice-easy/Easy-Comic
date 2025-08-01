# EasyComic - 功能丰富的 Flutter 漫画阅读器

**EasyComic** 是一款使用 Flutter 构建的、功能丰富、注重隐私和用户体验的开源漫画阅读器。

## ✨ 核心功能

*   **应用启动与架构**: 实现了基于整洁架构（Clean Architecture）的稳定启动流程。
*   **书架系统**:
    *   **网格/列表视图**: 支持两种布局切换，满足不同浏览习惯。
    *   **搜索功能**: 快速在书架中查找漫画。
    *   **排序功能**: 支持按名称、更新时间等多种方式排序。
*   **收藏夹系统**: 轻松收藏您喜欢的漫画，并集中管理。
*   **文件导入**: 支持从设备中导入常见的漫画压缩包格式。
*   **WebDAV 数据备份与恢复**: 通过 WebDAV 协议，安全地将您的阅读进度、收藏等数据备份到您自己的服务器，并随时恢复。
*   **主题切换**: 内置亮色和暗色（夜间）模式，保护您的视力。
*   **沉浸式阅读**: 提供多种阅读模式（水平/垂直）、手势缩放、亮度调节等功能。
*   **还有更多...**: 探索应用以发现更多细节功能！

## 🏗️ 项目结构与架构

本项目遵循 **整洁架构（Clean Architecture）** 的设计原则，将代码逻辑清晰地分离到不同的层次，以提高可维护性、可测试性和可扩展性。

`lib/` 目录下的核心结构如下：

*   **`lib/data`**: 数据层。负责所有数据的来源与管理，包括与本地数据库（Drift）、远程服务（WebDAV）的交互。它实现了领域层定义的接口（Repositories）。
*   **`lib/domain`**: 领域层。包含核心业务逻辑和实体（Entities）。它定义了应用需要做什么，但不关心如何做。这一层不依赖于任何其他层。
*   **`lib/presentation`**: 表示层。负责所有与 UI 相关的功能，包含页面（Screens）、小组件（Widgets）以及状态管理逻辑（BLoC）。它依赖于领域层来执行用户操作和展示数据。

## 🛠️ 技术栈

*   **核心框架**: Flutter
*   **状态管理**: BLoC (flutter_bloc)
*   **依赖注入**: GetIt
*   **本地数据库**: Drift (基于 SQLite)
*   **网络同步**: webdav_client
*   **文件选择**: file_picker
*   **异步编程**: Dart Streams, Future

## 🚀 如何运行

1.  **克隆仓库**
    ```sh
    git clone https://github.com/alice-easy/Easy-Comic.git
    cd Easy-Comic
    ```

2.  **获取依赖**
    ```sh
    flutter pub get
    ```

3.  **运行代码生成器 (如果需要)**
    ```sh
    flutter packages pub run build_runner build --delete-conflicting-outputs
    ```

4.  **运行项目**
    ```sh
    flutter run
    ```

## 🤝 贡献

我们欢迎任何形式的贡献！如果您有好的建议或发现了 Bug，请通过 a Pull Request。

## 📄 许可证

本项目采用 [MIT 许可证](LICENSE)。