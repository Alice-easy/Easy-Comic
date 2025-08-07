package com.easycomic.di

import android.content.Context
import androidx.room.Room
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.easycomic.core.database.AppDatabase
import com.easycomic.core.database.Manga
import com.easycomic.core.util.PerformanceInterceptor
import com.easycomic.core.util.MemoryDebugInterceptor
import com.easycomic.data.local.MangaLocalDataSource
import com.easycomic.data.remote.WebDavDataSource
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "easy_comic_database"
        )
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideMangaDao(database: AppDatabase) = database.mangaDao()

    @Provides
    @Singleton
    fun provideBookmarkDao(database: AppDatabase) = database.bookmarkDao()

    @Provides
    @Singleton
    fun provideReadingHistoryDao(database: AppDatabase) = database.readingHistoryDao()

    @Provides
    @Singleton
    fun provideMangaLocalDataSource(
        mangaDao: MangaDao,
        bookmarkDao: BookmarkDao,
        readingHistoryDao: ReadingHistoryDao
    ): MangaLocalDataSource {
        return MangaLocalDataSource(mangaDao, bookmarkDao, readingHistoryDao)
    }

    @Provides
    @Singleton
    fun provideWebDavDataSource(): WebDavDataSource {
        return WebDavDataSource()
    }
}

/**
 * Coil image loader configuration module
 */
@Module
@InstallIn(SingletonComponent::class)
object CoilModule {
    
    @Provides
    @Singleton
    fun provideImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                // Add custom decoders for image optimization
                add(BitmapFactoryDecoder.Factory())
                
                // Add memory cache configuration
                memoryCache {
                    MemoryCache.Builder(context)
                        .maxSizePercent(0.25) // Use 25% of available memory
                        .strongReferencesEnabled(true)
                        .build()
                }
                
                // Add disk cache configuration
                diskCache {
                    DiskCache.Builder()
                        .directory(context.cacheDir.resolve("image_cache"))
                        .maxSizeBytes(512L * 1024 * 1024) // 512MB
                        .build()
                }
                
                // Add custom interceptors for performance
                add(PerformanceInterceptor())
                add(MemoryDebugInterceptor())
            }
            .respectCacheHeaders(false) // Don't respect server cache headers
            .networkCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .allowHardware(true)
            .allowRgb565(true)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideMemoryCache(context: Context): MemoryCache {
        return MemoryCache.Builder(context)
            .maxSizePercent(0.25)
            .strongReferencesEnabled(true)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideDiskCache(context: Context): DiskCache {
        return DiskCache.Builder()
            .directory(context.cacheDir.resolve("image_cache"))
            .maxSizeBytes(512L * 1024 * 1024) // 512MB
            .build()
    }
}