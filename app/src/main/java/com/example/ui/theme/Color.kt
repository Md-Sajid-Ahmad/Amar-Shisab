package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// Primary Color Palette (from the user's uploaded reference image):
// Electric Blue: #0082FB
// Deep Royal Blue: #0064E0
// Crisp Canvas Off-White: #F1F5F8
// Obsidian Slate: #1C2B33
// Pure White: #FFFFFF
// =========================================================================

val MetaBlue = Color(0xFF0082FB)          // Electric Blue
val MetaDarkBlue = Color(0xFF0064E0)      // Deep Royal Blue
val MetaOffWhite = Color(0xFFF1F5F8)      // Soft clean background
val MetaObsidian = Color(0xFF1C2B33)      // Deep Charcoal Slate

// Elevated Surfaces & Accents
val White = Color(0xFFFFFFFF)
val MetaBlueLight = Color(0xFFEAF4FE)
val MetaBlueMuted = Color(0xFFCCE4FF)
val MetaSlateLight = Color(0xFFE2E8F0)
val MetaSlateMuted = Color(0xFF5A6E7C)

// Semantic Financial Indicators
val AccentExpense = Color(0xFFE03131)     // Crisp Coral Red for Expenses (-)
val AccentExpenseBg = Color(0xFFFFECEB)
val AccentIncome = Color(0xFF0CA678)      // Emerald Green for Income/Received (+)
val AccentIncomeBg = Color(0xFFE6FCF5)
val AccentWarning = Color(0xFFF59F00)     // Amber Gold
val AccentWarningBg = Color(0xFFFFF9DB)

// Yellow & Gold Highlights
val YellowBright = Color(0xFFFFD43B)      // Radiant golden spark
val YellowGold = Color(0xFFFAB005)
val YellowLight = Color(0xFFFFF9DB)
val WarningOrange = AccentWarning

// Legacy & Component Aliases for full backwards-compatibility
val Navy900 = MetaObsidian
val Navy800 = MetaDarkBlue
val Navy700 = Color(0xFF16324F)
val Navy600 = MetaBlue
val Navy500 = MetaBlue
val Navy400 = Color(0xFF389BF2)

val Slate600 = MetaSlateMuted
val Slate500 = Color(0xFF64748B)
val Slate400 = Color(0xFF94A3B8)
val Slate200 = MetaSlateLight
val Slate100 = MetaOffWhite

val DangerRed = AccentExpense
val DangerRedLight = AccentExpenseBg
val SuccessGreen = AccentIncome
val SuccessGreenLight = AccentIncomeBg
