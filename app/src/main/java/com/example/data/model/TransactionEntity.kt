package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

const val TYPE_EXPENSE = "EXPENSE"
const val TYPE_REFUND = "REFUND"

@Entity(tableName = "transactions")
@JsonClass(generateAdapter = true)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val amount: Long,
    val description: String,
    val category: String,
    val type: String = TYPE_EXPENSE,
    val notes: String = "",
    val timestamp: Long,
    val yearMonth: String, // Format: "yyyy-MM" in Asia/Dhaka timezone
    val accountId: Long = 1L
)
