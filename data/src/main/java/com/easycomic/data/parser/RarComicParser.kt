package com.easycomic.data.parser

import com.easycomic.domain.parser.ComicParser
import com.easycomic.data.util.NaturalOrderComparator
import com.easycomic.data.util.CoverExtractor
import com.github.junrar.Junrar
import java.io.File
import java.io.InputStream

/**
 * RAR格式漫画解析器
 * 支持自然序排序，确保页面按照正确的顺序显示
 * 实现智能封面提取和优化的内存管理
 * 
 * 注意：RAR解析需要完全解压，对于大文件可能消耗较多磁盘空间
 */
class RarComicParser(private val file: File) : ComicParser {

    private val tempDir: File = createTempDirectory()
    private val imageFiles: List<File>
    private val fileNames: List<String>

    init {
        try {
            // Junrar extracts all files to a temporary directory
            Junrar.extract(file, tempDir)
            
            val allImageFiles = tempDir.walk()
                .filter { it.isFile && isImageFile(it.name) }
                .toList()
            
            // 按优先级排序（封面优先，然后自然序）
            val sortedNames = CoverExtractor.sortImagesByPriority(
                allImageFiles.map { it.name }
            )
            
            // 按排序后的名称重新组织文件列表
            imageFiles = sortedNames.mapNotNull { name ->
                allImageFiles.find { it.name == name }
            }
            
            fileNames = imageFiles.map { it.name }
        } catch (e: Exception) {
            throw RuntimeException("Failed to extract RAR file: ${file.name}", e)
        }
    }

    override fun getPageCount(): Int {
        return imageFiles.size
    }

    override fun getPageStream(pageIndex: Int): InputStream? {
        if (pageIndex < 0 || pageIndex >= imageFiles.size) {
            return null
        }
        return try {
            imageFiles[pageIndex].inputStream()
        } catch (e: Exception) {
            null
        }
    }
    
    override fun getPageNames(): List<String> {
        return fileNames
    }
    
    override fun getCoverStream(): InputStream? {
        // 使用智能封面提取
        val coverIndex = findBestCoverIndex()
        return getPageStream(coverIndex)
    }
    
    override fun getPageSize(pageIndex: Int): Long {
        if (pageIndex < 0 || pageIndex >= imageFiles.size) {
            return 0L
        }
        return imageFiles[pageIndex].length()
    }
    
    override fun supportsRandomAccess(): Boolean = true

    override fun close() {
        // Clean up the temporary directory
        try {
            tempDir.deleteRecursively()
        } catch (e: Exception) {
            // 忽略清理错误，但记录日志
            System.err.println("Warning: Failed to clean up temporary directory: ${tempDir.absolutePath}")
        }
    }
    
    /**
     * 查找最佳封面索引
     */
    private fun findBestCoverIndex(): Int {
        // 查找明确的封面文件
        fileNames.forEachIndexed { index, name ->
            if (CoverExtractor.isCoverFile(name)) {
                return index
            }
        }
        
        // 没有找到明确封面，返回第一张图片
        return 0
    }

    private fun createTempDirectory(): File {
        // 兼容 API 24+ 的临时目录创建方法
        val tempDir = File(System.getProperty("java.io.tmpdir"), "easycomic_rar_${System.currentTimeMillis()}")
        if (!tempDir.mkdirs() && !tempDir.exists()) {
            throw RuntimeException("Failed to create temporary directory: ${tempDir.absolutePath}")
        }
        return tempDir
    }

    private fun isImageFile(fileName: String): Boolean {
        return CoverExtractor.isImageFile(fileName)
    }
}