package com.easycomic.memory

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicLong

/**
 * 内存监控器
 * 
 * 功能：
 * 1. 实时监控内存使用情况
 * 2. 检测内存泄漏
 * 3. 提供内存使用统计
 * 4. 内存压力预警
 */
class MemoryMonitor(private val context: Context) {
    
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val monitoringScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // 内存状态流
    private val _memoryState = MutableStateFlow(MemoryState.NORMAL)
    val memoryState: StateFlow<MemoryState> = _memoryState.asStateFlow()
    
    // 内存信息流
    private val _memoryInfo = MutableStateFlow(getCurrentMemoryInfo())
    val memoryInfo: StateFlow<MemoryInfo> = _memoryInfo.asStateFlow()
    
    // 监控统计
    private val totalGCCount = AtomicLong(0)
    private val totalGCTime = AtomicLong(0)
    private var lastGCCount = 0L
    private var lastGCTime = 0L
    
    // 监控配置
    private var isMonitoring = false
    private var monitoringJob: Job? = null
    
    companion object {
        private const val MONITORING_INTERVAL = 5000L // 5秒监控间隔
        private const val LOW_MEMORY_THRESHOLD = 0.15f // 低内存阈值
        private const val CRITICAL_MEMORY_THRESHOLD = 0.05f // 临界内存阈值
    }
    
    /**
     * 开始内存监控
     */
    fun startMonitoring() {
        if (isMonitoring) return
        
        isMonitoring = true
        monitoringJob = monitoringScope.launch {
            while (isActive && isMonitoring) {
                try {
                    updateMemoryInfo()
                    updateMemoryState()
                    updateGCStats()
                    delay(MONITORING_INTERVAL)
                } catch (e: Exception) {
                    // 监控异常处理
                    e.printStackTrace()
                }
            }
        }
    }
    
    /**
     * 停止内存监控
     */
    fun stopMonitoring() {
        isMonitoring = false
        monitoringJob?.cancel()
        monitoringJob = null
    }
    
    /**
     * 获取当前内存信息
     */
    fun getCurrentMemoryInfo(): MemoryInfo {
        val runtime = Runtime.getRuntime()
        val activityMemoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(activityMemoryInfo)
        
        return MemoryInfo(
            totalMemory = runtime.totalMemory(),
            availableMemory = activityMemoryInfo.availMem,
            usedMemory = runtime.totalMemory() - runtime.freeMemory(),
            maxMemory = runtime.maxMemory()
        )
    }
    
    /**
     * 获取详细内存统计
     */
    fun getDetailedMemoryStats(): DetailedMemoryStats {
        val runtime = Runtime.getRuntime()
        val memInfo = Debug.MemoryInfo()
        Debug.getMemoryInfo(memInfo)
        
        return DetailedMemoryStats(
            heapSize = runtime.totalMemory(),
            heapAllocated = runtime.totalMemory() - runtime.freeMemory(),
            heapFree = runtime.freeMemory(),
            maxHeap = runtime.maxMemory(),
            nativeHeap = memInfo.nativePrivateDirty * 1024L,
            dalvikHeap = memInfo.dalvikPrivateDirty * 1024L,
            otherMemory = memInfo.otherPrivateDirty * 1024L,
            totalPSS = memInfo.totalPss * 1024L,
            gcCount = totalGCCount.get(),
            gcTime = totalGCTime.get()
        )
    }
    
    /**
     * 检查是否存在内存泄漏风险
     */
    fun checkMemoryLeakRisk(): MemoryLeakRisk {
        val currentInfo = getCurrentMemoryInfo()
        val usageRatio = currentInfo.usedMemory.toFloat() / currentInfo.maxMemory
        val availableRatio = currentInfo.availableMemory.toFloat() / currentInfo.totalMemory
        
        return when {
            usageRatio > 0.9f || availableRatio < 0.05f -> MemoryLeakRisk.HIGH
            usageRatio > 0.8f || availableRatio < 0.1f -> MemoryLeakRisk.MEDIUM
            usageRatio > 0.7f || availableRatio < 0.2f -> MemoryLeakRisk.LOW
            else -> MemoryLeakRisk.NONE
        }
    }
    
