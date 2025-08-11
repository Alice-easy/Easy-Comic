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
 * 更新阅读进度用例
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\b\u0007\u0018\u00002\u000e\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u00030\u0001:\u0001\nB\r\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0007\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\u0002H\u0096B\u00a2\u0006\u0002\u0010\tR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000b"}, d2 = {"Lcom/easycomic/domain/usecase/manga/UpdateReadingProgressUseCase;", "Lcom/easycomic/domain/usecase/BaseUseCase;", "Lcom/easycomic/domain/usecase/manga/UpdateReadingProgressUseCase$Params;", "", "mangaRepository", "Lcom/easycomic/domain/repository/MangaRepository;", "(Lcom/easycomic/domain/repository/MangaRepository;)V", "invoke", "parameters", "(Lcom/easycomic/domain/usecase/manga/UpdateReadingProgressUseCase$Params;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Params", "app_debug"})
public final class UpdateReadingProgressUseCase implements com.easycomic.domain.usecase.BaseUseCase<com.easycomic.domain.usecase.manga.UpdateReadingProgressUseCase.Params, kotlin.Unit> {
    @org.jetbrains.annotations.NotNull()
    private final com.easycomic.domain.repository.MangaRepository mangaRepository = null;
    
    public UpdateReadingProgressUseCase(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.repository.MangaRepository mangaRepository) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object invoke(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.usecase.manga.UpdateReadingProgressUseCase.Params parameters, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\f\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001B\u001d\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\t\u0010\u000f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0010\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0011\u001a\u00020\u0007H\u00c6\u0003J\'\u0010\u0012\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u0007H\u00c6\u0001J\u0013\u0010\u0013\u001a\u00020\u00142\b\u0010\u0015\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0016\u001a\u00020\u0005H\u00d6\u0001J\t\u0010\u0017\u001a\u00020\u0018H\u00d6\u0001R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000e\u00a8\u0006\u0019"}, d2 = {"Lcom/easycomic/domain/usecase/manga/UpdateReadingProgressUseCase$Params;", "", "mangaId", "", "currentPage", "", "status", "Lcom/easycomic/domain/model/ReadingStatus;", "(JILcom/easycomic/domain/model/ReadingStatus;)V", "getCurrentPage", "()I", "getMangaId", "()J", "getStatus", "()Lcom/easycomic/domain/model/ReadingStatus;", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "toString", "", "app_debug"})
    public static final class Params {
        private final long mangaId = 0L;
        private final int currentPage = 0;
        @org.jetbrains.annotations.NotNull()
        private final com.easycomic.domain.model.ReadingStatus status = null;
        
        public final long component1() {
            return 0L;
        }
        
        public final int component2() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.easycomic.domain.model.ReadingStatus component3() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.easycomic.domain.usecase.manga.UpdateReadingProgressUseCase.Params copy(long mangaId, int currentPage, @org.jetbrains.annotations.NotNull()
        com.easycomic.domain.model.ReadingStatus status) {
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
        
        public Params(long mangaId, int currentPage, @org.jetbrains.annotations.NotNull()
        com.easycomic.domain.model.ReadingStatus status) {
            super();
        }
        
        public final long getMangaId() {
            return 0L;
        }
        
        public final int getCurrentPage() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.easycomic.domain.model.ReadingStatus getStatus() {
            return null;
        }
    }
}