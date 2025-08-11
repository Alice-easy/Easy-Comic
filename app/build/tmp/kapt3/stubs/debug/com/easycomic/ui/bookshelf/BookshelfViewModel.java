package com.easycomic.ui.bookshelf;

import androidx.lifecycle.ViewModel;
import android.net.Uri;
import com.easycomic.domain.model.Manga;
import com.easycomic.domain.model.ReadingStatus;
import com.easycomic.domain.model.ImportComicResult;
import com.easycomic.domain.model.BatchImportComicResult;
import com.easycomic.domain.model.ImportProgress;
import com.easycomic.domain.usecase.manga.*;
import com.easycomic.domain.usecase.NoParametersUseCase;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.*;
import timber.log.Timber;
import javax.inject.Inject;

/**
 * 书架视图模型
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u00a0\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\"\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0011\n\u0002\u0010\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\f\b\u0007\u0018\u00002\u00020\u0001BW\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\f\u001a\u00020\r\u0012\u0006\u0010\u000e\u001a\u00020\u000f\u0012\u0006\u0010\u0010\u001a\u00020\u0011\u0012\u0006\u0010\u0012\u001a\u00020\u0013\u0012\u0006\u0010\u0014\u001a\u00020\u0015\u00a2\u0006\u0002\u0010\u0016J \u0010;\u001a\u00020<2\u0006\u0010=\u001a\u00020\u001f2\u0006\u0010>\u001a\u00020&2\u0006\u0010?\u001a\u00020\u0019H\u0002J\u0006\u0010@\u001a\u00020<J\u0006\u0010A\u001a\u00020<J\u0006\u0010B\u001a\u00020<J\u0006\u0010C\u001a\u00020<J\u0006\u0010D\u001a\u00020<J\u000e\u0010E\u001a\u00020<2\u0006\u0010F\u001a\u00020GJ\u0014\u0010H\u001a\u00020<2\f\u0010I\u001a\b\u0012\u0004\u0012\u00020G0JJ\b\u0010K\u001a\u00020<H\u0002J\b\u0010L\u001a\u00020<H\u0002J\u0006\u0010M\u001a\u00020<J\u000e\u0010N\u001a\u00020<2\u0006\u0010=\u001a\u00020\u001fJ\u0006\u0010O\u001a\u00020<J\u000e\u0010P\u001a\u00020<2\u0006\u0010)\u001a\u00020\u0019J\u000e\u0010Q\u001a\u00020<2\u0006\u00107\u001a\u00020&J\u000e\u0010R\u001a\u00020<2\u0006\u0010S\u001a\u00020\"J\u000e\u0010T\u001a\u00020<2\u0006\u0010S\u001a\u00020\"J\u0006\u0010U\u001a\u00020<R\u0014\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00190\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u001b0\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u001d0\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u001f0\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010 \u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\"0!0\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010#\u001a\b\u0012\u0004\u0012\u00020$0\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010%\u001a\b\u0012\u0004\u0012\u00020&0\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\'\u001a\b\u0012\u0004\u0012\u00020(0\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010)\u001a\b\u0012\u0004\u0012\u00020\u00190*\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010,R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010-\u001a\b\u0012\u0004\u0012\u00020\u001b0*\u00a2\u0006\b\n\u0000\u001a\u0004\b.\u0010,R\u0017\u0010/\u001a\b\u0012\u0004\u0012\u00020\u001d0*\u00a2\u0006\b\n\u0000\u001a\u0004\b0\u0010,R\u000e\u0010\u0014\u001a\u00020\u0015X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u00101\u001a\b\u0012\u0004\u0012\u00020\u001f0*\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u0010,R\u001d\u00103\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\"0!0*\u00a2\u0006\b\n\u0000\u001a\u0004\b4\u0010,R\u0017\u00105\u001a\b\u0012\u0004\u0012\u00020$0*\u00a2\u0006\b\n\u0000\u001a\u0004\b6\u0010,R\u0017\u00107\u001a\b\u0012\u0004\u0012\u00020&0*\u00a2\u0006\b\n\u0000\u001a\u0004\b8\u0010,R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u00109\u001a\b\u0012\u0004\u0012\u00020(0*\u00a2\u0006\b\n\u0000\u001a\u0004\b:\u0010,\u00a8\u0006V"}, d2 = {"Lcom/easycomic/ui/bookshelf/BookshelfViewModel;", "Landroidx/lifecycle/ViewModel;", "getAllMangaUseCase", "Lcom/easycomic/domain/usecase/manga/GetAllMangaUseCase;", "searchMangaUseCase", "Lcom/easycomic/domain/usecase/manga/SearchMangaUseCase;", "getFavoriteMangaUseCase", "Lcom/easycomic/domain/usecase/manga/GetFavoriteMangaUseCase;", "getRecentMangaUseCase", "Lcom/easycomic/domain/usecase/manga/GetRecentMangaUseCase;", "deleteMangaUseCase", "Lcom/easycomic/domain/usecase/manga/DeleteMangaUseCase;", "deleteAllMangaUseCase", "Lcom/easycomic/domain/usecase/manga/DeleteAllMangaUseCase;", "toggleFavoriteUseCase", "Lcom/easycomic/domain/usecase/manga/ToggleFavoriteUseCase;", "importComicUseCase", "Lcom/easycomic/domain/usecase/manga/ImportComicUseCase;", "batchImportComicsUseCase", "Lcom/easycomic/domain/usecase/manga/BatchImportComicsUseCase;", "monitorImportProgressUseCase", "Lcom/easycomic/domain/usecase/manga/MonitorImportProgressUseCase;", "(Lcom/easycomic/domain/usecase/manga/GetAllMangaUseCase;Lcom/easycomic/domain/usecase/manga/SearchMangaUseCase;Lcom/easycomic/domain/usecase/manga/GetFavoriteMangaUseCase;Lcom/easycomic/domain/usecase/manga/GetRecentMangaUseCase;Lcom/easycomic/domain/usecase/manga/DeleteMangaUseCase;Lcom/easycomic/domain/usecase/manga/DeleteAllMangaUseCase;Lcom/easycomic/domain/usecase/manga/ToggleFavoriteUseCase;Lcom/easycomic/domain/usecase/manga/ImportComicUseCase;Lcom/easycomic/domain/usecase/manga/BatchImportComicsUseCase;Lcom/easycomic/domain/usecase/manga/MonitorImportProgressUseCase;)V", "_filterOption", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/easycomic/ui/bookshelf/FilterOption;", "_importProgress", "Lcom/easycomic/domain/model/ImportProgress;", "_importState", "Lcom/easycomic/ui/bookshelf/ImportState;", "_searchQuery", "", "_selectedMangaIds", "", "", "_selectionMode", "", "_sortOption", "Lcom/easycomic/ui/bookshelf/SortOption;", "_uiState", "Lcom/easycomic/ui/bookshelf/BookshelfUiState;", "filterOption", "Lkotlinx/coroutines/flow/StateFlow;", "getFilterOption", "()Lkotlinx/coroutines/flow/StateFlow;", "importProgress", "getImportProgress", "importState", "getImportState", "searchQuery", "getSearchQuery", "selectedMangaIds", "getSelectedMangaIds", "selectionMode", "getSelectionMode", "sortOption", "getSortOption", "uiState", "getUiState", "applyFiltersAndSort", "", "query", "sort", "filter", "cancelImport", "clearError", "clearImportError", "deleteSelectedManga", "deselectAll", "importComic", "uri", "Landroid/net/Uri;", "importComics", "uris", "", "loadAllManga", "monitorImportProgress", "resetImportState", "searchManga", "selectAll", "setFilterOption", "setSortOption", "toggleFavorite", "mangaId", "toggleMangaSelection", "toggleSelectionMode", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class BookshelfViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.easycomic.domain.usecase.manga.GetAllMangaUseCase getAllMangaUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.easycomic.domain.usecase.manga.SearchMangaUseCase searchMangaUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.easycomic.domain.usecase.manga.GetFavoriteMangaUseCase getFavoriteMangaUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.easycomic.domain.usecase.manga.GetRecentMangaUseCase getRecentMangaUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.easycomic.domain.usecase.manga.DeleteMangaUseCase deleteMangaUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.easycomic.domain.usecase.manga.DeleteAllMangaUseCase deleteAllMangaUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.easycomic.domain.usecase.manga.ToggleFavoriteUseCase toggleFavoriteUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.easycomic.domain.usecase.manga.ImportComicUseCase importComicUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.easycomic.domain.usecase.manga.BatchImportComicsUseCase batchImportComicsUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.easycomic.domain.usecase.manga.MonitorImportProgressUseCase monitorImportProgressUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.easycomic.ui.bookshelf.BookshelfUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.easycomic.ui.bookshelf.BookshelfUiState> uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _searchQuery = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> searchQuery = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.easycomic.ui.bookshelf.SortOption> _sortOption = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.easycomic.ui.bookshelf.SortOption> sortOption = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.easycomic.ui.bookshelf.FilterOption> _filterOption = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.easycomic.ui.bookshelf.FilterOption> filterOption = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _selectionMode = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> selectionMode = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.Set<java.lang.Long>> _selectedMangaIds = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.Set<java.lang.Long>> selectedMangaIds = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.easycomic.ui.bookshelf.ImportState> _importState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.easycomic.ui.bookshelf.ImportState> importState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.easycomic.domain.model.ImportProgress> _importProgress = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.easycomic.domain.model.ImportProgress> importProgress = null;
    
    @javax.inject.Inject()
    public BookshelfViewModel(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.usecase.manga.GetAllMangaUseCase getAllMangaUseCase, @org.jetbrains.annotations.NotNull()
    com.easycomic.domain.usecase.manga.SearchMangaUseCase searchMangaUseCase, @org.jetbrains.annotations.NotNull()
    com.easycomic.domain.usecase.manga.GetFavoriteMangaUseCase getFavoriteMangaUseCase, @org.jetbrains.annotations.NotNull()
    com.easycomic.domain.usecase.manga.GetRecentMangaUseCase getRecentMangaUseCase, @org.jetbrains.annotations.NotNull()
    com.easycomic.domain.usecase.manga.DeleteMangaUseCase deleteMangaUseCase, @org.jetbrains.annotations.NotNull()
    com.easycomic.domain.usecase.manga.DeleteAllMangaUseCase deleteAllMangaUseCase, @org.jetbrains.annotations.NotNull()
    com.easycomic.domain.usecase.manga.ToggleFavoriteUseCase toggleFavoriteUseCase, @org.jetbrains.annotations.NotNull()
    com.easycomic.domain.usecase.manga.ImportComicUseCase importComicUseCase, @org.jetbrains.annotations.NotNull()
    com.easycomic.domain.usecase.manga.BatchImportComicsUseCase batchImportComicsUseCase, @org.jetbrains.annotations.NotNull()
    com.easycomic.domain.usecase.manga.MonitorImportProgressUseCase monitorImportProgressUseCase) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.easycomic.ui.bookshelf.BookshelfUiState> getUiState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getSearchQuery() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.easycomic.ui.bookshelf.SortOption> getSortOption() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.easycomic.ui.bookshelf.FilterOption> getFilterOption() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getSelectionMode() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.Set<java.lang.Long>> getSelectedMangaIds() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.easycomic.ui.bookshelf.ImportState> getImportState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.easycomic.domain.model.ImportProgress> getImportProgress() {
        return null;
    }
    
    /**
     * 监听导入进度
     */
    private final void monitorImportProgress() {
    }
    
    /**
     * 导入单个漫画文件
     */
    public final void importComic(@org.jetbrains.annotations.NotNull()
    android.net.Uri uri) {
    }
    
    /**
     * 批量导入漫画文件
     */
    public final void importComics(@org.jetbrains.annotations.NotNull()
    java.util.List<? extends android.net.Uri> uris) {
    }
    
    /**
     * 取消导入
     */
    public final void cancelImport() {
    }
    
    /**
     * 重置导入状态
     */
    public final void resetImportState() {
    }
    
    /**
     * 清除导入错误
     */
    public final void clearImportError() {
    }
    
    /**
     * 加载所有漫画
     */
    private final void loadAllManga() {
    }
    
    /**
     * 搜索漫画
     */
    public final void searchManga(@org.jetbrains.annotations.NotNull()
    java.lang.String query) {
    }
    
    /**
     * 设置排序选项
     */
    public final void setSortOption(@org.jetbrains.annotations.NotNull()
    com.easycomic.ui.bookshelf.SortOption sortOption) {
    }
    
    /**
     * 设置筛选选项
     */
    public final void setFilterOption(@org.jetbrains.annotations.NotNull()
    com.easycomic.ui.bookshelf.FilterOption filterOption) {
    }
    
    /**
     * 应用筛选和排序
     */
    private final void applyFiltersAndSort(java.lang.String query, com.easycomic.ui.bookshelf.SortOption sort, com.easycomic.ui.bookshelf.FilterOption filter) {
    }
    
    /**
     * 切换选择模式
     */
    public final void toggleSelectionMode() {
    }
    
    /**
     * 选择/取消选择漫画
     */
    public final void toggleMangaSelection(long mangaId) {
    }
    
    /**
     * 全选
     */
    public final void selectAll() {
    }
    
    /**
     * 取消全选
     */
    public final void deselectAll() {
    }
    
    /**
     * 删除选中的漫画
     */
    public final void deleteSelectedManga() {
    }
    
    /**
     * 切换收藏状态
     */
    public final void toggleFavorite(long mangaId) {
    }
    
    /**
     * 清除错误
     */
    public final void clearError() {
    }
}