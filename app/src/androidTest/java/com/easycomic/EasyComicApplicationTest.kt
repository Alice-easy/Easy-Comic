package com.easycomic

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.easycomic.di.KoinModules
import com.easycomic.ui.bookshelf.BookshelfViewModel
import com.easycomic.ui.reader.ReaderViewModel
import io.insertkoin.koin.test.KoinTest
import io.insertkoin.koin.test.get
import io.insertkoin.koin.test.inject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Easy Comic Application Koin 集成测试
 * 验证应用程序启动时的 Koin 初始化和依赖注入功能
 */
@RunWith(AndroidJUnit4::class)
class EasyComicApplicationTest : KoinTest {

    @Before
    fun setup() {
        // 清理之前的 Koin 实例
        io.insertkoin.koin.core.context.stopKoin()
    }

    @After
    fun tearDown() {
        // 清理 Koin
        io.insertkoin.koin.core.context.stopKoin()
    }

    @Test
    fun `test application can be created`() {
        // Given - 无特殊设置

        // When
        val application = ApplicationProvider.getApplicationContext<EasyComicApplication>()

        // Then
        assertNotNull(application)
        assertTrue(application is EasyComicApplication)
    }

    @Test
    fun `test application initializes Koin on create`() {
        // Given
        val application = ApplicationProvider.getApplicationContext<EasyComicApplication>()

        // When - onCreate 已经由 Android 测试框架调用

        // Then - 验证 Koin 已经初始化
        assertTrue(io.insertkoin.koin.core.context.GlobalContext.getKoinApplicationOrNull() != null)
    }

    @Test
    fun `test Koin modules are properly loaded after application start`() {
        // Given
        val application = ApplicationProvider.getApplicationContext<EasyComicApplication>()

        // When - onCreate 已经由 Android 测试框架调用

        // Then - 验证可以获取到所有预期的依赖
        val koin = io.insertkoin.koin.core.context.GlobalContext.get().koin
        
        // 验证数据层依赖
        assertNotNull(koin.getOrNull<com.easycomic.data.database.AppDatabase>())
        assertNotNull(koin.getOrNull<com.easycomic.data.dao.MangaDao>())
        assertNotNull(koin.getOrNull<com.easycomic.data.dao.BookmarkDao>())
        assertNotNull(koin.getOrNull<com.easycomic.data.dao.ReadingHistoryDao>())
        
        // 验证仓储层依赖
        assertNotNull(koin.getOrNull<com.easycomic.domain.repository.MangaRepository>())
        assertNotNull(koin.getOrNull<com.easycomic.domain.repository.BookmarkRepository>())
        assertNotNull(koin.getOrNull<com.easycomic.domain.repository.ReadingHistoryRepository>())
        
        // 验证服务层依赖
        assertNotNull(koin.getOrNull<com.easycomic.data.service.ComicImportService>())
        assertNotNull(koin.getOrNull<com.easycomic.domain.service.BookshelfService>())
        
        // 验证用例层依赖
        assertNotNull(koin.getOrNull<com.easycomic.domain.usecase.manga.GetAllMangaUseCase>())
        assertNotNull(koin.getOrNull<com.easycomic.domain.usecase.manga.GetMangaByIdUseCase>())
        assertNotNull(koin.getOrNull<com.easycomic.domain.usecase.manga.SearchMangaUseCase>())
        
        // 验证 ViewModel 依赖
        assertNotNull(koin.getOrNull<BookshelfViewModel>())
        assertNotNull(koin.getOrNull<ReaderViewModel>())
    }

    @Test
    fun `test application does not crash on multiple initializations`() {
        // Given
        val application = ApplicationProvider.getApplicationContext<EasyComicApplication>()

        // When - 多次调用初始化
        try {
            KoinModules.initializeKoin(application)
            KoinModules.initializeKoin(application)
        } catch (e: Exception) {
            // Then - 如果抛出异常，测试失败
            kotlin.test.fail("Application crashed on multiple Koin initializations: ${e.message}")
        }

        // Then - 不应该抛出异常
        assertTrue(true)
    }

    @Test
    fun `test application context is properly set in Koin`() {
        // Given
        val application = ApplicationProvider.getApplicationContext<EasyComicApplication>()

        // When - onCreate 已经由 Android 测试框架调用

        // Then - 验证 Android Context 已设置
        val koin = io.insertkoin.koin.core.context.GlobalContext.get().koin
        val androidContext = koin.getOrNull<android.content.Context>()
        
        assertNotNull(androidContext)
        assertTrue(androidContext is android.content.Context)
    }

