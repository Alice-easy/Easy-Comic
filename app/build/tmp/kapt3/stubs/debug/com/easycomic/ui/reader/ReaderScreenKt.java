package com.easycomic.ui.reader;

import androidx.compose.foundation.layout.Arrangement;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.ExperimentalMaterial3Api;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.style.TextAlign;
import timber.log.Timber;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000D\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\u0007\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\u001a4\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\f\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0003\u001a\b\u0010\t\u001a\u00020\u0001H\u0003\u001a,\u0010\n\u001a\u00020\u00012\u0006\u0010\u000b\u001a\u00020\f2\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0003\u001a\u0017\u0010\u000f\u001a\u00020\u00012\b\u0010\u0010\u001a\u0004\u0018\u00010\u0011H\u0003\u00a2\u0006\u0002\u0010\u0012\u001a:\u0010\u0013\u001a\u00020\u00012\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00152\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\u0012\u0010\u0017\u001a\u000e\u0012\u0004\u0012\u00020\u0015\u0012\u0004\u0012\u00020\u00010\u0018H\u0003\u001a4\u0010\u0019\u001a\u00020\u00012\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00152\u0006\u0010\u0010\u001a\u00020\u00112\u0012\u0010\u0017\u001a\u000e\u0012\u0004\u0012\u00020\u0015\u0012\u0004\u0012\u00020\u00010\u0018H\u0003\u001a&\u0010\u001a\u001a\u00020\u00012\u0006\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\f2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0007\u001a<\u0010\u001e\u001a\u00020\u00012\u0006\u0010\u001f\u001a\u00020\f2\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00152\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\f\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0003\u00a8\u0006!"}, d2 = {"ComicPageDisplay", "", "bitmap", "Landroid/graphics/Bitmap;", "isLoading", "", "onPagePrevious", "Lkotlin/Function0;", "onPageNext", "EmptyState", "ErrorDialog", "message", "", "onDismiss", "onBack", "LoadingIndicator", "progress", "", "(Ljava/lang/Float;)V", "PageSelectorDialog", "currentPage", "", "maxPage", "onPageSelected", "Lkotlin/Function1;", "ReaderBottomBar", "ReaderScreen", "viewModel", "Lcom/easycomic/ui/reader/ReaderViewModel;", "filePath", "ReaderTopAppBar", "title", "onInfo", "app_debug"})
public final class ReaderScreenKt {
    
    /**
     * 简化的阅读器界面
     */
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void ReaderScreen(@org.jetbrains.annotations.NotNull()
    com.easycomic.ui.reader.ReaderViewModel viewModel, @org.jetbrains.annotations.NotNull()
    java.lang.String filePath, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onBack) {
    }
    
    /**
     * 顶部应用栏
     */
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    private static final void ReaderTopAppBar(java.lang.String title, int currentPage, int maxPage, kotlin.jvm.functions.Function0<kotlin.Unit> onBack, kotlin.jvm.functions.Function0<kotlin.Unit> onInfo) {
    }
    
    /**
     * 底部控制栏
     */
    @androidx.compose.runtime.Composable()
    private static final void ReaderBottomBar(int currentPage, int maxPage, float progress, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onPageSelected) {
    }
    
    /**
     * 漫画页面显示
     */
    @androidx.compose.runtime.Composable()
    private static final void ComicPageDisplay(android.graphics.Bitmap bitmap, boolean isLoading, kotlin.jvm.functions.Function0<kotlin.Unit> onPagePrevious, kotlin.jvm.functions.Function0<kotlin.Unit> onPageNext) {
    }
    
    /**
     * 页面选择对话框
     */
    @androidx.compose.runtime.Composable()
    private static final void PageSelectorDialog(int currentPage, int maxPage, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onPageSelected) {
    }
    
    /**
     * 错误对话框
     */
    @androidx.compose.runtime.Composable()
    private static final void ErrorDialog(java.lang.String message, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, kotlin.jvm.functions.Function0<kotlin.Unit> onBack) {
    }
    
    /**
     * 加载指示器
     */
    @androidx.compose.runtime.Composable()
    private static final void LoadingIndicator(java.lang.Float progress) {
    }
    
    /**
     * 空状态
     */
    @androidx.compose.runtime.Composable()
    private static final void EmptyState() {
    }
}