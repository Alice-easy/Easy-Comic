package com.easycomic.di

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.easycomic.data.dao.BookmarkDao
import com.easycomic.data.dao.MangaDao
import com.easycomic.data.dao.ReadingHistoryDao
import com.easycomic.data.database.AppDatabase
import com.easycomic.data.repository.BookmarkRepositoryImpl
import com.easycomic.data.repository.MangaRepositoryImpl
import com.easycomic.data.repository.ReadingHistoryRepositoryImpl
import com.easycomic.data.service.ComicImportService
import com.easycomic.domain.repository.BookmarkRepository
import com.easycomic.domain.repository.MangaRepository
import com.easycomic.domain.repository.ReadingHistoryRepository
import com.easycomic.domain.service.BookshelfService
import com.easycomic.domain.usecase.manga.*
import com.easycomic.ui.bookshelf.BookshelfViewModel
import com.easycomic.ui.reader.ReaderViewModel
import io.insertkoin.koin.test.KoinTest
import io.insertkoin.koin.test.checkModules
import io.insertkoin.koin.test.get
import io.insertkoin.koin.test.inject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Koin 模块配置测试
 * 验证所有依赖注入配置是否正确
 */
@RunWith(MockitoJUnitRunner::class)
class KoinModulesTest : KoinTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockDatabase: AppDatabase

    @Mock
    private lateinit var mockMangaDao: MangaDao

    @Mock
    private lateinit var mockBookmarkDao: BookmarkDao

    @Mock
    private lateinit var mockReadingHistoryDao: ReadingHistoryDao

    @Mock
    private lateinit var mockComicImportService: ComicImportService

    @Before
    fun setup() {
        // 模拟 DependencyContainer 的行为
        whenever(DependencyContainer.getDatabase()).thenReturn(mockDatabase)
        whenever(mockDatabase.mangaDao()).thenReturn(mockMangaDao)
        whenever(mockDatabase.bookmarkDao()).thenReturn(mockBookmarkDao)
        whenever(mockDatabase.readingHistoryDao()).thenReturn(mockReadingHistoryDao)
        whenever(DependencyContainer.getComicImportService()).thenReturn(mockComicImportService)
        
        // 初始化 Koin
        KoinModules.initializeKoin(mockContext)
    }

    @After
    fun tearDown() {
        // 清理 Koin
        io.insertkoin.koin.core.context.stopKoin()
    }

    @Test
    fun `test koin modules are properly configured`() {
        // 检查所有模块是否正确配置
        checkModules {
            modules(KoinModules.getAllModules())
        }
    }

    @Test
    fun `test database dependency injection`() {
        val database: AppDatabase = get()
        assertNotNull(database)
        assertTrue(database is AppDatabase)
    }

    @Test
    fun `test dao dependencies injection`() {
        val mangaDao: MangaDao = get()
        val bookmarkDao: BookmarkDao = get()
        val readingHistoryDao: ReadingHistoryDao = get()

        assertNotNull(mangaDao)
        assertNotNull(bookmarkDao)
        assertNotNull(readingHistoryDao)
        assertTrue(mangaDao is MangaDao)
        assertTrue(bookmarkDao is BookmarkDao)
        assertTrue(readingHistoryDao is ReadingHistoryDao)
    }

    @Test
    fun `test repository dependencies injection`() {
        val mangaRepository: MangaRepository = get()
        val bookmarkRepository: BookmarkRepository = get()
        val readingHistoryRepository: ReadingHistoryRepository = get()

        assertNotNull(mangaRepository)
        assertNotNull(bookmarkRepository)
        assertNotNull(readingHistoryRepository)
        assertTrue(mangaRepository is MangaRepositoryImpl)
        assertTrue(bookmarkRepository is BookmarkRepositoryImpl)
        assertTrue(readingHistoryRepository is ReadingHistoryRepositoryImpl)
    }

    @Test
    fun `test service dependencies injection`() {
        val comicImportService: ComicImportService = get()
        assertNotNull(comicImportService)
        assertTrue(comicImportService is ComicImportService)
    }

    @Test
    fun `test bookshelf service injection`() {
        val bookshelfService: BookshelfService = get()
        assertNotNull(bookshelfService)
        assertTrue(bookshelfService is BookshelfService)
    }

    @Test
    fun `test use case dependencies injection`() {
        val getAllMangaUseCase: GetAllMangaUseCase = get()
        val getMangaByIdUseCase: GetMangaByIdUseCase = get()
        val searchMangaUseCase: SearchMangaUseCase = get()
        val getFavoriteMangaUseCase: GetFavoriteMangaUseCase = get()
        val getRecentMangaUseCase: GetRecentMangaUseCase = get()
        val importComicUseCase: ImportComicUseCase = get()
        val batchImportComicsUseCase: BatchImportComicsUseCase = get()

        assertNotNull(getAllMangaUseCase)
        assertNotNull(getMangaByIdUseCase)
        assertNotNull(searchMangaUseCase)
        assertNotNull(getFavoriteMangaUseCase)
        assertNotNull(getRecentMangaUseCase)
        assertNotNull(importComicUseCase)
        assertNotNull(batchImportComicsUseCase)
    }

    @Test
    fun `test viewmodel dependencies injection`() {
        val bookshelfViewModel: BookshelfViewModel by inject()
        val readerViewModel: ReaderViewModel by inject()

        assertNotNull(bookshelfViewModel)
        assertNotNull(readerViewModel)
        assertTrue(bookshelfViewModel is BookshelfViewModel)
        assertTrue(readerViewModel is ReaderViewModel)
    }

    @Test
    fun `test dependency container compatibility`() {
        // 测试 DependencyContainer 是否与 Koin 兼容
        val databaseFromContainer = DependencyContainer.getDatabase()
        val databaseFromKoin: AppDatabase = get()

        assertNotNull(databaseFromContainer)
        assertNotNull(databaseFromKoin)
        // 由于我们使用 mock，它们应该是同一个实例
        assertTrue(databaseFromContainer === databaseFromKoin)
    }

    @Test
    fun `test all modules are included`() {
        val modules = KoinModules.getAllModules()
        assertTrue(modules.size >= 3) // dataModule, domainModule, uiModule
        
        val moduleNames = modules.map { it.toString() }
        assertTrue(moduleNames.any { it.contains("data", ignoreCase = true) })
        assertTrue(moduleNames.any { it.contains("domain", ignoreCase = true) })
        assertTrue(moduleNames.any { it.contains("ui", ignoreCase = true) })
    }
}