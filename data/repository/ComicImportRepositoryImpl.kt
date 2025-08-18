package com.easycomic.data.repository

import android.content.Context
import com.easycomic.data.dao.MangaDao
import com.easycomic.data.entity.MangaEntity
import com.easycomic.domain.parser.ComicParser
import com.easycomic.data.parser.RarComicParser
import com.easycomic.data.parser.ZipComicParser
import com.easycomic.domain.repository.ComicImportRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ComicImportRepositoryImpl(
    private val context: Context,
    private val mangaDao: MangaDao
) : ComicImportRepository {

    override suspend fun importComics(directory: File) = withContext(Dispatchers.IO) {
        if (!directory.isDirectory) return@withContext

        directory.listFiles()?.forEach { file ->
            val parser = getParserForFile(file)
            if (parser != null) {
                try {
                    val pageCount = parser.getPageCount()
                    if (pageCount > 0) {
                        val manga = MangaEntity(
                            title = file.nameWithoutExtension,
                            filePath = file.absolutePath,
                            fileSize = file.length(),
                            format = file.extension.uppercase(),
                            pageCount = pageCount,
                            dateAdded = System.currentTimeMillis()
                        )
                        mangaDao.insertOrUpdateManga(manga)
                    }
                } finally {
                    parser.close()
                }
            }
        }
    }

    private fun getParserForFile(file: File): ComicParser? {
        val extension = file.extension.lowercase()
        return when (extension) {
            "zip", "cbz" -> ZipComicParser(file)
            "rar", "cbr" -> RarComicParser(file)
            else -> null
        }
    }
}
