package com.easycomic.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.easycomic.data.dao.BookmarkDao
import com.easycomic.data.dao.MangaDao
import com.easycomic.data.dao.ReadingHistoryDao
import com.easycomic.data.entity.BookmarkEntity
import com.easycomic.data.entity.MangaEntity
import com.easycomic.data.entity.ReadingHistoryEntity

/**
 * 应用数据库
 * @Database 定义数据库配置
 */
@Database(
    entities = [
        MangaEntity::class,
        BookmarkEntity::class,
        ReadingHistoryEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun mangaDao(): MangaDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun readingHistoryDao(): ReadingHistoryDao
}