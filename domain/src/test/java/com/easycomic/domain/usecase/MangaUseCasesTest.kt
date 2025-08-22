package com.easycomic.domain.usecase

import com.easycomic.domain.model.Manga
import com.easycomic.domain.model.ReadingStatus
import com.easycomic.domain.repository.MangaRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * MangaUseCases单元测试
 * 
 * 测试漫画相关业务逻辑的各种场景，包括：
 * - 正常流程测试
 * - 边界条件测试
 * - 异常处理测试
 * - 数据流测试
 */
class MangaUseCasesTest {

    private val mockRepository = mockk<MangaRepository>()
    private lateinit var mangaUseCases: MangaUseCases

    // 测试数据
    private val testManga1 = Manga(
        id = 1L,
        title = "Test Manga 1",
        filePath = "/test/path/manga1.zip",
        pageCount = 20,
        currentPage = 5,
        readingStatus = ReadingStatus.READING,
        isFavorite = false,
        rating = 0f
    )

    private val testManga2 = Manga(
        id = 2L,
        title = "Test Manga 2",
        filePath = "/test/path/manga2.zip",
        pageCount = 30,
        currentPage = 30,
        readingStatus = ReadingStatus.COMPLETED,
        isFavorite = true,
        rating = 4.5f
    )

    private val testMangaList = listOf(testManga1, testManga2)

