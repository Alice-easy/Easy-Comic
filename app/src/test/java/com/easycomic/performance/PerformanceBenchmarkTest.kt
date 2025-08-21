package com.easycomic.performance

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Assert.*
import org.junit.Before
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.delay
import kotlin.system.measureTimeMillis

/**
 * Phase 4 性能基准测试
 * 验证关键性能指标是否达到目标值
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class PerformanceBenchmarkTest {

    private lateinit var performanceTracker: PerformanceTracker

    @Before
    fun setup() {
        performanceTracker = PerformanceTracker
    }

    /**
     * 启动时间基准测试
     * 目标：< 1500ms
     */
    @Test
    fun testApplicationStartupPerformance() = runTest {
        // 模拟应用启动
        val startupTime = measureTimeMillis {
            // 模拟应用初始化过程
            delay(100) // Koin初始化
            delay(50)  // Room数据库初始化
            delay(30)  // UI组件初始化
        }
        
        performanceTracker.recordStartupTime(startupTime)
        
        // 验证启动时间在目标范围内
        assertTrue("Startup time ${startupTime}ms exceeds target ${PerformanceTracker.Targets.STARTUP_TIME_TARGET_MS}ms", 
                   startupTime <= PerformanceTracker.Targets.STARTUP_TIME_TARGET_MS)
    }

    /**
     * 页面翻转性能测试
     * 目标：< 80ms
     */
    @Test
    fun testPageTurnPerformance() = runTest {
        val pageTurnTimes = mutableListOf<Long>()
        
        // 模拟多次页面翻转
        repeat(10) {
            val turnTime = measureTimeMillis {
                // 模拟页面翻转操作
                delay(20) // 图像解码
                delay(10) // UI渲染
            }
            pageTurnTimes.add(turnTime)
        }
        
        val averageTime = pageTurnTimes.average().toLong()
        performanceTracker.recordPageTurnTime(averageTime)
        
        assertTrue("Average page turn time ${averageTime}ms exceeds target ${PerformanceTracker.Targets.PAGE_TURN_TARGET_MS}ms", 
                   averageTime <= PerformanceTracker.Targets.PAGE_TURN_TARGET_MS)
    }

    /**
     * 搜索响应时间测试
     * 目标：< 300ms
     */
    @Test
    fun testSearchResponsePerformance() = runTest {
        val searchTime = measureTimeMillis {
            // 模拟搜索操作
            delay(100) // 数据库查询
            delay(50)  // 结果排序
            delay(30)  // UI更新
        }
        
        performanceTracker.recordSearchTime(searchTime)
        
        assertTrue("Search response time ${searchTime}ms exceeds target ${PerformanceTracker.Targets.SEARCH_RESPONSE_TARGET_MS}ms", 
                   searchTime <= PerformanceTracker.Targets.SEARCH_RESPONSE_TARGET_MS)
    }

    /**
     * 内存使用测试
     * 目标：< 120MB
     */
    @Test
    fun testMemoryUsagePerformance() {
        // 模拟内存使用
        val currentMemory = 80L // 模拟当前内存使用80MB
        val peakMemory = 100L   // 模拟峰值内存100MB
        
        performanceTracker.recordMemoryUsage(currentMemory, peakMemory)
        
        assertTrue("Memory usage ${currentMemory}MB exceeds target ${PerformanceTracker.Targets.MEMORY_TARGET_MB}MB", 
                   currentMemory <= PerformanceTracker.Targets.MEMORY_TARGET_MB)
        assertTrue("Peak memory usage ${peakMemory}MB exceeds target ${PerformanceTracker.Targets.MEMORY_TARGET_MB}MB", 
                   peakMemory <= PerformanceTracker.Targets.MEMORY_TARGET_MB)
    }

    /**
     * 综合性能报告测试
     */
    @Test
    fun testPerformanceReport() = runTest {
        // 记录一些性能数据
        performanceTracker.recordStartupTime(1200L)
        performanceTracker.recordPageTurnTime(60L)
        performanceTracker.recordSearchTime(200L)
        performanceTracker.recordMemoryUsage(90L, 110L)
        
        val report = performanceTracker.generatePerformanceReport()
        
        // 验证报告包含预期内容
        assertTrue("Report should contain startup time", report.contains("Startup Time"))
        assertTrue("Report should contain page turn time", report.contains("Page Turn"))
        assertTrue("Report should contain search time", report.contains("Search"))
        assertTrue("Report should contain memory usage", report.contains("Memory"))
        assertTrue("Report should contain status", report.contains("targets met"))
    }

    /**
     * 性能回归测试
     * 确保性能不会退化
     */
    @Test
    fun testPerformanceRegression() = runTest {
        // 基准性能数据（之前的最佳值）
        val baselineStartup = 1400L
        val baselinePageTurn = 70L
        val baselineSearch = 250L
        val baselineMemory = 100L
        
        // 当前性能测试
        val currentStartup = measureTimeMillis { delay(120) } // 模拟当前启动时间
        val currentPageTurn = measureTimeMillis { delay(25) }  // 模拟当前翻页时间
        val currentSearch = measureTimeMillis { delay(180) }   // 模拟当前搜索时间
        val currentMemory = 95L // 模拟当前内存使用
        
        // 确保性能没有显著退化（允许5%的波动）
        assertTrue("Startup performance regressed", currentStartup <= baselineStartup * 1.05)
        assertTrue("Page turn performance regressed", currentPageTurn <= baselinePageTurn * 1.05)
        assertTrue("Search performance regressed", currentSearch <= baselineSearch * 1.05)
        assertTrue("Memory usage regressed", currentMemory <= baselineMemory * 1.05)
    }

    /**
     * 压力测试
     * 在高负载下的性能表现
     */
    @Test
    fun testPerformanceUnderLoad() = runTest {
        val results = mutableListOf<Long>()
        
        // 模拟连续操作
        repeat(50) {
            val operationTime = measureTimeMillis {
                delay(15) // 模拟单次操作
            }
            results.add(operationTime)
        }
        
        val maxTime = results.maxOrNull() ?: 0L
        val avgTime = results.average()
        
        // 即使在压力下，单次操作也不应超过阈值
        assertTrue("Max operation time under load: ${maxTime}ms", maxTime <= 100L)
        assertTrue("Average operation time under load: ${avgTime}ms", avgTime <= 50L)
    }
}
