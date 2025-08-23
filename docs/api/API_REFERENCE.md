# 📚 Easy Comic API 参考文档

## 📋 概述

Easy Comic 项目的 API 参考文档，涵盖核心接口和使用方法。

## 🏗️ 核心 API

### Domain 层 API

#### Use Cases

##### GetMangaListUseCase

```kotlin
class GetMangaListUseCase(
    private val repository: MangaRepository
) : BaseUseCase<GetMangaListUseCase.Params, List<Manga>>() {

    override suspend fun execute(params: Params): List<Manga>

    data class Params(
        val query: String = "",
        val sortBy: SortOrder = SortOrder.NAME,
        val filterFavorites: Boolean = false
    )
}
```

##### ImportMangaUseCase

```kotlin
class ImportMangaUseCase(
    private val repository: MangaRepository,
    private val parserFactory: ComicParserFactory
) : BaseUseCase<ImportMangaUseCase.Params, ImportResult>() {

    override suspend fun execute(params: Params): ImportResult

    data class Params(
        val filePath: String,
        val fileName: String = "",
        val overwriteExisting: Boolean = false
    )
}
```

#### 数据模型

##### Manga

```kotlin
data class Manga(
    val id: String,
    val title: String,
    val filePath: String,
    val coverImagePath: String?,
    val totalPages: Int,
    val currentPage: Int = 0,
    val isFavorite: Boolean = false,
    val lastReadTime: Long = 0L,
    val readingStatus: ReadingStatus = ReadingStatus.NOT_STARTED
) {
    fun getReadingProgress(): Float
    fun isCompleted(): Boolean
    fun isStarted(): Boolean
}
```

#### Repository 接口

##### MangaRepository

```kotlin
interface MangaRepository {
    suspend fun getMangaList(query: String = "", sortBy: SortOrder = SortOrder.NAME): List<Manga>
    suspend fun getMangaById(id: String): Manga?
    suspend fun insertManga(manga: Manga)
    suspend fun updateManga(manga: Manga)
    suspend fun deleteManga(id: String)
    fun observeMangaList(): Flow<List<Manga>>
}
```

##### ComicParser

```kotlin
interface ComicParser {
    suspend fun getPageCount(filePath: String): Int
    suspend fun getPageImage(filePath: String, pageIndex: Int): ByteArray?
    suspend fun getAllPages(filePath: String): List<String>
    suspend fun getCoverImage(filePath: String): ByteArray?
    fun getSupportedFormats(): List<String>
}
```

### Data 层 API

#### 数据库实体

##### MangaEntity

```kotlin
@Entity(tableName = "manga")
data class MangaEntity(
    @PrimaryKey val id: String,
    val title: String,
    val filePath: String,
    val coverImagePath: String?,
    val totalPages: Int,
    val currentPage: Int,
    val isFavorite: Boolean,
    val lastReadTime: Long,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomainModel(): Manga
}
```

#### DAO 接口

##### MangaDao

```kotlin
@Dao
interface MangaDao {
    @Query("SELECT * FROM manga ORDER BY title ASC")
    suspend fun getAllManga(): List<MangaEntity>

    @Query("SELECT * FROM manga ORDER BY title ASC")
    fun observeAllManga(): Flow<List<MangaEntity>>

    @Query("SELECT * FROM manga WHERE title LIKE '%' || :query || '%'")
    suspend fun searchManga(query: String): List<MangaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManga(manga: MangaEntity)

    @Update
    suspend fun updateManga(manga: MangaEntity)

    @Delete
    suspend fun deleteManga(manga: MangaEntity)
}
```

### Presentation 层 API

#### UI 状态

##### BookshelfUiState

```kotlin
data class BookshelfUiState(
    val mangaList: List<Manga> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.NAME,
    val selectedMangaIds: Set<String> = emptySet()
)
```

##### ReaderUiState

```kotlin
data class ReaderUiState(
    val manga: Manga? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val isLoading: Boolean = true,
    val currentPageImage: ByteArray? = null,
    val isMenuVisible: Boolean = false,
    val zoomLevel: Float = 1.0f
)
```

## 🛠️ 工具类 API

### 性能监控

##### PerformanceTracker

```kotlin
object PerformanceTracker {
    fun startTrace(traceName: String): PerformanceTrace
    fun recordMetric(metricName: String, value: Long)
    fun getPerformanceReport(): PerformanceReport
}
```

### 国际化支持

##### LocalizationManager

```kotlin
object LocalizationManager {
    fun getCurrentLanguage(context: Context): SupportedLanguage
    fun setLanguage(context: Context, language: SupportedLanguage)
    fun createLocalizedContext(context: Context, language: SupportedLanguage): Context
}
```

### 无障碍支持

##### AccessibilityManager

```kotlin
object AccessibilityManager {
    fun isAccessibilityEnabled(context: Context): Boolean
    fun isTalkBackEnabled(context: Context): Boolean
    fun getFontScale(context: Context): FontScale
    fun setFontScale(context: Context, fontScale: FontScale)
}
```

## 📝 使用示例

### 基本用法

```kotlin
// 获取漫画列表
val mangaList = getMangaListUseCase.execute(
    GetMangaListUseCase.Params(query = "海贼王")
)

// 导入漫画
val result = importMangaUseCase.execute(
    ImportMangaUseCase.Params(filePath = "/path/to/manga.zip")
)

// 性能跟踪
val trace = PerformanceTracker.startTrace("manga_loading")
// ... 执行操作
trace.stop()

// 国际化
LocalizationManager.setLanguage(context, SupportedLanguage.SIMPLIFIED_CHINESE)
```

### ViewModel 使用

```kotlin
class BookshelfViewModel(
    private val mangaUseCases: MangaUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookshelfUiState())
    val uiState: StateFlow<BookshelfUiState> = _uiState.asStateFlow()

    fun loadMangaList() {
        viewModelScope.launch {
            try {
                val mangaList = mangaUseCases.getMangaList(
                    GetMangaListUseCase.Params()
                )
                _uiState.update { it.copy(mangaList = mangaList, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}
```

## 🔗 相关资源

- [开发者指南](DEVELOPER_GUIDE.md)
- [架构设计文档](../architecture/ARCHITECTURE_DESIGN.md)
- [贡献指南](../../CONTRIBUTING.md)
- [GitHub 项目](https://github.com/Alice-easy/Easy-Comic)