    @Before
    fun setup() {
        mangaUseCases = MangaUseCases(mockRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // ========== 查询操作测试 ==========

    @Test
    fun `getAllManga should return manga list from repository`() = runTest {
        // Given
        every { mockRepository.getAllManga() } returns flowOf(testMangaList)

        // When
        val result = mangaUseCases.getAllManga().first()

        // Then
        assertThat(result).isEqualTo(testMangaList)
        assertThat(result).hasSize(2)
        verify { mockRepository.getAllManga() }
    }

    @Test
    fun `getAllManga should return empty list when repository is empty`() = runTest {
        // Given
        every { mockRepository.getAllManga() } returns flowOf(emptyList())

        // When
        val result = mangaUseCases.getAllManga().first()

        // Then
        assertThat(result).isEmpty()
        verify { mockRepository.getAllManga() }
    }

    @Test
    fun `getMangaById should return manga when exists`() = runTest {
        // Given
        val mangaId = 1L
        coEvery { mockRepository.getMangaById(mangaId) } returns testManga1

        // When
        val result = mangaUseCases.getMangaById(mangaId)

        // Then
        assertThat(result).isEqualTo(testManga1)
        coVerify { mockRepository.getMangaById(mangaId) }
    }

    @Test
    fun `getMangaById should return null when manga does not exist`() = runTest {
        // Given
        val nonExistentId = 999L
        coEvery { mockRepository.getMangaById(nonExistentId) } returns null

        // When
        val result = mangaUseCases.getMangaById(nonExistentId)

        // Then
        assertThat(result).isNull()
        coVerify { mockRepository.getMangaById(nonExistentId) }
    }

    @Test
    fun `searchManga should return filtered results`() = runTest {
        // Given
        val query = "Test"
        val searchResults = listOf(testManga1)
        every { mockRepository.searchManga(query) } returns flowOf(searchResults)

        // When
        val result = mangaUseCases.searchManga(query).first()

        // Then
        assertThat(result).isEqualTo(searchResults)
        assertThat(result).hasSize(1)
        verify { mockRepository.searchManga(query) }
    }

    @Test
    fun `searchManga should return empty list for no matches`() = runTest {
        // Given
        val query = "NonExistent"
        every { mockRepository.searchManga(query) } returns flowOf(emptyList())

        // When
        val result = mangaUseCases.searchManga(query).first()

        // Then
        assertThat(result).isEmpty()
        verify { mockRepository.searchManga(query) }
    }

    @Test
    fun `searchManga should handle empty query`() = runTest {
        // Given
        val emptyQuery = ""
        every { mockRepository.searchManga(emptyQuery) } returns flowOf(emptyList())

        // When
        val result = mangaUseCases.searchManga(emptyQuery).first()

        // Then
        assertThat(result).isEmpty()
        verify { mockRepository.searchManga(emptyQuery) }
    }

    @Test
    fun `getFavoriteManga should return only favorite manga`() = runTest {
        // Given
        val favoriteList = listOf(testManga2)
        every { mockRepository.getFavoriteManga() } returns flowOf(favoriteList)

        // When
        val result = mangaUseCases.getFavoriteManga().first()

        // Then
        assertThat(result).isEqualTo(favoriteList)
        assertThat(result).hasSize(1)
        assertThat(result[0].isFavorite).isTrue()
        verify { mockRepository.getFavoriteManga() }
    }

    @Test
    fun `getRecentManga should return limited results with default limit`() = runTest {
        // Given
        every { mockRepository.getRecentManga(10) } returns flowOf(testMangaList)

        // When
        val result = mangaUseCases.getRecentManga().first()

        // Then
        assertThat(result).isEqualTo(testMangaList)
        verify { mockRepository.getRecentManga(10) }
    }

    @Test
    fun `getRecentManga should respect custom limit`() = runTest {
        // Given
        val limit = 5
        every { mockRepository.getRecentManga(limit) } returns flowOf(listOf(testManga1))

        // When
        val result = mangaUseCases.getRecentManga(limit).first()

        // Then
        assertThat(result).hasSize(1)
        verify { mockRepository.getRecentManga(limit) }
    }

    // ========== 更新操作测试 ==========

    @Test
    fun `insertOrUpdateManga should return inserted id`() = runTest {
        // Given
        val expectedId = 1L
        coEvery { mockRepository.insertOrUpdateManga(testManga1) } returns expectedId

        // When
        val result = mangaUseCases.insertOrUpdateManga(testManga1)

        // Then
        assertThat(result).isEqualTo(expectedId)
        coVerify { mockRepository.insertOrUpdateManga(testManga1) }
    }

    @Test
    fun `updateReadingProgress should call repository with correct parameters`() = runTest {
        // Given
        val mangaId = 1L
        val currentPage = 15
        val status = ReadingStatus.READING
        coEvery { mockRepository.updateReadingProgress(mangaId, currentPage, status) } just Runs

        // When
        mangaUseCases.updateReadingProgress(mangaId, currentPage, status)

        // Then
        coVerify { mockRepository.updateReadingProgress(mangaId, currentPage, status) }
    }

    @Test
    fun `toggleFavorite should call repository with correct id`() = runTest {
        // Given
        val mangaId = 1L
        coEvery { mockRepository.toggleFavorite(mangaId) } just Runs

        // When
        mangaUseCases.toggleFavorite(mangaId)

        // Then
        coVerify { mockRepository.toggleFavorite(mangaId) }
    }

    @Test
    fun `updateRating should call repository with valid rating`() = runTest {
        // Given
        val mangaId = 1L
        val rating = 4.5f
        coEvery { mockRepository.updateRating(mangaId, rating) } just Runs

        // When
        mangaUseCases.updateRating(mangaId, rating)

        // Then
        coVerify { mockRepository.updateRating(mangaId, rating) }
    }

    // ========== 批量操作测试 ==========

    @Test
    fun `insertAllManga should return list of ids`() = runTest {
        // Given
        val expectedIds = listOf(1L, 2L)
        coEvery { mockRepository.insertAllManga(testMangaList) } returns expectedIds

        // When
        val result = mangaUseCases.insertAllManga(testMangaList)

        // Then
        assertThat(result).isEqualTo(expectedIds)
        assertThat(result).hasSize(2)
        coVerify { mockRepository.insertAllManga(testMangaList) }
    }

    @Test
    fun `insertAllManga should handle empty list`() = runTest {
        // Given
        val emptyList = emptyList<Manga>()
        val expectedIds = emptyList<Long>()
        coEvery { mockRepository.insertAllManga(emptyList) } returns expectedIds

        // When
        val result = mangaUseCases.insertAllManga(emptyList)

        // Then
        assertThat(result).isEmpty()
        coVerify { mockRepository.insertAllManga(emptyList) }
    }

    @Test
    fun `deleteManga should call repository with correct manga`() = runTest {
        // Given
        coEvery { mockRepository.deleteManga(testManga1) } just Runs

        // When
        mangaUseCases.deleteManga(testManga1)

        // Then
        coVerify { mockRepository.deleteManga(testManga1) }
    }

    @Test
    fun `deleteAllManga should call repository with manga list`() = runTest {
        // Given
        coEvery { mockRepository.deleteAllManga(testMangaList) } just Runs

        // When
        mangaUseCases.deleteAllManga(testMangaList)

        // Then
        coVerify { mockRepository.deleteAllManga(testMangaList) }
    }

    @Test
    fun `markMangasAsRead should update progress for all manga`() = runTest {
        // Given
        val mangaIds = listOf(1L, 2L)
        val manga1 = testManga1.copy(pageCount = 20)
        val manga2 = testManga2.copy(pageCount = 30)
        
        coEvery { mockRepository.getMangaById(1L) } returns manga1
        coEvery { mockRepository.getMangaById(2L) } returns manga2
        coEvery { mockRepository.updateReadingProgress(any(), any(), any()) } just Runs

        // When
        mangaUseCases.markMangasAsRead(mangaIds)

        // Then
        coVerify { mockRepository.getMangaById(1L) }
        coVerify { mockRepository.getMangaById(2L) }
        coVerify { mockRepository.updateReadingProgress(1L, 20, ReadingStatus.COMPLETED) }
        coVerify { mockRepository.updateReadingProgress(2L, 30, ReadingStatus.COMPLETED) }
    }

    @Test
    fun `markMangasAsRead should handle non-existent manga gracefully`() = runTest {
        // Given
        val mangaIds = listOf(1L, 999L) // 999L 不存在
        coEvery { mockRepository.getMangaById(1L) } returns testManga1
        coEvery { mockRepository.getMangaById(999L) } returns null
        coEvery { mockRepository.updateReadingProgress(any(), any(), any()) } just Runs

        // When
        mangaUseCases.markMangasAsRead(mangaIds)

        // Then
        coVerify { mockRepository.getMangaById(1L) }
        coVerify { mockRepository.getMangaById(999L) }
        // 只为存在的漫画更新进度
        coVerify { mockRepository.updateReadingProgress(1L, testManga1.pageCount, ReadingStatus.COMPLETED) }
        // 不存在的漫画不会调用更新
        coVerify(exactly = 0) { mockRepository.updateReadingProgress(999L, any(), any()) }
    }

    @Test
    fun `markMangasAsRead should handle empty list`() = runTest {
        // Given
        val emptyIds = emptyList<Long>()

        // When
        mangaUseCases.markMangasAsRead(emptyIds)

        // Then
        // 没有调用任何repository方法
        coVerify(exactly = 0) { mockRepository.getMangaById(any()) }
        coVerify(exactly = 0) { mockRepository.updateReadingProgress(any(), any(), any()) }
    }

    // ========== 统计操作测试 ==========

    @Test
    fun `getMangaCount should return count from repository`() = runTest {
        // Given
        val expectedCount = 10
        every { mockRepository.getMangaCount() } returns flowOf(expectedCount)

        // When
        val result = mangaUseCases.getMangaCount().first()

        // Then
        assertThat(result).isEqualTo(expectedCount)
        verify { mockRepository.getMangaCount() }
    }

    @Test
    fun `getFavoriteCount should return favorite count from repository`() = runTest {
        // Given
        val expectedCount = 5
        every { mockRepository.getFavoriteCount() } returns flowOf(expectedCount)

        // When
        val result = mangaUseCases.getFavoriteCount().first()

        // Then
        assertThat(result).isEqualTo(expectedCount)
        verify { mockRepository.getFavoriteCount() }
    }

    @Test
    fun `getCompletedCount should return completed count from repository`() = runTest {
        // Given
        val expectedCount = 3
        every { mockRepository.getCompletedCount() } returns flowOf(expectedCount)

        // When
        val result = mangaUseCases.getCompletedCount().first()

        // Then
        assertThat(result).isEqualTo(expectedCount)
        verify { mockRepository.getCompletedCount() }
    }

    // ========== 边界条件和异常测试 ==========

    @Test
    fun `updateRating should handle boundary values`() = runTest {
        // Given
        val mangaId = 1L
        coEvery { mockRepository.updateRating(any(), any()) } just Runs

        // Test minimum rating
        mangaUseCases.updateRating(mangaId, 0f)
        coVerify { mockRepository.updateRating(mangaId, 0f) }

        // Test maximum rating
        mangaUseCases.updateRating(mangaId, 5f)
        coVerify { mockRepository.updateRating(mangaId, 5f) }
    }

    @Test
    fun `updateReadingProgress should handle boundary page values`() = runTest {
        // Given
        val mangaId = 1L
        coEvery { mockRepository.updateReadingProgress(any(), any(), any()) } just Runs

        // Test first page
        mangaUseCases.updateReadingProgress(mangaId, 0, ReadingStatus.UNREAD)
        coVerify { mockRepository.updateReadingProgress(mangaId, 0, ReadingStatus.UNREAD) }

        // Test last page
        val lastPage = 100
        mangaUseCases.updateReadingProgress(mangaId, lastPage, ReadingStatus.COMPLETED)
        coVerify { mockRepository.updateReadingProgress(mangaId, lastPage, ReadingStatus.COMPLETED) }
    }

    @Test
    fun `getCover should call repository with correct manga`() = runTest {
        // Given
        val expectedCoverData = null // Change to null as this should return Bitmap? 
        coEvery { mockRepository.getCover(testManga1) } returns expectedCoverData

        // When
        val result = mangaUseCases.getCover(testManga1)

        // Then
        assertThat(result).isEqualTo(expectedCoverData)
        coVerify { mockRepository.getCover(testManga1) }
    }
}