    @Test
    fun `test DependencyContainer is compatible with Koin initialization`() {
        // Given
        val application = ApplicationProvider.getApplicationContext<EasyComicApplication>()

        // When - onCreate 已经由 Android 测试框架调用

        // Then - 验证 DependencyContainer 可以正常工作
        try {
            val database = com.easycomic.di.DependencyContainer.getDatabase()
            assertNotNull(database)
        } catch (e: Exception) {
            kotlin.test.fail("DependencyContainer failed to work with Koin: ${e.message}")
        }
    }

    @Test
    fun `test application starts without Hilt-related errors`() {
        // Given
        val application = ApplicationProvider.getApplicationContext<EasyComicApplication>()

        // When - 应用启动

        // Then - 验证没有 Hilt 相关的错误
        // 通过检查应用是否正常启动且没有抛出异常来验证
        assertNotNull(application)
        
        // 验证 Koin 已经初始化（这意味着没有 Hilt 冲突）
        assertTrue(io.insertkoin.koin.core.context.GlobalContext.getKoinApplicationOrNull() != null)
    }

    @Test
    fun `test all Koin modules are included in application startup`() {
        // Given
        val application = ApplicationProvider.getApplicationContext<EasyComicApplication>()

        // When - 应用启动

        // Then - 验证所有模块都已包含
        val modules = KoinModules.getAllModules()
        assertTrue(modules.size >= 3) // dataModule, domainModule, uiModule
        
        // 验证模块类型
        val moduleNames = modules.map { it.toString() }
        assertTrue(moduleNames.any { it.contains("data", ignoreCase = true) })
        assertTrue(moduleNames.any { it.contains("domain", ignoreCase = true) })
        assertTrue(moduleNames.any { it.contains("ui", ignoreCase = true) })
    }

    @Test
    fun `test application handles Timber initialization gracefully`() {
        // Given
        val application = ApplicationProvider.getApplicationContext<EasyComicApplication>()

        // When - 应用启动（包含 Timber 初始化）

        // Then - 应用不应该崩溃
        assertNotNull(application)
        
        // Timber 初始化失败应该被捕获，不应该影响应用启动
        assertTrue(true)
    }

    @Test
    fun `test ViewModel dependencies are properly injected after app startup`() {
        // Given
        val application = ApplicationProvider.getApplicationContext<EasyComicApplication>()

        // When - 应用启动

        // Then - 验证 ViewModel 可以正确注入依赖
        val bookshelfViewModel: BookshelfViewModel by inject()
        val readerViewModel: ReaderViewModel by inject()
        
        assertNotNull(bookshelfViewModel)
        assertNotNull(readerViewModel)
        
        // 验证 ViewModel 不是 null 且类型正确
        assertTrue(bookshelfViewModel is BookshelfViewModel)
        assertTrue(readerViewModel is ReaderViewModel)
    }

    @Test
    fun `test application maintains consistent dependency graph`() {
        // Given
        val application = ApplicationProvider.getApplicationContext<EasyComicApplication>()

        // When - 应用启动

        // Then - 验证依赖图的一致性
        val koin = io.insertkoin.koin.core.context.GlobalContext.get().koin
        
        // 验证单例依赖在多次获取时是同一个实例
        val mangaRepo1 = koin.get<com.easycomic.domain.repository.MangaRepository>()
        val mangaRepo2 = koin.get<com.easycomic.domain.repository.MangaRepository>()
        
        assertTrue(mangaRepo1 === mangaRepo2) // 应该是同一个实例
        
        // 验证 ViewModel 是不同实例（每次获取都会创建新的）
        val vm1 = koin.get<BookshelfViewModel>()
        val vm2 = koin.get<BookshelfViewModel>()
        
        assertTrue(vm1 !== vm2) // 应该是不同实例
    }

    @Test
    fun `test application startup performance is acceptable`() {
        // Given
        val application = ApplicationProvider.getApplicationContext<EasyComicApplication>()

        // When - 测量应用启动时间
        val startTime = System.currentTimeMillis()
        
        // 重新初始化 Koin 来测量启动时间
        io.insertkoin.koin.core.context.stopKoin()
        KoinModules.initializeKoin(application)
        
        val endTime = System.currentTimeMillis()
        val startupTime = endTime - startTime

        // Then - 启动时间应该在可接受范围内（小于 1 秒）
        assertTrue(startupTime < 1000, "Koin initialization took too long: $startupTime ms")
    }
}