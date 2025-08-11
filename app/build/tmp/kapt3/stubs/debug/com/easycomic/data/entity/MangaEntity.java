package com.easycomic.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Index;
import androidx.room.ColumnInfo;

/**
 * 漫画实体类
 * @Entity 定义数据库表结构
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b6\b\u0087\b\u0018\u00002\u00020\u0001B\u00c5\u0001\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0005\u0012\u0006\u0010\b\u001a\u00020\u0005\u0012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\n\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u000b\u001a\u00020\u0003\u0012\b\b\u0002\u0010\f\u001a\u00020\r\u0012\b\b\u0002\u0010\u000e\u001a\u00020\r\u0012\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\u0011\u001a\u00020\u0012\u0012\b\b\u0002\u0010\u0013\u001a\u00020\u0014\u0012\b\b\u0002\u0010\u0015\u001a\u00020\u0016\u0012\b\b\u0002\u0010\u0017\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0018\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0019\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u001a\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u001bJ\t\u00104\u001a\u00020\u0003H\u00c6\u0003J\t\u00105\u001a\u00020\rH\u00c6\u0003J\u000b\u00106\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u00107\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u00108\u001a\u00020\u0012H\u00c6\u0003J\t\u00109\u001a\u00020\u0014H\u00c6\u0003J\t\u0010:\u001a\u00020\u0016H\u00c6\u0003J\t\u0010;\u001a\u00020\u0005H\u00c6\u0003J\t\u0010<\u001a\u00020\u0003H\u00c6\u0003J\t\u0010=\u001a\u00020\u0003H\u00c6\u0003J\t\u0010>\u001a\u00020\u0003H\u00c6\u0003J\t\u0010?\u001a\u00020\u0005H\u00c6\u0003J\t\u0010@\u001a\u00020\u0005H\u00c6\u0003J\t\u0010A\u001a\u00020\u0005H\u00c6\u0003J\t\u0010B\u001a\u00020\u0005H\u00c6\u0003J\u000b\u0010C\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010D\u001a\u00020\u0005H\u00c6\u0003J\t\u0010E\u001a\u00020\u0003H\u00c6\u0003J\t\u0010F\u001a\u00020\rH\u00c6\u0003J\u00cd\u0001\u0010G\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\u00052\b\b\u0002\u0010\b\u001a\u00020\u00052\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\n\u001a\u00020\u00052\b\b\u0002\u0010\u000b\u001a\u00020\u00032\b\b\u0002\u0010\f\u001a\u00020\r2\b\b\u0002\u0010\u000e\u001a\u00020\r2\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\u0011\u001a\u00020\u00122\b\b\u0002\u0010\u0013\u001a\u00020\u00142\b\b\u0002\u0010\u0015\u001a\u00020\u00162\b\b\u0002\u0010\u0017\u001a\u00020\u00052\b\b\u0002\u0010\u0018\u001a\u00020\u00032\b\b\u0002\u0010\u0019\u001a\u00020\u00032\b\b\u0002\u0010\u001a\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010H\u001a\u00020\u00142\b\u0010I\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010J\u001a\u00020\rH\u00d6\u0001J\t\u0010K\u001a\u00020\u0005H\u00d6\u0001R\u0016\u0010\u0006\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001dR\u0018\u0010\u000f\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001dR\u0016\u0010\u000e\u001a\u00020\r8\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010 R\u0016\u0010\u0019\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\"R\u0016\u0010\u001a\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010\"R\u0016\u0010\u0007\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010\u001dR\u0016\u0010\n\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u001dR\u0016\u0010\b\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\u001dR\u0016\u0010\u000b\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010\"R\u0018\u0010\t\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010\u001dR\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010\"R\u0016\u0010\u0013\u001a\u00020\u00148\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010*R\u0016\u0010\u0018\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010\"R\u0016\u0010\f\u001a\u00020\r8\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010 R\u0016\u0010\u0011\u001a\u00020\u00128\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010.R\u0016\u0010\u0015\u001a\u00020\u00168\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u00100R\u0016\u0010\u0017\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b1\u0010\u001dR\u0018\u0010\u0010\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u0010\u001dR\u0016\u0010\u0004\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b3\u0010\u001d\u00a8\u0006L"}, d2 = {"Lcom/easycomic/data/entity/MangaEntity;", "", "id", "", "title", "", "author", "description", "filePath", "fileUri", "fileFormat", "fileSize", "pageCount", "", "currentPage", "coverImagePath", "thumbnailPath", "rating", "", "isFavorite", "", "readingStatus", "Lcom/easycomic/data/entity/ReadingStatus;", "tags", "lastRead", "dateAdded", "dateModified", "(JLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;JIILjava/lang/String;Ljava/lang/String;FZLcom/easycomic/data/entity/ReadingStatus;Ljava/lang/String;JJJ)V", "getAuthor", "()Ljava/lang/String;", "getCoverImagePath", "getCurrentPage", "()I", "getDateAdded", "()J", "getDateModified", "getDescription", "getFileFormat", "getFilePath", "getFileSize", "getFileUri", "getId", "()Z", "getLastRead", "getPageCount", "getRating", "()F", "getReadingStatus", "()Lcom/easycomic/data/entity/ReadingStatus;", "getTags", "getThumbnailPath", "getTitle", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "toString", "app_debug"})
@androidx.room.Entity(tableName = "manga", indices = {@androidx.room.Index(value = {"title"}), @androidx.room.Index(value = {"author"}), @androidx.room.Index(value = {"last_read"}), @androidx.room.Index(value = {"date_added"}), @androidx.room.Index(value = {"is_favorite"})})
public final class MangaEntity {
    @androidx.room.PrimaryKey(autoGenerate = true)
    @androidx.room.ColumnInfo(name = "id")
    private final long id = 0L;
    @androidx.room.ColumnInfo(name = "title")
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String title = null;
    @androidx.room.ColumnInfo(name = "author")
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String author = null;
    @androidx.room.ColumnInfo(name = "description")
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String description = null;
    @androidx.room.ColumnInfo(name = "file_path")
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String filePath = null;
    @androidx.room.ColumnInfo(name = "file_uri")
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String fileUri = null;
    @androidx.room.ColumnInfo(name = "file_format")
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String fileFormat = null;
    @androidx.room.ColumnInfo(name = "file_size")
    private final long fileSize = 0L;
    @androidx.room.ColumnInfo(name = "page_count")
    private final int pageCount = 0;
    @androidx.room.ColumnInfo(name = "current_page")
    private final int currentPage = 0;
    @androidx.room.ColumnInfo(name = "cover_image_path")
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String coverImagePath = null;
    @androidx.room.ColumnInfo(name = "thumbnail_path")
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String thumbnailPath = null;
    @androidx.room.ColumnInfo(name = "rating")
    private final float rating = 0.0F;
    @androidx.room.ColumnInfo(name = "is_favorite")
    private final boolean isFavorite = false;
    @androidx.room.ColumnInfo(name = "reading_status")
    @org.jetbrains.annotations.NotNull()
    private final com.easycomic.data.entity.ReadingStatus readingStatus = null;
    @androidx.room.ColumnInfo(name = "tags")
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String tags = null;
    @androidx.room.ColumnInfo(name = "last_read")
    private final long lastRead = 0L;
    @androidx.room.ColumnInfo(name = "date_added")
    private final long dateAdded = 0L;
    @androidx.room.ColumnInfo(name = "date_modified")
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
    public final com.easycomic.data.entity.ReadingStatus component15() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component16() {
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
    public final com.easycomic.data.entity.MangaEntity copy(long id, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String author, @org.jetbrains.annotations.NotNull()
    java.lang.String description, @org.jetbrains.annotations.NotNull()
    java.lang.String filePath, @org.jetbrains.annotations.Nullable()
    java.lang.String fileUri, @org.jetbrains.annotations.NotNull()
    java.lang.String fileFormat, long fileSize, int pageCount, int currentPage, @org.jetbrains.annotations.Nullable()
    java.lang.String coverImagePath, @org.jetbrains.annotations.Nullable()
    java.lang.String thumbnailPath, float rating, boolean isFavorite, @org.jetbrains.annotations.NotNull()
    com.easycomic.data.entity.ReadingStatus readingStatus, @org.jetbrains.annotations.NotNull()
    java.lang.String tags, long lastRead, long dateAdded, long dateModified) {
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
    
    public MangaEntity(long id, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String author, @org.jetbrains.annotations.NotNull()
    java.lang.String description, @org.jetbrains.annotations.NotNull()
    java.lang.String filePath, @org.jetbrains.annotations.Nullable()
    java.lang.String fileUri, @org.jetbrains.annotations.NotNull()
    java.lang.String fileFormat, long fileSize, int pageCount, int currentPage, @org.jetbrains.annotations.Nullable()
    java.lang.String coverImagePath, @org.jetbrains.annotations.Nullable()
    java.lang.String thumbnailPath, float rating, boolean isFavorite, @org.jetbrains.annotations.NotNull()
    com.easycomic.data.entity.ReadingStatus readingStatus, @org.jetbrains.annotations.NotNull()
    java.lang.String tags, long lastRead, long dateAdded, long dateModified) {
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
    public final com.easycomic.data.entity.ReadingStatus getReadingStatus() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getTags() {
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
}