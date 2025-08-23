package com.easycomic

import android.app.Application
import com.easycomic.di.KoinModules
import timber.log.Timber

/**
 * Easy Comic 应用程序入口类
 * 
 * 现代化Android漫画阅读器
 */
class EasyComicApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化日志系统
        initializeLogging()
        
        // 初始化Koin依赖注入
        KoinModules.initializeKoin(this)
    }
    
    /**
     * 初始化日志系统
     */
    private fun initializeLogging() {
        // 仅在debug模式下启用日志
        Timber.plant(Timber.DebugTree())
    }
}