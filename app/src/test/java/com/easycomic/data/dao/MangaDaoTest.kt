package com.easycomic.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.easycomic.data.database.AppDatabase
import com.easycomic.data.entity.MangaEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.google.common.truth.Truth.assertThat
import java.util.Date

@RunWith(AndroidJUnit4::class)
@SmallTest
class MangaDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var mangaDao: MangaDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        mangaDao = database.mangaDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertManga_shouldReturnInsertedId() = runBlocking {
        // Given
        val manga = createTestManga(title = "Test Manga")
        
        // When
        val id = mangaDao.insertManga(manga)
        
        // Then
        assertThat(id).isGreaterThan(0)
    }

    @Test
    fun getMangaById_shouldReturnCorrectManga() = runBlocking {
        // Given
        val manga = createTestManga(title = "Test Manga")
        val id = mangaDao.insertManga(manga)
        
        // When
        val result = mangaDao.getMangaById(id)
        
        // Then
        assertThat(result).isNotNull()
        assertThat(result?.title).isEqualTo("Test Manga")
        assertThat(result?.id).isEqualTo(id)
    }

    @Test
    fun getAllManga_shouldReturnAllManga() = runBlocking {
        // Given
        val manga1 = createTestManga(title = "Manga 1")
        val manga2 = createTestManga(title = "Manga 2")
        mangaDao.insertManga(manga1)
        mangaDao.insertManga(manga2)
        
        // When
        val result = mangaDao.getAllManga().first()
        
        // Then
        assertThat(result).hasSize(2)
        assertThat(result[0].title).isEqualTo("Manga 1")
        assertThat(result[1].title).isEqualTo("Manga 2")
    }

    @Test
    fun searchManga_shouldReturnMatchingResults() = runBlocking {
        // Given
        val manga1 = createTestManga(title = "Naruto", author = "Masashi Kishimoto")
        val manga2 = createTestManga(title = "One Piece", author = "Eiichiro Oda")
        val manga3 = createTestManga(title = "Bleach", author = "Tite Kubo")
        mangaDao.insertManga(manga1)
        mangaDao.insertManga(manga2)
        mangaDao.insertManga(manga3)
        
        // When
        val result = mangaDao.searchManga("Naruto").first()
        
        // Then
        assertThat(result).hasSize(1)
        assertThat(result[0].title).isEqualTo("Naruto")
    }

    @Test
    fun getMangaByFormat_shouldReturnFilteredResults() = runBlocking {
        // Given
        val manga1 = createTestManga(title = "Manga 1", format = "CBZ")
        val manga2 = createTestManga(title = "Manga 2", format = "CBR")
        val manga3 = createTestManga(title = "Manga 3", format = "CBZ")
        mangaDao.insertManga(manga1)
        mangaDao.insertManga(manga2)
        mangaDao.insertManga(manga3)
        
        // When
        val result = mangaDao.getMangaByFormat("CBZ").first()
        
        // Then
        assertThat(result).hasSize(2)
        assertThat(result.all { it.format == "CBZ" }).isTrue()
    }

    @Test
    fun getFavoriteManga_shouldReturnOnlyFavorites() = runBlocking {
        // Given
        val manga1 = createTestManga(title = "Favorite 1", isFavorite = true)
        val manga2 = createTestManga(title = "Not Favorite", isFavorite = false)
        val manga3 = createTestManga(title = "Favorite 2", isFavorite = true)
        mangaDao.insertManga(manga1)
        mangaDao.insertManga(manga2)
        mangaDao.insertManga(manga3)
        
        // When
        val result = mangaDao.getFavoriteManga().first()
        
        // Then
        assertThat(result).hasSize(2)
        assertThat(result.all { it.isFavorite }).isTrue()
    }

    @Test
    fun updateManga_shouldUpdateFields() = runBlocking {
        // Given
        val manga = createTestManga(title = "Original Title")
        val id = mangaDao.insertManga(manga)
        
        // When
        val updatedManga = manga.copy(
            id = id,
            title = "Updated Title",
            currentPage = 10,
            isFavorite = true
        )
        mangaDao.updateManga(updatedManga)
        
        // Then
        val result = mangaDao.getMangaById(id)
        assertThat(result).isNotNull()
        assertThat(result?.title).isEqualTo("Updated Title")
        assertThat(result?.currentPage).isEqualTo(10)
        assertThat(result?.isFavorite).isTrue()
    }

    @Test
    fun deleteManga_shouldRemoveFromDatabase() = runBlocking {
        // Given
        val manga = createTestManga(title = "To Delete")
        val id = mangaDao.insertManga(manga)
        
        // When
        mangaDao.deleteManga(id)
        
        // Then
        val result = mangaDao.getMangaById(id)
        assertThat(result).isNull()
    }

    @Test
    fun getMangaCount_shouldReturnCorrectCount() = runBlocking {
        // Given
        mangaDao.insertManga(createTestManga(title = "Manga 1"))
        mangaDao.insertManga(createTestManga(title = "Manga 2"))
        mangaDao.insertManga(createTestManga(title = "Manga 3"))
        
        // When
        val count = mangaDao.getMangaCount()
        
        // Then
        assertThat(count).isEqualTo(3)
    }

    @Test
    fun updateReadingProgress_shouldUpdateProgress() = runBlocking {
        // Given
        val manga = createTestManga(title = "Test Manga", pageCount = 100)
        val id = mangaDao.insertManga(manga)
        
        // When
        mangaDao.updateReadingProgress(id, 50, 0.5f)
        
        // Then
        val result = mangaDao.getMangaById(id)
        assertThat(result).isNotNull()
        assertThat(result?.currentPage).isEqualTo(50)
        assertThat(result?.readingProgress).isEqualTo(0.5f)
    }

    @Test
    fun getRecentManga_shouldReturnOrderedByLastRead() = runBlocking {
        // Given
        val now = System.currentTimeMillis()
        val manga1 = createTestManga(title = "Old", lastRead = now - 10000)
        val manga2 = createTestManga(title = "Recent", lastRead = now)
        val manga3 = createTestManga(title = "Oldest", lastRead = now - 20000)
        mangaDao.insertManga(manga1)
        mangaDao.insertManga(manga2)
        mangaDao.insertManga(manga3)
        
        // When
        val result = mangaDao.getRecentManga().first()
        
        // Then
        assertThat(result).hasSize(3)
        assertThat(result[0].title).isEqualTo("Recent")
        assertThat(result[1].title).isEqualTo("Old")
        assertThat(result[2].title).isEqualTo("Oldest")
    }

    private fun createTestManga(
        title: String = "Test Manga",
        author: String = "Test Author",
        format: String = "CBZ",
        isFavorite: Boolean = false,
        pageCount: Int = 100,
        currentPage: Int = 0,
        lastRead: Long = System.currentTimeMillis()
    ): MangaEntity {
        return MangaEntity(
            title = title,
            author = author,
            description = "Test description",
            filePath = "/path/to/$title.cbz",
            fileSize = 1024000L,
            format = format,
            coverPath = "/path/to/cover.jpg",
            pageCount = pageCount,
            currentPage = currentPage,
            readingProgress = currentPage.toFloat() / pageCount,
            isFavorite = isFavorite,
            isCompleted = false,
            dateAdded = System.currentTimeMillis(),
            lastRead = lastRead,
            readingTime = 0,
            rating = 0.0f,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
}