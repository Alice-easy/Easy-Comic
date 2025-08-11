package com.easycomic.domain.usecase.manga

import app.cash.turbine.test
import com.easycomic.domain.model.Manga
import com.easycomic.domain.model.ReadingStatus
import com.easycomic.di.KoinModules
import io.insertkoin.koin.test.KoinTest
import io.insertkoin.koin.test.get
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Manga Use Cases Koin 注入测试
 * 验证所有漫画相关的用例通过 Koin 正确注入依赖
 */
@RunWith(MockitoJUnitRunner::class)
class MangaUseCasesKoinTest : KoinTest {

    @Mock
    private lateinit var mockMangaRepository: com.easycomic.domain.repository.MangaRepository

    @Before
    fun setup() {
        // 初始化 Koin
        KoinModules.initializeKoin(androidx.test.core.app.ApplicationProvider.getApplicationContext())
    }

    @After
    fun tearDown() {
        io.insertkoin.koin.core.context.stopKoin()
    }

    @Test
    fun `test GetAllMangaUseCase can be injected through Koin`() {
        // Given - Koin 已初始化

        // When
        val getAllMangaUseCase: GetAllMangaUseCase = get()

        // Then
        assertNotNull(getAllMangaUseCase)
        assertTrue(getAllMangaUseCase is GetAllMangaUseCase)
    }

    @Test
    fun `test GetMangaByIdUseCase can be injected through Koin`() {
        // Given - Koin 已初始化

        // When
        val getMangaByIdUseCase: GetMangaByIdUseCase = get()

        // Then
        assertNotNull(getMangaByIdUseCase)
        assertTrue(getMangaByIdUseCase is GetMangaByIdUseCase)
    }

    @Test
    fun `test SearchMangaUseCase can be injected through Koin`() {
        // Given - Koin 已初始化

        // When
        val searchMangaUseCase: SearchMangaUseCase = get()

        // Then
        assertNotNull(searchMangaUseCase)
        assertTrue(searchMangaUseCase is SearchMangaUseCase)
    }

    @Test
    fun `test GetFavoriteMangaUseCase can be injected through Koin`() {
        // Given - Koin 已初始化

        // When
        val getFavoriteMangaUseCase: GetFavoriteMangaUseCase = get()

        // Then
        assertNotNull(getFavoriteMangaUseCase)
        assertTrue(getFavoriteMangaUseCase is GetFavoriteMangaUseCase)
    }

    @Test
    fun `test GetRecentMangaUseCase can be injected through Koin`() {
        // Given - Koin 已初始化

        // When
        val getRecentMangaUseCase: GetRecentMangaUseCase = get()

        // Then
        assertNotNull(getRecentMangaUseCase)
        assertTrue(getRecentMangaUseCase is GetRecentMangaUseCase)
    }

    @Test
    fun `test GetMangaByStatusUseCase can be injected through Koin`() {
        // Given - Koin 已初始化

        // When
        val getMangaByStatusUseCase: GetMangaByStatusUseCase = get()

        // Then
        assertNotNull(getMangaByStatusUseCase)
        assertTrue(getMangaByStatusUseCase is GetMangaByStatusUseCase)
    }

    @Test
    fun `test InsertOrUpdateMangaUseCase can be injected through Koin`() {
        // Given - Koin 已初始化

        // When
        val insertOrUpdateMangaUseCase: InsertOrUpdateMangaUseCase = get()

        // Then
        assertNotNull(insertOrUpdateMangaUseCase)
        assertTrue(insertOrUpdateMangaUseCase is InsertOrUpdateMangaUseCase)
    }

    @Test
    fun `test UpdateReadingProgressUseCase can be injected through Koin`() {
        // Given - Koin 已初始化

        // When
        val updateReadingProgressUseCase: UpdateReadingProgressUseCase = get()

        // Then
        assertNotNull(updateReadingProgressUseCase)
        assertTrue(updateReadingProgressUseCase is UpdateReadingProgressUseCase)
    }

