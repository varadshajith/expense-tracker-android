package com.expensetracker.app.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.expensetracker.app.data.dao.ExpenseDao
import com.expensetracker.app.data.model.Expense

@Database(
    entities = [Expense::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    
    companion object {
        private const val DATABASE_NAME = "expense_database"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration() // For development - remove in production
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * For testing purposes - creates an in-memory database
         */
        fun getTestDatabase(context: Context): AppDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                AppDatabase::class.java
            )
            .allowMainThreadQueries() // Only for testing
            .build()
        }
        
        /**
         * Close the database instance
         */
        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
