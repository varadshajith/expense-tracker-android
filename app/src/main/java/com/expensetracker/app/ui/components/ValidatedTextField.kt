package com.expensetracker.app.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.privacysandbox.tools.core.validator.ValidationResult
import com.expensetracker.app.utils.ValidationUtils

@Composable
fun ValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    validator: (String) -> ValidationUtils.ValidationResult = { ValidationUtils.ValidationResult.Success },
    enabled: Boolean = true,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = if (singleLine) 1 else 2
) {
    var isDirty by remember { mutableStateOf(false) }
    val validationResult = remember(value, isDirty) {
        if (isDirty) validator(value) else ValidationUtils.ValidationResult.Success
    }
    
    val hasError = validationResult is ValidationUtils.ValidationResult.Error
    val errorMessage = (validationResult as? ValidationUtils.ValidationResult.Error).message
    
    OutlinedTextField(
        value = value,
        onValueChange = { 
            if (!isDirty) isDirty = true
            onValueChange(it)
        },
        label = { Text(label) },
        placeholder = if (placeholder.isNotEmpty()) { { Text(placeholder) } } else null,
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = keyboardOptions,
        isError = hasError,
        supportingText = errorMessage?.let { { Text(it) } },
        enabled = enabled,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun ValidatedAmountField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    ValidatedTextField(
        value = value,
        onValueChange = onValueChange,
        label = "Amount (₹)",
        placeholder = "0.00",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier,
        validator = ValidationUtils::validateAmount,
        enabled = enabled
    )
}

@Composable
fun ValidatedMerchantField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    ValidatedTextField(
        value = value,
        onValueChange = onValueChange,
        label = "Merchant",
        placeholder = "e.g., Cafe Coffee Day",
        modifier = modifier,
        validator = ValidationUtils::validateMerchant,
        enabled = enabled
    )
}

@Composable
fun ValidatedDescriptionField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    ValidatedTextField(
        value = value,
        onValueChange = onValueChange,
        label = "Description (Optional)",
        placeholder = "Add notes about this expense",
        modifier = modifier,
        validator = ValidationUtils::validateDescription,
        enabled = enabled,
        singleLine = false,
        maxLines = 4,
        minLines = 2
    )
}

@Composable
fun ValidatedCategoryField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    ValidatedTextField(
        value = value,
        onValueChange = onValueChange,
        label = "Category",
        placeholder = "e.g., Food & Dining",
        modifier = modifier,
        validator = ValidationUtils::validateCategory,
        enabled = enabled
    )
}

@Composable
fun FormValidationSummary(
    validationResult: ValidationUtils.FormValidationResult,
    modifier: Modifier = Modifier
) {
    if (!validationResult.isValid && validationResult.errors.isNotEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Please fix the following errors:",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                validationResult.errors.forEach { error ->
                    Text(
                        text = "• $error",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}
