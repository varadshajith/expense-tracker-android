package com.expensetracker.app.utils

import com.expensetracker.app.utils.ValidationResult.Success
import com.expensetracker.app.utils.ValidationResult.Error

import java.util.regex.Pattern

object ValidationUtils {
    

    /**
     * Validates expense amount
     */
    fun validateAmount(amount: String): ValidationResult {
        return when {
            amount.isBlank() -> ValidationResult.Error("Amount is required")
                        amount.toDoubleOrNull() == null -> ValidationResult.Error("Invalid amount format")
            amount.toDouble() <= 0 -> Error("Amount must be greater than 0")
            amount.toDouble() > 999999.99 -> ValidationResult.Error("Amount cannot exceed ₹999,999.99")
            else -> Success
        }
    }
    
    /**
     * Validates merchant name
     */
    fun validateMerchant(merchant: String): ValidationResult {
        return when {
            merchant.isBlank() -> ValidationResult.Error("Merchant is required")
            merchant.length < 2 -> ValidationResult.Error("Merchant name must be at least 2 characters")
            merchant.length > 100 -> ValidationResult.Error("Merchant name cannot exceed 100 characters")
            !isValidMerchantName(merchant) -> ValidationResult.Error("Merchant name contains invalid characters")
            else -> Success
        }
    }
    
    /**
     * Validates description
     */
    fun validateDescription(description: String): ValidationResult {
        return when {
            description.length > 500 -> ValidationResult.Error("Description cannot exceed 500 characters")
            else -> Success
        }
    }
    
    /**
     * Validates category
     */
    fun validateCategory(category: String): ValidationResult {
        return when {
            category.length > 50 -> ValidationResult.Error("Category cannot exceed 50 characters")
            !isValidCategoryName(category) -> ValidationResult.Error("Category contains invalid characters")
            else -> Success
        }
    }
    
    /**
     * Validates complete expense form
     */
    fun validateExpenseForm(
        amount: String,
        merchant: String,
        description: String = "",
        category: String = ""
    ): FormValidationResult {
        val amountResult = validateAmount(amount)
        val merchantResult = validateMerchant(merchant)
        val descriptionResult = validateDescription(description)
        val categoryResult = validateCategory(category)
        
        val errors = listOfNotNull(
            amountResult.takeIf { it is ValidationResult.Error }?.let { (it as ValidationResult.Error).message },
            merchantResult.takeIf { it is ValidationResult.Error }?.let { (it as ValidationResult.Error).message },
            descriptionResult.takeIf { it is ValidationResult.Error }?.let { (it as ValidationResult.Error).message },
            categoryResult.takeIf { it is ValidationResult.Error }?.let { (it as ValidationResult.Error).message }
        )
        
        return FormValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    /**
     * Checks if merchant name contains only valid characters
     */
    private fun isValidMerchantName(merchant: String): Boolean {
        val pattern = Pattern.compile("^[a-zA-Z0-9\\s&.,'-]+$")
        return pattern.matcher(merchant).matches()
    }
    
    /**
     * Checks if category name contains only valid characters
     */
    private fun isValidCategoryName(category: String): Boolean {
        val pattern = Pattern.compile("^[a-zA-Z0-9\\s&.,'-]+$")
        return pattern.matcher(category).matches()
    }
    
    /**
     * Formats amount for display
     */
    fun formatAmount(amount: Double): String {
        return String.format("₹%.2f", amount)
    }
    
    /**
     * Formats amount for input (removes currency symbol)
     */
    fun formatAmountForInput(amount: Double): String {
        return String.format("%.2f", amount)
    }
    
    /**
     * Sanitizes merchant name
     */
    fun sanitizeMerchantName(merchant: String): String {
        return merchant.trim()
            .replace(Regex("\\s+"), " ") // Replace multiple spaces with single space
            .replace(Regex("[^a-zA-Z0-9\\s&.,'-]"), "") // Remove invalid characters
    }
    
    /**
     * Sanitizes description
     */
    fun sanitizeDescription(description: String): String {
        return description.trim()
            .replace(Regex("\\s+"), " ") // Replace multiple spaces with single space
    }
    
    /**
     * Sanitizes category
     */
    fun sanitizeCategory(category: String): String {
        return category.trim()
            .replace(Regex("\\s+"), " ") // Replace multiple spaces with single space
            .replace(Regex("[^a-zA-Z0-9\\s&.,'-]"), "") // Remove invalid characters
    }
}

/**
 * Validation result sealed class
 */
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

/**
 * Form validation result
 */
data class FormValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)

/**
 * Field validation state for UI
 */
data class FieldValidationState(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val isDirty: Boolean = false // Whether user has interacted with field
)

/**
 * Form validation state for UI
 */
data class FormValidationState(
    val isValid: Boolean,
    val fieldStates: Map<String, FieldValidationState> = emptyMap(),
    val globalErrors: List<String> = emptyList()
)
