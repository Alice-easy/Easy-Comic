package com.easycomic.domain.usecase

import com.easycomic.domain.model.ThemeMode
import com.easycomic.domain.repository.ThemeRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * UpdateThemePreferenceUseCase单元测试
 * 
 * 测试更新主题偏好设置的各种场景：
 * - 更新主题模式
 * - 更新动态色彩设置
 * - 更新自定义种子颜色
 * - 重置到默认设置
 * - 边界条件处理
 * - 异常处理
 */
class UpdateThemePreferenceUseCaseTest {

    private val mockRepository = mockk<ThemeRepository>()
    private lateinit var updateThemePreferenceUseCase: UpdateThemePreferenceUseCase

    @Before
    fun setup() {
        updateThemePreferenceUseCase = UpdateThemePreferenceUseCase(mockRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // ========== 更新主题模式测试 ==========

    @Test
    fun `updateThemeMode should call repository with SYSTEM mode`() = runTest {
        // Given
        val themeMode = ThemeMode.SYSTEM
        coEvery { mockRepository.updateThemeMode(themeMode) } just Runs

        // When
        updateThemePreferenceUseCase.updateThemeMode(themeMode)

        // Then
        coVerify { mockRepository.updateThemeMode(themeMode) }
        confirmVerified(mockRepository)
    }

    @Test
    fun `updateThemeMode should call repository with LIGHT mode`() = runTest {
        // Given
        val themeMode = ThemeMode.LIGHT
        coEvery { mockRepository.updateThemeMode(themeMode) } just Runs

        // When
        updateThemePreferenceUseCase.updateThemeMode(themeMode)

        // Then
        coVerify { mockRepository.updateThemeMode(themeMode) }
        confirmVerified(mockRepository)
    }

    @Test
    fun `updateThemeMode should call repository with DARK mode`() = runTest {
        // Given
        val themeMode = ThemeMode.DARK
        coEvery { mockRepository.updateThemeMode(themeMode) } just Runs

        // When
        updateThemePreferenceUseCase.updateThemeMode(themeMode)

        // Then
        coVerify { mockRepository.updateThemeMode(themeMode) }
        confirmVerified(mockRepository)
    }

    @Test
    fun `updateThemeMode should handle all enum values correctly`() = runTest {
        // Given
        coEvery { mockRepository.updateThemeMode(any()) } just Runs

        // When & Then - 测试所有ThemeMode枚举值
        ThemeMode.values().forEach { mode ->
            updateThemePreferenceUseCase.updateThemeMode(mode)
            coVerify { mockRepository.updateThemeMode(mode) }
        }

        // 验证调用次数等于枚举值数量
        coVerify(exactly = ThemeMode.values().size) { mockRepository.updateThemeMode(any()) }
    }

    // ========== 更新动态色彩设置测试 ==========

    @Test
    fun `updateDynamicColors should call repository with true`() = runTest {
        // Given
        val useDynamicColors = true
        coEvery { mockRepository.updateDynamicColors(useDynamicColors) } just Runs

        // When
        updateThemePreferenceUseCase.updateDynamicColors(useDynamicColors)

        // Then
        coVerify { mockRepository.updateDynamicColors(useDynamicColors) }
        confirmVerified(mockRepository)
    }

    @Test
    fun `updateDynamicColors should call repository with false`() = runTest {
        // Given
        val useDynamicColors = false
        coEvery { mockRepository.updateDynamicColors(useDynamicColors) } just Runs

        // When
        updateThemePreferenceUseCase.updateDynamicColors(useDynamicColors)

        // Then
        coVerify { mockRepository.updateDynamicColors(useDynamicColors) }
        confirmVerified(mockRepository)
    }

    @Test
    fun `updateDynamicColors should handle both boolean values correctly`() = runTest {
        // Given
        coEvery { mockRepository.updateDynamicColors(any()) } just Runs

        // When
        updateThemePreferenceUseCase.updateDynamicColors(true)
        updateThemePreferenceUseCase.updateDynamicColors(false)

        // Then
        coVerify { mockRepository.updateDynamicColors(true) }
        coVerify { mockRepository.updateDynamicColors(false) }
        coVerify(exactly = 2) { mockRepository.updateDynamicColors(any()) }
    }

    // ========== 更新自定义种子颜色测试 ==========

    @Test
    fun `updateCustomSeedColor should call repository with valid color`() = runTest {
        // Given
        val color = 0xFF6200EEL
        coEvery { mockRepository.updateCustomSeedColor(color) } just Runs

        // When
        updateThemePreferenceUseCase.updateCustomSeedColor(color)

        // Then
        coVerify { mockRepository.updateCustomSeedColor(color) }
        confirmVerified(mockRepository)
    }

    @Test
    fun `updateCustomSeedColor should call repository with null color`() = runTest {
        // Given
        val color = null
        coEvery { mockRepository.updateCustomSeedColor(color) } just Runs

        // When
        updateThemePreferenceUseCase.updateCustomSeedColor(color)

        // Then
        coVerify { mockRepository.updateCustomSeedColor(color) }
        confirmVerified(mockRepository)
    }

    @Test
    fun `updateCustomSeedColor should handle zero color`() = runTest {
        // Given
        val color = 0L
        coEvery { mockRepository.updateCustomSeedColor(color) } just Runs

        // When
        updateThemePreferenceUseCase.updateCustomSeedColor(color)

        // Then
        coVerify { mockRepository.updateCustomSeedColor(color) }
        confirmVerified(mockRepository)
    }

    @Test
    fun `updateCustomSeedColor should handle maximum Long value`() = runTest {
        // Given
        val color = Long.MAX_VALUE
        coEvery { mockRepository.updateCustomSeedColor(color) } just Runs

        // When
        updateThemePreferenceUseCase.updateCustomSeedColor(color)

        // Then
        coVerify { mockRepository.updateCustomSeedColor(color) }
        confirmVerified(mockRepository)
    }

    @Test
    fun `updateCustomSeedColor should handle minimum Long value`() = runTest {
        // Given
        val color = Long.MIN_VALUE
        coEvery { mockRepository.updateCustomSeedColor(color) } just Runs

        // When
        updateThemePreferenceUseCase.updateCustomSeedColor(color)

        // Then
        coVerify { mockRepository.updateCustomSeedColor(color) }
        confirmVerified(mockRepository)
    }

    @Test
    fun `updateCustomSeedColor should handle typical ARGB color values`() = runTest {
        // Given
        val typicalColors = listOf(
            0xFF000000L, // Black
            0xFFFFFFFF.toULong().toLong(), // White
            0xFF6200EEL, // Purple
            0xFF03DAC6L, // Teal
            0xFFFF0266L, // Pink
            0xFF018786L, // Dark teal
            0xFFB00020L  // Dark red
        )
        coEvery { mockRepository.updateCustomSeedColor(any()) } just Runs

        // When & Then
        typicalColors.forEach { color ->
            updateThemePreferenceUseCase.updateCustomSeedColor(color)
            coVerify { mockRepository.updateCustomSeedColor(color) }
        }

        coVerify(exactly = typicalColors.size) { mockRepository.updateCustomSeedColor(any()) }
    }

    // ========== 重置到默认设置测试 ==========

    @Test
    fun `resetToDefault should call repository resetToDefault`() = runTest {
        // Given
        coEvery { mockRepository.resetToDefault() } just Runs

        // When
        updateThemePreferenceUseCase.resetToDefault()

        // Then
        coVerify { mockRepository.resetToDefault() }
        confirmVerified(mockRepository)
    }

    @Test
    fun `resetToDefault should not take any parameters`() = runTest {
        // Given
        coEvery { mockRepository.resetToDefault() } just Runs

        // When
        updateThemePreferenceUseCase.resetToDefault()

        // Then
        // 验证方法调用不需要任何参数
        coVerify { mockRepository.resetToDefault() }
        coVerify(exactly = 0) { mockRepository.updateThemeMode(any()) }
        coVerify(exactly = 0) { mockRepository.updateDynamicColors(any()) }
        coVerify(exactly = 0) { mockRepository.updateCustomSeedColor(any()) }
    }

    // ========== 异常处理测试 ==========

    @Test
    fun `updateThemeMode should propagate repository exceptions`() = runTest {
        // Given
        val themeMode = ThemeMode.LIGHT
        val exception = Exception("Repository error")
        coEvery { mockRepository.updateThemeMode(themeMode) } throws exception

        // When & Then
        try {
            updateThemePreferenceUseCase.updateThemeMode(themeMode)
            assertThat(false).isTrue() // 不应该到达这里
        } catch (e: Exception) {
            assertThat(e).isEqualTo(exception)
            assertThat(e.message).isEqualTo("Repository error")
            coVerify { mockRepository.updateThemeMode(themeMode) }
        }
    }

    @Test
    fun `updateDynamicColors should propagate repository exceptions`() = runTest {
        // Given
        val useDynamicColors = true
        val exception = RuntimeException("Database error")
        coEvery { mockRepository.updateDynamicColors(useDynamicColors) } throws exception

        // When & Then
        try {
            updateThemePreferenceUseCase.updateDynamicColors(useDynamicColors)
            assertThat(false).isTrue() // 不应该到达这里
        } catch (e: Exception) {
            assertThat(e).isEqualTo(exception)
            assertThat(e.message).isEqualTo("Database error")
            coVerify { mockRepository.updateDynamicColors(useDynamicColors) }
        }
    }

    @Test
    fun `updateCustomSeedColor should propagate repository exceptions`() = runTest {
        // Given
        val color = 0xFF6200EEL
        val exception = IllegalStateException("Invalid state")
        coEvery { mockRepository.updateCustomSeedColor(color) } throws exception

        // When & Then
        try {
            updateThemePreferenceUseCase.updateCustomSeedColor(color)
            assertThat(false).isTrue() // 不应该到达这里
        } catch (e: Exception) {
            assertThat(e).isEqualTo(exception)
            assertThat(e.message).isEqualTo("Invalid state")
            coVerify { mockRepository.updateCustomSeedColor(color) }
        }
    }

    @Test
    fun `resetToDefault should propagate repository exceptions`() = runTest {
        // Given
        val exception = UnsupportedOperationException("Operation not supported")
        coEvery { mockRepository.resetToDefault() } throws exception

        // When & Then
        try {
            updateThemePreferenceUseCase.resetToDefault()
            assertThat(false).isTrue() // 不应该到达这里
        } catch (e: Exception) {
            assertThat(e).isEqualTo(exception)
            assertThat(e.message).isEqualTo("Operation not supported")
            coVerify { mockRepository.resetToDefault() }
        }
    }

    // ========== 并发测试 ==========

    @Test
    fun `multiple concurrent updates should all call repository`() = runTest {
        // Given
        coEvery { mockRepository.updateThemeMode(any()) } just Runs
        coEvery { mockRepository.updateDynamicColors(any()) } just Runs
        coEvery { mockRepository.updateCustomSeedColor(any()) } just Runs

        // When - 模拟并发调用
        updateThemePreferenceUseCase.updateThemeMode(ThemeMode.LIGHT)
        updateThemePreferenceUseCase.updateDynamicColors(true)
        updateThemePreferenceUseCase.updateCustomSeedColor(0xFF6200EEL)

        // Then
        coVerify { mockRepository.updateThemeMode(ThemeMode.LIGHT) }
        coVerify { mockRepository.updateDynamicColors(true) }
        coVerify { mockRepository.updateCustomSeedColor(0xFF6200EEL) }
        coVerify(exactly = 1) { mockRepository.updateThemeMode(any()) }
        coVerify(exactly = 1) { mockRepository.updateDynamicColors(any()) }
        coVerify(exactly = 1) { mockRepository.updateCustomSeedColor(any()) }
    }

    // ========== 集成测试 ==========

    @Test
    fun `complete workflow - update all settings then reset`() = runTest {
        // Given
        coEvery { mockRepository.updateThemeMode(any()) } just Runs
        coEvery { mockRepository.updateDynamicColors(any()) } just Runs
        coEvery { mockRepository.updateCustomSeedColor(any()) } just Runs
        coEvery { mockRepository.resetToDefault() } just Runs

        // When - 完整的设置更新流程
        updateThemePreferenceUseCase.updateThemeMode(ThemeMode.DARK)
        updateThemePreferenceUseCase.updateDynamicColors(false)
        updateThemePreferenceUseCase.updateCustomSeedColor(0xFF03DAC6L)
        updateThemePreferenceUseCase.resetToDefault()

        // Then - 验证所有调用都正确执行
        coVerifyOrder {
            mockRepository.updateThemeMode(ThemeMode.DARK)
            mockRepository.updateDynamicColors(false)
            mockRepository.updateCustomSeedColor(0xFF03DAC6L)
            mockRepository.resetToDefault()
        }
    }

    @Test
    fun `sequential theme mode updates should all be applied`() = runTest {
        // Given
        coEvery { mockRepository.updateThemeMode(any()) } just Runs

        // When - 连续更新主题模式
        updateThemePreferenceUseCase.updateThemeMode(ThemeMode.SYSTEM)
        updateThemePreferenceUseCase.updateThemeMode(ThemeMode.LIGHT)
        updateThemePreferenceUseCase.updateThemeMode(ThemeMode.DARK)
        updateThemePreferenceUseCase.updateThemeMode(ThemeMode.SYSTEM)

        // Then - 验证所有更新都被调用
        coVerifyOrder {
            mockRepository.updateThemeMode(ThemeMode.SYSTEM)
            mockRepository.updateThemeMode(ThemeMode.LIGHT)
            mockRepository.updateThemeMode(ThemeMode.DARK)
            mockRepository.updateThemeMode(ThemeMode.SYSTEM)
        }
        coVerify(exactly = 4) { mockRepository.updateThemeMode(any()) }
    }

    @Test
    fun `sequential dynamic colors updates should all be applied`() = runTest {
        // Given
        coEvery { mockRepository.updateDynamicColors(any()) } just Runs

        // When - 连续更新动态色彩设置
        updateThemePreferenceUseCase.updateDynamicColors(true)
        updateThemePreferenceUseCase.updateDynamicColors(false)
        updateThemePreferenceUseCase.updateDynamicColors(true)

        // Then - 验证所有更新都被调用
        coVerifyOrder {
            mockRepository.updateDynamicColors(true)
            mockRepository.updateDynamicColors(false)
            mockRepository.updateDynamicColors(true)
        }
        coVerify(exactly = 3) { mockRepository.updateDynamicColors(any()) }
    }

    @Test
    fun `sequential custom color updates should all be applied`() = runTest {
        // Given
        coEvery { mockRepository.updateCustomSeedColor(any()) } just Runs

        // When - 连续更新自定义颜色
        updateThemePreferenceUseCase.updateCustomSeedColor(0xFF6200EEL)
        updateThemePreferenceUseCase.updateCustomSeedColor(null)
        updateThemePreferenceUseCase.updateCustomSeedColor(0xFF03DAC6L)
        updateThemePreferenceUseCase.updateCustomSeedColor(0L)

        // Then - 验证所有更新都被调用
        coVerifyOrder {
            mockRepository.updateCustomSeedColor(0xFF6200EEL)
            mockRepository.updateCustomSeedColor(null)
            mockRepository.updateCustomSeedColor(0xFF03DAC6L)
            mockRepository.updateCustomSeedColor(0L)
        }
        coVerify(exactly = 4) { mockRepository.updateCustomSeedColor(any()) }
    }

    // ========== 边界条件测试 ==========

    @Test
    fun `updateCustomSeedColor with null should work correctly`() = runTest {
        // Given
        coEvery { mockRepository.updateCustomSeedColor(null) } just Runs

        // When
        updateThemePreferenceUseCase.updateCustomSeedColor(null)

        // Then
        coVerify { mockRepository.updateCustomSeedColor(null) }
        coVerify(exactly = 1) { mockRepository.updateCustomSeedColor(any()) }
    }

    @Test
    fun `multiple resetToDefault calls should work correctly`() = runTest {
        // Given
        coEvery { mockRepository.resetToDefault() } just Runs

        // When
        updateThemePreferenceUseCase.resetToDefault()
        updateThemePreferenceUseCase.resetToDefault()
        updateThemePreferenceUseCase.resetToDefault()

        // Then
        coVerify(exactly = 3) { mockRepository.resetToDefault() }
    }

    // ========== 参数验证测试 ==========

    @Test
    fun `all update methods should accept their respective parameter types correctly`() = runTest {
        // Given
        coEvery { mockRepository.updateThemeMode(any()) } just Runs
        coEvery { mockRepository.updateDynamicColors(any()) } just Runs
        coEvery { mockRepository.updateCustomSeedColor(any()) } just Runs

        // When - 测试类型安全性
        val themeMode: ThemeMode = ThemeMode.LIGHT
        val useDynamicColors: Boolean = true
        val customColor: Long? = 0xFF6200EEL

        updateThemePreferenceUseCase.updateThemeMode(themeMode)
        updateThemePreferenceUseCase.updateDynamicColors(useDynamicColors)
        updateThemePreferenceUseCase.updateCustomSeedColor(customColor)

        // Then
        coVerify { mockRepository.updateThemeMode(themeMode) }
        coVerify { mockRepository.updateDynamicColors(useDynamicColors) }
        coVerify { mockRepository.updateCustomSeedColor(customColor) }
    }
}
