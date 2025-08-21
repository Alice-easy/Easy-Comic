package com.easycomic.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.easycomic.data.database.AppDatabase
import com.easycomic.data.entity.MangaEntity
import com.easycomic.domain.model.Manga
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Repository集成测试 - MangaRepository
 * 测试Repository与Room数据库的集成功能
 */
@RunWith(AndroidJUnit4::class)
class MangaRepositoryIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: MangaRepositoryImpl

    @Before
    fun setup() {
        // 创建内存数据库用于测试
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        // 创建Repository实例
        repository = MangaRepositoryImpl(database.mangaDao())
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertManga_shouldSaveToDatabase() = runTest {
        // Given
        val manga = Manga(
            id = 1L,
            title = "测试漫画",
            path = "/test/path",
            coverPath = "/test/cover.jpg",
            totalPages = 100,
            currentPage = 1,
            lastReadTime = System.currentTimeMillis(),
            isBookmarked = false
        )

        // When
        repository.insertManga(manga)

        // Then
        val savedManga = repository.getMangaById(1L).first()
        assertThat(savedManga).isNotNull()
        assertThat(savedManga?.title).isEqualTo("测试漫画")
        assertThat(savedManga?.path).isEqualTo("/test/path")
        assertThat(savedManga?.totalPages).isEqualTo(100)
    }

    @Test
    fun getAllManga_shouldReturnAllSavedManga() = runTest {
        // Given
        val manga1 = createTestManga(1L, "漫画1")
        val manga2 = createTestManga(2L, "漫画2")
        val manga3 = createTestManga(3L, "漫画3")

        repository.insertManga(manga1)
        repository.insertManga(manga2)
        repository.insertManga(manga3)

        // When
        val allManga = repository.getAllManga().first()

        // Then
        assertThat(allManga).hasSize(3)
        assertThat(allManga.map { it.title }).containsExactly("漫画1", "漫画2", "漫画3")
    }

    @Test
    fun updateManga_shouldModifyExistingManga() = runTest {
        // Given
        val originalManga = createTestManga(1L, "原始漫画")
        repository.insertManga(originalManga)

        val updatedManga = originalManga.copy(
            title = "更新后的漫画",
            currentPage = 50,
            isBookmarked = true
        )

        // When
        repository.updateManga(updatedManga)

        // Then
        val savedManga = repository.getMangaById(1L).first()
        assertThat(savedManga?.title).isEqualTo("更新后的漫画")
        assertThat(savedManga?.currentPage).isEqualTo(50)
        assertThat(savedManga?.isBookmarked).isTrue()
    }

    @Test
    fun deleteManga_shouldRemoveFromDatabase() = runTest {
        // Given
        val manga = createTestManga(1L, "待删除漫画")
        repository.insertManga(manga)

        // Verify it exists
        val existingManga = repository.getMangaById(1L).first()
        assertThat(existingManga).isNotNull()

        // When
        repository.deleteManga(manga)

        // Then
        val deletedManga = repository.getMangaById(1L).first()
        assertThat(deletedManga).isNull()
    }

    @Test
    fun searchManga_shouldReturnMatchingResults() = runTest {
        // Given
        val manga1 = createTestManga(1L, "火影忍者")
        val manga2 = createTestManga(2L, "海贼王")
        val manga3 = createTestManga(3L, "火影忍者疾风传")

        repository.insertManga(manga1)
        repository.insertManga(manga2)
        repository.insertManga(manga3)

        // When
        val searchResults = repository.searchManga("火影").first()

        // Then
        assertThat(searchResults).hasSize(2)
        assertThat(searchResults.map { it.title }).containsExactly("火影忍者", "火影忍者疾风传")
    }

    @Test
    fun getBookmarkedManga_shouldReturnOnlyBookmarked() = runTest {
        // Given
        val manga1 = createTestManga(1L, "漫画1", isBookmarked = true)
        val manga2 = createTestManga(2L, "漫画2", isBookmarked = false)
        val manga3 = createTestManga(3L, "漫画3", isBookmarked = true)

        repository.insertManga(manga1)
        repository.insertManga(manga2)
        repository.insertManga(manga3)

        // When
        val bookmarkedManga = repository.getBookmarkedManga().first()

        // Then
        assertThat(bookmarkedManga).hasSize(2)
        assertThat(bookmarkedManga.map { it.title }).containsExactly("漫画1", "漫画3")
    }

    @Test
    fun getRecentlyReadManga_shouldReturnInCorrectOrder() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val manga1 = createTestManga(1L, "最近读的", lastReadTime = currentTime - 1000)
        val manga2 = createTestManga(2L, "很久前读的", lastReadTime = currentTime - 10000)
        val manga3 = createTestManga(3L, "刚刚读的", lastReadTime = currentTime)

        repository.insertManga(manga1)
        repository.insertManga(manga2)
        repository.insertManga(manga3)

        // When
        val recentManga = repository.getRecentlyReadManga(10).first()

        // Then
        assertThat(recentManga).hasSize(3)
        // 应该按最近阅读时间降序排列
        assertThat(recentManga[0].title).isEqualTo("刚刚读的")
        assertThat(recentManga[1].title).isEqualTo("最近读的")
        assertThat(recentManga[2].title).isEqualTo("很久前读的")
    }

    @Test
    fun updateReadingProgress_shouldUpdateCurrentPageAndTime() = runTest {
        // Given
        val manga = createTestManga(1L, "测试漫画", currentPage = 1)
        repository.insertManga(manga)

        val newPage = 25
        val newTime = System.currentTimeMillis()

        // When
        repository.updateReadingProgress(1L, newPage, newTime)

        // Then
        val updatedManga = repository.getMangaById(1L).first()
        assertThat(updatedManga?.currentPage).isEqualTo(newPage)
        assertThat(updatedManga?.lastReadTime).isEqualTo(newTime)
    }

    @Test
    fun batchInsertManga_shouldSaveAllManga() = runTest {
        // Given
        val mangaList = listOf(
            createTestManga(1L, "批量漫画1"),
            createTestManga(2L, "批量漫画2"),
            createTestManga(3L, "批量漫画3"),
            createTestManga(4L, "批量漫画4"),
            createTestManga(5L, "批量漫画5")
        )

        // When
        repository.batchInsertManga(mangaList)

        // Then
        val allManga = repository.getAllManga().first()
        assertThat(allManga).hasSize(5)
        assertThat(allManga.map { it.title }).containsExactly(
            "批量漫画1", "批量漫画2", "批量漫画3", "批量漫画4", "批量漫画5"
        )
    }

    private fun createTestManga(
        id: Long,
        title: String,
        path: String = "/test/path/$id",
        coverPath: String = "/test/cover$id.jpg",
        totalPages: Int = 100,
        currentPage: Int = 1,
        lastReadTime: Long = System.currentTimeMillis(),
        isBookmarked: Boolean = false
    ): Manga {
        return Manga(
            id = id,
            title = title,
            path = path,
            coverPath = coverPath,
            totalPages = totalPages,
            currentPage = currentPage,
            lastReadTime = lastReadTime,
            isBookmarked = isBookmarked
        )
    }
}