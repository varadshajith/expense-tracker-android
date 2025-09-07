package com.expensetracker.app.notification

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.expensetracker.app.MainActivity
import com.expensetracker.app.data.database.AppDatabase
import com.expensetracker.app.data.repository.ExpenseRepository
import com.expensetracker.app.utils.SmsParser
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class NotificationIntegrationTest {
    
    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java, false, false)
    
    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var repository: ExpenseRepository
    private lateinit var notificationManager: ExpenseNotificationManager
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = AppDatabase.getTestDatabase(context)
        repository = ExpenseRepository(database.expenseDao())
        notificationManager = ExpenseNotificationManager(context)
    }
    
    @After
    fun cleanup() {
        database.close()
    }
    
    @Test
    fun `end-to-end notification flow should work correctly`() = runTest {
        // Step 1: Simulate UPI SMS parsing
        val sampleSMS = "UPI: ₹250 debited from A/c **1234 to Uber. UPI Ref: 123456789012"
        val transactionDetails = SmsParser.parseUPITransaction(sampleSMS)
        
        assertNotNull("Transaction details should be parsed", transactionDetails)
        assertEquals("Amount should be correct", 250.0, transactionDetails!!.amount, 0.01)
        assertEquals("Merchant should be correct", "Uber", transactionDetails.merchant)
        
        // Step 2: Create expense in database
        val expenseId = repository.createExpenseFromUPI(
            transactionDetails.amount,
            transactionDetails.merchant
        )
        
        assertTrue("Expense should be created with valid ID", expenseId > 0)
        
        // Step 3: Verify expense exists in database
        val createdExpense = repository.getExpenseById(expenseId)
        assertNotNull("Created expense should exist", createdExpense)
        assertEquals("Expense amount should match", transactionDetails.amount, createdExpense!!.amount, 0.01)
        assertEquals("Expense merchant should match", transactionDetails.merchant, createdExpense.merchant)
        assertEquals("Expense should be pending", "pending", createdExpense.status)
        
        // Step 4: Test notification creation (without actually showing it)
        try {
            notificationManager.showNewTransactionNotification(transactionDetails, expenseId)
            // If no exception is thrown, notification creation succeeded
        } catch (e: Exception) {
            fail("Notification creation should not fail: ${e.message}")
        }
        
        // Step 5: Test intent creation for notification click
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("action", "edit_expense")
            putExtra("expense_id", expenseId)
        }
        
        assertEquals("Intent action should be correct", "edit_expense", intent.getStringExtra("action"))
        assertEquals("Intent expense ID should be correct", expenseId, intent.getLongExtra("expense_id", -1))
    }
    
    @Test
    fun `notification click should navigate to correct expense`() = runTest {
        // Create a test expense
        val expenseId = repository.createExpenseFromUPI(100.0, "Test Merchant")
        
        // Create intent as if from notification click
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("action", "edit_expense")
            putExtra("expense_id", expenseId)
        }
        
        // Launch activity with the intent
        val activity = activityRule.launchActivity(intent)
        
        // Verify activity was launched
        assertNotNull("Activity should be launched", activity)
        
        // Verify intent extras are preserved
        assertEquals("Intent action should be preserved", "edit_expense", activity.intent.getStringExtra("action"))
        assertEquals("Intent expense ID should be preserved", expenseId, activity.intent.getLongExtra("expense_id", -1))
    }
    
    @Test
    fun `multiple notification types should work correctly`() = runTest {
        // Test new transaction notification
        val transactionDetails = SmsParser.UPITransactionDetails(150.0, "Coffee Shop")
        val expenseId = repository.createExpenseFromUPI(150.0, "Coffee Shop")
        
        try {
            notificationManager.showNewTransactionNotification(transactionDetails, expenseId)
        } catch (e: Exception) {
            fail("New transaction notification should not fail: ${e.message}")
        }
        
        // Test pending expense reminder
        try {
            notificationManager.showPendingExpenseReminder(1)
        } catch (e: Exception) {
            fail("Pending expense reminder should not fail: ${e.message}")
        }
        
        // Test expense completion notification
        try {
            notificationManager.showExpenseCompletedNotification("Coffee Shop", 150.0)
        } catch (e: Exception) {
            fail("Expense completion notification should not fail: ${e.message}")
        }
    }
    
    @Test
    fun `notification cancellation should work correctly`() {
        // Test that we can cancel notifications without crashing
        try {
            notificationManager.cancelAllNotifications()
            notificationManager.cancelNotification(1001)
        } catch (e: Exception) {
            fail("Notification cancellation should not fail: ${e.message}")
        }
    }
}
