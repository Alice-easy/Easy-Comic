package com.easycomic.domain.usecase.manga

import com.easycomic.domain.model.Manga
import com.easycomic.domain.model.ReadingStatus
import com.easycomic.domain.repository.MangaRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * DeleteComicsUseCase单元测试
 * 
 * 测试漫画删除功能的各种场景：
 * - 单个删除
 * - 批量删除
 * - 全部删除
 * - 边界条件处理
 * - 异常情况处理
 */
class DeleteComicsUseCaseTest {

    private val mockRepository = mockk<MangaRepository>()
    private lateinit var deleteComicsUseCase: DeleteComicsUseCase

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
        currentPage = 15,
        readingStatus = ReadingStatus.READING,
        isFavorite = true,
        rating = 4.0f
    )

    private val testManga3 = Manga(
        id = 3L,
        title = "Test Manga 3",
        filePath = "/test/path/manga3.zip",
        pageCount = 25,
        currentPage = 25,
        readingStatus = ReadingStatus.COMPLETED,
        isFavorite = false,
        rating = 3.5f
    )

    private val testMangaList = listOf(testManga1, testManga2, testManga3)

    @Before
    fun setup() {
        deleteComicsUseCase = DeleteComicsUseCase(mockRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // ========== 单个删除测试 ==========

    @Test
    fun `deleteSingle should delete manga when manga exists`() = runTest {
        // Given
        val mangaId = 1L
        coEvery { mockRepository.getMangaById(mangaId) } returns testManga1
        coEvery { mockRepository.deleteManga(testManga1) } just Runs

        // When
        deleteComicsUseCase.deleteSingle(mangaId)

        // Then
        coVerify { mockRepository.getMangaById(mangaId) }
        coVerify { mockRepository.deleteManga(testManga1) }
    }

    @Test
    fun `deleteSingle should not call delete when manga does not exist`() = runTest {
        // Given
        val nonExistentId = 999L
        coEvery { mockRepository.getMangaById(nonExistentId) } returns null

        // When
        deleteComicsUseCase.deleteSingle(nonExistentId)

        // Then
        coVerify { mockRepository.getMangaById(nonExistentId) }
        coVerify(exactly = 0) { mockRepository.deleteManga(any()) }
    }

    @Test
    fun `deleteSingle should handle zero id gracefully`() = runTest {
        // Given
        val zeroId = 0L
        coEvery { mockRepository.getMangaById(zeroId) } returns null

        // When
        deleteComicsUseCase.deleteSingle(zeroId)

        // Then
        coVerify { mockRepository.getMangaById(zeroId) }
        coVerify(exactly = 0) { mockRepository.deleteManga(any()) }
    }

    @Test
    fun `deleteSingle should handle negative id gracefully`() = runTest {
        // Given
        val negativeId = -1L
        coEvery { mockRepository.getMangaById(negativeId) } returns null

        // When
        deleteComicsUseCase.deleteSingle(negativeId)

        // Then
        coVerify { mockRepository.getMangaById(negativeId) }
        coVerify(exactly = 0) { mockRepository.deleteManga(any()) }
    }

    // ========== 批量删除测试 ==========

    @Test
    fun `deleteMultiple should delete all existing manga`() = runTest {
        // Given
        val mangaIds = listOf(1L, 2L, 3L)
        val expectedMangaList = listOf(testManga1, testManga2, testManga3)
        
        coEvery { mockRepository.getMangaById(1L) } returns testManga1
        coEvery { mockRepository.getMangaById(2L) } returns testManga2
        coEvery { mockRepository.getMangaById(3L) } returns testManga3
        coEvery { mockRepository.deleteAllManga(expectedMangaList) } just Runs

        // When
        deleteComicsUseCase.deleteMultiple(mangaIds)

        // Then
        coVerify { mockRepository.getMangaById(1L) }
        coVerify { mockRepository.getMangaById(2L) }
        coVerify { mockRepository.getMangaById(3L) }
        coVerify { mockRepository.deleteAllManga(expectedMangaList) }
    }

    @Test
    fun `deleteMultiple should only delete existing manga and skip non-existent ones`() = runTest {
        // Given
        val mangaIds = listOf(1L, 999L, 2L) // 999L 不存在
        val expectedMangaList = listOf(testManga1, testManga2) // 只包含存在的漫画
        
        coEvery { mockRepository.getMangaById(1L) } returns testManga1
        coEvery { mockRepository.getMangaById(999L) } returns null
        coEvery { mockRepository.getMangaById(2L) } returns testManga2
        coEvery { mockRepository.deleteAllManga(expectedMangaList) } just Runs

        // When
        deleteComicsUseCase.deleteMultiple(mangaIds)

        // Then
        coVerify { mockRepository.getMangaById(1L) }
        coVerify { mockRepository.getMangaById(999L) }
        coVerify { mockRepository.getMangaById(2L) }
        coVerify { mockRepository.deleteAllManga(expectedMangaList) }
    }

    @Test
    fun `deleteMultiple should not call deleteAllManga when no manga exist`() = runTest {
        // Given
        val nonExistentIds = listOf(997L, 998L, 999L)
        
        coEvery { mockRepository.getMangaById(997L) } returns null
        coEvery { mockRepository.getMangaById(998L) } returns null
        coEvery { mockRepository.getMangaById(999L) } returns null

        // When
        deleteComicsUseCase.deleteMultiple(nonExistentIds)

        // Then
        coVerify { mockRepository.getMangaById(997L) }
        coVerify { mockRepository.getMangaById(998L) }
        coVerify { mockRepository.getMangaById(999L) }
        coVerify(exactly = 0) { mockRepository.deleteAllManga(any()) }
    }

    @Test
    fun `deleteMultiple should handle empty list gracefully`() = runTest {
        // Given
        val emptyIds = emptyList<Long>()

        // When
        deleteComicsUseCase.deleteMultiple(emptyIds)

        // Then
        // 空列表不应该调用任何repository方法
        coVerify(exactly = 0) { mockRepository.getMangaById(any()) }
        coVerify(exactly = 0) { mockRepository.deleteAllManga(any()) }
    }

    @Test
    fun `deleteMultiple should handle single id list`() = runTest {
        // Given
        val singleId = listOf(1L)
        val expectedMangaList = listOf(testManga1)
        
        coEvery { mockRepository.getMangaById(1L) } returns testManga1
        coEvery { mockRepository.deleteAllManga(expectedMangaList) } just Runs

        // When
        deleteComicsUseCase.deleteMultiple(singleId)

        // Then
        coVerify { mockRepository.getMangaById(1L) }
        coVerify { mockRepository.deleteAllManga(expectedMangaList) }
    }

    @Test
    fun `deleteMultiple should handle duplicate ids`() = runTest {
        // Given
        val duplicateIds = listOf(1L, 1L, 2L, 1L) // 包含重复ID
        val expectedMangaList = listOf(testManga1, testManga1, testManga2, testManga1) // mapNotNull会保留所有结果
        
        coEvery { mockRepository.getMangaById(1L) } returns testManga1
        coEvery { mockRepository.getMangaById(2L) } returns testManga2
        coEvery { mockRepository.deleteAllManga(expectedMangaList) } just Runs

        // When
        deleteComicsUseCase.deleteMultiple(duplicateIds)

        // Then
        // 重复ID会导致多次调用getMangaById
        coVerify(exactly = 3) { mockRepository.getMangaById(1L) }
        coVerify(exactly = 1) { mockRepository.getMangaById(2L) }
        coVerify { mockRepository.deleteAllManga(expectedMangaList) }
    }

    // ========== 全部删除测试 ==========

    @Test
    fun `deleteAll should delete all manga when manga list is not empty`() = runTest {
        // Given
        every { mockRepository.getAllManga() } returns flowOf(testMangaList)
        coEvery { mockRepository.deleteAllManga(testMangaList) } just Runs

        // When
        deleteComicsUseCase.deleteAll()

        // Then
        verify { mockRepository.getAllManga() }
        coVerify { mockRepository.deleteAllManga(testMangaList) }
    }

    @Test
    fun `deleteAll should not call deleteAllManga when manga list is empty`() = runTest {
        // Given
        val emptyMangaList = emptyList<Manga>()
        every { mockRepository.getAllManga() } returns flowOf(emptyMangaList)

        // When
        deleteComicsUseCase.deleteAll()

        // Then
        verify { mockRepository.getAllManga() }
        coVerify(exactly = 0) { mockRepository.deleteAllManga(any()) }
    }

    @Test
    fun `deleteAll should handle single manga in list`() = runTest {
        // Given
        val singleMangaList = listOf(testManga1)
        every { mockRepository.getAllManga() } returns flowOf(singleMangaList)
        coEvery { mockRepository.deleteAllManga(singleMangaList) } just Runs

        // When
        deleteComicsUseCase.deleteAll()

        // Then
        verify { mockRepository.getAllManga() }
        coVerify { mockRepository.deleteAllManga(singleMangaList) }
    }

    // ========== 异常处理测试 ==========

    @Test
    fun `deleteSingle should handle repository exceptions gracefully`() = runTest {
        // Given
        val mangaId = 1L
        coEvery { mockRepository.getMangaById(mangaId) } throws Exception("Database error")

        // When & Then
        try {
            deleteComicsUseCase.deleteSingle(mangaId)
            assertThat(false).isTrue() // 不应该到达这里
        } catch (e: Exception) {
            assertThat(e.message).isEqualTo("Database error")
            coVerify { mockRepository.getMangaById(mangaId) }
            coVerify(exactly = 0) { mockRepository.deleteManga(any()) }
        }
    }

    @Test
    fun `deleteMultiple should handle repository exceptions in getMangaById`() = runTest {
        // Given
        val mangaIds = listOf(1L, 2L)
        coEvery { mockRepository.getMangaById(1L) } returns testManga1
        coEvery { mockRepository.getMangaById(2L) } throws Exception("Database error")

        // When & Then
        try {
            deleteComicsUseCase.deleteMultiple(mangaIds)
            assertThat(false).isTrue() // 不应该到达这里
        } catch (e: Exception) {
            assertThat(e.message).isEqualTo("Database error")
            coVerify { mockRepository.getMangaById(1L) }
            coVerify { mockRepository.getMangaById(2L) }
            coVerify(exactly = 0) { mockRepository.deleteAllManga(any()) }
        }
    }

    @Test
    fun `deleteAll should handle repository exceptions in getAllManga`() = runTest {
        // Given
        every { mockRepository.getAllManga() } throws Exception("Database connection error")

        // When & Then
        try {
            deleteComicsUseCase.deleteAll()
            assertThat(false).isTrue() // 不应该到达这里
        } catch (e: Exception) {
            assertThat(e.message).isEqualTo("Database connection error")
            verify { mockRepository.getAllManga() }
            coVerify(exactly = 0) { mockRepository.deleteAllManga(any()) }
        }
    }

    // ========== 集成测试 ==========

    @Test
    fun `complete workflow - deleteMultiple with mixed existing and non-existing manga`() = runTest {
        // Given
        val mixedIds = listOf(1L, 999L, 2L, 998L, 3L)
        val existingManga = listOf(testManga1, testManga2, testManga3)
        
        // 设置mock: 1L, 2L, 3L存在，999L, 998L不存在
        coEvery { mockRepository.getMangaById(1L) } returns testManga1
        coEvery { mockRepository.getMangaById(999L) } returns null
        coEvery { mockRepository.getMangaById(2L) } returns testManga2
        coEvery { mockRepository.getMangaById(998L) } returns null
        coEvery { mockRepository.getMangaById(3L) } returns testManga3
        coEvery { mockRepository.deleteAllManga(existingManga) } just Runs

        // When
        deleteComicsUseCase.deleteMultiple(mixedIds)

        // Then
        // 验证所有ID都被查询了
        coVerify { mockRepository.getMangaById(1L) }
        coVerify { mockRepository.getMangaById(999L) }
        coVerify { mockRepository.getMangaById(2L) }
        coVerify { mockRepository.getMangaById(998L) }
        coVerify { mockRepository.getMangaById(3L) }
        
        // 验证只删除了存在的漫画
        coVerify { mockRepository.deleteAllManga(existingManga) }
        
        // 确认没有其他不期望的调用
        confirmVerified(mockRepository)
    }
}
