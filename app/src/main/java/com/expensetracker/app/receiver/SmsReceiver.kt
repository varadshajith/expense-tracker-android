package com.expensetracker.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import androidx.work.*
import com.expensetracker.app.data.database.AppDatabase
import com.expensetracker.app.data.repository.ExpenseRepository
import com.expensetracker.app.utils.SmsParser
import com.expensetracker.app.work.SmsProcessingWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class SmsReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "SmsReceiver"
        private const val WORK_NAME_SMS_PROCESSING = "sms_processing_work"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            
            for (message in messages) {
                val messageBody = message.messageBody ?: continue
                val sender = message.originatingAddress ?: continue
                
                Log.d(TAG, "Received SMS from: $sender")
                Log.d(TAG, "Message body: $messageBody")
                
                // Check if this is a UPI transaction SMS
                if (SmsParser.isUPITransaction(sender, messageBody)) {
                    Log.d(TAG, "UPI transaction detected")
                    
                    // Process SMS in background using WorkManager
                    processSMSInBackground(context, sender, messageBody)
                } else {
                    Log.d(TAG, "Not a UPI transaction SMS")
                }
            }
        }
    }
    
    
    /**
     * Process SMS in background using WorkManager
     */
    private fun processSMSInBackground(context: Context, sender: String, messageBody: String) {
        val workRequest = OneTimeWorkRequestBuilder<SmsProcessingWorker>()
            .setInputData(
                Data.Builder()
                    .putString("sender", sender)
                    .putString("message_body", messageBody)
                    .build()
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                WORK_NAME_SMS_PROCESSING,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        
        Log.d(TAG, "SMS processing work enqueued")
    }
    
    /**
     * Alternative method: Process SMS directly (for testing or immediate processing)
     */
    private fun processSMSDirectly(context: Context, sender: String, messageBody: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val repository = ExpenseRepository(database.expenseDao())
                
                // Parse UPI transaction details
                val transactionDetails = SmsParser.parseUPITransaction(messageBody)
                
                if (transactionDetails != null) {
                    // Create expense entry
                    val expenseId = repository.createExpenseFromUPI(
                        transactionDetails.amount,
                        transactionDetails.merchant
                    )
                    
                    Log.d(TAG, "Expense created with ID: $expenseId")
                    
                    // Show notification (will be implemented in Phase 4)
                    // showNotification(context, transactionDetails)
                } else {
                    Log.d(TAG, "Could not parse transaction details from SMS")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing SMS", e)
            }
        }
    }
    
}
