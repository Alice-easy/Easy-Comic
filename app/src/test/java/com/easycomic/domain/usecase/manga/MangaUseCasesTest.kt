package com.easycomic.domain.usecase.manga

import com.easycomic.domain.model.Manga
import com.easycomic.domain.model.ReadingStatus
import com.easycomic.domain.repository.MangaRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * MangaUseCases 测试类
 */
@RunWith(MockitoJUnitRunner::class)
class MangaUseCasesTest {

    @Mock
    private lateinit var mangaRepository: MangaRepository

    private lateinit var getAllMangaUseCase: GetAllMangaUseCase
    private lateinit var getMangaByIdUseCase: GetMangaByIdUseCase
    private lateinit var searchMangaUseCase: SearchMangaUseCase
    private lateinit var getFavoriteMangaUseCase: GetFavoriteMangaUseCase
    private lateinit var getRecentMangaUseCase: GetRecentMangaUseCase
    private lateinit var getMangaByStatusUseCase: GetMangaByStatusUseCase
    private lateinit var insertOrUpdateMangaUseCase: InsertOrUpdateMangaUseCase
    private lateinit var updateReadingProgressUseCase: UpdateReadingProgressUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase
    private lateinit var updateRatingUseCase: UpdateRatingUseCase
    private lateinit var deleteMangaUseCase: DeleteMangaUseCase
    private lateinit var deleteAllMangaUseCase: DeleteAllMangaUseCase

    @Before
    fun setup() {
        getAllMangaUseCase = GetAllMangaUseCase(mangaRepository)
        getMangaByIdUseCase = GetMangaByIdUseCase(mangaRepository)
        searchMangaUseCase = SearchMangaUseCase(mangaRepository)
        getFavoriteMangaUseCase = GetFavoriteMangaUseCase(mangaRepository)
        getRecentMangaUseCase = GetRecentMangaUseCase(mangaRepository)
        getMangaByStatusUseCase = GetMangaByStatusUseCase(mangaRepository)
        insertOrUpdateMangaUseCase = InsertOrUpdateMangaUseCase(mangaRepository)
        updateReadingProgressUseCase = UpdateReadingProgressUseCase(mangaRepository)
        toggleFavoriteUseCase = ToggleFavoriteUseCase(mangaRepository)
        updateRatingUseCase = UpdateRatingUseCase(mangaRepository)
        deleteMangaUseCase = DeleteMangaUseCase(mangaRepository)
        deleteAllMangaUseCase = DeleteAllMangaUseCase(mangaRepository)
    }

    @Test
    fun `when getting all manga, should return flow of manga list`() = runTest {
        // Given
        val expectedManga = listOf(
            createMockManga(id = 1L, title = "漫画1"),
            createMockManga(id = 2L, title = "漫画2")
        )
        
        whenever(mangaRepository.getAllManga()).thenReturn(flow { emit(expectedManga) })

        // When
        val result = getAllMangaUseCase().first()

        // Then
        assertEquals(expectedManga, result)
        verify(mangaRepository).getAllManga()
    }

    @Test
    fun `when getting manga by id, should return manga if exists`() = runTest {
        // Given
        val expectedManga = createMockManga(id = 1L, title = "测试漫画")
        whenever(mangaRepository.getMangaById(1L)).thenReturn(expectedManga)

        // When
        val result = getMangaByIdUseCase(1L)

        // Then
        assertNotNull(result)
        assertEquals(expectedManga, result)
        verify(mangaRepository).getMangaById(1L)
    }

    @Test
    fun `when getting manga by id, should return null if not exists`() = runTest {
        // Given
        whenever(mangaRepository.getMangaById(999L)).thenReturn(null)

        // When
        val result = getMangaByIdUseCase(999L)

        // Then
        assertNull(result)
        verify(mangaRepository).getMangaById(999L)
    }

    @Test
    fun `when searching manga, should return filtered results`() = runTest {
        // Given
        val query = "测试"
        val expectedResults = listOf(
            createMockManga(id = 1L, title = "测试漫画"),
            createMockManga(id = 2L, title = "另一个测试")
        )
        
        whenever(mangaRepository.searchManga(query)).thenReturn(flow { emit(expectedResults) })

        // When
        val result = searchMangaUseCase(query).first()

        // Then
        assertEquals(expectedResults, result)
        verify(mangaRepository).searchManga(query)
    }

