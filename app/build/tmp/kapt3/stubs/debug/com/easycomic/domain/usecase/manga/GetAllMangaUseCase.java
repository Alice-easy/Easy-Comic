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
 * 获取所有漫画用例
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0007\u0018\u00002\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00040\u00030\u00020\u0001B\r\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\u0007J\u001a\u0010\b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00040\u00030\u0002H\u0096B\u00a2\u0006\u0002\u0010\tR\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcom/easycomic/domain/usecase/manga/GetAllMangaUseCase;", "Lcom/easycomic/domain/usecase/NoParametersUseCase;", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/easycomic/domain/model/Manga;", "mangaRepository", "Lcom/easycomic/domain/repository/MangaRepository;", "(Lcom/easycomic/domain/repository/MangaRepository;)V", "invoke", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class GetAllMangaUseCase implements com.easycomic.domain.usecase.NoParametersUseCase<kotlinx.coroutines.flow.Flow<? extends java.util.List<? extends com.easycomic.domain.model.Manga>>> {
    @org.jetbrains.annotations.NotNull()
    private final com.easycomic.domain.repository.MangaRepository mangaRepository = null;
    
    public GetAllMangaUseCase(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.repository.MangaRepository mangaRepository) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object invoke(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlinx.coroutines.flow.Flow<? extends java.util.List<com.easycomic.domain.model.Manga>>> $completion) {
        return null;
    }
}