package com.easycomic.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * 文件处理工具类
 */
object FileHelper {
    
    /**
     * 从 URI 获取文件路径
     */
    fun getFilePathFromUri(context: Context, uri: Uri): String? {
        return when (uri.scheme) {
            ContentResolver.SCHEME_FILE -> uri.path
            ContentResolver.SCHEME_CONTENT -> {
                // 对于 content URI，尝试复制到缓存目录
                val fileName = getFileNameFromUri(context, uri)
                val cacheFile = File(context.cacheDir, fileName)
                
                try {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        FileOutputStream(cacheFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    cacheFile.absolutePath
                } catch (e: Exception) {
                    Timber.e(e, "复制文件失败")
                    null
                }
            }
            else -> null
        }
    }
    
    /**
     * 从 URI 获取文件名
     */
    fun getFileNameFromUri(context: Context, uri: Uri): String {
        var fileName = "unknown_file"
        
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    fileName = it.getString(displayNameIndex)
                }
            }
        }
        
        return fileName
    }
    
    /**
     * 检查文件是否为支持的漫画格式
     */
    fun isSupportedComicFile(fileName: String): Boolean {
        val lowerCaseName = fileName.lowercase()
        return lowerCaseName.endsWith(".zip") ||
               lowerCaseName.endsWith(".cbz") ||
               lowerCaseName.endsWith(".rar") ||
               lowerCaseName.endsWith(".cbr")
    }
    
    /**
     * 获取支持的漫画文件扩展名
     */
    fun getSupportedExtensions(): Array<String> {
        return arrayOf(".zip", ".cbz", ".rar", ".cbr")
    }
    
    /**
     * 创建文件 URI 用于 FileProvider
     */
    fun getFileUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
    
    /**
     * 获取默认的漫画存储目录
     */
    fun getDefaultComicDirectory(context: Context): File {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ 使用应用专属目录
            File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "comics")
        } else {
            // Android 9 及以下使用外部存储
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "EasyComic/comics")
        }
    }
    
    /**
     * 创建漫画存储目录
     */
    fun createComicDirectory(context: Context): File {
        val comicDir = getDefaultComicDirectory(context)
        if (!comicDir.exists()) {
            val created = comicDir.mkdirs()
            if (!created) {
                Timber.e("无法创建漫画目录: ${comicDir.absolutePath}")
            }
        }
        return comicDir
    }
    
    /**
     * 计算文件大小
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
     * 检查文件是否存在
     */
    fun fileExists(filePath: String): Boolean {
        return File(filePath).exists()
    }
    
    /**
     * 删除文件
     */
    fun deleteFile(filePath: String): Boolean {
        return try {
            File(filePath).delete()
        } catch (e: Exception) {
            Timber.e(e, "删除文件失败: $filePath")
            false
        }
    }
    
    /**
     * 获取文件扩展名
     */
    fun getFileExtension(fileName: String): String {
        val dotIndex = fileName.lastIndexOf('.')
        return if (dotIndex > 0) {
            fileName.substring(dotIndex).lowercase()
        } else {
            ""
        }
    }
}