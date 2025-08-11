package com.easycomic.data.service;

import android.content.Context;
import android.net.Uri;
import com.easycomic.data.model.ComicInfo;
import com.easycomic.data.model.ImageData;
import com.easycomic.data.parser.ComicParser;
import com.easycomic.data.repository.MangaRepositoryImpl;
import com.easycomic.domain.model.Manga;
import com.easycomic.domain.model.ReadingStatus;
import com.easycomic.data.util.FileHelper;
import dagger.hilt.android.qualifiers.ApplicationContext;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.Flow;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FilenameUtils;
import timber.log.Timber;
import java.io.*;
import java.util.zip.ZipEntry;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 批量导入结果
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0014\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0087\b\u0018\u00002\u00020\u0001BI\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0005\u0012\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\b\u0012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\n\u0012\u000e\b\u0002\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\n0\f\u00a2\u0006\u0002\u0010\rJ\t\u0010\u0019\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001a\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001b\u001a\u00020\u0005H\u00c6\u0003J\u000b\u0010\u001c\u001a\u0004\u0018\u00010\bH\u00c6\u0003J\u000b\u0010\u001d\u001a\u0004\u0018\u00010\nH\u00c6\u0003J\u000f\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\n0\fH\u00c6\u0003JO\u0010\u001f\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\b2\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\n2\u000e\b\u0002\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\n0\fH\u00c6\u0001J\u0013\u0010 \u001a\u00020!2\b\u0010\"\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010#\u001a\u00020\u0005H\u00d6\u0001J\t\u0010$\u001a\u00020\bH\u00d6\u0001R\u0013\u0010\u0007\u001a\u0004\u0018\u00010\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0013\u0010\t\u001a\u0004\u0018\u00010\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0017\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\n0\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0011\u00a8\u0006%"}, d2 = {"Lcom/easycomic/data/service/BatchImportResult;", "", "status", "Lcom/easycomic/data/service/BatchImportStatus;", "currentIndex", "", "total", "currentFile", "", "currentItemResult", "Lcom/easycomic/data/service/ImportResult;", "results", "", "(Lcom/easycomic/data/service/BatchImportStatus;IILjava/lang/String;Lcom/easycomic/data/service/ImportResult;Ljava/util/List;)V", "getCurrentFile", "()Ljava/lang/String;", "getCurrentIndex", "()I", "getCurrentItemResult", "()Lcom/easycomic/data/service/ImportResult;", "getResults", "()Ljava/util/List;", "getStatus", "()Lcom/easycomic/data/service/BatchImportStatus;", "getTotal", "component1", "component2", "component3", "component4", "component5", "component6", "copy", "equals", "", "other", "hashCode", "toString", "app_debug"})
public final class BatchImportResult {
    @org.jetbrains.annotations.NotNull()
    private final com.easycomic.data.service.BatchImportStatus status = null;
    private final int currentIndex = 0;
    private final int total = 0;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String currentFile = null;
    @org.jetbrains.annotations.Nullable()
    private final com.easycomic.data.service.ImportResult currentItemResult = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.easycomic.data.service.ImportResult> results = null;
    
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.data.service.BatchImportStatus component1() {
        return null;
    }
    
    public final int component2() {
        return 0;
    }
    
    public final int component3() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.easycomic.data.service.ImportResult component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.easycomic.data.service.ImportResult> component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.data.service.BatchImportResult copy(@org.jetbrains.annotations.NotNull()
    com.easycomic.data.service.BatchImportStatus status, int currentIndex, int total, @org.jetbrains.annotations.Nullable()
    java.lang.String currentFile, @org.jetbrains.annotations.Nullable()
    com.easycomic.data.service.ImportResult currentItemResult, @org.jetbrains.annotations.NotNull()
    java.util.List<com.easycomic.data.service.ImportResult> results) {
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
    
    public BatchImportResult(@org.jetbrains.annotations.NotNull()
    com.easycomic.data.service.BatchImportStatus status, int currentIndex, int total, @org.jetbrains.annotations.Nullable()
    java.lang.String currentFile, @org.jetbrains.annotations.Nullable()
    com.easycomic.data.service.ImportResult currentItemResult, @org.jetbrains.annotations.NotNull()
    java.util.List<com.easycomic.data.service.ImportResult> results) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.data.service.BatchImportStatus getStatus() {
        return null;
    }
    
    public final int getCurrentIndex() {
        return 0;
    }
    
    public final int getTotal() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getCurrentFile() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.easycomic.data.service.ImportResult getCurrentItemResult() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.easycomic.data.service.ImportResult> getResults() {
        return null;
    }
}