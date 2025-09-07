package com.expensetracker.app.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.expensetracker.app.data.database.AppDatabase
import com.expensetracker.app.data.repository.ExpenseRepository
import com.expensetracker.app.utils.NotificationHelper
import com.expensetracker.app.utils.SmsParser

class SmsProcessingWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "SmsProcessingWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            val sender = inputData.getString("sender") ?: return Result.failure()
            val messageBody = inputData.getString("message_body") ?: return Result.failure()

            Log.d(TAG, "Processing SMS from: $sender")
            Log.d(TAG, "Message: $messageBody")

            // Get database and repository
            val database = AppDatabase.getDatabase(applicationContext)
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

                // Show notification for new transaction
                NotificationHelper.showNewTransactionNotification(
                    context = applicationContext,
                    amount = transactionDetails.amount,
                    merchant = transactionDetails.merchant,
                    expenseId = expenseId
                )

                Log.d(TAG, "Notification shown for new transaction")

                Result.success()
            } else {
                Log.d(TAG, "Could not parse transaction details from SMS")
                Result.failure()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing SMS in worker", e)
            Result.failure()
        }
    }

}
