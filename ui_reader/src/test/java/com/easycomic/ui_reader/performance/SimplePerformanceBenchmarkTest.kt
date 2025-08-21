package com.easycomic.ui_reader.performance

import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * 简化的性能基准测试
 */
class SimplePerformanceBenchmarkTest {

    private lateinit var performanceMonitor: SimplePerformanceMonitor

    @Before
    fun setup() {
        performanceMonitor = SimplePerformanceMonitor()
    }

    @Test
    fun `测试启动时间监控`() {
        // Given
        performanceMonitor.startStartupMonitoring()
        
        // When
        Thread.sleep(100) // 模拟启动时间
        val startupTime = performanceMonitor.endStartupMonitoring()
        
        // Then
        assertTrue("启动时间应该大于0", startupTime > 0)
        assertTrue("启动时间应该合理", startupTime >= 100)
    }

    @Test
    fun `测试翻页时间监控`() {
        // Given
        performanceMonitor.startPageLoadMonitoring()
        
        // When
        Thread.sleep(50) // 模拟翻页时间
        val pageLoadTime = performanceMonitor.endPageLoadMonitoring()
        
        // Then
        assertTrue("翻页时间应该大于0", pageLoadTime > 0)
        assertTrue("翻页时间应该合理", pageLoadTime >= 50)
    }

    @Test
    fun `测试平均翻页时间计算`() {
        // Given
        val testTimes = listOf(50L, 60L, 70L)
        
        // When
        testTimes.forEach { expectedTime ->
            performanceMonitor.startPageLoadMonitoring()
            Thread.sleep(expectedTime)
            performanceMonitor.endPageLoadMonitoring()
        }
        
        val averageTime = performanceMonitor.getAveragePageLoadTime()
        
        // Then
        assertTrue("平均时间应该在合理范围内", averageTime > 0)
        assertTrue("平均时间应该接近预期", averageTime >= 50)
    }

    @Test
    fun `测试性能基准检查`() {
        // Test startup time benchmark
        assertTrue("快速启动应该达标", 
            PerformanceBenchmarks.isStartupTimeGood(1000L))
        assertFalse("慢启动不应该达标", 
            PerformanceBenchmarks.isStartupTimeGood(2000L))
        
        // Test page load time benchmark
        assertTrue("快速翻页应该达标", 
            PerformanceBenchmarks.isPageLoadTimeGood(80L))
        assertFalse("慢翻页不应该达标", 
            PerformanceBenchmarks.isPageLoadTimeGood(150L))
        
        // Test gesture response time benchmark
        assertTrue("快速手势响应应该达标", 
            PerformanceBenchmarks.isGestureResponseTimeGood(30L))
        assertFalse("慢手势响应不应该达标", 
            PerformanceBenchmarks.isGestureResponseTimeGood(80L))
    }

    @Test
    fun `测试性能报告生成`() {
        // Given
        performanceMonitor.startPageLoadMonitoring()
        Thread.sleep(50)
        performanceMonitor.endPageLoadMonitoring()
        
        // When
        val report = performanceMonitor.getPerformanceReport()
        
        // Then
        assertTrue("报告应该包含标题", report.contains("简化性能报告"))
        assertTrue("报告应该包含翻页次数", report.contains("总翻页次数"))
        assertTrue("报告应该包含平均时间", report.contains("平均翻页时间"))
    }

    @Test
    fun `测试重置功能`() {
        // Given
        performanceMonitor.startPageLoadMonitoring()
        Thread.sleep(50)
        performanceMonitor.endPageLoadMonitoring()
        
        // When
        performanceMonitor.reset()
        val averageTime = performanceMonitor.getAveragePageLoadTime()
        
        // Then
        assertEquals("重置后平均时间应该为0", 0L, averageTime)
    }
}