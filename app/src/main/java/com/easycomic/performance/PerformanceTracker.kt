package com.easycomic.performance

import android.os.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

/**
 * Phase 4 性能监控系统
 * 用于监控应用关键性能指标和建立基准测试
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
    
    // 性能基准目标 (Phase 4)
    object Targets {
        const val STARTUP_TIME_TARGET_MS = 1500L      // 目标: < 1.5s (优化500ms)
        const val PAGE_TURN_TARGET_MS = 80L           // 目标: < 80ms (优化20ms)
        const val SEARCH_RESPONSE_TARGET_MS = 300L    // 目标: < 300ms (优化200ms)
        const val MEMORY_TARGET_MB = 120L             // 目标: < 120MB (优化30MB)
    }
    
    /**
     * 应用启动时间监控
     */
    fun startupTimingStart() {
        appStartTime = SystemClock.elapsedRealtime()
        Timber.d("📊 Performance: Startup timing started")
    }
    
    fun startupTimingEnd() {
        val startupTime = SystemClock.elapsedRealtime() - appStartTime
        _metrics.value = _metrics.value.copy(startupTime = startupTime)
        
        val status = if (startupTime <= Targets.STARTUP_TIME_TARGET_MS) "✅ PASSED" else "❌ FAILED"
        Timber.i("📊 Performance: Startup completed in ${startupTime}ms $status (Target: ${Targets.STARTUP_TIME_TARGET_MS}ms)")
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
        
        val status = if (pageTurnTime <= Targets.PAGE_TURN_TARGET_MS) "✅ PASSED" else "❌ FAILED"
        Timber.d("📊 Performance: Page transition in ${pageTurnTime}ms $status (Target: ${Targets.PAGE_TURN_TARGET_MS}ms)")
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
        
        val status = if (searchTime <= Targets.SEARCH_RESPONSE_TARGET_MS) "✅ PASSED" else "❌ FAILED"
        Timber.d("📊 Performance: Search completed in ${searchTime}ms $status (Target: ${Targets.SEARCH_RESPONSE_TARGET_MS}ms)")
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
        
        val status = if (usedMemory <= Targets.MEMORY_TARGET_MB) "✅ PASSED" else "❌ FAILED"
        Timber.v("📊 Performance: Memory usage ${usedMemory}MB $status (Target: ${Targets.MEMORY_TARGET_MB}MB)")
    }
    
    /**
     * 性能报告生成
     */
    fun generatePerformanceReport(): String {
        val metrics = _metrics.value
        return buildString {
            appendLine("📊 Performance Report - Phase 4")
            appendLine("========================================")
            appendLine("🚀 Startup Time: ${metrics.startupTime}ms (Target: ${Targets.STARTUP_TIME_TARGET_MS}ms)")
            appendLine("📖 Page Turn: ${metrics.averagePageTurnTime}ms (Target: ${Targets.PAGE_TURN_TARGET_MS}ms)")
            appendLine("🔍 Search: ${metrics.searchResponseTime}ms (Target: ${Targets.SEARCH_RESPONSE_TARGET_MS}ms)")
            appendLine("💾 Memory: ${metrics.memoryUsage}MB (Peak: ${metrics.peakMemoryUsage}MB, Target: ${Targets.MEMORY_TARGET_MB}MB)")
            appendLine()
            appendLine("Overall Status:")
            val allPassed = metrics.startupTime <= Targets.STARTUP_TIME_TARGET_MS &&
                    metrics.averagePageTurnTime <= Targets.PAGE_TURN_TARGET_MS &&
                    metrics.searchResponseTime <= Targets.SEARCH_RESPONSE_TARGET_MS &&
                    metrics.memoryUsage <= Targets.MEMORY_TARGET_MB
            appendLine(if (allPassed) "✅ All targets met" else "❌ Performance optimization needed")
        }
    }
    
    /**
     * 重置性能指标
     */
    fun reset() {
        _metrics.value = PerformanceMetrics()
        Timber.d("📊 Performance: Metrics reset")
    }
}
