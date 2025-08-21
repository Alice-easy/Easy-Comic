package com.easycomic.di

import android.content.Context
import com.easycomic.data.di.optimizedDataModule
import com.easycomic.data.di.productionDataModule
import com.easycomic.data.di.developmentDataModule
import com.easycomic.domain.di.domainModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.module.Module

/**
 * 简化的Koin依赖注入配置
 * 
 * 优化点：
 * 1. 统一的模块管理
 * 2. 环境区分配置
 * 3. 清晰的依赖层次
 * 4. 简化的初始化流程
 */
object SimplifiedKoinConfiguration {
    
    /**
     * 应用环境
     */
    enum class Environment {
        DEVELOPMENT,
        PRODUCTION
    }
    
    /**
     * 初始化Koin
     */
    fun initialize(
        context: Context,
        environment: Environment = Environment.PRODUCTION
    ) {
        startKoin {
            androidContext(context)
            androidLogger(
                if (environment == Environment.DEVELOPMENT) Level.DEBUG else Level.ERROR
            )
            modules(getModules(environment))
        }
    }
    
    /**
     * 获取模块列表
     */
    private fun getModules(environment: Environment): List<Module> {
        val baseModules = listOf(
            // 数据层
            when (environment) {
                Environment.DEVELOPMENT -> developmentDataModule
                Environment.PRODUCTION -> productionDataModule
            },
            
            // 业务层
            domainModule,
            
            // UI层
            getUiModule()
        )
        
        return baseModules
    }
    
    /**
     * 获取UI模块（处理多个同名模块的问题）
     */
    private fun getUiModule(): Module {
        return try {
            // 尝试使用最新的UI模块
            com.easycomic.ui.di.uiModule
        } catch (e: Exception) {
            // 如果失败，使用备用模块
            org.koin.dsl.module {
                // 基础UI配置
            }
        }
    }
    
    /**
     * 验证依赖注入是否正常
     */
    fun validateDependencies(): Boolean {
        return try {
            val koin = org.koin.core.context.GlobalContext.get()
            
            // 验证关键依赖
            koin.get<com.easycomic.domain.repository.MangaRepository>()
            koin.get<com.easycomic.domain.repository.BookmarkRepository>()
            koin.get<com.easycomic.domain.usecase.MangaUseCases>()
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取依赖统计信息
     */
    fun getDependencyCount(): Int {
        return try {
            val koin = org.koin.core.context.GlobalContext.get()
            koin.instanceRegistry.size()
        } catch (e: Exception) {
            0
        }
    }
}