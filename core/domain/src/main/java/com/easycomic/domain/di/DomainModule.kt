package com.easycomic.domain.di

import com.easycomic.domain.repository.MangaRepository
import com.easycomic.domain.repository.ThemeRepository
import com.easycomic.domain.usecase.MangaUseCases
import com.easycomic.domain.usecase.ThemeUseCases
import org.koin.dsl.module

/**
 * 领域层依赖注入模块 - 重构后使用聚合UseCase模式
 */
val domainModule = module {
    
    // 聚合UseCase - 漫画相关操作
    factory { MangaUseCases(get<MangaRepository>()) }
    
    // 聚合UseCase - 主题相关操作
    factory { ThemeUseCases(get<ThemeRepository>()) }
}
