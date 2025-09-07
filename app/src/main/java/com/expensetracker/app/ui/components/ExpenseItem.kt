package com.expensetracker.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.expensetracker.app.data.model.Expense

@Composable
fun ExpenseItem(
    expense: Expense,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Icon
            StatusIcon(
                isComplete = expense.isComplete(),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Expense Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Merchant Name
                Text(
                    text = expense.merchant,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Description or Category
                if (expense.description?.isNotBlank() == true) {
                    Text(
                        text = expense.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else if (expense.category?.isNotBlank() == true) {
                    Text(
                        text = expense.category,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "Tap to add details",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Date
                Text(
                    text = expense.getFormattedDate(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Amount
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = expense.getFormattedAmount(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Status Badge
                StatusBadge(
                    isComplete = expense.isComplete(),
                    modifier = Modifier
                )
            }
        }
    }
}

@Composable
private fun StatusIcon(
    isComplete: Boolean,
    modifier: Modifier = Modifier
) {
    val (icon, color) = if (isComplete) {
        Icons.Default.CheckCircle to MaterialTheme.colorScheme.primary
    } else {
        Icons.Default.Pending to MaterialTheme.colorScheme.secondary
    }
    
    Icon(
        imageVector = icon,
        contentDescription = if (isComplete) "Complete" else "Pending",
        tint = color,
        modifier = modifier
    )
}

@Composable
private fun StatusBadge(
    isComplete: Boolean,
    modifier: Modifier = Modifier
) {
    val (text, color, containerColor) = if (isComplete) {
        Triple("Complete", MaterialTheme.colorScheme.onPrimary, MaterialTheme.colorScheme.primary)
    } else {
        Triple("Pending", MaterialTheme.colorScheme.onSecondary, MaterialTheme.colorScheme.secondary)
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = containerColor
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

