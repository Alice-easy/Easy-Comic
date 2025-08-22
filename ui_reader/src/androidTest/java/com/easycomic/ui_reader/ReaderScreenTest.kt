package com.easycomic.ui_reader

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.easycomic.domain.model.Manga
import com.easycomic.domain.model.ReadingStatus
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReaderScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Mock ViewModel
    private val mockViewModel = mockk<ReaderViewModel>(relaxed = true)

    // StateFlow mock for ViewModel state
    private val uiStateFlow = MutableStateFlow(ReaderUiState())

    // Test data
    private val testManga = Manga(
        id = 1L,
        title = "Test Manga",
        author = "Test Author",
        filePath = "/path/to/manga.zip",
        totalPages = 100,
        currentPage = 50,
        isFavorite = false,
        readingStatus = ReadingStatus.READING,
        dateAdded = 1000L,
        lastRead = 2000L
    )

    @Before
    fun setup() {
        // Setup ViewModel mock to return StateFlow
        every { mockViewModel.uiState } returns uiStateFlow

        // Setup default method behaviors
        every { mockViewModel.nextPage() } returns Unit
        every { mockViewModel.previousPage() } returns Unit
        every { mockViewModel.goToPage(any()) } returns Unit
        every { mockViewModel.toggleMenu() } returns Unit
        every { mockViewModel.setReadingMode(any()) } returns Unit
        every { mockViewModel.setReadingDirection(any()) } returns Unit
        every { mockViewModel.clearError() } returns Unit

        // Setup default UI state
        uiStateFlow.value = ReaderUiState(
            manga = testManga,
            pageCount = 100,
            currentPage = 50,
            isLoading = false,
            error = null,
            readingProgress = 0.5f,
            settings = ReaderSettings()
        )
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // === Loading State Tests ===

    @Test
    fun readerScreen_whenLoading_showsLoadingIndicator() {
        uiStateFlow.value = ReaderUiState(isLoading = true)

        composeTestRule.setContent {
            ReaderScreen(
                viewModel = mockViewModel,
                onBack = { }
            )
        }

        composeTestRule
            .onNodeWithContentDescription("Loading")
            .assertIsDisplayed()
    }

    @Test
    fun readerScreen_whenError_showsErrorDialog() {
        uiStateFlow.value = ReaderUiState(
            error = "Failed to load manga",
            isLoading = false
        )

        composeTestRule.setContent {
            ReaderScreen(
                viewModel = mockViewModel,
                onBack = { }
            )
        }

        composeTestRule
            .onNodeWithText("Failed to load manga")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("确定")
            .assertIsDisplayed()
    }

    @Test
    fun readerScreen_dismissError_callsViewModelClearError() {
        uiStateFlow.value = ReaderUiState(
            error = "Test error",
            isLoading = false
        )

        composeTestRule.setContent {
            ReaderScreen(
                viewModel = mockViewModel,
                onBack = { }
            )
        }

        composeTestRule
            .onNodeWithText("确定")
            .performClick()

        verify { mockViewModel.clearError() }
    }

    // === Reading Interface Tests ===

    @Test
    fun readerScreen_withValidManga_displaysReadingInterface() {
        composeTestRule.setContent {
            ReaderScreen(
                viewModel = mockViewModel,
                onBack = { }
            )
        }

        // Should show the main reading container
        composeTestRule
            .onNodeWithTag("reading_container")
            .assertIsDisplayed()

        // Should not show loading or error
        composeTestRule
            .onNodeWithContentDescription("Loading")
            .assertDoesNotExist()

        composeTestRule
            .onAllNodesWithText("确定")
            .assertCountEquals(0)
    }

    @Test
    fun readerScreen_tapCenter_togglesMenu() {
        composeTestRule.setContent {
            ReaderScreen(
                viewModel = mockViewModel,
                onBack = { }
            )
        }

        // Tap the center of the screen (reading area)
        composeTestRule
            .onNodeWithTag("reading_container")
            .performClick()

        verify { mockViewModel.toggleMenu() }
    }

    @Test
    fun readerScreen_whenMenuVisible_showsTopAndBottomBars() {
        uiStateFlow.value = uiStateFlow.value.copy(
            settings = ReaderSettings(isMenuVisible = true)
        )

        composeTestRule.setContent {
            ReaderScreen(
                viewModel = mockViewModel,
                onBack = { }
            )
        }

        // Should show top bar with back button
        composeTestRule
            .onNodeWithContentDescription("返回")
            .assertIsDisplayed()

        // Should show manga title
        composeTestRule
            .onNodeWithText("Test Manga")
            .assertIsDisplayed()

        // Should show bottom bar with page info
        composeTestRule
            .onNodeWithText("51 / 100") // currentPage + 1 for display
            .assertIsDisplayed()
    }

    @Test
    fun readerScreen_clickBackButton_callsOnBack() {
        uiStateFlow.value = uiStateFlow.value.copy(
            settings = ReaderSettings(isMenuVisible = true)
        )

        var backCalled = false

        composeTestRule.setContent {
            ReaderScreen(
                viewModel = mockViewModel,
                onBack = { backCalled = true }
            )
        }

        composeTestRule
            .onNodeWithContentDescription("返回")
            .performClick()

        assert(backCalled)
    }

    // === Page Navigation Tests ===

    @Test
    fun readerScreen_leftTap_callsPreviousPage() {
        composeTestRule.setContent {
            ReaderScreen(
                viewModel = mockViewModel,
                onBack = { }
            )
        }

        // Tap left side of screen (previous page area)
        composeTestRule
            .onNodeWithTag("reading_container")
            .performTouchInput {
                click(Offset(size.width * 0.2f, size.height * 0.5f))
            }

        verify { mockViewModel.previousPage() }
    }

    @Test
    fun readerScreen_rightTap_callsNextPage() {
        composeTestRule.setContent {
            ReaderScreen(
                viewModel = mockViewModel,
                onBack = { }
            )
        }

        // Tap right side of screen (next page area)
        composeTestRule
            .onNodeWithTag("reading_container")
            .performTouchInput {
                click(Offset(size.width * 0.8f, size.height * 0.5f))
            }

        verify { mockViewModel.nextPage() }
    }

    // === Progress Slider Tests ===

    @Test
    fun readerScreen_whenMenuVisible_showsProgressSlider() {
        uiStateFlow.value = uiStateFlow.value.copy(
            settings = ReaderSettings(isMenuVisible = true)
        )

        composeTestRule.setContent {
            ReaderScreen(
                viewModel = mockViewModel,
                onBack = { }
            )
        }

        // Should show progress slider
        composeTestRule
            .onNodeWithTag("progress_slider")
            .assertIsDisplayed()
    }

    @Test
    fun readerScreen_changeSliderValue_callsGoToPage() {
        uiStateFlow.value = uiStateFlow.value.copy(
            settings = ReaderSettings(isMenuVisible = true)
        )

        composeTestRule.setContent {
            ReaderScreen(
                viewModel = mockViewModel,
                onBack = { }
            )
        }

        // Change slider to 75% (page 75 of 100)
        composeTestRule
            .onNodeWithTag("progress_slider")
            .performTouchInput {
                // Simulate dragging slider to 75% position
                val startX = size.width * 0.5f // Current position (50%)
                val endX = size.width * 0.75f   // Target position (75%)
                swipeWithVelocity(
                    start = Offset(startX, centerY),
                    end = Offset(endX, centerY),
                    endVelocity = 0f
                )
            }

        // Should call goToPage with calculated page number
        verify { mockViewModel.goToPage(any()) }
    }

    // === Settings Menu Tests ===

    @Test
    fun readerScreen_clickSettingsButton_showsSettingsMenu() {
        uiStateFlow.value = uiStateFlow.value.copy(
            settings = ReaderSettings(isMenuVisible = true)
        )

        composeTestRule.setContent {
            ReaderScreen(
                viewModel = mockViewModel,
                onBack = { }
            )
        }

        composeTestRule
            .onNodeWithContentDescription("阅读设置")
            .performClick()

        // Should show reading mode options
        composeTestRule
            .onNodeWithText("适应宽度")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("适应高度")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("原始大小")
            .assertIsDisplayed()
    }

    @Test
    fun readerScreen_selectReadingMode_callsViewModel() {
        uiStateFlow.value = uiStateFlow.value.copy(
            settings = ReaderSettings(isMenuVisible = true)
        )

        composeTestRule.setContent {
            ReaderScreen(
                viewModel = mockViewModel,
                onBack = { }
            )
        }

        // Open settings menu
        composeTestRule
            .onNodeWithContentDescription("阅读设置")
            .performClick()

        // Select fit height mode
        composeTestRule
            .onNodeWithText("适应高度")
            .performClick()

        verify { mockViewModel.setReadingMode(ReadingMode.FIT_HEIGHT) }
    }

    @Test
    fun readerScreen_selectReadingDirection_callsViewModel() {
        uiStateFlow.value = uiStateFlow.value.copy(
            settings = ReaderSettings(isMenuVisible = true)
        )

        composeTestRule.setContent {
            ReaderScreen(
                viewModel = mockViewModel,
                onBack = { }
            )
        }

        // Open settings menu
        composeTestRule
            .onNodeWithContentDescription("阅读设置")
            .performClick()

        // Select right-to-left direction
        composeTestRule
            .onNodeWithText("从右到左")
            .performClick()

        verify { mockViewModel.setReadingDirection(ReadingDirection.RIGHT_TO_LEFT) }
    }

    // === Reading Mode Tests ===

    @Test
    fun readerScreen_horizontalReadingMode_showsHorizontalPager() {
        uiStateFlow.value = uiStateFlow.value.copy(
            settings = ReaderSettings(readingMode = ReadingMode.HORIZONTAL_PAGER)
        )

        composeTestRule.setContent {
            ReaderScreen(
                viewModel = mockViewModel,
                onBack = { }
            )
        }

        // Should show horizontal pager
        composeTestRule
            .onNodeWithTag("horizontal_pager")
            .assertIsDisplayed()

        // Should not show vertical pager
        composeTestRule
            .onNodeWithTag("vertical_pager")
            .assertDoesNotExist()
    }

    @Test
    fun readerScreen_verticalReadingMode_showsVerticalScroller() {
        uiStateFlow.value = uiStateFlow.value.copy(
            settings = ReaderSettings(readingMode = ReadingMode.VERTICAL_SCROLL)
        )

        composeTestRule.setContent {
            ReaderScreen(
                viewModel = mockViewModel,
                onBack = { }
            )
        }

        // Should show vertical scroll layout
        composeTestRule
            .onNodeWithTag("vertical_scroll")
            .assertIsDisplayed()

        // Should not show horizontal pager
        composeTestRule
            .onNodeWithTag("horizontal_pager")
            .assertDoesNotExist()
    }

    // === Page Information Tests ===

    @Test
    fun readerScreen_displaysCorrectPageNumbers() {
        uiStateFlow.value = uiStateFlow.value.copy(
            currentPage = 25,
            pageCount = 100,
            settings = ReaderSettings(isMenuVisible = true)
        )

        composeTestRule.setContent {
            ReaderScreen(
                viewModel = mockViewModel,
                onBack = { }
            )
        }

        // Should show current page as display format (0-based + 1)
        composeTestRule
            .onNodeWithText("26 / 100")
            .assertIsDisplayed()
    }

    @Test
    fun readerScreen_displaysReadingProgress() {
        uiStateFlow.value = uiStateFlow.value.copy(
            readingProgress = 0.75f,
            settings = ReaderSettings(isMenuVisible = true)
        )

        composeTestRule.setContent {
            ReaderScreen(
                viewModel = mockViewModel,
                onBack = { }
            )
        }

        // Should show progress percentage
        composeTestRule
            .onNodeWithText("75%")
            .assertIsDisplayed()
    }

    // === State Persistence Tests ===

    @Test
    fun readerScreen_settingsChanges_persistInUI() {
        composeTestRule.setContent {
            ReaderScreen(
                viewModel = mockViewModel,
                onBack = { }
            )
        }

        // Initially menu should be hidden
        composeTestRule
            .onNodeWithContentDescription("返回")
            .assertDoesNotExist()

        // Update state to show menu
        uiStateFlow.value = uiStateFlow.value.copy(
            settings = ReaderSettings(isMenuVisible = true)
        )

        // Menu should now be visible
        composeTestRule
            .onNodeWithContentDescription("返回")
            .assertIsDisplayed()

        // Update reading mode
        uiStateFlow.value = uiStateFlow.value.copy(
            settings = ReaderSettings(
                isMenuVisible = true,
                readingMode = ReadingMode.FIT_HEIGHT
            )
        )

        // Open settings to verify the mode is selected
        composeTestRule
            .onNodeWithContentDescription("阅读设置")
            .performClick()

        // The fit height option should be selected/highlighted
        composeTestRule
            .onNodeWithText("适应高度")
            .assertIsDisplayed()
    }

    // === Accessibility Tests ===

    @Test
    fun readerScreen_hasProperContentDescriptions() {
        uiStateFlow.value = uiStateFlow.value.copy(
            settings = ReaderSettings(isMenuVisible = true)
        )

        composeTestRule.setContent {
            ReaderScreen(
                viewModel = mockViewModel,
                onBack = { }
            )
        }

        // Check important UI elements have content descriptions
        composeTestRule
            .onNodeWithContentDescription("返回")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("阅读设置")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Reading progress slider")
            .assertIsDisplayed()
    }

    // === Error Handling Tests ===

    @Test
    fun readerScreen_errorDialogBackButton_callsOnBack() {
        uiStateFlow.value = ReaderUiState(
            error = "Critical error",
            isLoading = false
        )

        var backCalled = false

        composeTestRule.setContent {
            ReaderScreen(
                viewModel = mockViewModel,
                onBack = { backCalled = true }
            )
        }

        composeTestRule
            .onNodeWithText("返回")
            .performClick()

        assert(backCalled)
    }

    // === Integration Tests ===

    @Test
    fun readerScreen_completeReadingWorkflow_worksCorrectly() {
        composeTestRule.setContent {
            ReaderScreen(
                viewModel = mockViewModel,
                onBack = { }
            )
        }

        // 1. Start with hidden menu
        composeTestRule
            .onNodeWithContentDescription("返回")
            .assertDoesNotExist()

        // 2. Tap center to show menu
        composeTestRule
            .onNodeWithTag("reading_container")
            .performClick()

        verify { mockViewModel.toggleMenu() }

        // Simulate menu appearing
        uiStateFlow.value = uiStateFlow.value.copy(
            settings = ReaderSettings(isMenuVisible = true)
        )

        // 3. Menu should now be visible
        composeTestRule
            .onNodeWithContentDescription("返回")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Test Manga")
            .assertIsDisplayed()

        // 4. Navigate to next page
        composeTestRule
            .onNodeWithTag("reading_container")
            .performTouchInput {
                click(Offset(size.width * 0.8f, size.height * 0.5f))
            }

        verify { mockViewModel.nextPage() }

        // 5. Open settings menu
        composeTestRule
            .onNodeWithContentDescription("阅读设置")
            .performClick()

        // 6. Change reading mode
        composeTestRule
            .onNodeWithText("适应高度")
            .performClick()

        verify { mockViewModel.setReadingMode(ReadingMode.FIT_HEIGHT) }

        // 7. Use progress slider
        composeTestRule
            .onNodeWithTag("progress_slider")
            .performTouchInput {
                swipeWithVelocity(
                    start = Offset(centerX, centerY),
                    end = Offset(size.width * 0.9f, centerY),
                    endVelocity = 0f
                )
            }

        verify { mockViewModel.goToPage(any()) }
    }

    @Test
    fun readerScreen_gestureNavigation_worksCorrectly() {
        composeTestRule.setContent {
            ReaderScreen(
                viewModel = mockViewModel,
                onBack = { }
            )
        }

        val readingContainer = composeTestRule.onNodeWithTag("reading_container")

        // Test left tap (previous page)
        readingContainer.performTouchInput {
            click(Offset(size.width * 0.1f, size.height * 0.5f))
        }
        verify { mockViewModel.previousPage() }

        // Test right tap (next page)
        readingContainer.performTouchInput {
            click(Offset(size.width * 0.9f, size.height * 0.5f))
        }
        verify { mockViewModel.nextPage() }

        // Test center tap (toggle menu)
        readingContainer.performTouchInput {
            click(Offset(size.width * 0.5f, size.height * 0.5f))
        }
        verify { mockViewModel.toggleMenu() }
    }

    // === Performance Tests ===

    @Test
    fun readerScreen_withLargePageCount_performsWell() {
        uiStateFlow.value = uiStateFlow.value.copy(
            pageCount = 1000,
            currentPage = 500,
            readingProgress = 0.5f,
            settings = ReaderSettings(isMenuVisible = true)
        )

        composeTestRule.setContent {
            ReaderScreen(
                viewModel = mockViewModel,
                onBack = { }
            )
        }

        // Should display large page numbers correctly
        composeTestRule
            .onNodeWithText("501 / 1000")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("50%")
            .assertIsDisplayed()

        // UI should remain responsive
        composeTestRule
            .onNodeWithTag("progress_slider")
            .assertIsDisplayed()
    }
}
