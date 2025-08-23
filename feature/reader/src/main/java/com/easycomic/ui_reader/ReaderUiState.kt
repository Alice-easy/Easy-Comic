package com.easycomic.ui_reader

import com.easycomic.domain.model.Manga

/**
 * 阅读器UI状态
 */
data class ReaderUiState(
    val manga: Manga? = null,
    val currentPage: Int = 0,
    val pageCount: Int = 0,
    val isLoading: Boolean = false,
    val isLoadingImage: Boolean = false,
    val error: String? = null,
    val settings: ReaderSettings = ReaderSettings(),
    val isBookmarked: Boolean = false,
    val readingProgress: Float = 0f
) {
    /**
     * 检查是否为第一页
     */
    val isFirstPage: Boolean
        get() = currentPage <= 0
    
    /**
     * 检查是否为最后一页
     */
    val isLastPage: Boolean
        get() = currentPage >= pageCount - 1
    
    /**
     * 计算阅读进度百分比
     */
    val progressPercentage: Int
        get() = if (pageCount > 0) {
            ((currentPage + 1) * 100 / pageCount).coerceIn(0, 100)
        } else 0
}
