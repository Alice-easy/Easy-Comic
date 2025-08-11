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
import com.easycomic.domain.model.Manga
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * ImportComicUseCase 测试类
 */
@RunWith(MockitoJUnitRunner::class)
class ImportComicUseCaseTest {

    @Mock
    private lateinit var comicImportService: ComicImportService

    private lateinit var importComicUseCase: ImportComicUseCase

    @Before
    fun setup() {
        importComicUseCase = ImportComicUseCase(comicImportService)
    }

    @Test
    fun `when importing valid comic file, should return success result`() = runTest {
        // Given
        val validUri = Uri.parse("content://com.example.provider/comic.cbz")
        val mockImportResult = ImportResult(
            status = ImportStatus.COMPLETED,
            mangaId = 1L,
            manga = createMockManga(),
            progress = 100
        )
        
        whenever(comicImportService.importComic(any())).thenReturn(flow { emit(mockImportResult) })

        // When
        val result = importComicUseCase(validUri).first()

        // Then
        assertTrue(result.success)
        assertEquals(1L, result.mangaId)
        assertNotNull(result.manga)
        assertNull(result.error)
        assertEquals(100, result.progress)
        verify(comicImportService).importComic(validUri)
    }

    @Test
    fun `when importing invalid file, should return error result`() = runTest {
        // Given
        val invalidUri = Uri.parse("content://com.example.provider/invalid.txt")
        val mockImportResult = ImportResult(
            status = ImportStatus.FAILED,
            error = "文件格式不支持"
        )
        
        whenever(comicImportService.importComic(any())).thenReturn(flow { emit(mockImportResult) })

        // When
        val result = importComicUseCase(invalidUri).first()

        // Then
        assertFalse(result.success)
        assertNull(result.mangaId)
        assertNull(result.manga)
        assertEquals("文件格式不支持", result.error)
        verify(comicImportService).importComic(invalidUri)
    }

    @Test
    fun `when import service throws exception, should handle error gracefully`() = runTest {
        // Given
        val uri = Uri.parse("content://com.example.provider/error.cbz")
        val exception = RuntimeException("网络错误")
        
        whenever(comicImportService.importComic(any())).thenReturn(flow { throw exception })

        // When
        val result = importComicUseCase(uri).first()

        // Then
        assertFalse(result.success)
        assertNull(result.mangaId)
        assertNull(result.manga)
        assertEquals("导入失败: 网络错误", result.error)
    }

    @Test
    fun `when import is in progress, should return correct progress`() = runTest {
        // Given
        val uri = Uri.parse("content://com.example/provider/progress.cbz")
        val processingResult = ImportResult(
            status = ImportStatus.PARSING,
            progress = 50
        )
        
        whenever(comicImportService.importComic(any())).thenReturn(flow { emit(processingResult) })

        // When
        val result = importComicUseCase(uri).first()

        // Then
        assertFalse(result.success)
        assertEquals(50, result.progress)
        verify(comicImportService).importComic(uri)
    }

    private fun createMockManga(): Manga {
        return Manga(
            id = 1L,
            title = "测试漫画",
            author = "测试作者",
            filePath = "/path/to/comic.cbz",
            pageCount = 100,
            currentPage = 0,
            fileFormat = "CBZ",
            fileSize = 1024000L
        )
    }
}

/**
 * BatchImportComicsUseCase 测试类
 */
@RunWith(MockitoJUnitRunner::class)
class BatchImportComicsUseCaseTest {

    @Mock
    private lateinit var comicImportService: ComicImportService

    private lateinit var batchImportComicsUseCase: BatchImportComicsUseCase

    @Before
    fun setup() {
        batchImportComicsUseCase = BatchImportComicsUseCase(comicImportService)
    }

