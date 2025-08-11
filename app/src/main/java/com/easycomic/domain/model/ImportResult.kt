package com.easycomic.domain.model

/**
 * 导入漫画结果模型
 */
data class ImportComicResult(
    val success: Boolean,
    val mangaId: Long? = null,
    val manga: Manga? = null,
    val error: String? = null,
    val progress: Int = 0
)

/**
 * 批量导入漫画结果模型
 */
data class BatchImportComicResult(
    val success: Boolean,
    val totalItems: Int = 0,
    val importedCount: Int = 0,
    val failedCount: Int = 0,
    val results: List<ImportComicResult> = emptyList(),
    val errors: List<String> = emptyList()
)

/**
 * 导入状态枚举
 */
enum class ImportStatus {
    IDLE,           // 空闲
    VALIDATING,     // 验证中
    PARSING,        // 解析中
    EXTRACTING,     // 提取中
    SAVING,         // 保存中
    COMPLETED,      // 完成
    FAILED,         // 失败
    CANCELLED       // 取消
}

/**
 * 导入进度信息
 */
data class ImportProgress(
    val status: ImportStatus = ImportStatus.IDLE,
    val progress: Int = 0,
    val currentFile: String? = null,
    val totalFiles: Int = 0,
    val processedFiles: Int = 0,
    val message: String? = null
)