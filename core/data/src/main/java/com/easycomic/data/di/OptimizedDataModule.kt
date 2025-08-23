package com.easycomic.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.easycomic.data.database.AppDatabase
import com.easycomic.data.repository.*
import com.easycomic.domain.repository.*
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * 优化后的数据层依赖注入模块
 * 
 * 优化点：
 * 1. 统一的模块管理
 * 2. 清晰的依赖层次
 * 3. 环境配置支持
 */
val optimizedDataModule = module {
    
    // DataStore - 重用DataModule中的定义
    single<DataStore<Preferences>> {
        androidContext().dataStore
    }
    
    // 数据库配置
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "easy_comic_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    // DAO层
    single { get<AppDatabase>().mangaDao() }
    single { get<AppDatabase>().bookmarkDao() }
    single { get<AppDatabase>().readingHistoryDao() }
    
    // Repository层 - 优先使用优化版本
    single<MangaRepository> { 
        try {
            OptimizedMangaRepositoryImpl(mangaDao = get())
        } catch (e: Exception) {
            MangaRepositoryImpl(mangaDao = get())
        }
    }
    
    single<BookmarkRepository> { 
        try {
            OptimizedBookmarkRepositoryImpl(bookmarkDao = get())
        } catch (e: Exception) {
            BookmarkRepositoryImpl(bookmarkDao = get())
        }
    }
    
    single<ReadingHistoryRepository> { 
        ReadingHistoryRepositoryImpl(readingHistoryDao = get())
    }
    
    single<ThemeRepository> { 
        ThemeRepositoryImpl(dataStore = get())
    }
    
    single<ComicImportRepository> { 
        ComicImportRepositoryImpl(
            context = androidContext(),
            mangaDao = get()
        )
    }
    
    // 文件解析器
    single<com.easycomic.domain.parser.ComicParserFactory> { 
        com.easycomic.data.parser.ComicParserFactoryImpl()
    }
    
    // 文件管理器
    single { 
        FileManager(androidContext())
    }
    
    // 缓存配置
    single(named("cache_size")) { 50 }
    single(named("cache_ttl")) { 300_000L }
}

/**
 * 生产环境数据模块
 */
val productionDataModule = module {
    includes(optimizedDataModule)
    
    single(named("cache_size")) { 100 }
    single(named("cache_ttl")) { 600_000L }
}

/**
 * 开发环境数据模块
 */
val developmentDataModule = module {
    includes(optimizedDataModule)
    
    single(named("cache_size")) { 20 }
    single(named("cache_ttl")) { 60_000L }
    single(named("debug_logging")) { true }
}