package com.easycomic.ui.main;

import android.content.Intent;
import android.net.Uri;
import android.provider.DocumentsContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.compose.foundation.layout.Arrangement;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.ExperimentalMaterial3Api;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.font.FontWeight;
import com.easycomic.model.Comic;
import timber.log.Timber;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000(\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\b\u0002\u001a\u0016\u0010\u0000\u001a\u00020\u00012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00010\u0003H\u0003\u001a\u001e\u0010\u0004\u001a\u00020\u00012\u0006\u0010\u0005\u001a\u00020\u00062\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00010\u0003H\u0003\u001a$\u0010\b\u001a\u00020\u00012\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00010\u0003H\u0003\u001a*\u0010\u000b\u001a\u00020\u00012\u0012\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00010\r2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00010\u0003H\u0007\u001a*\u0010\u000f\u001a\u00020\u00012\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00060\u00112\u0012\u0010\u0012\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00010\rH\u0003\u00a8\u0006\u0013"}, d2 = {"EmptyState", "", "onOpenFilePicker", "Lkotlin/Function0;", "FileCard", "filePath", "", "onClick", "InfoDialog", "onDismiss", "onShowMore", "MainScreen", "onOpenReader", "Lkotlin/Function1;", "onShowInfo", "RecentFilesList", "files", "", "onFileClick", "app_debug"})
public final class MainScreenKt {
    
    /**
     * 简化的主界面
     */
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void MainScreen(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onOpenReader, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onShowInfo) {
    }
    
    /**
     * 空状态
     */
    @androidx.compose.runtime.Composable()
    private static final void EmptyState(kotlin.jvm.functions.Function0<kotlin.Unit> onOpenFilePicker) {
    }
    
    /**
     * 最近文件列表
     */
    @androidx.compose.runtime.Composable()
    private static final void RecentFilesList(java.util.List<java.lang.String> files, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onFileClick) {
    }
    
    /**
     * 文件卡片
     */
    @androidx.compose.runtime.Composable()
    private static final void FileCard(java.lang.String filePath, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    /**
     * 信息对话框
     */
    @androidx.compose.runtime.Composable()
    private static final void InfoDialog(kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, kotlin.jvm.functions.Function0<kotlin.Unit> onShowMore) {
    }
}