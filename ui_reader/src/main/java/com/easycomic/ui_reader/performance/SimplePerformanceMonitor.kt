package com.easycomic.ui_reader.performance

/**
 * 简化的性能监控器
 * 用于基本的性能指标收集和报告
 */
class SimplePerformanceMonitor {
    
    private var startupStartTime: Long = 0
    private var pageLoadStartTime: Long = 0
    private var totalPageLoads: Int = 0
    private var totalPageLoadTime: Long = 0
    
    /**
     * 开始监控启动时间
     */
    fun startStartupMonitoring() {
        startupStartTime = System.currentTimeMillis()
    }
    
    /**
     * 结束启动时间监控
     */
    fun endStartupMonitoring(): Long {
        return if (startupStartTime > 0) {
            val duration = System.currentTimeMillis() - startupStartTime
            startupStartTime = 0
            duration
        } else {
            0L
        }
    }
    
    /**
     * 开始监控翻页时间
     */
    fun startPageLoadMonitoring() {
        pageLoadStartTime = System.currentTimeMillis()
    }
    
    /**
     * 结束翻页时间监控
     */
    fun endPageLoadMonitoring(): Long {
        return if (pageLoadStartTime > 0) {
            val duration = System.currentTimeMillis() - pageLoadStartTime
            totalPageLoads++
            totalPageLoadTime += duration
            pageLoadStartTime = 0
            duration
        } else {
            0L
        }
    }
    
    /**
     * 获取平均翻页时间
     */
    fun getAveragePageLoadTime(): Long {
        return if (totalPageLoads > 0) {
            totalPageLoadTime / totalPageLoads
        } else {
            0L
        }
    }
    
    /**
     * 获取性能报告
     */
    fun getPerformanceReport(): String {
        return buildString {
            appendLine("=== 简化性能报告 ===")
            appendLine("总翻页次数: $totalPageLoads")
            appendLine("平均翻页时间: ${getAveragePageLoadTime()}ms")
            appendLine("总翻页时间: ${totalPageLoadTime}ms")
        }
    }
    
    /**
     * 重置统计数据
     */
    fun reset() {
        startupStartTime = 0
        pageLoadStartTime = 0
        totalPageLoads = 0
        totalPageLoadTime = 0
    }
}

/**
 * 性能基准常量
 */
object PerformanceBenchmarks {
    const val TARGET_STARTUP_TIME = 1500L // 1.5秒
    const val TARGET_PAGE_LOAD_TIME = 100L // 100毫秒
    const val TARGET_GESTURE_RESPONSE_TIME = 50L // 50毫秒
    
    /**
     * 检查启动时间是否达标
     */
    fun isStartupTimeGood(time: Long): Boolean = time <= TARGET_STARTUP_TIME
    
    /**
     * 检查翻页时间是否达标
     */
    fun isPageLoadTimeGood(time: Long): Boolean = time <= TARGET_PAGE_LOAD_TIME
    
    /**
     * 检查手势响应时间是否达标
     */
    fun isGestureResponseTimeGood(time: Long): Boolean = time <= TARGET_GESTURE_RESPONSE_TIME
}