package com.easycomic.domain.di

import com.easycomic.domain.usecase.*
import org.koin.dsl.module

/**
 * Domain层依赖注入模块
 * 
 * 配置Domain层的所有用例类的依赖注入，包括聚合UseCase类和单独的UseCase类。
 * 采用聚合模式减少类的数量，提高可维护性。
 * 
 * @author EasyComic Team
 * @since 1.0.0
 */
val domainModule = module {
    // 聚合UseCase类
    factory { MangaUseCases(get()) }
    factory { ThemeUseCases(get()) }
    
    // 单独的UseCase类
    factory { GetThemePreferenceUseCase(get()) }
    factory { UpdateThemePreferenceUseCase(get()) }
}