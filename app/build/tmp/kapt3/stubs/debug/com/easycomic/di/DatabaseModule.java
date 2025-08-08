package com.easycomic.di;

@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0012\u0010\u0007\u001a\u00020\u00062\b\b\u0001\u0010\b\u001a\u00020\tH\u0007J\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\f\u001a\u00020\r2\u0006\u0010\u0005\u001a\u00020\u0006H\u0007\u00a8\u0006\u000e"}, d2 = {"Lcom/easycomic/di/DatabaseModule;", "", "()V", "provideBookmarkDao", "Lcom/easycomic/data/database/dao/BookmarkDao;", "db", "Lcom/easycomic/data/database/AppDatabase;", "provideDatabase", "context", "Landroid/content/Context;", "provideMangaDao", "Lcom/easycomic/data/database/dao/MangaDao;", "provideReadingHistoryDao", "Lcom/easycomic/data/database/dao/ReadingHistoryDao;", "app_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class DatabaseModule {
    @org.jetbrains.annotations.NotNull()
    public static final com.easycomic.di.DatabaseModule INSTANCE = null;
    
    private DatabaseModule() {
        super();
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.data.database.AppDatabase provideDatabase(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.data.database.dao.MangaDao provideMangaDao(@org.jetbrains.annotations.NotNull()
    com.easycomic.data.database.AppDatabase db) {
        return null;
    }
    
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.data.database.dao.BookmarkDao provideBookmarkDao(@org.jetbrains.annotations.NotNull()
    com.easycomic.data.database.AppDatabase db) {
        return null;
    }
    
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.data.database.dao.ReadingHistoryDao provideReadingHistoryDao(@org.jetbrains.annotations.NotNull()
    com.easycomic.data.database.AppDatabase db) {
        return null;
    }
}