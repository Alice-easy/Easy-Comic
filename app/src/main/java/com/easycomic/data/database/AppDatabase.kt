package com.easycomic.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.easycomic.data.database.dao.BookmarkDao
import com.easycomic.data.database.dao.MangaDao
import com.easycomic.data.database.dao.ReadingHistoryDao
import com.easycomic.data.database.entity.BookmarkEntity
import com.easycomic.data.database.entity.MangaEntity
import com.easycomic.data.database.entity.ReadingHistoryEntity

@Database(
	entities = [MangaEntity::class, BookmarkEntity::class, ReadingHistoryEntity::class],
	version = 1,
	exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
	abstract fun mangaDao(): MangaDao
	abstract fun bookmarkDao(): BookmarkDao
	abstract fun readingHistoryDao(): ReadingHistoryDao
}

