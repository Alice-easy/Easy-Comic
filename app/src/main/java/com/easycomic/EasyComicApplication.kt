package com.easycomic

import android.app.Application
import com.easycomic.di.SimplifiedKoinConfiguration

/**
 * 应用程序入口类
 * 
 * 集成了优化后的Koin依赖注入配置
 */
class EasyComicApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化优化后的Koin配置
        val environment = if (BuildConfig.DEBUG) {
            SimplifiedKoinConfiguration.Environment.DEVELOPMENT
        } else {
            SimplifiedKoinConfiguration.Environment.PRODUCTION
        }
        
        SimplifiedKoinConfiguration.initialize(
            context = this,
            environment = environment
        )
        
        // 验证依赖注入是否正常工作
        if (BuildConfig.DEBUG) {
            val isValid = SimplifiedKoinConfiguration.validateDependencies()
            val dependencyCount = SimplifiedKoinConfiguration.getDependencyCount()
            
            android.util.Log.d("EasyComicApp", 
                "Koin初始化完成 - 依赖验证: ${if (isValid) "✅" else "❌"}, 依赖数量: $dependencyCount"
            )
        }
    }
}