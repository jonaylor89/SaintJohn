package com.jonaylor.saintjohn.domain.usecase

import android.content.pm.ApplicationInfo
import com.jonaylor.saintjohn.domain.model.AppCategory

class AppCategorizationUseCase {

    fun categorize(packageName: String, category: Int): AppCategory {
        // First try using Android's declared category
        val declaredCategory = when (category) {
            ApplicationInfo.CATEGORY_GAME -> AppCategory.ENTERTAINMENT
            ApplicationInfo.CATEGORY_AUDIO -> AppCategory.MUSIC
            ApplicationInfo.CATEGORY_VIDEO -> AppCategory.ENTERTAINMENT
            ApplicationInfo.CATEGORY_IMAGE -> AppCategory.PHOTOGRAPHY
            ApplicationInfo.CATEGORY_SOCIAL -> AppCategory.SOCIAL
            ApplicationInfo.CATEGORY_NEWS -> AppCategory.NEWS
            ApplicationInfo.CATEGORY_MAPS -> AppCategory.TRAVEL
            ApplicationInfo.CATEGORY_PRODUCTIVITY -> AppCategory.PRODUCTIVITY
            else -> null
        }

        if (declaredCategory != null) {
            return declaredCategory
        }

        // Fallback to package name heuristics
        return categorizeByPackageName(packageName)
    }

    private fun categorizeByPackageName(packageName: String): AppCategory {
        val lowerPackage = packageName.lowercase()

        return when {
            // Social
            lowerPackage.contains("facebook") ||
            lowerPackage.contains("instagram") ||
            lowerPackage.contains("twitter") ||
            lowerPackage.contains("reddit") ||
            lowerPackage.contains("tiktok") ||
            lowerPackage.contains("snapchat") ||
            lowerPackage.contains("linkedin") -> AppCategory.SOCIAL

            // Communication
            lowerPackage.contains("whatsapp") ||
            lowerPackage.contains("telegram") ||
            lowerPackage.contains("messenger") ||
            lowerPackage.contains("signal") ||
            lowerPackage.contains("slack") ||
            lowerPackage.contains("discord") ||
            lowerPackage.contains("teams") ||
            lowerPackage.contains("zoom") ||
            lowerPackage.contains("meet") ||
            lowerPackage.contains("skype") -> AppCategory.COMMUNICATION

            // Productivity
            lowerPackage.contains("office") ||
            lowerPackage.contains("docs") ||
            lowerPackage.contains("sheets") ||
            lowerPackage.contains("slides") ||
            lowerPackage.contains("drive") ||
            lowerPackage.contains("notion") ||
            lowerPackage.contains("evernote") ||
            lowerPackage.contains("todoist") ||
            lowerPackage.contains("trello") ||
            lowerPackage.contains("asana") -> AppCategory.PRODUCTIVITY

            // Entertainment
            lowerPackage.contains("youtube") ||
            lowerPackage.contains("netflix") ||
            lowerPackage.contains("spotify") ||
            lowerPackage.contains("hulu") ||
            lowerPackage.contains("twitch") ||
            lowerPackage.contains("game") ||
            lowerPackage.contains("play") ||
            lowerPackage.contains("prime") && lowerPackage.contains("video") -> AppCategory.ENTERTAINMENT

            // Music
            lowerPackage.contains("music") ||
            lowerPackage.contains("spotify") ||
            lowerPackage.contains("soundcloud") ||
            lowerPackage.contains("pandora") -> AppCategory.MUSIC

            // Shopping
            lowerPackage.contains("amazon") && !lowerPackage.contains("video") ||
            lowerPackage.contains("ebay") ||
            lowerPackage.contains("shop") ||
            lowerPackage.contains("store") ||
            lowerPackage.contains("walmart") ||
            lowerPackage.contains("target") -> AppCategory.SHOPPING

            // Finance
            lowerPackage.contains("bank") ||
            lowerPackage.contains("paypal") ||
            lowerPackage.contains("venmo") ||
            lowerPackage.contains("finance") ||
            lowerPackage.contains("wallet") ||
            lowerPackage.contains("crypto") ||
            lowerPackage.contains("trading") -> AppCategory.FINANCE

            // Health
            lowerPackage.contains("health") ||
            lowerPackage.contains("fitness") ||
            lowerPackage.contains("workout") ||
            lowerPackage.contains("medical") -> AppCategory.HEALTH

            // Travel
            lowerPackage.contains("maps") ||
            lowerPackage.contains("uber") ||
            lowerPackage.contains("lyft") ||
            lowerPackage.contains("airbnb") ||
            lowerPackage.contains("booking") ||
            lowerPackage.contains("travel") ||
            lowerPackage.contains("flight") -> AppCategory.TRAVEL

            // Photography
            lowerPackage.contains("camera") ||
            lowerPackage.contains("photo") ||
            lowerPackage.contains("gallery") ||
            lowerPackage.contains("image") -> AppCategory.PHOTOGRAPHY

            // News
            lowerPackage.contains("news") ||
            lowerPackage.contains("cnn") ||
            lowerPackage.contains("bbc") ||
            lowerPackage.contains("nytimes") -> AppCategory.NEWS

            // Education
            lowerPackage.contains("education") ||
            lowerPackage.contains("learn") ||
            lowerPackage.contains("duolingo") ||
            lowerPackage.contains("coursera") ||
            lowerPackage.contains("udemy") ||
            lowerPackage.contains("khan") -> AppCategory.EDUCATION

            // Email as Communication
            lowerPackage.contains("mail") ||
            lowerPackage.contains("gmail") ||
            lowerPackage.contains("outlook") -> AppCategory.COMMUNICATION

            // Browser
            lowerPackage.contains("browser") ||
            lowerPackage.contains("chrome") ||
            lowerPackage.contains("firefox") ||
            lowerPackage.contains("safari") -> AppCategory.UTILITIES

            else -> AppCategory.OTHER
        }
    }
}
