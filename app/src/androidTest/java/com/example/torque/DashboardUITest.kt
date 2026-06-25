package com.example.torque

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.torque.components.ui.dashboard.TorqueDashboardScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun appLaunches() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.torque", appContext.packageName)
    }

    @Test
    fun dashboardShowsWelcomeText() {
        composeTestRule.setContent {
            TorqueDashboardScreen()
        }

        composeTestRule.onNodeWithText("Motorista").assertExists()
    }
}
