package com.expensetracker.app.notification

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.expensetracker.app.MainActivity
import com.expensetracker.app.utils.NotificationTestUtils
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class EndToEndNotificationTest {
    
    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java, false, false)
    
    @Test
    fun `complete notification flow should work for all sample UPI messages`() = runTest {
        val sampleMessages = NotificationTestUtils.getSampleUPIMessages()
        
        for (sampleMessage in sampleMessages) {
            val result = NotificationTestUtils.simulateCompleteNotificationFlow(
                sampleMessage.sms,
                sampleMessage.expectedAmount,
                sampleMessage.expectedMerchant
            )
            
            assertTrue(
                "Notification flow should succeed for SMS: ${sampleMessage.sms}. Error: ${result.error}",
                result.success
            )
            
            assertEquals(
                "Amount should match for SMS: ${sampleMessage.sms}",
                sampleMessage.expectedAmount,
                result.amount,
                0.01
            )
            
            assertEquals(
                "Merchant should match for SMS: ${sampleMessage.sms}",
                sampleMessage.expectedMerchant,
                result.merchant
            )
            
            assertNotNull(
                "Expense ID should be generated for SMS: ${sampleMessage.sms}",
                result.expenseId
            )
            
            assertTrue(
                "Expense ID should be positive for SMS: ${sampleMessage.sms}",
                result.expenseId!! > 0
            )
            
            // Verify notification intent
            if (result.intent != null) {
                assertTrue(
                    "Notification intent should be valid for SMS: ${sampleMessage.sms}",
                    NotificationTestUtils.verifyNotificationIntent(result.intent, result.expenseId!!)
                )
            }
        }
    }
    
    @Test
    fun `invalid SMS messages should not trigger notifications`() = runTest {
        val invalidMessages = NotificationTestUtils.getInvalidSMSMessages()
        
        for (invalidMessage in invalidMessages) {
            val result = NotificationTestUtils.simulateCompleteNotificationFlow(
                invalidMessage,
                0.0,
                ""
            )
            
            assertFalse(
                "Invalid SMS should not trigger notification: $invalidMessage",
                result.success
            )
            
            assertNotNull(
                "Error should be reported for invalid SMS: $invalidMessage",
                result.error
            )
        }
    }
    
    @Test
    fun `notification click should navigate to correct expense`() = runTest {
        // Test with a specific UPI message
        val testSMS = "UPI: ₹150 debited from A/c **1234 to Cafe Coffee Day. UPI Ref: 123456789012"
        val result = NotificationTestUtils.simulateCompleteNotificationFlow(
            testSMS,
            150.0,
            "Cafe Coffee Day"
        )
        
        assertTrue("Test SMS should be processed successfully", result.success)
        assertNotNull("Expense ID should be generated", result.expenseId)
        
        // Create intent as if from notification click
        val intent = NotificationTestUtils.createEditExpenseIntent(
            activityRule.activity.applicationContext,
            result.expenseId!!
        )
        
        // Launch activity with the intent
        val activity = activityRule.launchActivity(intent)
        
        // Verify activity was launched
        assertNotNull("Activity should be launched", activity)
        
        // Verify intent extras are preserved
        assertEquals(
            "Intent action should be preserved",
            "edit_expense",
            activity.intent.getStringExtra("action")
        )
        
        assertEquals(
            "Intent expense ID should be preserved",
            result.expenseId,
            activity.intent.getLongExtra("expense_id", -1)
        )
    }
    
    @Test
    fun `multiple notifications should work independently`() = runTest {
        val testMessages = listOf(
            NotificationTestUtils.SampleUPIMessage(
                "UPI: ₹100 debited to Shop1. UPI Ref: 111111111111",
                100.0,
                "Shop1"
            ),
            NotificationTestUtils.SampleUPIMessage(
                "UPI: ₹200 debited to Shop2. UPI Ref: 222222222222",
                200.0,
                "Shop2"
            ),
            NotificationTestUtils.SampleUPIMessage(
                "UPI: ₹300 debited to Shop3. UPI Ref: 333333333333",
                300.0,
                "Shop3"
            )
        )
        
        val results = mutableListOf<NotificationTestUtils.NotificationFlowResult>()
        
        for (testMessage in testMessages) {
            val result = NotificationTestUtils.simulateCompleteNotificationFlow(
                testMessage.sms,
                testMessage.expectedAmount,
                testMessage.expectedMerchant
            )
            
            results.add(result)
            
            assertTrue(
                "Notification flow should succeed for: ${testMessage.sms}",
                result.success
            )
        }
        
        // Verify all expenses have unique IDs
        val expenseIds = results.mapNotNull { it.expenseId }
        assertEquals(
            "All expenses should have unique IDs",
            expenseIds.size,
            expenseIds.distinct().size
        )
        
        // Verify all amounts are correct
        val expectedAmounts = testMessages.map { it.expectedAmount }
        val actualAmounts = results.map { it.amount }
        assertEquals(
            "All amounts should match expected values",
            expectedAmounts,
            actualAmounts
        )
        
        // Verify all merchants are correct
        val expectedMerchants = testMessages.map { it.expectedMerchant }
        val actualMerchants = results.map { it.merchant }
        assertEquals(
            "All merchants should match expected values",
            expectedMerchants,
            actualMerchants
        )
    }
    
    @Test
    fun `notification flow should handle edge cases gracefully`() = runTest {
        val edgeCases = listOf(
            // Very large amount
            NotificationTestUtils.SampleUPIMessage(
                "UPI: ₹999999.99 debited to Large Merchant. UPI Ref: 999999999999",
                999999.99,
                "Large Merchant"
            ),
            // Small amount
            NotificationTestUtils.SampleUPIMessage(
                "UPI: ₹0.01 debited to Small Merchant. UPI Ref: 000000000001",
                0.01,
                "Small Merchant"
            ),
            // Merchant with special characters
            NotificationTestUtils.SampleUPIMessage(
                "UPI: ₹100 debited to Café & Restaurant. UPI Ref: 444444444444",
                100.0,
                "Café & Restaurant"
            ),
            // Merchant with numbers
            NotificationTestUtils.SampleUPIMessage(
                "UPI: ₹100 debited to Store123. UPI Ref: 555555555555",
                100.0,
                "Store123"
            )
        )
        
        for (edgeCase in edgeCases) {
            val result = NotificationTestUtils.simulateCompleteNotificationFlow(
                edgeCase.sms,
                edgeCase.expectedAmount,
                edgeCase.expectedMerchant
            )
            
            assertTrue(
                "Edge case should be handled successfully: ${edgeCase.sms}",
                result.success
            )
            
            assertEquals(
                "Amount should be parsed correctly for edge case: ${edgeCase.sms}",
                edgeCase.expectedAmount,
                result.amount,
                0.01
            )
            
            assertEquals(
                "Merchant should be parsed correctly for edge case: ${edgeCase.sms}",
                edgeCase.expectedMerchant,
                result.merchant
            )
        }
    }
}
