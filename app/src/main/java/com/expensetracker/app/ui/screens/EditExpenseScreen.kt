package com.expensetracker.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expensetracker.app.data.model.Expense
import com.expensetracker.app.ui.components.ValidatedAmountField
import com.expensetracker.app.ui.components.ValidatedMerchantField
import com.expensetracker.app.ui.components.ValidatedDescriptionField
import com.expensetracker.app.ui.components.FormValidationSummary
import com.expensetracker.app.utils.ValidationUtils
import com.expensetracker.app.viewmodel.ExpenseViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseScreen(
    expenseId: Long?,
    expenseViewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val expense by expenseViewModel.currentExpense.collectAsStateWithLifecycle()

    var amount by remember { mutableStateOf("") }
    var merchant by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }

    // Load expense when the screen is shown
    LaunchedEffect(expenseId) {
        if (expenseId != null) {
            expenseViewModel.loadExpenseById(expenseId)
        } else {
            expenseViewModel.clearCurrentExpense()
        }
    }

    // Form validation
    val formValidationResult = remember(amount, merchant, description, category) {
        ValidationUtils.validateExpenseForm(amount, merchant, description, category)
    }

    val isFormValid = formValidationResult.isValid

    // Pre-fill form when expense is loaded
    LaunchedEffect(expense) {
        if (expense != null) {
            amount = expense.amount.toString()
            merchant = expense.merchant
            description = expense.description ?: ""
            category = expense.category ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (expenseId == null) "Add Expense" else "Edit Expense",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (expenseId != null) {
                        IconButton(
                            onClick = { showDeleteDialog = true }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (isFormValid) {
                                showSaveDialog = true
                            }
                        },
                        enabled = isFormValid,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = "Save",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Form Validation Summary
            FormValidationSummary(
                validationResult = formValidationResult,
                modifier = Modifier.fillMaxWidth()
            )

            // Amount Field
            ValidatedAmountField(
                value = amount,
                onValueChange = { amount = it },
                modifier = Modifier.fillMaxWidth()
            )

            // Merchant Field
            ValidatedMerchantField(
                value = merchant,
                onValueChange = { merchant = it },
                modifier = Modifier.fillMaxWidth()
            )

            // Description Field
            ValidatedDescriptionField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth()
            )

            // Category Field
            CategorySelector(
                selectedCategory = category,
                onCategorySelected = { category = it },
                modifier = Modifier.fillMaxWidth()
            )

            // Expense Preview (if editing existing expense)
            if (expense != null) {
                ExpensePreviewCard(
                    expense = expense,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Save Confirmation Dialog
    if (showSaveDialog) {
        SaveExpenseDialog(
            isNewExpense = expenseId == null,
            onConfirm = {
                coroutineScope.launch {
                    val sanitizedMerchant = ValidationUtils.sanitizeMerchantName(merchant)
                    val sanitizedDescription = ValidationUtils.sanitizeDescription(description)
                    val sanitizedCategory = ValidationUtils.sanitizeCategory(category)

                    val expenseToSave = expense?.copy(
                        amount = amount.toDouble(),
                        merchant = sanitizedMerchant,
                        description = sanitizedDescription.takeIf { it.isNotBlank() },
                        category = sanitizedCategory.takeIf { it.isNotBlank() }
                    ) ?: Expense(
                        amount = amount.toDouble(),
                        merchant = sanitizedMerchant,
                        description = sanitizedDescription.takeIf { it.isNotBlank() },
                        category = sanitizedCategory.takeIf { it.isNotBlank() }
                    )
                    expenseViewModel.saveExpense(expenseToSave)
                    onNavigateBack()
                }
            },
            onDismiss = { showSaveDialog = false }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        DeleteExpenseDialog(
            expense = expense,
            onConfirm = {
                coroutineScope.launch {
                    expense?.let { expenseViewModel.deleteExpense(it) }
                    onNavigateBack()
                }
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun CategorySelector(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        "Food & Dining",
        "Transportation",
        "Shopping",
        "Entertainment",
        "Healthcare",
        "Utilities",
        "Education",
        "Travel",
        "Other"
    )

    Column(modifier = modifier) {
        Text(
            text = "Category",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category) },
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        // Custom category input
        if (selectedCategory.isNotBlank() && selectedCategory !in categories) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = onCategorySelected,
                label = { Text("Custom Category") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }
    }
}

@Composable
private fun ExpensePreviewCard(
    expense: Expense,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Current Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Status:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = if (expense.isComplete()) "Complete" else "Pending",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (expense.isComplete())
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.secondary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Created:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = expense.getFormattedDate(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SaveExpenseDialog(
    isNewExpense: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Expense") },
        text = {
            Text(
                if (isNewExpense)
                    "Are you sure you want to add this expense?"
                else
                    "Are you sure you want to save these changes?"
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DeleteExpenseDialog(
    expense: Expense?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Expense") },
        text = {
            Text(
                "Are you sure you want to delete this expense? This action cannot be undone."
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