    @Test
    fun `when batch importing multiple valid files, should process all files successfully`() = runTest {
        // Given
        val fileUris = listOf(
            Uri.parse("content://com.example.provider/comic1.cbz"),
            Uri.parse("content://com.example.provider/comic2.cbz"),
            Uri.parse("content://com.example.provider/comic3.cbz")
        )
        
        val mockBatchResult = BatchImportResult(
            status = BatchImportStatus.COMPLETED,
            total = 3,
            results = listOf(
                ImportResult(status = ImportStatus.COMPLETED, mangaId = 1L),
                ImportResult(status = ImportStatus.COMPLETED, mangaId = 2L),
                ImportResult(status = ImportStatus.COMPLETED, mangaId = 3L)
            )
        )
        
        whenever(comicImportService.importComics(any())).thenReturn(flow { emit(mockBatchResult) })

        // When
        val result = batchImportComicsUseCase(fileUris).first()

        // Then
        assertTrue(result.success)
        assertEquals(3, result.totalItems)
        assertEquals(3, result.importedCount)
        assertEquals(0, result.failedCount)
        assertEquals(3, result.results.size)
        assertTrue(result.errors.isEmpty())
        verify(comicImportService).importComics(fileUris)
    }

    @Test
    fun `when batch importing with mixed files, should handle failures gracefully`() = runTest {
        // Given
        val fileUris = listOf(
            Uri.parse("content://com.example.provider/valid.cbz"),
            Uri.parse("content://com.example.provider/invalid.txt"),
            Uri.parse("content://com.example.provider/valid2.cbz")
        )
        
        val mockBatchResult = BatchImportResult(
            status = BatchImportStatus.COMPLETED,
            total = 3,
            results = listOf(
                ImportResult(status = ImportStatus.COMPLETED, mangaId = 1L),
                ImportResult(status = ImportStatus.FAILED, error = "文件格式不支持"),
                ImportResult(status = ImportStatus.COMPLETED, mangaId = 2L)
            )
        )
        
        whenever(comicImportService.importComics(any())).thenReturn(flow { emit(mockBatchResult) })

        // When
        val result = batchImportComicsUseCase(fileUris).first()

        // Then
        assertFalse(result.success)
        assertEquals(3, result.totalItems)
        assertEquals(2, result.importedCount)
        assertEquals(1, result.failedCount)
        assertEquals(3, result.results.size)
        assertEquals(1, result.errors.size)
        assertEquals("文件格式不支持", result.errors[0])
    }

    @Test
    fun `when batch importing empty list, should return empty result`() = runTest {
        // Given
        val emptyList = listOf<Uri>()
        val mockBatchResult = BatchImportResult(
            status = BatchImportStatus.COMPLETED,
            total = 0,
            results = emptyList()
        )
        
        whenever(comicImportService.importComics(any())).thenReturn(flow { emit(mockBatchResult) })

        // When
        val result = batchImportComicsUseCase(emptyList).first()

        // Then
        assertTrue(result.success)
        assertEquals(0, result.totalItems)
        assertEquals(0, result.importedCount)
        assertEquals(0, result.failedCount)
        assertTrue(result.results.isEmpty())
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `when batch import service throws exception, should handle error gracefully`() = runTest {
        // Given
        val fileUris = listOf(
            Uri.parse("content://com.example.provider/error.cbz")
        )
        val exception = RuntimeException("批量导入失败")
        
        whenever(comicImportService.importComics(any())).thenReturn(flow { throw exception })

        // When
        val result = batchImportComicsUseCase(fileUris).first()

        // Then
        assertFalse(result.success)
        assertEquals(0, result.totalItems)
        assertEquals(0, result.importedCount)
        assertEquals(0, result.failedCount)
        assertTrue(result.results.isEmpty())
        assertEquals(1, result.errors.size)
        assertEquals("批量导入失败: 批量导入失败", result.errors[0])
    }
}

/**
 * MonitorImportProgressUseCase 测试类
 */
@RunWith(MockitoJUnitRunner::class)
class MonitorImportProgressUseCaseTest {

    @Mock
    private lateinit var importProgressHolder: ImportProgressHolder

    private lateinit var monitorImportProgressUseCase: MonitorImportProgressUseCase

    @Before
    fun setup() {
        monitorImportProgressUseCase = MonitorImportProgressUseCase(importProgressHolder)
    }

