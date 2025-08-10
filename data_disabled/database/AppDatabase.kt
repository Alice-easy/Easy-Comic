package com.easycomic.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun mangaDao(): MangaDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun readingHistoryDao(): ReadingHistoryDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "easy_comic_database"
                )
                .addCallback(AppDatabaseCallback(scope))
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * 数据库回调，用于初始化数据
         */
        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        // 初始化数据库操作
                        // 这里可以添加一些默认数据
                    }
                }
            }
        }
        
        /**
         * 数据库迁移（版本 1 到 2）
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 添加新字段或表结构变更
                // 例如：database.execSQL("ALTER TABLE manga ADD COLUMN new_field TEXT")
            }
        }
    }
}