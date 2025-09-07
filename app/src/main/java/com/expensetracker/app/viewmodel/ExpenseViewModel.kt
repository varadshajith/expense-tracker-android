package com.expensetracker.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.data.model.Expense
import com.expensetracker.app.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExpenseViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {
    
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()
    
    private val _currentExpense = MutableStateFlow<Expense?>(null)
    val currentExpense: StateFlow<Expense?> = _currentExpense.asStateFlow()
    
    init {
        loadExpenses()
    }
    
    fun loadExpenses() {
        viewModelScope.launch {
            repository.getAllExpenses().collect { expenseList ->
                _expenses.value = expenseList
            }
        }
    }
    
    fun loadExpenseById(id: Long) {
        viewModelScope.launch {
            _currentExpense.value = repository.getExpenseById(id)
        }
    }
    
    fun saveExpense(expense: Expense) {
        viewModelScope.launch {
            val expenseToSave = expense.copy(
                status = if (expense.hasCompleteDetails()) Expense.STATUS_COMPLETE else Expense.STATUS_PENDING
            )
            if (expenseToSave.id == 0L) {
                repository.insertExpense(expenseToSave)
            } else {
                repository.updateExpense(expenseToSave)
            }
        }
    }
    
    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun clearCurrentExpense() {
        _currentExpense.value = null
    }
}

class ExpenseViewModelFactory(
    private val repository: ExpenseRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
