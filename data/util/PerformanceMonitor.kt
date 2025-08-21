package com.easycomic.data.util

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
    private var isMonitoringEnabled = true
    
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
        Timber.d("应用启动监控开始: $appStartTime")
    }
    
    /**
     * 记录首帧渲染完成时间
     */
    fun onFirstFrameRendered() {
        firstFrameTime = System.currentTimeMillis()
        val startupTime = firstFrameTime - appStartTime
        
        Timber.i("启动时间监控 - 首帧渲染: ${startupTime}ms")
        
        // 检查是否达标
        val targetTime = TARGET_COLD_START_MS
        if (startupTime > targetTime) {
            Timber.w("启动时间超标: ${startupTime}ms > ${targetTime}ms")
        } else {
            Timber.i("启动时间达标: ${startupTime}ms < ${targetTime}ms")
        }
    }
    
    /**
     * 监控操作响应时间
     */
    inline fun <T> measureOperation(
        operationName: String,
        targetMs: Long = 500L,
        operation: () -> T
    ): T {
        val startTime = System.currentTimeMillis()
        val result = operation()
        val duration = System.currentTimeMillis() - startTime
        
        if (duration > targetMs) {
            Timber.w("操作响应超标 [$operationName]: ${duration}ms > ${targetMs}ms")
        } else {
            Timber.d("操作响应达标 [$operationName]: ${duration}ms")
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
        if (totalPss > MAX_MEMORY_MB) {
            Timber.w("内存使用超标: ${totalPss}MB > ${MAX_MEMORY_MB}MB")
        }
        
        return info
    }
    
    /**
     * 定期监控内存使用情况
     */
    fun logMemoryUsage(context: Context, tag: String = "") {
        if (!isMonitoringEnabled) return
        
        val memInfo = getCurrentMemoryUsage(context)
        Timber.d("内存监控 [$tag]: Java=${memInfo.javaHeapUsedMB}/${memInfo.javaHeapMaxMB}MB, " +
                "Native=${memInfo.nativeHeapMB}MB, Total=${memInfo.totalPssMB}MB, " +
                "Available=${memInfo.availableMemoryMB}MB, LowMemory=${memInfo.isLowMemory}")
    }
    
    /**
     * 检测内存泄漏
     */
    fun checkForMemoryLeaks(context: Context) {
        val memInfo = getCurrentMemoryUsage(context)
        
        if (memInfo.isLowMemory) {
            Timber.w("检测到内存压力状况，建议进行内存清理")
        }
        
        // Java堆使用率检查
        val heapUsagePercent = (memInfo.javaHeapUsedMB.toDouble() / memInfo.javaHeapMaxMB * 100).toInt()
        if (heapUsagePercent > 80) {
            Timber.w("Java堆使用率过高: $heapUsagePercent%")
        }
        
        // 建议GC
        if (heapUsagePercent > 70) {
            System.gc()
            Timber.d("建议GC清理内存")
        }
    }
    
    /**
     * 启用/禁用性能监控
     */
    fun setMonitoringEnabled(enabled: Boolean) {
        isMonitoringEnabled = enabled
        Timber.d("性能监控${if (enabled) "启用" else "禁用"}")
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
