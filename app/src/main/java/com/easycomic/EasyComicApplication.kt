package com.easycomic

import android.app.Application
import com.easycomic.di.SimplifiedKoinConfiguration
import com.easycomic.monitoring.CrashReportingManager
import timber.log.Timber

/**
 * 应用程序入口类
 * 
 * 集成了优化后的Koin依赖注入配置
 */
class EasyComicApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化日志系统
        initializeLogging()
        
        // 初始化崩溃报告和监控
        initializeCrashReporting()
        
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
            
            Timber.d(
                "Koin初始化完成 - 依赖验证: ${if (isValid) "✅" else "❌"}, 依赖数量: $dependencyCount"
            )
        }
        
        // 设置应用信息用于崩溃报告
        setupCrashReportingMetadata()
    }
    
    /**
     * 初始化日志系统
     */
    private fun initializeLogging() {
        if (BuildConfig.DEBUG) {
            // Debug模式：显示详细日志
            Timber.plant(Timber.DebugTree())
        } else {
            // Release模式：只记录错误和警告
            Timber.plant(object : Timber.Tree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    if (priority >= android.util.Log.WARN) {
                        // 记录到崩溃报告系统
                        CrashReportingManager.logError(
                            message = "[$tag] $message",
                            throwable = t
                        )
                    }
                }
            })
        }
        
        Timber.i("Easy Comic启动 - 版本: ${BuildConfig.VERSION_NAME}")
    }
    
    /**
     * 初始化崩溃报告系统
     */
    private fun initializeCrashReporting() {
        try {
            CrashReportingManager.initialize(
                context = this,
                isDebug = BuildConfig.DEBUG
            )
            
            Timber.d("崩溃报告系统初始化完成")
        } catch (e: Exception) {
            Timber.e(e, "崩溃报告系统初始化失败")
        }
    }
    
    /**
     * 设置崩溃报告的元数据
     */
    private fun setupCrashReportingMetadata() {
        try {
            CrashReportingManager.setCustomKey("app_version", BuildConfig.VERSION_NAME)
            CrashReportingManager.setCustomKey("app_build", BuildConfig.VERSION_CODE.toString())
            CrashReportingManager.setCustomKey("debug_mode", BuildConfig.DEBUG.toString())
            
            // 可以添加更多有用的元数据
            CrashReportingManager.setCustomKey("android_version", android.os.Build.VERSION.RELEASE)
            CrashReportingManager.setCustomKey("device_model", android.os.Build.MODEL)
            CrashReportingManager.setCustomKey("device_manufacturer", android.os.Build.MANUFACTURER)
            
            Timber.d("崩溃报告元数据设置完成")
        } catch (e: Exception) {
            Timber.e(e, "设置崩溃报告元数据失败")
        }
    }
}