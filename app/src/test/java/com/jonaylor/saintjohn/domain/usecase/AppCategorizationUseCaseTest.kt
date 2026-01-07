package com.jonaylor.saintjohn.domain.usecase

import android.content.pm.ApplicationInfo
import com.jonaylor.saintjohn.domain.model.AppCategory
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests for app categorization logic.
 * 
 * App categorization should:
 * - Use Android's declared category when available
 * - Fall back to package name heuristics
 * - Categorize common apps correctly
 */
class AppCategorizationUseCaseTest {

    private lateinit var useCase: AppCategorizationUseCase

    @Before
    fun setup() {
        useCase = AppCategorizationUseCase()
    }

    @Test
    fun `should use declared category GAME`() {
        val result = useCase.categorize("com.example.game", ApplicationInfo.CATEGORY_GAME)
        assertEquals(AppCategory.ENTERTAINMENT, result)
    }

    @Test
    fun `should use declared category SOCIAL`() {
        val result = useCase.categorize("com.example.app", ApplicationInfo.CATEGORY_SOCIAL)
        assertEquals(AppCategory.SOCIAL, result)
    }

    @Test
    fun `should use declared category PRODUCTIVITY`() {
        val result = useCase.categorize("com.example.app", ApplicationInfo.CATEGORY_PRODUCTIVITY)
        assertEquals(AppCategory.PRODUCTIVITY, result)
    }

    @Test
    fun `should use declared category NEWS`() {
        val result = useCase.categorize("com.example.app", ApplicationInfo.CATEGORY_NEWS)
        assertEquals(AppCategory.NEWS, result)
    }

    @Test
    fun `should categorize facebook as SOCIAL`() {
        val result = useCase.categorize("com.facebook.katana", ApplicationInfo.CATEGORY_UNDEFINED)
        assertEquals(AppCategory.SOCIAL, result)
    }

    @Test
    fun `should categorize instagram as SOCIAL`() {
        val result = useCase.categorize("com.instagram.android", ApplicationInfo.CATEGORY_UNDEFINED)
        assertEquals(AppCategory.SOCIAL, result)
    }

    @Test
    fun `should categorize twitter as SOCIAL`() {
        val result = useCase.categorize("com.twitter.android", ApplicationInfo.CATEGORY_UNDEFINED)
        assertEquals(AppCategory.SOCIAL, result)
    }

    @Test
    fun `should categorize whatsapp as COMMUNICATION`() {
        val result = useCase.categorize("com.whatsapp", ApplicationInfo.CATEGORY_UNDEFINED)
        assertEquals(AppCategory.COMMUNICATION, result)
    }

    @Test
    fun `should categorize telegram as COMMUNICATION`() {
        val result = useCase.categorize("org.telegram.messenger", ApplicationInfo.CATEGORY_UNDEFINED)
        assertEquals(AppCategory.COMMUNICATION, result)
    }

    @Test
    fun `should categorize slack as COMMUNICATION`() {
        val result = useCase.categorize("com.Slack", ApplicationInfo.CATEGORY_UNDEFINED)
        assertEquals(AppCategory.COMMUNICATION, result)
    }

    @Test
    fun `should categorize gmail as COMMUNICATION`() {
        val result = useCase.categorize("com.google.android.gmail", ApplicationInfo.CATEGORY_UNDEFINED)
        assertEquals(AppCategory.COMMUNICATION, result)
    }

    @Test
    fun `should categorize youtube as ENTERTAINMENT`() {
        val result = useCase.categorize("com.google.android.youtube", ApplicationInfo.CATEGORY_UNDEFINED)
        assertEquals(AppCategory.ENTERTAINMENT, result)
    }

    @Test
    fun `should categorize netflix as ENTERTAINMENT`() {
        val result = useCase.categorize("com.netflix.mediaclient", ApplicationInfo.CATEGORY_UNDEFINED)
        assertEquals(AppCategory.ENTERTAINMENT, result)
    }

    @Test
    fun `should categorize spotify as ENTERTAINMENT`() {
        val result = useCase.categorize("com.spotify.music", ApplicationInfo.CATEGORY_UNDEFINED)
        assertEquals(AppCategory.ENTERTAINMENT, result)
    }

    @Test
    fun `should categorize uber as TRAVEL`() {
        val result = useCase.categorize("com.ubercab", ApplicationInfo.CATEGORY_UNDEFINED)
        assertEquals(AppCategory.TRAVEL, result)
    }

    @Test
    fun `should categorize google maps as TRAVEL`() {
        val result = useCase.categorize("com.google.android.apps.maps", ApplicationInfo.CATEGORY_UNDEFINED)
        assertEquals(AppCategory.TRAVEL, result)
    }

    @Test
    fun `should categorize amazon shopping as SHOPPING`() {
        val result = useCase.categorize("com.amazon.mShop.android.shopping", ApplicationInfo.CATEGORY_UNDEFINED)
        assertEquals(AppCategory.SHOPPING, result)
    }

    @Test
    fun `should categorize paypal as FINANCE`() {
        val result = useCase.categorize("com.paypal.android.p2pmobile", ApplicationInfo.CATEGORY_UNDEFINED)
        assertEquals(AppCategory.FINANCE, result)
    }

    @Test
    fun `should categorize chrome as UTILITIES`() {
        val result = useCase.categorize("com.android.chrome", ApplicationInfo.CATEGORY_UNDEFINED)
        assertEquals(AppCategory.UTILITIES, result)
    }

    @Test
    fun `should categorize duolingo as EDUCATION`() {
        val result = useCase.categorize("com.duolingo", ApplicationInfo.CATEGORY_UNDEFINED)
        assertEquals(AppCategory.EDUCATION, result)
    }

    @Test
    fun `should categorize unknown package as OTHER`() {
        val result = useCase.categorize("com.example.unknownapp", ApplicationInfo.CATEGORY_UNDEFINED)
        assertEquals(AppCategory.OTHER, result)
    }

    @Test
    fun `package name matching should be case insensitive`() {
        val result1 = useCase.categorize("com.FACEBOOK.app", ApplicationInfo.CATEGORY_UNDEFINED)
        val result2 = useCase.categorize("com.FaceBook.App", ApplicationInfo.CATEGORY_UNDEFINED)
        
        assertEquals(AppCategory.SOCIAL, result1)
        assertEquals(AppCategory.SOCIAL, result2)
    }

    @Test
    fun `declared category should take precedence over package name heuristics`() {
        val result = useCase.categorize("com.facebook.games", ApplicationInfo.CATEGORY_PRODUCTIVITY)
        assertEquals(AppCategory.PRODUCTIVITY, result)
    }
}
