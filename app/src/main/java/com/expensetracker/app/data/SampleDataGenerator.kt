package com.expensetracker.app.data

import com.expensetracker.app.data.model.Expense
import com.expensetracker.app.data.repository.ExpenseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

object SampleDataGenerator {
    
    /**
     * Generate sample expenses for testing and demonstration
     */
    fun generateSampleExpenses(): List<Expense> {
        val calendar = Calendar.getInstance()
        val currentTime = System.currentTimeMillis()
        
        return listOf(
            // Complete expenses
            Expense(
                amount = 150.0,
                merchant = "Cafe Coffee Day",
                description = "Morning coffee and sandwich",
                category = Expense.CATEGORY_FOOD,
                status = Expense.STATUS_COMPLETE,
                date = currentTime - (24 * 60 * 60 * 1000) // 1 day ago
            ),
            Expense(
                amount = 250.0,
                merchant = "Uber",
                description = "Ride to office",
                category = Expense.CATEGORY_TRANSPORT,
                status = Expense.STATUS_COMPLETE,
                date = currentTime - (2 * 24 * 60 * 60 * 1000) // 2 days ago
            ),
            Expense(
                amount = 1200.0,
                merchant = "Big Bazaar",
                description = "Weekly grocery shopping",
                category = Expense.CATEGORY_SHOPPING,
                status = Expense.STATUS_COMPLETE,
                date = currentTime - (3 * 24 * 60 * 60 * 1000) // 3 days ago
            ),
            Expense(
                amount = 500.0,
                merchant = "Netflix",
                description = "Monthly subscription",
                category = Expense.CATEGORY_ENTERTAINMENT,
                status = Expense.STATUS_COMPLETE,
                date = currentTime - (5 * 24 * 60 * 60 * 1000) // 5 days ago
            ),
            Expense(
                amount = 800.0,
                merchant = "Apollo Pharmacy",
                description = "Medicine for cold",
                category = Expense.CATEGORY_HEALTHCARE,
                status = Expense.STATUS_COMPLETE,
                date = currentTime - (7 * 24 * 60 * 60 * 1000) // 1 week ago
            ),
            
            // Pending expenses (from UPI transactions)
            Expense(
                amount = 75.0,
                merchant = "PhonePe - Local Store",
                status = Expense.STATUS_PENDING,
                date = currentTime - (30 * 60 * 1000) // 30 minutes ago
            ),
            Expense(
                amount = 300.0,
                merchant = "GPay - Restaurant",
                status = Expense.STATUS_PENDING,
                date = currentTime - (2 * 60 * 60 * 1000) // 2 hours ago
            ),
            Expense(
                amount = 120.0,
                merchant = "Paytm - Metro",
                status = Expense.STATUS_PENDING,
                date = currentTime - (4 * 60 * 60 * 1000) // 4 hours ago
            ),
            Expense(
                amount = 450.0,
                merchant = "BHIM - Gas Station",
                status = Expense.STATUS_PENDING,
                date = currentTime - (6 * 60 * 60 * 1000) // 6 hours ago
            ),
            Expense(
                amount = 200.0,
                merchant = "PhonePe - Coffee Shop",
                status = Expense.STATUS_PENDING,
                date = currentTime - (8 * 60 * 60 * 1000) // 8 hours ago
            )
        )
    }
    
    /**
     * Insert sample data into the repository
     */
    fun insertSampleData(repository: ExpenseRepository) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sampleExpenses = generateSampleExpenses()
                repository.insertExpenses(sampleExpenses)
                println("Sample data inserted successfully: ${sampleExpenses.size} expenses")
            } catch (e: Exception) {
                println("Error inserting sample data: ${e.message}")
            }
        }
    }
    
    /**
     * Create a single sample expense for testing
     */
    fun createSampleExpense(
        amount: Double = 100.0,
        merchant: String = "Sample Merchant",
        description: String? = null,
        category: String? = null,
        status: String = Expense.STATUS_PENDING
    ): Expense {
        return Expense(
            amount = amount,
            merchant = merchant,
            description = description,
            category = category,
            status = status
        )
    }
    
    /**
     * Create a UPI transaction expense (pending)
     */
    fun createUPIExpense(amount: Double, merchant: String): Expense {
        return Expense(
            amount = amount,
            merchant = merchant,
            status = Expense.STATUS_PENDING
        )
    }
    
    /**
     * Create a complete expense with all details
     */
    fun createCompleteExpense(
        amount: Double,
        merchant: String,
        description: String,
        category: String
    ): Expense {
        return Expense(
            amount = amount,
            merchant = merchant,
            description = description,
            category = category,
            status = Expense.STATUS_COMPLETE
        )
    }
}
