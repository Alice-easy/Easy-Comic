package com.easycomic.domain.model;

/**
 * 漫画领域模型
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b?\b\u0087\b\u0018\u00002\u00020\u0001B\u00cb\u0001\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0005\u0012\u0006\u0010\b\u001a\u00020\u0005\u0012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\n\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u000b\u001a\u00020\u0003\u0012\b\b\u0002\u0010\f\u001a\u00020\r\u0012\b\b\u0002\u0010\u000e\u001a\u00020\r\u0012\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\u0011\u001a\u00020\u0012\u0012\b\b\u0002\u0010\u0013\u001a\u00020\u0014\u0012\b\b\u0002\u0010\u0015\u001a\u00020\u0016\u0012\u000e\b\u0002\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00050\u0018\u0012\b\b\u0002\u0010\u0019\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u001a\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u001b\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u001cJ\t\u0010=\u001a\u00020\u0003H\u00c6\u0003J\t\u0010>\u001a\u00020\rH\u00c6\u0003J\u000b\u0010?\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010@\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010A\u001a\u00020\u0012H\u00c6\u0003J\t\u0010B\u001a\u00020\u0014H\u00c6\u0003J\t\u0010C\u001a\u00020\u0016H\u00c6\u0003J\u000f\u0010D\u001a\b\u0012\u0004\u0012\u00020\u00050\u0018H\u00c6\u0003J\t\u0010E\u001a\u00020\u0003H\u00c6\u0003J\t\u0010F\u001a\u00020\u0003H\u00c6\u0003J\t\u0010G\u001a\u00020\u0003H\u00c6\u0003J\t\u0010H\u001a\u00020\u0005H\u00c6\u0003J\t\u0010I\u001a\u00020\u0005H\u00c6\u0003J\t\u0010J\u001a\u00020\u0005H\u00c6\u0003J\t\u0010K\u001a\u00020\u0005H\u00c6\u0003J\u000b\u0010L\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010M\u001a\u00020\u0005H\u00c6\u0003J\t\u0010N\u001a\u00020\u0003H\u00c6\u0003J\t\u0010O\u001a\u00020\rH\u00c6\u0003J\u00d3\u0001\u0010P\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\u00052\b\b\u0002\u0010\b\u001a\u00020\u00052\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\n\u001a\u00020\u00052\b\b\u0002\u0010\u000b\u001a\u00020\u00032\b\b\u0002\u0010\f\u001a\u00020\r2\b\b\u0002\u0010\u000e\u001a\u00020\r2\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\u0011\u001a\u00020\u00122\b\b\u0002\u0010\u0013\u001a\u00020\u00142\b\b\u0002\u0010\u0015\u001a\u00020\u00162\u000e\b\u0002\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00050\u00182\b\b\u0002\u0010\u0019\u001a\u00020\u00032\b\b\u0002\u0010\u001a\u001a\u00020\u00032\b\b\u0002\u0010\u001b\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010Q\u001a\u00020\u00142\b\u0010R\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\u0010\u0010S\u001a\u00020\u00052\u0006\u0010T\u001a\u00020\u0003H\u0002J\t\u0010U\u001a\u00020\rH\u00d6\u0001J\t\u0010V\u001a\u00020\u0005H\u00d6\u0001R\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001eR\u0013\u0010\u000f\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u001eR\u0011\u0010\u000e\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010!R\u0011\u0010\u001a\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010#R\u0011\u0010\u001b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010#R\u0011\u0010\u0007\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u001eR\u0011\u0010\n\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\u001eR\u0011\u0010\b\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010\u001eR\u0011\u0010\u000b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010#R\u0013\u0010\t\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010\u001eR\u0011\u0010*\u001a\u00020\u00058F\u00a2\u0006\u0006\u001a\u0004\b+\u0010\u001eR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010#R\u0011\u0010-\u001a\u00020\u00148F\u00a2\u0006\u0006\u001a\u0004\b-\u0010.R\u0011\u0010\u0013\u001a\u00020\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010.R\u0011\u0010\u0019\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u0010#R\u0011\u0010\f\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b0\u0010!R\u0011\u00101\u001a\u00020\u00128F\u00a2\u0006\u0006\u001a\u0004\b2\u00103R\u0011\u0010\u0011\u001a\u00020\u0012\u00a2\u0006\b\n\u0000\u001a\u0004\b4\u00103R\u0011\u0010\u0015\u001a\u00020\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b5\u00106R\u0017\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00050\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b7\u00108R\u0011\u00109\u001a\u00020\u00058F\u00a2\u0006\u0006\u001a\u0004\b:\u0010\u001eR\u0013\u0010\u0010\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b;\u0010\u001eR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b<\u0010\u001e\u00a8\u0006W"}, d2 = {"Lcom/easycomic/domain/model/Manga;", "", "id", "", "title", "", "author", "description", "filePath", "fileUri", "fileFormat", "fileSize", "pageCount", "", "currentPage", "coverImagePath", "thumbnailPath", "rating", "", "isFavorite", "", "readingStatus", "Lcom/easycomic/domain/model/ReadingStatus;", "tags", "", "lastRead", "dateAdded", "dateModified", "(JLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;JIILjava/lang/String;Ljava/lang/String;FZLcom/easycomic/domain/model/ReadingStatus;Ljava/util/List;JJJ)V", "getAuthor", "()Ljava/lang/String;", "getCoverImagePath", "getCurrentPage", "()I", "getDateAdded", "()J", "getDateModified", "getDescription", "getFileFormat", "getFilePath", "getFileSize", "getFileUri", "formattedFileSize", "getFormattedFileSize", "getId", "isCompleted", "()Z", "getLastRead", "getPageCount", "progressPercentage", "getProgressPercentage", "()F", "getRating", "getReadingStatus", "()Lcom/easycomic/domain/model/ReadingStatus;", "getTags", "()Ljava/util/List;", "tagsString", "getTagsString", "getThumbnailPath", "getTitle", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "formatFileSize", "size", "hashCode", "toString", "app_debug"})
public final class Manga {
    private final long id = 0L;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String title = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String author = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String description = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String filePath = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String fileUri = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String fileFormat = null;
    private final long fileSize = 0L;
    private final int pageCount = 0;
    private final int currentPage = 0;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String coverImagePath = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String thumbnailPath = null;
    private final float rating = 0.0F;
    private final boolean isFavorite = false;
    @org.jetbrains.annotations.NotNull()
    private final com.easycomic.domain.model.ReadingStatus readingStatus = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.String> tags = null;
    private final long lastRead = 0L;
    private final long dateAdded = 0L;
    private final long dateModified = 0L;
    
    public final long component1() {
        return 0L;
    }
    
    public final int component10() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component11() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component12() {
        return null;
    }
    
    public final float component13() {
        return 0.0F;
    }
    
    public final boolean component14() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.domain.model.ReadingStatus component15() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> component16() {
        return null;
    }
    
    public final long component17() {
        return 0L;
    }
    
    public final long component18() {
        return 0L;
    }
    
    public final long component19() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component7() {
        return null;
    }
    
    public final long component8() {
        return 0L;
    }
    
    public final int component9() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.domain.model.Manga copy(long id, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String author, @org.jetbrains.annotations.NotNull()
    java.lang.String description, @org.jetbrains.annotations.NotNull()
    java.lang.String filePath, @org.jetbrains.annotations.Nullable()
    java.lang.String fileUri, @org.jetbrains.annotations.NotNull()
    java.lang.String fileFormat, long fileSize, int pageCount, int currentPage, @org.jetbrains.annotations.Nullable()
    java.lang.String coverImagePath, @org.jetbrains.annotations.Nullable()
    java.lang.String thumbnailPath, float rating, boolean isFavorite, @org.jetbrains.annotations.NotNull()
    com.easycomic.domain.model.ReadingStatus readingStatus, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> tags, long lastRead, long dateAdded, long dateModified) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
    
    public Manga(long id, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String author, @org.jetbrains.annotations.NotNull()
    java.lang.String description, @org.jetbrains.annotations.NotNull()
    java.lang.String filePath, @org.jetbrains.annotations.Nullable()
    java.lang.String fileUri, @org.jetbrains.annotations.NotNull()
    java.lang.String fileFormat, long fileSize, int pageCount, int currentPage, @org.jetbrains.annotations.Nullable()
    java.lang.String coverImagePath, @org.jetbrains.annotations.Nullable()
    java.lang.String thumbnailPath, float rating, boolean isFavorite, @org.jetbrains.annotations.NotNull()
    com.easycomic.domain.model.ReadingStatus readingStatus, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> tags, long lastRead, long dateAdded, long dateModified) {
        super();
    }
    
    public final long getId() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getTitle() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getAuthor() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDescription() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFilePath() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getFileUri() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFileFormat() {
        return null;
    }
    
    public final long getFileSize() {
        return 0L;
    }
    
    public final int getPageCount() {
        return 0;
    }
    
    public final int getCurrentPage() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getCoverImagePath() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getThumbnailPath() {
        return null;
    }
    
    public final float getRating() {
        return 0.0F;
    }
    
    public final boolean isFavorite() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.domain.model.ReadingStatus getReadingStatus() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> getTags() {
        return null;
    }
    
    public final long getLastRead() {
        return 0L;
    }
    
    public final long getDateAdded() {
        return 0L;
    }
    
    public final long getDateModified() {
        return 0L;
    }
    
    public final float getProgressPercentage() {
        return 0.0F;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFormattedFileSize() {
        return null;
    }
    
    public final boolean isCompleted() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getTagsString() {
        return null;
    }
    
    /**
     * 格式化文件大小
     */
    private final java.lang.String formatFileSize(long size) {
        return null;
    }
}