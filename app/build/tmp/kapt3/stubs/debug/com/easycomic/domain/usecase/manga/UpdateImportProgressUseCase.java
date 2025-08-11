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
 * 更新导入进度用例
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0007\u0018\u00002\u000e\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u00030\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0007\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\u0002H\u0096B\u00a2\u0006\u0002\u0010\tR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcom/easycomic/domain/usecase/manga/UpdateImportProgressUseCase;", "Lcom/easycomic/domain/usecase/BaseUseCase;", "Lcom/easycomic/domain/model/ImportProgress;", "", "importProgressHolder", "Lcom/easycomic/domain/usecase/manga/ImportProgressHolder;", "(Lcom/easycomic/domain/usecase/manga/ImportProgressHolder;)V", "invoke", "parameters", "(Lcom/easycomic/domain/model/ImportProgress;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class UpdateImportProgressUseCase implements com.easycomic.domain.usecase.BaseUseCase<com.easycomic.domain.model.ImportProgress, kotlin.Unit> {
    @org.jetbrains.annotations.NotNull()
    private final com.easycomic.domain.usecase.manga.ImportProgressHolder importProgressHolder = null;
    
    @javax.inject.Inject()
    public UpdateImportProgressUseCase(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.usecase.manga.ImportProgressHolder importProgressHolder) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object invoke(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.model.ImportProgress parameters, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}