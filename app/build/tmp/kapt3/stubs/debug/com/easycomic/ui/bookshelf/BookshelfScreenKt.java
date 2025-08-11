package com.easycomic.ui.bookshelf;

import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.lazy.grid.GridCells;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.style.TextAlign;
import androidx.navigation.NavController;
import androidx.activity.result.contract.ActivityResultContracts;
import com.easycomic.domain.model.Manga;
import com.easycomic.ui.bookshelf.BookshelfViewModel.SortOption;
import com.easycomic.ui.bookshelf.BookshelfViewModel.FilterOption;
import com.easycomic.ui.bookshelf.BookshelfViewModel.ImportProgress;
import com.easycomic.ui.bookshelf.BookshelfViewModel.ImportState;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000X\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0010\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010 \n\u0000\n\u0002\u0010\"\n\u0002\u0010\t\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a.\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0014\b\u0002\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0007\u001a\u00ba\u0001\u0010\t\u001a\u00020\u00012\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000b2\u0012\u0010\u000f\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u00010\u00072\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00010\u00112\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00010\u00112\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00010\u00112\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00010\u00112\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00010\u00112\u0006\u0010\u0016\u001a\u00020\r2\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00010\u00112\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00010\u00112\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00010\u00112\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00010\u0011H\u0003\u001a\u001e\u0010\u001b\u001a\u00020\u00012\u0006\u0010\u000e\u001a\u00020\u000b2\f\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u00010\u0011H\u0003\u001a\u001e\u0010\u001d\u001a\u00020\u00012\u0006\u0010\u001e\u001a\u00020\u000b2\f\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00010\u0011H\u0003\u001a2\u0010 \u001a\u00020\u00012\u0006\u0010!\u001a\u00020\"2\u0012\u0010#\u001a\u000e\u0012\u0004\u0012\u00020\"\u0012\u0004\u0012\u00020\u00010\u00072\f\u0010$\u001a\b\u0012\u0004\u0012\u00020\u00010\u0011H\u0003\u001a\b\u0010%\u001a\u00020\u0001H\u0003\u001ah\u0010&\u001a\u00020\u00012\f\u0010\'\u001a\b\u0012\u0004\u0012\u00020\b0(2\u0006\u0010\u0016\u001a\u00020\r2\f\u0010)\u001a\b\u0012\u0004\u0012\u00020+0*2\u0012\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00010\u00072\u0012\u0010,\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00010\u00072\u0012\u0010-\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0003\u001a2\u0010.\u001a\u00020\u00012\u0006\u0010/\u001a\u0002002\u0012\u00101\u001a\u000e\u0012\u0004\u0012\u000200\u0012\u0004\u0012\u00020\u00010\u00072\f\u0010$\u001a\b\u0012\u0004\u0012\u00020\u00010\u0011H\u0003\u00a8\u00062"}, d2 = {"BookshelfScreen", "", "viewModel", "Lcom/easycomic/ui/bookshelf/BookshelfViewModel;", "navController", "Landroidx/navigation/NavController;", "onMangaClick", "Lkotlin/Function1;", "Lcom/easycomic/domain/model/Manga;", "BookshelfTopAppBar", "title", "", "showSearch", "", "searchQuery", "onSearchQueryChange", "onSearchToggle", "Lkotlin/Function0;", "onSortClick", "onFilterClick", "onAddClick", "onBatchImportClick", "selectionMode", "onSelectAll", "onDeselectAll", "onDeleteSelected", "onCloseSelection", "EmptyScreen", "onAddMangaClick", "ErrorScreen", "error", "onRetry", "FilterDialog", "currentFilter", "Lcom/easycomic/ui/bookshelf/FilterOption;", "onFilterSelected", "onDismiss", "LoadingIndicator", "MangaGrid", "mangaList", "", "selectedMangaIds", "", "", "onMangaLongClick", "onFavoriteClick", "SortDialog", "currentSort", "Lcom/easycomic/ui/bookshelf/SortOption;", "onSortSelected", "app_debug"})
public final class BookshelfScreenKt {
    
    /**
     * 书架界面
     */
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void BookshelfScreen(@org.jetbrains.annotations.NotNull()
    com.easycomic.ui.bookshelf.BookshelfViewModel viewModel, @org.jetbrains.annotations.NotNull()
    androidx.navigation.NavController navController, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.easycomic.domain.model.Manga, kotlin.Unit> onMangaClick) {
    }
    
    /**
     * 书架顶部应用栏
     */
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    private static final void BookshelfTopAppBar(java.lang.String title, boolean showSearch, java.lang.String searchQuery, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onSearchQueryChange, kotlin.jvm.functions.Function0<kotlin.Unit> onSearchToggle, kotlin.jvm.functions.Function0<kotlin.Unit> onSortClick, kotlin.jvm.functions.Function0<kotlin.Unit> onFilterClick, kotlin.jvm.functions.Function0<kotlin.Unit> onAddClick, kotlin.jvm.functions.Function0<kotlin.Unit> onBatchImportClick, boolean selectionMode, kotlin.jvm.functions.Function0<kotlin.Unit> onSelectAll, kotlin.jvm.functions.Function0<kotlin.Unit> onDeselectAll, kotlin.jvm.functions.Function0<kotlin.Unit> onDeleteSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onCloseSelection) {
    }
    
    /**
     * 漫画网格
     */
    @androidx.compose.runtime.Composable()
    private static final void MangaGrid(java.util.List<com.easycomic.domain.model.Manga> mangaList, boolean selectionMode, java.util.Set<java.lang.Long> selectedMangaIds, kotlin.jvm.functions.Function1<? super com.easycomic.domain.model.Manga, kotlin.Unit> onMangaClick, kotlin.jvm.functions.Function1<? super com.easycomic.domain.model.Manga, kotlin.Unit> onMangaLongClick, kotlin.jvm.functions.Function1<? super com.easycomic.domain.model.Manga, kotlin.Unit> onFavoriteClick) {
    }
    
    /**
     * 加载指示器
     */
    @androidx.compose.runtime.Composable()
    private static final void LoadingIndicator() {
    }
    
    /**
     * 错误界面
     */
    @androidx.compose.runtime.Composable()
    private static final void ErrorScreen(java.lang.String error, kotlin.jvm.functions.Function0<kotlin.Unit> onRetry) {
    }
    
    /**
     * 空界面
     */
    @androidx.compose.runtime.Composable()
    private static final void EmptyScreen(java.lang.String searchQuery, kotlin.jvm.functions.Function0<kotlin.Unit> onAddMangaClick) {
    }
    
    /**
     * 排序对话框
     */
    @androidx.compose.runtime.Composable()
    private static final void SortDialog(com.easycomic.ui.bookshelf.SortOption currentSort, kotlin.jvm.functions.Function1<? super com.easycomic.ui.bookshelf.SortOption, kotlin.Unit> onSortSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    /**
     * 筛选对话框
     */
    @androidx.compose.runtime.Composable()
    private static final void FilterDialog(com.easycomic.ui.bookshelf.FilterOption currentFilter, kotlin.jvm.functions.Function1<? super com.easycomic.ui.bookshelf.FilterOption, kotlin.Unit> onFilterSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
}