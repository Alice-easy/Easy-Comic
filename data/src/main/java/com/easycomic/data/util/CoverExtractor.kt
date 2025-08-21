package com.easycomic.data.util

import com.easycomic.domain.parser.ComicParser
import java.io.InputStream
import java.io.ByteArrayOutputStream

/**
 * 漫画封面提取工具
 * 实现智能封面选择算法，支持多种封面识别策略
 */
class CoverExtractor {
    
    companion object {
        private val COVER_NAMES = listOf(
            "cover", "front", "poster", "thumbnail", "thumb",
            "封面", "表紙", "表纸", "カバー"
        )
        
        private val PREFERRED_EXTENSIONS = listOf("jpg", "jpeg", "png", "webp")
        
        /**
         * 从漫画解析器中提取封面
         * @param parser 漫画解析器
         * @return 封面图片的字节数组，如果没有找到合适的封面则返回null
         */
        fun extractCover(parser: ComicParser): ByteArray? {
            if (parser.getPageCount() == 0) {
                return null
            }
            
            // 策略1: 查找专用封面文件
            val explicitCover = findExplicitCover(parser)
            if (explicitCover != null) {
                return explicitCover
            }
            
            // 策略2: 使用第一张图片作为封面
            return getFirstPageImage(parser)
        }
        
        /**
         * 查找明确标记为封面的文件
         */
        private fun findExplicitCover(parser: ComicParser): ByteArray? {
            // 这里需要访问解析器的内部文件列表
            // 由于当前的ComicParser接口限制，我们先跳过这个策略
            // 在实际实现中，可能需要扩展ComicParser接口来支持获取文件列表
            return null
        }
        
        /**
         * 获取第一张图片作为封面
         */
        private fun getFirstPageImage(parser: ComicParser): ByteArray? {
            val firstPageStream = parser.getPageStream(0)
            return firstPageStream?.use { inputStream ->
                val buffer = ByteArrayOutputStream()
                inputStream.copyTo(buffer)
                buffer.toByteArray()
            }
        }
        
        /**
         * 检查文件名是否为封面文件
         */
        fun isCoverFile(fileName: String): Boolean {
            val lowerFileName = fileName.lowercase()
            val nameWithoutExtension = lowerFileName.substringBeforeLast('.')
            
            return COVER_NAMES.any { coverName ->
                nameWithoutExtension.contains(coverName) ||
                nameWithoutExtension == coverName ||
                nameWithoutExtension.startsWith(coverName) ||
                nameWithoutExtension.endsWith(coverName)
            }
        }
        
        /**
         * 检查文件是否为图片格式
         */
        fun isImageFile(fileName: String): Boolean {
            val extension = fileName.substringAfterLast('.', "").lowercase()
            return extension in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
        }
        
        /**
         * 根据优先级对图片文件进行排序
         * 封面文件优先，然后按照扩展名优先级和自然序排序
         */
        fun sortImagesByPriority(fileNames: List<String>): List<String> {
            return fileNames.sortedWith { name1, name2 ->
                val isCover1 = isCoverFile(name1)
                val isCover2 = isCoverFile(name2)
                
                when {
                    isCover1 && !isCover2 -> -1
                    !isCover1 && isCover2 -> 1
                    else -> {
                        // 都是封面或都不是封面，按扩展名优先级排序
                        val ext1 = name1.substringAfterLast('.', "").lowercase()
                        val ext2 = name2.substringAfterLast('.', "").lowercase()
                        
                        val priority1 = PREFERRED_EXTENSIONS.indexOf(ext1).takeIf { it >= 0 } ?: Int.MAX_VALUE
                        val priority2 = PREFERRED_EXTENSIONS.indexOf(ext2).takeIf { it >= 0 } ?: Int.MAX_VALUE
                        
                        if (priority1 != priority2) {
                            priority1.compareTo(priority2)
                        } else {
                            // 扩展名优先级相同，使用自然序排序
                            NaturalOrderComparator().compare(name1, name2)
                        }
                    }
                }
            }
        }
    }
}
