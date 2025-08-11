package com.easycomic.data.di

import android.content.Context
import androidx.room.Room
import com.easycomic.data.dao.BookmarkDao
import com.easycomic.data.dao.MangaDao
import com.easycomic.data.dao.ReadingHistoryDao
import com.easycomic.data.database.AppDatabase
import com.easycomic.data.repository.BookmarkRepositoryImpl
import com.easycomic.data.repository.MangaRepositoryImpl
import com.easycomic.data.repository.ReadingHistoryRepositoryImpl
import com.easycomic.data.service.ComicImportService
import com.easycomic.data.util.FileHelper
import com.easycomic.domain.repository.BookmarkRepository
import com.easycomic.domain.repository.MangaRepository
import com.easycomic.domain.repository.ReadingHistoryRepository
import com.easycomic.domain.usecase.manga.ImportProgressHolder

/**
 * 数据层依赖注入模块 - 使用 Koin
 * 
 * 注意：这个文件保留用于兼容性，实际的依赖注入现在通过 Koin 处理
 * 所有依赖都在 KoinModules.kt 中定义
 */
object DataModule {
    
    /**
     * 提供AppDatabase - 已移动到Koin模块
     */
    fun provideAppDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "easy_comic_database"
        ).build()
    }
    
    /**
     * 提供MangaDao - 已移动到Koin模块
     */
    fun provideMangaDao(database: AppDatabase): MangaDao = database.mangaDao()
    
    /**
     * 提供BookmarkDao - 已移动到Koin模块
     */
    fun provideBookmarkDao(database: AppDatabase): BookmarkDao = database.bookmarkDao()
    
    /**
     * 提供ReadingHistoryDao - 已移动到Koin模块
     */
    fun provideReadingHistoryDao(database: AppDatabase): ReadingHistoryDao = database.readingHistoryDao()
    
    /**
     * 提供MangaRepository - 已移动到Koin模块
     */
    fun provideMangaRepository(mangaDao: MangaDao): MangaRepository = MangaRepositoryImpl(mangaDao)
    
    /**
     * 提供BookmarkRepository - 已移动到Koin模块
     */
    fun provideBookmarkRepository(bookmarkDao: BookmarkDao): BookmarkRepository = BookmarkRepositoryImpl(bookmarkDao)
    
    /**
     * 提供ReadingHistoryRepository - 已移动到Koin模块
     */
    fun provideReadingHistoryRepository(readingHistoryDao: ReadingHistoryDao): ReadingHistoryRepository = ReadingHistoryRepositoryImpl(readingHistoryDao)
    
    /**
     * 提供ComicImportService - 已移动到Koin模块
     */
    fun provideComicImportService(
        context: Context,
        mangaRepositoryImpl: MangaRepositoryImpl
    ): ComicImportService = ComicImportService(context, mangaRepositoryImpl, FileHelper)
    
    /**
     * 创建ImportProgressHolder - 已移动到Koin模块
     */
    fun createImportProgressHolder(): ImportProgressHolder = ImportProgressHolder()
}