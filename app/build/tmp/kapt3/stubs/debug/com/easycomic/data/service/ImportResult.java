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
 * 导入结果
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0014\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0087\b\u0018\u00002\u00020\u0001B;\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\t\u0012\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\u0002\u0010\fJ\t\u0010\u0018\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0019\u001a\u00020\u0005H\u00c6\u0003J\u0010\u0010\u001a\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003\u00a2\u0006\u0002\u0010\u0012J\u000b\u0010\u001b\u001a\u0004\u0018\u00010\tH\u00c6\u0003J\u000b\u0010\u001c\u001a\u0004\u0018\u00010\u000bH\u00c6\u0003JF\u0010\u001d\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00072\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\t2\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u000bH\u00c6\u0001\u00a2\u0006\u0002\u0010\u001eJ\u0013\u0010\u001f\u001a\u00020 2\b\u0010!\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\"\u001a\u00020\u0005H\u00d6\u0001J\t\u0010#\u001a\u00020\u000bH\u00d6\u0001R\u0013\u0010\n\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0013\u0010\b\u001a\u0004\u0018\u00010\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0015\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\n\n\u0002\u0010\u0013\u001a\u0004\b\u0011\u0010\u0012R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017\u00a8\u0006$"}, d2 = {"Lcom/easycomic/data/service/ImportResult;", "", "status", "Lcom/easycomic/data/service/ImportStatus;", "progress", "", "mangaId", "", "manga", "Lcom/easycomic/domain/model/Manga;", "error", "", "(Lcom/easycomic/data/service/ImportStatus;ILjava/lang/Long;Lcom/easycomic/domain/model/Manga;Ljava/lang/String;)V", "getError", "()Ljava/lang/String;", "getManga", "()Lcom/easycomic/domain/model/Manga;", "getMangaId", "()Ljava/lang/Long;", "Ljava/lang/Long;", "getProgress", "()I", "getStatus", "()Lcom/easycomic/data/service/ImportStatus;", "component1", "component2", "component3", "component4", "component5", "copy", "(Lcom/easycomic/data/service/ImportStatus;ILjava/lang/Long;Lcom/easycomic/domain/model/Manga;Ljava/lang/String;)Lcom/easycomic/data/service/ImportResult;", "equals", "", "other", "hashCode", "toString", "app_debug"})
public final class ImportResult {
    @org.jetbrains.annotations.NotNull()
    private final com.easycomic.data.service.ImportStatus status = null;
    private final int progress = 0;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Long mangaId = null;
    @org.jetbrains.annotations.Nullable()
    private final com.easycomic.domain.model.Manga manga = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String error = null;
    
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.data.service.ImportStatus component1() {
        return null;
    }
    
    public final int component2() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Long component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.easycomic.domain.model.Manga component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.data.service.ImportResult copy(@org.jetbrains.annotations.NotNull()
    com.easycomic.data.service.ImportStatus status, int progress, @org.jetbrains.annotations.Nullable()
    java.lang.Long mangaId, @org.jetbrains.annotations.Nullable()
    com.easycomic.domain.model.Manga manga, @org.jetbrains.annotations.Nullable()
    java.lang.String error) {
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
    
    public ImportResult(@org.jetbrains.annotations.NotNull()
    com.easycomic.data.service.ImportStatus status, int progress, @org.jetbrains.annotations.Nullable()
    java.lang.Long mangaId, @org.jetbrains.annotations.Nullable()
    com.easycomic.domain.model.Manga manga, @org.jetbrains.annotations.Nullable()
    java.lang.String error) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.data.service.ImportStatus getStatus() {
        return null;
    }
    
    public final int getProgress() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Long getMangaId() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.easycomic.domain.model.Manga getManga() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getError() {
        return null;
    }
}