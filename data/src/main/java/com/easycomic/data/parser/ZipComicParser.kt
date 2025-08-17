package com.easycomic.data.parser

import java.io.File
import java.io.InputStream
import java.util.zip.ZipFile

/**
 * ZIP格式漫画解析器
 */
class ZipComicParser(private val file: File) : ComicParser {

    private val zipFile = ZipFile(file)
    private val entries = zipFile.entries().asSequence().filter {
        !it.isDirectory && isImageFile(it.name)
    }.sortedBy { it.name }.toList()

    override fun getPageCount(): Int {
        return entries.size
    }

    override fun getPageStream(pageIndex: Int): InputStream? {
        if (pageIndex < 0 || pageIndex >= entries.size) {
            return null
        }
        return zipFile.getInputStream(entries[pageIndex])
    }

    override fun close() {
        zipFile.close()
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