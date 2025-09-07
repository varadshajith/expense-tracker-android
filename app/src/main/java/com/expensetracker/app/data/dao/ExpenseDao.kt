package com.expensetracker.app.data.dao

import androidx.room.*
import com.expensetracker.app.data.model.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    
    // ========== READ OPERATIONS ==========
    
    /**
     * Get all expenses ordered by date (newest first)
     */
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>
    
    /**
     * Get expense by ID
     */
    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): Expense?
    
    /**
     * Get expenses by status (pending/complete)
     */
    @Query("SELECT * FROM expenses WHERE status = :status ORDER BY date DESC")
    fun getExpensesByStatus(status: String): Flow<List<Expense>>
    
    /**
     * Get pending expenses (need more details)
     */
    @Query("SELECT * FROM expenses WHERE status = 'pending' ORDER BY date DESC")
    fun getPendingExpenses(): Flow<List<Expense>>
    
    /**
     * Get complete expenses (have all details)
     */
    @Query("SELECT * FROM expenses WHERE status = 'complete' ORDER BY date DESC")
    fun getCompleteExpenses(): Flow<List<Expense>>
    
    /**
     * Get expenses by category
     */
    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY date DESC")
    fun getExpensesByCategory(category: String): Flow<List<Expense>>
    
    /**
     * Get expenses by merchant
     */
    @Query("SELECT * FROM expenses WHERE merchant LIKE '%' || :merchant || '%' ORDER BY date DESC")
    fun getExpensesByMerchant(merchant: String): Flow<List<Expense>>
    
    /**
     * Get expenses within date range
     */
    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>>
    
    /**
     * Get expenses for a specific month/year
     */
    @Query("""
        SELECT * FROM expenses 
        WHERE strftime('%Y-%m', datetime(date/1000, 'unixepoch')) = :monthYear 
        ORDER BY date DESC
    """)
    fun getExpensesByMonth(monthYear: String): Flow<List<Expense>>
    
    /**
     * Search expenses by description or merchant
     */
    @Query("""
        SELECT * FROM expenses 
        WHERE description LIKE '%' || :query || '%' 
        OR merchant LIKE '%' || :query || '%'
        ORDER BY date DESC
    """)
    fun searchExpenses(query: String): Flow<List<Expense>>
    
    // ========== AGGREGATION QUERIES ==========
    
    /**
     * Get total amount spent
     */
    @Query("SELECT SUM(amount) FROM expenses")
    suspend fun getTotalAmount(): Double?
    
    /**
     * Get total amount by status
     */
    @Query("SELECT SUM(amount) FROM expenses WHERE status = :status")
    suspend fun getTotalAmountByStatus(status: String): Double?
    
    /**
     * Get total amount by category
     */
    @Query("SELECT SUM(amount) FROM expenses WHERE category = :category")
    suspend fun getTotalAmountByCategory(category: String): Double?
    
    /**
     * Get expense count
     */
    @Query("SELECT COUNT(*) FROM expenses")
    suspend fun getExpenseCount(): Int
    
    /**
     * Get expense count by status
     */
    @Query("SELECT COUNT(*) FROM expenses WHERE status = :status")
    suspend fun getExpenseCountByStatus(status: String): Int
    
    // ========== WRITE OPERATIONS ==========
    
    /**
     * Insert a new expense
     */
    @Insert
    suspend fun insertExpense(expense: Expense): Long
    
    /**
     * Insert multiple expenses
     */
    @Insert
    suspend fun insertExpenses(expenses: List<Expense>): List<Long>
    
    /**
     * Update an existing expense
     */
    @Update
    suspend fun updateExpense(expense: Expense)
    
    /**
     * Update expense status
     */
    @Query("UPDATE expenses SET status = :status WHERE id = :id")
    suspend fun updateExpenseStatus(id: Long, status: String)
    
    /**
     * Update expense details (description and category)
     */
    @Query("UPDATE expenses SET description = :description, category = :category, status = :status WHERE id = :id")
    suspend fun updateExpenseDetails(id: Long, description: String?, category: String?, status: String)
    
    /**
     * Delete an expense
     */
    @Delete
    suspend fun deleteExpense(expense: Expense)
    
    /**
     * Delete expense by ID
     */
    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)
    
    /**
     * Delete all expenses
     */
    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()
    
    /**
     * Delete expenses by status
     */
    @Query("DELETE FROM expenses WHERE status = :status")
    suspend fun deleteExpensesByStatus(status: String)
    
    // ========== UTILITY QUERIES ==========
    
    /**
     * Get all unique categories
     */
    @Query("SELECT DISTINCT category FROM expenses WHERE category IS NOT NULL AND category != '' ORDER BY category")
    suspend fun getAllCategories(): List<String>
    
    /**
     * Get all unique merchants
     */
    @Query("SELECT DISTINCT merchant FROM expenses ORDER BY merchant")
    suspend fun getAllMerchants(): List<String>
    
    /**
     * Get recent expenses (last N days)
     */
    @Query("SELECT * FROM expenses WHERE date >= :daysAgo ORDER BY date DESC")
    fun getRecentExpenses(daysAgo: Long): Flow<List<Expense>>
}
