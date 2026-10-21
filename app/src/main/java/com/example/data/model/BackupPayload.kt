package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BackupPayload(
    val version: Int = 1,
    val appName: String = "Amar Hisab",
    val exportedAt: Long = System.currentTimeMillis(),
    val accounts: List<AccountEntity> = emptyList(),
    val budgets: List<MonthlyBudgetEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val loans: List<LoanEntity> = emptyList()
)
