package com.easycomic.data.di;

import android.content.Context;
import androidx.room.Room;
import com.easycomic.data.dao.BookmarkDao;
import com.easycomic.data.dao.MangaDao;
import com.easycomic.data.dao.ReadingHistoryDao;
import com.easycomic.data.database.AppDatabase;
import com.easycomic.data.repository.BookmarkRepositoryImpl;
import com.easycomic.data.repository.MangaRepositoryImpl;
import com.easycomic.data.repository.ReadingHistoryRepositoryImpl;
import com.easycomic.data.service.ComicImportService;
import com.easycomic.data.util.FileHelper;
import com.easycomic.domain.repository.BookmarkRepository;
import com.easycomic.domain.repository.MangaRepository;
import com.easycomic.domain.repository.ReadingHistoryRepository;
import com.easycomic.domain.usecase.manga.ImportProgressHolder;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

/**
 * 数据层依赖注入模块 - 使用 Hilt
 */
@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000P\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u0003\u001a\u00020\u00042\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u0004H\u0007J\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\bH\u0007J\u001a\u0010\r\u001a\u00020\u000e2\b\b\u0001\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u000f\u001a\u00020\u0010H\u0007J\u0010\u0010\u0011\u001a\u00020\u00122\u0006\u0010\t\u001a\u00020\u0004H\u0007J\u0010\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0012H\u0007J\u0010\u0010\u0016\u001a\u00020\u00172\u0006\u0010\t\u001a\u00020\u0004H\u0007J\u0010\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u0017H\u0007\u00a8\u0006\u001b"}, d2 = {"Lcom/easycomic/data/di/DataModule;", "", "()V", "provideAppDatabase", "Lcom/easycomic/data/database/AppDatabase;", "context", "Landroid/content/Context;", "provideBookmarkDao", "Lcom/easycomic/data/dao/BookmarkDao;", "database", "provideBookmarkRepository", "Lcom/easycomic/domain/repository/BookmarkRepository;", "bookmarkDao", "provideComicImportService", "Lcom/easycomic/data/service/ComicImportService;", "mangaRepositoryImpl", "Lcom/easycomic/data/repository/MangaRepositoryImpl;", "provideMangaDao", "Lcom/easycomic/data/dao/MangaDao;", "provideMangaRepository", "Lcom/easycomic/domain/repository/MangaRepository;", "mangaDao", "provideReadingHistoryDao", "Lcom/easycomic/data/dao/ReadingHistoryDao;", "provideReadingHistoryRepository", "Lcom/easycomic/domain/repository/ReadingHistoryRepository;", "readingHistoryDao", "app_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class DataModule {
    @org.jetbrains.annotations.NotNull()
    public static final com.easycomic.data.di.DataModule INSTANCE = null;
    
    private DataModule() {
        super();
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.data.database.AppDatabase provideAppDatabase(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.data.dao.MangaDao provideMangaDao(@org.jetbrains.annotations.NotNull()
    com.easycomic.data.database.AppDatabase database) {
        return null;
    }
    
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.data.dao.BookmarkDao provideBookmarkDao(@org.jetbrains.annotations.NotNull()
    com.easycomic.data.database.AppDatabase database) {
        return null;
    }
    
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.data.dao.ReadingHistoryDao provideReadingHistoryDao(@org.jetbrains.annotations.NotNull()
    com.easycomic.data.database.AppDatabase database) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.domain.repository.MangaRepository provideMangaRepository(@org.jetbrains.annotations.NotNull()
    com.easycomic.data.dao.MangaDao mangaDao) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.domain.repository.BookmarkRepository provideBookmarkRepository(@org.jetbrains.annotations.NotNull()
    com.easycomic.data.dao.BookmarkDao bookmarkDao) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.domain.repository.ReadingHistoryRepository provideReadingHistoryRepository(@org.jetbrains.annotations.NotNull()
    com.easycomic.data.dao.ReadingHistoryDao readingHistoryDao) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.data.service.ComicImportService provideComicImportService(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.easycomic.data.repository.MangaRepositoryImpl mangaRepositoryImpl) {
        return null;
    }
}