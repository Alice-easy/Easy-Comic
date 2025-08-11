package com.easycomic.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import app.cash.turbine.test
import com.easycomic.data.dao.MangaDao
import com.easycomic.data.database.AppDatabase
import com.easycomic.data.entity.MangaEntity
import com.easycomic.data.entity.ReadingStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * AppDatabase 集成测试
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class AppDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var mangaDao: MangaDao

    @Before
    fun setup() {
        // 使用内存数据库进行测试
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        mangaDao = database.mangaDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `when inserting manga, should be able to retrieve it`() = runTest {
        // Given
        val manga = createTestMangaEntity(title = "测试漫画", id = 1L)

        // When
        mangaDao.insertOrUpdateManga(manga)
        val result = mangaDao.getMangaById(1L)

        // Then
        assertNotNull(result)
        assertEquals("测试漫画", result.title)
        assertEquals("测试作者", result.author)
    }

    @Test
    fun `when getting all manga, should return all inserted manga`() = runTest {
        // Given
        val manga1 = createTestMangaEntity(title = "漫画1", id = 1L)
        val manga2 = createTestMangaEntity(title = "漫画2", id = 2L)
        
        mangaDao.insertOrUpdateManga(manga1)
        mangaDao.insertOrUpdateManga(manga2)

        // When
        val result = mangaDao.getAllManga().first()

        // Then
        assertEquals(2, result.size)
        assertTrue(result.any { it.title == "漫画1" })
        assertTrue(result.any { it.title == "漫画2" })
    }

    @Test
    fun `when searching manga, should return matching results`() = runTest {
        // Given
        val manga1 = createTestMangaEntity(title = "测试漫画", id = 1L)
        val manga2 = createTestMangaEntity(title = "另一个漫画", id = 2L)
        val manga3 = createTestMangaEntity(title = "不相关", id = 3L)
        
        mangaDao.insertOrUpdateManga(manga1)
        mangaDao.insertOrUpdateManga(manga2)
        mangaDao.insertOrUpdateManga(manga3)

        // When
        val result = mangaDao.searchManga("测试").first()

        // Then
        assertEquals(1, result.size)
        assertEquals("测试漫画", result[0].title)
    }

    @Test
    fun `when getting favorite manga, should return only favorites`() = runTest {
        // Given
        val favoriteManga = createTestMangaEntity(title = "收藏漫画", id = 1L, isFavorite = true)
        val normalManga = createTestMangaEntity(title = "普通漫画", id = 2L, isFavorite = false)
        
        mangaDao.insertOrUpdateManga(favoriteManga)
        mangaDao.insertOrUpdateManga(normalManga)

        // When
        val result = mangaDao.getFavoriteManga().first()

        // Then
        assertEquals(1, result.size)
        assertEquals("收藏漫画", result[0].title)
        assertTrue(result[0].isFavorite)
    }

    @Test
    fun `when getting manga by status, should return filtered results`() = runTest {
        // Given
        val readingManga = createTestMangaEntity(title = "阅读中", id = 1L, readingStatus = ReadingStatus.READING)
        val completedManga = createTestMangaEntity(title = "已完成", id = 2L, readingStatus = ReadingStatus.COMPLETED)
        val unreadManga = createTestMangaEntity(title = "未读", id = 3L, readingStatus = ReadingStatus.UNREAD)
        
        mangaDao.insertOrUpdateManga(readingManga)
        mangaDao.insertOrUpdateManga(completedManga)
        mangaDao.insertOrUpdateManga(unreadManga)

        // When
        val result = mangaDao.getMangaByStatus(ReadingStatus.READING).first()

        // Then
        assertEquals(1, result.size)
        assertEquals("阅读中", result[0].title)
        assertEquals(ReadingStatus.READING, result[0].readingStatus)
    }

    @Test
    fun `when getting recent manga, should return limited results`() = runTest {
        // Given
        val manga1 = createTestMangaEntity(title = "漫画1", id = 1L, dateAdded = 1000L)
        val manga2 = createTestMangaEntity(title = "漫画2", id = 2L, dateAdded = 2000L)
        val manga3 = createTestMangaEntity(title = "漫画3", id = 3L, dateAdded = 3000L)
        
        mangaDao.insertOrUpdateManga(manga1)
        mangaDao.insertOrUpdateManga(manga2)
        mangaDao.insertOrUpdateManga(manga3)

        // When
        val result = mangaDao.getRecentManga(2).first()

        // Then
        assertEquals(2, result.size)
        // 应该按时间降序排列
        assertEquals("漫画3", result[0].title)
        assertEquals("漫画2", result[1].title)
    }

    @Test
    fun `when updating reading progress, should persist changes`() = runTest {
        // Given
        val manga = createTestMangaEntity(title = "测试漫画", id = 1L)
        mangaDao.insertOrUpdateManga(manga)

        // When
        mangaDao.updateReadingProgress(
            mangaId = 1L,
            currentPage = 50,
            status = ReadingStatus.READING
        )

        // Then
        val result = mangaDao.getMangaById(1L)
        assertNotNull(result)
        assertEquals(50, result.currentPage)
        assertEquals(ReadingStatus.READING, result.readingStatus)
    }

    @Test
    fun `when toggling favorite, should update favorite status`() = runTest {
        // Given
        val manga = createTestMangaEntity(title = "测试漫画", id = 1L, isFavorite = false)
        mangaDao.insertOrUpdateManga(manga)

        // When
        mangaDao.toggleFavorite(1L)

        // Then
        val result = mangaDao.getMangaById(1L)
        assertNotNull(result)
        assertTrue(result.isFavorite)

        // When - toggle again
        mangaDao.toggleFavorite(1L)

        // Then
        val result2 = mangaDao.getMangaById(1L)
        assertNotNull(result2)
        assertFalse(result2.isFavorite)
    }

    @Test
    fun `when updating rating, should persist rating changes`() = runTest {
        // Given
        val manga = createTestMangaEntity(title = "测试漫画", id = 1L, rating = 3.0f)
        mangaDao.insertOrUpdateManga(manga)

        // When
        mangaDao.updateRating(1L, 4.5f)

        // Then
        val result = mangaDao.getMangaById(1L)
        assertNotNull(result)
        assertEquals(4.5f, result.rating)
    }

    @Test
    fun `when deleting manga, should remove it from database`() = runTest {
        // Given
        val manga = createTestMangaEntity(title = "测试漫画", id = 1L)
        mangaDao.insertOrUpdateManga(manga)

        // When
        mangaDao.deleteManga(manga)

        // Then
        val result = mangaDao.getMangaById(1L)
        assertNull(result)
    }

    @Test
    fun `when deleting multiple manga, should remove all from database`() = runTest {
        // Given
        val manga1 = createTestMangaEntity(title = "漫画1", id = 1L)
        val manga2 = createTestMangaEntity(title = "漫画2", id = 2L)
        val manga3 = createTestMangaEntity(title = "漫画3", id = 3L)
        
        mangaDao.insertOrUpdateManga(manga1)
        mangaDao.insertOrUpdateManga(manga2)
        mangaDao.insertOrUpdateManga(manga3)

        // When
        mangaDao.deleteAllManga(listOf(manga1, manga2))

        // Then
        assertNull(mangaDao.getMangaById(1L))
        assertNull(mangaDao.getMangaById(2L))
        assertNotNull(mangaDao.getMangaById(3L)) // manga3 should still exist
    }

    @Test
    fun `when getting manga count, should return correct count`() = runTest {
        // Given
        val manga1 = createTestMangaEntity(title = "漫画1", id = 1L)
        val manga2 = createTestMangaEntity(title = "漫画2", id = 2L)
        
        mangaDao.insertOrUpdateManga(manga1)
        mangaDao.insertOrUpdateManga(manga2)

        // When
        val result = mangaDao.getMangaCount().first()

        // Then
        assertEquals(2, result)
    }

    @Test
    fun `when getting favorite count, should return correct count`() = runTest {
        // Given
        val favorite1 = createTestMangaEntity(title = "收藏1", id = 1L, isFavorite = true)
        val favorite2 = createTestMangaEntity(title = "收藏2", id = 2L, isFavorite = true)
        val normal = createTestMangaEntity(title = "普通", id = 3L, isFavorite = false)
        
        mangaDao.insertOrUpdateManga(favorite1)
        mangaDao.insertOrUpdateManga(favorite2)
        mangaDao.insertOrUpdateManga(normal)

        // When
        val result = mangaDao.getFavoriteCount().first()

        // Then
        assertEquals(2, result)
    }

    @Test
    fun `when getting completed count, should return correct count`() = runTest {
        // Given
        val completed1 = createTestMangaEntity(title = "完成1", id = 1L, readingStatus = ReadingStatus.COMPLETED)
        val completed2 = createTestMangaEntity(title = "完成2", id = 2L, readingStatus = ReadingStatus.COMPLETED)
        val reading = createTestMangaEntity(title = "阅读中", id = 3L, readingStatus = ReadingStatus.READING)
        
        mangaDao.insertOrUpdateManga(completed1)
        mangaDao.insertOrUpdateManga(completed2)
        mangaDao.insertOrUpdateManga(reading)

        // When
        val result = mangaDao.getCompletedCount().first()

        // Then
        assertEquals(2, result)
    }

    @Test
    fun `when getting manga by file path, should return correct manga`() = runTest {
        // Given
        val filePath = "/path/to/specific/comic.cbz"
        val manga = createTestMangaEntity(title = "特定漫画", id = 1L, filePath = filePath)
        mangaDao.insertOrUpdateManga(manga)

        // When
        val result = mangaDao.getMangaByFilePath(filePath)

        // Then
        assertNotNull(result)
        assertEquals("特定漫画", result.title)
        assertEquals(filePath, result.filePath)
    }

    @Test
    fun `when inserting existing manga, should update it instead of creating new`() = runTest {
        // Given
        val originalManga = createTestMangaEntity(title = "原标题", id = 1L)
        mangaDao.insertOrUpdateManga(originalManga)

        // When
        val updatedManga = originalManga.copy(title = "更新后标题")
        mangaDao.insertOrUpdateManga(updatedManga)

        // Then
        val result = mangaDao.getMangaById(1L)
        assertNotNull(result)
        assertEquals("更新后标题", result.title)
        // 确保没有创建新记录
        val allManga = mangaDao.getAllManga().first()
        assertEquals(1, allManga.size)
    }

    @Test
    fun `when testing database flow emissions, should work correctly`() = runTest {
        // Given
        val manga = createTestMangaEntity(title = "测试漫画", id = 1L)

        // When
        mangaDao.test {
            // 初始状态应该为空
            val initialState = awaitItem()
            assertTrue(initialState.isEmpty())

            // 插入数据
            mangaDao.insertOrUpdateManga(manga)

            // 应该收到更新
            val updatedState = awaitItem()
            assertEquals(1, updatedState.size)
            assertEquals("测试漫画", updatedState[0].title)

            // 删除数据
            mangaDao.deleteManga(manga)

            // 应该再次收到更新
            val finalState = awaitItem()
            assertTrue(finalState.isEmpty())

            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createTestMangaEntity(
        title: String,
        id: Long,
        isFavorite: Boolean = false,
        readingStatus: ReadingStatus = ReadingStatus.UNREAD,
        filePath: String = "/path/to/comic.cbz",
        rating: Float = 4.0f,
        dateAdded: Long = System.currentTimeMillis()
    ): MangaEntity {
        return MangaEntity(
            id = id,
            title = title,
            author = "测试作者",
            description = "测试描述",
            filePath = filePath,
            fileUri = "content://com.example.provider/comic.cbz",
            fileFormat = "CBZ",
            fileSize = 1024000L,
            pageCount = 100,
            currentPage = 0,
            coverImagePath = "/path/to/cover.jpg",
            thumbnailPath = "/path/to/thumbnail.jpg",
            rating = rating,
            isFavorite = isFavorite,
            readingStatus = readingStatus,
            tags = "",
            lastRead = System.currentTimeMillis(),
            dateAdded = dateAdded,
            dateModified = System.currentTimeMillis()
        )
    }
}