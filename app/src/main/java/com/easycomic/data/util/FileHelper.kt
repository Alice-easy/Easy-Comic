package com.easycomic.data.util

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import timber.log.Timber
import java.io.*
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes

/**
 * 文件操作工具类
 */
object FileHelper {
    
    /**
     * 获取文件名
     */
    fun getFileName(context: Context, uri: Uri): String {
        var name = ""
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst()) {
                name = it.getString(nameIndex)
            }
        }
        if (name.isBlank()) {
            name = uri.path?.substringAfterLast('/') ?: "unknown"
        }
        return name
    }
    
    /**
     * 获取文件大小
     */
    fun getFileSize(context: Context, uri: Uri): Long {
        var size: Long = 0
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
            if (it.moveToFirst()) {
                size = it.getLong(sizeIndex)
            }
        }
        
        // 如果cursor没有获取到大小，尝试从文件流获取
        if (size == 0L) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    size = stream.available().toLong()
                }
            } catch (e: Exception) {
                Timber.e(e, "获取文件大小失败")
            }
        }
        
        return size
    }
    
    /**
     * 获取文件路径
     */
    fun getFilePath(context: Context, uri: Uri): String {
        var path = uri.path ?: ""
        
        // 如果是内容URI，尝试获取真实路径
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            path = getRealPathFromURI(context, uri) ?: path
        }
        
        return path
    }
    
    /**
     * 获取文件MIME类型
     */
    fun getMimeType(context: Context, uri: Uri): String? {
        var mimeType: String? = context.contentResolver.getType(uri)
        if (mimeType == null) {
            val extension = getFileExtension(uri)
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return mimeType
    }
    
    /**
     * 获取文件扩展名
     */
    fun getFileExtension(context: Context, uri: Uri): String {
        val fileName = getFileName(context, uri)
        return fileName.substringAfterLast('.', "").lowercase()
    }
    
    /**
     * 获取文件扩展名
     */
    fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast('.', "").lowercase()
    }
    
    /**
     * 检查文件是否存在
     */
    fun fileExists(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.read() != -1
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 复制文件到指定目录
     */
    fun copyFile(context: Context, uri: Uri, destDir: File): File? {
        return try {
            val fileName = getFileName(context, uri)
            val destFile = File(destDir, fileName)
            
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            destFile
        } catch (e: Exception) {
            Timber.e(e, "复制文件失败")
            null
        }
    }
    
    /**
     * 删除文件
     */
    fun deleteFile(file: File): Boolean {
        return try {
            file.delete()
        } catch (e: Exception) {
            Timber.e(e, "删除文件失败")
            false
        }
    }
    
    /**
     * 删除文件
     */
    fun deleteFile(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.delete(uri, null, null) > 0
        } catch (e: Exception) {
            Timber.e(e, "删除文件失败")
            false
        }
    }
    
    /**
     * 创建目录
     */
    fun createDirectory(dir: File): Boolean {
        return try {
            if (!dir.exists()) {
                dir.mkdirs()
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "创建目录失败")
            false
        }
    }
    
    /**
     * 格式化文件大小
     */
    fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
            else -> "${size / (1024 * 1024 * 1024)} GB"
        }
    }
    
    /**
     * 检查是否为支持的漫画格式
     */
    fun isSupportedComicFormat(context: Context, uri: Uri): Boolean {
        val extension = getFileExtension(context, uri)
        return SUPPORTED_COMIC_FORMATS.contains(extension)
    }
    
    /**
     * 检查是否为图片文件
     */
    fun isImageFile(context: Context, uri: Uri): Boolean {
        val extension = getFileExtension(context, uri)
        return IMAGE_EXTENSIONS.contains(extension)
    }
    
    /**
     * 获取文件修改时间
     */
    fun getLastModified(context: Context, uri: Uri): Long {
        return try {
            val path = getFilePath(context, uri)
            if (path.isNotBlank()) {
                val file = File(path)
                if (file.exists()) {
                    return file.lastModified()
                }
            }
            
            // 尝试从cursor获取
            var lastModified: Long = 0
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val modifiedIndex = it.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                if (it.moveToFirst() && modifiedIndex != -1) {
                    lastModified = it.getLong(modifiedIndex)
                }
            }
            
            if (lastModified == 0L) {
                lastModified = System.currentTimeMillis()
            }
            
            lastModified
        } catch (e: Exception) {
            Timber.e(e, "获取文件修改时间失败")
            System.currentTimeMillis()
        }
    }
    
    /**
     * 读取文件为字节数组
     */
    fun readFileToBytes(context: Context, uri: Uri): ByteArray? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.readBytes()
            }
        } catch (e: Exception) {
            Timber.e(e, "读取文件失败")
            null
        }
    }
    
    /**
     * 从URI获取真实文件路径
     */
    private fun getRealPathFromURI(context: Context, uri: Uri): String? {
        val docId = DocumentsContract.getDocumentId(uri)
        val split = docId.split(":")
        val type = split[0]
        val path = if (type == "primary") {
            "/storage/emulated/0/${split[1]}"
        } else {
            "/storage/${type}/${split[1]}"
        }
        
        // 检查文件是否存在
        val file = File(path)
        return if (file.exists()) path else null
    }
    
    companion object {
        // 支持的漫画格式
        val SUPPORTED_COMIC_FORMATS = listOf("zip", "cbz", "rar", "cbr", "7z", "cb7")
        
        // 支持的图片格式
        val IMAGE_EXTENSIONS = listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
        
        // 常用的漫画文件扩展名
        val COMIC_EXTENSIONS = listOf("cbz", "cbr", "cb7", "cbt")
    }
}