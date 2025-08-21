package com.easycomic.data.parser

import android.content.Context
import com.easycomic.data.repository.ComicFileInfo
import com.easycomic.data.repository.FileManager
import com.easycomic.domain.parser.ComicParser
import com.easycomic.domain.parser.ComicParserFactory
import java.io.File

/**
 * 漫画解析器工厂实现类
 * 支持传统文件系统和SAF文件访问
 */
class ComicParserFactoryImpl(
    private val context: Context
) {
    
    private val fileManager = FileManager(context)

    fun create(file: File): ComicParser? {
        val extension = file.extension.lowercase()
        return when (extension) {
            "zip", "cbz" -> ZipComicParser(file)
            "rar", "cbr" -> RarComicParser(file)
            else -> null
        }
    }
    
    /**
     * 创建解析器（支持SAF文件）
     * @param comicFileInfo 漫画文件信息
     * @return 对应的解析器，如果格式不支持则返回null
     */
    fun createParserForSAF(comicFileInfo: ComicFileInfo): ComicParser? {
        val extension = comicFileInfo.name.substringAfterLast('.', "").lowercase()
        
        return when (extension) {
            "zip", "cbz" -> {
                if (comicFileInfo.isFromSAF) {
                    // SAF文件需要通过输入流访问
                    SAFZipComicParser(context, comicFileInfo)
                } else {
                    // 传统文件可以直接访问
                    val file = File(comicFileInfo.filePath!!)
                    ZipComicParser(file)
                }
            }
            "rar", "cbr" -> {
                if (comicFileInfo.isFromSAF) {
                    // SAF文件需要通过输入流访问
                    SAFRarComicParser(context, comicFileInfo)
                } else {
                    // 传统文件可以直接访问
                    val file = File(comicFileInfo.filePath!!)
                    RarComicParser(file)
                }
            }
            else -> null
        }
    }
}
