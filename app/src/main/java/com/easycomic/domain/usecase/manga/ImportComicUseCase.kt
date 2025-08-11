package com.easycomic.domain.usecase.manga

import android.net.Uri
import com.easycomic.data.service.ComicImportService
import com.easycomic.data.service.ImportResult
import com.easycomic.data.service.ImportStatus
import com.easycomic.data.service.BatchImportResult
import com.easycomic.data.service.BatchImportStatus
import com.easycomic.domain.model.ImportComicResult
import com.easycomic.domain.model.ImportProgress
import com.easycomic.domain.model.ImportStatus as DomainImportStatus
import com.easycomic.domain.usecase.BaseUseCase
import com.easycomic.domain.usecase.NoParametersUseCase
import kotlinx.coroutines.flow.*
import timber.log.Timber

/**
 * 导入单个漫画文件用例
 */
class ImportComicUseCase(
    private val comicImportService: ComicImportService
) : BaseUseCase<Uri, Flow<ImportComicResult>> {
    
    override suspend fun invoke(parameters: Uri): Flow<ImportComicResult> {
        return comicImportService.importComic(parameters)
            .map { result ->
                ImportComicResult(
                    success = result.status == ImportStatus.COMPLETED,
                    mangaId = result.mangaId,
                    manga = result.manga,
                    error = result.error,
                    progress = result.progress
                )
            }
            .catch { e ->
                Timber.e(e, "导入漫画失败")
                emit(ImportComicResult(
                    success = false,
                    error = "导入失败: ${e.message}"
                ))
            }
    }
}

/**
 * 批量导入漫画文件用例
 */
class BatchImportComicsUseCase @Inject constructor(
    private val comicImportService: ComicImportService
) : BaseUseCase<List<Uri>, Flow<com.easycomic.domain.model.BatchImportComicResult>> {
    
    override suspend fun invoke(parameters: List<Uri>): Flow<com.easycomic.domain.model.BatchImportComicResult> {
        return comicImportService.importComics(parameters)
            .map { result ->
                com.easycomic.domain.model.BatchImportComicResult(
                    success = result.status == BatchImportStatus.COMPLETED,
                    totalItems = result.total,
                    importedCount = result.results.count { it.status == ImportStatus.COMPLETED },
                    failedCount = result.results.count { it.status == ImportStatus.FAILED },
                    results = result.results.map { importResult ->
                        ImportComicResult(
                            success = importResult.status == ImportStatus.COMPLETED,
                            mangaId = importResult.mangaId,
                            manga = importResult.manga,
                            error = importResult.error,
                            progress = importResult.progress
                        )
                    },
                    errors = result.results.filter { it.status == ImportStatus.FAILED }
                        .mapNotNull { it.error }
                )
            }
            .catch { e ->
                Timber.e(e, "批量导入漫画失败")
                emit(com.easycomic.domain.model.BatchImportComicResult(
                    success = false,
                    errors = listOf("批量导入失败: ${e.message}")
                ))
            }
    }
}

/**
 * 监听导入进度用例
 */
class MonitorImportProgressUseCase @Inject constructor(
    private val importProgressHolder: ImportProgressHolder
) : NoParametersUseCase<Flow<ImportProgress>> {
    
    override suspend fun invoke(): Flow<ImportProgress> {
        return importProgressHolder.progressFlow
    }
}

/**
 * 更新导入进度用例
 */
class UpdateImportProgressUseCase @Inject constructor(
    private val importProgressHolder: ImportProgressHolder
) : BaseUseCase<ImportProgress, Unit> {
    
    override suspend fun invoke(parameters: ImportProgress) {
        importProgressHolder.updateProgress(parameters)
    }
}

/**
 * 导入进度持有者
 * 用于在应用范围内共享导入进度状态
 */
@javax.inject.Singleton
class ImportProgressHolder @Inject constructor() {
    
    private val _progressFlow = MutableStateFlow(ImportProgress())
    val progressFlow: StateFlow<ImportProgress> = _progressFlow.asStateFlow()
    
    fun updateProgress(progress: ImportProgress) {
        _progressFlow.value = progress
    }
    
    fun resetProgress() {
        _progressFlow.value = ImportProgress()
    }
}

/**
 * 导入漫画结果扩展函数
 */
private fun ImportStatus.toDomainImportStatus(): DomainImportStatus {
    return when (this) {
        ImportStatus.PROCESSING -> DomainImportStatus.VALIDATING
        ImportStatus.PARSING -> DomainImportStatus.PARSING
        ImportStatus.EXTRACTING_COVER -> DomainImportStatus.EXTRACTING
        ImportStatus.SAVING_TO_DATABASE -> DomainImportStatus.SAVING
        ImportStatus.COMPLETED -> DomainImportStatus.COMPLETED
        ImportStatus.FAILED -> DomainImportStatus.FAILED
    }
}