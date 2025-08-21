package com.easycomic.domain.model

import com.easycomic.domain.model.ReadingStatus

/**
 * 漫画领域模型
 * 
 * 表示漫画的核心业务实体，包含漫画的所有基本信息和状态。
 * 该模型遵循Clean Architecture原则，不依赖任何外部框架。
 * 
 * ## 主要功能：
 * - 存储漫画基本信息（标题、作者、描述等）
 * - 管理文件相关信息（路径、格式、大小等）
 * - 跟踪阅读状态和进度
 * - 提供便利的计算属性和格式化方法
 * 
 * @property id 漫画唯一标识符，0表示新创建的漫画
 * @property title 漫画标题，必填字段
 * @property author 漫画作者，可选字段
 * @property description 漫画描述，可选字段
 * @property filePath 漫画文件路径，必填字段
 * @property fileUri 文件URI，用于SAF访问，可选
 * @property fileFormat 文件格式（如ZIP、RAR等）
 * @property fileSize 文件大小（字节）
 * @property pageCount 漫画总页数
 * @property currentPage 当前阅读页数
 * @property coverImagePath 封面图片路径，可选
 * @property thumbnailPath 缩略图路径，可选
 * @property rating 用户评分（0.0-5.0）
 * @property isFavorite 是否收藏
 * @property readingStatus 阅读状态
 * @property tags 标签列表
 * @property lastRead 最后阅读时间戳
 * @property dateAdded 添加时间戳
 * @property dateModified 修改时间戳
 * 
 * @author EasyComic Team
 * @since 1.0.0
 */
data class Manga(
    val id: Long = 0,
    val title: String,
    val author: String = "",
    val description: String = "",
    val filePath: String,
    val fileUri: String? = null,
    val fileFormat: String = "",
    val fileSize: Long = 0,
    val pageCount: Int = 0,
    val currentPage: Int = 0,
    val coverImagePath: String? = null,
    val thumbnailPath: String? = null,
    val rating: Float = 0f,
    val isFavorite: Boolean = false,
    val readingStatus: ReadingStatus = ReadingStatus.UNREAD,
    val tags: List<String> = emptyList(),
    val lastRead: Long = System.currentTimeMillis(),
    val dateAdded: Long = System.currentTimeMillis(),
    val dateModified: Long = System.currentTimeMillis()
) {
    /**
     * 获取阅读进度百分比
     * 
     * @return Float 阅读进度百分比（0.0-100.0）
     */
    val progressPercentage: Float
        get() = if (pageCount > 0) (currentPage.toFloat() / pageCount.toFloat()) * 100f else 0f
    
    /**
     * 获取格式化的文件大小
     * 
     * @return String 格式化后的文件大小字符串（如"1.5 MB"）
     */
    val formattedFileSize: String
        get() = formatFileSize(fileSize)
    
    /**
     * 是否已读完
     * 
     * @return Boolean true表示已读完，false表示未读完
     */
    val isCompleted: Boolean
        get() = readingStatus == ReadingStatus.COMPLETED || currentPage >= pageCount
    
    /**
     * 获取标签字符串
     * 
     * @return String 逗号分隔的标签字符串
     */
    val tagsString: String
        get() = tags.joinToString(", ")
    
    /**
     * 格式化文件大小
     * 
     * 将字节数转换为人类可读的格式（B、KB、MB、GB）
     * 
     * @param size 文件大小（字节）
     * @return String 格式化后的文件大小字符串
     */
    private fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
            else -> "${size / (1024 * 1024 * 1024)} GB"
        }
    }
}
