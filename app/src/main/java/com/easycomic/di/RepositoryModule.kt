package com.easycomic.di

import com.easycomic.data.repository.BookmarkRepository
import com.easycomic.data.repository.MangaRepository
import com.easycomic.data.repository.ReadingHistoryRepository
import com.easycomic.data.repository.WebDavRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMangaRepository(
        mangaLocalDataSource: com.easycomic.data.local.MangaLocalDataSource,
        webDavDataSource: com.easycomic.data.remote.WebDavDataSource
    ): MangaRepository {
        return MangaRepository(mangaLocalDataSource, webDavDataSource)
    }

    @Provides
    @Singleton
    fun provideBookmarkRepository(
        bookmarkDao: com.easycomic.core.database.BookmarkDao
    ): BookmarkRepository {
        return BookmarkRepository(bookmarkDao)
    }

    @Provides
    @Singleton
    fun provideReadingHistoryRepository(
        readingHistoryDao: com.easycomic.core.database.ReadingHistoryDao
    ): ReadingHistoryRepository {
        return ReadingHistoryRepository(readingHistoryDao)
    }

    @Provides
    @Singleton
    fun provideWebDavRepository(
        webDavDataSource: com.easycomic.data.remote.WebDavDataSource
    ): WebDavRepository {
        return WebDavRepository(webDavDataSource)
    }
}