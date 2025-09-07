package com.expensetracker.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.expensetracker.app.ui.screens.ExpenseListScreen
import com.expensetracker.app.ui.screens.EditExpenseScreen
import com.expensetracker.app.viewmodel.ExpenseViewModel

@Composable
fun ExpenseNavigation(
    navController: NavHostController,
    expenseViewModel: ExpenseViewModel,
    initialExpenseId: Long? = null,
    modifier: Modifier = Modifier
) {
    // Navigate to EditExpense screen if initialExpenseId is provided
    LaunchedEffect(initialExpenseId) {
        if (initialExpenseId != null) {
            navController.navigate(ExpenseRoutes.editExpenseRoute(initialExpenseId))
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = ExpenseRoutes.EXPENSE_LIST,
        modifier = modifier
    ) {
        composable(ExpenseRoutes.EXPENSE_LIST) {
            ExpenseListScreen(
                expenseViewModel = expenseViewModel,
                onNavigateToEdit = { expenseId ->
                    navController.navigate(ExpenseRoutes.editExpenseRoute(expenseId))
                }
            )
        }
        
        composable(
            route = "${ExpenseRoutes.EDIT_EXPENSE}/{expenseId}",
            arguments = listOf(
                navArgument("expenseId") {
                    type = NavType.StringType
                    defaultValue = "new"
                }
            )
        ) { backStackEntry ->
            val expenseIdString = backStackEntry.arguments?.getString("expenseId")
            val expenseId = if (expenseIdString == "new") null else expenseIdString?.toLongOrNull()
            
            EditExpenseScreen(
                expenseId = expenseId,
                expenseViewModel = expenseViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
