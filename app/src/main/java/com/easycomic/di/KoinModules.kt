package com.easycomic.di

import com.easycomic.data.dao.BookmarkDao
import com.easycomic.data.dao.MangaDao
import com.easycomic.data.dao.ReadingHistoryDao
import com.easycomic.data.database.AppDatabase
import com.easycomic.data.repository.BookmarkRepositoryImpl
import com.easycomic.data.repository.MangaRepositoryImpl
import com.easycomic.data.repository.ReadingHistoryRepositoryImpl
import com.easycomic.data.service.ComicImportService
import com.easycomic.domain.repository.BookmarkRepository
import com.easycomic.domain.repository.MangaRepository
import com.easycomic.domain.repository.ReadingHistoryRepository
import com.easycomic.domain.service.BookshelfService
import com.easycomic.domain.usecase.manga.*
import com.easycomic.ui.di.uiModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.module
import org.koin.dsl.module

/**
 * Koin 依赖注入模块配置
 */
object KoinModules {
    
    /**
     * 数据层模块
     */
    private val dataModule = module {
        // Database
        single { DependencyContainer.getDatabase() }
        
        // DAOs
        single { get<AppDatabase>().mangaDao() }
        single { get<AppDatabase>().bookmarkDao() }
        single { get<AppDatabase>().readingHistoryDao() }
        
        // Repositories
        single { MangaRepositoryImpl(get()) }
        single { BookmarkRepositoryImpl(get()) }
        single { ReadingHistoryRepositoryImpl(get()) }
        
        // Services
        single { DependencyContainer.getComicImportService() }
    }
    
    /**
     * 领域层模块
     */
    private val domainModule = module {
        // 漫画用例
        single { GetAllMangaUseCase(get()) }
        single { GetMangaByIdUseCase(get()) }
        single { SearchMangaUseCase(get()) }
        single { GetFavoriteMangaUseCase(get()) }
        single { GetRecentMangaUseCase(get()) }
        single { GetMangaByStatusUseCase(get()) }
        single { InsertOrUpdateMangaUseCase(get()) }
        single { UpdateReadingProgressUseCase(get()) }
        single { ToggleFavoriteUseCase(get()) }
        single { UpdateRatingUseCase(get()) }
        single { DeleteMangaUseCase(get()) }
        single { DeleteAllMangaUseCase(get()) }
        
        // 导入相关用例
        single { ImportComicUseCase(get()) }
        single { BatchImportComicsUseCase(get(), get()) }
        single { MonitorImportProgressUseCase(get()) }
        single { UpdateImportProgressUseCase(get()) }
        
        // 共享状态
        single { ImportProgressHolder() }
        
        // 书架服务
        single { 
            BookshelfService(
                getAllMangaUseCase = get(),
                searchMangaUseCase = get(),
                getFavoriteMangaUseCase = get(),
                getRecentMangaUseCase = get(),
                deleteMangaUseCase = get(),
                deleteAllMangaUseCase = get(),
                toggleFavoriteUseCase = get(),
                importComicUseCase = get(),
                batchImportComicsUseCase = get(),
                monitorImportProgressUseCase = get()
            )
        }
    }
    
    /**
     * 获取所有模块
     */
    fun getAllModules(): List<Module> = listOf(dataModule, domainModule, uiModule)
    
    /**
     * 初始化Koin
     */
    fun initializeKoin(context: android.content.Context) {
        // 先初始化依赖容器
        DependencyContainer.initialize(context)
        
        // 再初始化Koin
        startKoin {
            androidContext(context)
            modules(getAllModules())
        }
    }
}