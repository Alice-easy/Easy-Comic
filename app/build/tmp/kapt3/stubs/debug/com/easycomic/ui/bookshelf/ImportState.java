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
 * 导入状态
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0019\b\u0087\b\u0018\u00002\u00020\u0001BS\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\b\u0012\b\b\u0002\u0010\t\u001a\u00020\n\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\f\u0012\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u000e\u00a2\u0006\u0002\u0010\u000fJ\t\u0010\u001b\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001c\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010\u001d\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\u000b\u0010\u001e\u001a\u0004\u0018\u00010\bH\u00c6\u0003J\t\u0010\u001f\u001a\u00020\nH\u00c6\u0003J\u000b\u0010 \u001a\u0004\u0018\u00010\fH\u00c6\u0003J\u000b\u0010!\u001a\u0004\u0018\u00010\u000eH\u00c6\u0003JW\u0010\"\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\b2\b\b\u0002\u0010\t\u001a\u00020\n2\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\f2\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u000eH\u00c6\u0001J\u0013\u0010#\u001a\u00020\u00032\b\u0010$\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010%\u001a\u00020\nH\u00d6\u0001J\t\u0010&\u001a\u00020\u000eH\u00d6\u0001R\u0013\u0010\u000b\u001a\u0004\u0018\u00010\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0013\u0010\u0007\u001a\u0004\u0018\u00010\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0013\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0013\u0010\r\u001a\u0004\u0018\u00010\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0004\u0010\u001aR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0002\u0010\u001a\u00a8\u0006\'"}, d2 = {"Lcom/easycomic/ui/bookshelf/ImportState;", "", "isImporting", "", "isBatchImport", "currentImportUri", "Landroid/net/Uri;", "currentImportResult", "Lcom/easycomic/domain/model/ImportComicResult;", "batchImportTotal", "", "batchImportResult", "Lcom/easycomic/domain/model/BatchImportComicResult;", "importError", "", "(ZZLandroid/net/Uri;Lcom/easycomic/domain/model/ImportComicResult;ILcom/easycomic/domain/model/BatchImportComicResult;Ljava/lang/String;)V", "getBatchImportResult", "()Lcom/easycomic/domain/model/BatchImportComicResult;", "getBatchImportTotal", "()I", "getCurrentImportResult", "()Lcom/easycomic/domain/model/ImportComicResult;", "getCurrentImportUri", "()Landroid/net/Uri;", "getImportError", "()Ljava/lang/String;", "()Z", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "equals", "other", "hashCode", "toString", "app_debug"})
public final class ImportState {
    private final boolean isImporting = false;
    private final boolean isBatchImport = false;
    @org.jetbrains.annotations.Nullable()
    private final android.net.Uri currentImportUri = null;
    @org.jetbrains.annotations.Nullable()
    private final com.easycomic.domain.model.ImportComicResult currentImportResult = null;
    private final int batchImportTotal = 0;
    @org.jetbrains.annotations.Nullable()
    private final com.easycomic.domain.model.BatchImportComicResult batchImportResult = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String importError = null;
    
    public final boolean component1() {
        return false;
    }
    
    public final boolean component2() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final android.net.Uri component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.easycomic.domain.model.ImportComicResult component4() {
        return null;
    }
    
    public final int component5() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.easycomic.domain.model.BatchImportComicResult component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.ui.bookshelf.ImportState copy(boolean isImporting, boolean isBatchImport, @org.jetbrains.annotations.Nullable()
    android.net.Uri currentImportUri, @org.jetbrains.annotations.Nullable()
    com.easycomic.domain.model.ImportComicResult currentImportResult, int batchImportTotal, @org.jetbrains.annotations.Nullable()
    com.easycomic.domain.model.BatchImportComicResult batchImportResult, @org.jetbrains.annotations.Nullable()
    java.lang.String importError) {
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
    
    public ImportState(boolean isImporting, boolean isBatchImport, @org.jetbrains.annotations.Nullable()
    android.net.Uri currentImportUri, @org.jetbrains.annotations.Nullable()
    com.easycomic.domain.model.ImportComicResult currentImportResult, int batchImportTotal, @org.jetbrains.annotations.Nullable()
    com.easycomic.domain.model.BatchImportComicResult batchImportResult, @org.jetbrains.annotations.Nullable()
    java.lang.String importError) {
        super();
    }
    
    public final boolean isImporting() {
        return false;
    }
    
    public final boolean isBatchImport() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final android.net.Uri getCurrentImportUri() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.easycomic.domain.model.ImportComicResult getCurrentImportResult() {
        return null;
    }
    
    public final int getBatchImportTotal() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.easycomic.domain.model.BatchImportComicResult getBatchImportResult() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getImportError() {
        return null;
    }
    
    public ImportState() {
        super();
    }
}