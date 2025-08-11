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
 * 导入进度持有者
 * 用于在应用范围内共享导入进度状态
 */
@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\n\u001a\u00020\u000bJ\u000e\u0010\f\u001a\u00020\u000b2\u0006\u0010\r\u001a\u00020\u0005R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00050\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\t\u00a8\u0006\u000e"}, d2 = {"Lcom/easycomic/domain/usecase/manga/ImportProgressHolder;", "", "()V", "_progressFlow", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/easycomic/domain/model/ImportProgress;", "progressFlow", "Lkotlinx/coroutines/flow/StateFlow;", "getProgressFlow", "()Lkotlinx/coroutines/flow/StateFlow;", "resetProgress", "", "updateProgress", "progress", "app_debug"})
public final class ImportProgressHolder {
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.easycomic.domain.model.ImportProgress> _progressFlow = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.easycomic.domain.model.ImportProgress> progressFlow = null;
    
    @javax.inject.Inject()
    public ImportProgressHolder() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.easycomic.domain.model.ImportProgress> getProgressFlow() {
        return null;
    }
    
    public final void updateProgress(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.model.ImportProgress progress) {
    }
    
    public final void resetProgress() {
    }
}