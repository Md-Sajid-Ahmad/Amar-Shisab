package com.example.data.repository

import com.example.data.local.AccountDao
import com.example.data.local.LoanDao
import com.example.data.local.MonthlyBudgetDao
import com.example.data.local.TransactionDao
import com.example.data.model.AccountEntity
import com.example.data.model.BackupPayload
import com.example.data.model.FinancialSummary
import com.example.data.model.LoanEntity
import com.example.data.model.MonthlyBudgetEntity
import com.example.data.model.TYPE_EXPENSE
import com.example.data.model.TransactionEntity
import com.example.data.model.firestore.FirestoreAccount
import com.example.data.model.firestore.FirestoreMonthlyBudget
import com.example.data.model.firestore.FirestoreTransaction
import com.example.util.DateUtils
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant

class ExpenseRepository(
    val transactionDao: TransactionDao,
    val monthlyBudgetDao: MonthlyBudgetDao,
    val accountDao: AccountDao,
    val loanDao: LoanDao
) {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val backupAdapter = moshi.adapter(BackupPayload::class.java)

    fun getAllAccounts(): Flow<List<AccountEntity>> {
        return accountDao.getAllAccounts()
    }

    suspend fun ensureDefaultAccount(): AccountEntity {
        val count = accountDao.getAccountCount()
        if (count == 0) {
            val defaultAcc = AccountEntity(
                name = "আমার হাতখরচ",
                iconEmoji = "👛",
                colorHex = "#0F2537"
            )
            val id = accountDao.insert(defaultAcc)
            return defaultAcc.copy(id = id)
        }
        return accountDao.getAllAccountsSync().first()
    }

    suspend fun createAccount(name: String, iconEmoji: String, colorHex: String = "#0F2537"): Long {
        require(name.isNotBlank()) { "হিসাবের নাম অবশ্যই দিতে হবে" }
        val acc = AccountEntity(
            name = name.trim(),
            iconEmoji = iconEmoji,
            colorHex = colorHex
        )
        return accountDao.insert(acc)
    }

    suspend fun updateAccount(account: AccountEntity) {
        require(account.name.isNotBlank()) { "হিসাবের নাম অবশ্যই দিতে হবে" }
        accountDao.update(account)
    }

    suspend fun deleteAccount(account: AccountEntity) {
        val all = accountDao.getAllAccountsSync()
        if (all.size <= 1) {
            throw IllegalStateException("কমপক্ষে একটি হিসাবের খাতা থাকতে হবে!")
        }
        accountDao.delete(account)
        transactionDao.deleteTransactionsByAccountId(account.id)
    }

    fun getTransactionsForMonth(yearMonth: String): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsForMonth(yearMonth)
    }

    fun getTransactionsForAccountAndMonth(accountId: Long, yearMonth: String): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsForAccountAndMonth(accountId, yearMonth)
    }

    fun getAllTransactions(): Flow<List<TransactionEntity>> {
        return transactionDao.getAllTransactions()
    }

    fun getAllTransactionsForAccount(accountId: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getAllTransactionsForAccount(accountId)
    }

    fun getRecentTransactions(limit: Int = 5): Flow<List<TransactionEntity>> {
        return transactionDao.getRecentTransactions(limit)
    }

    fun getBudgetForMonth(yearMonth: String): Flow<MonthlyBudgetEntity?> {
        return monthlyBudgetDao.getBudgetForMonth(yearMonth)
    }

    fun getAllBudgets(): Flow<List<MonthlyBudgetEntity>> {
        return monthlyBudgetDao.getAllBudgets()
    }

    fun getFinancialSummary(yearMonth: String): Flow<FinancialSummary> {
        return combine(
            monthlyBudgetDao.getBudgetForMonth(yearMonth),
            transactionDao.getTransactionsForMonth(yearMonth)
        ) { budget, transactions ->
            val starting = budget?.startingBalance ?: 0L
            val target = budget?.targetBudget ?: 0L
            FinancialSummary.calculate(
                yearMonth = yearMonth,
                startingBalance = starting,
                targetBudget = target,
                transactions = transactions
            )
        }
    }

    fun getFinancialSummaryForAccount(accountId: Long, yearMonth: String): Flow<FinancialSummary> {
        return combine(
            monthlyBudgetDao.getBudgetForMonth(yearMonth),
            transactionDao.getTransactionsForAccountAndMonth(accountId, yearMonth)
        ) { budget, transactions ->
            val starting = budget?.startingBalance ?: 0L
            val target = budget?.targetBudget ?: 0L
            FinancialSummary.calculate(
                yearMonth = yearMonth,
                startingBalance = starting,
                targetBudget = target,
                transactions = transactions
            )
        }
    }

    suspend fun addTransaction(
        amount: Long,
        description: String,
        category: String,
        type: String = TYPE_EXPENSE,
        notes: String = "",
        accountId: Long = 1L
    ): Long {
        require(amount > 0L) { "পরিমাণ অবশ্যই ০ এর চেয়ে বেশি হতে হবে" }
        require(description.isNotBlank()) { "বিবরণ অবশ্যই দিতে হবে" }

        val timestamp = Instant.now().toEpochMilli()
        val yearMonth = DateUtils.getYearMonthFromTimestamp(timestamp)

        val entity = TransactionEntity(
            amount = amount,
            description = description.trim(),
            category = category.trim(),
            type = type,
            notes = notes.trim(),
            timestamp = timestamp,
            yearMonth = yearMonth,
            accountId = accountId
        )
        return transactionDao.insert(entity)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        require(transaction.amount > 0L) { "পরিমাণ অবশ্যই ০ এর চেয়ে বেশি হতে হবে" }
        require(transaction.description.isNotBlank()) { "বিবরণ অবশ্যই দিতে হবে" }

        val correctYearMonth = DateUtils.getYearMonthFromTimestamp(transaction.timestamp)
        val updated = transaction.copy(yearMonth = correctYearMonth)
        transactionDao.update(updated)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.delete(transaction)
    }

    suspend fun saveMonthlyBudget(
        yearMonth: String,
        targetBudget: Long,
        startingBalance: Long
    ) {
        require(targetBudget >= 0L) { "বাজেট লক্ষ্য ০ বা তার বেশি হতে হবে" }
        val budget = MonthlyBudgetEntity(
            yearMonth = yearMonth,
            targetBudget = targetBudget,
            startingBalance = startingBalance,
            updatedAt = System.currentTimeMillis()
        )
        monthlyBudgetDao.insertOrUpdate(budget)
    }

    // --- Loan (ধার-দেনা) Operations ---
    fun getAllLoans(): Flow<List<LoanEntity>> {
        return loanDao.getAllLoans()
    }

    suspend fun createLoan(
        personName: String,
        phoneNumber: String = "",
        type: String = "LENT",
        amount: Long,
        dueDate: Long = 0L,
        notes: String = ""
    ): Long {
        require(personName.isNotBlank()) { "ব্যক্তির নাম অবশ্যই দিতে হবে" }
        require(amount > 0L) { "টাকার পরিমাণ ০ এর চেয়ে বেশি হতে হবে" }
        val loan = LoanEntity(
            personName = personName.trim(),
            phoneNumber = phoneNumber.trim(),
            type = type,
            amount = amount,
            paidAmount = 0L,
            dateTaken = System.currentTimeMillis(),
            dueDate = dueDate,
            notes = notes.trim(),
            isSettled = false
        )
        return loanDao.insert(loan)
    }

    suspend fun recordLoanPayment(loanId: Long, paymentAmount: Long) {
        require(paymentAmount > 0L) { "পরিশোধের পরিমাণ ০ এর চেয়ে বেশি হতে হবে" }
        val loan = loanDao.getLoanById(loanId) ?: return
        val newPaid = loan.paidAmount + paymentAmount
        val settled = newPaid >= loan.amount
        val updated = loan.copy(
            paidAmount = newPaid,
            isSettled = settled
        )
        loanDao.update(updated)
    }

    suspend fun settleLoan(loanId: Long, isSettled: Boolean) {
        val loan = loanDao.getLoanById(loanId) ?: return
        val updated = if (isSettled) {
            loan.copy(isSettled = true, paidAmount = loan.amount)
        } else {
            loan.copy(isSettled = false)
        }
        loanDao.update(updated)
    }

    suspend fun updateLoan(loan: LoanEntity) {
        loanDao.update(loan)
    }

    suspend fun deleteLoan(loan: LoanEntity) {
        loanDao.delete(loan)
    }

    suspend fun deleteLoanById(id: Long) {
        loanDao.deleteById(id)
    }

    suspend fun exportBackupJson(): String {
        val accounts = accountDao.getAllAccountsSync()
        val budgets = monthlyBudgetDao.getAllBudgetsSync()
        val transactions = transactionDao.getAllTransactionsSync()
        val loans = loanDao.getAllLoansSync()
        val payload = BackupPayload(
            version = 1,
            appName = "Amar Hisab",
            exportedAt = System.currentTimeMillis(),
            accounts = accounts,
            budgets = budgets,
            transactions = transactions,
            loans = loans
        )
        return backupAdapter.indent("  ").toJson(payload)
    }

    suspend fun restoreBackupJson(jsonString: String): Result<Int> {
        return try {
            val payload = backupAdapter.fromJson(jsonString)
                ?: return Result.failure(IllegalArgumentException("ফাইলের ফরম্যাট সঠিক নয়"))

            if (payload.version <= 0 || payload.appName != "Amar Hisab") {
                return Result.failure(IllegalArgumentException("এটি 'আমার হিসাব' ব্যাকআপ ফাইল নয়"))
            }

            // Restore accounts
            for (acc in payload.accounts) {
                val existing = accountDao.getAccountById(acc.id)
                if (existing == null) {
                    accountDao.insert(acc)
                }
            }

            // Validate transactions
            val validTransactions = payload.transactions.filter {
                it.amount > 0L && it.description.isNotBlank() && it.timestamp > 0L
            }

            val existingTransactions = transactionDao.getAllTransactionsSync()
            val existingSet = existingTransactions.map {
                "${it.timestamp}_${it.description}_${it.amount}_${it.category}"
            }.toSet()

            val newTransactionsToInsert = mutableListOf<TransactionEntity>()
            for (tx in validTransactions) {
                val key = "${tx.timestamp}_${tx.description}_${tx.amount}_${tx.category}"
                if (!existingSet.contains(key)) {
                    val ym = DateUtils.getYearMonthFromTimestamp(tx.timestamp)
                    newTransactionsToInsert.add(tx.copy(id = 0L, yearMonth = ym))
                }
            }

            if (newTransactionsToInsert.isNotEmpty()) {
                transactionDao.insertAll(newTransactionsToInsert)
            }

            // Restore monthly budgets
            for (b in payload.budgets) {
                val existingBudget = monthlyBudgetDao.getBudgetSync(b.yearMonth)
                if (existingBudget == null) {
                    monthlyBudgetDao.insertOrUpdate(b)
                }
            }

            // Restore loans
            if (payload.loans.isNotEmpty()) {
                val existingLoans = loanDao.getAllLoansSync()
                val existingLoanKeys = existingLoans.map { "${it.personName}_${it.amount}_${it.dateTaken}" }.toSet()
                val newLoansToInsert = payload.loans.filter {
                    !existingLoanKeys.contains("${it.personName}_${it.amount}_${it.dateTaken}")
                }.map { it.copy(id = 0L) }
                if (newLoansToInsert.isNotEmpty()) {
                    loanDao.insertAll(newLoansToInsert)
                }
            }

            Result.success(newTransactionsToInsert.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearAllData() {
        transactionDao.deleteAll()
        monthlyBudgetDao.deleteAll()
        loanDao.deleteAll()
        accountDao.deleteAll()
        ensureDefaultAccount()
    }
}
