package com.expensetracker.app.utils

import org.junit.Assert.*
import org.junit.Test

class SmsParserTest {

    @Test
    fun `parseUPITransaction - standard debit message with multi-word merchant`() {
        val message = "UPI: ₹150 debited from A/c **1234 to Cafe Coffee Day. UPI Ref: 123456789012"
        val result = SmsParser.parseUPITransaction(message)
        assertNotNull(result)
        assertEquals(150.0, result!!.amount, 0.0)
        assertEquals("Cafe Coffee Day", result.merchant)
    }

    @Test
    fun `parseUPITransaction - paid to message with 'via'`() {
        val message = "Rs.250 paid to Uber via PhonePe. Transaction ID: 987654321098"
        val result = SmsParser.parseUPITransaction(message)
        assertNotNull(result)
        assertEquals(250.0, result!!.amount, 0.0)
        assertEquals("Uber", result.merchant)
    }

    @Test
    fun `parseUPITransaction - spent at message with multi-word merchant`() {
        val message = "INR 1200 spent at Big Bazaar. UPI Ref: 112233445566"
        val result = SmsParser.parseUPITransaction(message)
        assertNotNull(result)
        assertEquals(1200.0, result!!.amount, 0.0)
        assertEquals("Big Bazaar", result.merchant)
    }

    @Test
    fun `parseUPITransaction - merchant at end of sentence`() {
        val message = "₹500 debited from your account to Netflix. Transaction successful."
        val result = SmsParser.parseUPITransaction(message)
        assertNotNull(result)
        assertEquals(500.0, result!!.amount, 0.0)
        assertEquals("Netflix", result.merchant)
    }

    @Test
    fun `parseUPITransaction - GPay message with multi-word merchant`() {
        val message = "Rs.75 paid to Local Store via GPay. Ref: 556677889900"
        val result = SmsParser.parseUPITransaction(message)
        assertNotNull(result)
        assertEquals(75.0, result!!.amount, 0.0)
        assertEquals("Local Store", result.merchant)
    }

    @Test
    fun `parseUPITransaction - non-transactional message`() {
        val message = "Your account balance is low."
        val result = SmsParser.parseUPITransaction(message)
        assertNull(result)
    }

    @Test
    fun `parseUPITransaction - message with no amount`() {
        val message = "Transaction to Amazon failed."
        val result = SmsParser.parseUPITransaction(message)
        assertNull(result)
    }

    @Test
    fun `parseUPITransaction - message with no merchant`() {
        val message = "You have spent ₹500."
        val result = SmsParser.parseUPITransaction(message)
        assertNotNull(result)
        assertEquals(500.0, result!!.amount, 0.0)
        assertEquals("Unknown Merchant", result.merchant)
    }

    @Test
    fun `parseUPITransaction - another multi-word merchant example`() {
        val message = "You have spent Rs. 350.50 at The Corner Bistro."
        val result = SmsParser.parseUPITransaction(message)
        assertNotNull(result)
        assertEquals(350.50, result!!.amount, 0.0)
        assertEquals("The Corner Bistro", result.merchant)
    }
}
