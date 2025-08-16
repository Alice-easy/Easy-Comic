package com.easycomic.domain.usecase.manga

import com.easycomic.data.entity.ReadingStatus
import com.easycomic.domain.model.Manga
import com.easycomic.domain.repository.MangaRepository
import com.easycomic.domain.usecase.BaseUseCase
import com.easycomic.domain.usecase.NoParametersUseCase
import com.easycomic.utils.ComicParser
import com.easycomic.utils.RarComicParser
import com.easycomic.utils.ZipComicParser
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * 获取所有漫画用例
 */
class GetAllMangaUseCase(
    private val mangaRepository: MangaRepository
) : NoParametersUseCase<Flow<List<Manga>>> {
    override suspend fun invoke(): Flow<List<Manga>> {
        return mangaRepository.getAllManga()
    }
}

/**
 * 获取漫画详情用例
 */
class GetMangaByIdUseCase(
    private val mangaRepository: MangaRepository
) : BaseUseCase<Long, Manga?> {
    override suspend fun invoke(parameters: Long): Manga? {
        return mangaRepository.getMangaById(parameters)
    }
}

/**
 * 搜索漫画用例
 */
class SearchMangaUseCase(
    private val mangaRepository: MangaRepository
) : BaseUseCase<String, Flow<List<Manga>>> {
    override suspend fun invoke(parameters: String): Flow<List<Manga>> {
        return mangaRepository.searchManga(parameters)
    }
}

/**
 * 获取收藏漫画用例
 */
class GetFavoriteMangaUseCase(
    private val mangaRepository: MangaRepository
) : NoParametersUseCase<Flow<List<Manga>>> {
    override suspend fun invoke(): Flow<List<Manga>> {
        return mangaRepository.getFavoriteManga()
    }
}

/**
 * 获取最近阅读漫画用例
 */
class GetRecentMangaUseCase(
    private val mangaRepository: MangaRepository
) : BaseUseCase<Int, Flow<List<Manga>>> {
    override suspend fun invoke(parameters: Int): Flow<List<Manga>> {
        return mangaRepository.getRecentManga(parameters)
    }
}

/**
 * 根据状态获取漫画用例
 */
class GetMangaByStatusUseCase(
    private val mangaRepository: MangaRepository
) : BaseUseCase<com.easycomic.data.entity.ReadingStatus, Flow<List<Manga>>> {
    override suspend fun invoke(parameters: com.easycomic.data.entity.ReadingStatus): Flow<List<Manga>> {
        return mangaRepository.getMangaByStatus(parameters)
    }
}

/**
 * 添加或更新漫画用例
 */
class InsertOrUpdateMangaUseCase(
    private val mangaRepository: MangaRepository
) : BaseUseCase<Manga, Long> {
    override suspend fun invoke(parameters: Manga): Long {
        return mangaRepository.insertOrUpdateManga(parameters)
    }
}

/**
 * 更新阅读进度用例
 */
class UpdateReadingProgressUseCase(
    private val mangaRepository: MangaRepository
) : BaseUseCase<UpdateReadingProgressUseCase.Params, Unit> {
    
    data class Params(
        val mangaId: Long,
        val currentPage: Int,
        val status: com.easycomic.data.entity.ReadingStatus
    )
    
    override suspend fun invoke(parameters: Params) {
        mangaRepository.updateReadingProgress(
            parameters.mangaId,
            parameters.currentPage,
            parameters.status
        )
    }
}

/**
 * 切换收藏状态用例
 */
class ToggleFavoriteUseCase(
    private val mangaRepository: MangaRepository
) : BaseUseCase<Long, Unit> {
    override suspend fun invoke(parameters: Long) {
        mangaRepository.toggleFavorite(parameters)
    }
}

/**
 * 更新评分用例
 */
class UpdateRatingUseCase(
    private val mangaRepository: MangaRepository
) : BaseUseCase<UpdateRatingUseCase.Params, Unit> {
    
    data class Params(
        val mangaId: Long,
        val rating: Float
    )
    
    override suspend fun invoke(parameters: Params) {
        mangaRepository.updateRating(parameters.mangaId, parameters.rating)
    }
}

/**
 * 删除漫画用例
 */
class DeleteMangaUseCase(
    private val mangaRepository: MangaRepository
) : BaseUseCase<Manga, Unit> {
    override suspend fun invoke(parameters: Manga) {
        mangaRepository.deleteManga(parameters)
    }
}

/**
 * 批量删除漫画用例
 */
class DeleteAllMangaUseCase(
    private val mangaRepository: MangaRepository
) : BaseUseCase<List<Manga>, Unit> {
    override suspend fun invoke(parameters: List<Manga>) {
        mangaRepository.deleteAllManga(parameters)
    }
}

/**
 * 导入漫画用例
 */
class ImportComicsUseCase(
    private val mangaRepository: MangaRepository
) : BaseUseCase<File, Unit> {
    override suspend fun invoke(parameters: File) {
        if (!parameters.isDirectory) return

        parameters.listFiles()?.forEach { file ->
            val parser = getParserForFile(file)
            if (parser != null) {
                try {
                    val pageCount = parser.getPageCount()
                    if (pageCount > 0) {
                        val manga = Manga(
                            title = file.nameWithoutExtension,
                            filePath = file.absolutePath,
                            fileSize = file.length(),
                            fileFormat = file.extension.uppercase(),
                            pageCount = pageCount,
                            dateAdded = System.currentTimeMillis(),
                            dateModified = System.currentTimeMillis(),
                            lastRead = System.currentTimeMillis(),
                            readingStatus = ReadingStatus.UNREAD
                        )
                        mangaRepository.insertOrUpdateManga(manga)
                    }
                } finally {
                    parser.close()
                }
            }
        }
    }

    private fun getParserForFile(file: File): ComicParser? {
        val extension = file.extension.lowercase()
        return when (extension) {
            "zip", "cbz" -> ZipComicParser(file)
            "rar", "cbr" -> RarComicParser(file)
            else -> null
        }
    }
}

/**
 * 获取封面用例
 */
class GetCoverUseCase(
    private val mangaRepository: MangaRepository
) : BaseUseCase<Manga, android.graphics.Bitmap?> {
    override suspend fun invoke(parameters: Manga): android.graphics.Bitmap? {
        return mangaRepository.getCover(parameters)
    }
}
