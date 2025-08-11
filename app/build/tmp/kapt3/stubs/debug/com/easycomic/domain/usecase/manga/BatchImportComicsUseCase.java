package com.easycomic.domain.usecase.manga;

import android.net.Uri;
import com.easycomic.domain.model.Manga;
import com.easycomic.domain.model.ReadingStatus;
import com.easycomic.domain.model.ImportProgress;
import com.easycomic.domain.model.ImportComicResult;
import com.easycomic.domain.model.BatchImportComicResult;
import com.easycomic.domain.model.ImportStatus;
import com.easycomic.domain.usecase.manga.ImportProgressHolder;
import com.easycomic.domain.repository.MangaRepository;
import com.easycomic.domain.usecase.BaseUseCase;
import com.easycomic.domain.usecase.NoParametersUseCase;
import kotlinx.coroutines.flow.Flow;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 批量导入漫画用例
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0007\u0018\u00002\u000e\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u00030\u0001:\u0001\rB\u0015\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u0016\u0010\t\u001a\u00020\u00032\u0006\u0010\n\u001a\u00020\u000bH\u0096B\u00a2\u0006\u0002\u0010\fR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/easycomic/domain/usecase/manga/BatchImportComicsUseCase;", "Lcom/easycomic/domain/usecase/BaseUseCase;", "error/NonExistentClass", "Lcom/easycomic/domain/model/BatchImportComicResult;", "importComicUseCase", "Lcom/easycomic/domain/usecase/manga/ImportComicUseCase;", "importProgressHolder", "Lcom/easycomic/domain/usecase/manga/ImportProgressHolder;", "(Lcom/easycomic/domain/usecase/manga/ImportComicUseCase;Lcom/easycomic/domain/usecase/manga/ImportProgressHolder;)V", "invoke", "parameters", "Lcom/easycomic/domain/usecase/manga/BatchImportComicsUseCase$Params;", "(Lcom/easycomic/domain/usecase/manga/BatchImportComicsUseCase$Params;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Params", "app_debug"})
public final class BatchImportComicsUseCase implements com.easycomic.domain.usecase.BaseUseCase<error.NonExistentClass, com.easycomic.domain.model.BatchImportComicResult> {
    @org.jetbrains.annotations.NotNull()
    private final com.easycomic.domain.usecase.manga.ImportComicUseCase importComicUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.easycomic.domain.usecase.manga.ImportProgressHolder importProgressHolder = null;
    
    public BatchImportComicsUseCase(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.usecase.manga.ImportComicUseCase importComicUseCase, @org.jetbrains.annotations.NotNull()
    com.easycomic.domain.usecase.manga.ImportProgressHolder importProgressHolder) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object invoke(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.usecase.manga.BatchImportComicsUseCase.Params parameters, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.easycomic.domain.model.BatchImportComicResult> $completion) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\t\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001B+\u0012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\u0016\b\u0002\u0010\u0005\u001a\u0010\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\b\u0018\u00010\u0006\u00a2\u0006\u0002\u0010\tJ\u000f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00c6\u0003J\u0017\u0010\u000f\u001a\u0010\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\b\u0018\u00010\u0006H\u00c6\u0003J1\u0010\u0010\u001a\u00020\u00002\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u0016\b\u0002\u0010\u0005\u001a\u0010\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\b\u0018\u00010\u0006H\u00c6\u0001J\u0013\u0010\u0011\u001a\u00020\u00122\b\u0010\u0013\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0014\u001a\u00020\u0015H\u00d6\u0001J\t\u0010\u0016\u001a\u00020\u0017H\u00d6\u0001R\u0017\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u001f\u0010\u0005\u001a\u0010\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\b\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\r\u00a8\u0006\u0018"}, d2 = {"Lcom/easycomic/domain/usecase/manga/BatchImportComicsUseCase$Params;", "", "fileUris", "", "Landroid/net/Uri;", "onProgress", "Lkotlin/Function1;", "Lcom/easycomic/domain/model/ImportProgress;", "", "(Ljava/util/List;Lkotlin/jvm/functions/Function1;)V", "getFileUris", "()Ljava/util/List;", "getOnProgress", "()Lkotlin/jvm/functions/Function1;", "component1", "component2", "copy", "equals", "", "other", "hashCode", "", "toString", "", "app_debug"})
    public static final class Params {
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<android.net.Uri> fileUris = null;
        @org.jetbrains.annotations.Nullable()
        private final kotlin.jvm.functions.Function1<com.easycomic.domain.model.ImportProgress, kotlin.Unit> onProgress = null;
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<android.net.Uri> component1() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final kotlin.jvm.functions.Function1<com.easycomic.domain.model.ImportProgress, kotlin.Unit> component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.easycomic.domain.usecase.manga.BatchImportComicsUseCase.Params copy(@org.jetbrains.annotations.NotNull()
        java.util.List<? extends android.net.Uri> fileUris, @org.jetbrains.annotations.Nullable()
        kotlin.jvm.functions.Function1<? super com.easycomic.domain.model.ImportProgress, kotlin.Unit> onProgress) {
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
        
        public Params(@org.jetbrains.annotations.NotNull()
        java.util.List<? extends android.net.Uri> fileUris, @org.jetbrains.annotations.Nullable()
        kotlin.jvm.functions.Function1<? super com.easycomic.domain.model.ImportProgress, kotlin.Unit> onProgress) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<android.net.Uri> getFileUris() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final kotlin.jvm.functions.Function1<com.easycomic.domain.model.ImportProgress, kotlin.Unit> getOnProgress() {
            return null;
        }
    }
}