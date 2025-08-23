package com.easycomic.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.easycomic.data.dao.BookmarkDao
import com.easycomic.data.dao.MangaDao
import com.easycomic.data.dao.ReadingHistoryDao
import com.easycomic.data.entity.BookmarkEntity
import com.easycomic.data.entity.MangaEntity
import com.easycomic.data.entity.ReadingHistoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
abstract class AppDatabase : RoomDatabase() {

    abstract fun mangaDao(): MangaDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun readingHistoryDao(): ReadingHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "easy_comic_database"
                )
                // 移除迁移和回调，以简化配置
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
