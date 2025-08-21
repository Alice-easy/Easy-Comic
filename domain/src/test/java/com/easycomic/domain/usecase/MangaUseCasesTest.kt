package com.easycomic.domain.usecase

import com.easycomic.domain.model.Manga
import com.easycomic.domain.model.ReadingStatus
import com.easycomic.domain.repository.MangaRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * MangaUseCases单元测试
 */
class MangaUseCasesTest {

    private lateinit var mangaRepository: MangaRepository
    private lateinit var mangaUseCases: MangaUseCases

    private val testManga = Manga(
        id = 1L,
        title = "测试漫画",
        author = "测试作者",
        filePath = "/test/path",
        pageCount = 100,
        currentPage = 50
    )

    @Before
    fun setup() {
        mangaRepository = mockk()
        mangaUseCases = MangaUseCases(mangaRepository)
    }

    @Test
    fun `getAllManga should return flow of manga list`() = runTest {
        // Given
        val mangaList = listOf(testManga)
        every { mangaRepository.getAllManga() } returns flowOf(mangaList)

        // When
        val result = mangaUseCases.getAllManga()

        // Then
        result.collect { mangas ->
            assertEquals(1, mangas.size)
            assertEquals(testManga, mangas.first())
        }
    }

    @Test
    fun `getMangaById should return manga when exists`() = runTest {
        // Given
        coEvery { mangaRepository.getMangaById(1L) } returns testManga

        // When
        val result = mangaUseCases.getMangaById(1L)

        // Then
        assertEquals(testManga, result)
    }

    @Test
    fun `getMangaById should return null when not exists`() = runTest {
        // Given
        coEvery { mangaRepository.getMangaById(999L) } returns null

        // When
        val result = mangaUseCases.getMangaById(999L)

        // Then
        assertNull(result)
    }

    @Test
    fun `searchManga should return filtered results`() = runTest {
        // Given
        val searchQuery = "测试"
        val searchResults = listOf(testManga)
        every { mangaRepository.searchManga(searchQuery) } returns flowOf(searchResults)

        // When
        val result = mangaUseCases.searchManga(searchQuery)

        // Then
        result.collect { mangas ->
            assertEquals(1, mangas.size)
            assertEquals(testManga, mangas.first())
        }
    }

    @Test
    fun `getFavoriteManga should return favorite manga list`() = runTest {
        // Given
        val favoriteManga = testManga.copy(isFavorite = true)
        every { mangaRepository.getFavoriteManga() } returns flowOf(listOf(favoriteManga))

        // When
        val result = mangaUseCases.getFavoriteManga()

        // Then
        result.collect { mangas ->
            assertEquals(1, mangas.size)
            assertEquals(true, mangas.first().isFavorite)
        }
    }

    @Test
    fun `getRecentManga should return recent manga with limit`() = runTest {
        // Given
        val limit = 5
        val recentManga = listOf(testManga)
        every { mangaRepository.getRecentManga(limit) } returns flowOf(recentManga)

        // When
        val result = mangaUseCases.getRecentManga(limit)

        // Then
        result.collect { mangas ->
            assertEquals(1, mangas.size)
        }
        coVerify { mangaRepository.getRecentManga(limit) }
    }

    @Test
    fun `insertOrUpdateManga should return manga id`() = runTest {
        // Given
        val expectedId = 1L
        coEvery { mangaRepository.insertOrUpdateManga(testManga) } returns expectedId

        // When
        val result = mangaUseCases.insertOrUpdateManga(testManga)

        // Then
        assertEquals(expectedId, result)
        coVerify { mangaRepository.insertOrUpdateManga(testManga) }
    }

    @Test
    fun `updateReadingProgress should call repository`() = runTest {
        // Given
        val mangaId = 1L
        val currentPage = 75
        val status = ReadingStatus.READING
        coEvery { mangaRepository.updateReadingProgress(mangaId, currentPage, status) } returns Unit

        // When
        mangaUseCases.updateReadingProgress(mangaId, currentPage, status)

        // Then
        coVerify { mangaRepository.updateReadingProgress(mangaId, currentPage, status) }
    }

