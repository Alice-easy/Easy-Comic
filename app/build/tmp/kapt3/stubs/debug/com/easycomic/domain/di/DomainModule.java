package com.easycomic.domain.di;

import com.easycomic.domain.repository.BookmarkRepository;
import com.easycomic.domain.repository.MangaRepository;
import com.easycomic.domain.repository.ReadingHistoryRepository;
import com.easycomic.data.service.ComicImportService;
import com.easycomic.domain.usecase.manga.GetAllMangaUseCase;
import com.easycomic.domain.usecase.manga.GetMangaByIdUseCase;
import com.easycomic.domain.usecase.manga.SearchMangaUseCase;
import com.easycomic.domain.usecase.manga.GetFavoriteMangaUseCase;
import com.easycomic.domain.usecase.manga.GetRecentMangaUseCase;
import com.easycomic.domain.usecase.manga.GetMangaByStatusUseCase;
import com.easycomic.domain.usecase.manga.InsertOrUpdateMangaUseCase;
import com.easycomic.domain.usecase.manga.UpdateReadingProgressUseCase;
import com.easycomic.domain.usecase.manga.ToggleFavoriteUseCase;
import com.easycomic.domain.usecase.manga.UpdateRatingUseCase;
import com.easycomic.domain.usecase.manga.DeleteMangaUseCase;
import com.easycomic.domain.usecase.manga.DeleteAllMangaUseCase;
import com.easycomic.domain.usecase.manga.ImportComicUseCase;
import com.easycomic.domain.usecase.manga.BatchImportComicsUseCase;
import com.easycomic.domain.usecase.manga.MonitorImportProgressUseCase;
import com.easycomic.domain.usecase.manga.UpdateImportProgressUseCase;
import com.easycomic.domain.usecase.manga.ImportProgressHolder;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

/**
 * 领域层依赖注入模块 - 使用 Hilt
 */
