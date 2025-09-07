package com.expensetracker.app.work

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.expensetracker.app.data.database.AppDatabase
import com.expensetracker.app.data.dao.ExpenseDao
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmsProcessingWorkerTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var expenseDao: ExpenseDao

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        expenseDao = database.expenseDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `test SmsProcessingWorker success`() = runBlocking {
        val sampleSms = "UPI: ₹150 debited from A/c **1234 to Cafe Coffee Day. UPI Ref: 123456789012"
        val inputData = Data.Builder()
            .putString("sender", "VK-UPI")
            .putString("message_body", sampleSms)
            .build()

        val worker = TestListenableWorkerBuilder<SmsProcessingWorker>(context)
            .setInputData(inputData)
            .build()

        val result = worker.startWork().get()

        assertEquals(ListenableWorker.Result.success(), result)

        val expenses = expenseDao.searchExpenses("Cafe Coffee Day").first()
        assertNotNull(expenses)
        assertEquals(1, expenses.size)
        assertEquals(150.0, expenses[0].amount, 0.0)
        assertEquals("Cafe Coffee Day", expenses[0].merchant)
    }
}
