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

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\f\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\u001a\f\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u0002\u00a8\u0006\u0003"}, d2 = {"toDomainImportStatus", "Lcom/easycomic/domain/model/ImportStatus;", "Lcom/easycomic/data/service/ImportStatus;", "app_debug"})
public final class ImportComicUseCaseKt {
    
    /**
     * 导入漫画结果扩展函数
     */
    private static final com.easycomic.domain.model.ImportStatus toDomainImportStatus(com.easycomic.data.service.ImportStatus $this$toDomainImportStatus) {
        return null;
    }
}