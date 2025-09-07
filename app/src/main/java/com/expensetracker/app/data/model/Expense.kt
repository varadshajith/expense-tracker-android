package com.expensetracker.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import java.text.SimpleDateFormat
import java.util.*

@Entity(
    tableName = "expenses",
    indices = [
        Index(value = ["date"]),
        Index(value = ["status"]),
        Index(value = ["merchant"])
    ]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val amount: Double,
    val merchant: String,
    val description: String? = null,
    val category: String? = null,
    val status: String = "pending" // "pending" or "complete"
) {
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_COMPLETE = "complete"
        
        // Common expense categories
        const val CATEGORY_FOOD = "Food"
        const val CATEGORY_TRANSPORT = "Transport"
        const val CATEGORY_SHOPPING = "Shopping"
        const val CATEGORY_ENTERTAINMENT = "Entertainment"
        const val CATEGORY_HEALTHCARE = "Healthcare"
        const val CATEGORY_UTILITIES = "Utilities"
        const val CATEGORY_OTHER = "Other"
    }
    
    /**
     * Check if the expense is pending (needs more details)
     */
    fun isPending(): Boolean = status == STATUS_PENDING
    
    /**
     * Check if the expense is complete (has all details)
     */
    fun isComplete(): Boolean = status == STATUS_COMPLETE
    
    /**
     * Get formatted date string
     */
    fun getFormattedDate(): String {
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return formatter.format(Date(date))
    }
    
    /**
     * Get formatted amount string with currency symbol
     */
    fun getFormattedAmount(): String {
        return "₹${String.format("%.2f", amount)}"
    }
    
    /**
     * Check if expense has all required details
     */
    fun hasCompleteDetails(): Boolean {
        return description?.isNotBlank() == true && 
               category?.isNotBlank() == true
    }
}
