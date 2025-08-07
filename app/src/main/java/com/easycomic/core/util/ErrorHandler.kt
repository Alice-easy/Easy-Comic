package com.easycomic.core.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.easycomic.R

@Singleton
class ErrorHandler @Inject constructor() {
    
    fun getReadableErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is java.io.FileNotFoundException -> "File not found"
            is java.io.IOException -> "File access error"
            is java.lang.OutOfMemoryError -> "Out of memory"
            is java.lang.SecurityException -> "Permission denied"
            is java.net.UnknownHostException -> "Network error - no internet connection"
            is java.net.SocketTimeoutException -> "Network timeout"
            is java.net.ConnectException -> "Connection failed"
            is org.json.JSONException -> "Data parsing error"
            is java.lang.IllegalArgumentException -> "Invalid input"
            is java.lang.IllegalStateException -> "Invalid state"
            is kotlin.KotlinNullPointerException -> "Unexpected null value"
            else -> throwable.message ?: "Unknown error occurred"
        }
    }
    
    fun isRecoverableError(throwable: Throwable): Boolean {
        return when (throwable) {
            is java.net.UnknownHostException,
            is java.net.SocketTimeoutException,
            is java.net.ConnectException,
            is java.io.IOException -> true
            else -> false
        }
    }
    
    fun shouldShowUserError(throwable: Throwable): Boolean {
        return when (throwable) {
            is java.lang.OutOfMemoryError,
            is java.lang.SecurityException,
            is java.lang.IllegalArgumentException,
            is java.lang.IllegalStateException,
            is kotlin.KotlinNullPointerException -> false
            else -> true
        }
    }
    
    fun getFileOperationError(throwable: Throwable): String {
        return when (throwable) {
            is java.io.FileNotFoundException -> "The selected file was not found"
            is java.io.IOException -> "Unable to read the file"
            is java.lang.SecurityException -> "Permission denied to access the file"
            is java.lang.OutOfMemoryError -> "File is too large to process"
            else -> "Failed to process the file"
        }
    }
    
    fun getNetworkError(throwable: Throwable): String {
        return when (throwable) {
            is java.net.UnknownHostException -> "No internet connection"
            is java.net.SocketTimeoutException -> "Request timed out"
            is java.net.ConnectException -> "Unable to connect to server"
            else -> "Network error occurred"
        }
    }
    
    fun getDatabaseError(throwable: Throwable): String {
        return when (throwable) {
            is android.database.sqlite.SQLiteException -> "Database error"
            is java.lang.IllegalStateException -> "Database locked or busy"
            else -> "Data storage error"
        }
    }
    
    fun getComicFileError(throwable: Throwable): String {
        return when (throwable) {
            is java.util.zip.ZipException -> "Invalid ZIP file"
            is java.io.IOException -> "Failed to read comic file"
            is java.lang.OutOfMemoryError -> "Comic file is too large"
            else -> "Unsupported comic file format"
        }
    }
}

sealed class AppError(
    val message: String,
    val cause: Throwable? = null,
    val isRecoverable: Boolean = false
) {
    class FileError(message: String, cause: Throwable? = null) : AppError(message, cause, true)
    class NetworkError(message: String, cause: Throwable? = null) : AppError(message, cause, true)
    class DatabaseError(message: String, cause: Throwable? = null) : AppError(message, cause, false)
    class ComicFileError(message: String, cause: Throwable? = null) : AppError(message, cause, false)
    class UserError(message: String) : AppError(message, null, true)
    class SystemError(message: String, cause: Throwable? = null) : AppError(message, cause, false)
}

@Composable
fun ErrorUtils.getUserFriendlyError(error: AppError): String {
    return when (error) {
        is AppError.FileError -> stringResource(R.string.file_processing_error)
        is AppError.NetworkError -> stringResource(R.string.common_error)
        is AppError.DatabaseError -> "Data storage error"
        is AppError.ComicFileError -> stringResource(R.string.file_unsupported_format)
        is AppError.UserError -> error.message
        is AppError.SystemError -> stringResource(R.string.common_error)
    }
}