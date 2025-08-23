package com.easycomic.performance

import android.os.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

/**
 * 性能监控系统
 * 用于监控应用关键性能指标
 */
object PerformanceTracker {
    
    data class PerformanceMetrics(
        val startupTime: Long = 0L,
        val averagePageTurnTime: Long = 0L,
        val searchResponseTime: Long = 0L,
        val memoryUsage: Long = 0L,
        val peakMemoryUsage: Long = 0L
    )
    
    private val _metrics = MutableStateFlow(PerformanceMetrics())
    val metrics: StateFlow<PerformanceMetrics> = _metrics.asStateFlow()
    
    private var appStartTime: Long = 0L
    private var lastPageTurnStart: Long = 0L
    private var lastSearchStart: Long = 0L
    
    // 性能基准目标
    object Targets {
        const val STARTUP_TIME_TARGET_MS = 1500L
        const val PAGE_TURN_TARGET_MS = 80L
        const val SEARCH_RESPONSE_TARGET_MS = 300L
        const val MEMORY_TARGET_MB = 120L
    }
    
    /**
     * 应用启动时间监控
     */
    fun startupTimingStart() {
        appStartTime = SystemClock.elapsedRealtime()
    }
    
    fun startupTimingEnd() {
        val startupTime = SystemClock.elapsedRealtime() - appStartTime
        _metrics.value = _metrics.value.copy(startupTime = startupTime)
    }
    
    /**
     * 翻页响应时间监控
     */
    fun pageTransitionStart() {
        lastPageTurnStart = SystemClock.elapsedRealtime()
    }
    
    fun pageTransitionEnd() {
        val pageTurnTime = SystemClock.elapsedRealtime() - lastPageTurnStart
        _metrics.value = _metrics.value.copy(averagePageTurnTime = pageTurnTime)
    }
    
    /**
     * 搜索响应时间监控
     */
    fun searchStart() {
        lastSearchStart = SystemClock.elapsedRealtime()
    }
    
    fun searchEnd() {
        val searchTime = SystemClock.elapsedRealtime() - lastSearchStart
        _metrics.value = _metrics.value.copy(searchResponseTime = searchTime)
    }
    
    /**
     * 内存使用监控
     */
    fun updateMemoryUsage() {
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024) // MB
        val currentMetrics = _metrics.value
        
        _metrics.value = currentMetrics.copy(
            memoryUsage = usedMemory,
            peakMemoryUsage = maxOf(usedMemory, currentMetrics.peakMemoryUsage)
        )
    }
    
    /**
     * 性能报告生成
     */
    fun generatePerformanceReport(): String {
        val metrics = _metrics.value
        return buildString {
            appendLine("📊 性能报告")
            appendLine("========================================")
            appendLine("🚀 启动时间: ${metrics.startupTime}ms")
            appendLine("📖 翻页时间: ${metrics.averagePageTurnTime}ms")
            appendLine("🔍 搜索时间: ${metrics.searchResponseTime}ms")
            appendLine("💾 内存使用: ${metrics.memoryUsage}MB (峰值: ${metrics.peakMemoryUsage}MB)")
        }
    }
    
    /**
     * 重置性能指标
     */
    fun reset() {
        _metrics.value = PerformanceMetrics()
    }
}