    @Test
    fun `toggleFavorite should call repository`() = runTest {
        // Given
        val mangaId = 1L
        coEvery { mangaRepository.toggleFavorite(mangaId) } returns Unit

        // When
        mangaUseCases.toggleFavorite(mangaId)

        // Then
        coVerify { mangaRepository.toggleFavorite(mangaId) }
    }

    @Test
    fun `updateRating should call repository`() = runTest {
        // Given
        val mangaId = 1L
        val rating = 4.5f
        coEvery { mangaRepository.updateRating(mangaId, rating) } returns Unit

        // When
        mangaUseCases.updateRating(mangaId, rating)

        // Then
        coVerify { mangaRepository.updateRating(mangaId, rating) }
    }

    @Test
    fun `insertAllManga should return list of ids`() = runTest {
        // Given
        val mangaList = listOf(testManga, testManga.copy(id = 2L))
        val expectedIds = listOf(1L, 2L)
        coEvery { mangaRepository.insertAllManga(mangaList) } returns expectedIds

        // When
        val result = mangaUseCases.insertAllManga(mangaList)

        // Then
        assertEquals(expectedIds, result)
        coVerify { mangaRepository.insertAllManga(mangaList) }
    }

    @Test
    fun `deleteManga should call repository`() = runTest {
        // Given
        coEvery { mangaRepository.deleteManga(testManga) } returns Unit

        // When
        mangaUseCases.deleteManga(testManga)

        // Then
        coVerify { mangaRepository.deleteManga(testManga) }
    }

    @Test
    fun `deleteAllManga should call repository`() = runTest {
        // Given
        val mangaList = listOf(testManga)
        coEvery { mangaRepository.deleteAllManga(mangaList) } returns Unit

        // When
        mangaUseCases.deleteAllManga(mangaList)

        // Then
        coVerify { mangaRepository.deleteAllManga(mangaList) }
    }

    @Test
    fun `markMangasAsRead should update reading progress for all manga`() = runTest {
        // Given
        val mangaIds = listOf(1L, 2L)
        val manga1 = testManga.copy(id = 1L, pageCount = 100)
        val manga2 = testManga.copy(id = 2L, pageCount = 150)
        
        coEvery { mangaRepository.getMangaById(1L) } returns manga1
        coEvery { mangaRepository.getMangaById(2L) } returns manga2
        coEvery { mangaRepository.updateReadingProgress(any(), any(), any()) } returns Unit

        // When
        mangaUseCases.markMangasAsRead(mangaIds)

        // Then
        coVerify { mangaRepository.updateReadingProgress(1L, 100, ReadingStatus.COMPLETED) }
        coVerify { mangaRepository.updateReadingProgress(2L, 150, ReadingStatus.COMPLETED) }
    }

    @Test
    fun `getMangaCount should return count flow`() = runTest {
        // Given
        val expectedCount = 10
        every { mangaRepository.getMangaCount() } returns flowOf(expectedCount)

        // When
        val result = mangaUseCases.getMangaCount()

        // Then
        result.collect { count ->
            assertEquals(expectedCount, count)
        }
    }

    @Test
    fun `getFavoriteCount should return favorite count flow`() = runTest {
        // Given
        val expectedCount = 5
        every { mangaRepository.getFavoriteCount() } returns flowOf(expectedCount)

        // When
        val result = mangaUseCases.getFavoriteCount()

        // Then
        result.collect { count ->
            assertEquals(expectedCount, count)
        }
    }

    @Test
    fun `getCompletedCount should return completed count flow`() = runTest {
        // Given
        val expectedCount = 3
        every { mangaRepository.getCompletedCount() } returns flowOf(expectedCount)

        // When
        val result = mangaUseCases.getCompletedCount()

        // Then
        result.collect { count ->
            assertEquals(expectedCount, count)
        }
    }
}