package com.easycomic.domain.usecase.manga

import com.easycomic.domain.model.Manga
import com.easycomic.domain.model.ReadingStatus
import com.easycomic.domain.repository.MangaRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * MarkMangasAsReadUseCase单元测试
 * 
 * 测试标记漫画为已读/未读的各种场景：
 * - 单个标记为已读
 * - 批量标记为已读
 * - 单个标记为未读
 * - 批量标记为未读
 * - 边界条件处理
 * - 异常情况处理
 */
class MarkMangasAsReadUseCaseTest {

    private val mockRepository = mockk<MangaRepository>()
    private lateinit var markMangasAsReadUseCase: MarkMangasAsReadUseCase

    // 测试数据
    private val testManga1 = Manga(
        id = 1L,
        title = "Test Manga 1",
        filePath = "/test/path/manga1.zip",
        pageCount = 20,
        currentPage = 5,
        readingStatus = ReadingStatus.READING,
        isFavorite = false,
        rating = 0f,
        lastRead = 1000L
    )

    private val testManga2 = Manga(
        id = 2L,
        title = "Test Manga 2",
        filePath = "/test/path/manga2.zip",
        pageCount = 30,
        currentPage = 0,
        readingStatus = ReadingStatus.UNREAD,
        isFavorite = true,
        rating = 4.0f,
        lastRead = 0L
    )

    private val testManga3 = Manga(
        id = 3L,
        title = "Test Manga 3",
        filePath = "/test/path/manga3.zip",
        pageCount = 25,
        currentPage = 24,
        readingStatus = ReadingStatus.COMPLETED,
        isFavorite = false,
        rating = 3.5f,
        lastRead = 2000L
    )

    // 零页数的漫画（边界情况）
    private val zeroPageManga = Manga(
        id = 4L,
        title = "Zero Page Manga",
        filePath = "/test/path/zero.zip",
        pageCount = 0,
        currentPage = 0,
        readingStatus = ReadingStatus.UNREAD,
        isFavorite = false,
        rating = 0f,
        lastRead = 0L
    )

