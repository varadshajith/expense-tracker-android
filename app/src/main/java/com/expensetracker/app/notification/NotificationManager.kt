package com.expensetracker.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.expensetracker.app.MainActivity
import com.expensetracker.app.R
import com.expensetracker.app.utils.SmsParser

class ExpenseNotificationManager(private val context: Context) {
    
    companion object {
        private const val CHANNEL_ID_EXPENSE_ALERTS = "expense_alerts"
        private const val CHANNEL_ID_EXPENSE_REMINDERS = "expense_reminders"
        private const val NOTIFICATION_ID_EXPENSE_ALERT = 1001
        private const val NOTIFICATION_ID_EXPENSE_REMINDER = 1002
        
        // Request codes for PendingIntents
        private const val REQUEST_CODE_NEW_EXPENSE = 2001
        private const val REQUEST_CODE_EDIT_EXPENSE = 2002
        private const val REQUEST_CODE_PENDING_REMINDER = 2003
    }
    
    private val notificationManager = NotificationManagerCompat.from(context)
    
    init {
        createNotificationChannels()
    }
    
    /**
     * Create notification channels for different types of notifications
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Expense Alerts Channel - High priority for new transactions
            val expenseAlertsChannel = NotificationChannel(
                CHANNEL_ID_EXPENSE_ALERTS,
                "Expense Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new UPI transactions detected"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            
            // Expense Reminders Channel - Default priority for pending expenses
            val expenseRemindersChannel = NotificationChannel(
                CHANNEL_ID_EXPENSE_REMINDERS,
                "Expense Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for pending expenses that need details"
                enableLights(true)
                enableVibration(false)
                setShowBadge(true)
            }
            
            systemNotificationManager.createNotificationChannel(expenseAlertsChannel)
            systemNotificationManager.createNotificationChannel(expenseRemindersChannel)
        }
    }
    
    /**
     * Show notification for new UPI transaction detected
     */
    fun showNewTransactionNotification(transactionDetails: SmsParser.UPITransactionDetails, expenseId: Long) {
        val intent = createEditExpenseIntent(expenseId)
        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_EDIT_EXPENSE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_EXPENSE_ALERTS)
            .setSmallIcon(R.drawable.ic_notification_expense)
            .setContentTitle("New Expense Detected")
            .setContentText("₹${String.format("%.2f", transactionDetails.amount)} spent at ${transactionDetails.merchant}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("A new UPI transaction of ₹${String.format("%.2f", transactionDetails.amount)} at ${transactionDetails.merchant} has been detected. Tap to add details.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                R.drawable.ic_edit,
                "Add Details",
                pendingIntent
            )
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_EXPENSE_ALERT, notification)
    }
    
    /**
     * Show notification for pending expenses reminder
     */
    fun showPendingExpenseReminder(pendingCount: Int) {
        val intent = createMainActivityIntent()
        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_PENDING_REMINDER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_EXPENSE_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification_reminder)
            .setContentTitle("Pending Expenses")
            .setContentText("You have $pendingCount expense(s) waiting for details")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("You have $pendingCount expense(s) that were automatically detected but need additional details like description and category. Tap to review and complete them.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                R.drawable.ic_list,
                "View All",
                pendingIntent
            )
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_EXPENSE_REMINDER, notification)
    }
    
    /**
     * Show notification for expense completion
     */
    fun showExpenseCompletedNotification(merchant: String, amount: Double) {
        val intent = createMainActivityIntent()
        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_NEW_EXPENSE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_EXPENSE_ALERTS)
            .setSmallIcon(R.drawable.ic_notification_success)
            .setContentTitle("Expense Completed")
            .setContentText("₹${String.format("%.2f", amount)} at $merchant has been saved")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Your expense of ₹${String.format("%.2f", amount)} at $merchant has been successfully saved with all details.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_EXPENSE_ALERT, notification)
    }
    
    /**
     * Create intent to open EditExpense screen
     */
    private fun createEditExpenseIntent(expenseId: Long): Intent {
        return Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("action", "edit_expense")
            putExtra("expense_id", expenseId)
        }
    }
    
    /**
     * Create intent to open MainActivity
     */
    private fun createMainActivityIntent(): Intent {
        return Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("action", "view_expenses")
        }
    }
    
    /**
     * Cancel all notifications
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
    
    /**
     * Cancel specific notification
     */
    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
    
    /**
     * Check if notifications are enabled
     */
    fun areNotificationsEnabled(): Boolean {
        return notificationManager.areNotificationsEnabled()
    }
    
    /**
     * Get notification channel importance
     */
    fun getChannelImportance(channelId: String): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = systemNotificationManager.getNotificationChannel(channelId)
            channel?.importance ?: NotificationManager.IMPORTANCE_DEFAULT
        } else {
            NotificationManagerCompat.IMPORTANCE_DEFAULT
        }
    }
}
