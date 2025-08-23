package com.easycomic.performance

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * 性能跟踪器
 * 用于监控应用性能指标
 */
class PerformanceTracker {
    
    private val eventCounts = ConcurrentHashMap<String, AtomicLong>()
    private val eventTimes = ConcurrentHashMap<String, Long>()
    private val scope = CoroutineScope(Dispatchers.IO)
    
    /**
     * 记录事件
     */
    fun logEvent(eventName: String) {
        scope.launch {
            eventCounts.computeIfAbsent(eventName) { AtomicLong(0) }.incrementAndGet()
            Log.d("PerformanceTracker", "Event: $eventName")
        }
    }
    
    /**
     * 开始计时
     */
    fun startTiming(operationName: String) {
        eventTimes[operationName] = System.currentTimeMillis()
    }
    
    /**
     * 结束计时并记录
     */
    fun endTiming(operationName: String) {
        val startTime = eventTimes.remove(operationName)
        if (startTime != null) {
            val duration = System.currentTimeMillis() - startTime
            Log.d("PerformanceTracker", "Operation: $operationName took ${duration}ms")
        }
    }
    
    /**
     * 获取性能统计
     */
    fun getStats(): Map<String, Long> {
        return eventCounts.mapValues { it.value.get() }
    }
}