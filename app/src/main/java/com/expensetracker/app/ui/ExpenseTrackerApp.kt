package com.expensetracker.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expensetracker.app.ui.navigation.ExpenseNavigation
import com.expensetracker.app.ui.theme.ExpenseTrackerTheme
import com.expensetracker.app.viewmodel.ExpenseViewModel
import com.expensetracker.app.viewmodel.ExpenseViewModelFactory
import com.expensetracker.app.ExpenseTrackerApplication

@Composable
fun ExpenseTrackerApp(
    application: ExpenseTrackerApplication,
    initialExpenseId: Long? = null
) {
    ExpenseTrackerTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            val navController = rememberNavController()
            
            // Create ViewModel with dependency injection
            val expenseViewModel: ExpenseViewModel = viewModel(
                factory = ExpenseViewModelFactory(application.expenseRepository)
            )
            
            ExpenseNavigation(
                navController = navController,
                expenseViewModel = expenseViewModel,
                initialExpenseId = initialExpenseId,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
