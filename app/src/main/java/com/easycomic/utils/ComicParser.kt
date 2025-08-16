package com.easycomic.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.github.junrar.api.Archive
import com.github.junrar.impl.FileVolumeManager
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.zip.ZipFile

interface ComicParser {
    fun getPageCount(): Int
    fun getPageStream(pageNumber: Int): InputStream?
    fun getCover(): Bitmap?
    fun close()
}

class ZipComicParser(private val file: File) : ComicParser {

    private val zipFile = ZipFile(file)
    private val entries = zipFile.entries().asSequence()
        .filter { !it.isDirectory && isImageFile(it.name) }
        .sortedBy { it.name }
        .toList()

    override fun getPageCount(): Int = entries.size

    override fun getPageStream(pageNumber: Int): InputStream? {
        if (pageNumber !in entries.indices) return null
        return zipFile.getInputStream(entries[pageNumber])
    }

    override fun getCover(): Bitmap? = getPageStream(0)?.use { BitmapFactory.decodeStream(it) }

    override fun close() {
        zipFile.close()
    }
}

class RarComicParser(private val file: File) : ComicParser {

    private val archive = Archive(FileVolumeManager(file))
    private val fileHeaders = archive.fileHeaders
        .filter { !it.isDirectory && isImageFile(it.fileName) }
        .sortedBy { it.fileName }

    override fun getPageCount(): Int = fileHeaders.size

    override fun getPageStream(pageNumber: Int): InputStream? {
        if (pageNumber !in fileHeaders.indices) return null
        
        val header = fileHeaders[pageNumber]
        val outputStream = ByteArrayOutputStream()
        archive.extractFile(header, outputStream)
        return ByteArrayInputStream(outputStream.toByteArray())
    }

    override fun getCover(): Bitmap? = getPageStream(0)?.use { BitmapFactory.decodeStream(it) }

    override fun close() {
        archive.close()
    }
}

private fun isImageFile(fileName: String): Boolean {
    val lowercased = fileName.lowercase()
    return lowercased.endsWith(".jpg") || lowercased.endsWith(".jpeg") ||
           lowercased.endsWith(".png") || lowercased.endsWith(".webp") || lowercased.endsWith(".gif")
}
