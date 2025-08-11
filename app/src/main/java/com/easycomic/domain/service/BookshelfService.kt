package com.easycomic.domain.service

import android.net.Uri
import com.easycomic.domain.model.Manga
import com.easycomic.domain.model.ImportComicResult
import com.easycomic.domain.model.BatchImportComicResult
import com.easycomic.domain.model.ImportProgress
import com.easycomic.domain.usecase.manga.*
import kotlinx.coroutines.flow.Flow

/**
 * 书架服务 - 封装书架相关的用例操作
 * 提供统一的接口来管理漫画数据
 */
class BookshelfService(
    private val getAllMangaUseCase: GetAllMangaUseCase,
    private val searchMangaUseCase: SearchMangaUseCase,
    private val getFavoriteMangaUseCase: GetFavoriteMangaUseCase,
    private val getRecentMangaUseCase: GetRecentMangaUseCase,
    private val deleteMangaUseCase: DeleteMangaUseCase,
    private val deleteAllMangaUseCase: DeleteAllMangaUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val importComicUseCase: ImportComicUseCase,
    private val batchImportComicsUseCase: BatchImportComicsUseCase,
    private val monitorImportProgressUseCase: MonitorImportProgressUseCase
) {
    
    /**
     * 获取所有漫画
     */
    fun getAllManga(): Flow<List<Manga>> = getAllMangaUseCase()
    
    /**
     * 搜索漫画
     */
    fun searchManga(query: String): Flow<List<Manga>> = searchMangaUseCase(query)
    
    /**
     * 获取收藏的漫画
     */
    fun getFavoriteManga(): Flow<List<Manga>> = getFavoriteMangaUseCase()
    
    /**
     * 获取最近阅读的漫画
     */
    fun getRecentManga(): Flow<List<Manga>> = getRecentMangaUseCase()
    
    /**
     * 删除漫画
     */
    suspend fun deleteManga(manga: Manga) = deleteMangaUseCase(manga)
    
    /**
     * 删除多个漫画
     */
    suspend fun deleteAllManga(mangaList: List<Manga>) {
        mangaList.forEach { manga ->
            deleteMangaUseCase(manga)
        }
    }
    
    /**
     * 删除所有漫画
     */
    suspend fun deleteAllManga() = deleteAllMangaUseCase()
    
    /**
     * 切换收藏状态
     */
    suspend fun toggleFavorite(mangaId: Long) = toggleFavoriteUseCase(mangaId)
    
    /**
     * 导入单个漫画
     */
    fun importComic(uri: Uri): Flow<ImportComicResult> = importComicUseCase(uri)
    
    /**
     * 批量导入漫画
     */
    fun importComics(uris: List<Uri>): Flow<BatchImportComicResult> = batchImportComicsUseCase(uris)
    
    /**
     * 监控导入进度
     */
    fun monitorImportProgress(): Flow<ImportProgress> = monitorImportProgressUseCase()
}