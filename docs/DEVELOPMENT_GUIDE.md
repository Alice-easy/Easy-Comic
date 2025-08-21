# Easy Comic - 开发指南和代码规范

## 目录
- [开发环境配置](#开发环境配置)
- [项目结构](#项目结构)
- [代码规范](#代码规范)
- [开发流程](#开发流程)
- [测试指南](#测试指南)
- [性能优化指南](#性能优化指南)
- [常见问题](#常见问题)

## 开发环境配置

### 必需工具
- **Android Studio**: Hedgehog | 2023.1.1 或更高版本
- **JDK**: 17 或更高版本
- **Kotlin**: 1.9.20
- **Gradle**: 8.14.3
- **Android SDK**: API 35 (compileSdk)，最低支持 API 24 (minSdk)

### 推荐插件
- **Detekt**: 代码质量检查
- **Dokka**: API文档生成
- **Kotlin Multiplatform Mobile**: Kotlin开发支持

### 项目配置
```bash
# 克隆项目
git clone https://github.com/your-repo/Easy-Comic.git
cd Easy-Comic

# 构建项目
./gradlew build

# 运行测试
./gradlew test

# 生成文档
./gradlew generateAllDocs
```

## 项目结构

### 模块划分
```
Easy-Comic/
├── app/                    # 应用主模块
├── domain/                 # 业务逻辑层（纯Kotlin）
├── data/                   # 数据访问层
├── ui_bookshelf/          # 书架UI模块
├── ui_reader/             # 阅读器UI模块
├── ui_di/                 # UI依赖注入模块
└── docs/                  # 项目文档
```

### 包结构规范
```kotlin
com.easycomic.{module}/
├── di/                    # 依赖注入配置
├── model/                 # 数据模型
├── repository/            # 仓库接口/实现
├── usecase/              # 用例类
├── ui/                   # UI组件
├── util/                 # 工具类
└── {feature}/            # 功能模块
```

## 代码规范

### Kotlin 编码规范

#### 1. 命名规范
```kotlin
// 类名：PascalCase
class MangaRepository

// 函数名：camelCase
fun getAllManga()

// 变量名：camelCase
val mangaList = listOf<Manga>()

// 常量：UPPER_SNAKE_CASE
const val MAX_CACHE_SIZE = 100

// 包名：小写，用点分隔
package com.easycomic.domain.usecase
```

#### 2. 类结构顺序
```kotlin
class ExampleClass {
    // 1. 伴生对象
    companion object {
        const val TAG = "ExampleClass"
    }
    
    // 2. 属性声明
    private val repository: Repository
    
    // 3. 初始化块
    init {
        // 初始化代码
    }
    
    // 4. 构造函数
    constructor(repository: Repository) {
        this.repository = repository
    }
    
    // 5. 公共方法
    fun publicMethod() {}
    
    // 6. 私有方法
    private fun privateMethod() {}
    
    // 7. 内部类
    inner class InnerClass
}
```

#### 3. 函数规范
```kotlin
// 单表达式函数
fun isCompleted(): Boolean = readingStatus == ReadingStatus.COMPLETED

// 多行函数参数对齐
fun updateReadingProgress(
    mangaId: Long,
    currentPage: Int,
    status: ReadingStatus
) {
    // 函数体
}

// 高阶函数
inline fun <T> measureTime(
    operation: String,
    block: () -> T
): T {
    val startTime = System.currentTimeMillis()
    return block().also {
        val duration = System.currentTimeMillis() - startTime
        Log.d(TAG, "$operation took ${duration}ms")
    }
}
```

### 文档注释规范

#### KDoc 注释格式
```kotlin
/**
 * 漫画相关用例聚合类
 * 
 * 提供漫画数据的完整业务逻辑操作，包括查询、更新、批量操作和统计功能。
 * 
 * ## 使用示例：
 * ```kotlin
 * val mangaUseCases = MangaUseCases(mangaRepository)
 * val allManga = mangaUseCases.getAllManga().first()
 * ```
 * 
 * @property mangaRepository 漫画数据仓库，用于数据持久化操作
 * @constructor 创建漫画用例实例
 * 
 * @author EasyComic Team
 * @since 1.0.0
 * @see MangaRepository
 */
class MangaUseCases(
    private val mangaRepository: MangaRepository
) {
    
    /**
     * 获取所有漫画列表
     * 
     * 从数据库中获取所有漫画，返回响应式数据流。
     * 数据会自动更新，UI层可以直接观察此流。
     * 
     * @return Flow<List<Manga>> 漫画列表的数据流
     * @throws DatabaseException 数据库访问异常
     * 
     * @sample
     * ```kotlin
     * mangaUseCases.getAllManga()
     *     .collect { mangaList ->
     *         // 处理漫画列表
     *     }
     * ```
     */
    fun getAllManga(): Flow<List<Manga>> = mangaRepository.getAllManga()
}
```

### Compose UI 规范

#### 1. Composable 函数命名
```kotlin
// Composable函数使用PascalCase
@Composable
fun BookshelfScreen(
    modifier: Modifier = Modifier,
    onComicClick: (Manga) -> Unit = {}
) {
    // UI实现
}

// 预览函数
@Preview
@Composable
private fun BookshelfScreenPreview() {
    EasyComicTheme {
        BookshelfScreen()
    }
}
```

#### 2. 状态管理
```kotlin
// 使用remember和mutableStateOf管理本地状态
@Composable
fun SearchBar(
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    
    OutlinedTextField(
        value = searchQuery,
        onValueChange = { newQuery ->
            searchQuery = newQuery
            onSearchQueryChange(newQuery)
        },
        modifier = modifier
    )
}

// 使用ViewModel管理复杂状态
@Composable
fun BookshelfScreen(
    viewModel: BookshelfViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    when {
        uiState.isLoading -> LoadingIndicator()
        uiState.isError -> ErrorMessage(uiState.errorMessage)
        else -> BookshelfContent(uiState.mangaList)
    }
}
```

#### 3. 主题和样式
```kotlin
// 使用Material Design 3主题
@Composable
fun ComicCard(
    manga: Manga,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        // 卡片内容
    }
}
```

### 依赖注入规范

#### Koin 配置
```kotlin
// 模块定义
val domainModule = module {
    // 使用factory创建每次都是新实例的对象
    factory { MangaUseCases(get()) }
    
    // 使用single创建单例对象
    single<MangaRepository> { MangaRepositoryImpl(get(), get()) }
    
    // 使用scoped创建作用域内单例
    scope<BookshelfViewModel> {
        scoped { BookshelfViewModel(get()) }
    }
}

// 模块组织
val appModules = listOf(
    domainModule,
    dataModule,
    uiModule
)
```

## 开发流程

### Git 工作流

#### 分支命名规范
```bash
# 功能分支
feature/manga-reader-optimization
feature/theme-system

# 修复分支
bugfix/memory-leak-fix
hotfix/crash-on-startup

# 发布分支
release/v1.0.0
```

#### 提交信息规范
```bash
# 格式：<type>(<scope>): <description>
feat(reader): add zoom functionality
fix(bookshelf): resolve memory leak in comic loading
docs(architecture): update clean architecture documentation
test(domain): add unit tests for MangaUseCases
refactor(data): optimize repository caching strategy
```

### 代码审查清单

#### 功能性检查
- [ ] 功能是否按需求正确实现
- [ ] 边界条件是否正确处理
- [ ] 错误处理是否完善
- [ ] 性能是否满足要求

#### 代码质量检查
- [ ] 代码是否遵循项目规范
- [ ] 是否有重复代码
- [ ] 函数是否过于复杂
- [ ] 变量命名是否清晰

#### 架构检查
- [ ] 是否遵循Clean Architecture原则
- [ ] 依赖方向是否正确
- [ ] 模块职责是否清晰
- [ ] 接口设计是否合理

## 测试指南

### 测试策略

#### 1. 单元测试 (70%)
```kotlin
// Domain层测试
class MangaUseCasesTest {
    
    private val mockRepository = mockk<MangaRepository>()
    private val mangaUseCases = MangaUseCases(mockRepository)
    
    @Test
    fun `getAllManga should return manga list from repository`() = runTest {
        // Given
        val expectedManga = listOf(
            Manga(id = 1, title = "Test Manga")
        )
        every { mockRepository.getAllManga() } returns flowOf(expectedManga)
        
        // When
        val result = mangaUseCases.getAllManga().first()
        
        // Then
        assertThat(result).isEqualTo(expectedManga)
        verify { mockRepository.getAllManga() }
    }
}
```

#### 2. 集成测试 (20%)
```kotlin
// Repository集成测试
@RunWith(AndroidJUnit4::class)
class MangaRepositoryImplTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private lateinit var database: AppDatabase
    private lateinit var repository: MangaRepositoryImpl
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        repository = MangaRepositoryImpl(
            mangaDao = database.mangaDao(),
            comicParserFactory = mockk()
        )
    }
    
    @Test
    fun insertAndRetrieveManga() = runTest {
        // Given
        val manga = Manga(title = "Test Manga", filePath = "/test/path")
        
        // When
        val insertedId = repository.insertOrUpdateManga(manga)
        val retrievedManga = repository.getMangaById(insertedId)
        
        // Then
        assertThat(retrievedManga?.title).isEqualTo("Test Manga")
    }
}
```

#### 3. UI测试 (10%)
```kotlin
// Compose UI测试
class BookshelfScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun bookshelfScreen_displaysComics() {
        // Given
        val testComics = listOf(
            Manga(id = 1, title = "Test Comic 1"),
            Manga(id = 2, title = "Test Comic 2")
        )
        
        // When
        composeTestRule.setContent {
            EasyComicTheme {
                BookshelfScreen(
                    comics = testComics,
                    onComicClick = {}
                )
            }
        }
        
        // Then
        composeTestRule
            .onNodeWithText("Test Comic 1")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Test Comic 2")
            .assertIsDisplayed()
    }
}
```

### 测试工具配置

#### 依赖配置
```kotlin
// build.gradle.kts
dependencies {
    // 单元测试
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("com.google.truth:truth:1.1.4")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    
    // Android测试
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    
    // 测试工具
    testImplementation("app.cash.turbine:turbine:1.0.0")
    androidTestImplementation("androidx.room:room-testing:2.6.1")
}
```

## 性能优化指南

### 内存优化

#### 1. 图片加载优化
```kotlin
// 使用Coil进行图片加载优化
@Composable
fun ComicImage(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build(),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}
```

#### 2. 对象池管理
```kotlin
class ObjectPoolManager {
    private val bitmapPool = Pools.SynchronizedPool<Bitmap>(10)
    
    fun acquireBitmap(width: Int, height: Int): Bitmap {
        return bitmapPool.acquire() 
            ?: Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }
    
    fun releaseBitmap(bitmap: Bitmap) {
        bitmap.recycle()
        bitmapPool.release(bitmap)
    }
}
```

### 网络优化

#### 缓存策略
```kotlin
class ThreeLevelCacheRepository<T> {
    private val memoryCache = LruCache<String, T>(maxSize)
    private val diskCache = DiskLruCache.open(cacheDir, version, valueCount, maxSize)
    
    suspend fun getData(key: String): T? {
        // 1. 检查内存缓存
        memoryCache[key]?.let { return it }
        
        // 2. 检查磁盘缓存
        diskCache.get(key)?.let { data ->
            memoryCache.put(key, data)
            return data
        }
        
        // 3. 从网络获取
        return fetchFromNetwork(key)?.also { data ->
            memoryCache.put(key, data)
            diskCache.put(key, data)
        }
    }
}
```

### 构建优化

#### Gradle 配置优化
```kotlin
// gradle.properties
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=1g
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true
android.useAndroidX=true
android.enableJetifier=true
```

## 常见问题

### 构建问题

#### Q: 编译时出现 "Unresolved reference" 错误
**A:** 检查以下几点：
1. 确保所有依赖都正确添加到 `build.gradle.kts`
2. 检查 `libs.versions.toml` 中的版本定义
3. 执行 `./gradlew clean build` 清理重新构建
4. 检查模块间的依赖关系是否正确

#### Q: Dokka 文档生成失败
**A:** 常见解决方案：
```bash
# 清理构建缓存
./gradlew clean

# 检查 Dokka 配置
./gradlew :domain:dokkaHtml --info

# 确保所有模块都有正确的 KDoc 注释
```

#### Q: 内存溢出 (OutOfMemoryError)
**A:** 优化措施：
1. 增加 JVM 堆内存：`org.gradle.jvmargs=-Xmx4g`
2. 启用并行构建：`org.gradle.parallel=true`
3. 使用构建缓存：`org.gradle.caching=true`

### 运行时问题

#### Q: 应用启动崩溃
**A:** 检查步骤：
1. 查看 Logcat 错误信息
2. 检查 Koin 依赖注入配置
3. 确保所有必需的权限已声明
4. 检查 ProGuard/R8 混淆规则

#### Q: 图片加载失败
**A:** 排查方向：
1. 检查文件路径是否正确
2. 确认 Storage Access Framework 权限
3. 验证图片格式是否支持
4. 检查内存使用情况

### 性能问题

#### Q: 应用响应缓慢
**A:** 性能优化建议：
1. 使用 Android Profiler 分析性能瓶颈
2. 检查主线程是否有耗时操作
3. 优化数据库查询和缓存策略
4. 减少不必要的重组 (Recomposition)

#### Q: 内存泄漏
**A:** 内存泄漏检测：
```kotlin
// 使用 LeakCanary 检测内存泄漏
debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")

// 在 Application 中启用
class EasyComicApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            LeakCanary.install(this)
        }
    }
}
```

### 测试问题

#### Q: 单元测试失败
**A:** 常见原因和解决方案：
1. **Mock 对象配置错误**：检查 MockK 的 `every` 和 `verify` 配置
2. **协程测试问题**：使用 `runTest` 和 `TestDispatcher`
3. **数据库测试失败**：确保使用 `Room.inMemoryDatabaseBuilder`

#### Q: UI 测试不稳定
**A:** 提高测试稳定性：
```kotlin
// 等待异步操作完成
composeTestRule.waitUntil(timeoutMillis = 5000) {
    composeTestRule
        .onAllNodesWithText("Loading...")
        .fetchSemanticsNodes()
        .isEmpty()
}

// 使用语义化测试
composeTestRule
    .onNodeWithContentDescription("Comic cover")
    .assertIsDisplayed()
```

## 最佳实践

### 代码组织

#### 1. 功能模块化
```kotlin
// 按功能组织代码，而不是按技术层次
com.easycomic.feature.bookshelf/
├── data/
├── domain/
├── ui/
└── di/
```

#### 2. 依赖管理
```kotlin
// 使用版本目录统一管理依赖
// libs.versions.toml
[versions]
kotlin = "1.9.20"
compose = "1.5.8"

[libraries]
kotlin-stdlib = { module = "org.jetbrains.kotlin:kotlin-stdlib", version.ref = "kotlin" }
compose-ui = { module = "androidx.compose.ui:ui", version.ref = "compose" }
```

### 性能最佳实践

#### 1. Compose 性能优化
```kotlin
// 使用 remember 缓存计算结果
@Composable
fun ExpensiveComposable(data: List<Item>) {
    val processedData = remember(data) {
        data.map { processItem(it) }
    }
    
    LazyColumn {
        items(processedData) { item ->
            ItemCard(item = item)
        }
    }
}

// 避免在 Composable 中创建对象
@Composable
fun BadExample() {
    val list = listOf(1, 2, 3) // 每次重组都会创建新列表
}

@Composable
fun GoodExample() {
    val list = remember { listOf(1, 2, 3) } // 只创建一次
}
```

#### 2. 数据库优化
```kotlin
// 使用索引优化查询
@Entity(
    tableName = "manga",
    indices = [
        Index(value = ["title"]),
        Index(value = ["last_read_time"])
    ]
)
data class MangaEntity(...)

// 使用分页查询大量数据
@Query("SELECT * FROM manga ORDER BY last_read_time DESC LIMIT :limit OFFSET :offset")
suspend fun getMangaPage(limit: Int, offset: Int): List<MangaEntity>
```

### 安全最佳实践

#### 1. 数据验证
```kotlin
// 输入验证
fun validateMangaTitle(title: String): Boolean {
    return title.isNotBlank() && 
           title.length <= MAX_TITLE_LENGTH &&
           !title.contains(INVALID_CHARACTERS)
}

// 文件类型验证
fun isValidComicFile(uri: Uri): Boolean {
    val mimeType = contentResolver.getType(uri)
    return mimeType in SUPPORTED_MIME_TYPES
}
```

#### 2. 权限管理
```kotlin
// 动态权限请求
class PermissionManager {
    fun requestStoragePermission(
        activity: ComponentActivity,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        val launcher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) onGranted() else onDenied()
        }
        
        launcher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}
```

## 发布流程

### 版本管理

#### 语义化版本控制
```
MAJOR.MINOR.PATCH
1.0.0 - 初始版本
1.1.0 - 新增功能
1.1.1 - 修复问题
2.0.0 - 重大更新
```

#### 发布检查清单
- [ ] 所有测试通过
- [ ] 代码审查完成
- [ ] 性能测试通过
- [ ] 文档更新完成
- [ ] 版本号更新
- [ ] 更新日志编写

### 构建配置

#### 发布构建配置
```kotlin
// app/build.gradle.kts
android {
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

## 工具推荐

### 开发工具
- **Android Studio**: 官方 IDE
- **Scrcpy**: 设备屏幕镜像
- **ADB**: Android 调试桥

### 调试工具
- **Flipper**: 移动应用调试平台
- **LeakCanary**: 内存泄漏检测
- **Chucker**: 网络请求监控

### 性能分析
- **Android Profiler**: 官方性能分析工具
- **Systrace**: 系统级性能分析
- **Method Tracing**: 方法调用分析

### 代码质量
- **Detekt**: Kotlin 静态代码分析
- **Android Lint**: Android 代码检查
- **SonarQube**: 代码质量管理平台

## 附录

### A. 快速参考

#### 常用命令
```bash
# 构建项目
./gradlew build

# 运行测试
./gradlew test
./gradlew connectedAndroidTest

# 代码质量检查
./gradlew detekt
./gradlew lint

# 生成文档
./gradlew dokkaHtml

# 清理项目
./gradlew clean
```

#### 项目配置文件
- `build.gradle.kts` - 构建配置
- `gradle.properties` - Gradle 属性
- `libs.versions.toml` - 依赖版本管理
- `detekt.yml` - 代码质量规则
- `lint.xml` - Android Lint 配置

### B. 技术栈版本

| 技术 | 版本 | 说明 |
|------|------|------|
| Kotlin | 1.9.20 | 编程语言 |
| Compose | 1.5.8 | UI 框架 |
| Room | 2.6.1 | 数据库 |
| Koin | 3.5.3 | 依赖注入 |
| Coroutines | 1.8.0 | 异步编程 |

### C. 联系方式

- **项目仓库**: https://github.com/your-repo/Easy-Comic
- **问题反馈**: GitHub Issues
- **技术讨论**: 项目 Wiki

## 总结

本开发指南涵盖了 Easy Comic 项目的完整开发流程，从环境配置到发布上线。遵循这些规范和最佳实践，可以确保：

1. **代码质量**：统一的编码规范和代码审查流程
2. **开发效率**：清晰的项目结构和开发流程
3. **产品质量**：完善的测试体系和性能优化
4. **团队协作**：标准化的工作流程和文档规范

持续改进这些规范，与时俱进地更新技术栈和最佳实践，是保持项目健康发展的关键。

---

**文档版本**: v1.0.0  
**最后更新**: 2025年1月  
**维护者**: EasyComic 开发团队
</content>
</write_to_file>

现在让我更新任务进度，标记架构设计文档和开发指南已完成：

<plan_update>
<arguments>
{
  "title": "Android漫画阅读应用技术债务优化",
  "features": ["代码重构优化", "性能优化", "架构改进", "测试覆盖率提升", "文档完善"],
  "tech": {
    "Android": "Kotlin + Clean Architecture + Jetpack Compose + Room + Koin"
  },
  "design": "基于Clean Architecture的模块化重构，采用MVVM模式和Compose组件化设计，建立完整的测试体系和文档规范",
  "plan": {
    "项目结构分析和技术债务评估": "done",
    "配置代码质量检查工具(Detekt、Android Lint)": "done",
    "重构Domain层复杂业务逻辑类": "done",
    "优化Data层Repository实现和Room查询": "done",
    "优化Koin依赖注入配置": "done",
    "重构UI层复杂Compose组件": "done",
    "实现内存优化策略(对象池、缓存管理)": "done",
    "优化文件解析性能(异步处理、分页加载)": "done",
    "添加Domain层单元测试": "done",
    "添加Repository集成测试": "done",
    "添加UI层Compose测试": "done",
    "配置Dokka文档生成": "done",
    "完善代码注释和KDoc": "done",
    "编写架构设计文档": "done",
    "编写开发指南和代码规范": "done"
  }
}
</arguments>
</plan_update>

<ask_followup_question>
<question>技术债务优化项目的所有15个任务已全部完成！我已经成功创建了完整的架构设计文档和开发指南。

**已完成的主要成果：**

📋 **架构设计文档** (`docs/architecture/ARCHITECTURE_DESIGN.md`)：
- Clean Architecture 详细设计说明
- 模块化架构和层次结构
- 设计模式应用（Repository、Factory、Observer、Strategy）
- 依赖注入架构和性能优化策略
- 测试架构和安全架构设计

📚 **开发指南** (`docs/DEVELOPMENT_GUIDE.md`)：
- 完整的开发环境配置指南
- Kotlin 和 Compose 编码规范
- KDoc 文档注释标准
- Git 工作流和代码审查清单
- 测试策略和性能优化指南
- 常见问题解决方案和最佳实践

**技术债务优化总结：**
✅ 代码重构优化 - 简化了复杂类和方法，提高了可读性
✅ 性能优化 - 实现了内存管理和文件解析优化
✅ 架构改进 - 完善了依赖注入和模块解耦
✅ 测试覆盖率提升 - 建立了完整的单元测试、集成测试和UI测试体系
✅ 文档完善 - 创建了详细的架构文档、开发指南和API文档

现在您希望：
1. 查看具体的文档内容并提供反馈
2. 继续进行其他项目改进工作
3. 开始新的功能开发任务</question>
<options>["查看文档内容并提供反馈", "继续其他项目改进工作", "开始新的功能开发任务"]</options>
# Easy Comic - 开发指南和代码规范

## 目录
- [开发环境配置](#开发环境配置)
- [项目结构](#项目结构)
- [代码规范](#代码规范)
- [开发流程](#开发流程)
- [测试指南](#测试指南)
- [性能优化指南](#性能优化指南)
- [常见问题](#常见问题)

## 开发环境配置

### 必需工具
- **Android Studio**: Hedgehog | 2023.1.1 或更高版本
- **JDK**: 17 或更高版本
- **Kotlin**: 1.9.20
- **Gradle**: 8.14.3
- **Android SDK**: API 35 (compileSdk)，最低支持 API 24 (minSdk)

### 推荐插件
- **Detekt**: 代码质量检查
- **Dokka**: API文档生成
- **Kotlin Multiplatform Mobile**: Kotlin开发支持

### 项目配置
```bash
# 克隆项目
git clone https://github.com/your-repo/Easy-Comic.git
cd Easy-Comic

# 构建项目
./gradlew build

# 运行测试
./gradlew test

# 生成文档
./gradlew generateAllDocs
```

## 项目结构

### 模块划分
```
Easy-Comic/
├── app/                    # 应用主模块
├── domain/                 # 业务逻辑层（纯Kotlin）
├── data/                   # 数据访问层
├── ui_bookshelf/          # 书架UI模块
├── ui_reader/             # 阅读器UI模块
├── ui_di/                 # UI依赖注入模块
└── docs/                  # 项目文档
```

### 包结构规范
```kotlin
com.easycomic.{module}/
├── di/                    # 依赖注入配置
├── model/                 # 数据模型
├── repository/            # 仓库接口/实现
├── usecase/              # 用例类
├── ui/                   # UI组件
├── util/                 # 工具类
└── {feature}/            # 功能模块
```

## 代码规范

### Kotlin 编码规范

#### 1. 命名规范
```kotlin
// 类名：PascalCase
class MangaRepository

// 函数名：camelCase
fun getAllManga()

// 变量名：camelCase
val mangaList = listOf<Manga>()

// 常量：UPPER_SNAKE_CASE
const val MAX_CACHE_SIZE = 100

// 包名：小写，用点分隔
package com.easycomic.domain.usecase
```

#### 2. 类结构顺序
```kotlin
class ExampleClass {
    // 1. 伴生对象
    companion object {
        const val TAG = "ExampleClass"
    }
    
    // 2. 属性声明
    private val repository: Repository
    
    // 3. 初始化块
    init {
        // 初始化代码
    }
    
    // 4. 构造函数
    constructor(repository: Repository) {
        this.repository = repository
    }
    
    // 5. 公共方法
    fun publicMethod() {}
    
    // 6. 私有方法
    private fun privateMethod() {}
    
    // 7. 内部类
    inner class InnerClass
}
```

#### 3. 函数规范
```kotlin
// 单表达式函数
fun isCompleted(): Boolean = readingStatus == ReadingStatus.COMPLETED

// 多行函数参数对齐
fun updateReadingProgress(
    mangaId: Long,
    currentPage: Int,
    status: ReadingStatus
) {
    // 函数体
}

// 高阶函数
inline fun <T> measureTime(
    operation: String,
    block: () -> T
): T {
    val startTime = System.currentTimeMillis()
    return block().also {
        val duration = System.currentTimeMillis() - startTime
        Log.d(TAG, "$operation took ${duration}ms")
    }
}
```

### 文档注释规范

#### KDoc 注释格式
```kotlin
/**
 * 漫画相关用例聚合类
 * 
 * 提供漫画数据的完整业务逻辑操作，包括查询、更新、批量操作和统计功能。
 * 
 * ## 使用示例：
 * ```kotlin
 * val mangaUseCases = MangaUseCases(mangaRepository)
 * val allManga = mangaUseCases.getAllManga().first()
 * ```
 * 
 * @property mangaRepository 漫画数据仓库，用于数据持久化操作
 * @constructor 创建漫画用例实例
 * 
 * @author EasyComic Team
 * @since 1.0.0
 * @see MangaRepository
 */
class MangaUseCases(
    private val mangaRepository: MangaRepository
) {
    
    /**
     * 获取所有漫画列表
     * 
     * 从数据库中获取所有漫画，返回响应式数据流。
     * 数据会自动更新，UI层可以直接观察此流。
     * 
     * @return Flow<List<Manga>> 漫画列表的数据流
     * @throws DatabaseException 数据库访问异常
     * 
     * @sample
     * ```kotlin
     * mangaUseCases.getAllManga()
     *     .collect { mangaList ->
     *         // 处理漫画列表
     *     }
     * ```
     */
    fun getAllManga(): Flow<List<Manga>> = mangaRepository.getAllManga()
}
```

### Compose UI 规范

#### 1. Composable 函数命名
```kotlin
// Composable函数使用PascalCase
@Composable
fun BookshelfScreen(
    modifier: Modifier = Modifier,
    onComicClick: (Manga) -> Unit = {}
) {
    // UI实现
}

// 预览函数
@Preview
@Composable
private fun BookshelfScreenPreview() {
    EasyComicTheme {
        BookshelfScreen()
    }
}
```

#### 2. 状态管理
```kotlin
// 使用remember和mutableStateOf管理本地状态
@Composable
fun SearchBar(
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    
    OutlinedTextField(
        value = searchQuery,
        onValueChange = { newQuery ->
            searchQuery = newQuery
            onSearchQueryChange(newQuery)
        },
        modifier = modifier
    )
}

// 使用ViewModel管理复杂状态
@Composable
fun BookshelfScreen(
    viewModel: BookshelfViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    when {
        uiState.isLoading -> LoadingIndicator()
        uiState.isError -> ErrorMessage(uiState.errorMessage)
        else -> BookshelfContent(uiState.mangaList)
    }
}
```

#### 3. 主题和样式
```kotlin
// 使用Material Design 3主题
@Composable
fun ComicCard(
    manga: Manga,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        // 卡片内容
    }
}
```

### 依赖注入规范

#### Koin 配置
```kotlin
// 模块定义
val domainModule = module {
    // 使用factory创建每次都是新实例的对象
    factory { MangaUseCases(get()) }
    
    // 使用single创建单例对象
    single<MangaRepository> { MangaRepositoryImpl(get(), get()) }
    
    // 使用scoped创建作用域内单例
    scope<BookshelfViewModel> {
        scoped { BookshelfViewModel(get()) }
    }
}

// 模块组织
val appModules = listOf(
    domainModule,
    dataModule,
    uiModule
)
```

## 开发流程

### Git 工作流

#### 分支命名规范
```bash
# 功能分支
feature/manga-reader-optimization
feature/theme-system

# 修复分支
bugfix/memory-leak-fix
hotfix/crash-on-startup

# 发布分支
release/v1.0.0
```

#### 提交信息规范
```bash
# 格式：<type>(<scope>): <description>
feat(reader): add zoom functionality
fix(bookshelf): resolve memory leak in comic loading
docs(architecture): update clean architecture documentation
test(domain): add unit tests for MangaUseCases
refactor(data): optimize repository caching strategy
```

### 代码审查清单

#### 功能性检查
- [ ] 功能是否按需求正确实现
- [ ] 边界条件是否正确处理
- [ ] 错误处理是否完善
- [ ] 性能是否满足要求

#### 代码质量检查
- [ ] 代码是否遵循项目规范
- [ ] 是否有重复代码
- [ ] 函数是否过于复杂
- [ ] 变量命名是否清晰

#### 架构检查
- [ ] 是否遵循Clean Architecture原则
- [ ] 依赖方向是否正确
- [ ] 模块职责是否清晰
- [ ] 接口设计是否合理

## 测试指南

### 测试策略

#### 1. 单元测试 (70%)
```kotlin
// Domain层测试
class MangaUseCasesTest {
    
    private val mockRepository = mockk<MangaRepository>()
    private val mangaUseCases = MangaUseCases(mockRepository)
    
    @Test
    fun `getAllManga should return manga list from repository`() = runTest {
        // Given
        val expectedManga = listOf(
            Manga(id = 1, title = "Test Manga")
        )
        every { mockRepository.getAllManga() } returns flowOf(expectedManga)
        
        // When
        val result = mangaUseCases.getAllManga().first()
        
        // Then
        assertThat(result).isEqualTo(expectedManga)
        verify { mockRepository.getAllManga() }
    }
}
```

#### 2. 集成测试 (20%)
```kotlin
// Repository集成测试
@RunWith(AndroidJUnit4::class)
class MangaRepositoryImplTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private lateinit var database: AppDatabase
    private lateinit var repository: MangaRepositoryImpl
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        repository = MangaRepositoryImpl(
            mangaDao = database.mangaDao(),
            comicParserFactory = mockk()
        )
    }
    
    @Test
    fun insertAndRetrieveManga() = runTest {
        // Given
        val manga = Manga(title = "Test Manga", filePath = "/test/path")
        
        // When
        val insertedId = repository.insertOrUpdateManga(manga)
        val retrievedManga = repository.getMangaById(insertedId)
        
        // Then
        assertThat(retrievedManga?.title).isEqualTo("Test Manga")
    }
}
```

#### 3. UI测试 (10%)
```kotlin
// Compose UI测试
class BookshelfScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun bookshelfScreen_displaysComics() {
        // Given
        val testComics = listOf(
            Manga(id = 1, title = "Test Comic 1"),
            Manga(id = 2, title = "Test Comic 2")
        )
        
        // When
        composeTestRule.setContent {
            EasyComicTheme {
                BookshelfScreen(
                    comics = testComics,
                    onComicClick = {}
                )
            }
        }
        
        // Then
        composeTestRule
            .onNodeWithText("Test Comic 1")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Test Comic 2")
            .assertIsDisplayed()
    }
}
```

### 测试工具配置

#### 依赖配置
```kotlin
// build.gradle.kts
dependencies {
    // 单元测试
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("com.google.truth:truth:1.1.4")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    
    // Android测试
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    
    // 测试工具
    testImplementation("app.cash.turbine:turbine:1.0.0")
    androidTestImplementation("androidx.room:room-testing:2.6.1")
}
```

## 性能优化指南

### 内存优化

#### 1. 图片加载优化
```kotlin
// 使用Coil进行图片加载优化
@Composable
fun ComicImage(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build(),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}
```

#### 2. 对象池管理
```kotlin
class ObjectPoolManager {
    private val bitmapPool = Pools.SynchronizedPool<Bitmap>(10)
    
    fun acquireBitmap(width: Int, height: Int): Bitmap {
        return bitmapPool.acquire() 
            ?: Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }
    
    fun releaseBitmap(bitmap: Bitmap) {
        bitmap.recycle()
        bitmapPool.release(bitmap)
    }
}
```

### 网络优化

#### 缓存策略
```kotlin
class ThreeLevelCacheRepository<T> {
    private val memoryCache = LruCache<String, T>(maxSize)
    private val diskCache = DiskLruCache.open(cacheDir, version, valueCount, maxSize)
    
    suspend fun getData(key: String): T? {
        // 1. 检查内存缓存
        memoryCache[key]?.let { return it }
        
        // 2. 检查磁盘缓存
        diskCache.get(key)?.let { data ->
            memoryCache.put(key, data)
            return data
        }
        
        // 3. 从网络获取
        return fetchFromNetwork(key)?.also { data ->
            memoryCache.