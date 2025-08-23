package com.easycomic.domain.di

import com.easycomic.domain.repository.MangaRepository
import com.easycomic.domain.repository.ThemeRepository
import com.easycomic.domain.repository.ComicImportRepository
import com.easycomic.domain.usecase.MangaUseCases
import com.easycomic.domain.usecase.ThemeUseCases
import com.easycomic.domain.usecase.manga.*
import org.koin.dsl.module

/**
 * 领域层依赖注入模块 - 重构后使用聚合UseCase模式
 */
val domainModule = module {
    
    // 聚合UseCase - 漫画相关操作
    factory { MangaUseCases(get<MangaRepository>()) }
    
    // 聚合UseCase - 主题相关操作
    factory { ThemeUseCases(get<ThemeRepository>()) }
    
    // 单独的UseCase类 - 兼容旧的feature模块
    factory { GetAllMangaUseCase(get<MangaRepository>()) }
    factory { GetMangaByIdUseCase(get<MangaRepository>()) }
    factory { ImportComicsUseCase(get<ComicImportRepository>()) }
    factory { UpdateReadingProgressUseCase(get<MangaRepository>()) }
    factory { DeleteComicsUseCase(get<MangaRepository>()) }
    factory { UpdateMangaFavoriteStatusUseCase(get<MangaRepository>()) }
    factory { MarkMangasAsReadUseCase(get<MangaRepository>()) }
}
