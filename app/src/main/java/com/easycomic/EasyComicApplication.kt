package com.easycomic

import android.app.Application
import timber.log.Timber

/**
 * 简化的应用类，不使用依赖注入
 */
class EasyComicApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化日志 - 简化版本，暂时不考虑BuildConfig
        try {
            Timber.plant(Timber.DebugTree())
            Timber.d("EasyComic Application started")
        } catch (e: Exception) {
            // 如果Timber初始化失败，忽略
        }
    }
}