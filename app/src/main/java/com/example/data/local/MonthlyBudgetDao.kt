package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.MonthlyBudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyBudgetDao {

    @Query("SELECT * FROM monthly_budgets WHERE yearMonth = :yearMonth LIMIT 1")
    fun getBudgetForMonth(yearMonth: String): Flow<MonthlyBudgetEntity?>

    @Query("SELECT * FROM monthly_budgets WHERE yearMonth = :yearMonth LIMIT 1")
    suspend fun getBudgetSync(yearMonth: String): MonthlyBudgetEntity?

    @Query("SELECT * FROM monthly_budgets ORDER BY yearMonth DESC")
    fun getAllBudgets(): Flow<List<MonthlyBudgetEntity>>

    @Query("SELECT * FROM monthly_budgets ORDER BY yearMonth DESC")
    suspend fun getAllBudgetsSync(): List<MonthlyBudgetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(budget: MonthlyBudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(budgets: List<MonthlyBudgetEntity>)

    @Query("DELETE FROM monthly_budgets")
    suspend fun deleteAll()
}
