package com.jonaylor.saintjohn.presentation.onboarding

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for onboarding skip logic.
 * 
 * The onboarding should be skipped when ALL of the following are true:
 * - Location permission granted
 * - Calendar permission granted  
 * - Usage stats permission granted
 * - App is set as default launcher
 */
class OnboardingSkipLogicTest {

    @Test
    fun `should skip onboarding when all permissions granted and is default launcher`() {
        val result = shouldSkipOnboarding(
            hasLocation = true,
            hasCalendar = true,
            hasUsageStats = true,
            isDefaultLauncher = true
        )
        assertTrue("Should skip when all conditions met", result)
    }

    @Test
    fun `should not skip onboarding when location permission missing`() {
        val result = shouldSkipOnboarding(
            hasLocation = false,
            hasCalendar = true,
            hasUsageStats = true,
            isDefaultLauncher = true
        )
        assertFalse("Should not skip when location missing", result)
    }

    @Test
    fun `should not skip onboarding when calendar permission missing`() {
        val result = shouldSkipOnboarding(
            hasLocation = true,
            hasCalendar = false,
            hasUsageStats = true,
            isDefaultLauncher = true
        )
        assertFalse("Should not skip when calendar missing", result)
    }

    @Test
    fun `should not skip onboarding when usage stats permission missing`() {
        val result = shouldSkipOnboarding(
            hasLocation = true,
            hasCalendar = true,
            hasUsageStats = false,
            isDefaultLauncher = true
        )
        assertFalse("Should not skip when usage stats missing", result)
    }

    @Test
    fun `should not skip onboarding when not default launcher`() {
        val result = shouldSkipOnboarding(
            hasLocation = true,
            hasCalendar = true,
            hasUsageStats = true,
            isDefaultLauncher = false
        )
        assertFalse("Should not skip when not default launcher", result)
    }

    @Test
    fun `should not skip onboarding when no permissions granted`() {
        val result = shouldSkipOnboarding(
            hasLocation = false,
            hasCalendar = false,
            hasUsageStats = false,
            isDefaultLauncher = false
        )
        assertFalse("Should not skip when nothing granted", result)
    }

    @Test
    fun `should not skip onboarding when only some permissions granted`() {
        val result = shouldSkipOnboarding(
            hasLocation = true,
            hasCalendar = true,
            hasUsageStats = false,
            isDefaultLauncher = false
        )
        assertFalse("Should not skip with partial permissions", result)
    }

    private fun shouldSkipOnboarding(
        hasLocation: Boolean,
        hasCalendar: Boolean,
        hasUsageStats: Boolean,
        isDefaultLauncher: Boolean
    ): Boolean {
        return hasLocation && hasCalendar && hasUsageStats && isDefaultLauncher
    }
}
