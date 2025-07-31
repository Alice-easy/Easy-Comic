# Product Context

This file provides a high-level overview of the project and the expected product that will be created. Initially it is based upon projectBrief.md (if provided) and all other available project-related information in the working directory. This file is intended to be updated as the project evolves, and should be used to inform all other modes of the project's goals and context.
2025-07-30 09:08:18 - Log of updates made will be appended as footnotes to the end of this file.

*

## Project Goal

*   
*   为 'Easy-Comic' 应用打造一个功能强大、用户体验一流的漫画阅读器，提供高度的定制化和流畅的交互。

## Key Features

*   **阅读界面布局优化**:
    *   支持单页/双页模式切换。
    *   支持水平（左右翻页）/垂直（滚动阅读）模式。
*   **交互功能增强**:
    *   通过手势（双指开合）进行页面缩放。
    *   应用内亮度调节功能，独立于系统亮度。
*   **阅读进度管理**:
    *   添加/删除书签功能。
    *   自动记录和访问阅读历史。
*   **个性化设置**:
    *   多种预设背景颜色（如护眼模式、夜间模式）。
    *   *   可选择不同的翻页动画效果（如拟物、平移、淡入淡出）。

*   **页面排序与导航**:
    *   添加可拖动的翻页进度条，用于快速导航。
    *   支持基于文件名的页面自动排序。
    *   支持用户通过拖拽缩略图进行手动页面排序，并持久化保存该设置。
## Enhanced Core Reading Features (2025-07-31 Update)

* **核心显示与导航组件**:
  * 基于PageView/CustomScrollView的主图像查看器
  * 流畅的页面过渡动画 (slide, fade, scale效果)
  * 上一页/下一页导航功能
  * GestureDetector触摸手势和键盘事件处理
  * 页面进度指示器 (E1:P1格式)
  * Timer控制的自动翻页 (默认5秒间隔)

* **阅读模式与屏幕配置**:
  * SystemChrome屏幕方向锁定
  * 左到右/右到左阅读模式切换
  * OrientationBuilder屏幕方向布局适配
  * wakelock插件屏幕常亮功能

* **缩放与交互控制**:
  * InteractiveViewer图像缩放和平移
  * GestureDetector双击智能缩放
  * 自定义捏合手势识别器
  * 可配置点击区域检测 (默认25%屏幕区域)
  * hardware_buttons插件音量键翻页

* **视觉效果与主题管理**:
  * ThemeData暗色主题支持
  * 动态图片亮度滤镜
  * SystemChrome全屏阅读模式
  * 可切换页面信息显示
  * AnimationController流畅动画效果

* **文件处理与性能优化**:
  * archive插件支持CBZ/CBR/ZIP/RAR格式
  * LRU算法图像缓存系统
  * CachedNetworkImage或自定义缓存管理
  * 异步图像加载机制
  * 内存管理策略防止泄漏

* **用户界面组件**:
  * 底部AppBar/自定义播放控制栏
  * SharedPreferences设置持久化
  * Slider组件章节导航
  * SQLite书签收藏功能
  * 阅读进度持久化存储

* **状态管理与架构**:
  * Provider/Bloc/Riverpod状态管理
  * Repository模式数据层处理
  * Service层核心功能服务

* **平台适配与优化**:
  * Android/iOS平台特定优化
  * 响应式布局设计
  * 完善错误处理机制

## Overall Architecture

* **BLoC + Repository 架构模式**: 采用业务逻辑组件模式，实现UI、业务逻辑和数据层的清晰分离
* **模块化设计**: 核心显示、交互控制、数据管理、平台适配等模块独立设计
* **性能优先**: LRU缓存、异步加载、内存管理等性能优化策略
* **跨平台兼容**: Android/iOS平台特定优化，响应式布局设计

## System Architecture Design (2025-07-31 Updated)

### 三层架构设计
* **UI层 (Presentation Layer)**: ReaderScreen、ReaderCore、手势处理、菜单组件
* **业务逻辑层 (Business Logic Layer)**: ReaderBloc状态管理、导航服务、缓存服务、主题服务
* **数据层 (Data Layer)**: Repository模式、Drift数据库、Archive解析、平台服务集成

### 核心组件架构
* **ReaderBloc**: 事件驱动状态管理，支持LoadComic、PageChanged、ZoomChanged等完整事件流
* **Repository模式**: IComicRepository、ISettingsRepository、IBookmarkRepository接口抽象
* **智能缓存系统**: LRU内存缓存 + 磁盘缓存 + 优先级预加载队列 + 内存压力监控
* **Service层**: NavigationService、ZoomService、AutoPageService、ThemeService业务服务

### 性能优化架构
* **内存管理**: 自动内存压力检测、智能缓存淘汰、GC调度优化
* **异步数据流**: 多线程图像处理、后台预加载、流式数据更新
* **图像优化**: 智能尺寸调整、动态压缩质量、格式优化选择

### 平台适配架构
* **Android/iOS**: 平台特定服务实现 (亮度控制、硬件按键、文件系统)
* **响应式设计**: 多屏幕尺寸适配、方向变化处理、安全区域适配
* **硬件集成**: 音量键翻页、屏幕常亮、触觉反馈、传感器集成

---

## Functional Specification (2025-07-31)

A detailed functional specification has been created to translate user requirements into a concrete development plan. This document breaks down all features into modular components, defines their responsibilities, provides pseudocode, and lists necessary third-party libraries.

*   **[View Full Specification](specs/flutter_comic_reader_functional_spec.md)**