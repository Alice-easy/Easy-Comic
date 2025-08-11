package com.easycomic.data.service;

import android.content.Context;
import android.net.Uri;
import com.easycomic.data.model.ComicInfo;
import com.easycomic.data.model.ImageData;
import com.easycomic.data.parser.ComicParser;
import com.easycomic.data.repository.MangaRepositoryImpl;
import com.easycomic.domain.model.Manga;
import com.easycomic.domain.model.ReadingStatus;
import com.easycomic.data.util.FileHelper;
import dagger.hilt.android.qualifiers.ApplicationContext;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.Flow;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FilenameUtils;
import timber.log.Timber;
import java.io.*;
import java.util.zip.ZipEntry;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 批量导入状态
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0006\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/easycomic/data/service/BatchImportStatus;", "", "(Ljava/lang/String;I)V", "PROCESSING", "ITEM_COMPLETED", "COMPLETED", "FAILED", "app_debug"})
public enum BatchImportStatus {
    /*public static final*/ PROCESSING /* = new PROCESSING() */,
    /*public static final*/ ITEM_COMPLETED /* = new ITEM_COMPLETED() */,
    /*public static final*/ COMPLETED /* = new COMPLETED() */,
    /*public static final*/ FAILED /* = new FAILED() */;
    
    BatchImportStatus() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.easycomic.data.service.BatchImportStatus> getEntries() {
        return null;
    }
}