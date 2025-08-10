package com.easycomic.data.di

import com.easycomic.data.dao.BookmarkDao
import com.easycomic.data.dao.MangaDao
import com.easycomic.data.dao.ReadingHistoryDao
import com.easycomic.data.database.AppDatabase
import com.easycomic.data.repository.BookmarkRepositoryImpl
import com.easycomic.data.repository.MangaRepositoryImpl
import com.easycomic.data.repository.ReadingHistoryRepositoryImpl
import com.easycomic.domain.repository.BookmarkRepository
import com.easycomic.domain.repository.MangaRepository
import com.easycomic.domain.repository.ReadingHistoryRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * 数据层依赖注入模块
 */
val dataModule = module {
    
    // 数据库
    single {
        AppDatabase.getDatabase(androidContext(), get())
    }
    
    // DAO
    single { get<AppDatabase>().mangaDao() }
    single { get<AppDatabase>().bookmarkDao() }
    single { get<AppDatabase>().readingHistoryDao() }
    
    // 仓库实现
    single<MangaRepository> { MangaRepositoryImpl(get()) }
    single<BookmarkRepository> { BookmarkRepositoryImpl(get()) }
    single<ReadingHistoryRepository> { ReadingHistoryRepositoryImpl(get()) }
}