package com.expensetracker.app.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.expensetracker.app.MainActivity
import com.expensetracker.app.notification.ExpenseNotificationManager
import com.expensetracker.app.utils.UPITransactionDetails

object NotificationHelper {
    
    /**
     * Check if notification permissions are granted
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    
    /**
     * Request notification permissions (for Android 13+)
     */
    fun requestNotificationPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // This would typically be called from an Activity
            // For now, we'll just log that permission is needed
            android.util.Log.d("NotificationHelper", "Notification permission needed for Android 13+")
        }
    }
    
    /**
     * Create intent to open EditExpense screen
     */
    fun createEditExpenseIntent(context: Context, expenseId: Long): Intent {
        return Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("action", "edit_expense")
            putExtra("expense_id", expenseId)
        }
    }
    
    /**
     * Create intent to open MainActivity
     */
    fun createMainActivityIntent(context: Context): Intent {
        return Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("action", "view_expenses")
        }
    }
    
    /**
     * Show notification for new UPI transaction
     */
    fun showNewTransactionNotification(
        context: Context,
        amount: Double,
        merchant: String,
        expenseId: Long
    ) {
        val notificationManager = ExpenseNotificationManager(context)
        val transactionDetails = UPITransactionDetails(amount, merchant)
        notificationManager.showNewTransactionNotification(transactionDetails, expenseId)
    }
    
    /**
     * Show notification for pending expenses reminder
     */
    fun showPendingExpenseReminder(context: Context, pendingCount: Int) {
        val notificationManager = ExpenseNotificationManager(context)
        notificationManager.showPendingExpenseReminder(pendingCount)
    }
    
    /**
     * Show notification for expense completion
     */
    fun showExpenseCompletedNotification(context: Context, merchant: String, amount: Double) {
        val notificationManager = ExpenseNotificationManager(context)
        notificationManager.showExpenseCompletedNotification(merchant, amount)
    }
    
    /**
     * Cancel all notifications
     */
    fun cancelAllNotifications(context: Context) {
        val notificationManager = ExpenseNotificationManager(context)
        notificationManager.cancelAllNotifications()
    }
    
    /**
     * Get notification channel importance
     */
    fun getChannelImportance(context: Context, channelId: String): Int {
        val notificationManager = ExpenseNotificationManager(context)
        return notificationManager.getChannelImportance(channelId)
    }
}
