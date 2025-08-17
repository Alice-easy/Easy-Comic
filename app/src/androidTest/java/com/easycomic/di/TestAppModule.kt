package com.easycomic.di

import androidx.room.Room
import com.easycomic.data.database.AppDatabase
import com.easycomic.data.repository.BookmarkRepositoryImpl
import com.easycomic.data.repository.ComicImportRepositoryImpl
import com.easycomic.data.repository.MangaRepositoryImpl
import com.easycomic.data.repository.ReadingHistoryRepositoryImpl
import com.easycomic.domain.repository.BookmarkRepository
import com.easycomic.domain.repository.ComicImportRepository
import com.easycomic.domain.repository.MangaRepository
import com.easycomic.domain.repository.ReadingHistoryRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * 用于仪器测试的 Koin 模块
 */
val testModule = module {
    // 使用内存数据库替换真实的数据库
    single {
        Room.inMemoryDatabaseBuilder(
            androidContext(),
            AppDatabase::class.java
        )
        .allowMainThreadQueries() // 仅在测试中使用
        .build()
    }

    // DAO (将使用内存数据库)
    single { get<AppDatabase>().mangaDao() }
    single { get<AppDatabase>().bookmarkDao() }
    single { get<AppDatabase>().readingHistoryDao() }

    // 仓库实现 (将使用内存数据库)
    single<MangaRepository> { MangaRepositoryImpl(get()) }
    single<BookmarkRepository> { BookmarkRepositoryImpl(get()) }
    single<ReadingHistoryRepository> { ReadingHistoryRepositoryImpl(get()) }
    single<ComicImportRepository> { ComicImportRepositoryImpl(androidContext(), get()) }
}