    @Before
    fun setup() {
        markMangasAsReadUseCase = MarkMangasAsReadUseCase(mockRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // ========== 单个标记为已读测试 ==========

    @Test
    fun `markSingleAsRead should mark manga as read when manga exists and has pages`() = runTest {
        // Given
        val mangaId = 1L
        val expectedUpdatedManga = testManga1.copy(
            currentPage = testManga1.pageCount - 1,
            lastRead = System.currentTimeMillis()
        )
        
        coEvery { mockRepository.getMangaById(mangaId) } returns testManga1
        coEvery { mockRepository.updateManga(any()) } returns 1L

        // When
        markMangasAsReadUseCase.markSingleAsRead(mangaId)

        // Then
        coVerify { mockRepository.getMangaById(mangaId) }
        coVerify { 
            mockRepository.updateManga(match { updatedManga ->
                updatedManga.id == testManga1.id &&
                updatedManga.currentPage == testManga1.pageCount - 1 &&
                updatedManga.lastRead > testManga1.lastRead &&
                updatedManga.title == testManga1.title &&
                updatedManga.pageCount == testManga1.pageCount
            })
        }
    }

    @Test
    fun `markSingleAsRead should not update when manga does not exist`() = runTest {
        // Given
        val nonExistentId = 999L
        coEvery { mockRepository.getMangaById(nonExistentId) } returns null

        // When
        markMangasAsReadUseCase.markSingleAsRead(nonExistentId)

        // Then
        coVerify { mockRepository.getMangaById(nonExistentId) }
        coVerify(exactly = 0) { mockRepository.updateManga(any()) }
    }

    @Test
    fun `markSingleAsRead should not update when manga has zero pages`() = runTest {
        // Given
        val mangaId = 4L
        coEvery { mockRepository.getMangaById(mangaId) } returns zeroPageManga

        // When
        markMangasAsReadUseCase.markSingleAsRead(mangaId)

        // Then
        coVerify { mockRepository.getMangaById(mangaId) }
        coVerify(exactly = 0) { mockRepository.updateManga(any()) }
    }

    @Test
    fun `markSingleAsRead should handle single page manga correctly`() = runTest {
        // Given
        val singlePageManga = testManga1.copy(pageCount = 1, currentPage = 0)
        val mangaId = 1L
        
        coEvery { mockRepository.getMangaById(mangaId) } returns singlePageManga
        coEvery { mockRepository.updateManga(any()) } returns 1L

        // When
        markMangasAsReadUseCase.markSingleAsRead(mangaId)

        // Then
        coVerify { mockRepository.getMangaById(mangaId) }
        coVerify { 
            mockRepository.updateManga(match { updatedManga ->
                updatedManga.currentPage == 0 // 单页漫画的最后一页是第0页
            })
        }
    }

    @Test
    fun `markSingleAsRead should update lastRead timestamp`() = runTest {
        // Given
        val mangaId = 1L
        val oldTimestamp = testManga1.lastRead
        
        coEvery { mockRepository.getMangaById(mangaId) } returns testManga1
        coEvery { mockRepository.updateManga(any()) } returns 1L

        // When
        val startTime = System.currentTimeMillis()
        markMangasAsReadUseCase.markSingleAsRead(mangaId)
        val endTime = System.currentTimeMillis()

        // Then
        coVerify { 
            mockRepository.updateManga(match { updatedManga ->
                updatedManga.lastRead >= startTime && 
                updatedManga.lastRead <= endTime &&
                updatedManga.lastRead > oldTimestamp
            })
        }
    }

    // ========== 批量标记为已读测试 ==========

    @Test
    fun `markMultipleAsRead should mark all existing manga as read`() = runTest {
        // Given
        val mangaIds = listOf(1L, 2L, 3L)
        
        coEvery { mockRepository.getMangaById(1L) } returns testManga1
        coEvery { mockRepository.getMangaById(2L) } returns testManga2
        coEvery { mockRepository.getMangaById(3L) } returns testManga3
        coEvery { mockRepository.updateManga(any()) } returns 1L

        // When
        markMangasAsReadUseCase.markMultipleAsRead(mangaIds)

        // Then
        coVerify { mockRepository.getMangaById(1L) }
        coVerify { mockRepository.getMangaById(2L) }
        coVerify { mockRepository.getMangaById(3L) }
        coVerify(exactly = 3) { mockRepository.updateManga(any()) }
    }

    @Test
    fun `markMultipleAsRead should skip non-existent manga`() = runTest {
        // Given
        val mangaIds = listOf(1L, 999L, 2L) // 999L 不存在
        
        coEvery { mockRepository.getMangaById(1L) } returns testManga1
        coEvery { mockRepository.getMangaById(999L) } returns null
        coEvery { mockRepository.getMangaById(2L) } returns testManga2
        coEvery { mockRepository.updateManga(any()) } returns 1L

        // When
        markMangasAsReadUseCase.markMultipleAsRead(mangaIds)

        // Then
        coVerify { mockRepository.getMangaById(1L) }
        coVerify { mockRepository.getMangaById(999L) }
        coVerify { mockRepository.getMangaById(2L) }
        // 只为存在的漫画调用更新
        coVerify(exactly = 2) { mockRepository.updateManga(any()) }
    }

    @Test
    fun `markMultipleAsRead should handle empty list gracefully`() = runTest {
        // Given
        val emptyIds = emptyList<Long>()

        // When
        markMangasAsReadUseCase.markMultipleAsRead(emptyIds)

        // Then
        // 空列表不应该调用任何repository方法
        coVerify(exactly = 0) { mockRepository.getMangaById(any()) }
        coVerify(exactly = 0) { mockRepository.updateManga(any()) }
    }

    @Test
    fun `markMultipleAsRead should handle single item list`() = runTest {
        // Given
        val singleId = listOf(1L)
        
        coEvery { mockRepository.getMangaById(1L) } returns testManga1
        coEvery { mockRepository.updateManga(any()) } returns 1L

        // When
        markMangasAsReadUseCase.markMultipleAsRead(singleId)

        // Then
        coVerify { mockRepository.getMangaById(1L) }
        coVerify(exactly = 1) { mockRepository.updateManga(any()) }
    }

    @Test
    fun `markMultipleAsRead should handle duplicate ids`() = runTest {
        // Given
        val duplicateIds = listOf(1L, 1L, 2L) // 包含重复ID
        
        coEvery { mockRepository.getMangaById(1L) } returns testManga1
        coEvery { mockRepository.getMangaById(2L) } returns testManga2
        coEvery { mockRepository.updateManga(any()) } returns 1L

        // When
        markMangasAsReadUseCase.markMultipleAsRead(duplicateIds)

        // Then
        // 重复ID会导致多次调用
        coVerify(exactly = 2) { mockRepository.getMangaById(1L) }
        coVerify(exactly = 1) { mockRepository.getMangaById(2L) }
        coVerify(exactly = 3) { mockRepository.updateManga(any()) }
    }

    // ========== 单个标记为未读测试 ==========

    @Test
    fun `markAsUnread should reset manga to first page when manga exists`() = runTest {
        // Given
        val mangaId = 3L // 使用已读完的漫画
        
        coEvery { mockRepository.getMangaById(mangaId) } returns testManga3
        coEvery { mockRepository.updateManga(any()) } returns 1L

        // When
        markMangasAsReadUseCase.markAsUnread(mangaId)

        // Then
        coVerify { mockRepository.getMangaById(mangaId) }
        coVerify { 
            mockRepository.updateManga(match { updatedManga ->
                updatedManga.id == testManga3.id &&
                updatedManga.currentPage == 0 &&
                updatedManga.lastRead == 0L &&
                updatedManga.title == testManga3.title &&
                updatedManga.pageCount == testManga3.pageCount
            })
        }
    }

    @Test
    fun `markAsUnread should not update when manga does not exist`() = runTest {
        // Given
        val nonExistentId = 999L
        coEvery { mockRepository.getMangaById(nonExistentId) } returns null

        // When
        markMangasAsReadUseCase.markAsUnread(nonExistentId)

        // Then
        coVerify { mockRepository.getMangaById(nonExistentId) }
        coVerify(exactly = 0) { mockRepository.updateManga(any()) }
    }

    @Test
    fun `markAsUnread should handle already unread manga`() = runTest {
        // Given
        val mangaId = 2L // 使用未开始的漫画
        
        coEvery { mockRepository.getMangaById(mangaId) } returns testManga2
        coEvery { mockRepository.updateManga(any()) } returns 1L

        // When
        markMangasAsReadUseCase.markAsUnread(mangaId)

        // Then
        coVerify { mockRepository.getMangaById(mangaId) }
        coVerify { 
            mockRepository.updateManga(match { updatedManga ->
                updatedManga.currentPage == 0 &&
                updatedManga.lastRead == 0L
            })
        }
    }

    @Test
    fun `markAsUnread should handle zero page manga`() = runTest {
        // Given
        val mangaId = 4L
        
        coEvery { mockRepository.getMangaById(mangaId) } returns zeroPageManga
        coEvery { mockRepository.updateManga(any()) } returns 1L

        // When
        markMangasAsReadUseCase.markAsUnread(mangaId)

        // Then
        coVerify { mockRepository.getMangaById(mangaId) }
        coVerify { 
            mockRepository.updateManga(match { updatedManga ->
                updatedManga.currentPage == 0 &&
                updatedManga.lastRead == 0L
            })
        }
    }

    // ========== 批量标记为未读测试 ==========

    @Test
    fun `markMultipleAsUnread should reset all existing manga to unread`() = runTest {
        // Given
        val mangaIds = listOf(1L, 2L, 3L)
        
        coEvery { mockRepository.getMangaById(1L) } returns testManga1
        coEvery { mockRepository.getMangaById(2L) } returns testManga2
        coEvery { mockRepository.getMangaById(3L) } returns testManga3
        coEvery { mockRepository.updateManga(any()) } returns 1L

        // When
        markMangasAsReadUseCase.markMultipleAsUnread(mangaIds)

        // Then
        coVerify { mockRepository.getMangaById(1L) }
        coVerify { mockRepository.getMangaById(2L) }
        coVerify { mockRepository.getMangaById(3L) }
        coVerify(exactly = 3) { mockRepository.updateManga(any()) }
        
        // 验证所有漫画都被重置为未读状态
        coVerify(exactly = 3) { 
            mockRepository.updateManga(match { updatedManga ->
                updatedManga.currentPage == 0 && updatedManga.lastRead == 0L
            })
        }
    }

    @Test
    fun `markMultipleAsUnread should skip non-existent manga`() = runTest {
        // Given
        val mangaIds = listOf(1L, 999L, 3L) // 999L 不存在
        
        coEvery { mockRepository.getMangaById(1L) } returns testManga1
        coEvery { mockRepository.getMangaById(999L) } returns null
        coEvery { mockRepository.getMangaById(3L) } returns testManga3
        coEvery { mockRepository.updateManga(any()) } returns 1L

        // When
        markMangasAsReadUseCase.markMultipleAsUnread(mangaIds)

        // Then
        coVerify { mockRepository.getMangaById(1L) }
        coVerify { mockRepository.getMangaById(999L) }
        coVerify { mockRepository.getMangaById(3L) }
        // 只为存在的漫画调用更新
        coVerify(exactly = 2) { mockRepository.updateManga(any()) }
    }

    @Test
    fun `markMultipleAsUnread should handle empty list gracefully`() = runTest {
        // Given
        val emptyIds = emptyList<Long>()

        // When
        markMangasAsReadUseCase.markMultipleAsUnread(emptyIds)

        // Then
        // 空列表不应该调用任何repository方法
        coVerify(exactly = 0) { mockRepository.getMangaById(any()) }
        coVerify(exactly = 0) { mockRepository.updateManga(any()) }
    }

    // ========== 异常处理测试 ==========

    @Test
    fun `markSingleAsRead should handle repository exceptions in getMangaById`() = runTest {
        // Given
        val mangaId = 1L
        coEvery { mockRepository.getMangaById(mangaId) } throws Exception("Database error")

        // When & Then
        try {
            markMangasAsReadUseCase.markSingleAsRead(mangaId)
            assertThat(false).isTrue() // 不应该到达这里
        } catch (e: Exception) {
            assertThat(e.message).isEqualTo("Database error")
            coVerify { mockRepository.getMangaById(mangaId) }
            coVerify(exactly = 0) { mockRepository.updateManga(any()) }
        }
    }

    @Test
    fun `markSingleAsRead should handle repository exceptions in updateManga`() = runTest {
        // Given
        val mangaId = 1L
        coEvery { mockRepository.getMangaById(mangaId) } returns testManga1
        coEvery { mockRepository.updateManga(any()) } throws Exception("Update failed")

        // When & Then
        try {
            markMangasAsReadUseCase.markSingleAsRead(mangaId)
            assertThat(false).isTrue() // 不应该到达这里
        } catch (e: Exception) {
            assertThat(e.message).isEqualTo("Update failed")
            coVerify { mockRepository.getMangaById(mangaId) }
            coVerify { mockRepository.updateManga(any()) }
        }
    }

    @Test
    fun `markAsUnread should handle repository exceptions in updateManga`() = runTest {
        // Given
        val mangaId = 3L
        coEvery { mockRepository.getMangaById(mangaId) } returns testManga3
        coEvery { mockRepository.updateManga(any()) } throws Exception("Update failed")

        // When & Then
        try {
            markMangasAsReadUseCase.markAsUnread(mangaId)
            assertThat(false).isTrue() // 不应该到达这里
        } catch (e: Exception) {
            assertThat(e.message).isEqualTo("Update failed")
            coVerify { mockRepository.getMangaById(mangaId) }
            coVerify { mockRepository.updateManga(any()) }
        }
    }

    // ========== 边界条件测试 ==========

    @Test
    fun `markSingleAsRead should handle negative manga id`() = runTest {
        // Given
        val negativeId = -1L
        coEvery { mockRepository.getMangaById(negativeId) } returns null

        // When
        markMangasAsReadUseCase.markSingleAsRead(negativeId)

        // Then
        coVerify { mockRepository.getMangaById(negativeId) }
        coVerify(exactly = 0) { mockRepository.updateManga(any()) }
    }

    @Test
    fun `markAsUnread should handle negative manga id`() = runTest {
        // Given
        val negativeId = -1L
        coEvery { mockRepository.getMangaById(negativeId) } returns null

        // When
        markMangasAsReadUseCase.markAsUnread(negativeId)

        // Then
        coVerify { mockRepository.getMangaById(negativeId) }
        coVerify(exactly = 0) { mockRepository.updateManga(any()) }
    }

    // ========== 集成测试 ==========

    @Test
    fun `complete workflow - mark as read then mark as unread`() = runTest {
        // Given
        val mangaId = 1L
        val readManga = testManga1.copy(
            currentPage = testManga1.pageCount - 1,
            lastRead = System.currentTimeMillis()
        )
        
        // 第一次调用返回原始漫画，第二次调用返回已读漫画
        coEvery { mockRepository.getMangaById(mangaId) } returns testManga1 andThen readManga
        coEvery { mockRepository.updateManga(any()) } returns 1L

        // When - 先标记为已读
        markMangasAsReadUseCase.markSingleAsRead(mangaId)
        
        // Then - 验证标记为已读
        coVerify { 
            mockRepository.updateManga(match { updatedManga ->
                updatedManga.currentPage == testManga1.pageCount - 1 &&
                updatedManga.lastRead > testManga1.lastRead
            })
        }

        // When - 再标记为未读
        markMangasAsReadUseCase.markAsUnread(mangaId)
        
        // Then - 验证标记为未读
        coVerify { 
            mockRepository.updateManga(match { updatedManga ->
                updatedManga.currentPage == 0 &&
                updatedManga.lastRead == 0L
            })
        }
        
        // 总共调用了2次getMangaById和2次updateManga
        coVerify(exactly = 2) { mockRepository.getMangaById(mangaId) }
        coVerify(exactly = 2) { mockRepository.updateManga(any()) }
    }
}