@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0080\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0007J\u0010\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0007J\u0010\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000b\u001a\u00020\fH\u0007J\u0010\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u000b\u001a\u00020\fH\u0007J\u0010\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u000b\u001a\u00020\fH\u0007J\u0010\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u000b\u001a\u00020\fH\u0007J\u0010\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u000b\u001a\u00020\fH\u0007J\u0010\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u000b\u001a\u00020\fH\u0007J\u0010\u0010\u0019\u001a\u00020\u00062\u0006\u0010\u001a\u001a\u00020\u001bH\u0007J\u0010\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u000b\u001a\u00020\fH\u0007J\u0010\u0010\u001e\u001a\u00020\u001f2\u0006\u0010\u0007\u001a\u00020\bH\u0007J\u0010\u0010 \u001a\u00020!2\u0006\u0010\u000b\u001a\u00020\fH\u0007J\u0010\u0010\"\u001a\u00020#2\u0006\u0010\u000b\u001a\u00020\fH\u0007J\u0010\u0010$\u001a\u00020%2\u0006\u0010\u0007\u001a\u00020\bH\u0007J\u0010\u0010&\u001a\u00020\'2\u0006\u0010\u000b\u001a\u00020\fH\u0007J\u0010\u0010(\u001a\u00020)2\u0006\u0010\u000b\u001a\u00020\fH\u0007\u00a8\u0006*"}, d2 = {"Lcom/easycomic/domain/di/DomainModule;", "", "()V", "provideBatchImportComicsUseCase", "Lcom/easycomic/domain/usecase/manga/BatchImportComicsUseCase;", "importComicUseCase", "Lcom/easycomic/domain/usecase/manga/ImportComicUseCase;", "importProgressHolder", "Lcom/easycomic/domain/usecase/manga/ImportProgressHolder;", "provideDeleteAllMangaUseCase", "Lcom/easycomic/domain/usecase/manga/DeleteAllMangaUseCase;", "mangaRepository", "Lcom/easycomic/domain/repository/MangaRepository;", "provideDeleteMangaUseCase", "Lcom/easycomic/domain/usecase/manga/DeleteMangaUseCase;", "provideGetAllMangaUseCase", "Lcom/easycomic/domain/usecase/manga/GetAllMangaUseCase;", "provideGetFavoriteMangaUseCase", "Lcom/easycomic/domain/usecase/manga/GetFavoriteMangaUseCase;", "provideGetMangaByIdUseCase", "Lcom/easycomic/domain/usecase/manga/GetMangaByIdUseCase;", "provideGetMangaByStatusUseCase", "Lcom/easycomic/domain/usecase/manga/GetMangaByStatusUseCase;", "provideGetRecentMangaUseCase", "Lcom/easycomic/domain/usecase/manga/GetRecentMangaUseCase;", "provideImportComicUseCase", "comicImportService", "Lcom/easycomic/data/service/ComicImportService;", "provideInsertOrUpdateMangaUseCase", "Lcom/easycomic/domain/usecase/manga/InsertOrUpdateMangaUseCase;", "provideMonitorImportProgressUseCase", "Lcom/easycomic/domain/usecase/manga/MonitorImportProgressUseCase;", "provideSearchMangaUseCase", "Lcom/easycomic/domain/usecase/manga/SearchMangaUseCase;", "provideToggleFavoriteUseCase", "Lcom/easycomic/domain/usecase/manga/ToggleFavoriteUseCase;", "provideUpdateImportProgressUseCase", "Lcom/easycomic/domain/usecase/manga/UpdateImportProgressUseCase;", "provideUpdateRatingUseCase", "Lcom/easycomic/domain/usecase/manga/UpdateRatingUseCase;", "provideUpdateReadingProgressUseCase", "Lcom/easycomic/domain/usecase/manga/UpdateReadingProgressUseCase;", "app_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class DomainModule {
    @org.jetbrains.annotations.NotNull()
    public static final com.easycomic.domain.di.DomainModule INSTANCE = null;
    
    private DomainModule() {
        super();
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.domain.usecase.manga.GetAllMangaUseCase provideGetAllMangaUseCase(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.repository.MangaRepository mangaRepository) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.domain.usecase.manga.GetMangaByIdUseCase provideGetMangaByIdUseCase(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.repository.MangaRepository mangaRepository) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.domain.usecase.manga.SearchMangaUseCase provideSearchMangaUseCase(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.repository.MangaRepository mangaRepository) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.domain.usecase.manga.GetFavoriteMangaUseCase provideGetFavoriteMangaUseCase(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.repository.MangaRepository mangaRepository) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.domain.usecase.manga.GetRecentMangaUseCase provideGetRecentMangaUseCase(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.repository.MangaRepository mangaRepository) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.domain.usecase.manga.GetMangaByStatusUseCase provideGetMangaByStatusUseCase(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.repository.MangaRepository mangaRepository) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.domain.usecase.manga.InsertOrUpdateMangaUseCase provideInsertOrUpdateMangaUseCase(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.repository.MangaRepository mangaRepository) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.domain.usecase.manga.UpdateReadingProgressUseCase provideUpdateReadingProgressUseCase(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.repository.MangaRepository mangaRepository) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.domain.usecase.manga.ToggleFavoriteUseCase provideToggleFavoriteUseCase(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.repository.MangaRepository mangaRepository) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.domain.usecase.manga.UpdateRatingUseCase provideUpdateRatingUseCase(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.repository.MangaRepository mangaRepository) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.domain.usecase.manga.DeleteMangaUseCase provideDeleteMangaUseCase(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.repository.MangaRepository mangaRepository) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.domain.usecase.manga.DeleteAllMangaUseCase provideDeleteAllMangaUseCase(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.repository.MangaRepository mangaRepository) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.domain.usecase.manga.ImportComicUseCase provideImportComicUseCase(@org.jetbrains.annotations.NotNull()
    com.easycomic.data.service.ComicImportService comicImportService) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.domain.usecase.manga.BatchImportComicsUseCase provideBatchImportComicsUseCase(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.usecase.manga.ImportComicUseCase importComicUseCase, @org.jetbrains.annotations.NotNull()
    com.easycomic.domain.usecase.manga.ImportProgressHolder importProgressHolder) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.domain.usecase.manga.MonitorImportProgressUseCase provideMonitorImportProgressUseCase(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.usecase.manga.ImportProgressHolder importProgressHolder) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.domain.usecase.manga.UpdateImportProgressUseCase provideUpdateImportProgressUseCase(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.usecase.manga.ImportProgressHolder importProgressHolder) {
        return null;
    }
}