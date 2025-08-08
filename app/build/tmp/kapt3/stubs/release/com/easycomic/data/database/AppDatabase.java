package com.easycomic.data.database;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\'\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&J\b\u0010\u0005\u001a\u00020\u0006H&J\b\u0010\u0007\u001a\u00020\bH&\u00a8\u0006\t"}, d2 = {"Lcom/easycomic/data/database/AppDatabase;", "Landroidx/room/RoomDatabase;", "()V", "bookmarkDao", "Lcom/easycomic/data/database/dao/BookmarkDao;", "mangaDao", "Lcom/easycomic/data/database/dao/MangaDao;", "readingHistoryDao", "Lcom/easycomic/data/database/dao/ReadingHistoryDao;", "app_release"})
@androidx.room.Database(entities = {com.easycomic.data.database.entity.MangaEntity.class, com.easycomic.data.database.entity.BookmarkEntity.class, com.easycomic.data.database.entity.ReadingHistoryEntity.class}, version = 1, exportSchema = true)
public abstract class AppDatabase extends androidx.room.RoomDatabase {
    
    public AppDatabase() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.easycomic.data.database.dao.MangaDao mangaDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.easycomic.data.database.dao.BookmarkDao bookmarkDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.easycomic.data.database.dao.ReadingHistoryDao readingHistoryDao();
}