package com.expensetracker.app.notification

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.expensetracker.app.MainActivity
import com.expensetracker.app.utils.SmsParser
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class NotificationFlowTest {
    
    private lateinit var context: Context
    private lateinit var notificationManager: ExpenseNotificationManager
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        notificationManager = ExpenseNotificationManager(context)
    }
    
    @Test
    fun `notification channels should be created correctly`() {
        // Test that notification channels are properly configured
        val alertsImportance = notificationManager.getChannelImportance("expense_alerts")
        val remindersImportance = notificationManager.getChannelImportance("expense_reminders")
        
        // Expense alerts should be high importance
        assertTrue("Expense alerts channel should be high importance", alertsImportance >= 4)
        
        // Expense reminders should be default importance
        assertTrue("Expense reminders channel should be default importance", remindersImportance >= 3)
    }
    
    @Test
    fun `notification permissions should be checkable`() {
        // Test that we can check notification permissions
        val areEnabled = notificationManager.areNotificationsEnabled()
        
        // This test verifies the method doesn't crash
        // Actual permission state depends on test environment
        assertNotNull("Notification permission check should not return null", areEnabled)
    }
    
    @Test
    fun `notification manager should handle null transaction details gracefully`() {
        // Test error handling for null transaction details
        try {
            // This should not crash even with invalid data
            val transactionDetails = SmsParser.parseUPITransaction("")
            assertNull("Empty SMS should return null transaction details", transactionDetails)
        } catch (e: Exception) {
            fail("Notification manager should handle null transaction details gracefully: ${e.message}")
        }
    }
    
    @Test
    fun `notification manager should handle valid transaction details`() {
        // Test with valid transaction details
        val sampleSMS = "UPI: ₹150 debited from A/c **1234 to Cafe Coffee Day. UPI Ref: 123456789012"
        val transactionDetails = SmsParser.parseUPITransaction(sampleSMS)
        
        assertNotNull("Valid SMS should return transaction details", transactionDetails)
        assertEquals("Amount should be parsed correctly", 150.0, transactionDetails!!.amount, 0.01)
        assertTrue("Merchant should be parsed", transactionDetails.merchant.isNotBlank())
    }
}
