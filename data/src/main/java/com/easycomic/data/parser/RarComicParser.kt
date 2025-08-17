package com.easycomic.data.parser

import com.github.junrar.Junrar
import com.github.junrar.rarfile.FileHeader
import java.io.File
import java.io.InputStream
import java.nio.file.Files

/**
 * RAR格式漫画解析器
 */
class RarComicParser(private val file: File) : ComicParser {

    private val tempDir: File = Files.createTempDirectory("easycomic_rar_").toFile()
    private val imageFiles: List<File>

    init {
        // Junrar extracts all files to a temporary directory
        Junrar.extract(file, tempDir)
        imageFiles = tempDir.walk()
            .filter { it.isFile && isImageFile(it.name) }
            .sortedBy { it.name }
            .toList()
    }

    override fun getPageCount(): Int {
        return imageFiles.size
    }

    override fun getPageStream(pageIndex: Int): InputStream? {
        if (pageIndex < 0 || pageIndex >= imageFiles.size) {
            return null
        }
        return imageFiles[pageIndex].inputStream()
    }

    override fun close() {
        // Clean up the temporary directory
        tempDir.deleteRecursively()
    }

    private fun isImageFile(fileName: String): Boolean {
        val lowerCaseName = fileName.lowercase()
        return lowerCaseName.endsWith(".jpg") ||
               lowerCaseName.endsWith(".jpeg") ||
               lowerCaseName.endsWith(".png") ||
               lowerCaseName.endsWith(".gif") ||
               lowerCaseName.endsWith(".webp")
    }
}