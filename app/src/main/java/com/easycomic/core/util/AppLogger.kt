package com.easycomic.core.util

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLogger @Inject constructor() {
    
    companion object {
        private const val TAG = "EasyComic"
        private const val DEBUG = true
    }
    
    fun d(message: String, throwable: Throwable? = null) {
        if (DEBUG) {
            Log.d(TAG, message, throwable)
        }
    }
    
    fun i(message: String, throwable: Throwable? = null) {
        Log.i(TAG, message, throwable)
    }
    
    fun w(message: String, throwable: Throwable? = null) {
        Log.w(TAG, message, throwable)
    }
    
    fun e(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }
    
    fun wtf(message: String, throwable: Throwable? = null) {
        Log.wtf(TAG, message, throwable)
    }
    
    fun logNetworkError(endpoint: String, error: Throwable) {
        e("Network error for endpoint: $endpoint", error)
    }
    
    fun logDatabaseOperation(operation: String, table: String, success: Boolean, error: Throwable? = null) {
        val message = "Database operation: $operation on table: $table - Success: $success"
        if (success) {
            i(message)
        } else {
            e(message, error)
        }
    }
    
    fun logFileOperation(operation: String, filePath: String, success: Boolean, error: Throwable? = null) {
        val message = "File operation: $operation on file: $filePath - Success: $success"
        if (success) {
            i(message)
        } else {
            e(message, error)
        }
    }
    
    fun logUserAction(action: String, details: String? = null) {
        val message = "User action: $action${details?.let { " - $it" } ?: ""}"
        i(message)
    }
    
    fun logPerformanceMetric(metric: String, value: Long, unit: String = "ms") {
        d("Performance metric: $metric = $value $unit")
    }
    
    fun logMemoryUsage() {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val usedMB = usedMemory / (1024 * 1024)
        val maxMB = maxMemory / (1024 * 1024)
        d("Memory usage: $usedMB MB / $maxMB MB")
    }
    
    fun logCrash(error: Throwable, additionalInfo: String? = null) {
        e("App crash: ${error.message}", error)
        additionalInfo?.let { e("Additional info: $it") }
    }
}