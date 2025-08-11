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
 * 阅读器UI状态
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b!\b\u0087\b\u0018\u00002\u00020\u0001Bm\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u0012\b\b\u0002\u0010\t\u001a\u00020\b\u0012\b\b\u0002\u0010\n\u001a\u00020\u000b\u0012\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\r\u0012\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u000b\u0012\b\b\u0002\u0010\u000f\u001a\u00020\b\u0012\b\b\u0002\u0010\u0010\u001a\u00020\b\u00a2\u0006\u0002\u0010\u0011J\t\u0010\u001f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010 \u001a\u00020\bH\u00c6\u0003J\t\u0010!\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\"\u001a\u00020\u0006H\u00c6\u0003J\t\u0010#\u001a\u00020\bH\u00c6\u0003J\t\u0010$\u001a\u00020\bH\u00c6\u0003J\t\u0010%\u001a\u00020\u000bH\u00c6\u0003J\u000b\u0010&\u001a\u0004\u0018\u00010\rH\u00c6\u0003J\u000b\u0010\'\u001a\u0004\u0018\u00010\u000bH\u00c6\u0003J\t\u0010(\u001a\u00020\bH\u00c6\u0003Jq\u0010)\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\b2\b\b\u0002\u0010\n\u001a\u00020\u000b2\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\r2\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u000b2\b\b\u0002\u0010\u000f\u001a\u00020\b2\b\b\u0002\u0010\u0010\u001a\u00020\bH\u00c6\u0001J\u0013\u0010*\u001a\u00020\u00032\b\u0010+\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010,\u001a\u00020\bH\u00d6\u0001J\t\u0010-\u001a\u00020\u000bH\u00d6\u0001R\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0013\u0010\f\u001a\u0004\u0018\u00010\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0013\u0010\u000e\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0013R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0002\u0010\u0019R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0004\u0010\u0019R\u0011\u0010\t\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0015R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001cR\u0011\u0010\u0010\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u0015R\u0011\u0010\u000f\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u0015\u00a8\u0006."}, d2 = {"Lcom/easycomic/ui/reader/ReaderUiState;", "", "isLoading", "", "isLoadingImage", "progress", "", "currentPage", "", "maxPage", "comicTitle", "", "currentPageBitmap", "Landroid/graphics/Bitmap;", "error", "screenWidth", "screenHeight", "(ZZFIILjava/lang/String;Landroid/graphics/Bitmap;Ljava/lang/String;II)V", "getComicTitle", "()Ljava/lang/String;", "getCurrentPage", "()I", "getCurrentPageBitmap", "()Landroid/graphics/Bitmap;", "getError", "()Z", "getMaxPage", "getProgress", "()F", "getScreenHeight", "getScreenWidth", "component1", "component10", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "toString", "app_debug"})
public final class ReaderUiState {
    private final boolean isLoading = false;
    private final boolean isLoadingImage = false;
    private final float progress = 0.0F;
    private final int currentPage = 0;
    private final int maxPage = 0;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String comicTitle = null;
    @org.jetbrains.annotations.Nullable()
    private final android.graphics.Bitmap currentPageBitmap = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String error = null;
    private final int screenWidth = 0;
    private final int screenHeight = 0;
    
    public final boolean component1() {
        return false;
    }
    
    public final int component10() {
        return 0;
    }
    
    public final boolean component2() {
        return false;
    }
    
    public final float component3() {
        return 0.0F;
    }
    
    public final int component4() {
        return 0;
    }
    
    public final int component5() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final android.graphics.Bitmap component7() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component8() {
        return null;
    }
    
    public final int component9() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.ui.reader.ReaderUiState copy(boolean isLoading, boolean isLoadingImage, float progress, int currentPage, int maxPage, @org.jetbrains.annotations.NotNull()
    java.lang.String comicTitle, @org.jetbrains.annotations.Nullable()
    android.graphics.Bitmap currentPageBitmap, @org.jetbrains.annotations.Nullable()
    java.lang.String error, int screenWidth, int screenHeight) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
    
    public ReaderUiState(boolean isLoading, boolean isLoadingImage, float progress, int currentPage, int maxPage, @org.jetbrains.annotations.NotNull()
    java.lang.String comicTitle, @org.jetbrains.annotations.Nullable()
    android.graphics.Bitmap currentPageBitmap, @org.jetbrains.annotations.Nullable()
    java.lang.String error, int screenWidth, int screenHeight) {
        super();
    }
    
    public final boolean isLoading() {
        return false;
    }
    
    public final boolean isLoadingImage() {
        return false;
    }
    
    public final float getProgress() {
        return 0.0F;
    }
    
    public final int getCurrentPage() {
        return 0;
    }
    
    public final int getMaxPage() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getComicTitle() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final android.graphics.Bitmap getCurrentPageBitmap() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getError() {
        return null;
    }
    
    public final int getScreenWidth() {
        return 0;
    }
    
    public final int getScreenHeight() {
        return 0;
    }
    
    public ReaderUiState() {
        super();
    }
}