    @Test
    fun `when getting favorite manga, should return only favorites`() = runTest {
        // Given
        val expectedFavorites = listOf(
            createMockManga(id = 1L, title = "收藏漫画1", isFavorite = true),
            createMockManga(id = 2L, title = "收藏漫画2", isFavorite = true)
        )
        
        whenever(mangaRepository.getFavoriteManga()).thenReturn(flow { emit(expectedFavorites) })

        // When
        val result = getFavoriteMangaUseCase().first()

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.isFavorite })
        verify(mangaRepository).getFavoriteManga()
    }

    @Test
    fun `when getting recent manga, should return limited results`() = runTest {
        // Given
        val limit = 5
        val expectedRecent = listOf(
            createMockManga(id = 1L, title = "最近1"),
            createMockManga(id = 2L, title = "最近2")
        )
        
        whenever(mangaRepository.getRecentManga(limit)).thenReturn(flow { emit(expectedRecent) })

        // When
        val result = getRecentMangaUseCase(limit).first()

        // Then
        assertEquals(expectedRecent, result)
        verify(mangaRepository).getRecentManga(limit)
    }

    @Test
    fun `when getting manga by status, should return filtered by status`() = runTest {
        // Given
        val status = ReadingStatus.READING
        val expectedResults = listOf(
            createMockManga(id = 1L, title = "阅读中", readingStatus = ReadingStatus.READING),
            createMockManga(id = 2L, title = "也在阅读中", readingStatus = ReadingStatus.READING)
        )
        
        whenever(mangaRepository.getMangaByStatus(status)).thenReturn(flow { emit(expectedResults) })

        // When
        val result = getMangaByStatusUseCase(status).first()

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.readingStatus == ReadingStatus.READING })
        verify(mangaRepository).getMangaByStatus(status)
    }

    @Test
    fun `when inserting new manga, should return manga id`() = runTest {
        // Given
        val manga = createMockManga(id = 0L) // ID为0表示新插入
        val expectedId = 1L
        whenever(mangaRepository.insertOrUpdateManga(manga)).thenReturn(expectedId)

        // When
        val result = insertOrUpdateMangaUseCase(manga)

        // Then
        assertEquals(expectedId, result)
        verify(mangaRepository).insertOrUpdateManga(manga)
    }

    @Test
    fun `when updating existing manga, should return manga id`() = runTest {
        // Given
        val manga = createMockManga(id = 1L)
        val expectedId = 1L
        whenever(mangaRepository.insertOrUpdateManga(manga)).thenReturn(expectedId)

        // When
        val result = insertOrUpdateMangaUseCase(manga)

        // Then
        assertEquals(expectedId, result)
        verify(mangaRepository).insertOrUpdateManga(manga)
    }

    @Test
    fun `when updating reading progress, should call repository with correct parameters`() = runTest {
        // Given
        val params = UpdateReadingProgressUseCase.Params(
            mangaId = 1L,
            currentPage = 10,
            status = ReadingStatus.READING
        )
        
        whenever(mangaRepository.updateReadingProgress(any(), any(), any())).thenReturn(Unit)

        // When
        updateReadingProgressUseCase(params)

        // Then
        verify(mangaRepository).updateReadingProgress(
            mangaId = 1L,
            currentPage = 10,
            status = ReadingStatus.READING
        )
    }

    @Test
    fun `when toggling favorite, should call repository with manga id`() = runTest {
        // Given
        val mangaId = 1L
        whenever(mangaRepository.toggleFavorite(mangaId)).thenReturn(Unit)

        // When
        toggleFavoriteUseCase(mangaId)

        // Then
        verify(mangaRepository).toggleFavorite(mangaId)
    }

    @Test
    fun `when updating rating, should call repository with correct parameters`() = runTest {
        // Given
        val params = UpdateRatingUseCase.Params(
            mangaId = 1L,
            rating = 4.5f
        )
        
        whenever(mangaRepository.updateRating(any(), any())).thenReturn(Unit)

        // When
        updateRatingUseCase(params)

        // Then
        verify(mangaRepository).updateRating(mangaId = 1L, rating = 4.5f)
    }

    @Test
    fun `when deleting manga, should call repository with manga`() = runTest {
        // Given
        val manga = createMockManga(id = 1L)
        whenever(mangaRepository.deleteManga(manga)).thenReturn(Unit)

        // When
        deleteMangaUseCase(manga)

        // Then
        verify(mangaRepository).deleteManga(manga)
    }

    @Test
    fun `when deleting multiple manga, should call repository with manga list`() = runTest {
        // Given
        val mangaList = listOf(
            createMockManga(id = 1L),
            createMockManga(id = 2L)
        )
        whenever(mangaRepository.deleteAllManga(mangaList)).thenReturn(Unit)

        // When
        deleteAllMangaUseCase(mangaList)

        // Then
        verify(mangaRepository).deleteAllManga(mangaList)
    }

    @Test
    fun `when updating reading progress, should handle all reading statuses`() = runTest {
        // Given
        val readingStatuses = listOf(
            ReadingStatus.UNREAD,
            ReadingStatus.READING,
            ReadingStatus.COMPLETED,
            ReadingStatus.PAUSED,
            ReadingStatus.DROPPED
        )
        
        whenever(mangaRepository.updateReadingProgress(any(), any(), any())).thenReturn(Unit)

        // When & Then
        readingStatuses.forEach { status ->
            val params = UpdateReadingProgressUseCase.Params(
                mangaId = 1L,
                currentPage = 5,
                status = status
            )
            
            updateReadingProgressUseCase(params)
            
            verify(mangaRepository).updateReadingProgress(
                mangaId = 1L,
                currentPage = 5,
                status = status
            )
        }
    }

    @Test
    fun `when updating rating, should handle valid rating range`() = runTest {
        // Given
        val validRatings = listOf(0.0f, 2.5f, 5.0f)
        whenever(mangaRepository.updateRating(any(), any())).thenReturn(Unit)

        // When & Then
        validRatings.forEach { rating ->
            val params = UpdateRatingUseCase.Params(
                mangaId = 1L,
                rating = rating
            )
            
            updateRatingUseCase(params)
            
            verify(mangaRepository).updateRating(mangaId = 1L, rating = rating)
        }
    }

    @Test
    fun `when getting recent manga, should handle different limit values`() = runTest {
        // Given
        val limits = listOf(5, 10, 20, 50)
        
        // When & Then
        limits.forEach { limit ->
            whenever(mangaRepository.getRecentManga(limit)).thenReturn(flow { emit(emptyList()) })
            
            val result = getRecentMangaUseCase(limit).first()
            
            assertTrue(result.isEmpty())
            verify(mangaRepository).getRecentManga(limit)
        }
    }

    @Test
    fun `when searching with empty query, should return empty results`() = runTest {
        // Given
        val emptyQuery = ""
        whenever(mangaRepository.searchManga(emptyQuery)).thenReturn(flow { emit(emptyList()) })

        // When
        val result = searchMangaUseCase(emptyQuery).first()

        // Then
        assertTrue(result.isEmpty())
        verify(mangaRepository).searchManga(emptyQuery)
    }

    @Test
    fun `when searching with special characters, should handle gracefully`() = runTest {
        // Given
        val specialQuery = "测试@#$%"
        whenever(mangaRepository.searchManga(specialQuery)).thenReturn(flow { emit(emptyList()) })

        // When
        val result = searchMangaUseCase(specialQuery).first()

        // Then
        assertTrue(result.isEmpty())
        verify(mangaRepository).searchManga(specialQuery)
    }

    private fun createMockManga(
        id: Long,
        title: String,
        isFavorite: Boolean = false,
        readingStatus: ReadingStatus = ReadingStatus.UNREAD
    ): Manga {
        return Manga(
            id = id,
            title = title,
            author = "测试作者",
            description = "测试描述",
            filePath = "/path/to/comic.cbz",
            fileUri = "content://com.example.provider/comic.cbz",
            fileFormat = "CBZ",
            fileSize = 1024000L,
            pageCount = 100,
            currentPage = 0,
            coverImagePath = "/path/to/cover.jpg",
            thumbnailPath = "/path/to/thumbnail.jpg",
            rating = 4.5f,
            isFavorite = isFavorite,
            readingStatus = readingStatus,
            tags = emptyList(),
            lastRead = System.currentTimeMillis(),
            dateAdded = System.currentTimeMillis(),
            dateModified = System.currentTimeMillis()
        )
    }
}