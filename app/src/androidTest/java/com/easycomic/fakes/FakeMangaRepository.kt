package com.easycomic.fakes

import android.graphics.Bitmap
import com.easycomic.domain.model.Manga
import com.easycomic.domain.model.ReadingStatus
import com.easycomic.domain.repository.MangaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeMangaRepository : MangaRepository {

    private val mangas = mutableListOf<Manga>()

    init {
        // Initialize with a default fake manga for testing purposes
        val fakeManga = Manga(
            id = 1L,
            title = "Test Manga 1", // Changed to match the test expectation
            filePath = "/fake/path/to/manga.cbz",
            pageCount = 100,
            currentPage = 0,
            readingStatus = ReadingStatus.UNREAD
        )
        mangas.add(fakeManga)
    }

    override fun getAllManga(): Flow<List<Manga>> {
        return flowOf(mangas.toList())
    }

    override suspend fun getMangaById(id: Long): Manga? {
        return mangas.find { it.id == id }
    }

    override suspend fun getMangaByFilePath(filePath: String): Manga? {
        return mangas.find { it.filePath == filePath }
    }

    override fun searchManga(query: String): Flow<List<Manga>> {
        return flowOf(mangas.filter { it.title.contains(query, ignoreCase = true) })
    }

    override fun getFavoriteManga(): Flow<List<Manga>> {
        return flowOf(mangas.filter { it.isFavorite })
    }

    override fun getMangaByStatus(status: ReadingStatus): Flow<List<Manga>> {
        return flowOf(mangas.filter { it.readingStatus == status })
    }

    override fun getRecentManga(limit: Int): Flow<List<Manga>> {
        return flowOf(mangas.sortedByDescending { it.lastRead }.take(limit))
    }

    override suspend fun insertOrUpdateManga(manga: Manga): Long {
        val existingIndex = mangas.indexOfFirst { it.id == manga.id }
        val idToReturn: Long
        if (existingIndex != -1) {
            // Update existing manga
            mangas[existingIndex] = manga
            idToReturn = manga.id
        } else {
            // Add new manga, assign a new ID if it's 0
            val newManga = if (manga.id == 0L) {
                val newId = (mangas.maxOfOrNull { it.id } ?: 0L) + 1L
                manga.copy(id = newId)
            } else {
                manga
            }
            mangas.add(newManga)
            idToReturn = newManga.id
        }
        return idToReturn
    }

    override suspend fun insertAllManga(mangaList: List<Manga>): List<Long> {
        return mangaList.map { insertOrUpdateManga(it) }
    }

    override suspend fun updateReadingProgress(mangaId: Long, currentPage: Int, status: ReadingStatus) {
        val index = mangas.indexOfFirst { it.id == mangaId }
        if (index != -1) {
            mangas[index] = mangas[index].copy(
                currentPage = currentPage,
                readingStatus = status,
                lastRead = System.currentTimeMillis()
            )
        }
    }

    override suspend fun toggleFavorite(mangaId: Long) {
        val index = mangas.indexOfFirst { it.id == mangaId }
        if (index != -1) {
            mangas[index] = mangas[index].copy(isFavorite = !mangas[index].isFavorite)
        }
    }

    override suspend fun updateRating(mangaId: Long, rating: Float) {
        val index = mangas.indexOfFirst { it.id == mangaId }
        if (index != -1) {
            mangas[index] = mangas[index].copy(rating = rating)
        }
    }

    override suspend fun deleteManga(manga: Manga) {
        mangas.removeAll { it.id == manga.id }
    }

    override suspend fun deleteAllManga(mangaList: List<Manga>) {
        val idsToDelete = mangaList.map { it.id }.toSet()
        mangas.removeAll { idsToDelete.contains(it.id) }
    }

    override fun getMangaCount(): Flow<Int> {
        return flowOf(mangas.size)
    }

    override fun getFavoriteCount(): Flow<Int> {
        return flowOf(mangas.count { it.isFavorite })
    }

    override fun getCompletedCount(): Flow<Int> {
        return flowOf(mangas.count { it.readingStatus == ReadingStatus.COMPLETED })
    }

    override suspend fun getCover(manga: Manga): Bitmap? {
        // Return a dummy bitmap for testing
        return Bitmap.createBitmap(100, 150, Bitmap.Config.ARGB_8888)
    }
}