    /**
     * 获取内存使用建议
     */
    fun getMemoryOptimizationSuggestions(): List<MemoryOptimizationSuggestion> {
        val suggestions = mutableListOf<MemoryOptimizationSuggestion>()
        val stats = getDetailedMemoryStats()
        val leakRisk = checkMemoryLeakRisk()
        
        // 基于内存使用情况提供建议
        if (stats.heapAllocated > stats.maxHeap * 0.8f) {
            suggestions.add(
                MemoryOptimizationSuggestion(
                    type = SuggestionType.HEAP_USAGE,
                    priority = Priority.HIGH,
                    description = "堆内存使用率过高，建议清理缓存或减少对象创建",
                    action = "执行内存清理或优化对象使用"
                )
            )
        }
        
        if (leakRisk != MemoryLeakRisk.NONE) {
            suggestions.add(
                MemoryOptimizationSuggestion(
                    type = SuggestionType.MEMORY_LEAK,
                    priority = when (leakRisk) {
                        MemoryLeakRisk.HIGH -> Priority.CRITICAL
                        MemoryLeakRisk.MEDIUM -> Priority.HIGH
                        else -> Priority.MEDIUM
                    },
                    description = "检测到潜在内存泄漏风险",
                    action = "检查长期持有的对象引用，使用内存分析工具"
                )
            )
        }
        
        if (stats.gcCount > 100 && stats.gcTime > 1000) {
            suggestions.add(
                MemoryOptimizationSuggestion(
                    type = SuggestionType.GC_PRESSURE,
                    priority = Priority.MEDIUM,
                    description = "GC压力较大，频繁的垃圾回收影响性能",
                    action = "优化对象创建模式，使用对象池"
                )
            )
        }
        
        return suggestions
    }
    
    /**
     * 更新内存信息
     */
    private fun updateMemoryInfo() {
        _memoryInfo.value = getCurrentMemoryInfo()
    }
    
    /**
     * 更新内存状态
     */
    private fun updateMemoryState() {
        val memInfo = _memoryInfo.value
        val availableRatio = memInfo.availableMemory.toFloat() / memInfo.totalMemory
        
        val newState = when {
            availableRatio < CRITICAL_MEMORY_THRESHOLD -> MemoryState.CRITICAL
            availableRatio < LOW_MEMORY_THRESHOLD -> MemoryState.LOW
            else -> MemoryState.NORMAL
        }
        
        if (_memoryState.value != newState) {
            _memoryState.value = newState
        }
    }
    
    /**
     * 更新GC统计信息
     */
    private fun updateGCStats() {
        val currentGCCount = Debug.getGlobalGcInvocationCount()
        val currentGCTime = Debug.getGlobalGcDuration()
        
        if (currentGCCount > lastGCCount) {
            totalGCCount.addAndGet(currentGCCount - lastGCCount)
            totalGCTime.addAndGet(currentGCTime - lastGCTime)
        }
        
        lastGCCount = currentGCCount
        lastGCTime = currentGCTime
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        stopMonitoring()
        monitoringScope.cancel()
    }
}

/**
 * 内存状态枚举
 */
enum class MemoryState {
    NORMAL,     // 正常
    LOW,        // 内存不足
    CRITICAL    // 内存严重不足
}

/**
 * 内存泄漏风险等级
 */
enum class MemoryLeakRisk {
    NONE,       // 无风险
    LOW,        // 低风险
    MEDIUM,     // 中等风险
    HIGH        // 高风险
}

/**
 * 详细内存统计信息
 */
data class DetailedMemoryStats(
    val heapSize: Long,
    val heapAllocated: Long,
    val heapFree: Long,
    val maxHeap: Long,
    val nativeHeap: Long,
    val dalvikHeap: Long,
    val otherMemory: Long,
    val totalPSS: Long,
    val gcCount: Long,
    val gcTime: Long
) {
    val heapUsageRatio: Float
        get() = if (maxHeap > 0) heapAllocated.toFloat() / maxHeap else 0f
}

/**
 * 内存优化建议
 */
data class MemoryOptimizationSuggestion(
    val type: SuggestionType,
    val priority: Priority,
    val description: String,
    val action: String
)

/**
 * 建议类型
 */
enum class SuggestionType {
    HEAP_USAGE,     // 堆内存使用
    MEMORY_LEAK,    // 内存泄漏
    GC_PRESSURE,    // GC压力
    CACHE_SIZE,     // 缓存大小
    OBJECT_POOL     // 对象池
}

/**
 * 优先级
 */
enum class Priority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}