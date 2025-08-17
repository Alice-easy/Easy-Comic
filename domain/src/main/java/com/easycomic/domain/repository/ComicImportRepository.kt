package com.easycomic.domain.repository

import java.io.File

/**
 * 漫画导入仓库接口
 */
interface ComicImportRepository {
    /**
     * 从指定目录导入漫画
     */
    suspend fun importComics(directory: File)
}