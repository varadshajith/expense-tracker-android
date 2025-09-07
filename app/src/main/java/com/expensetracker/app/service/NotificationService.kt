package com.expensetracker.app.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.expensetracker.app.R
import com.expensetracker.app.notification.ExpenseNotificationManager

class NotificationService : Service() {
    
    companion object {
        private const val TAG = "NotificationService"
        private const val NOTIFICATION_ID = 1000
        private const val CHANNEL_ID = "notification_service"
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "NotificationService created")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "NotificationService started")
        
        // Create foreground notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Expense Tracker")
            .setContentText("Monitoring UPI transactions")
            .setSmallIcon(R.drawable.ic_notification_expense)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        startForeground(NOTIFICATION_ID, notification)
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "NotificationService destroyed")
    }
}
