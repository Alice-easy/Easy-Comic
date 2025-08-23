package com.easycomic.ui_reader.performance

import android.os.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

/**
 * 阅读器性能监控器
 * 监控启动时间、翻页响应时间、内存使用等关键指标
 */
class ReaderPerformanceMonitor {
    
    private val _performanceMetrics = MutableStateFlow(PerformanceMetrics())
    val performanceMetrics: StateFlow<PerformanceMetrics> = _performanceMetrics.asStateFlow()
    
    private var startupStartTime: Long = 0
    private var pageLoadStartTime: Long = 0
    private var imageLoadStartTime: Long = 0
    
    /**
     * 开始监控启动时间
     */
    fun startStartupMonitoring() {
        startupStartTime = SystemClock.elapsedRealtime()
        Timber.d("开始监控阅读器启动时间")
    }
    
    /**
     * 结束启动时间监控
     */
    fun endStartupMonitoring() {
        if (startupStartTime > 0) {
            val startupTime = SystemClock.elapsedRealtime() - startupStartTime
            updateMetrics { it.copy(startupTime = startupTime) }
            Timber.d("阅读器启动时间: ${startupTime}ms")
            startupStartTime = 0
        }
    }
    
    /**
     * 开始监控翻页时间
     */
    fun startPageLoadMonitoring() {
        pageLoadStartTime = SystemClock.elapsedRealtime()
    }
    
    /**
     * 结束翻页时间监控
     */
    fun endPageLoadMonitoring() {
        if (pageLoadStartTime > 0) {
            val pageLoadTime = SystemClock.elapsedRealtime() - pageLoadStartTime
            updateMetrics { 
                it.copy(
                    averagePageLoadTime = calculateAveragePageLoadTime(pageLoadTime),
                    totalPageLoads = it.totalPageLoads + 1
                )
            }
            Timber.d("翻页时间: ${pageLoadTime}ms")
            pageLoadStartTime = 0
        }
    }
    
    /**
     * 开始监控图片加载时间
     */
    fun startImageLoadMonitoring() {
        imageLoadStartTime = SystemClock.elapsedRealtime()
    }
    
    /**
     * 结束图片加载时间监控
     */
    fun endImageLoadMonitoring() {
        if (imageLoadStartTime > 0) {
            val imageLoadTime = SystemClock.elapsedRealtime() - imageLoadStartTime
            updateMetrics { 
                it.copy(
                    averageImageLoadTime = calculateAverageImageLoadTime(imageLoadTime),
                    totalImageLoads = it.totalImageLoads + 1
                )
            }
            Timber.d("图片加载时间: ${imageLoadTime}ms")
            imageLoadStartTime = 0
        }
    }
    
    /**
     * 记录内存使用情况
     */
    fun recordMemoryUsage(memoryUsage: Long) {
        updateMetrics { 
            it.copy(
                currentMemoryUsage = memoryUsage,
                peakMemoryUsage = maxOf(it.peakMemoryUsage, memoryUsage)
            )
        }
    }
    
    /**
     * 记录手势响应时间
     */
    fun recordGestureResponseTime(responseTime: Long) {
        updateMetrics { 
            it.copy(
                averageGestureResponseTime = calculateAverageGestureResponseTime(responseTime),
                totalGestures = it.totalGestures + 1
            )
        }
    }
    
    /**
     * 重置性能指标
     */
    fun resetMetrics() {
        _performanceMetrics.value = PerformanceMetrics()
        Timber.d("性能指标已重置")
    }
    
    /**
     * 获取性能报告
     */
    fun getPerformanceReport(): String {
        val metrics = _performanceMetrics.value
        return buildString {
            appendLine("=== 阅读器性能报告 ===")
            appendLine("启动时间: ${metrics.startupTime}ms")
            appendLine("平均翻页时间: ${metrics.averagePageLoadTime}ms")
            appendLine("平均图片加载时间: ${metrics.averageImageLoadTime}ms")
            appendLine("平均手势响应时间: ${metrics.averageGestureResponseTime}ms")
            appendLine("当前内存使用: ${metrics.currentMemoryUsage / 1024 / 1024}MB")
            appendLine("峰值内存使用: ${metrics.peakMemoryUsage / 1024 / 1024}MB")
            appendLine("总翻页次数: ${metrics.totalPageLoads}")
            appendLine("总图片加载次数: ${metrics.totalImageLoads}")
            appendLine("总手势次数: ${metrics.totalGestures}")
        }
    }
    
    private fun updateMetrics(update: (PerformanceMetrics) -> PerformanceMetrics) {
        _performanceMetrics.value = update(_performanceMetrics.value)
    }
    
    private fun calculateAveragePageLoadTime(newTime: Long): Long {
        val current = _performanceMetrics.value
        return if (current.totalPageLoads == 0) {
            newTime
        } else {
            (current.averagePageLoadTime * current.totalPageLoads + newTime) / (current.totalPageLoads + 1)
        }
    }
    
    private fun calculateAverageImageLoadTime(newTime: Long): Long {
        val current = _performanceMetrics.value
        return if (current.totalImageLoads == 0) {
            newTime
        } else {
            (current.averageImageLoadTime * current.totalImageLoads + newTime) / (current.totalImageLoads + 1)
        }
    }
    
    private fun calculateAverageGestureResponseTime(newTime: Long): Long {
        val current = _performanceMetrics.value
        return if (current.totalGestures == 0) {
            newTime
        } else {
            (current.averageGestureResponseTime * current.totalGestures + newTime) / (current.totalGestures + 1)
        }
    }
}

/**
 * 性能指标数据类
 */
data class PerformanceMetrics(
    val startupTime: Long = 0,
    val averagePageLoadTime: Long = 0,
    val averageImageLoadTime: Long = 0,
    val averageGestureResponseTime: Long = 0,
    val currentMemoryUsage: Long = 0,
    val peakMemoryUsage: Long = 0,
    val totalPageLoads: Int = 0,
    val totalImageLoads: Int = 0,
    val totalGestures: Int = 0
) {
    /**
     * 检查性能是否达标
     */
    fun isPerformanceGood(): Boolean {
        return startupTime < 1500 && // 启动时间小于1.5秒
               averagePageLoadTime < 100 && // 翻页时间小于100ms
               averageGestureResponseTime < 50 && // 手势响应小于50ms
               peakMemoryUsage < 150 * 1024 * 1024 // 峰值内存小于150MB
    }
    
    /**
     * 获取性能等级
     */
    fun getPerformanceGrade(): PerformanceGrade {
        return when {
            isPerformanceGood() -> PerformanceGrade.EXCELLENT
            startupTime < 2000 && averagePageLoadTime < 150 -> PerformanceGrade.GOOD
            startupTime < 3000 && averagePageLoadTime < 200 -> PerformanceGrade.FAIR
            else -> PerformanceGrade.POOR
        }
    }
}

/**
 * 性能等级枚举
 */
enum class PerformanceGrade {
    EXCELLENT, GOOD, FAIR, POOR
}