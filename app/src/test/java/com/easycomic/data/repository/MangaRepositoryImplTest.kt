package com.easycomic.data.repository

import com.easycomic.data.dao.MangaDao
import com.easycomic.data.entity.MangaEntity
import com.easycomic.data.entity.ReadingStatus as DataReadingStatus
import com.easycomic.domain.model.Manga
import com.easycomic.domain.model.ReadingStatus as DomainReadingStatus
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
 * MangaRepositoryImpl 测试类
 */
@RunWith(MockitoJUnitRunner::class)
class MangaRepositoryImplTest {

    @Mock
    private lateinit var mangaDao: MangaDao

    private lateinit var mangaRepository: MangaRepositoryImpl

    @Before
    fun setup() {
        mangaRepository = MangaRepositoryImpl(mangaDao)
    }

    @Test
    fun `when getting all manga, should return flow of manga list`() = runTest {
        // Given
        val mockEntities = listOf(
            createMockMangaEntity(id = 1L, title = "漫画1"),
            createMockMangaEntity(id = 2L, title = "漫画2")
        )
        val expectedManga = listOf(
            createMockManga(id = 1L, title = "漫画1"),
            createMockManga(id = 2L, title = "漫画2")
        )
        
        whenever(mangaDao.getAllManga()).thenReturn(flow { emit(mockEntities) })

        // When
        val result = mangaRepository.getAllManga().first()

        // Then
        assertEquals(2, result.size)
        assertEquals(expectedManga[0], result[0])
        assertEquals(expectedManga[1], result[1])
        verify(mangaDao).getAllManga()
    }

    @Test
    fun `when getting manga by id, should return manga if exists`() = runTest {
        // Given
        val mockEntity = createMockMangaEntity(id = 1L, title = "测试漫画")
        val expectedManga = createMockManga(id = 1L, title = "测试漫画")
        
        whenever(mangaDao.getMangaById(1L)).thenReturn(mockEntity)

        // When
        val result = mangaRepository.getMangaById(1L)

        // Then
        assertNotNull(result)
        assertEquals(expectedManga, result)
        verify(mangaDao).getMangaById(1L)
    }

    @Test
    fun `when getting manga by id, should return null if not exists`() = runTest {
        // Given
        whenever(mangaDao.getMangaById(999L)).thenReturn(null)

        // When
        val result = mangaRepository.getMangaById(999L)

        // Then
        assertNull(result)
        verify(mangaDao).getMangaById(999L)
    }

    @Test
    fun `when getting manga by file path, should return manga if exists`() = runTest {
        // Given
        val filePath = "/path/to/comic.cbz"
        val mockEntity = createMockMangaEntity(id = 1L, title = "测试漫画", filePath = filePath)
        val expectedManga = createMockManga(id = 1L, title = "测试漫画", filePath = filePath)
        
        whenever(mangaDao.getMangaByFilePath(filePath)).thenReturn(mockEntity)

        // When
        val result = mangaRepository.getMangaByFilePath(filePath)

        // Then
        assertNotNull(result)
        assertEquals(expectedManga, result)
        verify(mangaDao).getMangaByFilePath(filePath)
    }

    @Test
    fun `when searching manga, should return filtered results`() = runTest {
        // Given
        val query = "测试"
        val mockEntities = listOf(
            createMockMangaEntity(id = 1L, title = "测试漫画"),
            createMockMangaEntity(id = 2L, title = "另一个测试")
        )
        val expectedManga = listOf(
            createMockManga(id = 1L, title = "测试漫画"),
            createMockManga(id = 2L, title = "另一个测试")
        )
        
        whenever(mangaDao.searchManga(query)).thenReturn(flow { emit(mockEntities) })

        // When
        val result = mangaRepository.searchManga(query).first()

        // Then
        assertEquals(2, result.size)
        assertEquals(expectedManga[0], result[0])
        assertEquals(expectedManga[1], result[1])
        verify(mangaDao).searchManga(query)
    }

