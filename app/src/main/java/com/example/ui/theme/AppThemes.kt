package com.example.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.data.preferences.UserPreferencesRepository

data class AppThemeColors(
    val key: String,
    val nameBn: String,
    val nameEn: String,
    val emoji: String,
    val primary: Color,
    val primaryDark: Color,
    val topBarBg: Color,
    val background: Color,
    val surface: Color,
    val container: Color,
    val border: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val expense: Color,
    val expenseBg: Color,
    val income: Color,
    val incomeBg: Color,
    val highlight: Color,
    val heroGradient: List<Color>,
    val previewSwatches: List<Color>
)

object AppThemes {

    // 1. Electric Blue & Obsidian (Original Reference Palette)
    val BlueTheme = AppThemeColors(
        key = UserPreferencesRepository.THEME_BLUE,
        nameBn = "ইলেকট্রিক ব্লু (মূল)",
        nameEn = "Electric Blue",
        emoji = "💙",
        primary = Color(0xFF0082FB),
        primaryDark = Color(0xFF0064E0),
        topBarBg = Color(0xFF0064E0),
        background = Color(0xFFF1F5F8),
        surface = Color(0xFFFFFFFF),
        container = Color(0xFFEAF4FE),
        border = Color(0xFFE2E8F0),
        textPrimary = Color(0xFF1C2B33),
        textMuted = Color(0xFF5A6E7C),
        expense = Color(0xFFE03131),
        expenseBg = Color(0xFFFFECEB),
        income = Color(0xFF0CA678),
        incomeBg = Color(0xFFE6FCF5),
        highlight = Color(0xFFFFD43B),
        heroGradient = listOf(Color(0xFF0082FB), Color(0xFF0064E0)),
        previewSwatches = listOf(Color(0xFF0082FB), Color(0xFF0064E0), Color(0xFF1C2B33), Color(0xFFF1F5F8))
    )

    // 2. Fresh Lime (From User Image 1: "Лайм" - #1F2A05, #3D520B, #648215, #8FB61F, #B4D944, #D6EE8D, #F1FBD3)
    val LimeTheme = AppThemeColors(
        key = UserPreferencesRepository.THEME_LIME,
        nameBn = "ফ্রেশ লাইম (সবুজ)",
        nameEn = "Fresh Lime",
        emoji = "🍏",
        primary = Color(0xFF648215),
        primaryDark = Color(0xFF3D520B),
        topBarBg = Color(0xFF1F2A05),
        background = Color(0xFFF6FAF0),
        surface = Color(0xFFFFFFFF),
        container = Color(0xFFEFF8D3),
        border = Color(0xFFD6EE8D),
        textPrimary = Color(0xFF1F2A05),
        textMuted = Color(0xFF4D631F),
        expense = Color(0xFFD32F2F),
        expenseBg = Color(0xFFFFEBEE),
        income = Color(0xFF2E7D32),
        incomeBg = Color(0xFFE8F5E9),
        highlight = Color(0xFFB4D944),
        heroGradient = listOf(Color(0xFF648215), Color(0xFF1F2A05)),
        previewSwatches = listOf(Color(0xFF8FB61F), Color(0xFF648215), Color(0xFF3D520B), Color(0xFF1F2A05))
    )

    // 3. Emerald & Sage (From User Image 2: "@uxdam_" - #00412E, #96BF8A, #E8EAE5, #FFFFFF)
    val EmeraldTheme = AppThemeColors(
        key = UserPreferencesRepository.THEME_EMERALD,
        nameBn = "এমারেল্ড ও সেইজ",
        nameEn = "Emerald & Sage",
        emoji = "🌲",
        primary = Color(0xFF2D7557),
        primaryDark = Color(0xFF00412E),
        topBarBg = Color(0xFF00412E),
        background = Color(0xFFF0F4F1),
        surface = Color(0xFFFFFFFF),
        container = Color(0xFFE4EDE5),
        border = Color(0xFFD0DDD2),
        textPrimary = Color(0xFF00412E),
        textMuted = Color(0xFF4C6B5E),
        expense = Color(0xFFC93B3B),
        expenseBg = Color(0xFFFFECEB),
        income = Color(0xFF006B4B),
        incomeBg = Color(0xFFE6F5EE),
        highlight = Color(0xFF96BF8A),
        heroGradient = listOf(Color(0xFF2D7557), Color(0xFF00412E)),
        previewSwatches = listOf(Color(0xFF00412E), Color(0xFF2D7557), Color(0xFF96BF8A), Color(0xFFE8EAE5))
    )

    // 4. Royal Amethyst (From User Image 3: "Аметист" - #250A33, #451460, #6C2296, #9838CE, #BB74E2, #D9AEF1, #F3E4FB)
    val AmethystTheme = AppThemeColors(
        key = UserPreferencesRepository.THEME_AMETHYST,
        nameBn = "রয়্যাল অ্যামেথিস্ট",
        nameEn = "Royal Amethyst",
        emoji = "💜",
        primary = Color(0xFF9838CE),
        primaryDark = Color(0xFF6C2296),
        topBarBg = Color(0xFF250A33),
        background = Color(0xFFFAF2FC),
        surface = Color(0xFFFFFFFF),
        container = Color(0xFFF4E5FC),
        border = Color(0xFFE5C8F5),
        textPrimary = Color(0xFF250A33),
        textMuted = Color(0xFF6E467E),
        expense = Color(0xFFD92546),
        expenseBg = Color(0xFFFFEBF0),
        income = Color(0xFF109B74),
        incomeBg = Color(0xFFE6FAF3),
        highlight = Color(0xFFBB74E2),
        heroGradient = listOf(Color(0xFF9838CE), Color(0xFF451460)),
        previewSwatches = listOf(Color(0xFF9838CE), Color(0xFF6C2296), Color(0xFF451460), Color(0xFF250A33))
    )

    val allThemes = listOf(BlueTheme, LimeTheme, EmeraldTheme, AmethystTheme)

    fun getTheme(key: String): AppThemeColors {
        return allThemes.find { it.key.equals(key, ignoreCase = true) } ?: BlueTheme
    }
}

val LocalAppTheme = compositionLocalOf { AppThemes.BlueTheme }
val LocalIsBengali = compositionLocalOf { true }
