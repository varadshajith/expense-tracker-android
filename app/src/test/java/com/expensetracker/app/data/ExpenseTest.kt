package com.expensetracker.app.data

import com.expensetracker.app.data.model.Expense
import org.junit.Test
import org.junit.Assert.*

class ExpenseTest {
    
    @Test
    fun `expense should have correct default values`() {
        val expense = Expense(
            amount = 100.0,
            merchant = "Test Merchant"
        )
        
        assertEquals(0L, expense.id)
        assertTrue(expense.date > 0)
        assertEquals(100.0, expense.amount, 0.01)
        assertEquals("Test Merchant", expense.merchant)
        assertNull(expense.description)
        assertNull(expense.category)
        assertEquals(Expense.STATUS_PENDING, expense.status)
    }
    
    @Test
    fun `expense should correctly identify pending status`() {
        val pendingExpense = Expense(
            amount = 100.0,
            merchant = "Test Merchant",
            status = Expense.STATUS_PENDING
        )
        
        assertTrue(pendingExpense.isPending())
        assertFalse(pendingExpense.isComplete())
    }
    
    @Test
    fun `expense should correctly identify complete status`() {
        val completeExpense = Expense(
            amount = 100.0,
            merchant = "Test Merchant",
            description = "Test Description",
            category = "Food",
            status = Expense.STATUS_COMPLETE
        )
        
        assertFalse(completeExpense.isPending())
        assertTrue(completeExpense.isComplete())
    }
    
    @Test
    fun `expense should format amount correctly`() {
        val expense = Expense(
            amount = 123.45,
            merchant = "Test Merchant"
        )
        
        assertEquals("₹123.45", expense.getFormattedAmount())
    }
    
    @Test
    fun `expense should format date correctly`() {
        val expense = Expense(
            amount = 100.0,
            merchant = "Test Merchant",
            date = 1640995200000L // Jan 1, 2022
        )
        
        val formattedDate = expense.getFormattedDate()
        assertTrue(formattedDate.contains("Jan"))
        assertTrue(formattedDate.contains("2022"))
    }
    
    @Test
    fun `expense should correctly identify complete details`() {
        val expenseWithDetails = Expense(
            amount = 100.0,
            merchant = "Test Merchant",
            description = "Test Description",
            category = "Food"
        )
        
        assertTrue(expenseWithDetails.hasCompleteDetails())
    }
    
    @Test
    fun `expense should correctly identify incomplete details`() {
        val expenseWithoutDescription = Expense(
            amount = 100.0,
            merchant = "Test Merchant",
            category = "Food"
        )
        
        assertFalse(expenseWithoutDescription.hasCompleteDetails())
        
        val expenseWithoutCategory = Expense(
            amount = 100.0,
            merchant = "Test Merchant",
            description = "Test Description"
        )
        
        assertFalse(expenseWithoutCategory.hasCompleteDetails())
    }
    
    @Test
    fun `expense constants should have correct values`() {
        assertEquals("pending", Expense.STATUS_PENDING)
        assertEquals("complete", Expense.STATUS_COMPLETE)
        assertEquals("Food", Expense.CATEGORY_FOOD)
        assertEquals("Transport", Expense.CATEGORY_TRANSPORT)
        assertEquals("Shopping", Expense.CATEGORY_SHOPPING)
        assertEquals("Entertainment", Expense.CATEGORY_ENTERTAINMENT)
        assertEquals("Healthcare", Expense.CATEGORY_HEALTHCARE)
        assertEquals("Utilities", Expense.CATEGORY_UTILITIES)
        assertEquals("Other", Expense.CATEGORY_OTHER)
    }
}
