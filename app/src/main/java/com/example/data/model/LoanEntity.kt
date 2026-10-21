package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

const val LOAN_TYPE_LENT = "LENT"         // টাকা ধার দিয়েছি (আমি পাবো)
const val LOAN_TYPE_BORROWED = "BORROWED" // টাকা ধার নিয়েছি (আমাকে দিতে হবে)

@Entity(tableName = "loans")
@JsonClass(generateAdapter = true)
data class LoanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val personName: String,
    val phoneNumber: String = "",
    val type: String = LOAN_TYPE_LENT,
    val amount: Long,                    // Total loan principal
    val paidAmount: Long = 0L,           // Total amount repaid so far
    val dateTaken: Long = System.currentTimeMillis(),
    val dueDate: Long = 0L,              // Due date timestamp (0 if not specified)
    val notes: String = "",
    val isSettled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val remainingAmount: Long
        get() = (amount - paidAmount).coerceAtLeast(0L)

    val progress: Float
        get() = if (amount > 0L) (paidAmount.toFloat() / amount.toFloat()).coerceIn(0f, 1f) else 0f

    val isOverdue: Boolean
        get() = !isSettled && dueDate > 0L && dueDate < System.currentTimeMillis()
}