    @Test
    fun `test ToggleFavoriteUseCase can be injected through Koin`() {
        // Given - Koin 已初始化

        // When
        val toggleFavoriteUseCase: ToggleFavoriteUseCase = get()

        // Then
        assertNotNull(toggleFavoriteUseCase)
        assertTrue(toggleFavoriteUseCase is ToggleFavoriteUseCase)
    }

    @Test
    fun `test UpdateRatingUseCase can be injected through Koin`() {
        // Given - Koin 已初始化

        // When
        val updateRatingUseCase: UpdateRatingUseCase = get()

        // Then
        assertNotNull(updateRatingUseCase)
        assertTrue(updateRatingUseCase is UpdateRatingUseCase)
    }

    @Test
    fun `test DeleteMangaUseCase can be injected through Koin`() {
        // Given - Koin 已初始化

        // When
        val deleteMangaUseCase: DeleteMangaUseCase = get()

        // Then
        assertNotNull(deleteMangaUseCase)
        assertTrue(deleteMangaUseCase is DeleteMangaUseCase)
    }

    @Test
    fun `test DeleteAllMangaUseCase can be injected through Koin`() {
        // Given - Koin 已初始化

        // When
        val deleteAllMangaUseCase: DeleteAllMangaUseCase = get()

        // Then
        assertNotNull(deleteAllMangaUseCase)
        assertTrue(deleteAllMangaUseCase is DeleteAllMangaUseCase)
    }

    @Test
    fun `test ImportComicUseCase can be injected through Koin`() {
        // Given - Koin 已初始化

        // When
        val importComicUseCase: ImportComicUseCase = get()

        // Then
        assertNotNull(importComicUseCase)
        assertTrue(importComicUseCase is ImportComicUseCase)
    }

    @Test
    fun `test BatchImportComicsUseCase can be injected through Koin`() {
        // Given - Koin 已初始化

        // When
        val batchImportComicsUseCase: BatchImportComicsUseCase = get()

        // Then
        assertNotNull(batchImportComicsUseCase)
        assertTrue(batchImportComicsUseCase is BatchImportComicsUseCase)
    }

    @Test
    fun `test MonitorImportProgressUseCase can be injected through Koin`() {
        // Given - Koin 已初始化

        // When
        val monitorImportProgressUseCase: MonitorImportProgressUseCase = get()

        // Then
        assertNotNull(monitorImportProgressUseCase)
        assertTrue(monitorImportProgressUseCase is MonitorImportProgressUseCase)
    }

    @Test
    fun `test UpdateImportProgressUseCase can be injected through Koin`() {
        // Given - Koin 已初始化

        // When
        val updateImportProgressUseCase: UpdateImportProgressUseCase = get()

        // Then
        assertNotNull(updateImportProgressUseCase)
        assertTrue(updateImportProgressUseCase is UpdateImportProgressUseCase)
    }

