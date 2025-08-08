package com.easycomic.data.database.dao

import androidx.room.*
import com.easycomic.data.database.entity.BookmarkEntity
import com.easycomic.data.database.entity.MangaEntity
import com.easycomic.data.database.entity.ReadingHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MangaDao {
	@Query("SELECT * FROM manga ORDER BY (last_read IS NULL) ASC, last_read DESC, date_added DESC")
	fun observeAll(): Flow<List<MangaEntity>>

	@Insert(onConflict = OnConflictStrategy.ABORT)
	suspend fun insert(entity: MangaEntity): Long

	@Update
	suspend fun update(entity: MangaEntity)

	@Delete
	suspend fun delete(entity: MangaEntity)

	@Query("SELECT * FROM manga WHERE id = :id")
	suspend fun getById(id: Long): MangaEntity?

	@Query("UPDATE manga SET current_page = :page, reading_progress = :progress, last_read = :lastRead, updated_at = :updatedAt WHERE id = :id")
	suspend fun updateProgress(id: Long, page: Int, progress: Float, lastRead: Long, updatedAt: Long)

	@Query("UPDATE manga SET is_favorite = :favorite, updated_at = :updatedAt WHERE id = :id")
	suspend fun updateFavorite(id: Long, favorite: Boolean, updatedAt: Long)
}

@Dao
interface BookmarkDao {
	@Query("SELECT * FROM bookmark WHERE manga_id = :mangaId ORDER BY page_number ASC")
	fun observeByManga(mangaId: Long): Flow<List<BookmarkEntity>>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun upsert(entity: BookmarkEntity): Long

	@Delete
	suspend fun delete(entity: BookmarkEntity)
}

@Dao
interface ReadingHistoryDao {
	@Query("SELECT * FROM reading_history WHERE manga_id = :mangaId ORDER BY session_start DESC")
	fun observeByManga(mangaId: Long): Flow<List<ReadingHistoryEntity>>

	@Insert(onConflict = OnConflictStrategy.ABORT)
	suspend fun insert(entity: ReadingHistoryEntity): Long
}

