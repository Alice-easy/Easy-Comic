package com.easycomic.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.easycomic.data.dao.BookmarkDao
import com.easycomic.data.dao.MangaDao
import com.easycomic.data.dao.ReadingHistoryDao
import com.easycomic.data.database.AppDatabase
import com.easycomic.data.parser.ComicParserFactoryImpl
import com.easycomic.data.repository.BookmarkRepositoryImpl
import com.easycomic.data.repository.ComicImportRepositoryImpl
import com.easycomic.data.repository.FileManager
import com.easycomic.data.repository.MangaRepositoryImpl
import com.easycomic.data.repository.ReadingHistoryRepositoryImpl
import com.easycomic.data.repository.ThemeRepositoryImpl
import com.easycomic.domain.parser.ComicParserFactory
import com.easycomic.domain.repository.BookmarkRepository
import com.easycomic.domain.repository.ComicImportRepository
import com.easycomic.domain.repository.MangaRepository
import com.easycomic.domain.repository.ReadingHistoryRepository
import com.easycomic.domain.repository.ThemeRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

// DataStore扩展属性
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

/**
 * 数据层依赖注入模块
 */
val dataModule = module {
    
    // DataStore
    single<DataStore<Preferences>> {
        androidContext().dataStore
    }
    
    // 数据库
    single {
        AppDatabase.getDatabase(androidContext())
    }
    
    // DAO
    single { get<AppDatabase>().mangaDao() }
    single { get<AppDatabase>().bookmarkDao() }
    single { get<AppDatabase>().readingHistoryDao() }
    
    // 工具类
    single { FileManager(androidContext()) }
    
    // 仓库实现
    single<MangaRepository> { MangaRepositoryImpl(get()) }
    single<BookmarkRepository> { BookmarkRepositoryImpl(get()) }
    single<ReadingHistoryRepository> { ReadingHistoryRepositoryImpl(get()) }
    single<ComicImportRepository> { ComicImportRepositoryImpl(androidContext(), get()) }
    single<ThemeRepository> { ThemeRepositoryImpl(get()) }
}
