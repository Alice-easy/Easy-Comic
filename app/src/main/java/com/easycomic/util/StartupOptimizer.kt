package com.easycomic.util

import android.app.Application
import timber.log.Timber

/**
 * 启动优化管理器
 * 负责优化应用启动时间，包括懒加载、预加载策略等
 */
object StartupOptimizer {
    
    private var isInitialized = false
    
    /**
     * 初始化启动优化配置
     */
    fun initialize(application: Application) {
        if (isInitialized) return
        
        Timber.d("启动优化初始化开始")
        
        // 1. 预热关键类
        preloadCriticalClasses()
        
        // 2. 配置懒加载
        configureLazyLoading()
        
        // 3. 优化Timber配置
        optimizeLogging()
        
        isInitialized = true
        Timber.d("启动优化初始化完成")
    }
    
    /**
     * 预热关键类，减少首次使用时的类加载延迟
     */
    private fun preloadCriticalClasses() {
        try {
            // 预热核心类
            Class.forName("com.easycomic.domain.model.Manga")
            Class.forName("com.easycomic.data.entity.MangaEntity")
            Class.forName("kotlinx.coroutines.flow.StateFlow")
            
            Timber.d("关键类预热完成")
        } catch (e: Exception) {
            Timber.w(e, "类预热失败")
        }
    }
    
    /**
     * 配置懒加载策略
     */
    private fun configureLazyLoading() {
        // 设置协程调度器
        System.setProperty("kotlinx.coroutines.scheduler.core.pool.size", "2")
        System.setProperty("kotlinx.coroutines.scheduler.max.pool.size", "8")
        
        Timber.d("懒加载配置完成")
    }
    
    /**
     * 优化日志配置
     */
    private fun optimizeLogging() {
        // 在Release版本中可以禁用或减少日志
        // 暂时使用简化配置
        Timber.plant(Timber.DebugTree())
        
        Timber.d("日志配置优化完成")
    }
    
    /**
     * 获取启动优化建议
     */
    fun getOptimizationRecommendations(): List<String> {
        return listOf(
            "启用R8代码混淆和优化",
            "使用启动配置文件(Baseline Profile)",
            "延迟非关键模块初始化",
            "优化Application.onCreate()执行时间",
            "减少主线程阻塞操作",
            "预加载关键资源",
            "优化依赖注入图构建"
        )
    }
}
