package com.easycomic

import android.app.Application
import com.easycomic.di.KoinModules
import timber.log.Timber

/**
 * Easy Comic应用程序类
 * 负责应用初始化和依赖注入配置
 */
class EasyComicApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化Koin依赖注入
        KoinModules.initializeKoin(this)
        
        // 初始化日志 - 简化版本，暂时不考虑BuildConfig
        try {
            Timber.plant(Timber.DebugTree())
            Timber.d("TEST_APP_CLASS", "!!!!!!!!!!!!!! PRODUCTION EasyComicApplication CREATED !!!!!!!!!!!!!!")
        } catch (e: Exception) {
            // 如果Timber初始化失败，忽略
        }
        
        Timber.d("EasyComicApplication初始化完成")
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        Timber.w("系统内存不足警告")
        
        // 执行内存清理
        System.gc()
    }
    
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        
        when (level) {
            TRIM_MEMORY_RUNNING_MODERATE -> {
                Timber.d("内存使用适中，开始轻度清理")
            }
            TRIM_MEMORY_RUNNING_LOW -> {
                Timber.w("内存使用偏高，开始中度清理")
            }
            TRIM_MEMORY_RUNNING_CRITICAL -> {
                Timber.e("内存使用严重，开始重度清理")
                // 可以在这里清理缓存等非必要资源
                System.gc()
            }
        }
        
        Timber.d("内存整理-$level")
    }
}