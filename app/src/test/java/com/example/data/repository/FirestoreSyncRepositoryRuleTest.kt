package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.AccountEntity
import com.example.data.model.MonthlyBudgetEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FirestoreSyncRepositoryRuleTest : FirestoreEmulatorTestBase() {

    private val testEmail = "testuser@example.com"
    private val defaultTimeoutMs = 10000L

    @Test
    fun saveUserProfile_authenticated_success() = runBlocking {
        val uid = signInTestUser(testEmail)
        val repository = FirestoreSyncRepository(firestore, auth)

        val result = withTimeout(defaultTimeoutMs) {
            repository.saveUserProfile(displayName = "Test User", email = testEmail)
        }
        assertTrue("User profile save should succeed", result.isSuccess)
    }

    @Test
    fun uploadTransaction_authenticated_success() = runBlocking {
        signInTestUser(testEmail)
        val repository = FirestoreSyncRepository(firestore, auth)

        val tx = TransactionEntity(
            id = 101L,
            amount = 1200L,
            description = "Test Groceries",
            category = "Bazaar",
            type = "EXPENSE",
            notes = "Test notes",
            timestamp = System.currentTimeMillis(),
            yearMonth = "2026-10",
            accountId = 1L
        )

        val result = withTimeout(defaultTimeoutMs) {
            repository.uploadTransaction(tx)
        }
        assertTrue("Upload transaction should succeed: ${result.exceptionOrNull()?.message}", result.isSuccess)
        assertEquals("tx_101", result.getOrThrow())
    }

    @Test
    fun uploadAccountAndBudget_authenticated_success() = runBlocking {
        signInTestUser(testEmail)
        val repository = FirestoreSyncRepository(firestore, auth)

        val acc = AccountEntity(
            id = 5L,
            name = "Test Wallet",
            iconEmoji = "💰",
            colorHex = "#123456"
        )
        val accResult = withTimeout(defaultTimeoutMs) {
            repository.uploadAccount(acc)
        }
        assertTrue("Upload account should succeed", accResult.isSuccess)

        val budget = MonthlyBudgetEntity(
            yearMonth = "2026-10",
            targetBudget = 15000L,
            startingBalance = 5000L
        )
        val budgetResult = withTimeout(defaultTimeoutMs) {
            repository.uploadBudget(budget)
        }
        assertTrue("Upload budget should succeed", budgetResult.isSuccess)
    }

    @Test
    fun unauthenticated_failsOperations() = runBlocking {
        auth.signOut()
        val repository = FirestoreSyncRepository(firestore, auth)

        val tx = TransactionEntity(
            id = 999L,
            amount = 500L,
            description = "Unauthorized",
            category = "Food",
            type = "EXPENSE",
            timestamp = System.currentTimeMillis(),
            yearMonth = "2026-10"
        )

        val result = withTimeout(defaultTimeoutMs) {
            repository.uploadTransaction(tx)
        }
        assertTrue("Unauthenticated upload must fail", result.isFailure)
    }

    @Test
    fun observeRemoteTransactions_emitsUploadedTransaction() = runBlocking {
        signInTestUser(testEmail)
        val repository = FirestoreSyncRepository(firestore, auth)

        val tx = TransactionEntity(
            id = 202L,
            amount = 850L,
            description = "Electricity Bill",
            category = "Bills",
            type = "EXPENSE",
            timestamp = System.currentTimeMillis(),
            yearMonth = "2026-10"
        )
        repository.uploadTransaction(tx)

        val emittedList = withTimeout(defaultTimeoutMs) {
            repository.observeRemoteTransactions().first { list ->
                list.any { it.amount == 850L && it.description == "Electricity Bill" }
            }
        }
        assertTrue("Emitted list should contain uploaded transaction", emittedList.isNotEmpty())
    }
}
