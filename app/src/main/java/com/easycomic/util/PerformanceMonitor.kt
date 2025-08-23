package com.easycomic.util

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import android.os.Process
import timber.log.Timber
import kotlin.math.round

/**
 * 性能监控工具类
 * 监控启动时间、内存使用、响应时间等性能指标
 */
object PerformanceMonitor {
    
    private var appStartTime: Long = 0
    private var firstFrameTime: Long = 0
    private var monitoringEnabled = true
    
    // 性能目标常量
    private const val TARGET_COLD_START_MS = 2000L // 目标冷启动时间 < 2秒
    private const val TARGET_HOT_START_MS = 1000L  // 目标热启动时间 < 1秒
    private const val TARGET_PAGE_FLIP_MS = 100L   // 目标翻页响应时间 < 100ms
    private const val TARGET_SEARCH_MS = 500L      // 目标搜索响应时间 < 500ms
    private const val MAX_MEMORY_MB = 150         // 最大内存使用 < 150MB
    
    /**
     * 开始监控应用启动时间
     */
    fun startAppLaunch() {
        appStartTime = System.currentTimeMillis()
    }
    
    /**
     * 记录首帧渲染完成时间
     */
    fun onFirstFrameRendered() {
        firstFrameTime = System.currentTimeMillis()
        val startupTime = firstFrameTime - appStartTime
        
        // 性能监控记录（仅调试模式）
        if (monitoringEnabled) {
            Timber.i("启动时间: ${startupTime}ms")
        }
    }
    
    /**
     * 监控操作响应时间
     */
    fun <T> measureOperation(
        operationName: String,
        targetMs: Long = 500L,
        operation: () -> T
    ): T {
        val startTime = System.currentTimeMillis()
        val result = operation()
        val duration = System.currentTimeMillis() - startTime
        
        if (monitoringEnabled && duration > targetMs) {
            Timber.w("操作响应超标 [$operationName]: ${duration}ms")
        }
        
        return result
    }
    
    /**
     * 监控翻页操作性能
     */
    fun measurePageFlip(operation: () -> Unit) {
        measureOperation("翻页操作", TARGET_PAGE_FLIP_MS) {
            operation()
        }
    }
    
    /**
     * 监控搜索操作性能
     */
    fun measureSearch(operation: () -> Unit) {
        measureOperation("搜索操作", TARGET_SEARCH_MS) {
            operation()
        }
    }
    
    /**
     * 获取当前内存使用情况
     */
    fun getCurrentMemoryUsage(context: Context): MemoryInfo {
        val runtime = Runtime.getRuntime()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        
        // 获取应用内存信息
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        // 获取当前进程内存
        val pids = intArrayOf(Process.myPid())
        val processMemoryInfo = activityManager.getProcessMemoryInfo(pids)[0]
        
        val javaHeapUsed = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
        val javaHeapMax = runtime.maxMemory() / 1024 / 1024
        val nativeHeap = Debug.getNativeHeapAllocatedSize() / 1024 / 1024
        val totalPss = processMemoryInfo.totalPss.toLong() / 1024 // KB转MB
        
        val info = MemoryInfo(
            javaHeapUsedMB = javaHeapUsed,
            javaHeapMaxMB = javaHeapMax,
            nativeHeapMB = nativeHeap,
            totalPssMB = totalPss,
            availableMemoryMB = memoryInfo.availMem / 1024 / 1024,
            isLowMemory = memoryInfo.lowMemory
        )
        
        // 检查内存使用是否超标
        if (monitoringEnabled && totalPss > MAX_MEMORY_MB) {
            Timber.w("内存使用超标: ${totalPss}MB")
        }
        
        return info
    }
    
    /**
     * 定期监控内存使用情况
     */
    fun logMemoryUsage(context: Context, tag: String = "") {
        if (!monitoringEnabled) return
        
        val memInfo = getCurrentMemoryUsage(context)
        Timber.v("内存监控 [$tag]: Total=${memInfo.totalPssMB}MB")
    }
    
    /**
     * 检测内存泄漏
     */
    fun checkForMemoryLeaks(context: Context) {
        val memInfo = getCurrentMemoryUsage(context)
        
        if (memInfo.isLowMemory && monitoringEnabled) {
            Timber.w("检测到内存压力状况")
        }
        
        // Java堆使用率检查
        val heapUsagePercent = (memInfo.javaHeapUsedMB.toDouble() / memInfo.javaHeapMaxMB * 100).toInt()
        if (heapUsagePercent > 80 && monitoringEnabled) {
            Timber.w("Java堆使用率过高: $heapUsagePercent%")
        }
        
        // 自动GC
        if (heapUsagePercent > 70) {
            System.gc()
        }
    }
    
    /**
     * 启用/禁用性能监控
     */
    fun setMonitoringEnabled(enabled: Boolean) {
        monitoringEnabled = enabled
    }
    
    /**
     * 生成性能报告
     */
    fun generatePerformanceReport(context: Context): PerformanceReport {
        val memInfo = getCurrentMemoryUsage(context)
        val startupTime = if (firstFrameTime > 0) firstFrameTime - appStartTime else -1
        
        return PerformanceReport(
            startupTimeMs = startupTime,
            memoryInfo = memInfo,
            startupTarget = startupTime <= TARGET_COLD_START_MS,
            memoryTarget = memInfo.totalPssMB <= MAX_MEMORY_MB,
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * 内存信息数据类
     */
    data class MemoryInfo(
        val javaHeapUsedMB: Long,
        val javaHeapMaxMB: Long,
        val nativeHeapMB: Long,
        val totalPssMB: Long,
        val availableMemoryMB: Long,
        val isLowMemory: Boolean
    )
    
    /**
     * 性能报告数据类
     */
    data class PerformanceReport(
        val startupTimeMs: Long,
        val memoryInfo: MemoryInfo,
        val startupTarget: Boolean,
        val memoryTarget: Boolean,
        val timestamp: Long
    ) {
        fun toLogString(): String {
            return "性能报告:\n" +
                    "启动时间: ${startupTimeMs}ms ${if (startupTarget) "✅" else "❌"}\n" +
                    "内存使用: ${memoryInfo.totalPssMB}MB ${if (memoryTarget) "✅" else "❌"}\n" +
                    "Java堆: ${memoryInfo.javaHeapUsedMB}/${memoryInfo.javaHeapMaxMB}MB\n" +
                    "Native堆: ${memoryInfo.nativeHeapMB}MB\n" +
                    "可用内存: ${memoryInfo.availableMemoryMB}MB"
        }
    }
}