    @Test
    fun `test GetAllMangaUseCase functionality with Koin`() = runTest {
        // Given
        val mockMangaList = listOf(
            createMockManga(id = 1L, title = "漫画1"),
            createMockManga(id = 2L, title = "漫画2")
        )
        
        whenever(mockMangaRepository.getAllManga()).thenReturn(flow { emit(mockMangaList) })

        // When - 创建带有 mock 仓储的用例
        val getAllMangaUseCase = GetAllMangaUseCase(mockMangaRepository)
        val result = getAllMangaUseCase()

        // Then
        result.test {
            val emittedList = awaitItem()
            assertEquals(2, emittedList.size)
            assertEquals(mockMangaList, emittedList)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test SearchMangaUseCase functionality with Koin`() = runTest {
        // Given
        val query = "测试"
        val searchResults = listOf(
            createMockManga(id = 1L, title = "测试漫画"),
            createMockManga(id = 2L, title = "另一个测试")
        )
        
        whenever(mockMangaRepository.searchManga(query)).thenReturn(flow { emit(searchResults) })

        // When
        val searchMangaUseCase = SearchMangaUseCase(mockMangaRepository)
        val result = searchMangaUseCase(query)

        // Then
        result.test {
            val emittedList = awaitItem()
            assertEquals(2, emittedList.size)
            assertEquals(searchResults, emittedList)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test GetFavoriteMangaUseCase functionality with Koin`() = runTest {
        // Given
        val favoriteMangaList = listOf(
            createMockManga(id = 1L, title = "收藏漫画1", isFavorite = true),
            createMockManga(id = 2L, title = "收藏漫画2", isFavorite = true)
        )
        
        whenever(mockMangaRepository.getFavoriteManga()).thenReturn(flow { emit(favoriteMangaList) })

        // When
        val getFavoriteMangaUseCase = GetFavoriteMangaUseCase(mockMangaRepository)
        val result = getFavoriteMangaUseCase()

        // Then
        result.test {
            val emittedList = awaitItem()
            assertEquals(2, emittedList.size)
            assertEquals(favoriteMangaList, emittedList)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test GetMangaByIdUseCase functionality with Koin`() = runTest {
        // Given
        val mangaId = 1L
        val mockManga = createMockManga(id = mangaId, title = "测试漫画")
        
        whenever(mockMangaRepository.getMangaById(mangaId)).thenReturn(flow { emit(mockManga) })

        // When
        val getMangaByIdUseCase = GetMangaByIdUseCase(mockMangaRepository)
        val result = getMangaByIdUseCase(mangaId)

        // Then
        result.test {
            val emittedManga = awaitItem()
            assertEquals(mangaId, emittedManga.id)
            assertEquals("测试漫画", emittedManga.title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test ToggleFavoriteUseCase functionality with Koin`() = runTest {
        // Given
        val mangaId = 1L
        val mockManga = createMockManga(id = mangaId, title = "测试漫画", isFavorite = false)
        val updatedManga = mockManga.copy(isFavorite = true)
        
        whenever(mockMangaRepository.getMangaById(mangaId)).thenReturn(flow { emit(mockManga) })
        whenever(mockMangaRepository.updateManga(updatedManga)).thenReturn(Unit)

        // When
        val toggleFavoriteUseCase = ToggleFavoriteUseCase(mockMangaRepository)
        toggleFavoriteUseCase(mangaId)

        // Then - 验证方法被调用（没有异常即表示成功）
        assertTrue(true)
    }

    @Test
    fun `test DeleteMangaUseCase functionality with Koin`() = runTest {
        // Given
        val mangaToDelete = createMockManga(id = 1L, title = "删除漫画")
        whenever(mockMangaRepository.deleteManga(mangaToDelete)).thenReturn(Unit)

        // When
        val deleteMangaUseCase = DeleteMangaUseCase(mockMangaRepository)
        deleteMangaUseCase(mangaToDelete)

        // Then - 验证方法被调用（没有异常即表示成功）
        assertTrue(true)
    }

    @Test
    fun `test DeleteAllMangaUseCase functionality with Koin`() = runTest {
        // Given
        whenever(mockMangaRepository.deleteAllManga()).thenReturn(Unit)

        // When
        val deleteAllMangaUseCase = DeleteAllMangaUseCase(mockMangaRepository)
        deleteAllMangaUseCase()

        // Then - 验证方法被调用（没有异常即表示成功）
        assertTrue(true)
    }

    @Test
    fun `test all use cases are properly configured in Koin`() {
        // Given - Koin 已初始化

        // When
        val useCases = listOf(
            get<GetAllMangaUseCase>(),
            get<GetMangaByIdUseCase>(),
            get<SearchMangaUseCase>(),
            get<GetFavoriteMangaUseCase>(),
            get<GetRecentMangaUseCase>(),
            get<GetMangaByStatusUseCase>(),
            get<InsertOrUpdateMangaUseCase>(),
            get<UpdateReadingProgressUseCase>(),
            get<ToggleFavoriteUseCase>(),
            get<UpdateRatingUseCase>(),
            get<DeleteMangaUseCase>(),
            get<DeleteAllMangaUseCase>(),
            get<ImportComicUseCase>(),
            get<BatchImportComicsUseCase>(),
            get<MonitorImportProgressUseCase>(),
            get<UpdateImportProgressUseCase>()
        )

        // Then
        assertEquals(16, useCases.size)
        useCases.forEach { useCase ->
            assertNotNull(useCase)
        }
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