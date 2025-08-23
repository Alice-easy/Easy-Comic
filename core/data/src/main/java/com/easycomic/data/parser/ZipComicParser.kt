package com.easycomic.data.parser

import com.easycomic.domain.parser.ComicParser
import com.easycomic.data.util.NaturalOrderComparator
import com.easycomic.data.util.CoverExtractor
import java.io.File
import java.io.InputStream
import java.util.zip.ZipFile
import java.util.zip.ZipEntry

/**
 * ZIP格式漫画解析器
 * 支持自然序排序，确保页面按照正确的顺序显示
 * 实现智能封面提取和优化的内存管理
 */
class ZipComicParser(private val file: File) : ComicParser {

    private val zipFile = ZipFile(file)
    private val entries: List<ZipEntry>
    private val entryNames: List<String>

    init {
        // 获取所有图片文件并按优先级排序
        val imageEntries = zipFile.entries().asSequence()
            .filter { !it.isDirectory && isImageFile(it.name) }
            .toList()
        
        val sortedNames = CoverExtractor.sortImagesByPriority(
            imageEntries.map { it.name }
        )
        
        // 按排序后的名称重新组织条目
        entries = sortedNames.mapNotNull { name ->
            imageEntries.find { it.name == name }
        }
        
        entryNames = entries.map { it.name }
    }

    override fun getPageCount(): Int {
        return entries.size
    }

    override fun getPageStream(pageIndex: Int): InputStream? {
        if (pageIndex < 0 || pageIndex >= entries.size) {
            return null
        }
        return try {
            zipFile.getInputStream(entries[pageIndex])
        } catch (e: Exception) {
            null
        }
    }
    
    override fun getPageNames(): List<String> {
        return entryNames
    }
    
    override fun getCoverStream(): InputStream? {
        // 使用智能封面提取
        val coverIndex = findBestCoverIndex()
        return getPageStream(coverIndex)
    }
    
    override fun getPageSize(pageIndex: Int): Long {
        if (pageIndex < 0 || pageIndex >= entries.size) {
            return 0L
        }
        return entries[pageIndex].size
    }
    
    override fun supportsRandomAccess(): Boolean = true

    override fun close() {
        try {
            zipFile.close()
        } catch (e: Exception) {
            // 忽略关闭错误
        }
    }
    
    /**
     * 查找最佳封面索引
     */
    private fun findBestCoverIndex(): Int {
        // 查找明确的封面文件
        entryNames.forEachIndexed { index, name ->
            if (CoverExtractor.isCoverFile(name)) {
                return index
            }
        }
        
        // 没有找到明确封面，返回第一张图片
        return 0
    }

    private fun isImageFile(fileName: String): Boolean {
        return CoverExtractor.isImageFile(fileName)
    }
}