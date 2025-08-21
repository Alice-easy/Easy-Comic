package com.easycomic.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.easycomic.data.database.AppDatabase
import com.easycomic.domain.model.Bookmark
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Repository集成测试 - BookmarkRepository
 * 测试书签Repository与Room数据库的集成功能
 */
@RunWith(AndroidJUnit4::class)
class BookmarkRepositoryIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: BookmarkRepositoryImpl

    @Before
    fun setup() {
        // 创建内存数据库用于测试
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        // 创建Repository实例
        repository = BookmarkRepositoryImpl(database.bookmarkDao())
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        database.close()
    }

    @Test
    fun addBookmark_shouldSaveToDatabase() = runTest {
        // Given
        val bookmark = Bookmark(
            id = 1L,
            mangaId = 100L,
            pageNumber = 25,
            note = "精彩的战斗场面",
            createdAt = System.currentTimeMillis()
        )

        // When
        repository.addBookmark(bookmark)

        // Then
        val savedBookmark = repository.getBookmarkById(1L).first()
        assertThat(savedBookmark).isNotNull()
        assertThat(savedBookmark?.mangaId).isEqualTo(100L)
        assertThat(savedBookmark?.pageNumber).isEqualTo(25)
        assertThat(savedBookmark?.note).isEqualTo("精彩的战斗场面")
    }

    @Test
    fun getBookmarksByMangaId_shouldReturnCorrectBookmarks() = runTest {
        // Given
        val mangaId = 100L
        val bookmark1 = createTestBookmark(1L, mangaId, 10, "第一个书签")
        val bookmark2 = createTestBookmark(2L, mangaId, 20, "第二个书签")
        val bookmark3 = createTestBookmark(3L, 200L, 15, "其他漫画的书签")

        repository.addBookmark(bookmark1)
        repository.addBookmark(bookmark2)
        repository.addBookmark(bookmark3)

        // When
        val bookmarks = repository.getBookmarksByMangaId(mangaId).first()

        // Then
        assertThat(bookmarks).hasSize(2)
        assertThat(bookmarks.map { it.note }).containsExactly("第一个书签", "第二个书签")
        assertThat(bookmarks.all { it.mangaId == mangaId }).isTrue()
    }

    @Test
    fun getAllBookmarks_shouldReturnAllSavedBookmarks() = runTest {
        // Given
        val bookmark1 = createTestBookmark(1L, 100L, 10, "书签1")
        val bookmark2 = createTestBookmark(2L, 200L, 20, "书签2")
        val bookmark3 = createTestBookmark(3L, 300L, 30, "书签3")

        repository.addBookmark(bookmark1)
        repository.addBookmark(bookmark2)
        repository.addBookmark(bookmark3)

        // When
        val allBookmarks = repository.getAllBookmarks().first()

        // Then
        assertThat(allBookmarks).hasSize(3)
        assertThat(allBookmarks.map { it.note }).containsExactly("书签1", "书签2", "书签3")
    }

    @Test
    fun updateBookmark_shouldModifyExistingBookmark() = runTest {
        // Given
        val originalBookmark = createTestBookmark(1L, 100L, 10, "原始备注")
        repository.addBookmark(originalBookmark)

        val updatedBookmark = originalBookmark.copy(
            pageNumber = 25,
            note = "更新后的备注"
        )

        // When
        repository.updateBookmark(updatedBookmark)

        // Then
        val savedBookmark = repository.getBookmarkById(1L).first()
        assertThat(savedBookmark?.pageNumber).isEqualTo(25)
        assertThat(savedBookmark?.note).isEqualTo("更新后的备注")
    }

    @Test
    fun deleteBookmark_shouldRemoveFromDatabase() = runTest {
        // Given
        val bookmark = createTestBookmark(1L, 100L, 10, "待删除书签")
        repository.addBookmark(bookmark)

        // Verify it exists
        val existingBookmark = repository.getBookmarkById(1L).first()
        assertThat(existingBookmark).isNotNull()

        // When
        repository.deleteBookmark(bookmark)

        // Then
        val deletedBookmark = repository.getBookmarkById(1L).first()
        assertThat(deletedBookmark).isNull()
    }

    @Test
    fun deleteBookmarksByMangaId_shouldRemoveAllBookmarksForManga() = runTest {
        // Given
        val mangaId = 100L
        val bookmark1 = createTestBookmark(1L, mangaId, 10, "书签1")
        val bookmark2 = createTestBookmark(2L, mangaId, 20, "书签2")
        val bookmark3 = createTestBookmark(3L, 200L, 15, "其他漫画书签")

        repository.addBookmark(bookmark1)
        repository.addBookmark(bookmark2)
        repository.addBookmark(bookmark3)

        // When
        repository.deleteBookmarksByMangaId(mangaId)

        // Then
        val remainingBookmarks = repository.getAllBookmarks().first()
        assertThat(remainingBookmarks).hasSize(1)
        assertThat(remainingBookmarks[0].mangaId).isEqualTo(200L)
        assertThat(remainingBookmarks[0].note).isEqualTo("其他漫画书签")
    }

    @Test
    fun getBookmarksByPageRange_shouldReturnBookmarksInRange() = runTest {
        // Given
        val mangaId = 100L
        val bookmark1 = createTestBookmark(1L, mangaId, 5, "第5页")
        val bookmark2 = createTestBookmark(2L, mangaId, 15, "第15页")
        val bookmark3 = createTestBookmark(3L, mangaId, 25, "第25页")
        val bookmark4 = createTestBookmark(4L, mangaId, 35, "第35页")

        repository.addBookmark(bookmark1)
        repository.addBookmark(bookmark2)
        repository.addBookmark(bookmark3)
        repository.addBookmark(bookmark4)

        // When
        val bookmarksInRange = repository.getBookmarksByPageRange(mangaId, 10, 30).first()

        // Then
        assertThat(bookmarksInRange).hasSize(2)
        assertThat(bookmarksInRange.map { it.pageNumber }).containsExactly(15, 25)
        assertThat(bookmarksInRange.map { it.note }).containsExactly("第15页", "第25页")
    }

    @Test
    fun searchBookmarks_shouldReturnMatchingResults() = runTest {
        // Given
        val bookmark1 = createTestBookmark(1L, 100L, 10, "精彩的战斗场面")
        val bookmark2 = createTestBookmark(2L, 200L, 20, "感人的剧情")
        val bookmark3 = createTestBookmark(3L, 300L, 30, "精彩的对话")

        repository.addBookmark(bookmark1)
        repository.addBookmark(bookmark2)
        repository.addBookmark(bookmark3)

        // When
        val searchResults = repository.searchBookmarks("精彩").first()

        // Then
        assertThat(searchResults).hasSize(2)
        assertThat(searchResults.map { it.note }).containsExactly("精彩的战斗场面", "精彩的对话")
    }

    @Test
    fun getRecentBookmarks_shouldReturnInCorrectOrder() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val bookmark1 = createTestBookmark(1L, 100L, 10, "最近的", currentTime - 1000)
        val bookmark2 = createTestBookmark(2L, 200L, 20, "很久前的", currentTime - 10000)
        val bookmark3 = createTestBookmark(3L, 300L, 30, "刚刚的", currentTime)

        repository.addBookmark(bookmark1)
        repository.addBookmark(bookmark2)
        repository.addBookmark(bookmark3)

        // When
        val recentBookmarks = repository.getRecentBookmarks(10).first()

        // Then
        assertThat(recentBookmarks).hasSize(3)
        // 应该按创建时间降序排列
        assertThat(recentBookmarks[0].note).isEqualTo("刚刚的")
        assertThat(recentBookmarks[1].note).isEqualTo("最近的")
        assertThat(recentBookmarks[2].note).isEqualTo("很久前的")
    }

    @Test
    fun batchAddBookmarks_shouldSaveAllBookmarks() = runTest {
        // Given
        val bookmarks = listOf(
            createTestBookmark(1L, 100L, 10, "批量书签1"),
            createTestBookmark(2L, 100L, 20, "批量书签2"),
            createTestBookmark(3L, 200L, 15, "批量书签3"),
            createTestBookmark(4L, 200L, 25, "批量书签4")
        )

        // When
        repository.batchAddBookmarks(bookmarks)

        // Then
        val allBookmarks = repository.getAllBookmarks().first()
        assertThat(allBookmarks).hasSize(4)
        assertThat(allBookmarks.map { it.note }).containsExactly(
            "批量书签1", "批量书签2", "批量书签3", "批量书签4"
        )
    }

    @Test
    fun hasBookmarkAtPage_shouldReturnCorrectResult() = runTest {
        // Given
        val mangaId = 100L
        val bookmark = createTestBookmark(1L, mangaId, 25, "第25页书签")
        repository.addBookmark(bookmark)

        // When & Then
        val hasBookmarkAt25 = repository.hasBookmarkAtPage(mangaId, 25).first()
        val hasBookmarkAt30 = repository.hasBookmarkAtPage(mangaId, 30).first()

        assertThat(hasBookmarkAt25).isTrue()
        assertThat(hasBookmarkAt30).isFalse()
    }

    @Test
    fun getBookmarkCount_shouldReturnCorrectCount() = runTest {
        // Given
        val mangaId = 100L
        val bookmark1 = createTestBookmark(1L, mangaId, 10, "书签1")
        val bookmark2 = createTestBookmark(2L, mangaId, 20, "书签2")
        val bookmark3 = createTestBookmark(3L, 200L, 15, "其他漫画书签")

        repository.addBookmark(bookmark1)
        repository.addBookmark(bookmark2)
        repository.addBookmark(bookmark3)

        // When
        val countForManga100 = repository.getBookmarkCount(mangaId).first()
        val countForManga200 = repository.getBookmarkCount(200L).first()

        // Then
        assertThat(countForManga100).isEqualTo(2)
        assertThat(countForManga200).isEqualTo(1)
    }

    private fun createTestBookmark(
        id: Long,
        mangaId: Long,
        pageNumber: Int,
        note: String,
        createdAt: Long = System.currentTimeMillis()
    ): Bookmark {
        return Bookmark(
            id = id,
            mangaId = mangaId,
            pageNumber = pageNumber,
            note = note,
            createdAt = createdAt
        )
    }
}