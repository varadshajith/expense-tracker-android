package com.expensetracker.app.utils

import android.content.Context
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.UnknownHostException

object ErrorHandler {
    
    private const val TAG = "ErrorHandler"
    
    /**
     * Handles different types of errors and returns user-friendly messages
     */
    fun getErrorMessage(error: Throwable): String {
        return when (error) {
            is IOException -> "Network error. Please check your connection."
            is UnknownHostException -> "Unable to connect. Please check your internet connection."
            is IllegalArgumentException -> "Invalid data provided: ${error.message}"
            is IllegalStateException -> "Operation not allowed: ${error.message}"
            is SecurityException -> "Permission denied: ${error.message}"
            is OutOfMemoryError -> "Not enough memory to complete this operation."
            is StackOverflowError -> "Operation too complex. Please try again."
            else -> "An unexpected error occurred: ${error.message ?: "Unknown error"}"
        }
    }
    
    /**
     * Logs error with appropriate level
     */
    fun logError(error: Throwable, message: String = "Error occurred") {
        when (error) {
            is SecurityException -> Log.w(TAG, "$message: ${error.message}", error)
            is IllegalArgumentException -> Log.w(TAG, "$message: ${error.message}", error)
            is IllegalStateException -> Log.w(TAG, "$message: ${error.message}", error)
            else -> Log.e(TAG, "$message: ${error.message}", error)
        }
    }
    
    /**
     * Handles database errors specifically
     */
    fun handleDatabaseError(error: Throwable): String {
        return when {
            error.message?.contains("UNIQUE constraint failed") == true -> 
                "This expense already exists."
            error.message?.contains("FOREIGN KEY constraint failed") == true -> 
                "Cannot delete this expense as it's referenced by other data."
            error.message?.contains("NOT NULL constraint failed") == true -> 
                "Required fields are missing."
            error.message?.contains("database is locked") == true -> 
                "Database is busy. Please try again."
            error.message?.contains("no such table") == true -> 
                "Database structure error. Please restart the app."
            else -> "Database error: ${getErrorMessage(error)}"
        }
    }
    
    /**
     * Handles SMS parsing errors
     */
    fun handleSMSError(error: Throwable): String {
        return when {
            error.message?.contains("permission") == true -> 
                "SMS permission required to read transaction messages."
            error.message?.contains("parse") == true -> 
                "Unable to parse SMS message. Please add expense manually."
            else -> "SMS processing error: ${getErrorMessage(error)}"
        }
    }
    
    /**
     * Handles notification errors
     */
    fun handleNotificationError(error: Throwable): String {
        return when {
            error.message?.contains("permission") == true -> 
                "Notification permission required to show expense alerts."
            error.message?.contains("channel") == true -> 
                "Notification channel error. Please restart the app."
            else -> "Notification error: ${getErrorMessage(error)}"
        }
    }
    
    /**
     * Handles validation errors
     */
    fun handleValidationError(error: Throwable): String {
        return when {
            error.message?.contains("amount") == true -> 
                "Please enter a valid amount."
            error.message?.contains("merchant") == true -> 
                "Please enter a valid merchant name."
            error.message?.contains("category") == true -> 
                "Please enter a valid category."
            else -> "Validation error: ${getErrorMessage(error)}"
        }
    }
}

/**
 * Error state for UI components
 */
data class ErrorState(
    val hasError: Boolean = false,
    val message: String = "",
    val type: ErrorType = ErrorType.GENERAL
)

enum class ErrorType {
    GENERAL,
    NETWORK,
    DATABASE,
    VALIDATION,
    PERMISSION,
    SMS,
    NOTIFICATION
}

/**
 * Composable for handling errors with Snackbar
 */
@Composable
fun ErrorSnackbar(
    errorState: ErrorState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onErrorDismissed: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(errorState.hasError) {
        if (errorState.hasError) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = errorState.message,
                    actionLabel = "Dismiss"
                )
                onErrorDismissed()
            }
        }
    }
}

/**
 * Error boundary for catching and handling errors
 */
class ErrorBoundary {
    private var lastError: Throwable? = null
    private var errorCount = 0
    private val maxErrors = 5
    
    fun handleError(error: Throwable): String {
        errorCount++
        lastError = error
        
        if (errorCount > maxErrors) {
            return "Too many errors occurred. Please restart the app."
        }
        
        ErrorHandler.logError(error, "Error boundary caught error")
        return ErrorHandler.getErrorMessage(error)
    }
    
    fun reset() {
        lastError = null
        errorCount = 0
    }
    
    fun hasRecentError(): Boolean {
        return lastError != null && errorCount > 0
    }
}

/**
 * Result wrapper for operations that can fail
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    
    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }
    
    inline fun onError(action: (Throwable) -> Unit): Result<T> {
        if (this is Error) action(exception)
        return this
    }
    
    inline fun <R> map(transform: (T) -> R): Result<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
        }
    }
    
    inline fun <R> flatMap(transform: (T) -> Result<R>): Result<R> {
        return when (this) {
            is Success -> transform(data)
            is Error -> this
        }
    }
}

/**
 * Extension functions for Result
 */
fun <T> Result<T>.getOrElse(defaultValue: T): T {
    return when (this) {
        is Result.Success -> data
        is Result.Error -> defaultValue
    }
}

fun <T> Result<T>.getOrNull(): T? {
    return when (this) {
        is Result.Success -> data
        is Result.Error -> null
    }
}

fun <T> Result<T>.getOrThrow(): T {
    return when (this) {
        is Result.Success -> data
        is Result.Error -> throw exception
    }
}
