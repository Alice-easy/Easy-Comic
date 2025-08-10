package com.easycomic.model;

/**
 * 支持的文件类型
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\"\n\u0002\u0010\u000e\n\u0002\b\b\n\u0002\u0010\u000b\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0005J\u000e\u0010\u0010\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0005R\u0017\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\u0007R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0011"}, d2 = {"Lcom/easycomic/model/ComicFileType;", "", "()V", "ALL_ARCHIVE_TYPES", "", "", "getALL_ARCHIVE_TYPES", "()Ljava/util/Set;", "ALL_SUPPORTED_TYPES", "getALL_SUPPORTED_TYPES", "SUPPORTED_IMAGES", "SUPPORTED_RAR", "SUPPORTED_ZIP", "isImageFile", "", "filePath", "isSupportedArchive", "app_debug"})
public final class ComicFileType {
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Set<java.lang.String> SUPPORTED_ZIP = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Set<java.lang.String> SUPPORTED_RAR = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Set<java.lang.String> SUPPORTED_IMAGES = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Set<java.lang.String> ALL_ARCHIVE_TYPES = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Set<java.lang.String> ALL_SUPPORTED_TYPES = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.easycomic.model.ComicFileType INSTANCE = null;
    
    private ComicFileType() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Set<java.lang.String> getALL_ARCHIVE_TYPES() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Set<java.lang.String> getALL_SUPPORTED_TYPES() {
        return null;
    }
    
    public final boolean isSupportedArchive(@org.jetbrains.annotations.NotNull()
    java.lang.String filePath) {
        return false;
    }
    
    public final boolean isImageFile(@org.jetbrains.annotations.NotNull()
    java.lang.String filePath) {
        return false;
    }
}