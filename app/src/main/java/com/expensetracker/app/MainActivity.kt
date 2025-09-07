package com.expensetracker.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.expensetracker.app.ui.ExpenseTrackerApp

class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        enableEdgeToEdge()
        
        // Handle notification intents
        handleNotificationIntent(intent)
        
        // Get application instance for dependency injection
        val application = application as ExpenseTrackerApplication
        
        setContent {
            ExpenseTrackerApp(
                application = application,
                initialExpenseId = getInitialExpenseId()
            )
        }
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }
    
    private fun handleNotificationIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.getStringExtra("action")
            when (action) {
                "edit_expense" -> {
                    val expenseId = intent.getLongExtra("expense_id", -1L)
                    if (expenseId != -1L) {
                        Log.d(TAG, "Opening EditExpense screen for ID: $expenseId")
                        // The expense ID will be passed to the app composable
                    }
                }
                "view_expenses" -> {
                    Log.d(TAG, "Opening ExpenseList screen")
                }
            }
        }
    }
    
    private fun getInitialExpenseId(): Long? {
        return intent?.getLongExtra("expense_id", -1L)?.takeIf { it != -1L }
    }
}