    @Test
    fun `when getting favorite manga, should return only favorites`() = runTest {
        // Given
        val mockEntities = listOf(
            createMockMangaEntity(id = 1L, title = "收藏漫画1", isFavorite = true),
            createMockMangaEntity(id = 2L, title = "收藏漫画2", isFavorite = true)
        )
        val expectedManga = listOf(
            createMockManga(id = 1L, title = "收藏漫画1", isFavorite = true),
            createMockManga(id = 2L, title = "收藏漫画2", isFavorite = true)
        )
        
        whenever(mangaDao.getFavoriteManga()).thenReturn(flow { emit(mockEntities) })

        // When
        val result = mangaRepository.getFavoriteManga().first()

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.isFavorite })
        assertEquals(expectedManga[0], result[0])
        assertEquals(expectedManga[1], result[1])
        verify(mangaDao).getFavoriteManga()
    }

    @Test
    fun `when getting manga by status, should return filtered by status`() = runTest {
        // Given
        val status = DomainReadingStatus.READING
        val mockEntities = listOf(
            createMockMangaEntity(id = 1L, title = "阅读中", readingStatus = DataReadingStatus.READING),
            createMockMangaEntity(id = 2L, title = "也在阅读中", readingStatus = DataReadingStatus.READING)
        )
        val expectedManga = listOf(
            createMockManga(id = 1L, title = "阅读中", readingStatus = DomainReadingStatus.READING),
            createMockManga(id = 2L, title = "也在阅读中", readingStatus = DomainReadingStatus.READING)
        )
        
        whenever(mangaDao.getMangaByStatus(DataReadingStatus.READING)).thenReturn(flow { emit(mockEntities) })

        // When
        val result = mangaRepository.getMangaByStatus(status).first()

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.readingStatus == DomainReadingStatus.READING })
        assertEquals(expectedManga[0], result[0])
        assertEquals(expectedManga[1], result[1])
        verify(mangaDao).getMangaByStatus(DataReadingStatus.READING)
    }

    @Test
    fun `when getting recent manga, should return limited results`() = runTest {
        // Given
        val limit = 5
        val mockEntities = listOf(
            createMockMangaEntity(id = 1L, title = "最近1"),
            createMockMangaEntity(id = 2L, title = "最近2")
        )
        val expectedManga = listOf(
            createMockManga(id = 1L, title = "最近1"),
            createMockManga(id = 2L, title = "最近2")
        )
        
        whenever(mangaDao.getRecentManga(limit)).thenReturn(flow { emit(mockEntities) })

        // When
        val result = mangaRepository.getRecentManga(limit).first()

        // Then
        assertEquals(2, result.size)
        assertEquals(expectedManga[0], result[0])
        assertEquals(expectedManga[1], result[1])
        verify(mangaDao).getRecentManga(limit)
    }

    @Test
    fun `when inserting manga, should return manga id`() = runTest {
        // Given
        val manga = createMockManga(id = 0L) // ID为0表示新插入
        val expectedId = 1L
        val entity = manga.toEntity()
        
        whenever(mangaDao.insertOrUpdateManga(entity)).thenReturn(expectedId)

        // When
        val result = mangaRepository.insertOrUpdateManga(manga)

        // Then
        assertEquals(expectedId, result)
        verify(mangaDao).insertOrUpdateManga(entity)
    }

    @Test
    fun `when updating manga, should return manga id`() = runTest {
        // Given
        val manga = createMockManga(id = 1L)
        val expectedId = 1L
        val entity = manga.toEntity()
        
        whenever(mangaDao.insertOrUpdateManga(entity)).thenReturn(expectedId)

        // When
        val result = mangaRepository.insertOrUpdateManga(manga)

        // Then
        assertEquals(expectedId, result)
        verify(mangaDao).insertOrUpdateManga(entity)
    }

    @Test
    fun `when inserting multiple manga, should return list of ids`() = runTest {
        // Given
        val mangaList = listOf(
            createMockManga(id = 0L, title = "漫画1"),
            createMockManga(id = 0L, title = "漫画2")
        )
        val expectedIds = listOf(1L, 2L)
        val entities = mangaList.map { it.toEntity() }
        
        whenever(mangaDao.insertAllManga(entities)).thenReturn(expectedIds)

        // When
        val result = mangaRepository.insertAllManga(mangaList)

        // Then
        assertEquals(expectedIds, result)
        verify(mangaDao).insertAllManga(entities)
    }

    @Test
    fun `when updating reading progress, should call dao with correct parameters`() = runTest {
        // Given
        val mangaId = 1L
        val currentPage = 10
        val status = DomainReadingStatus.READING

        // When
        mangaRepository.updateReadingProgress(mangaId, currentPage, status)

        // Then
        verify(mangaDao).updateReadingProgress(mangaId, currentPage, DataReadingStatus.READING)
    }

    @Test
    fun `when toggling favorite, should call dao with manga id`() = runTest {
        // Given
        val mangaId = 1L

        // When
        mangaRepository.toggleFavorite(mangaId)

        // Then
        verify(mangaDao).toggleFavorite(mangaId)
    }

    @Test
    fun `when updating rating, should call dao with correct parameters`() = runTest {
        // Given
        val mangaId = 1L
        val rating = 4.5f

        // When
        mangaRepository.updateRating(mangaId, rating)

        // Then
        verify(mangaDao).updateRating(mangaId, rating)
    }

    @Test
    fun `when deleting manga, should call dao with entity`() = runTest {
        // Given
        val manga = createMockManga(id = 1L)
        val entity = manga.toEntity()

        // When
        mangaRepository.deleteManga(manga)

        // Then
        verify(mangaDao).deleteManga(entity)
    }

    @Test
    fun `when deleting multiple manga, should call dao with entities`() = runTest {
        // Given
        val mangaList = listOf(
            createMockManga(id = 1L),
            createMockManga(id = 2L)
        )
        val entities = mangaList.map { it.toEntity() }

        // When
        mangaRepository.deleteAllManga(mangaList)

        // Then
        verify(mangaDao).deleteAllManga(entities)
    }

    @Test
    fun `when getting manga count, should return count flow`() = runTest {
        // Given
        val expectedCount = 42
        whenever(mangaDao.getMangaCount()).thenReturn(flow { emit(expectedCount) })

        // When
        val result = mangaRepository.getMangaCount().first()

        // Then
        assertEquals(expectedCount, result)
        verify(mangaDao).getMangaCount()
    }

    @Test
    fun `when getting favorite count, should return favorite count flow`() = runTest {
        // Given
        val expectedCount = 10
        whenever(mangaDao.getFavoriteCount()).thenReturn(flow { emit(expectedCount) })

        // When
        val result = mangaRepository.getFavoriteCount().first()

        // Then
        assertEquals(expectedCount, result)
        verify(mangaDao).getFavoriteCount()
    }

    @Test
    fun `when getting completed count, should return completed count flow`() = runTest {
        // Given
        val expectedCount = 25
        whenever(mangaDao.getCompletedCount()).thenReturn(flow { emit(expectedCount) })

        // When
        val result = mangaRepository.getCompletedCount().first()

        // Then
        assertEquals(expectedCount, result)
        verify(mangaDao).getCompletedCount()
    }

    private fun createMockMangaEntity(
        id: Long,
        title: String,
        filePath: String = "/path/to/comic.cbz",
        isFavorite: Boolean = false,
        readingStatus: DataReadingStatus = DataReadingStatus.UNREAD
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
            rating = 4.5f,
            isFavorite = isFavorite,
            readingStatus = readingStatus,
            tags = "",
            lastRead = System.currentTimeMillis(),
            dateAdded = System.currentTimeMillis(),
            dateModified = System.currentTimeMillis()
        )
    }

    private fun createMockManga(
        id: Long,
        title: String,
        filePath: String = "/path/to/comic.cbz",
        isFavorite: Boolean = false,
        readingStatus: DomainReadingStatus = DomainReadingStatus.UNREAD
    ): Manga {
        return Manga(
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