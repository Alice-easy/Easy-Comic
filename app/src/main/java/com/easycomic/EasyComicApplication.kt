package com.easycomic

import android.app.Application
import com.easycomic.di.KoinModules
import timber.log.Timber

/**
 * 应用程序入口类 - 简化版本
 * 
 * 用于虚拟机测试
 */
class EasyComicApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化日志系统
        initializeLogging()
        
        // 初始化Koin依赖注入
        try {
            KoinModules.initializeKoin(this)
            Timber.d("Koin初始化完成")
        } catch (e: Exception) {
            Timber.e(e, "Koin初始化失败")
        }
        
        Timber.i("Easy Comic 虚拟机测试版本启动")
    }
    
    /**
     * 初始化日志系统
     */
    private fun initializeLogging() {
        // 简化版本：总是使用Debug模式
        Timber.plant(Timber.DebugTree())
        
        Timber.i("Easy Comic启动 - 虚拟机测试版本")
    }
}