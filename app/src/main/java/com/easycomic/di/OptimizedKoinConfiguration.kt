package com.easycomic.di

import android.content.Context
import com.easycomic.data.di.optimizedDataModule
import com.easycomic.domain.di.domainModule
import com.easycomic.ui.di.uiModule
import com.easycomic.ui_bookshelf.di.bookshelfModule
import com.easycomic.ui_reader.di.readerModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.module.Module
import org.koin.core.qualifier.named

/**
 * 优化后的Koin依赖注入配置
 * 
 * 优化点：
 * 1. 模块化管理和分层配置
 * 2. 环境区分（开发/生产）
 * 3. 依赖验证和性能监控
 * 4. 统一的配置管理
 */
object OptimizedKoinConfiguration {
    
    /**
     * 应用环境枚举
     */
    enum class Environment {
        DEVELOPMENT,
        PRODUCTION,
        TESTING
    }
    
    /**
     * 获取所有模块（按依赖层次排序）
     */
    fun getAllModules(environment: Environment = Environment.PRODUCTION): List<Module> {
        return when (environment) {
            Environment.DEVELOPMENT -> getDevelopmentModules()
            Environment.PRODUCTION -> getProductionModules()
            Environment.TESTING -> getTestingModules()
        }
    }
    
    /**
     * 生产环境模块配置
     */
    private fun getProductionModules(): List<Module> {
        return listOf(
            // 核心层（最底层）
            com.easycomic.data.di.productionDataModule,
            
            // 业务层
            domainModule,
            
            // 表现层
            uiModule,
            bookshelfModule,
            readerModule,
            
            // 应用层配置
            applicationModule
        )
    }
    
    /**
     * 开发环境模块配置（包含调试工具）
     */
    private fun getDevelopmentModules(): List<Module> {
        return listOf(
            // 核心层（开发环境配置）
            com.easycomic.data.di.developmentDataModule,
            
            // 业务层
            domainModule,
            
            // 表现层
            uiModule,
            bookshelfModule,
            readerModule,
            
            // 应用层配置
            applicationModule,
            developmentModule
        )
    }
    
    /**
     * 测试环境模块配置
     */
    private fun getTestingModules(): List<Module> {
        return listOf(
            testDataModule,
            domainModule,
            testUiModule
        )
    }
    
    /**
     * 初始化Koin（优化版）
     */
    fun initializeKoin(
        context: Context,
        environment: Environment = Environment.PRODUCTION
    ) {
        startKoin {
            // Android上下文
            androidContext(context)
            
            // 日志配置
            androidLogger(
                when (environment) {
                    Environment.DEVELOPMENT -> Level.DEBUG
                    Environment.PRODUCTION -> Level.ERROR
                    Environment.TESTING -> Level.NONE
                }
            )
            
            // 模块配置
            modules(getAllModules(environment))
            
            // 性能监控（仅开发环境）
            if (environment == Environment.DEVELOPMENT) {
                printLogger(Level.DEBUG)
            }
        }
        
        // 依赖验证（仅开发环境）
        if (environment == Environment.DEVELOPMENT) {
            validateDependencies()
        }
    }
    
    /**
     * 依赖验证
     */
    private fun validateDependencies() {
        try {
            // 验证关键依赖是否正确注入
            org.koin.core.context.GlobalContext.get().apply {
                // 验证数据层
                get<com.easycomic.domain.repository.MangaRepository>()
                get<com.easycomic.domain.repository.BookmarkRepository>()
                get<com.easycomic.domain.repository.ThemeRepository>()
                
                // 验证业务层
                get<com.easycomic.domain.usecase.MangaUseCases>()
                get<com.easycomic.domain.usecase.ThemeUseCases>()
                
                println("✅ Koin依赖验证通过")
            }
        } catch (e: Exception) {
            println("❌ Koin依赖验证失败: ${e.message}")
            throw e
        }
    }
    
    /**
     * 获取依赖统计信息
     */
    fun getDependencyStats(): DependencyStats {
        val koin = org.koin.core.context.GlobalContext.get()
        val registry = koin.instanceRegistry
        
        return DependencyStats(
            totalDefinitions = registry.size(),
            singletonCount = registry.instances.size,
            factoryCount = registry.size() - registry.instances.size
        )
    }
    
    /**
     * 依赖统计数据类
     */
    data class DependencyStats(
        val totalDefinitions: Int,
        val singletonCount: Int,
        val factoryCount: Int
    ) {
        override fun toString(): String {
            return """
                📊 Koin依赖统计:
                - 总定义数: $totalDefinitions
                - 单例数: $singletonCount  
                - 工厂数: $factoryCount
            """.trimIndent()
        }
    }
}

/**
 * 应用层模块
 */
private val applicationModule = org.koin.dsl.module {
    
    // 应用配置
    single(named("app_version")) { "1.0.0" }
    single(named("debug_mode")) { false }
    
    // 性能监控
    single { 
        com.easycomic.performance.PerformanceTracker()
    }
}

/**
 * 开发环境专用模块
 */
private val developmentModule = org.koin.dsl.module {
    
    // 调试工具
    single(named("debug_mode")) { true }
    
    // 开发工具
    factory { 
        DevelopmentTools(get())
    }
}

/**
 * 测试数据模块
 */
