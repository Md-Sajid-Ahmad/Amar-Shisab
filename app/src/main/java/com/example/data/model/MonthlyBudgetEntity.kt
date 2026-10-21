package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "monthly_budgets")
@JsonClass(generateAdapter = true)
data class MonthlyBudgetEntity(
    @PrimaryKey
    val yearMonth: String, // Format: "yyyy-MM", e.g., "2026-10"
    val targetBudget: Long = 0L,
    val startingBalance: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis()
)
