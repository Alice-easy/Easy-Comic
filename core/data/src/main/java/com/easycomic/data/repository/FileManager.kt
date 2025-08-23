package com.easycomic.data.repository

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * 文件管理工具类，处理SAF和传统文件系统
 */
class FileManager(private val context: Context) {
    
    /**
     * 支持的漫画文件格式
     */
    private val supportedFormats = setOf("zip", "cbz", "rar", "cbr", "pdf")
    
    /**
     * 从Document Tree URI导入漫画文件
     * @param treeUri 文档树URI
     * @return 导入的文件列表
     */
    suspend fun importFromDocumentTree(treeUri: Uri): List<ComicFileInfo> = withContext(Dispatchers.IO) {
        val documentTree = DocumentFile.fromTreeUri(context, treeUri)
            ?: return@withContext emptyList()
        
        val comicFiles = mutableListOf<ComicFileInfo>()
        scanDocumentTree(documentTree, comicFiles)
        
        comicFiles
    }
    
    /**
     * 递归扫描文档树中的漫画文件
     */
    private fun scanDocumentTree(documentFile: DocumentFile, comicFiles: MutableList<ComicFileInfo>) {
        documentFile.listFiles().forEach { file ->
            when {
                file.isDirectory -> {
                    // 递归扫描子目录
                    scanDocumentTree(file, comicFiles)
                }
                file.isFile && isComicFile(file.name) -> {
                    comicFiles.add(
                        ComicFileInfo(
                            uri = file.uri,
                            name = file.name ?: "未知文件",
                            size = file.length(),
                            isFromSAF = true,
                            lastModified = file.lastModified()
                        )
                    )
                }
            }
        }
    }
    
    /**
     * 从传统文件系统导入漫画文件
     * @param directory 目录文件对象
     * @return 导入的文件列表
     */
    suspend fun importFromDirectory(directory: File): List<ComicFileInfo> = withContext(Dispatchers.IO) {
        if (!directory.isDirectory) return@withContext emptyList()
        
        val comicFiles = mutableListOf<ComicFileInfo>()
        scanDirectory(directory, comicFiles)
        
        comicFiles
    }
    
    /**
     * 递归扫描目录中的漫画文件
     */
    private fun scanDirectory(directory: File, comicFiles: MutableList<ComicFileInfo>) {
        directory.listFiles()?.forEach { file ->
            when {
                file.isDirectory -> {
                    // 递归扫描子目录
                    scanDirectory(file, comicFiles)
                }
                file.isFile && isComicFile(file.name) -> {
                    comicFiles.add(
                        ComicFileInfo(
                            uri = Uri.fromFile(file),
                            name = file.name,
                            size = file.length(),
                            isFromSAF = false,
                            lastModified = file.lastModified(),
                            filePath = file.absolutePath
                        )
                    )
                }
            }
        }
    }
    
    /**
     * 检查文件是否为支持的漫画格式
     */
    private fun isComicFile(fileName: String?): Boolean {
        if (fileName == null) return false
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return extension in supportedFormats
    }
    
    /**
     * 获取文件输入流（支持SAF和传统文件）
     */
    suspend fun getInputStream(comicFileInfo: ComicFileInfo): InputStream? = withContext(Dispatchers.IO) {
        try {
            if (comicFileInfo.isFromSAF) {
                context.contentResolver.openInputStream(comicFileInfo.uri)
            } else {
                File(comicFileInfo.filePath ?: return@withContext null).inputStream()
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 将SAF文件复制到应用内部存储（可选功能）
     * @param comicFileInfo 漫画文件信息
     * @return 复制后的文件路径，失败返回null
     */
    suspend fun copyToInternalStorage(comicFileInfo: ComicFileInfo): String? = withContext(Dispatchers.IO) {
        if (!comicFileInfo.isFromSAF) {
            // 已经是本地文件
            return@withContext comicFileInfo.filePath
        }
        
        try {
            val inputStream = context.contentResolver.openInputStream(comicFileInfo.uri) ?: return@withContext null
            
            // 创建内部存储目录
            val comicsDir = File(context.filesDir, "comics")
            if (!comicsDir.exists()) {
                comicsDir.mkdirs()
            }
            
            // 生成唯一文件名
            val fileName = "${System.currentTimeMillis()}_${comicFileInfo.name}"
            val outputFile = File(comicsDir, fileName)
            
            // 复制文件
            inputStream.use { input ->
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            outputFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 获取可读的文件大小字符串
     */
    fun formatFileSize(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kilobytes = bytes / 1024.0
        if (kilobytes < 1024) return "%.1f KB".format(kilobytes)
        val megabytes = kilobytes / 1024.0
        if (megabytes < 1024) return "%.1f MB".format(megabytes)
        val gigabytes = megabytes / 1024.0
        return "%.1f GB".format(gigabytes)
    }
}

/**
 * 漫画文件信息数据类
 */
data class ComicFileInfo(
    val uri: Uri,
    val name: String,
    val size: Long,
    val isFromSAF: Boolean,
    val lastModified: Long,
    val filePath: String? = null
)
