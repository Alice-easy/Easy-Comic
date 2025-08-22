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
 * UpdateMangaFavoriteStatusUseCase单元测试
 * 
 * 测试更新漫画收藏状态的各种场景：
 * - 单个收藏状态更新
 * - 批量收藏状态更新
 * - 收藏状态切换
 * - 边界条件处理
 * - 异常情况处理
 */
class UpdateMangaFavoriteStatusUseCaseTest {

    private val mockRepository = mockk<MangaRepository>()
    private lateinit var updateMangaFavoriteStatusUseCase: UpdateMangaFavoriteStatusUseCase

    // 测试数据 - 未收藏的漫画
    private val unfavoriteManga1 = Manga(
        id = 1L,
        title = "Unfavorite Manga 1",
        filePath = "/test/path/manga1.zip",
        pageCount = 20,
        currentPage = 5,
        readingStatus = ReadingStatus.READING,
        isFavorite = false,
        rating = 0f
    )

    private val unfavoriteManga2 = Manga(
        id = 2L,
        title = "Unfavorite Manga 2",
        filePath = "/test/path/manga2.zip",
        pageCount = 30,
        currentPage = 0,
        readingStatus = ReadingStatus.UNREAD,
        isFavorite = false,
        rating = 0f
    )

    // 测试数据 - 已收藏的漫画
    private val favoriteManga1 = Manga(
        id = 3L,
        title = "Favorite Manga 1",
        filePath = "/test/path/manga3.zip",
        pageCount = 25,
        currentPage = 15,
        readingStatus = ReadingStatus.READING,
        isFavorite = true,
        rating = 4.5f
    )

    private val favoriteManga2 = Manga(
        id = 4L,
        title = "Favorite Manga 2",
        filePath = "/test/path/manga4.zip",
        pageCount = 40,
        currentPage = 40,
        readingStatus = ReadingStatus.COMPLETED,
        isFavorite = true,
        rating = 5.0f
    )

