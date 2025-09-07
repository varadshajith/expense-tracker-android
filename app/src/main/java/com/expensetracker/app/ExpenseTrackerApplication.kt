package com.expensetracker.app

import android.app.Application
import com.expensetracker.app.data.database.AppDatabase
import com.expensetracker.app.data.repository.ExpenseRepository

class ExpenseTrackerApplication : Application() {
    
    // Database instance
    val database by lazy { AppDatabase.getDatabase(this) }
    
    // Repository instance
    val expenseRepository by lazy { ExpenseRepository(database.expenseDao()) }
    
}
