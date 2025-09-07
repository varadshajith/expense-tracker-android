package com.expensetracker.app.ui.navigation

object ExpenseRoutes {
    const val EXPENSE_LIST = "expense_list"
    const val EDIT_EXPENSE = "edit_expense"
    
    fun editExpenseRoute(expenseId: Long?): String {
        return if (expenseId != null) {
            "$EDIT_EXPENSE/$expenseId"
        } else {
            "$EDIT_EXPENSE/new"
        }
    }
}
