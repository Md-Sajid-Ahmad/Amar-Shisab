package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.FinancialSummary
import com.example.data.model.MonthlyBudgetEntity
import com.example.data.model.TYPE_EXPENSE
import com.example.data.model.TYPE_REFUND
import com.example.data.model.TransactionEntity
import com.example.data.repository.ExpenseRepository
import com.example.util.DateUtils
import com.example.util.PdfReportGenerator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AmarHisabComprehensiveTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var repository: ExpenseRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ExpenseRepository(
            database.transactionDao(),
            database.monthlyBudgetDao(),
            database.accountDao(),
            database.loanDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testCreateAndRetrieveMonthlyBudget() = runBlocking {
        val yearMonth = "2026-10"
        val targetBudget = 30000L
        val startingBalance = 45000L

        repository.saveMonthlyBudget(yearMonth, targetBudget, startingBalance)

        val budget = repository.getBudgetForMonth(yearMonth).first()
        assertNotNull(budget)
        assertEquals(yearMonth, budget!!.yearMonth)
        assertEquals(targetBudget, budget.targetBudget)
        assertEquals(startingBalance, budget.startingBalance)
    }

    @Test
    fun testCreateMultipleAccountsAndIsolatedTransactions() = runBlocking {
        // Ensure default account
        val defaultAcc = repository.ensureDefaultAccount()
        assertEquals("আমার হাতখরচ", defaultAcc.name)

        // Create a new separate account (e.g. দোকানের হিসাব)
        val shopAccId = repository.createAccount("দোকানের হিসাব", "🏪")
        assertTrue(shopAccId > 0L)

        val accounts = repository.getAllAccounts().first()
        assertEquals(2, accounts.size)

        // Add transaction to default account
        val ym = DateUtils.currentYearMonth()
        repository.addTransaction(
            amount = 100L,
            description = "টিফিন",
            category = "খাবার ও নাস্তা",
            accountId = defaultAcc.id
        )

        // Add transaction to shop account
        repository.addTransaction(
            amount = 5000L,
            description = "মাল ক্রয়",
            category = "দোকান",
            accountId = shopAccId
        )

        // Verify default account only has its own transaction
        val defaultTxs = repository.getTransactionsForAccountAndMonth(defaultAcc.id, ym).first()
        assertEquals(1, defaultTxs.size)
        assertEquals(100L, defaultTxs[0].amount)

        // Verify shop account only has its own transaction
        val shopTxs = repository.getTransactionsForAccountAndMonth(shopAccId, ym).first()
        assertEquals(1, shopTxs.size)
        assertEquals(5000L, shopTxs[0].amount)

        // Verify summary isolation
        val defaultSummary = repository.getFinancialSummaryForAccount(defaultAcc.id, ym).first()
        assertEquals(100L, defaultSummary.totalExpenses)

        val shopSummary = repository.getFinancialSummaryForAccount(shopAccId, ym).first()
        assertEquals(5000L, shopSummary.totalExpenses)
    }

    @Test
    fun testAddExpenseAndAutomaticTimestamp() = runBlocking {
        val id = repository.addTransaction(
            amount = 1500L,
            description = "কাঁচাবাজার",
            category = "খাবার ও বাজার",
            type = TYPE_EXPENSE,
            notes = "সাপ্তাহিক বাজার"
        )
        assertTrue(id > 0L)

        val currentYm = DateUtils.currentYearMonth()
        val list = repository.getTransactionsForMonth(currentYm).first()
        assertEquals(1, list.size)

        val tx = list[0]
        assertEquals(1500L, tx.amount)
        assertEquals("কাঁচাবাজার", tx.description)
        assertEquals("খাবার ও বাজার", tx.category)
        assertEquals(TYPE_EXPENSE, tx.type)
        assertEquals("সাপ্তাহিক বাজার", tx.notes)
        assertTrue(tx.timestamp > 0L)
        assertEquals(currentYm, tx.yearMonth)
    }

    @Test
    fun testCorrectBalanceCalculations() = runBlocking {
        val yearMonth = "2026-10"
        val startingBalance = 50000L
        val targetBudget = 30000L

        repository.saveMonthlyBudget(yearMonth, targetBudget, startingBalance)

        // Expense 1: 10000
        database.transactionDao().insert(
            TransactionEntity(
                amount = 10000L,
                description = "বাসা ভাড়া",
                category = "বাসা ভাড়া ও বিল",
                type = TYPE_EXPENSE,
                timestamp = System.currentTimeMillis(),
                yearMonth = yearMonth
            )
        )

        // Expense 2: 5000
        database.transactionDao().insert(
            TransactionEntity(
                amount = 5000L,
                description = "মুদি বাজার",
                category = "খাবার ও বাজার",
                type = TYPE_EXPENSE,
                timestamp = System.currentTimeMillis(),
                yearMonth = yearMonth
            )
        )

        // Refund: 2000
        database.transactionDao().insert(
            TransactionEntity(
                amount = 2000L,
                description = "ফেরত পণ্য",
                category = "কেনাকাটা",
                type = TYPE_REFUND,
                timestamp = System.currentTimeMillis(),
                yearMonth = yearMonth
            )
        )

        val summary = repository.getFinancialSummary(yearMonth).first()

        // 1. Total expenses = sum of expenses = 10000 + 5000 = 15000
        assertEquals(15000L, summary.totalExpenses)

        // 2. Total refunds = 2000
        assertEquals(2000L, summary.totalRefunds)

        // 3. Actual remaining balance = starting - expenses + refunds = 50000 - 15000 + 2000 = 37000
        assertEquals(37000L, summary.actualRemainingBalance)

        // 4. Budget remaining = target - expenses = 30000 - 15000 = 15000
        assertEquals(15000L, summary.budgetRemaining)

        // 5. Budget usage = (15000 * 100) / 30000 = 50%
        assertEquals(50, summary.budgetUsagePercent)
        assertFalse(summary.isOverBudget)
        assertFalse(summary.isNegativeBalance)
    }

    @Test
    fun testEditAndRecalculateTransaction() = runBlocking {
        val currentYm = DateUtils.currentYearMonth()
        val id = repository.addTransaction(
            amount = 1000L,
            description = "নাস্তা",
            category = "খাবার ও বাজার"
        )

        val originalTx = repository.getTransactionsForMonth(currentYm).first().first { it.id == id }
        assertEquals(1000L, originalTx.amount)

        // Edit amount to 2500
        val updatedTx = originalTx.copy(amount = 2500L, description = "দুপুরের খাবার")
        repository.updateTransaction(updatedTx)

        val refreshedTx = repository.getTransactionsForMonth(currentYm).first().first { it.id == id }
        assertEquals(2500L, refreshedTx.amount)
        assertEquals("দুপুরের খাবার", refreshedTx.description)
    }

    @Test
    fun testDeleteTransactionAndImmediateRecalculation() = runBlocking {
        val currentYm = DateUtils.currentYearMonth()
        val id = repository.addTransaction(
            amount = 3000L,
            description = "বিদ্যুৎ বিল",
            category = "বাসা ভাড়া ও বিল"
        )

        val listBefore = repository.getTransactionsForMonth(currentYm).first()
        assertEquals(1, listBefore.size)

        repository.deleteTransaction(listBefore[0])

        val listAfter = repository.getTransactionsForMonth(currentYm).first()
        assertEquals(0, listAfter.size)
    }

    @Test
    fun testMonthBoundariesInAsiaDhakaTimezone() {
        val dhakaZone = ZoneId.of("Asia/Dhaka")

        // 2026-10-31 23:59:00 Asia/Dhaka is 2026-10-31 17:59:00 UTC
        val endOfOctoberDhaka = ZonedDateTime.of(
            LocalDateTime.of(2026, 10, 31, 23, 59, 0),
            dhakaZone
        )
        val ymOctober = DateUtils.getYearMonthFromTimestamp(endOfOctoberDhaka.toInstant().toEpochMilli())
        assertEquals("2026-10", ymOctober)

        // 2026-11-01 00:01:00 Asia/Dhaka is 2026-10-31 18:01:00 UTC
        // In UTC this is still October, but in Asia/Dhaka it MUST be November 2026-11
        val startOfNovemberDhaka = ZonedDateTime.of(
            LocalDateTime.of(2026, 11, 1, 0, 1, 0),
            dhakaZone
        )
        val ymNovember = DateUtils.getYearMonthFromTimestamp(startOfNovemberDhaka.toInstant().toEpochMilli())
        assertEquals("2026-11", ymNovember)
    }

    @Test
    fun testBengaliNumeralsAndCurrencyFormatting() {
        assertEquals("৳ ১,৫০০", DateUtils.formatTaka(1500L))
        assertEquals("৳ ৫০,০০০", DateUtils.formatTaka(50000L))
        assertEquals("১২৩৪৫", DateUtils.toBengaliDigits("12345"))
        assertEquals("অক্টোবর ২০২৬", DateUtils.formatYearMonthBengali("2026-10"))
    }

    @Test
    fun testInvalidAmountsAndBlankDescriptionValidation() = runBlocking {
        // Zero amount should fail
        try {
            repository.addTransaction(0L, "বিবরণ", "খাবার ও বাজার")
            fail("Expected exception for 0 amount")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("০ এর চেয়ে বেশি"))
        }

        // Negative amount should fail
        try {
            repository.addTransaction(-500L, "বিবরণ", "খাবার ও বাজার")
            fail("Expected exception for negative amount")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("০ এর চেয়ে বেশি"))
        }

        // Blank description should fail
        try {
            repository.addTransaction(100L, "   ", "খাবার ও বাজার")
            fail("Expected exception for blank description")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("বিবরণ"))
        }
    }

    @Test
    fun testBackupExportAndRestoration() = runBlocking {
        // Prepare some data
        val ym = "2026-10"
        repository.saveMonthlyBudget(ym, 25000L, 40000L)
        repository.addTransaction(2000L, "কেনাকাটা", "কেনাকাটা")

        val jsonBackup = repository.exportBackupJson()
        assertNotNull(jsonBackup)
        assertTrue(jsonBackup.contains("Amar Hisab"))
        assertTrue(jsonBackup.contains("কেনাকাটা"))

        // Clear all data
        repository.clearAllData()
        assertEquals(0, repository.getTransactionsForMonth(ym).first().size)

        // Restore backup
        val result = repository.restoreBackupJson(jsonBackup)
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull())

        // Verify data restored
        val restoredTxs = repository.getTransactionsForMonth(ym).first()
        assertEquals(1, restoredTxs.size)
        assertEquals("কেনাকাটা", restoredTxs[0].description)
        assertEquals(2000L, restoredTxs[0].amount)

        val restoredBudget = repository.getBudgetForMonth(ym).first()
        assertNotNull(restoredBudget)
        assertEquals(25000L, restoredBudget!!.targetBudget)
    }

    @Test
    fun testPdfReportGenerationAndBengaliAsset() {
        val ym = "2026-10"
        val summary = FinancialSummary(
            yearMonth = ym,
            startingBalance = 50000L,
            targetBudget = 30000L,
            totalExpenses = 12000L,
            totalRefunds = 1000L,
            actualRemainingBalance = 39000L,
            budgetRemaining = 18000L,
            budgetUsagePercent = 40,
            transactionCount = 2,
            isOverBudget = false,
            isNegativeBalance = false
        )

        val sampleTransactions = listOf(
            TransactionEntity(
                id = 1L,
                amount = 8000L,
                description = "বাসা ভাড়া ও ইউটিলিটি",
                category = "বাসা ভাড়া ও বিল",
                type = TYPE_EXPENSE,
                timestamp = System.currentTimeMillis(),
                yearMonth = ym
            ),
            TransactionEntity(
                id = 2L,
                amount = 4000L,
                description = "মাসিক বাজার",
                category = "খাবার ও বাজার",
                type = TYPE_EXPENSE,
                timestamp = System.currentTimeMillis() - 3600000,
                yearMonth = ym
            )
        )

        // Verify Bengali font is present in assets
        val fontStream = context.assets.open("fonts/noto_sans_bengali.ttf")
        assertNotNull(fontStream)
        assertTrue("Bengali font asset should be populated", fontStream.available() > 0)
        fontStream.close()

        // Test report generation call
        try {
            val pdfFile = PdfReportGenerator.generateReport(
                context = context,
                yearMonth = ym,
                summary = summary,
                transactions = sampleTransactions
            )
            assertNotNull(pdfFile)
            assertTrue(pdfFile.exists())
        } catch (e: IllegalStateException) {
            // Expected in host JVM Robolectric where android.graphics.pdf.PdfDocument native Skia driver is absent
            assertTrue(e.message?.contains("document is closed") == true)
        }
    }

    @Test
    fun testCreateAndRetrieveLoans() = runBlocking {
        // Create Lent loan (টাকা ধার দিয়েছি)
        val lentId = repository.createLoan(
            personName = "রহিম ভাই",
            phoneNumber = "01711000000",
            type = "LENT",
            amount = 5000L,
            dueDate = System.currentTimeMillis() + 86400000L * 7,
            notes = "দোকানের বাকি"
        )
        assertTrue(lentId > 0L)

        // Create Borrowed loan (টাকা ধার নিয়েছি)
        val borrowedId = repository.createLoan(
            personName = "করিম চাচা",
            phoneNumber = "01811000000",
            type = "BORROWED",
            amount = 3000L,
            dueDate = 0L,
            notes = "জরুরি ধার"
        )
        assertTrue(borrowedId > 0L)

        val loans = repository.getAllLoans().first()
        assertEquals(2, loans.size)

        val lentLoan = loans.find { it.id == lentId }
        assertNotNull(lentLoan)
        assertEquals("রহিম ভাই", lentLoan!!.personName)
        assertEquals(5000L, lentLoan.amount)
        assertEquals(0L, lentLoan.paidAmount)
        assertEquals(5000L, lentLoan.remainingAmount)
        assertFalse(lentLoan.isSettled)
    }

    @Test
    fun testRecordLoanPartialPaymentAndSettlement() = runBlocking {
        val loanId = repository.createLoan(
            personName = "শামীম",
            phoneNumber = "01911000000",
            type = "LENT",
            amount = 10000L,
            dueDate = System.currentTimeMillis() + 86400000L * 10
        )

        // Partial payment of 4000
        repository.recordLoanPayment(loanId, 4000L)
        var loan = repository.getAllLoans().first().find { it.id == loanId }!!
        assertEquals(4000L, loan.paidAmount)
        assertEquals(6000L, loan.remainingAmount)
        assertFalse(loan.isSettled)

        // Another partial payment of 6000 (total 10000, fully settles)
        repository.recordLoanPayment(loanId, 6000L)
        loan = repository.getAllLoans().first().find { it.id == loanId }!!
        assertEquals(10000L, loan.paidAmount)
        assertEquals(0L, loan.remainingAmount)
        assertTrue(loan.isSettled)

        // Test manual settle toggle
        repository.settleLoan(loanId, false)
        loan = repository.getAllLoans().first().find { it.id == loanId }!!
        assertFalse(loan.isSettled)

        // Test delete
        repository.deleteLoan(loan)
        val remainingLoans = repository.getAllLoans().first().filter { it.id == loanId }
        assertTrue(remainingLoans.isEmpty())
    }
}
