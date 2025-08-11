package com.easycomic.di

import android.content.Context
import androidx.room.Room
import com.easycomic.data.dao.BookmarkDao
import com.easycomic.data.dao.MangaDao
import com.easycomic.data.dao.ReadingHistoryDao
import com.easycomic.data.database.AppDatabase
import com.easycomic.data.repository.BookmarkRepositoryImpl
import com.easycomic.data.repository.MangaRepositoryImpl
import com.easycomic.data.repository.ReadingHistoryRepositoryImpl
import com.easycomic.data.service.ComicImportService
import com.easycomic.data.util.FileHelper
import com.easycomic.domain.repository.BookmarkRepository
import com.easycomic.domain.repository.MangaRepository
import com.easycomic.domain.repository.ReadingHistoryRepository
import com.easycomic.domain.usecase.manga.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * DependencyContainer 测试
 * 验证手动依赖注入容器的功能
 */
@RunWith(MockitoJUnitRunner::class)
class DependencyContainerTest {

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

    @Before
    fun setup() {
        // 重置 DependencyContainer 状态
        clearDependencyContainer()
    }

    @After
    fun tearDown() {
        clearDependencyContainer()
    }

    @Test
    fun `test initialize with context`() {
        // Given
        whenever(mockContext.applicationContext).thenReturn(mockContext)

        // When
        DependencyContainer.initialize(mockContext)

        // Then - 不抛出异常即表示初始化成功
        assertTrue(true)
    }

    @Test
    fun `test database initialization`() {
        // Given
        whenever(mockContext.applicationContext).thenReturn(mockContext)
        whenever(mockDatabase.mangaDao()).thenReturn(mockMangaDao)
        whenever(mockDatabase.bookmarkDao()).thenReturn(mockBookmarkDao)
        whenever(mockDatabase.readingHistoryDao()).thenReturn(mockReadingHistoryDao)

        // When
        DependencyContainer.initialize(mockContext)
        val database = DependencyContainer.getDatabase()

        // Then
        assertNotNull(database)
        assertEquals(mockDatabase, database)
    }

    @Test
    fun `test repository initialization`() {
        // Given
        whenever(mockContext.applicationContext).thenReturn(mockContext)
        whenever(mockDatabase.mangaDao()).thenReturn(mockMangaDao)
        whenever(mockDatabase.bookmarkDao()).thenReturn(mockBookmarkDao)
        whenever(mockDatabase.readingHistoryDao()).thenReturn(mockReadingHistoryDao)

        // When
        DependencyContainer.initialize(mockContext)

        // Then
        val mangaRepository = DependencyContainer.getMangaRepository()
        val bookmarkRepository = DependencyContainer.getBookmarkRepository()
        val readingHistoryRepository = DependencyContainer.getReadingHistoryRepository()

        assertNotNull(mangaRepository)
        assertNotNull(bookmarkRepository)
        assertNotNull(readingHistoryRepository)
        assertTrue(mangaRepository is MangaRepositoryImpl)
        assertTrue(bookmarkRepository is BookmarkRepositoryImpl)
        assertTrue(readingHistoryRepository is ReadingHistoryRepositoryImpl)
    }

    @Test
    fun `test service initialization`() {
        // Given
        whenever(mockContext.applicationContext).thenReturn(mockContext)
        whenever(mockDatabase.mangaDao()).thenReturn(mockMangaDao)
        whenever(mockDatabase.bookmarkDao()).thenReturn(mockBookmarkDao)
        whenever(mockDatabase.readingHistoryDao()).thenReturn(mockReadingHistoryDao)

        // When
        DependencyContainer.initialize(mockContext)

        // Then
        val comicImportService = DependencyContainer.getComicImportService()
        assertNotNull(comicImportService)
        assertTrue(comicImportService is ComicImportService)
    }

    @Test
    fun `test use case initialization`() {
        // Given
        whenever(mockContext.applicationContext).thenReturn(mockContext)
        whenever(mockDatabase.mangaDao()).thenReturn(mockMangaDao)
        whenever(mockDatabase.bookmarkDao()).thenReturn(mockBookmarkDao)
        whenever(mockDatabase.readingHistoryDao()).thenReturn(mockReadingHistoryDao)

        // When
        DependencyContainer.initialize(mockContext)

        // Then
        val getAllMangaUseCase = DependencyContainer.getAllMangaUseCase()
        val getMangaByIdUseCase = DependencyContainer.getGetMangaByIdUseCase()
        val searchMangaUseCase = DependencyContainer.getSearchMangaUseCase()
        val getFavoriteMangaUseCase = DependencyContainer.getGetFavoriteMangaUseCase()
        val getRecentMangaUseCase = DependencyContainer.getGetRecentMangaUseCase()
        val importComicUseCase = DependencyContainer.getImportComicUseCase()

        assertNotNull(getAllMangaUseCase)
        assertNotNull(getMangaByIdUseCase)
        assertNotNull(searchMangaUseCase)
        assertNotNull(getFavoriteMangaUseCase)
        assertNotNull(getRecentMangaUseCase)
        assertNotNull(importComicUseCase)
    }

