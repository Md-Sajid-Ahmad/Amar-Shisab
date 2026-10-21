package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryMeta(
    val id: String,
    val nameBn: String,
    val icon: ImageVector,
    val color: Color
)

object ExpenseCategories {
    val FOOD = CategoryMeta("food", "খাবার ও বাজার", Icons.Default.Restaurant, Color(0xFFF59E0B))
    val HOUSING = CategoryMeta("housing", "বাসা ভাড়া ও বিল", Icons.Default.Home, Color(0xFF3B82F6))
    val TRANSPORT = CategoryMeta("transport", "পরিবহন ও যাতায়াত", Icons.Default.DirectionsBus, Color(0xFF10B981))
    val SHOPPING = CategoryMeta("shopping", "কেনাকাটা", Icons.Default.ShoppingBag, Color(0xFFEC4899))
    val HEALTH = CategoryMeta("health", "চিকিৎসা ও ওষুধ", Icons.Default.LocalHospital, Color(0xFFEF4444))
    val EDUCATION = CategoryMeta("education", "শিক্ষা ও বই", Icons.Default.School, Color(0xFF8B5CF6))
    val ENTERTAINMENT = CategoryMeta("entertainment", "বিনোদন ও ভ্রমণ", Icons.Default.SportsEsports, Color(0xFF6366F1))
    val FAMILY = CategoryMeta("family", "পরিবার ও উপহার", Icons.Default.CardGiftcard, Color(0xFF14B8A6))
    val OTHER = CategoryMeta("other", "অন্যান্য খরচ", Icons.Default.MoreHoriz, Color(0xFF64748B))

    val ALL = listOf(
        FOOD,
        HOUSING,
        TRANSPORT,
        SHOPPING,
        HEALTH,
        EDUCATION,
        ENTERTAINMENT,
        FAMILY,
        OTHER
    )

    fun getCategoryMeta(name: String): CategoryMeta {
        return ALL.find { it.nameBn == name || it.id == name } ?: OTHER
    }
}
