package com.easycomic.di

import android.content.Context
import androidx.room.Room
import com.easycomic.data.dao.BookmarkDao
import com.easycomic.data.dao.MangaDao
import com.easycomic.data.dao.ReadingHistoryDao
import com.easycomic.data.database.AppDatabase
import com.easycomic.data.repository.BookmarkRepositoryImpl
import com.easycomic.data.repository.MangaRepositoryImpl
import com.easycomic.data.repository.ReadingHistoryRepositoryImpl
import com.easycomic.data.service.ComicImportService
import com.easycomic.data.util.FileHelper
import com.easycomic.domain.repository.BookmarkRepository
import com.easycomic.domain.repository.MangaRepository
import com.easycomic.domain.repository.ReadingHistoryRepository
import com.easycomic.domain.usecase.manga.*

/**
 * 手动依赖注入容器 - 单例模式管理所有依赖
 */
object DependencyContainer {
    
    private lateinit var appContext: Context
    
    // Database
    private lateinit var database: AppDatabase
    
    // DAOs
    private lateinit var mangaDao: MangaDao
    private lateinit var bookmarkDao: BookmarkDao
    private lateinit var readingHistoryDao: ReadingHistoryDao
    
    // Repositories
    private lateinit var mangaRepository: MangaRepository
    private lateinit var bookmarkRepository: BookmarkRepository
    private lateinit var readingHistoryRepository: ReadingHistoryRepository
    
    // Services
    private lateinit var comicImportService: ComicImportService
    
    // Use Cases
    private lateinit var getAllMangaUseCase: GetAllMangaUseCase
    private lateinit var getMangaByIdUseCase: GetMangaByIdUseCase
    private lateinit var searchMangaUseCase: SearchMangaUseCase
    private lateinit var getFavoriteMangaUseCase: GetFavoriteMangaUseCase
    private lateinit var getRecentMangaUseCase: GetRecentMangaUseCase
    private lateinit var getMangaByStatusUseCase: GetMangaByStatusUseCase
    private lateinit var insertOrUpdateMangaUseCase: InsertOrUpdateMangaUseCase
    private lateinit var updateReadingProgressUseCase: UpdateReadingProgressUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase
    private lateinit var updateRatingUseCase: UpdateRatingUseCase
    private lateinit var deleteMangaUseCase: DeleteMangaUseCase
    private lateinit var deleteAllMangaUseCase: DeleteAllMangaUseCase
    private lateinit var importComicUseCase: ImportComicUseCase
    private lateinit var batchImportComicsUseCase: BatchImportComicsUseCase
    private lateinit var monitorImportProgressUseCase: MonitorImportProgressUseCase
    private lateinit var updateImportProgressUseCase: UpdateImportProgressUseCase
    
    // Shared state
    private val importProgressHolder = ImportProgressHolder()
    
    /**
     * 初始化依赖容器
     */
    fun initialize(context: Context) {
        appContext = context.applicationContext
        initializeDatabase()
        initializeRepositories()
        initializeServices()
        initializeUseCases()
    }
    
    /**
     * 初始化数据库
     */
    private fun initializeDatabase() {
        database = Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "easy_comic_database"
        ).build()
        
        mangaDao = database.mangaDao()
        bookmarkDao = database.bookmarkDao()
        readingHistoryDao = database.readingHistoryDao()
    }
    
    /**
     * 初始化仓储层
     */
    private fun initializeRepositories() {
        mangaRepository = MangaRepositoryImpl(mangaDao)
        bookmarkRepository = BookmarkRepositoryImpl(bookmarkDao)
        readingHistoryRepository = ReadingHistoryRepositoryImpl(readingHistoryDao)
    }
    
    /**
     * 初始化服务层
     */
    private fun initializeServices() {
        comicImportService = ComicImportService(appContext, mangaRepository as MangaRepositoryImpl, FileHelper)
    }
    
    /**
     * 初始化用例层
     */
    private fun initializeUseCases() {
        // 漫画用例
        getAllMangaUseCase = GetAllMangaUseCase(mangaRepository)
        getMangaByIdUseCase = GetMangaByIdUseCase(mangaRepository)
        searchMangaUseCase = SearchMangaUseCase(mangaRepository)
        getFavoriteMangaUseCase = GetFavoriteMangaUseCase(mangaRepository)
        getRecentMangaUseCase = GetRecentMangaUseCase(mangaRepository)
        getMangaByStatusUseCase = GetMangaByStatusUseCase(mangaRepository)
        insertOrUpdateMangaUseCase = InsertOrUpdateMangaUseCase(mangaRepository)
        updateReadingProgressUseCase = UpdateReadingProgressUseCase(mangaRepository)
        toggleFavoriteUseCase = ToggleFavoriteUseCase(mangaRepository)
        updateRatingUseCase = UpdateRatingUseCase(mangaRepository)
        deleteMangaUseCase = DeleteMangaUseCase(mangaRepository)
        deleteAllMangaUseCase = DeleteAllMangaUseCase(mangaRepository)
        
        // 导入相关用例
        importComicUseCase = ImportComicUseCase(comicImportService)
        batchImportComicsUseCase = BatchImportComicsUseCase(importComicUseCase, importProgressHolder)
        monitorImportProgressUseCase = MonitorImportProgressUseCase(importProgressHolder)
        updateImportProgressUseCase = UpdateImportProgressUseCase(importProgressHolder)
    }
    
    // Database accessors
    fun getDatabase(): AppDatabase = database
    
    // Repository accessors
    fun getMangaRepository(): MangaRepository = mangaRepository
    fun getBookmarkRepository(): BookmarkRepository = bookmarkRepository
    fun getReadingHistoryRepository(): ReadingHistoryRepository = readingHistoryRepository
    
    // Service accessors
    fun getComicImportService(): ComicImportService = comicImportService
    
    // Use case accessors
    fun getAllMangaUseCase(): GetAllMangaUseCase = getAllMangaUseCase
    fun getGetMangaByIdUseCase(): GetMangaByIdUseCase = getMangaByIdUseCase
    fun getSearchMangaUseCase(): SearchMangaUseCase = searchMangaUseCase
    fun getGetFavoriteMangaUseCase(): GetFavoriteMangaUseCase = getFavoriteMangaUseCase
    fun getGetRecentMangaUseCase(): GetRecentMangaUseCase = getRecentMangaUseCase
    fun getGetMangaByStatusUseCase(): GetMangaByStatusUseCase = getMangaByStatusUseCase
    fun getInsertOrUpdateMangaUseCase(): InsertOrUpdateMangaUseCase = insertOrUpdateMangaUseCase
    fun getUpdateReadingProgressUseCase(): UpdateReadingProgressUseCase = updateReadingProgressUseCase
    fun getToggleFavoriteUseCase(): ToggleFavoriteUseCase = toggleFavoriteUseCase
    fun getUpdateRatingUseCase(): UpdateRatingUseCase = updateRatingUseCase
    fun getDeleteMangaUseCase(): DeleteMangaUseCase = deleteMangaUseCase
    fun getDeleteAllMangaUseCase(): DeleteAllMangaUseCase = deleteAllMangaUseCase
    fun getImportComicUseCase(): ImportComicUseCase = importComicUseCase
    fun getBatchImportComicsUseCase(): BatchImportComicsUseCase = batchImportComicsUseCase
    fun getMonitorImportProgressUseCase(): MonitorImportProgressUseCase = monitorImportProgressUseCase
    fun getUpdateImportProgressUseCase(): UpdateImportProgressUseCase = updateImportProgressUseCase
}