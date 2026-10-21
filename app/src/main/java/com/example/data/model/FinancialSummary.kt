package com.example.data.model

data class FinancialSummary(
    val yearMonth: String,
    val startingBalance: Long,
    val targetBudget: Long,
    val totalExpenses: Long,
    val totalRefunds: Long,
    val actualRemainingBalance: Long,
    val budgetRemaining: Long,
    val budgetUsagePercent: Int,
    val transactionCount: Int,
    val isOverBudget: Boolean,
    val isNegativeBalance: Boolean
) {
    companion object {
        fun calculate(
            yearMonth: String,
            startingBalance: Long,
            targetBudget: Long,
            transactions: List<TransactionEntity>
        ): FinancialSummary {
            var expenses = 0L
            var refunds = 0L

            for (tx in transactions) {
                if (tx.type == TYPE_REFUND) {
                    refunds += tx.amount
                } else {
                    expenses += tx.amount
                }
            }

            val actualRemaining = startingBalance - expenses + refunds
            val budgetRemaining = targetBudget - expenses
            val usagePercent = if (targetBudget > 0L) {
                ((expenses.toDouble() / targetBudget.toDouble()) * 100).toInt()
            } else {
                0
            }

            return FinancialSummary(
                yearMonth = yearMonth,
                startingBalance = startingBalance,
                targetBudget = targetBudget,
                totalExpenses = expenses,
                totalRefunds = refunds,
                actualRemainingBalance = actualRemaining,
                budgetRemaining = budgetRemaining,
                budgetUsagePercent = usagePercent,
                transactionCount = transactions.size,
                isOverBudget = targetBudget > 0L && expenses > targetBudget,
                isNegativeBalance = actualRemaining < 0L
            )
        }
    }
}