    @Test
    fun `when monitoring progress, should return progress flow from holder`() = runTest {
        // Given
        val expectedProgress = ImportProgress(
            status = DomainImportStatus.PARSING,
            progress = 50,
            message = "正在解析..."
        )
        
        whenever(importProgressHolder.progressFlow).thenReturn(kotlinx.coroutines.flow.MutableStateFlow(expectedProgress))

        // When
        val result = monitorImportProgressUseCase().first()

        // Then
        assertEquals(expectedProgress, result)
        verify(importProgressHolder).progressFlow
    }
}

/**
 * UpdateImportProgressUseCase 测试类
 */
@RunWith(MockitoJUnitRunner::class)
class UpdateImportProgressUseCaseTest {

    @Mock
    private lateinit var importProgressHolder: ImportProgressHolder

    private lateinit var updateImportProgressUseCase: UpdateImportProgressUseCase

    @Before
    fun setup() {
        updateImportProgressUseCase = UpdateImportProgressUseCase(importProgressHolder)
    }

    @Test
    fun `when updating progress, should call holder update method`() = runTest {
        // Given
        val progress = ImportProgress(
            status = DomainImportStatus.COMPLETED,
            progress = 100,
            message = "导入完成"
        )

        // When
        updateImportProgressUseCase(progress)

        // Then
        verify(importProgressHolder).updateProgress(progress)
    }
}

/**
 * ImportProgressHolder 测试类
 */
@RunWith(MockitoJUnitRunner::class)
class ImportProgressHolderTest {

    private lateinit var importProgressHolder: ImportProgressHolder

    @Before
    fun setup() {
        importProgressHolder = ImportProgressHolder()
    }

    @Test
    fun `when initialized, should have default progress values`() = runTest {
        // When
        val progress = importProgressHolder.progressFlow.value

        // Then
        assertEquals(DomainImportStatus.IDLE, progress.status)
        assertEquals(0, progress.progress)
        assertNull(progress.currentFile)
        assertEquals(0, progress.totalFiles)
        assertEquals(0, progress.processedFiles)
        assertNull(progress.message)
    }

    @Test
    fun `when updating progress, should emit correct state through flow`() = runTest {
        // Given
        val progress = ImportProgress(
            status = DomainImportStatus.PARSING,
            progress = 50,
            currentFile = "test.cbz",
            totalFiles = 10,
            processedFiles = 5,
            message = "正在处理..."
        )

        // When
        importProgressHolder.updateProgress(progress)

        // Then
        val currentState = importProgressHolder.progressFlow.value
        assertEquals(DomainImportStatus.PARSING, currentState.status)
        assertEquals(50, currentState.progress)
        assertEquals("test.cbz", currentState.currentFile)
        assertEquals(10, currentState.totalFiles)
        assertEquals(5, currentState.processedFiles)
        assertEquals("正在处理...", currentState.message)
    }

    @Test
    fun `when monitoring progress, should receive updates through flow`() = runTest {
        // Given
        val progressUpdates = mutableListOf<ImportProgress>()
        val job = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined).launch {
            importProgressHolder.progressFlow.collect { progress ->
                progressUpdates.add(progress)
            }
        }

        // When
        val progress1 = ImportProgress(status = DomainImportStatus.VALIDATING, progress = 10)
        val progress2 = ImportProgress(status = DomainImportStatus.PARSING, progress = 50)
        
        importProgressHolder.updateProgress(progress1)
        importProgressHolder.updateProgress(progress2)

        // Then
        assertEquals(3, progressUpdates.size) // Initial + 2 updates
        assertEquals(DomainImportStatus.IDLE, progressUpdates[0].status)
        assertEquals(DomainImportStatus.VALIDATING, progressUpdates[1].status)
        assertEquals(DomainImportStatus.PARSING, progressUpdates[2].status)
        
        job.cancel()
    }

    @Test
    fun `when resetting progress, should return to initial state`() = runTest {
        // Given
        val progress = ImportProgress(
            status = DomainImportStatus.COMPLETED,
            progress = 100,
            message = "导入完成"
        )
        importProgressHolder.updateProgress(progress)

        // When
        importProgressHolder.resetProgress()

        // Then
        val currentState = importProgressHolder.progressFlow.value
        assertEquals(DomainImportStatus.IDLE, currentState.status)
        assertEquals(0, currentState.progress)
        assertNull(progress.currentFile)
        assertEquals(0, currentState.totalFiles)
        assertEquals(0, currentState.processedFiles)
        assertNull(currentState.message)
    }
}