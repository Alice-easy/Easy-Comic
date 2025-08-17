package com.easycomic.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.easycomic.data.dao.MangaDao
import com.easycomic.data.database.AppDatabase
import com.easycomic.data.entity.MangaEntity
import com.google.common.truth.Truth.assertThat
import com.easycomic.domain.model.ReadingStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MangaRepositoryImplTest {

    private lateinit var mangaDao: MangaDao
    private lateinit var db: AppDatabase
    private lateinit var repository: MangaRepositoryImpl

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).allowMainThreadQueries().build()
        mangaDao = db.mangaDao()
        repository = MangaRepositoryImpl(mangaDao)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun getAllManga_returnsAllMangaFromDao() = runTest {
        val manga1 = MangaEntity(id = 1, title = "Manga 1", filePath = "/path/1", fileSize = 1, format = "CBZ", dateAdded = 1)
        val manga2 = MangaEntity(id = 2, title = "Manga 2", filePath = "/path/2", fileSize = 1, format = "CBZ", dateAdded = 2)
        mangaDao.insertAllManga(listOf(manga1, manga2))

        val allManga = repository.getAllManga().first()
        assertThat(allManga).hasSize(2)
        assertThat(allManga.map { it.title }).containsExactly("Manga 1", "Manga 2")
    }

    @Test
    fun searchManga_returnsMatchingMangaFromDao() = runTest {
        val manga1 = MangaEntity(id = 1, title = "Naruto", filePath = "/path/1", fileSize = 1, format = "CBZ", dateAdded = 1)
        val manga2 = MangaEntity(id = 2, title = "Bleach", filePath = "/path/2", fileSize = 1, format = "CBZ", dateAdded = 2)
        mangaDao.insertAllManga(listOf(manga1, manga2))

        val searchResult = repository.searchManga("naru").first()
        assertThat(searchResult).hasSize(1)
        assertThat(searchResult.first().title).isEqualTo("Naruto")
    }

    @Test
    fun updateReadingProgress_updatesProgressAndStatus() = runTest {
        val manga = MangaEntity(id = 1, title = "Test Manga", filePath = "/path/1", fileSize = 100, format = "CBZ", dateAdded = 1, pageCount = 100)
        mangaDao.insertOrUpdateManga(manga)

        // Progress to page 50
        repository.updateReadingProgress(1, 50, ReadingStatus.READING)
        var updatedManga = mangaDao.getMangaById(1)
        assertThat(updatedManga?.readingProgress).isEqualTo(0.5f)
        assertThat(updatedManga?.isCompleted).isFalse()

        // Finish reading
        repository.updateReadingProgress(1, 100, ReadingStatus.COMPLETED)
        updatedManga = mangaDao.getMangaById(1)
        assertThat(updatedManga?.readingProgress).isEqualTo(1.0f)
        assertThat(updatedManga?.isCompleted).isTrue()
    }
}