private val testDataModule = org.koin.dsl.module {
    
    // 测试用的Mock Repository
    single<com.easycomic.domain.repository.MangaRepository> { 
        MockMangaRepository() 
    }
    
    single<com.easycomic.domain.repository.BookmarkRepository> { 
        MockBookmarkRepository() 
    }
    
    single<com.easycomic.domain.repository.ThemeRepository> { 
        MockThemeRepository() 
    }
}

/**
 * 测试UI模块
 */
private val testUiModule = org.koin.dsl.module {
    
    // 测试用的ViewModel
    org.koin.androidx.viewmodel.dsl.viewModel { 
        TestViewModel(get(), get()) 
    }
}

/**
 * 开发工具类
 */
class DevelopmentTools(
    private val performanceTracker: com.easycomic.performance.PerformanceTracker
) {
    
    fun logDependencyInjectionTime() {
        performanceTracker.logEvent("dependency_injection_completed")
    }
    
    fun validateAllDependencies() {
        OptimizedKoinConfiguration.validateDependencies()
    }
}

// Mock类（用于测试）
class MockMangaRepository : com.easycomic.domain.repository.MangaRepository {
    // 实现所有接口方法的Mock版本
    override fun getAllManga() = kotlinx.coroutines.flow.flowOf(emptyList<com.easycomic.domain.model.Manga>())
    override suspend fun getMangaById(id: Long) = null
    override suspend fun getMangaByFilePath(filePath: String) = null
    override fun searchManga(query: String) = kotlinx.coroutines.flow.flowOf(emptyList<com.easycomic.domain.model.Manga>())
    override fun getFavoriteManga() = kotlinx.coroutines.flow.flowOf(emptyList<com.easycomic.domain.model.Manga>())
    override fun getMangaByStatus(status: com.easycomic.domain.model.ReadingStatus) = kotlinx.coroutines.flow.flowOf(emptyList<com.easycomic.domain.model.Manga>())
    override fun getRecentManga(limit: Int) = kotlinx.coroutines.flow.flowOf(emptyList<com.easycomic.domain.model.Manga>())
    override suspend fun insertOrUpdateManga(manga: com.easycomic.domain.model.Manga) = 0L
    override suspend fun updateManga(manga: com.easycomic.domain.model.Manga) = 0L
    override suspend fun insertAllManga(mangaList: List<com.easycomic.domain.model.Manga>) = emptyList<Long>()
    override suspend fun updateReadingProgress(mangaId: Long, currentPage: Int, status: com.easycomic.domain.model.ReadingStatus) {}
    override suspend fun toggleFavorite(mangaId: Long) {}
    override suspend fun updateRating(mangaId: Long, rating: Float) {}
    override suspend fun deleteManga(manga: com.easycomic.domain.model.Manga) {}
    override suspend fun deleteAllManga(mangaList: List<com.easycomic.domain.model.Manga>) {}
    override fun getMangaCount() = kotlinx.coroutines.flow.flowOf(0)
    override fun getFavoriteCount() = kotlinx.coroutines.flow.flowOf(0)
    override fun getCompletedCount() = kotlinx.coroutines.flow.flowOf(0)
    override suspend fun getCover(manga: com.easycomic.domain.model.Manga) = null
}

class MockBookmarkRepository : com.easycomic.domain.repository.BookmarkRepository {
    // 实现所有接口方法的Mock版本
    override fun getBookmarksByMangaId(mangaId: Long) = kotlinx.coroutines.flow.flowOf(emptyList<com.easycomic.domain.model.Bookmark>())
    override suspend fun getBookmarkByPage(mangaId: Long, pageNumber: Int) = emptyList<com.easycomic.domain.model.Bookmark>()
    override suspend fun getBookmarkById(bookmarkId: Long) = null
    override suspend fun hasBookmarkForPage(mangaId: Long, pageNumber: Int) = false
    override suspend fun addBookmark(bookmark: com.easycomic.domain.model.Bookmark) = 0L
    override suspend fun updateBookmark(bookmark: com.easycomic.domain.model.Bookmark) {}
    override suspend fun deleteBookmark(bookmark: com.easycomic.domain.model.Bookmark) {}
    override suspend fun deleteBookmarkById(bookmarkId: Long) {}
    override suspend fun deleteBookmarksByMangaId(mangaId: Long) {}
    override suspend fun deleteBookmarkByPage(mangaId: Long, pageNumber: Int) {}
    override suspend fun deleteAllBookmarks(bookmarkList: List<com.easycomic.domain.model.Bookmark>) {}
    override fun getBookmarkCount() = kotlinx.coroutines.flow.flowOf(0)
    override suspend fun getBookmarkCountByMangaId(mangaId: Long) = 0
    override fun getRecentBookmarks(limit: Int) = kotlinx.coroutines.flow.flowOf(emptyList<com.easycomic.domain.model.Bookmark>())
}

class MockThemeRepository : com.easycomic.domain.repository.ThemeRepository {
    // 实现所有接口方法的Mock版本
    override fun getThemePreference() = kotlinx.coroutines.flow.flowOf(
        com.easycomic.domain.model.ThemePreference()
    )
    override suspend fun updateThemeMode(themeMode: com.easycomic.domain.model.ThemeMode) {}
    override suspend fun updateDynamicColors(useDynamicColors: Boolean) {}
    override suspend fun updateCustomSeedColor(color: Long?) {}
    override suspend fun resetToDefault() {}
}

class TestViewModel(
    private val mangaUseCases: com.easycomic.domain.usecase.MangaUseCases,
    private val themeUseCases: com.easycomic.domain.usecase.ThemeUseCases
) : androidx.lifecycle.ViewModel()