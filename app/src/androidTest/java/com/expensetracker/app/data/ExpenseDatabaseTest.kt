package com.expensetracker.app.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.expensetracker.app.data.dao.ExpenseDao
import com.expensetracker.app.data.database.AppDatabase
import com.expensetracker.app.data.model.Expense
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class ExpenseDatabaseTest {
    
    private lateinit var database: AppDatabase
    private lateinit var expenseDao: ExpenseDao
    
    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        expenseDao = database.expenseDao()
    }
    
    @After
    fun closeDb() {
        database.close()
    }
    
    @Test
    fun `insert and get expense should work correctly`() = runTest {
        // Given
        val expense = Expense(
            amount = 100.0,
            merchant = "Test Merchant",
            description = "Test Description",
            category = "Food",
            status = Expense.STATUS_COMPLETE
        )
        
        // When
        val insertedId = expenseDao.insertExpense(expense)
        val retrievedExpense = expenseDao.getExpenseById(insertedId)
        
        // Then
        assertNotNull(retrievedExpense)
        assertEquals(expense.amount, retrievedExpense!!.amount, 0.01)
        assertEquals(expense.merchant, retrievedExpense.merchant)
        assertEquals(expense.description, retrievedExpense.description)
        assertEquals(expense.category, retrievedExpense.category)
        assertEquals(expense.status, retrievedExpense.status)
    }
    
    @Test
    fun `getAllExpenses should return all expenses ordered by date`() = runTest {
        // Given
        val expense1 = Expense(
            amount = 100.0,
            merchant = "Merchant 1",
            date = System.currentTimeMillis() - 1000
        )
        val expense2 = Expense(
            amount = 200.0,
            merchant = "Merchant 2",
            date = System.currentTimeMillis()
        )
        
        // When
        expenseDao.insertExpense(expense1)
        expenseDao.insertExpense(expense2)
        val allExpenses = expenseDao.getAllExpenses().first()
        
        // Then
        assertEquals(2, allExpenses.size)
        // Should be ordered by date DESC (newest first)
        assertEquals(expense2.merchant, allExpenses[0].merchant)
        assertEquals(expense1.merchant, allExpenses[1].merchant)
    }
    
    @Test
    fun `getExpensesByStatus should filter correctly`() = runTest {
        // Given
        val pendingExpense = Expense(
            amount = 100.0,
            merchant = "Pending Merchant",
            status = Expense.STATUS_PENDING
        )
        val completeExpense = Expense(
            amount = 200.0,
            merchant = "Complete Merchant",
            description = "Description",
            category = "Food",
            status = Expense.STATUS_COMPLETE
        )
        
        // When
        expenseDao.insertExpense(pendingExpense)
        expenseDao.insertExpense(completeExpense)
        val pendingExpenses = expenseDao.getPendingExpenses().first()
        val completeExpenses = expenseDao.getCompleteExpenses().first()
        
        // Then
        assertEquals(1, pendingExpenses.size)
        assertEquals(1, completeExpenses.size)
        assertEquals(Expense.STATUS_PENDING, pendingExpenses[0].status)
        assertEquals(Expense.STATUS_COMPLETE, completeExpenses[0].status)
    }
    
    @Test
    fun `updateExpense should modify existing expense`() = runTest {
        // Given
        val expense = Expense(
            amount = 100.0,
            merchant = "Original Merchant",
            status = Expense.STATUS_PENDING
        )
        val insertedId = expenseDao.insertExpense(expense)
        
        // When
        val updatedExpense = expense.copy(
            id = insertedId,
            description = "Updated Description",
            category = "Food",
            status = Expense.STATUS_COMPLETE
        )
        expenseDao.updateExpense(updatedExpense)
        val retrievedExpense = expenseDao.getExpenseById(insertedId)
        
        // Then
        assertNotNull(retrievedExpense)
        assertEquals("Updated Description", retrievedExpense!!.description)
        assertEquals("Food", retrievedExpense.category)
        assertEquals(Expense.STATUS_COMPLETE, retrievedExpense.status)
    }
    
    @Test
    fun `deleteExpense should remove expense from database`() = runTest {
        // Given
        val expense = Expense(
            amount = 100.0,
            merchant = "Test Merchant"
        )
        val insertedId = expenseDao.insertExpense(expense)
        
        // When
        val expenseToDelete = expense.copy(id = insertedId)
        expenseDao.deleteExpense(expenseToDelete)
        val retrievedExpense = expenseDao.getExpenseById(insertedId)
        
        // Then
        assertNull(retrievedExpense)
    }
    
    @Test
    fun `searchExpenses should find expenses by description or merchant`() = runTest {
        // Given
        val expense1 = Expense(
            amount = 100.0,
            merchant = "Coffee Shop",
            description = "Morning coffee"
        )
        val expense2 = Expense(
            amount = 200.0,
            merchant = "Restaurant",
            description = "Lunch with friends"
        )
        val expense3 = Expense(
            amount = 50.0,
            merchant = "Grocery Store",
            description = "Weekly groceries"
        )
        
        // When
        expenseDao.insertExpense(expense1)
        expenseDao.insertExpense(expense2)
        expenseDao.insertExpense(expense3)
        
        val coffeeResults = expenseDao.searchExpenses("coffee").first()
        val restaurantResults = expenseDao.searchExpenses("Restaurant").first()
        
        // Then
        assertEquals(1, coffeeResults.size)
        assertEquals("Coffee Shop", coffeeResults[0].merchant)
        
        assertEquals(1, restaurantResults.size)
        assertEquals("Restaurant", restaurantResults[0].merchant)
    }
    
    @Test
    fun `getTotalAmount should calculate correct sum`() = runTest {
        // Given
        val expense1 = Expense(amount = 100.0, merchant = "Merchant 1")
        val expense2 = Expense(amount = 200.0, merchant = "Merchant 2")
        val expense3 = Expense(amount = 50.0, merchant = "Merchant 3")
        
        // When
        expenseDao.insertExpense(expense1)
        expenseDao.insertExpense(expense2)
        expenseDao.insertExpense(expense3)
        val totalAmount = expenseDao.getTotalAmount()
        
        // Then
        assertEquals(350.0, totalAmount, 0.01)
    }
    
    @Test
    fun `getExpenseCount should return correct count`() = runTest {
        // Given
        val expense1 = Expense(amount = 100.0, merchant = "Merchant 1")
        val expense2 = Expense(amount = 200.0, merchant = "Merchant 2")
        
        // When
        expenseDao.insertExpense(expense1)
        expenseDao.insertExpense(expense2)
        val count = expenseDao.getExpenseCount()
        
        // Then
        assertEquals(2, count)
    }
    
    @Test
    fun `getAllCategories should return unique categories`() = runTest {
        // Given
        val expense1 = Expense(amount = 100.0, merchant = "Merchant 1", category = "Food")
        val expense2 = Expense(amount = 200.0, merchant = "Merchant 2", category = "Transport")
        val expense3 = Expense(amount = 50.0, merchant = "Merchant 3", category = "Food")
        
        // When
        expenseDao.insertExpense(expense1)
        expenseDao.insertExpense(expense2)
        expenseDao.insertExpense(expense3)
        val categories = expenseDao.getAllCategories()
        
        // Then
        assertEquals(2, categories.size)
        assertTrue(categories.contains("Food"))
        assertTrue(categories.contains("Transport"))
    }
}
