package com.easycomic.domain.usecase.manga;

import android.net.Uri;
import com.easycomic.data.service.ComicImportService;
import com.easycomic.data.service.ImportResult;
import com.easycomic.data.service.ImportStatus;
import com.easycomic.data.service.BatchImportResult;
import com.easycomic.data.service.BatchImportStatus;
import com.easycomic.domain.model.ImportComicResult;
import com.easycomic.domain.model.ImportProgress;
import com.easycomic.domain.usecase.BaseUseCase;
import com.easycomic.domain.usecase.NoParametersUseCase;
import kotlinx.coroutines.flow.*;
import timber.log.Timber;
import javax.inject.Inject;

/**
 * 监听导入进度用例
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0007\u0018\u00002\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00030\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00030\u0002H\u0096B\u00a2\u0006\u0002\u0010\bR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Lcom/easycomic/domain/usecase/manga/MonitorImportProgressUseCase;", "Lcom/easycomic/domain/usecase/NoParametersUseCase;", "Lkotlinx/coroutines/flow/Flow;", "Lcom/easycomic/domain/model/ImportProgress;", "importProgressHolder", "Lcom/easycomic/domain/usecase/manga/ImportProgressHolder;", "(Lcom/easycomic/domain/usecase/manga/ImportProgressHolder;)V", "invoke", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class MonitorImportProgressUseCase implements com.easycomic.domain.usecase.NoParametersUseCase<kotlinx.coroutines.flow.Flow<? extends com.easycomic.domain.model.ImportProgress>> {
    @org.jetbrains.annotations.NotNull()
    private final com.easycomic.domain.usecase.manga.ImportProgressHolder importProgressHolder = null;
    
    @javax.inject.Inject()
    public MonitorImportProgressUseCase(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.usecase.manga.ImportProgressHolder importProgressHolder) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object invoke(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlinx.coroutines.flow.Flow<com.easycomic.domain.model.ImportProgress>> $completion) {
        return null;
    }
}