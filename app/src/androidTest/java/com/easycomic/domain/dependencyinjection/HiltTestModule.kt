package com.easycomic.domain.dependencyinjection

import android.content.Context
import androidx.room.Room
import com.easycomic.data.dao.MangaDao
import com.easycomic.data.database.AppDatabase
import com.easycomic.data.repository.BookmarkRepositoryImpl
import com.easycomic.data.repository.MangaRepositoryImpl
import com.easycomic.data.repository.ReadingHistoryRepositoryImpl
import com.easycomic.domain.repository.BookmarkRepository
import com.easycomic.domain.repository.MangaRepository
import com.easycomic.domain.repository.ReadingHistoryRepository
import com.easycomic.domain.usecase.manga.*
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Hilt 测试模块
 * 为测试提供模拟的依赖项
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = []
)
object HiltTestModule {

    @Provides
    @Singleton
    fun provideTestDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @Provides
    fun provideMangaDao(database: AppDatabase): MangaDao {
        return database.mangaDao()
    }

    @Provides
    @Singleton
    fun provideMangaRepository(mangaDao: MangaDao): MangaRepository {
        return MangaRepositoryImpl(mangaDao)
    }

    @Provides
    @Singleton
    fun provideBookmarkRepository(): BookmarkRepository {
        return BookmarkRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideReadingHistoryRepository(): ReadingHistoryRepository {
        return ReadingHistoryRepositoryImpl()
    }

    @Provides
    fun provideGetAllMangaUseCase(mangaRepository: MangaRepository): GetAllMangaUseCase {
        return GetAllMangaUseCase(mangaRepository)
    }

    @Provides
    fun provideGetMangaByIdUseCase(mangaRepository: MangaRepository): GetMangaByIdUseCase {
        return GetMangaByIdUseCase(mangaRepository)
    }

    @Provides
    fun provideSearchMangaUseCase(mangaRepository: MangaRepository): SearchMangaUseCase {
        return SearchMangaUseCase(mangaRepository)
    }

    @Provides
    fun provideGetFavoriteMangaUseCase(mangaRepository: MangaRepository): GetFavoriteMangaUseCase {
        return GetFavoriteMangaUseCase(mangaRepository)
    }

    @Provides
    fun provideGetRecentMangaUseCase(mangaRepository: MangaRepository): GetRecentMangaUseCase {
        return GetRecentMangaUseCase(mangaRepository)
    }

    @Provides
    fun provideGetMangaByStatusUseCase(mangaRepository: MangaRepository): GetMangaByStatusUseCase {
        return GetMangaByStatusUseCase(mangaRepository)
    }

    @Provides
    fun provideInsertOrUpdateMangaUseCase(mangaRepository: MangaRepository): InsertOrUpdateMangaUseCase {
        return InsertOrUpdateMangaUseCase(mangaRepository)
    }

    @Provides
    fun provideUpdateReadingProgressUseCase(mangaRepository: MangaRepository): UpdateReadingProgressUseCase {
        return UpdateReadingProgressUseCase(mangaRepository)
    }

    @Provides
    fun provideToggleFavoriteUseCase(mangaRepository: MangaRepository): ToggleFavoriteUseCase {
        return ToggleFavoriteUseCase(mangaRepository)
    }

    @Provides
    fun provideUpdateRatingUseCase(mangaRepository: MangaRepository): UpdateRatingUseCase {
        return UpdateRatingUseCase(mangaRepository)
    }

    @Provides
    fun provideDeleteMangaUseCase(mangaRepository: MangaRepository): DeleteMangaUseCase {
        return DeleteMangaUseCase(mangaRepository)
    }

    @Provides
    fun provideDeleteAllMangaUseCase(mangaRepository: MangaRepository): DeleteAllMangaUseCase {
        return DeleteAllMangaUseCase(mangaRepository)
    }
}