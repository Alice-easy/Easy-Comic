package com.easycomic.data.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.easycomic.data.dao.BookmarkDao;
import com.easycomic.data.dao.MangaDao;
import com.easycomic.data.dao.ReadingHistoryDao;
import com.easycomic.data.entity.BookmarkEntity;
import com.easycomic.data.entity.MangaEntity;
import com.easycomic.data.entity.ReadingHistoryEntity;

/**
 * 应用数据库
 * @Database 定义数据库配置
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\'\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&J\b\u0010\u0005\u001a\u00020\u0006H&J\b\u0010\u0007\u001a\u00020\bH&\u00a8\u0006\t"}, d2 = {"Lcom/easycomic/data/database/AppDatabase;", "Landroidx/room/RoomDatabase;", "()V", "bookmarkDao", "Lcom/easycomic/data/dao/BookmarkDao;", "mangaDao", "Lcom/easycomic/data/dao/MangaDao;", "readingHistoryDao", "Lcom/easycomic/data/dao/ReadingHistoryDao;", "app_debug"})
@androidx.room.Database(entities = {com.easycomic.data.entity.MangaEntity.class, com.easycomic.data.entity.BookmarkEntity.class, com.easycomic.data.entity.ReadingHistoryEntity.class}, version = 1, exportSchema = true)
@androidx.room.TypeConverters(value = {com.easycomic.data.database.Converters.class})
public abstract class AppDatabase extends androidx.room.RoomDatabase {
    
    public AppDatabase() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.easycomic.data.dao.MangaDao mangaDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.easycomic.data.dao.BookmarkDao bookmarkDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.easycomic.data.dao.ReadingHistoryDao readingHistoryDao();
}