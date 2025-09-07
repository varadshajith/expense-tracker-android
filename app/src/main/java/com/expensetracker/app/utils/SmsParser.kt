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
        
        // Check if message contains UPI keywords
        val hasUPIKeywords = UPI_KEYWORDS.any { keyword ->
            messageUpper.contains(keyword.uppercase())
        }
        
        // Check if message contains amount (₹ or Rs.)
        val hasAmount = messageBody.contains("₹") || 
                       messageBody.contains("Rs.") || 
                       messageBody.contains("INR") ||
                       messageBody.contains("rupees")
        
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
            Regex("₹\\s*(\\d+(?:\\.\\d{2})?)"),
            Regex("Rs\\.?\\s*(\\d+(?:\\.\\d{2})?)"),
            Regex("INR\\s*(\\d+(?:\\.\\d{2})?)"),
            Regex("(\\d+(?:\\.\\d{2})?)\\s*rupees?"),
            Regex("(\\d+(?:\\.\\d{2})?)\\s*rs"),
            Regex("(\\d+(?:\\.\\d{2})?)\\s*₹"),
            Regex("amount\\s*:?\\s*(\\d+(?:\\.\\d{2})?)", RegexOption.IGNORE_CASE)
        )
        
        for (pattern in amountPatterns) {
            val match = pattern.find(messageBody)
            if (match != null) {
                val amountStr = match.groupValues[1]
                val amount = amountStr.toDoubleOrNull()
                if (amount != null && amount > 0) {
                    return amount
                }
            }
        }
        
        return 0.0
    }
    
    /**
     * Extract merchant name from SMS message
     */
    private fun extractMerchant(messageBody: String): String {
        // Common patterns for merchant extraction.
        // The regex tries to find a merchant name after keywords like "to", "at", "paid", etc.
        // It captures text until it hits a terminating word like "on", "via", a period, or the end of the line.
        val merchantPatterns = listOf(
            Regex("to\s+(.+?)(?:\s+on\s|\s+via\s|\s+at\s|\.|$)", RegexOption.IGNORE_CASE),
            Regex("at\s+(.+?)(?:\s+on\s|\s+via\s|\.)", RegexOption.IGNORE_CASE),
            Regex("paid\s+(.+?)(?:\s+on\s|\s+via\s|\.)", RegexOption.IGNORE_CASE),
            Regex("spent\s+(.+?)(?:\s+on\s|\s+via\s|\.)", RegexOption.IGNORE_CASE),
            Regex("merchant:\s*(.+?)(?:\s+on\s|\s+via\s|\.)", RegexOption.IGNORE_CASE),
            Regex("vendor:\s*(.+?)(?:\s+on\s|\s+via\s|\.)", RegexOption.IGNORE_CASE),
            Regex("shop:\s*(.+?)(?:\s+on\s|\s+via\s|\.)", RegexOption.IGNORE_CASE),
            Regex("store:\s*(.+?)(?:\s+on\s|\s+via\s|\.)", RegexOption.IGNORE_CASE)
        )

        for (pattern in merchantPatterns) {
            val match = pattern.find(messageBody)
            if (match != null) {
                // Remove any trailing characters like '.' or ',' from the matched group
                val merchant = match.groupValues[1].trimEnd('.', ',').trim()
                if (merchant.isNotBlank() && merchant.length > 1) {
                    return merchant
                }
            }
        }

        return "Unknown Merchant"
    }
    
    /**
     * Get sample UPI SMS messages for testing
     */
    fun getSampleUPIMessages(): List<String> {
        return listOf(
            "UPI: ₹150 debited from A/c **1234 to Cafe Coffee Day. UPI Ref: 123456789012",
            "Rs.250 paid to Uber via PhonePe. Transaction ID: 987654321098",
            "INR 1200 spent at Big Bazaar. UPI Ref: 112233445566",
            "₹500 debited from your account to Netflix. Transaction successful.",
            "Rs.75 paid to Local Store via GPay. Ref: 556677889900",
            "INR 300 transferred to Restaurant. UPI transaction completed.",
            "₹120 spent at Metro Station via Paytm. Transaction ID: 334455667788",
            "Rs.450 debited to Gas Station. UPI Ref: 778899001122",
            "INR 200 paid to Coffee Shop. Transaction successful.",
            "₹800 debited from A/c **5678 to Apollo Pharmacy. UPI Ref: 990011223344"
        )
    }
    
    /**
     * Data class to hold parsed UPI transaction details
     */
    data class UPITransactionDetails(
        val amount: Double,
        val merchant: String
    )
}
