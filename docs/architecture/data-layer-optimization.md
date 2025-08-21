# Data层优化指南

## 📊 当前状态总结

Data层已完成90%的核心功能，主要包括：
- ✅ 完整的Room数据库架构
- ✅ 功能齐全的DAO接口 
- ✅ Repository模式实现
- ✅ ZIP/RAR文件解析器
- ✅ 依赖注入配置

## 🎯 待优化功能 (10%)

### 1. 封面提取算法实现

**当前状态**: `MangaRepositoryImpl.getCover()` 方法返回null

**优化方案**:
```kotlin
override suspend fun getCover(manga: Manga): Bitmap? = withContext(Dispatchers.IO) {
    try {
        val parser = ComicParserFactoryImpl().create(File(manga.filePath))
        parser?.use { p ->
            // 智能选择封面页 (通常是第一页)
            val coverStream = p.getPageStream(0)
            coverStream?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        }
    } catch (e: Exception) {
        Log.e("MangaRepository", "Failed to extract cover", e)
        null
    }
}
```

**实现要点**:
- 缓存提取的封面到本地存储
- 支持多种图片格式
- 处理损坏文件的异常情况
- 实现封面尺寸优化 (避免OOM)

### 2. 图片缓存管理策略

**当前问题**: 缺少图片缓存机制，可能导致重复加载

**优化方案**:
```kotlin
class CoverCacheManager(
    private val context: Context,
    private val maxCacheSize: Long = 100 * 1024 * 1024 // 100MB
) {
    private val cacheDir = File(context.cacheDir, "covers")
    
    suspend fun getCachedCover(mangaId: Long): File? {
        val cacheFile = File(cacheDir, "$mangaId.jpg")
        return if (cacheFile.exists()) cacheFile else null
    }
    
    suspend fun cacheCover(mangaId: Long, bitmap: Bitmap): File? {
        // 实现LRU缓存策略
        // 压缩图片到合适尺寸
        // 保存到缓存目录
    }
}
```

### 3. 大文件处理优化

**当前问题**: RarComicParser对大文件的内存占用过高

**优化方案**:
- 实现流式解压，避免一次性解压所有文件
- 按需解压页面，用完即删除
- 实现文件大小检查，超过阈值时使用不同策略

```kotlin
class OptimizedRarComicParser(
    private val file: File,
    private val maxMemoryUsage: Long = 200 * 1024 * 1024 // 200MB
) : ComicParser {
    
    private var isLargeFile = file.length() > maxMemoryUsage
    
    override fun getPageStream(pageIndex: Int): InputStream? {
        return if (isLargeFile) {
            // 流式解压单个文件
            extractSinglePage(pageIndex)
        } else {
            // 传统方式
            super.getPageStream(pageIndex)
        }
    }
}
```

### 4. 自然序排序算法

**当前问题**: 图片文件排序不符合自然顺序 ("Image 10.jpg" 排在 "Image 2.jpg" 前面)

**优化方案**:
```kotlin
private fun naturalSortComparator(): Comparator<String> {
    return Comparator { s1, s2 ->
        val regex = "\\d+".toRegex()
        val s1Parts = s1.split(regex)
        val s2Parts = s2.split(regex)
        val s1Numbers = regex.findAll(s1).map { it.value.toInt() }.toList()
        val s2Numbers = regex.findAll(s2).map { it.value.toInt() }.toList()
        
        // 比较文本部分和数字部分
        for (i in 0 until minOf(s1Parts.size, s2Parts.size)) {
            val textCompare = s1Parts[i].compareTo(s2Parts[i])
            if (textCompare != 0) return@Comparator textCompare
            
            if (i < s1Numbers.size && i < s2Numbers.size) {
                val numberCompare = s1Numbers[i].compareTo(s2Numbers[i])
                if (numberCompare != 0) return@Comparator numberCompare
            }
        }
        
        s1.length.compareTo(s2.length)
    }
}
```

### 5. 统一错误处理机制

**当前问题**: 各个组件的错误处理不统一

**优化方案**:
```kotlin
sealed class DataError : Exception() {
    object FileNotFound : DataError()
    object UnsupportedFormat : DataError()
    object CorruptedFile : DataError()
    object InsufficientMemory : DataError()
    data class DatabaseError(val cause: Throwable) : DataError()
}

class ErrorHandler {
    suspend fun <T> safeExecute(
        operation: suspend () -> T,
        onError: (DataError) -> T
    ): T {
        return try {
            operation()
        } catch (e: Exception) {
            val dataError = mapToDataError(e)
            onError(dataError)
        }
    }
}
```

## 🧪 测试覆盖增强

### 1. Repository单元测试
```kotlin
@Test
fun `test manga insertion and retrieval`() = runTest {
    // Given
    val manga = createTestManga()
    
    // When
    val insertId = repository.insertOrUpdateManga(manga)
    val retrieved = repository.getMangaById(insertId)
    
    // Then
    assertEquals(manga.title, retrieved?.title)
}
```

### 2. DAO集成测试
```kotlin
@Test
fun `test search functionality with complex query`() = runTest {
    // 测试搜索功能的准确性和性能
}
```

### 3. Parser性能测试
```kotlin
@Test
fun `test large file parsing performance`() = runTest {
    // 测试大文件解析的内存使用和速度
}
```

## 🚀 性能优化目标

### 数据库查询优化
- 目标响应时间: < 50ms
- 复杂查询优化: 使用合适的索引
- 分页查询: 避免一次性加载大量数据

### 文件解析性能
- ZIP解析速度: > 100MB/s
- RAR解析速度: > 50MB/s  
- 内存峰值: < 200MB (大文件场景)

### 缓存效率
- 封面缓存命中率: > 85%
- 缓存大小控制: < 100MB
- 缓存清理策略: LRU + 时间过期

## 📋 实施计划

### Week 1: 核心功能完善
- [ ] 实现封面提取算法
- [ ] 添加图片缓存管理
- [ ] 完善错误处理机制

### Week 2: 性能优化
- [ ] 优化大文件处理
- [ ] 实现自然序排序
- [ ] 增加单元测试覆盖

### Week 3: 测试与文档
- [ ] 性能基准测试
- [ ] 完善代码文档
- [ ] 集成测试覆盖

## 🔧 开发环境优化

### 构建配置优化
```kotlin
// data/build.gradle.kts
dependencies {
    // 测试依赖
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:4.6.1")
    testImplementation("androidx.room:room-testing:2.6.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    
    // 性能监控
    implementation("androidx.startup:startup-runtime:1.1.1")
}
```

### 代码质量工具
- 静态代码分析: Detekt
- 代码覆盖率: JaCoCo
- 性能监控: Memory Profiler

---

**更新日期**: 2025年8月21日
**下次评估**: 2025年9月1日
