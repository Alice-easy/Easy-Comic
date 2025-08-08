package com.easycomic.di

import android.content.Context
import androidx.room.Room
import com.easycomic.data.database.AppDatabase
import com.easycomic.data.database.dao.BookmarkDao
import com.easycomic.data.database.dao.MangaDao
import com.easycomic.data.database.dao.ReadingHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "easy_comic.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideMangaDao(db: AppDatabase): MangaDao = db.mangaDao()
    @Provides fun provideBookmarkDao(db: AppDatabase): BookmarkDao = db.bookmarkDao()
    @Provides fun provideReadingHistoryDao(db: AppDatabase): ReadingHistoryDao = db.readingHistoryDao()
}
