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
 * 筛选选项
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0007\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007\u00a8\u0006\b"}, d2 = {"Lcom/easycomic/ui/bookshelf/FilterOption;", "", "(Ljava/lang/String;I)V", "ALL", "FAVORITES", "READING", "COMPLETED", "UNREAD", "app_debug"})
public enum FilterOption {
    /*public static final*/ ALL /* = new ALL() */,
    /*public static final*/ FAVORITES /* = new FAVORITES() */,
    /*public static final*/ READING /* = new READING() */,
    /*public static final*/ COMPLETED /* = new COMPLETED() */,
    /*public static final*/ UNREAD /* = new UNREAD() */;
    
    FilterOption() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.easycomic.ui.bookshelf.FilterOption> getEntries() {
        return null;
    }
}