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
 * 排序选项
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\n\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\bj\u0002\b\tj\u0002\b\n\u00a8\u0006\u000b"}, d2 = {"Lcom/easycomic/ui/bookshelf/SortOption;", "", "(Ljava/lang/String;I)V", "TITLE_ASC", "TITLE_DESC", "DATE_ADDED_ASC", "DATE_ADDED_DESC", "LAST_READ_ASC", "LAST_READ_DESC", "RATING_ASC", "RATING_DESC", "app_debug"})
public enum SortOption {
    /*public static final*/ TITLE_ASC /* = new TITLE_ASC() */,
    /*public static final*/ TITLE_DESC /* = new TITLE_DESC() */,
    /*public static final*/ DATE_ADDED_ASC /* = new DATE_ADDED_ASC() */,
    /*public static final*/ DATE_ADDED_DESC /* = new DATE_ADDED_DESC() */,
    /*public static final*/ LAST_READ_ASC /* = new LAST_READ_ASC() */,
    /*public static final*/ LAST_READ_DESC /* = new LAST_READ_DESC() */,
    /*public static final*/ RATING_ASC /* = new RATING_ASC() */,
    /*public static final*/ RATING_DESC /* = new RATING_DESC() */;
    
    SortOption() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.easycomic.ui.bookshelf.SortOption> getEntries() {
        return null;
    }
}