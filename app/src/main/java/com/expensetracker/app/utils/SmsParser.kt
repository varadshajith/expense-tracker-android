package com.expensetracker.app.utils

import android.util.Log

object SmsParser {
    
    private const val TAG = "SmsParser"
    
    // UPI Provider sender IDs and keywords
    private val UPI_SENDERS = setOf(
        "VK-UPI", "UPI", "BHIM", "PAYTM", "PHONEPE", "GPay", "GOOGLEPAY",
        "AMAZONPAY", "AMAZON", "CRED", "FREECHARGE", "MOBIKWIK", "JIO",
        "AIRTEL", "VODAFONE", "IDEA", "BSNL", "MTNL", "HDFC", "ICICI",
        "SBI", "AXIS", "KOTAK", "YES", "INDUS", "PNB", "BOI", "CANARA"
    )
    
    private val UPI_KEYWORDS = setOf(
        "debited", "paid", "spent", "transaction", "upi", "payment",
        "rs.", "₹", "inr", "successful", "completed", "transferred",
        "sent", "received", "credit", "debit"
    )
    
    /**
     * Check if the SMS is from a UPI provider and contains transaction details
     */
    fun isUPITransaction(sender: String, messageBody: String): Boolean {
        val senderUpper = sender.uppercase()
        val messageUpper = messageBody.uppercase()
        
        // Check if sender is a known UPI provider
        val isFromUPIProvider = UPI_SENDERS.any { upiSender ->
            senderUpper.contains(upiSender.uppercase())
        }
        
        // Check if message contains amount (₹ or Rs.)
        val hasAmount = messageBody.contains("₹") || 
                       messageBody.contains("Rs.") || 
                       messageBody.contains("INR") ||
                       messageBody.contains("rupees")
        
        val hasUPIKeywords = UPI_KEYWORDS.any { keyword ->
            messageUpper.contains(keyword.uppercase())
        }
        
        return isFromUPIProvider && (hasUPIKeywords || hasAmount)
    }
    
    /**
     * Parse UPI transaction details from SMS message
     */
    fun parseUPITransaction(messageBody: String): UPITransactionDetails? {
        return try {
            val amount = extractAmount(messageBody)
            val merchant = extractMerchant(messageBody)
            
            if (amount > 0 && merchant.isNotBlank()) {
                UPITransactionDetails(amount, merchant)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing UPI transaction", e)
            null
        }
    }
    
    /**
     * Extract amount from SMS message
     */
    private fun extractAmount(messageBody: String): Double {
        // Look for patterns like ₹100, Rs.100, INR 100, etc.
        val amountPatterns = listOf(
            Regex("""₹\s*(\d+(?:\.\d{2})?)"""),
            Regex("""Rs\.?\s*(\d+(?:\.\d{2})?)"""),
            Regex("""INR\s*(\d+(?:\.\d{2})?)"""),
            Regex("""rupees\s*(\d+(?:\.\d{2})?)""")
        )
        
        for (pattern in amountPatterns) {
            val match = pattern.find(messageBody)
            if (match != null) {
                return match.groupValues[1].toDouble()
            }
        }
        
        return 0.0
    }
    
    /**
     * Extract merchant from SMS message
     */
    private fun extractMerchant(messageBody: String): String {
        // Look for patterns like "to [Merchant Name]"
        val merchantPattern = Regex("""to\s+([a-zA-Z0-9\s]+)""")
        val match = merchantPattern.find(messageBody)
        
        return match?.groupValues?.get(1)?.trim() ?: ""
    }
}

data class UPITransactionDetails(
    val amount: Double,
    val merchant: String
)