package com.easycomic

import android.app.Application
import com.easycomic.di.KoinModules
import timber.log.Timber

/**
 * 使用 Koin 进行依赖注入的应用类
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
    }
}