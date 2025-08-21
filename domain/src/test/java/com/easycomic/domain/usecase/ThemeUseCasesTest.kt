package com.easycomic.domain.usecase

import com.easycomic.domain.model.ThemeMode
import com.easycomic.domain.model.ThemePreference
import com.easycomic.domain.repository.ThemeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

/**
 * ThemeUseCases单元测试
 */
class ThemeUseCasesTest {

    private lateinit var themeRepository: ThemeRepository
    private lateinit var themeUseCases: ThemeUseCases

    private val testThemePreference = ThemePreference(
        themeMode = ThemeMode.SYSTEM,
        useDynamicColors = true,
        customSeedColor = null
    )

    @Before
    fun setup() {
        themeRepository = mockk()
        themeUseCases = ThemeUseCases(themeRepository)
    }

    @Test
    fun `getThemePreference should return theme preference flow`() = runTest {
        // Given
        every { themeRepository.getThemePreference() } returns flowOf(testThemePreference)

        // When
        val result = themeUseCases.getThemePreference()

        // Then
        result.collect { preference ->
            assertEquals(testThemePreference, preference)
        }
    }

    @Test
    fun `updateThemeMode should call repository`() = runTest {
        // Given
        val themeMode = ThemeMode.DARK
        coEvery { themeRepository.updateThemeMode(themeMode) } returns Unit

        // When
        themeUseCases.updateThemeMode(themeMode)

        // Then
        coVerify { themeRepository.updateThemeMode(themeMode) }
    }

    @Test
    fun `updateDynamicColors should call repository`() = runTest {
        // Given
        val useDynamicColors = false
        coEvery { themeRepository.updateDynamicColors(useDynamicColors) } returns Unit

        // When
        themeUseCases.updateDynamicColors(useDynamicColors)

        // Then
        coVerify { themeRepository.updateDynamicColors(useDynamicColors) }
    }

    @Test
    fun `updateCustomSeedColor should call repository`() = runTest {
        // Given
        val customColor = 0xFF123456L
        coEvery { themeRepository.updateCustomSeedColor(customColor) } returns Unit

        // When
        themeUseCases.updateCustomSeedColor(customColor)

        // Then
        coVerify { themeRepository.updateCustomSeedColor(customColor) }
    }

    @Test
    fun `resetToDefault should call repository`() = runTest {
        // Given
        coEvery { themeRepository.resetToDefault() } returns Unit

        // When
        themeUseCases.resetToDefault()

        // Then
        coVerify { themeRepository.resetToDefault() }
    }

    @Test
    fun `toggleThemeMode should switch from SYSTEM to LIGHT`() = runTest {
        // Given
        val currentPreference = testThemePreference.copy(themeMode = ThemeMode.SYSTEM)
        every { themeRepository.getThemePreference() } returns flowOf(currentPreference)
        coEvery { themeRepository.updateThemeMode(ThemeMode.LIGHT) } returns Unit

        // When
        themeUseCases.toggleThemeMode()

        // Then
        coVerify { themeRepository.updateThemeMode(ThemeMode.LIGHT) }
    }

    @Test
    fun `toggleThemeMode should switch from LIGHT to DARK`() = runTest {
        // Given
        val currentPreference = testThemePreference.copy(themeMode = ThemeMode.LIGHT)
        every { themeRepository.getThemePreference() } returns flowOf(currentPreference)
        coEvery { themeRepository.updateThemeMode(ThemeMode.DARK) } returns Unit

        // When
        themeUseCases.toggleThemeMode()

        // Then
        coVerify { themeRepository.updateThemeMode(ThemeMode.DARK) }
    }

    @Test
    fun `toggleThemeMode should switch from DARK to SYSTEM`() = runTest {
        // Given
        val currentPreference = testThemePreference.copy(themeMode = ThemeMode.DARK)
        every { themeRepository.getThemePreference() } returns flowOf(currentPreference)
        coEvery { themeRepository.updateThemeMode(ThemeMode.SYSTEM) } returns Unit

        // When
        themeUseCases.toggleThemeMode()

        // Then
        coVerify { themeRepository.updateThemeMode(ThemeMode.SYSTEM) }
    }
}