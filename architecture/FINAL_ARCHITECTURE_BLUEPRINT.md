# Easy-Comic Flutter 应用架构蓝图

**版本:** 1.0
**日期:** 2025-08-01

## 1. 引言

本文档为 "Easy-Comic" Flutter 应用提供了最终的、权威的系统架构设计。此蓝图基于 [`specs/comprehensive_feature_spec.pseudo`](../specs/comprehensive_feature_spec.pseudo) 中定义的全面功能规范，并整合了项目早期的架构决策和模式。

其目标是为开发团队提供一个清晰、健壮且可扩展的框架，以指导后续的所有开发、重构和维护工作。

## 2. 核心架构原则

本应用严格遵循 **整洁架构 (Clean Architecture)** 原则，旨在实现关注点分离 (SoC)、增强可测试性、独立于外部框架和 UI。

*   **依赖规则**: 所有依赖关系都指向内部。UI 和基础设施（数据层）依赖于核心业务逻辑（领域层），但领域层不依赖于任何外部层。
*   **模块化**: 功能被划分为独立的、可独立开发的模块（例如书架、阅读器、同步），每个模块都遵循分层架构。
*   **领域驱动设计 (DDD)**: 领域层是架构的核心，包含了应用的业务实体和规则，使其与外部变化隔离。

## 3. 技术选型

根据项目需求和现有实践，确定以下技术栈：

*   **状态管理**: **BLoC (flutter_bloc)** - 用于管理UI状态，将业务逻辑与UI组件分离，提供可预测的状态转换。
*   **依赖注入**: **GetIt** - 作为服务定位器，解耦应用各层之间的类依赖关系。
*   **本地数据库**: **Drift (基于 SQLite)** - 用于持久化核心数据，如漫画信息、书架、收藏夹等。提供类型安全的 SQL 查询。
*   **键值对存储**: **SharedPreferences** - 用于存储用户设置和其他轻量级配置。
*   **函数式编程**: **dartz (Either)** - 用于优雅地处理错误和成功两种结果，避免过多的 `try-catch` 块。
*   **WebDAV 同步**: **webdav_client** - 用于实现与 WebDAV 服务器的数据备份和恢复。
*   **文件处理**: **archive** - 用于解压漫画存档文件（.cbz, .zip）。
*   **日志**: **logger** - 用于结构化和可配置的应用日志记录。
*   **网络状态**: **connectivity_plus** - 用于检测网络连接状态。

## 4. 分层架构详解

应用被划分为三个主要层次：表示层、领域层和数据层。

```mermaid
graph TD
    A[表示层 (Presentation)] --> B[领域层 (Domain)]
    C[数据层 (Data)] --> B

    subgraph 表示层 (Presentation)
        P1[UI (Widgets)]
        P2[状态管理 (BLoCs)]
    end

    subgraph 领域层 (Domain)
        D1[实体 (Entities)]
        D2[仓库接口 (Repository Interfaces)]
        D3[用例 (UseCases)]
    end

    subgraph 数据层 (Data)
        DA1[仓库实现 (Repository Implementations)]
        DA2[数据源 (DataSources)]
        DA3[数据模型 (Data Models)]
    end

    P1 -- interacts with --> P2
    P2 -- calls --> D3
    D3 -- uses --> D2
    DA1 -- implements --> D2
    DA1 -- gets data from --> DA2
    DA2 -- uses --> DA3
```

### 4.1 表示层 (Presentation Layer)

*   **职责**: 显示UI，并将用户输入传递给业务逻辑层。
*   **组件**:
    *   **UI (Widgets)**: Flutter 的原生 Widgets，负责渲染。它们是“哑”组件，仅根据状态进行渲染，并将所有用户交互委托给 BLoC。
    *   **状态管理 (BLoCs)**: 每个具有复杂状态的页面或组件都有一个 BLoC。BLoC 接收来自 UI 的事件，通过调用领域层的 UseCases 执行业务逻辑，并发出新的状态供 UI 渲染。

### 4.2 领域层 (Domain Layer)

