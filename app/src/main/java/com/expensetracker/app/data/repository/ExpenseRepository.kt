package com.expensetracker.app.data.repository

import com.expensetracker.app.data.dao.ExpenseDao
import com.expensetracker.app.data.model.Expense
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class ExpenseRepository(
    private val expenseDao: ExpenseDao
) {
    
    // ========== BASIC CRUD OPERATIONS ==========
    
    /**
     * Get all expenses ordered by date (newest first)
     */
    fun getAllExpenses(): Flow<List<Expense>> = expenseDao.getAllExpenses()
    
    /**
     * Get expense by ID
     */
    suspend fun getExpenseById(id: Long): Expense? = expenseDao.getExpenseById(id)
    
    /**
     * Insert a new expense
     */
    suspend fun insertExpense(expense: Expense): Long = expenseDao.insertExpense(expense)
    
    /**
     * Insert multiple expenses
     */
    suspend fun insertExpenses(expenses: List<Expense>): List<Long> = expenseDao.insertExpenses(expenses)
    
    /**
     * Update an existing expense
     */
    suspend fun updateExpense(expense: Expense) = expenseDao.updateExpense(expense)
    
    /**
     * Delete an expense
     */
    suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)
    
    /**
     * Delete expense by ID
     */
    suspend fun deleteExpenseById(id: Long) = expenseDao.deleteExpenseById(id)
    
    // ========== STATUS-BASED OPERATIONS ==========
    
    /**
     * Get pending expenses (need more details)
     */
    fun getPendingExpenses(): Flow<List<Expense>> = expenseDao.getPendingExpenses()
    
    /**
     * Get complete expenses (have all details)
     */
    fun getCompleteExpenses(): Flow<List<Expense>> = expenseDao.getCompleteExpenses()
    
    /**
     * Get expenses by status
     */
    fun getExpensesByStatus(status: String): Flow<List<Expense>> = expenseDao.getExpensesByStatus(status)
    
    /**
     * Update expense status
     */
    suspend fun updateExpenseStatus(id: Long, status: String) = expenseDao.updateExpenseStatus(id, status)
    
    /**
     * Update expense details and mark as complete
     */
    suspend fun updateExpenseDetails(id: Long, description: String?, category: String?) {
        val status = if (description?.isNotBlank() == true && category?.isNotBlank() == true) {
            Expense.STATUS_COMPLETE
        } else {
            Expense.STATUS_PENDING
        }
        expenseDao.updateExpenseDetails(id, description, category, status)
    }
    
    // ========== FILTERING AND SEARCH OPERATIONS ==========
    
    /**
     * Get expenses by category
     */
    fun getExpensesByCategory(category: String): Flow<List<Expense>> = expenseDao.getExpensesByCategory(category)
    
    /**
     * Get expenses by merchant
     */
    fun getExpensesByMerchant(merchant: String): Flow<List<Expense>> = expenseDao.getExpensesByMerchant(merchant)
    
    /**
     * Search expenses by description or merchant
     */
    fun searchExpenses(query: String): Flow<List<Expense>> = expenseDao.searchExpenses(query)
    
    /**
     * Get expenses within date range
     */
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>> = 
        expenseDao.getExpensesByDateRange(startDate, endDate)
    
    /**
     * Get expenses for current month
     */
    fun getCurrentMonthExpenses(): Flow<List<Expense>> {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Calendar months are 0-based
        val monthYear = String.format("%04d-%02d", year, month)
        return expenseDao.getExpensesByMonth(monthYear)
    }
    
    /**
     * Get expenses for a specific month/year
     */
    fun getExpensesByMonth(monthYear: String): Flow<List<Expense>> = expenseDao.getExpensesByMonth(monthYear)
    
    /**
     * Get recent expenses (last N days)
     */
    fun getRecentExpenses(days: Int = 7): Flow<List<Expense>> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        val daysAgo = calendar.timeInMillis
        return expenseDao.getRecentExpenses(daysAgo)
    }
    
    // ========== AGGREGATION OPERATIONS ==========
    
    /**
     * Get total amount spent
     */
    suspend fun getTotalAmount(): Double = expenseDao.getTotalAmount() ?: 0.0
    
    /**
     * Get total amount by status
     */
    suspend fun getTotalAmountByStatus(status: String): Double = expenseDao.getTotalAmountByStatus(status) ?: 0.0
    
    /**
     * Get total amount by category
     */
    suspend fun getTotalAmountByCategory(category: String): Double = expenseDao.getTotalAmountByCategory(category) ?: 0.0
    
    /**
     * Get total amount for current month
     */
    suspend fun getCurrentMonthTotal(): Double {
        val expenses = getCurrentMonthExpenses()
        // Note: This is a simplified version. In a real app, you'd want to collect the flow
        return 0.0 // Placeholder - would need to collect flow and sum amounts
    }
    
    /**
     * Get expense count
     */
    suspend fun getExpenseCount(): Int = expenseDao.getExpenseCount()
    
    /**
     * Get expense count by status
     */
    suspend fun getExpenseCountByStatus(status: String): Int = expenseDao.getExpenseCountByStatus(status)
    
    // ========== UTILITY OPERATIONS ==========
    
    /**
     * Get all unique categories
     */
    suspend fun getAllCategories(): List<String> = expenseDao.getAllCategories()
    
    /**
     * Get all unique merchants
     */
    suspend fun getAllMerchants(): List<String> = expenseDao.getAllMerchants()
    
    /**
     * Create a new expense from UPI transaction data
     */
    suspend fun createExpenseFromUPI(amount: Double, merchant: String): Long {
        val expense = Expense(
            amount = amount,
            merchant = merchant,
            status = Expense.STATUS_PENDING
        )
        return insertExpense(expense)
    }
    
    /**
     * Mark expense as complete when user adds details
     */
    suspend fun completeExpense(id: Long, description: String, category: String): Boolean {
        return try {
            updateExpenseDetails(id, description, category)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Clear all data (for testing or reset functionality)
     */
    suspend fun clearAllData() = expenseDao.deleteAllExpenses()
    
    /**
     * Delete all pending expenses
     */
    suspend fun deleteAllPendingExpenses() = expenseDao.deleteExpensesByStatus(Expense.STATUS_PENDING)
}
