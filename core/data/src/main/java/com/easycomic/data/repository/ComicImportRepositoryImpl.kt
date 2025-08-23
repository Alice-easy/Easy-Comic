package com.easycomic.data.repository

import android.content.Context
import android.net.Uri
import com.easycomic.data.dao.MangaDao
import com.easycomic.data.entity.MangaEntity
import com.easycomic.domain.parser.ComicParser
import com.easycomic.data.parser.RarComicParser
import com.easycomic.data.parser.ZipComicParser
import com.easycomic.domain.repository.ComicImportRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.io.File

class ComicImportRepositoryImpl(
    private val context: Context,
    private val mangaDao: MangaDao
) : ComicImportRepository {

    private val fileManager = FileManager(context)

    override suspend fun importComics(directory: File) = withContext(Dispatchers.IO) {
        if (!directory.isDirectory) return@withContext

        val comicFiles = fileManager.importFromDirectory(directory)
        processComicFiles(comicFiles)
    }
    
    /**
     * 从Document Tree URI导入漫画
     * @param treeUri Document Tree URI
     * @return 导入进度Flow
     */
    suspend fun importFromDocumentTree(treeUri: Uri): Flow<ImportProgress> = flow {
        emit(ImportProgress.Scanning)
        
        try {
            val comicFiles = fileManager.importFromDocumentTree(treeUri)
            emit(ImportProgress.Found(comicFiles.size))
            
            var processed = 0
            comicFiles.forEach { comicFile ->
                try {
                    processComicFile(comicFile)
                    processed++
                    emit(ImportProgress.Processing(processed, comicFiles.size))
                } catch (e: Exception) {
                    Timber.e(e, "Failed to process comic file: ${comicFile.name}")
                }
            }
            
            emit(ImportProgress.Completed(processed))
        } catch (e: Exception) {
            Timber.e(e, "Failed to import from document tree")
            emit(ImportProgress.Error(e.message ?: "导入失败"))
        }
    }
    
    /**
     * 处理多个漫画文件
     */
    private suspend fun processComicFiles(comicFiles: List<ComicFileInfo>) {
        comicFiles.forEach { comicFile ->
            try {
                processComicFile(comicFile)
            } catch (e: Exception) {
                Timber.e(e, "Failed to process comic file: ${comicFile.name}")
            }
        }
    }
    
    /**
     * 处理单个漫画文件
     */
    private suspend fun processComicFile(comicFileInfo: ComicFileInfo) {
        // 检查文件是否已存在
        val existingManga = mangaDao.getMangaByFilePath(comicFileInfo.filePath ?: comicFileInfo.uri.toString())
        if (existingManga != null) {
            Timber.d("Comic already exists: ${comicFileInfo.name}")
            return
        }
        
        val parser = createParserForFile(comicFileInfo) ?: return
        
        try {
            val pageCount = parser.getPageCount()
            if (pageCount > 0) {
                // 获取封面
                val coverImagePath = extractAndSaveCover(parser, comicFileInfo)
                
                val manga = MangaEntity(
                    title = comicFileInfo.name.substringBeforeLast('.'),
                    filePath = comicFileInfo.filePath ?: comicFileInfo.uri.toString(),
                    fileSize = comicFileInfo.size,
                    format = comicFileInfo.name.substringAfterLast('.', "").uppercase(),
                    pageCount = pageCount,
                    dateAdded = System.currentTimeMillis(),
                    lastModified = comicFileInfo.lastModified,
                    coverImagePath = coverImagePath,
                    isFromSAF = comicFileInfo.isFromSAF
                )
                
                mangaDao.insertOrUpdateManga(manga)
                Timber.d("Successfully imported: ${comicFileInfo.name}")
            }
        } finally {
            parser.close()
        }
    }
    
    /**
     * 为文件创建解析器
     */
    private fun createParserForFile(comicFileInfo: ComicFileInfo): ComicParser? {
        val extension = comicFileInfo.name.substringAfterLast('.', "").lowercase()
        
        return when (extension) {
            "zip", "cbz" -> {
                // 对于SAF文件，暂时跳过，等实现完善后再启用
                if (comicFileInfo.isFromSAF) {
                    null // TODO: 实现SAF ZIP解析器
                } else {
                    // 传统文件可以直接访问
                    val file = File(comicFileInfo.filePath!!)
                    ZipComicParser(file)
                }
            }
            "rar", "cbr" -> {
                // 对于SAF文件，暂时跳过，等实现完善后再启用
                if (comicFileInfo.isFromSAF) {
                    null // TODO: 实现SAF RAR解析器
                } else {
                    // 传统文件可以直接访问
                    val file = File(comicFileInfo.filePath!!)
                    RarComicParser(file)
                }
            }
            else -> null
        }
    }
    
    /**
     * 提取并保存封面图片
     */
    private suspend fun extractAndSaveCover(
        parser: ComicParser,
        comicFileInfo: ComicFileInfo
    ): String? = withContext(Dispatchers.IO) {
        try {
            val coverStream = parser.getCoverStream()
            if (coverStream != null) {
                val coversDir = File(context.filesDir, "covers")
                if (!coversDir.exists()) {
                    coversDir.mkdirs()
                }
                
                val fileName = "${System.currentTimeMillis()}_${comicFileInfo.name.hashCode()}.jpg"
                val coverFile = File(coversDir, fileName)
                
                coverStream.use { input ->
                    coverFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                coverFile.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to extract cover for: ${comicFileInfo.name}")
            null
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

/**
 * 导入进度状态
 */
sealed class ImportProgress {
    object Scanning : ImportProgress()
    data class Found(val count: Int) : ImportProgress()
    data class Processing(val processed: Int, val total: Int) : ImportProgress()
    data class Completed(val imported: Int) : ImportProgress()
    data class Error(val message: String) : ImportProgress()
}