*   **职责**: 包含应用的核心业务逻辑和规则。此层完全独立，不包含任何 Flutter 或第三方库的特定代码。
*   **组件**:
    *   **实体 (Entities)**: 代表核心业务对象的纯 Dart 类（例如 `Comic`, `Bookshelf`）。
    *   **仓库接口 (Repository Interfaces)**: 定义数据操作的契约（例如 `IComicRepository`）。它们由数据层实现，由 UseCases 使用。
    *   **用例 (UseCases / Interactors)**: 封装单一、具体的业务操作（例如 `GetBookshelvesUseCase`）。它们是领域层逻辑的主要执行者，协调一个或多个仓库。

### 4.3 数据层 (Data Layer)

*   **职责**: 实现领域层定义的仓库接口，处理所有数据的来源和持久化。
*   **组件**:
    *   **仓库实现 (Repository Implementations)**: 实现领域层的仓库接口，并负责从一个或多个数据源获取、缓存和组合数据。
    *   **数据源 (DataSources)**:
        *   **LocalDataSource**: 与本地数据库（Drift）或 SharedPreferences 交互。
        *   **RemoteDataSource**: 与远程 API（如 WebDAV）交互。
    *   **数据模型 (Data Models)**: Drift 表或其他数据源使用的具体数据结构。这些模型可以被映射到领域层的实体。

## 5. 核心组件和数据流

以下是一个关键流程的数据流示例：**加载并显示书架中的漫画**。

1.  **UI (`HomeScreen`)**: 触发 `LoadBookshelf` 事件发送给 `HomeBloc`。
2.  **BLoC (`HomeBloc`)**: 接收到事件，发出 `HomeLoading` 状态，并调用 `GetBookshelvesUseCase`。
3.  **UseCase (`GetBookshelvesUseCase`)**: 调用 `IComicRepository` 的 `getComicsByBookshelf` 方法。
4.  **Repository Impl (`ComicRepositoryImpl`)**: 实现该方法，首先查询 `ComicLocalDataSource`。
5.  **DataSource (`ComicLocalDataSource`)**: 使用 Drift 从 SQLite 数据库中查询 `Comics` 表，返回数据模型列表。
6.  **Repository Impl**: 将数据模型列表映射为领域实体 (`List<Comic>`) 列表。
7.  **UseCase**: 将实体列表返回给 BLoC。
8.  **BLoC**: 接收到漫画列表，发出一个包含该列表的 `HomeLoaded` 状态。
9.  **UI**: `BlocBuilder` 接收到 `HomeLoaded` 状态，并使用漫画列表数据构建 `GridView` 或 `ListView`。

## 6. 模块化设计

应用功能根据 [`specs/comprehensive_feature_spec.pseudo`](../specs/comprehensive_feature_spec.pseudo) 分为以下核心模块：

*   **MOD-01: 应用启动
*   **MOD-02: 数据持久化 (Drift, SharedPreferences)
*   **MOD-03: 书架功能
*   **MOD-04: 收藏夹系统
*   **MOD-05: 文件管理和导入
*   **MOD-06: WebDAV 备份
*   **MOD-07: UI 增强
*   **MOD-08: 错误处理和调试
*   **MOD-09: 性能优化

每个模块都应在各自的特性目录中实现其分层架构（Presentation, Domain, Data）。

## 7. 目录结构

为了反映整洁架构，`lib` 目录应按以下结构组织：

```
lib/
|-- core/                     # 应用核心工具和服务 (e.g., Error Handling, NetworkInfo, Logging)
|   |-- services/
|   |-- error/
|   |-- utils/
|
|-- data/                     # 数据层
|   |-- datasources/
|   |   |-- local/
|   |   |-- remote/
|   |-- models/
|   |-- repositories/         # Repository 实现
|
|-- domain/                   # 领域层
|   |-- entities/
|   |-- repositories/         # Repository 接口
|   |-- usecases/
|
|-- presentation/             # 表示层
|   |-- features/             # 按功能模块组织
|   |   |-- bookshelf/
|   |   |   |-- bloc/
|   |   |   |-- view/
|   |   |   |-- widgets/
|   |   |-- reader/
|   |   |-- settings/
|   |-- widgets/              # 通用小部件
|
|-- injection_container.dart  # GetIt 依赖注入配置
|-- main.dart                 # 应用入口点
```

---
**文档结束**