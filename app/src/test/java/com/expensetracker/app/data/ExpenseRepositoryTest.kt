package com.expensetracker.app.data

import com.expensetracker.app.data.model.Expense
import com.expensetracker.app.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class ExpenseRepositoryTest {
    
    @Mock
    private lateinit var mockExpenseDao: com.expensetracker.app.data.dao.ExpenseDao
    
    private lateinit var expenseRepository: ExpenseRepository
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        expenseRepository = ExpenseRepository(mockExpenseDao)
    }
    
    @Test
    fun `insertExpense should call dao insertExpense`() = runTest {
        // Given
        val expense = Expense(
            amount = 100.0,
            merchant = "Test Merchant"
        )
        val expectedId = 1L
        `when`(mockExpenseDao.insertExpense(expense)).thenReturn(expectedId)
        
        // When
        val result = expenseRepository.insertExpense(expense)
        
        // Then
        assertEquals(expectedId, result)
        verify(mockExpenseDao).insertExpense(expense)
    }
    
    @Test
    fun `getExpenseById should call dao getExpenseById`() = runTest {
        // Given
        val expenseId = 1L
        val expectedExpense = Expense(
            id = expenseId,
            amount = 100.0,
            merchant = "Test Merchant"
        )
        `when`(mockExpenseDao.getExpenseById(expenseId)).thenReturn(expectedExpense)
        
        // When
        val result = expenseRepository.getExpenseById(expenseId)
        
        // Then
        assertEquals(expectedExpense, result)
        verify(mockExpenseDao).getExpenseById(expenseId)
    }
    
    @Test
    fun `updateExpense should call dao updateExpense`() = runTest {
        // Given
        val expense = Expense(
            id = 1L,
            amount = 100.0,
            merchant = "Test Merchant"
        )
        
        // When
        expenseRepository.updateExpense(expense)
        
        // Then
        verify(mockExpenseDao).updateExpense(expense)
    }
    
    @Test
    fun `deleteExpense should call dao deleteExpense`() = runTest {
        // Given
        val expense = Expense(
            id = 1L,
            amount = 100.0,
            merchant = "Test Merchant"
        )
        
        // When
        expenseRepository.deleteExpense(expense)
        
        // Then
        verify(mockExpenseDao).deleteExpense(expense)
    }
    
    @Test
    fun `getPendingExpenses should call dao getPendingExpenses`() = runTest {
        // Given
        val pendingExpenses = listOf(
            Expense(
                id = 1L,
                amount = 100.0,
                merchant = "Test Merchant 1",
                status = Expense.STATUS_PENDING
            ),
            Expense(
                id = 2L,
                amount = 200.0,
                merchant = "Test Merchant 2",
                status = Expense.STATUS_PENDING
            )
        )
        `when`(mockExpenseDao.getPendingExpenses()).thenReturn(kotlinx.coroutines.flow.flowOf(pendingExpenses))
        
        // When
        val result = expenseRepository.getPendingExpenses().first()
        
        // Then
        assertEquals(pendingExpenses, result)
        verify(mockExpenseDao).getPendingExpenses()
    }
    
    @Test
    fun `getCompleteExpenses should call dao getCompleteExpenses`() = runTest {
        // Given
        val completeExpenses = listOf(
            Expense(
                id = 1L,
                amount = 100.0,
                merchant = "Test Merchant 1",
                description = "Test Description 1",
                category = "Food",
                status = Expense.STATUS_COMPLETE
            )
        )
        `when`(mockExpenseDao.getCompleteExpenses()).thenReturn(kotlinx.coroutines.flow.flowOf(completeExpenses))
        
        // When
        val result = expenseRepository.getCompleteExpenses().first()
        
        // Then
        assertEquals(completeExpenses, result)
        verify(mockExpenseDao).getCompleteExpenses()
    }
    
    @Test
    fun `createExpenseFromUPI should create pending expense`() = runTest {
        // Given
        val amount = 150.0
        val merchant = "UPI Merchant"
        val expectedId = 1L
        `when`(mockExpenseDao.insertExpense(any())).thenReturn(expectedId)
        
        // When
        val result = expenseRepository.createExpenseFromUPI(amount, merchant)
        
        // Then
        assertEquals(expectedId, result)
        verify(mockExpenseDao).insertExpense(argThat { expense ->
            expense.amount == amount &&
            expense.merchant == merchant &&
            expense.status == Expense.STATUS_PENDING
        })
    }
    
    @Test
    fun `completeExpense should update details and mark as complete`() = runTest {
        // Given
        val expenseId = 1L
        val description = "Test Description"
        val category = "Food"
        
        // When
        val result = expenseRepository.completeExpense(expenseId, description, category)
        
        // Then
        assertTrue(result)
        verify(mockExpenseDao).updateExpenseDetails(expenseId, description, category, Expense.STATUS_COMPLETE)
    }
    
    @Test
    fun `completeExpense should handle exceptions gracefully`() = runTest {
        // Given
        val expenseId = 1L
        val description = "Test Description"
        val category = "Food"
        doThrow(RuntimeException("Database error")).`when`(mockExpenseDao).updateExpenseDetails(any(), any(), any(), any())
        
        // When
        val result = expenseRepository.completeExpense(expenseId, description, category)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `getTotalAmount should return zero when dao returns null`() = runTest {
        // Given
        `when`(mockExpenseDao.getTotalAmount()).thenReturn(null)
        
        // When
        val result = expenseRepository.getTotalAmount()
        
        // Then
        assertEquals(0.0, result, 0.01)
    }
    
    @Test
    fun `getTotalAmount should return correct amount`() = runTest {
        // Given
        val expectedTotal = 500.0
        `when`(mockExpenseDao.getTotalAmount()).thenReturn(expectedTotal)
        
        // When
        val result = expenseRepository.getTotalAmount()
        
        // Then
        assertEquals(expectedTotal, result, 0.01)
    }
    
    @Test
    fun `clearAllData should call dao deleteAllExpenses`() = runTest {
        // When
        expenseRepository.clearAllData()
        
        // Then
        verify(mockExpenseDao).deleteAllExpenses()
    }
}