    @Test
    fun `test dependency container returns same instances`() {
        // Given
        whenever(mockContext.applicationContext).thenReturn(mockContext)
        whenever(mockDatabase.mangaDao()).thenReturn(mockMangaDao)
        whenever(mockDatabase.bookmarkDao()).thenReturn(mockBookmarkDao)
        whenever(mockDatabase.readingHistoryDao()).thenReturn(mockReadingHistoryDao)

        // When
        DependencyContainer.initialize(mockContext)

        // Then
        val mangaRepository1 = DependencyContainer.getMangaRepository()
        val mangaRepository2 = DependencyContainer.getMangaRepository()

        val getAllMangaUseCase1 = DependencyContainer.getAllMangaUseCase()
        val getAllMangaUseCase2 = DependencyContainer.getAllMangaUseCase()

        // 验证返回的是同一个实例（单例模式）
        assertEquals(mangaRepository1, mangaRepository2)
        assertEquals(getAllMangaUseCase1, getAllMangaUseCase2)
    }

    @Test
    fun `test database dao access`() {
        // Given
        whenever(mockContext.applicationContext).thenReturn(mockContext)
        whenever(mockDatabase.mangaDao()).thenReturn(mockMangaDao)
        whenever(mockDatabase.bookmarkDao()).thenReturn(mockBookmarkDao)
        whenever(mockDatabase.readingHistoryDao()).thenReturn(mockReadingHistoryDao)

        // When
        DependencyContainer.initialize(mockContext)

        // Then
        val database = DependencyContainer.getDatabase()
        val mangaDao = database.mangaDao()
        val bookmarkDao = database.bookmarkDao()
        val readingHistoryDao = database.readingHistoryDao()

        assertNotNull(mangaDao)
        assertNotNull(bookmarkDao)
        assertNotNull(readingHistoryDao)
        assertEquals(mockMangaDao, mangaDao)
        assertEquals(mockBookmarkDao, bookmarkDao)
        assertEquals(mockReadingHistoryDao, readingHistoryDao)
    }

    @Test
    fun `test import progress holder initialization`() {
        // Given
        whenever(mockContext.applicationContext).thenReturn(mockContext)
        whenever(mockDatabase.mangaDao()).thenReturn(mockMangaDao)
        whenever(mockDatabase.bookmarkDao()).thenReturn(mockBookmarkDao)
        whenever(mockDatabase.readingHistoryDao()).thenReturn(mockReadingHistoryDao)

        // When
        DependencyContainer.initialize(mockContext)

        // Then - 通过反射或其他方式验证 ImportProgressHolder 已初始化
        // 由于 ImportProgressHolder 是私有的，我们通过测试相关用例来间接验证
        val monitorImportProgressUseCase = DependencyContainer.getMonitorImportProgressUseCase()
        assertNotNull(monitorImportProgressUseCase)
    }

    @Test
    fun `test multiple initialization should not fail`() {
        // Given
        whenever(mockContext.applicationContext).thenReturn(mockContext)
        whenever(mockDatabase.mangaDao()).thenReturn(mockMangaDao)
        whenever(mockDatabase.bookmarkDao()).thenReturn(mockBookmarkDao)
        whenever(mockDatabase.readingHistoryDao()).thenReturn(mockReadingHistoryDao)

        // When - 多次初始化
        DependencyContainer.initialize(mockContext)
        DependencyContainer.initialize(mockContext)

        // Then - 不应该抛出异常
        assertTrue(true)
    }

    @Test(expected = IllegalStateException::class)
    fun `test access database before initialization should throw exception`() {
        // When - 在初始化之前访问数据库
        DependencyContainer.getDatabase()

        // Then - 应该抛出异常
    }

    @Test(expected = IllegalStateException::class)
    fun `test access repository before initialization should throw exception`() {
        // When - 在初始化之前访问仓储
        DependencyContainer.getMangaRepository()

        // Then - 应该抛出异常
    }

    /**
     * 清理 DependencyContainer 状态的辅助方法
     */
    private fun clearDependencyContainer() {
        try {
            // 通过反射重置 DependencyContainer 的状态
            val field = DependencyContainer::class.java.getDeclaredField("appContext")
            field.isAccessible = true
            field.set(null, null)
        } catch (e: Exception) {
            // 忽略反射异常
        }
    }
}