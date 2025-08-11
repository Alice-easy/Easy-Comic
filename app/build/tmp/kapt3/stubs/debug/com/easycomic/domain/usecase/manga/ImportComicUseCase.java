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
 * 导入单个漫画文件用例
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0007\u0018\u00002\u0014\u0012\u0004\u0012\u00020\u0002\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00040\u00030\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\u0007J\u001c\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u0006\u0010\t\u001a\u00020\u0002H\u0096B\u00a2\u0006\u0002\u0010\nR\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000b"}, d2 = {"Lcom/easycomic/domain/usecase/manga/ImportComicUseCase;", "Lcom/easycomic/domain/usecase/BaseUseCase;", "Landroid/net/Uri;", "Lkotlinx/coroutines/flow/Flow;", "Lcom/easycomic/domain/model/ImportComicResult;", "comicImportService", "Lcom/easycomic/data/service/ComicImportService;", "(Lcom/easycomic/data/service/ComicImportService;)V", "invoke", "parameters", "(Landroid/net/Uri;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class ImportComicUseCase implements com.easycomic.domain.usecase.BaseUseCase<android.net.Uri, kotlinx.coroutines.flow.Flow<? extends com.easycomic.domain.model.ImportComicResult>> {
    @org.jetbrains.annotations.NotNull()
    private final com.easycomic.data.service.ComicImportService comicImportService = null;
    
    @javax.inject.Inject()
    public ImportComicUseCase(@org.jetbrains.annotations.NotNull()
    com.easycomic.data.service.ComicImportService comicImportService) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object invoke(@org.jetbrains.annotations.NotNull()
    android.net.Uri parameters, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlinx.coroutines.flow.Flow<com.easycomic.domain.model.ImportComicResult>> $completion) {
        return null;
    }
}