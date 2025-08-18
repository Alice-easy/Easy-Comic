package com.easycomic

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.easycomic.data.di.dataModule
import com.easycomic.domain.di.domainModule
import com.easycomic.ui_bookshelf.di.bookshelfModule
import com.easycomic.ui_reader.di.readerModule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.rule.KoinTestRule

@RunWith(AndroidJUnit4::class)
class AppNavigationTest : KoinTest {

    // Rule order is important. KoinTestRule must be initialized before the activity is launched.
    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(dataModule, domainModule, bookshelfModule, readerModule)
    }

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun app_launches_and_displays_bookshelf() {
        // The KoinTestRule now correctly handles the Koin lifecycle before the UI is created.
        // The app should launch correctly and display the "Bookshelf" text.
        composeTestRule.onNodeWithText("Bookshelf").assertExists()
    }
}