    @Before
    fun setup() {
        updateMangaFavoriteStatusUseCase = UpdateMangaFavoriteStatusUseCase(mockRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // ========== 单个收藏状态更新测试 ==========

    @Test
    fun `updateSingle should set manga as favorite when manga exists`() = runTest {
        // Given
        val mangaId = 1L
        val isFavorite = true
        
        coEvery { mockRepository.getMangaById(mangaId) } returns unfavoriteManga1
        coEvery { mockRepository.updateManga(any()) } returns 1L

        // When
        updateMangaFavoriteStatusUseCase.updateSingle(mangaId, isFavorite)

        // Then
        coVerify { mockRepository.getMangaById(mangaId) }
        coVerify { 
            mockRepository.updateManga(match { updatedManga ->
                updatedManga.id == unfavoriteManga1.id &&
                updatedManga.isFavorite == true &&
                updatedManga.title == unfavoriteManga1.title &&
                updatedManga.pageCount == unfavoriteManga1.pageCount &&
                updatedManga.currentPage == unfavoriteManga1.currentPage
            })
        }
    }

    @Test
    fun `updateSingle should remove manga from favorite when manga exists`() = runTest {
        // Given
        val mangaId = 3L
        val isFavorite = false
        
        coEvery { mockRepository.getMangaById(mangaId) } returns favoriteManga1
        coEvery { mockRepository.updateManga(any()) } returns 1L

        // When
        updateMangaFavoriteStatusUseCase.updateSingle(mangaId, isFavorite)

        // Then
        coVerify { mockRepository.getMangaById(mangaId) }
        coVerify { 
            mockRepository.updateManga(match { updatedManga ->
                updatedManga.id == favoriteManga1.id &&
                updatedManga.isFavorite == false &&
                updatedManga.title == favoriteManga1.title &&
                updatedManga.rating == favoriteManga1.rating
            })
        }
    }

    @Test
    fun `updateSingle should not update when manga does not exist`() = runTest {
        // Given
        val nonExistentId = 999L
        val isFavorite = true
        
        coEvery { mockRepository.getMangaById(nonExistentId) } returns null

        // When
        updateMangaFavoriteStatusUseCase.updateSingle(nonExistentId, isFavorite)

        // Then
        coVerify { mockRepository.getMangaById(nonExistentId) }
        coVerify(exactly = 0) { mockRepository.updateManga(any()) }
    }

    @Test
    fun `updateSingle should not change status when setting same favorite status`() = runTest {
        // Given
        val mangaId = 3L
        val isFavorite = true // 已经是收藏状态
        
        coEvery { mockRepository.getMangaById(mangaId) } returns favoriteManga1
        coEvery { mockRepository.updateManga(any()) } returns 1L

        // When
        updateMangaFavoriteStatusUseCase.updateSingle(mangaId, isFavorite)

        // Then
        coVerify { mockRepository.getMangaById(mangaId) }
        coVerify { 
            mockRepository.updateManga(match { updatedManga ->
                updatedManga.isFavorite == true // 状态保持不变
            })
        }
    }

    @Test
    fun `updateSingle should handle zero id gracefully`() = runTest {
        // Given
        val zeroId = 0L
        val isFavorite = true
        
        coEvery { mockRepository.getMangaById(zeroId) } returns null

        // When
        updateMangaFavoriteStatusUseCase.updateSingle(zeroId, isFavorite)

        // Then
        coVerify { mockRepository.getMangaById(zeroId) }
        coVerify(exactly = 0) { mockRepository.updateManga(any()) }
    }

    @Test
    fun `updateSingle should handle negative id gracefully`() = runTest {
        // Given
        val negativeId = -1L
        val isFavorite = true
        
        coEvery { mockRepository.getMangaById(negativeId) } returns null

        // When
        updateMangaFavoriteStatusUseCase.updateSingle(negativeId, isFavorite)

        // Then
        coVerify { mockRepository.getMangaById(negativeId) }
        coVerify(exactly = 0) { mockRepository.updateManga(any()) }
    }

    // ========== 批量收藏状态更新测试 ==========

    @Test
    fun `updateMultiple should update all existing manga to favorite`() = runTest {
        // Given
        val mangaIds = listOf(1L, 2L)
        val isFavorite = true
        
        coEvery { mockRepository.getMangaById(1L) } returns unfavoriteManga1
        coEvery { mockRepository.getMangaById(2L) } returns unfavoriteManga2
        coEvery { mockRepository.updateManga(any()) } returns 1L

        // When
        updateMangaFavoriteStatusUseCase.updateMultiple(mangaIds, isFavorite)

        // Then
        coVerify { mockRepository.getMangaById(1L) }
        coVerify { mockRepository.getMangaById(2L) }
        coVerify(exactly = 2) { mockRepository.updateManga(any()) }
        
        // 验证所有漫画都被设置为收藏
        coVerify(exactly = 2) { 
            mockRepository.updateManga(match { updatedManga ->
                updatedManga.isFavorite == true
            })
        }
    }

    @Test
    fun `updateMultiple should update all existing manga to unfavorite`() = runTest {
        // Given
        val mangaIds = listOf(3L, 4L)
        val isFavorite = false
        
        coEvery { mockRepository.getMangaById(3L) } returns favoriteManga1
        coEvery { mockRepository.getMangaById(4L) } returns favoriteManga2
        coEvery { mockRepository.updateManga(any()) } returns 1L

        // When
        updateMangaFavoriteStatusUseCase.updateMultiple(mangaIds, isFavorite)

        // Then
        coVerify { mockRepository.getMangaById(3L) }
        coVerify { mockRepository.getMangaById(4L) }
        coVerify(exactly = 2) { mockRepository.updateManga(any()) }
        
        // 验证所有漫画都被设置为非收藏
        coVerify(exactly = 2) { 
            mockRepository.updateManga(match { updatedManga ->
                updatedManga.isFavorite == false
            })
        }
    }

    @Test
    fun `updateMultiple should skip non-existent manga`() = runTest {
        // Given
        val mangaIds = listOf(1L, 999L, 2L) // 999L 不存在
        val isFavorite = true
        
        coEvery { mockRepository.getMangaById(1L) } returns unfavoriteManga1
        coEvery { mockRepository.getMangaById(999L) } returns null
        coEvery { mockRepository.getMangaById(2L) } returns unfavoriteManga2
        coEvery { mockRepository.updateManga(any()) } returns 1L

        // When
        updateMangaFavoriteStatusUseCase.updateMultiple(mangaIds, isFavorite)

        // Then
        coVerify { mockRepository.getMangaById(1L) }
        coVerify { mockRepository.getMangaById(999L) }
        coVerify { mockRepository.getMangaById(2L) }
        // 只为存在的漫画调用更新
        coVerify(exactly = 2) { mockRepository.updateManga(any()) }
    }

    @Test
    fun `updateMultiple should handle empty list gracefully`() = runTest {
        // Given
        val emptyIds = emptyList<Long>()
        val isFavorite = true

        // When
        updateMangaFavoriteStatusUseCase.updateMultiple(emptyIds, isFavorite)

        // Then
        // 空列表不应该调用任何repository方法
        coVerify(exactly = 0) { mockRepository.getMangaById(any()) }
        coVerify(exactly = 0) { mockRepository.updateManga(any()) }
    }

    @Test
    fun `updateMultiple should handle single item list`() = runTest {
        // Given
        val singleId = listOf(1L)
        val isFavorite = true
        
        coEvery { mockRepository.getMangaById(1L) } returns unfavoriteManga1
        coEvery { mockRepository.updateManga(any()) } returns 1L

        // When
        updateMangaFavoriteStatusUseCase.updateMultiple(singleId, isFavorite)

        // Then
        coVerify { mockRepository.getMangaById(1L) }
        coVerify(exactly = 1) { mockRepository.updateManga(any()) }
    }

    @Test
    fun `updateMultiple should handle duplicate ids`() = runTest {
        // Given
        val duplicateIds = listOf(1L, 1L, 2L) // 包含重复ID
        val isFavorite = true
        
        coEvery { mockRepository.getMangaById(1L) } returns unfavoriteManga1
        coEvery { mockRepository.getMangaById(2L) } returns unfavoriteManga2
        coEvery { mockRepository.updateManga(any()) } returns 1L

        // When
        updateMangaFavoriteStatusUseCase.updateMultiple(duplicateIds, isFavorite)

        // Then
        // 重复ID会导致多次调用
        coVerify(exactly = 2) { mockRepository.getMangaById(1L) }
        coVerify(exactly = 1) { mockRepository.getMangaById(2L) }
        coVerify(exactly = 3) { mockRepository.updateManga(any()) }
    }

    @Test
    fun `updateMultiple should handle mixed existing and non-existing manga`() = runTest {
        // Given
        val mixedIds = listOf(1L, 999L, 3L, 998L)
        val isFavorite = true
        
        coEvery { mockRepository.getMangaById(1L) } returns unfavoriteManga1
        coEvery { mockRepository.getMangaById(999L) } returns null
        coEvery { mockRepository.getMangaById(3L) } returns favoriteManga1
        coEvery { mockRepository.getMangaById(998L) } returns null
        coEvery { mockRepository.updateManga(any()) } returns 1L

        // When
        updateMangaFavoriteStatusUseCase.updateMultiple(mixedIds, isFavorite)

        // Then
        coVerify { mockRepository.getMangaById(1L) }
        coVerify { mockRepository.getMangaById(999L) }
        coVerify { mockRepository.getMangaById(3L) }
        coVerify { mockRepository.getMangaById(998L) }
        // 只为存在的漫画调用更新
        coVerify(exactly = 2) { mockRepository.updateManga(any()) }
    }

    // ========== 收藏状态切换测试 ==========

    @Test
    fun `toggleFavorite should change unfavorite manga to favorite`() = runTest {
        // Given
        val mangaId = 1L
        
        coEvery { mockRepository.getMangaById(mangaId) } returns unfavoriteManga1
        coEvery { mockRepository.updateManga(any()) } returns 1L

        // When
        updateMangaFavoriteStatusUseCase.toggleFavorite(mangaId)

        // Then
        coVerify { mockRepository.getMangaById(mangaId) }
        coVerify { 
            mockRepository.updateManga(match { updatedManga ->
                updatedManga.id == unfavoriteManga1.id &&
                updatedManga.isFavorite == true && // 从false切换到true
                updatedManga.title == unfavoriteManga1.title
            })
        }
    }

    @Test
    fun `toggleFavorite should change favorite manga to unfavorite`() = runTest {
        // Given
        val mangaId = 3L
        
        coEvery { mockRepository.getMangaById(mangaId) } returns favoriteManga1
        coEvery { mockRepository.updateManga(any()) } returns 1L

        // When
        updateMangaFavoriteStatusUseCase.toggleFavorite(mangaId)

        // Then
        coVerify { mockRepository.getMangaById(mangaId) }
        coVerify { 
            mockRepository.updateManga(match { updatedManga ->
                updatedManga.id == favoriteManga1.id &&
                updatedManga.isFavorite == false && // 从true切换到false
                updatedManga.title == favoriteManga1.title
            })
        }
    }

    @Test
    fun `toggleFavorite should not update when manga does not exist`() = runTest {
        // Given
        val nonExistentId = 999L
        
        coEvery { mockRepository.getMangaById(nonExistentId) } returns null

        // When
        updateMangaFavoriteStatusUseCase.toggleFavorite(nonExistentId)

        // Then
        coVerify { mockRepository.getMangaById(nonExistentId) }
        coVerify(exactly = 0) { mockRepository.updateManga(any()) }
    }

    @Test
    fun `toggleFavorite should preserve other manga properties`() = runTest {
        // Given
        val mangaId = 1L
        
        coEvery { mockRepository.getMangaById(mangaId) } returns unfavoriteManga1
        coEvery { mockRepository.updateManga(any()) } returns 1L

        // When
        updateMangaFavoriteStatusUseCase.toggleFavorite(mangaId)

        // Then
        coVerify { 
            mockRepository.updateManga(match { updatedManga ->
                // 验证其他属性保持不变
                updatedManga.id == unfavoriteManga1.id &&
                updatedManga.title == unfavoriteManga1.title &&
                updatedManga.filePath == unfavoriteManga1.filePath &&
                updatedManga.pageCount == unfavoriteManga1.pageCount &&
                updatedManga.currentPage == unfavoriteManga1.currentPage &&
                updatedManga.readingStatus == unfavoriteManga1.readingStatus &&
                updatedManga.rating == unfavoriteManga1.rating &&
                // 只有isFavorite被改变
                updatedManga.isFavorite != unfavoriteManga1.isFavorite
            })
        }
    }

    // ========== 异常处理测试 ==========

    @Test
    fun `updateSingle should handle repository exceptions in getMangaById`() = runTest {
        // Given
        val mangaId = 1L
        val isFavorite = true
        coEvery { mockRepository.getMangaById(mangaId) } throws Exception("Database error")

        // When & Then
        try {
            updateMangaFavoriteStatusUseCase.updateSingle(mangaId, isFavorite)
            assertThat(false).isTrue() // 不应该到达这里
        } catch (e: Exception) {
            assertThat(e.message).isEqualTo("Database error")
            coVerify { mockRepository.getMangaById(mangaId) }
            coVerify(exactly = 0) { mockRepository.updateManga(any()) }
        }
    }

    @Test
    fun `updateSingle should handle repository exceptions in updateManga`() = runTest {
        // Given
        val mangaId = 1L
        val isFavorite = true
        coEvery { mockRepository.getMangaById(mangaId) } returns unfavoriteManga1
        coEvery { mockRepository.updateManga(any()) } throws Exception("Update failed")

        // When & Then
        try {
            updateMangaFavoriteStatusUseCase.updateSingle(mangaId, isFavorite)
            assertThat(false).isTrue() // 不应该到达这里
        } catch (e: Exception) {
            assertThat(e.message).isEqualTo("Update failed")
            coVerify { mockRepository.getMangaById(mangaId) }
            coVerify { mockRepository.updateManga(any()) }
        }
    }

    @Test
    fun `toggleFavorite should handle repository exceptions in getMangaById`() = runTest {
        // Given
        val mangaId = 1L
        coEvery { mockRepository.getMangaById(mangaId) } throws Exception("Database error")

        // When & Then
        try {
            updateMangaFavoriteStatusUseCase.toggleFavorite(mangaId)
            assertThat(false).isTrue() // 不应该到达这里
        } catch (e: Exception) {
            assertThat(e.message).isEqualTo("Database error")
            coVerify { mockRepository.getMangaById(mangaId) }
            coVerify(exactly = 0) { mockRepository.updateManga(any()) }
        }
    }

    @Test
    fun `toggleFavorite should handle repository exceptions in updateManga`() = runTest {
        // Given
        val mangaId = 1L
        coEvery { mockRepository.getMangaById(mangaId) } returns unfavoriteManga1
        coEvery { mockRepository.updateManga(any()) } throws Exception("Update failed")

        // When & Then
        try {
            updateMangaFavoriteStatusUseCase.toggleFavorite(mangaId)
            assertThat(false).isTrue() // 不应该到达这里
        } catch (e: Exception) {
            assertThat(e.message).isEqualTo("Update failed")
            coVerify { mockRepository.getMangaById(mangaId) }
            coVerify { mockRepository.updateManga(any()) }
        }
    }

    // ========== 集成测试 ==========

    @Test
    fun `complete workflow - updateSingle then toggleFavorite`() = runTest {
        // Given
        val mangaId = 1L
        val favoriteUpdatedManga = unfavoriteManga1.copy(isFavorite = true)
        
        // 设置mock：第一次返回原始漫画，第二次返回已收藏的漫画
        coEvery { mockRepository.getMangaById(mangaId) } returns unfavoriteManga1 andThen favoriteUpdatedManga
        coEvery { mockRepository.updateManga(any()) } returns 1L

        // When - 先设置为收藏
        updateMangaFavoriteStatusUseCase.updateSingle(mangaId, true)
        
        // Then - 验证设置为收藏
        coVerify { 
            mockRepository.updateManga(match { updatedManga ->
                updatedManga.id == unfavoriteManga1.id &&
                updatedManga.isFavorite == true
            })
        }

        // When - 再切换收藏状态
        updateMangaFavoriteStatusUseCase.toggleFavorite(mangaId)
        
        // Then - 验证切换为非收藏
        coVerify { 
            mockRepository.updateManga(match { updatedManga ->
                updatedManga.id == unfavoriteManga1.id &&
                updatedManga.isFavorite == false
            })
        }
        
        // 总共调用了2次getMangaById和2次updateManga
        coVerify(exactly = 2) { mockRepository.getMangaById(mangaId) }
        coVerify(exactly = 2) { mockRepository.updateManga(any()) }
    }

    @Test
    fun `updateMultiple with different states should work correctly`() = runTest {
        // Given
        val mixedIds = listOf(1L, 3L) // 一个未收藏，一个已收藏
        val isFavorite = true
        
        coEvery { mockRepository.getMangaById(1L) } returns unfavoriteManga1 // 未收藏→收藏
        coEvery { mockRepository.getMangaById(3L) } returns favoriteManga1   // 已收藏→收藏（保持）
        coEvery { mockRepository.updateManga(any()) } returns 1L

        // When
        updateMangaFavoriteStatusUseCase.updateMultiple(mixedIds, isFavorite)

        // Then
        coVerify { mockRepository.getMangaById(1L) }
        coVerify { mockRepository.getMangaById(3L) }
        
        // 两个漫画都应该被更新为收藏状态
        coVerify(exactly = 2) { 
            mockRepository.updateManga(match { updatedManga ->
                updatedManga.isFavorite == true
            })
        }
        
        // 确认没有其他不期望的调用
        confirmVerified(mockRepository)
    }
}
