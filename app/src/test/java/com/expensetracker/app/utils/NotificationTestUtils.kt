package com.expensetracker.app.utils

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.expensetracker.app.MainActivity
import com.expensetracker.app.data.database.AppDatabase
import com.expensetracker.app.data.repository.ExpenseRepository
import com.expensetracker.app.notification.ExpenseNotificationManager
import kotlinx.coroutines.test.runTest

object NotificationTestUtils {
    
    /**
     * Simulate the complete notification flow from SMS to EditExpense screen
     */
    suspend fun simulateCompleteNotificationFlow(
        smsMessage: String,
        expectedAmount: Double,
        expectedMerchant: String
    ): NotificationFlowResult {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = AppDatabase.getTestDatabase(context)
        val repository = ExpenseRepository(database.expenseDao())
        val notificationManager = ExpenseNotificationManager(context)
        
        return try {
            // Step 1: Parse SMS
            val transactionDetails = SmsParser.parseUPITransaction(smsMessage)
            
            if (transactionDetails == null) {
                return NotificationFlowResult(
                    success = false,
                    error = "Failed to parse SMS: $smsMessage",
                    expenseId = null,
                    amount = 0.0,
                    merchant = ""
                )
            }
            
            // Step 2: Create expense
            val expenseId = repository.createExpenseFromUPI(
                transactionDetails.amount,
                transactionDetails.merchant
            )
            
            // Step 3: Verify expense creation
            val createdExpense = repository.getExpenseById(expenseId)
            if (createdExpense == null) {
                return NotificationFlowResult(
                    success = false,
                    error = "Failed to create expense in database",
                    expenseId = null,
                    amount = 0.0,
                    merchant = ""
                )
            }
            
            // Step 4: Create notification (without showing it)
            notificationManager.showNewTransactionNotification(transactionDetails, expenseId)
            
            // Step 5: Create intent for notification click
            val intent = createEditExpenseIntent(context, expenseId)
            
            // Step 6: Verify results
            val success = transactionDetails.amount == expectedAmount &&
                         transactionDetails.merchant == expectedMerchant &&
                         createdExpense.amount == expectedAmount &&
                         createdExpense.merchant == expectedMerchant &&
                         createdExpense.status == "pending"
            
            NotificationFlowResult(
                success = success,
                error = if (success) null else "Validation failed",
                expenseId = expenseId,
                amount = transactionDetails.amount,
                merchant = transactionDetails.merchant,
                intent = intent
            )
            
        } catch (e: Exception) {
            NotificationFlowResult(
                success = false,
                error = "Exception occurred: ${e.message}",
                expenseId = null,
                amount = 0.0,
                merchant = ""
            )
        } finally {
            database.close()
        }
    }
    
    /**
     * Create intent for EditExpense screen navigation
     */
    fun createEditExpenseIntent(context: Context, expenseId: Long): Intent {
        return Intent(context, MainActivity::class.java).apply {
            putExtra("action", "edit_expense")
            putExtra("expense_id", expenseId)
        }
    }
    
    /**
     * Create intent for MainActivity navigation
     */
    fun createMainActivityIntent(context: Context): Intent {
        return Intent(context, MainActivity::class.java).apply {
            putExtra("action", "view_expenses")
        }
    }
    
    /**
     * Get sample UPI SMS messages for testing
     */
    fun getSampleUPIMessages(): List<SampleUPIMessage> {
        return listOf(
            SampleUPIMessage(
                sms = "UPI: ₹150 debited from A/c **1234 to Cafe Coffee Day. UPI Ref: 123456789012",
                expectedAmount = 150.0,
                expectedMerchant = "Cafe Coffee Day"
            ),
            SampleUPIMessage(
                sms = "Rs.250 paid to Uber via PhonePe. Transaction ID: 987654321098",
                expectedAmount = 250.0,
                expectedMerchant = "Uber"
            ),
            SampleUPIMessage(
                sms = "INR 1200 spent at Big Bazaar. UPI Ref: 112233445566",
                expectedAmount = 1200.0,
                expectedMerchant = "Big Bazaar"
            ),
            SampleUPIMessage(
                sms = "₹500 debited from your account to Netflix. Transaction successful.",
                expectedAmount = 500.0,
                expectedMerchant = "Netflix"
            ),
            SampleUPIMessage(
                sms = "HDFC: ₹75 paid to Local Store. UPI Ref: 556677889900",
                expectedAmount = 75.0,
                expectedMerchant = "Local Store"
            ),
            SampleUPIMessage(
                sms = "Paytm: ₹120 spent at Metro Station. Transaction ID: 334455667788",
                expectedAmount = 120.0,
                expectedMerchant = "Metro Station"
            ),
            SampleUPIMessage(
                sms = "BHIM: ₹450 debited to Gas Station. UPI Ref: 778899001122",
                expectedAmount = 450.0,
                expectedMerchant = "Gas Station"
            ),
            SampleUPIMessage(
                sms = "GPay: ₹200 paid to Coffee Shop. Transaction successful.",
                expectedAmount = 200.0,
                expectedMerchant = "Coffee Shop"
            ),
            SampleUPIMessage(
                sms = "ICICI: ₹800 debited from A/c **5678 to Apollo Pharmacy. UPI Ref: 990011223344",
                expectedAmount = 800.0,
                expectedMerchant = "Apollo Pharmacy"
            ),
            SampleUPIMessage(
                sms = "PhonePe: ₹300 transferred to Restaurant. UPI transaction completed.",
                expectedAmount = 300.0,
                expectedMerchant = "Restaurant"
            )
        )
    }
    
    /**
     * Get invalid SMS messages for testing error handling
     */
    fun getInvalidSMSMessages(): List<String> {
        return listOf(
            "Random text message",
            "UPI: Transaction successful",
            "Hello world",
            "Your OTP is 123456",
            "Bank alert: Your account balance is ₹50000",
            "Weather update: Sunny day ahead",
            "UPI: Payment failed",
            "Transaction cancelled by user"
        )
    }
    
    /**
     * Verify notification intent has correct data
     */
    fun verifyNotificationIntent(intent: Intent, expectedExpenseId: Long): Boolean {
        return intent.getStringExtra("action") == "edit_expense" &&
               intent.getLongExtra("expense_id", -1) == expectedExpenseId
    }
    
    /**
     * Data class for sample UPI messages
     */
    data class SampleUPIMessage(
        val sms: String,
        val expectedAmount: Double,
        val expectedMerchant: String
    )
    
    /**
     * Data class for notification flow test results
     */
    data class NotificationFlowResult(
        val success: Boolean,
        val error: String?,
        val expenseId: Long?,
        val amount: Double,
        val merchant: String,
        val intent: Intent? = null
    )
}
