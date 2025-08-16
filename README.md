# Easy Comic - 专业Android漫画阅读器

[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-35-blue.svg)](https://developer.android.com/about/versions/15)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.10.00-green.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

一款采用现代Android开发技术栈构建的专业漫画阅读器应用，支持多种漫画格式，提供流畅的阅读体验和云端同步功能。

## ⚠️ 当前项目状态

**请注意：** 本项目正处于积极开发阶段。部分核心功能模块，特别是 **书架 (Bookshelf)** 和 **阅读器 (Reader)**，其代码已暂时禁用或尚未完全实现。当前版本主要用于展示项目的基础架构和部分核心逻辑。

- **已完成:**
    - Clean Architecture 基础架构搭建
    - Hilt 依赖注入配置
    - Room 数据库基础设置
    - 核心 Domain 和 Data 层模型
- **开发中:**
    - **书架 UI 与功能**
    - **阅读器核心交互**
    - 文件解析与管理
- **计划中:**
    - WebDAV 同步
    - 高级设置与用户偏好
    - 性能全面优化

## 📖 项目概述

Easy Comic是一个完全基于**Clean Architecture**和**MVVM模式**的Android漫画阅读器，采用**Jetpack Compose**构建现代化UI界面，支持**ZIP/RAR/CBZ/CBR**格式的漫画文件，并提供**WebDAV云端同步**功能。

### 🎯 设计目标
- **用户体验优先**：启动时间<2秒，翻页响应<100ms
- **现代化架构**：Clean Architecture + MVVM + Repository模式
- **可维护性**：模块化设计，单一职责原则
- **扩展性**：支持多种文件格式和云服务
- **性能优化**：内存管理、图片缓存、数据库优化

## 🚀 技术栈详情

### 📱 核心框架
| 技术栈 | 版本 | 用途 |
|--------|------|------|
| **Kotlin** | 1.9.0 | 开发语言 |
| **Android SDK** | Min 24, Target 35 | 平台支持 |
| **Jetpack Compose** | BOM 2024.10.00 | UI框架 |
| **Material Design 3** | Latest | 设计规范 |

### 🏗️ 架构组件
```
Clean Architecture Layers:
┌─── Presentation Layer ───┐
│  • Jetpack Compose UI    │
│  • ViewModels (MVVM)     │
│  • Navigation Component  │
└───────────────────────────┘
           │
┌─── Domain Layer ─────────┐
│  • Use Cases             │
│  • Repository Interfaces │
│  • Domain Models         │
└───────────────────────────┘
           │
┌─── Data Layer ───────────┐
│  • Repository Impl       │
│  • Room Database         │
│  • WebDAV Client         │
│  • File System Access    │
└───────────────────────────┘
```

### 🛠️ 依赖注入与数据管理
- **Hilt**: 依赖注入框架
- **Room Database**: 本地数据存储
- **DataStore**: 配置和偏好存储
- **Kotlin Coroutines + Flow**: 异步编程

### 🌐 网络与同步
- **Retrofit2 + OkHttp3**: HTTP客户端
- **WebDAV (Sardine)**: 云端同步协议
- **WorkManager**: 后台同步任务
- **Security-Crypto**: 认证信息加密存储

### 🖼️ 图片处理
- **Coil**: 图片加载与缓存
- **BitmapRegionDecoder**: 大图片内存优化
- **EXIF**: 图片方向自动校正

### 📁 文件格式支持
- **ZIP/CBZ**: 原生Android支持
- **RAR/CBR**: JunRar库支持
- **图片格式**: JPEG, PNG, GIF, WebP

## 🏢 项目架构

```
app/src/main/java/com/easycomic/
├── data/                     # 数据层
│   ├── database/            # Room数据库
│   │   ├── dao/            # 数据访问对象
│   │   ├── entity/         # 数据库实体
│   │   └── AppDatabase.kt  # 数据库配置
│   ├── repository/         # Repository实现
│   │   ├── MangaRepositoryImpl.kt
│   │   ├── BookmarkRepositoryImpl.kt
│   │   └── HistoryRepositoryImpl.kt
│   └── webdav/            # WebDAV同步
│       ├── WebDAVClient.kt
│       └── SyncService.kt
├── domain/                  # 领域层
│   ├── model/              # 域模型
│   │   ├── Manga.kt
│   │   ├── Bookmark.kt
│   │   └── ReadingHistory.kt
│   ├── repository/         # Repository接口
│   │   ├── MangaRepository.kt
│   │   ├── BookmarkRepository.kt
│   │   └── HistoryRepository.kt
│   └── usecase/           # 用例
│       ├── manga/         # 漫画相关用例
│       ├── bookmark/      # 书签用例
│       └── sync/         # 同步用例
├── presentation/           # 表示层
│   ├── ui/                # UI组件
│   │   ├── bookshelf/     # 书架模块
│   │   │   ├── BookshelfScreen.kt
│   │   │   ├── BookshelfViewModel.kt
│   │   │   └── components/
│   │   ├── reader/        # 阅读器模块
│   │   │   ├── ReaderScreen.kt
│   │   │   ├── ReaderViewModel.kt
│   │   │   └── components/
│   │   ├── settings/      # 设置模块
│   │   │   ├── SettingsScreen.kt
│   │   │   ├── SettingsViewModel.kt
│   │   │   └── components/
│   │   └── favorites/     # 收藏模块
│   │       ├── FavoritesScreen.kt
│   │       ├── FavoritesViewModel.kt
│   │       └── components/
│   ├── theme/             # 主题样式
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   └── components/        # 通用UI组件
│       ├── ComicCard.kt
│       ├── ReadingProgress.kt
│       └── SearchBar.kt
├── core/                   # 核心功能
│   ├── database/          # 数据库核心
│   ├── network/           # 网络核心
│   └── util/             # 工具类
├── utils/                 # 工具函数
│   ├── FileUtils.kt      # 文件处理
│   ├── ImageUtils.kt     # 图片处理
│   └── ComicParser.kt    # 漫画解析
└── di/                   # 依赖注入
    ├── DatabaseModule.kt
    ├── NetworkModule.kt
    └── RepositoryModule.kt
```

## 📊 数据库设计

### 核心数据表

#### 📚 manga 表
```sql
CREATE TABLE manga (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,                    -- 漫画标题
    author TEXT,                           -- 作者
    description TEXT,                      -- 描述
    file_path TEXT NOT NULL UNIQUE,       -- 文件路径
    file_size INTEGER NOT NULL,           -- 文件大小（字节）
    format TEXT NOT NULL,                 -- 文件格式（ZIP/RAR/CBZ/CBR）
    cover_path TEXT,                      -- 封面图片路径
    page_count INTEGER DEFAULT 0,        -- 总页数
    current_page INTEGER DEFAULT 1,       -- 当前页码
    reading_progress REAL DEFAULT 0.0,   -- 阅读进度（0.0-1.0）
    is_favorite INTEGER DEFAULT 0,       -- 是否收藏（0/1）
    is_completed INTEGER DEFAULT 0,      -- 是否已完成（0/1）
    date_added INTEGER NOT NULL,         -- 添加时间戳
    last_read INTEGER,                   -- 最后阅读时间戳
    reading_time INTEGER DEFAULT 0,     -- 总阅读时间（秒）
    rating REAL DEFAULT 0.0,           -- 评分（0.0-5.0）
    created_at INTEGER NOT NULL,        -- 创建时间
    updated_at INTEGER NOT NULL         -- 更新时间
);
```

#### 🔖 bookmark 表
```sql
CREATE TABLE bookmark (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    manga_id INTEGER NOT NULL,           -- 关联的漫画ID
    page_number INTEGER NOT NULL,       -- 书签页码
    bookmark_name TEXT,                 -- 书签名称
    notes TEXT,                        -- 备注
    created_at INTEGER NOT NULL,       -- 创建时间戳
    FOREIGN KEY (manga_id) REFERENCES manga(id) ON DELETE CASCADE,
    UNIQUE(manga_id, page_number)      -- 同一漫画同一页面只能有一个书签
);
```

#### 📈 reading_history 表
```sql
CREATE TABLE reading_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    manga_id INTEGER NOT NULL,          -- 关联的漫画ID
    page_number INTEGER NOT NULL,      -- 阅读页码
    reading_time INTEGER NOT NULL,     -- 本次阅读时间（秒）
    session_start INTEGER NOT NULL,    -- 会话开始时间戳
    session_end INTEGER NOT NULL,      -- 会话结束时间戳
    reading_speed REAL,                -- 阅读速度（页/分钟）
    created_at INTEGER NOT NULL,       -- 记录创建时间
    FOREIGN KEY (manga_id) REFERENCES manga(id) ON DELETE CASCADE
);
```

### 索引优化
```sql
-- 性能优化索引
CREATE INDEX idx_manga_title ON manga(title);
CREATE INDEX idx_manga_last_read ON manga(last_read DESC);
CREATE INDEX idx_manga_favorite ON manga(is_favorite, last_read DESC);
CREATE INDEX idx_bookmark_manga_id ON bookmark(manga_id);
CREATE INDEX idx_history_manga_id ON reading_history(manga_id);
CREATE INDEX idx_history_session ON reading_history(session_start);
```

## 🎨 核心功能模块

### 📚 书架管理模块
**⚠️ 状态：开发中，当前已禁用**

**功能特性：**
- **网格布局显示**：自适应网格，支持2-4列显示
- **搜索功能**：标题、作者、标签全文搜索
- **分类管理**：按格式、状态、评分分类
- **多选操作**：批量删除、移动、标记操作
- **排序选项**：标题、添加时间、最后阅读、评分排序

**实现要点：**
- 使用`LazyVerticalGrid`实现高性能网格布局
- `Flow`响应式数据流确保实时更新
- 图片缓存策略优化封面加载性能

### 📖 阅读器核心模块
**⚠️ 状态：开发中，当前已禁用**

**功能特性：**
- **多屏幕适配**：横竖屏无缝切换
- **手势控制**：双指缩放、单指滑动翻页
- **阅读模式**：
  - 适应屏幕：自动适配屏幕尺寸
  - 填充屏幕：保持比例填满屏幕
  - 原始尺寸：100%原始大小显示
- **翻页方式**：
  - 左右滑动：传统翻页方式
  - 上下滚动：连续滚动阅读
  - 点击翻页：左右区域点击翻页
- **沉浸式体验**：全屏模式，状态栏自动隐藏

**技术实现：**
```kotlin
// 图片处理优化示例
class ImageProcessor {
    fun processMangaPage(imagePath: String): Bitmap? {
        return BitmapRegionDecoder.newInstance(imagePath, false)?.let { decoder ->
            val options = BitmapFactory.Options().apply {
                inSampleSize = calculateSampleSize(decoder.width, decoder.height)
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            decoder.decodeRegion(Rect(0, 0, decoder.width, decoder.height), options)
        }
    }
    
    private fun calculateSampleSize(width: Int, height: Int): Int {
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels
        return max(width / screenWidth, height / screenHeight).takeIf { it > 1 } ?: 1
    }
}
```

### ⭐ 收藏和历史模块
**收藏管理：**
- 收藏夹分组管理
- 自定义收藏标签
- 收藏统计和分析

**历史跟踪：**
- 详细阅读记录
- 阅读时长统计
- 阅读进度可视化
- 阅读速度分析

### ⚙️ 设置页面模块
**主题设置：**
- 跟随系统、浅色、深色主题
- 自定义主色调
- 夜间模式护眼设置

**阅读偏好：**
- 默认阅读方向
- 默认缩放模式
- 翻页动画效果
- 屏幕亮度控制

### ☁️ WebDAV同步模块
**同步功能：**
- **全量同步**：首次同步，完整数据传输
- **增量同步**：仅同步变更部分，节省流量
- **冲突处理**：时间戳策略，保留最新版本
- **离线支持**：网络恢复后自动同步

**安全特性：**
- 认证信息加密存储
- HTTPS连接保护
- 数据传输压缩

## 🚀 开发阶段指导

### 📋 第一阶段：基础架构搭建（已完成）
- [x] **项目初始化**
  - Gradle配置和依赖管理
  - Hilt依赖注入配置
  - Room数据库搭建
  - 导航架构设置

- [x] **核心模块设计**
  - Domain层模型定义
  - Repository接口设计
  - 基础UI主题配置
  - 文件系统权限处理

### 🎯 第二阶段：核心功能开发（进行中）
- [ ] **文件解析器**
  - ZIP/CBZ格式支持
  - RAR/CBR格式支持
  - 图片排序和处理
  - 封面自动提取

- [ ] **阅读器实现**
  - 基础阅读界面
  - 缩放和翻页手势
  - 进度保存机制
  - 书签系统

- [ ] **书架功能**
  - 漫画列表显示
  - 搜索和筛选
  - 文件导入功能

### 🔧 第三阶段：高级功能实现（2-3周）
- [ ] **WebDAV同步**
  - 服务器连接配置
  - 同步机制实现
  - 冲突解决策略
  - 后台同步服务

- [ ] **用户体验优化**
  - 设置页面完善
  - 主题系统实现
  - 性能优化调整
  - 错误处理完善

### ✨ 第四阶段：完善和优化（1-2周）
- [ ] **性能调优**
  - 内存使用优化
  - 启动时间优化
  - 数据库查询优化
  - 图片缓存策略

- [ ] **测试和发布**
  - 单元测试编写
  - UI测试实现
  - 性能测试验证
  - 打包和发布准备

## ⚡ 性能优化策略

### 📱 启动优化
```kotlin
// 应用启动优化
class EasyComicApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 延迟初始化非关键组件
        DelayedInitializer.schedule {
            initializeSecondaryComponents()
        }
        
        // 预加载关键数据
        preloadEssentialData()
    }
}
```

### 🖼️ 图片处理优化
- **内存管理**：使用`BitmapRegionDecoder`处理大图片
- **缓存策略**：Coil的多级缓存（内存+磁盘）
- **懒加载**：`LazyColumn`实现图片延迟加载
- **格式优化**：优先使用WebP格式，减少内存占用

### 💾 数据库优化
```sql
-- 查询优化示例
-- 使用复合索引优化常用查询
CREATE INDEX idx_manga_composite ON manga(is_favorite, last_read DESC, title);

-- 分页查询，避免全表扫描
SELECT * FROM manga 
ORDER BY last_read DESC 
LIMIT 20 OFFSET ?;
```

### 🔄 协程优化
```kotlin
// 协程最佳实践
class MangaRepository @Inject constructor(
    private val mangaDao: MangaDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    fun getMangaList(): Flow<List<Manga>> = mangaDao.getAllManga()
        .flowOn(ioDispatcher)
        .catch { exception ->
            Log.e("MangaRepository", "Error loading manga", exception)
            emit(emptyList())
        }
}
```

## 📏 用户体验规范

### ⏱️ 性能指标
- **启动时间**：冷启动 < 2秒，热启动 < 1秒
- **翻页响应**：手势响应 < 100ms
- **搜索响应**：搜索结果展示 < 500ms
- **同步速度**：WebDAV同步进度实时反馈

### 🎨 UI/UX设计原则
- **Material Design 3**：遵循最新设计规范
- **无障碍支持**：完整的TalkBack和语音导航支持
- **响应式设计**：平板和手机完美适配
- **一致性**：统一的交互模式和视觉风格

### 🌓 主题适配
```kotlin
// 动态主题支持
@Composable
fun EasyComicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(LocalContext.current)
            else dynamicLightColorScheme(LocalContext.current)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

## 🛠️ 开发环境配置

### 📋 环境要求
- **Android Studio**: Hedgehog | 2023.1.1或更高版本
- **JDK**: JDK 17（推荐使用JetBrains Runtime）
- **Kotlin**: 1.9.0+
- **Gradle**: 8.4+
- **Android Gradle Plugin**: 8.2.0+

### 🚀 快速开始
```bash
# 1. 克隆项目
git clone https://github.com/yourusername/easy-comic.git
cd easy-comic

# 2. 检查环境
./gradlew --version

# 3. 构建项目
./gradlew build

# 4. 运行测试
./gradlew test

# 5. 安装到设备
./gradlew installDebug
```

### 📦 关键依赖版本
```kotlin
// libs.versions.toml 参考配置
[versions]
compose-bom = "2024.10.00"
kotlin = "1.9.0"
hilt = "2.48"
room = "2.6.1"
retrofit = "2.9.0"
coil = "2.5.0"
```

## 🧪 测试策略

### 🎯 测试覆盖
- **单元测试**：Repository、UseCase、ViewModel
- **集成测试**：数据库操作、网络请求
- **UI测试**：关键用户流程
- **性能测试**：内存、启动时间、响应时间

### 📝 测试示例
```kotlin
@RunWith(AndroidJUnit4::class)
class MangaRepositoryTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    @Test
    fun `when manga added, should return in list`() = runTest {
        // Given
        val manga = Manga(title = "Test Manga", filePath = "/test.zip")
        
        // When
        repository.addManga(manga)
        
        // Then
        val result = repository.getAllManga().first()
        assertThat(result).contains(manga)
    }
}
```

## 📄 许可证

本项目采用 MIT 许可证 - 详情请查看 [LICENSE](LICENSE) 文件。

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支：`git checkout -b feature/AmazingFeature`
3. 提交更改：`git commit -m 'Add some AmazingFeature'`
4. 推送到分支：`git push origin feature/AmazingFeature`
5. 开启 Pull Request

### 📋 开发规范
- 遵循 [Kotlin 编码规范](https://kotlinlang.org/docs/coding-conventions.html)
- 提交前运行 `./gradlew detekt` 进行代码检查
- 新功能需要编写对应的单元测试
- 提交信息遵循 [Conventional Commits](https://conventionalcommits.org/)

## 📞 支持与反馈

- **问题报告**: [GitHub Issues](https://github.com/yourusername/easy-comic/issues)
- **功能请求**: [GitHub Discussions](https://github.com/yourusername/easy-comic/discussions)
- **邮件联系**: easy@ea.cloudns.ch

---

<div align="center">

**Easy Comic - 让阅读更简单** 📚✨

Made with ❤️ by Easy Comic Team

</div>