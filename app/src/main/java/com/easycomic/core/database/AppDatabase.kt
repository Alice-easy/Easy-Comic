package com.easycomic.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Database(
    entities = [Manga::class, Bookmark::class, ReadingHistory::class],
    version = 2, // Incremented version for migration
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mangaDao(): MangaDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun readingHistoryDao(): ReadingHistoryDao
}

/**
 * Type converters for complex data types
 * Handles JSON serialization for List<String> tags and other complex types
 */
class Converters {
    private val gson = Gson()
    
    /**
     * Convert List<String> to JSON string for database storage
     */
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }
    
    /**
     * Convert JSON string to List<String> from database
     */
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
    
    /**
     * Convert String to JSON string for database storage
     */
    fun fromString(value: String?): String {
        return gson.toJson(value)
    }
    
    /**
     * Convert JSON string to String from database
     */
    fun toString(value: String?): String? {
        return if (value == null) null else {
            val stringType = object : TypeToken<String>() {}.type
            gson.fromJson(value, stringType)
        }
    }
}

/**
 * Database migration from version 1 to version 2
 * Handles the removal of readingHistoryId from Manga entity and adds new fields
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create new manga table with updated schema
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS manga_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                filePath TEXT NOT NULL,
                coverImagePath TEXT,
                totalPages INTEGER NOT NULL,
                currentPage INTEGER NOT NULL,
                isFavorite INTEGER NOT NULL,
                fileSize INTEGER NOT NULL,
                lastModified INTEGER NOT NULL,
                dateAdded INTEGER NOT NULL,
                format TEXT NOT NULL,
                author TEXT,
                description TEXT,
                tags TEXT NOT NULL, // Now stored as JSON string
                rating REAL NOT NULL,
                progressPercentage REAL NOT NULL,
                lastReadTimestamp INTEGER NOT NULL
            )
            """.trimIndent()
        )
        
        // Copy data from old table to new table, converting fields as needed
        database.execSQL(
            """
            INSERT INTO manga_new (
                id, title, filePath, coverImagePath, totalPages, currentPage, 
                isFavorite, fileSize, lastModified, dateAdded, format, author, 
                description, rating, progressPercentage
            )
            SELECT 
                id, title, filePath, coverImagePath, totalPages, currentPage, 
                isFavorite, fileSize, lastModified, dateAdded, format, author, 
                description, rating, progressPercentage
            FROM manga
            """.trimIndent()
        )
        
        // Drop old table and rename new table
        database.execSQL("DROP TABLE manga")
        database.execSQL("ALTER TABLE manga_new RENAME TO manga")
        
        // Create indexes for the manga table
        database.execSQL("CREATE INDEX IF NOT EXISTS index_manga_title ON manga(title)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_manga_filePath ON manga(filePath)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_manga_isFavorite ON manga(isFavorite)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_manga_dateAdded ON manga(dateAdded)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_manga_progressPercentage ON manga(progressPercentage)")
        
        // Update reading_history table with new fields
        database.execSQL(
            """
            ALTER TABLE reading_history ADD COLUMN readingProgress REAL NOT NULL DEFAULT 0.0
            """.trimIndent()
        )
        database.execSQL(
            """
            ALTER TABLE reading_history ADD COLUMN sessionStartTime INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )
        database.execSQL(
            """
            ALTER TABLE reading_history ADD COLUMN notes TEXT
            """.trimIndent()
        )
        
        // Create indexes for reading_history table
        database.execSQL("CREATE INDEX IF NOT EXISTS index_reading_history_mangaId ON reading_history(mangaId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_reading_history_lastReadTimestamp ON reading_history(lastReadTimestamp)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_reading_history_isCompleted ON reading_history(isCompleted)")
    }
}