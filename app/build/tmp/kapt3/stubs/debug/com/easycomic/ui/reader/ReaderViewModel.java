package com.easycomic.ui.reader;

import androidx.lifecycle.ViewModel;
import com.easycomic.domain.model.Manga;
import com.easycomic.domain.usecase.manga.GetMangaByIdUseCase;
import com.easycomic.domain.usecase.manga.UpdateReadingProgressUseCase;
import com.easycomic.image.ImageLoader;
import com.easycomic.model.Comic;
import com.easycomic.model.ComicPage;
import com.easycomic.model.ParseResult;
import com.easycomic.parser.ComicParser;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.StateFlow;
import timber.log.Timber;
import javax.inject.Inject;

/**
 * 阅读器ViewModel - 使用 Clean Architecture
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000d\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\r\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0006\u0010\u0018\u001a\u00020\u0019J\u0006\u0010\u001a\u001a\u00020\u0019J\u0006\u0010\u001b\u001a\u00020\u001cJ\u0006\u0010\u001d\u001a\u00020\u001eJ\u0006\u0010\u001f\u001a\u00020\u001cJ\u000e\u0010 \u001a\u00020\u00192\u0006\u0010!\u001a\u00020\"J\u000e\u0010#\u001a\u00020\u00192\u0006\u0010$\u001a\u00020\u001cJ\b\u0010%\u001a\u00020\u0019H\u0002J\u0006\u0010&\u001a\u00020\u0019J\b\u0010\'\u001a\u00020\u0019H\u0014J\u0010\u0010(\u001a\u00020\u00192\u0006\u0010)\u001a\u00020\"H\u0002J\u0006\u0010*\u001a\u00020\u0019J\u0010\u0010+\u001a\u00020\u00192\u0006\u0010!\u001a\u00020\"H\u0002J\u0016\u0010,\u001a\u00020\u00192\u0006\u0010-\u001a\u00020\"2\u0006\u0010.\u001a\u00020\"R\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\f\u001a\u0004\u0018\u00010\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00100\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0013\u001a\u0004\u0018\u00010\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\t0\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006/"}, d2 = {"Lcom/easycomic/ui/reader/ReaderViewModel;", "Landroidx/lifecycle/ViewModel;", "getMangaByIdUseCase", "Lcom/easycomic/domain/usecase/manga/GetMangaByIdUseCase;", "updateReadingProgressUseCase", "Lcom/easycomic/domain/usecase/manga/UpdateReadingProgressUseCase;", "(Lcom/easycomic/domain/usecase/manga/GetMangaByIdUseCase;Lcom/easycomic/domain/usecase/manga/UpdateReadingProgressUseCase;)V", "_uiState", "Landroidx/compose/runtime/MutableState;", "Lcom/easycomic/ui/reader/ReaderUiState;", "comicParser", "Lcom/easycomic/parser/ComicParser;", "currentComic", "Lcom/easycomic/model/Comic;", "currentPageList", "", "Lcom/easycomic/model/ComicPage;", "imageLoader", "Lcom/easycomic/image/ImageLoader;", "progressSaveJob", "Lkotlinx/coroutines/Job;", "uiState", "getUiState", "()Landroidx/compose/runtime/MutableState;", "clearError", "", "forceSaveProgress", "getCacheInfo", "", "getCurrentProgress", "", "getMemoryInfo", "goToPage", "pageNumber", "", "loadComic", "filePath", "loadCurrentPage", "nextPage", "onCleared", "prefetchNextPages", "currentPageIndex", "previousPage", "saveProgress", "setScreenSize", "width", "height", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class ReaderViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.easycomic.domain.usecase.manga.GetMangaByIdUseCase getMangaByIdUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.easycomic.domain.usecase.manga.UpdateReadingProgressUseCase updateReadingProgressUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.easycomic.parser.ComicParser comicParser = null;
    @org.jetbrains.annotations.NotNull()
    private final com.easycomic.image.ImageLoader imageLoader = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState<com.easycomic.ui.reader.ReaderUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState<com.easycomic.ui.reader.ReaderUiState> uiState = null;
    @org.jetbrains.annotations.Nullable()
    private com.easycomic.model.Comic currentComic;
    @org.jetbrains.annotations.NotNull()
    private java.util.List<com.easycomic.model.ComicPage> currentPageList;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job progressSaveJob;
    
    @javax.inject.Inject()
    public ReaderViewModel(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.usecase.manga.GetMangaByIdUseCase getMangaByIdUseCase, @org.jetbrains.annotations.NotNull()
    com.easycomic.domain.usecase.manga.UpdateReadingProgressUseCase updateReadingProgressUseCase) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.compose.runtime.MutableState<com.easycomic.ui.reader.ReaderUiState> getUiState() {
        return null;
    }
    
    /**
     * 加载漫画
     */
    public final void loadComic(@org.jetbrains.annotations.NotNull()
    java.lang.String filePath) {
    }
    
    /**
     * 加载当前页面
     */
    private final void loadCurrentPage() {
    }
    
    /**
     * 导航到下一页
     */
    public final void nextPage() {
    }
    
    /**
     * 导航到上一页
     */
    public final void previousPage() {
    }
    
    /**
     * 跳转到指定页面
     */
    public final void goToPage(int pageNumber) {
    }
    
    /**
     * 保存阅读进度（带防抖）
     */
    private final void saveProgress(int pageNumber) {
    }
    
    /**
     * 预加载下一页
     */
    private final void prefetchNextPages(int currentPageIndex) {
    }
    
    /**
     * 设置屏幕尺寸
     */
    public final void setScreenSize(int width, int height) {
    }
    
    /**
     * 清除错误信息
     */
    public final void clearError() {
    }
    
    /**
     * 获取当前进度
     */
    public final float getCurrentProgress() {
        return 0.0F;
    }
    
    /**
     * 强制保存进度
     */
    public final void forceSaveProgress() {
    }
    
    /**
     * 获取内存信息
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getMemoryInfo() {
        return null;
    }
    
    /**
     * 获取缓存信息
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getCacheInfo() {
        return null;
    }
    
    @java.lang.Override()
    protected void onCleared() {
    }
}