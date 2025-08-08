package com.easycomic.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
	tableName = "manga",
	indices = [
		Index(value = ["title"], name = "idx_manga_title"),
		Index(value = ["last_read"], name = "idx_manga_last_read"),
		Index(value = ["is_favorite", "last_read"], name = "idx_manga_favorite")
	]
)
data class MangaEntity(
	@PrimaryKey(autoGenerate = true) val id: Long = 0L,
	val title: String,
	val author: String? = null,
	val description: String? = null,
	@ColumnInfo(name = "file_path") val filePath: String,
	@ColumnInfo(name = "file_size") val fileSize: Long,
	val format: String,
	@ColumnInfo(name = "cover_path") val coverPath: String? = null,
	@ColumnInfo(name = "page_count") val pageCount: Int = 0,
	@ColumnInfo(name = "current_page") val currentPage: Int = 1,
	@ColumnInfo(name = "reading_progress") val readingProgress: Float = 0f,
	@ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false,
	@ColumnInfo(name = "is_completed") val isCompleted: Boolean = false,
	@ColumnInfo(name = "date_added") val dateAdded: Long,
	@ColumnInfo(name = "last_read") val lastRead: Long? = null,
	@ColumnInfo(name = "reading_time") val readingTime: Long = 0L,
	val rating: Float = 0f,
	@ColumnInfo(name = "created_at") val createdAt: Long,
	@ColumnInfo(name = "updated_at") val updatedAt: Long
)

@Entity(
	tableName = "bookmark",
	foreignKeys = [
		ForeignKey(
			entity = MangaEntity::class,
			parentColumns = ["id"],
			childColumns = ["manga_id"],
			onDelete = ForeignKey.CASCADE
		)
	],
	indices = [
		Index(value = ["manga_id"], name = "idx_bookmark_manga_id"),
		Index(value = ["manga_id", "page_number"], unique = true)
	]
)
data class BookmarkEntity(
	@PrimaryKey(autoGenerate = true) val id: Long = 0L,
	@ColumnInfo(name = "manga_id") val mangaId: Long,
	@ColumnInfo(name = "page_number") val pageNumber: Int,
	@ColumnInfo(name = "bookmark_name") val bookmarkName: String? = null,
	val notes: String? = null,
	@ColumnInfo(name = "created_at") val createdAt: Long
)

@Entity(
	tableName = "reading_history",
	foreignKeys = [
		ForeignKey(
			entity = MangaEntity::class,
			parentColumns = ["id"],
			childColumns = ["manga_id"],
			onDelete = ForeignKey.CASCADE
		)
	],
	indices = [
		Index(value = ["manga_id"], name = "idx_history_manga_id"),
		Index(value = ["session_start"], name = "idx_history_session")
	]
)
data class ReadingHistoryEntity(
	@PrimaryKey(autoGenerate = true) val id: Long = 0L,
	@ColumnInfo(name = "manga_id") val mangaId: Long,
	@ColumnInfo(name = "page_number") val pageNumber: Int,
	@ColumnInfo(name = "reading_time") val readingTime: Long,
	@ColumnInfo(name = "session_start") val sessionStart: Long,
	@ColumnInfo(name = "session_end") val sessionEnd: Long,
	@ColumnInfo(name = "reading_speed") val readingSpeed: Float? = null,
	@ColumnInfo(name = "created_at") val createdAt: Long
)

