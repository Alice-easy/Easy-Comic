package com.easycomic.data.database.entity;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\u000b\n\u0002\b:\b\u0087\b\u0018\u00002\u00020\u0001B\u00bd\u0001\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u0005\u0012\u0006\u0010\b\u001a\u00020\u0005\u0012\u0006\u0010\t\u001a\u00020\u0003\u0012\u0006\u0010\n\u001a\u00020\u0005\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\f\u001a\u00020\r\u0012\b\b\u0002\u0010\u000e\u001a\u00020\r\u0012\b\b\u0002\u0010\u000f\u001a\u00020\u0010\u0012\b\b\u0002\u0010\u0011\u001a\u00020\u0012\u0012\b\b\u0002\u0010\u0013\u001a\u00020\u0012\u0012\u0006\u0010\u0014\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\u0016\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0017\u001a\u00020\u0010\u0012\u0006\u0010\u0018\u001a\u00020\u0003\u0012\u0006\u0010\u0019\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u001aJ\t\u00103\u001a\u00020\u0003H\u00c6\u0003J\t\u00104\u001a\u00020\rH\u00c6\u0003J\t\u00105\u001a\u00020\u0010H\u00c6\u0003J\t\u00106\u001a\u00020\u0012H\u00c6\u0003J\t\u00107\u001a\u00020\u0012H\u00c6\u0003J\t\u00108\u001a\u00020\u0003H\u00c6\u0003J\u0010\u00109\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010*J\t\u0010:\u001a\u00020\u0003H\u00c6\u0003J\t\u0010;\u001a\u00020\u0010H\u00c6\u0003J\t\u0010<\u001a\u00020\u0003H\u00c6\u0003J\t\u0010=\u001a\u00020\u0003H\u00c6\u0003J\t\u0010>\u001a\u00020\u0005H\u00c6\u0003J\u000b\u0010?\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010@\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010A\u001a\u00020\u0005H\u00c6\u0003J\t\u0010B\u001a\u00020\u0003H\u00c6\u0003J\t\u0010C\u001a\u00020\u0005H\u00c6\u0003J\u000b\u0010D\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010E\u001a\u00020\rH\u00c6\u0003J\u00d4\u0001\u0010F\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\b\u001a\u00020\u00052\b\b\u0002\u0010\t\u001a\u00020\u00032\b\b\u0002\u0010\n\u001a\u00020\u00052\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\f\u001a\u00020\r2\b\b\u0002\u0010\u000e\u001a\u00020\r2\b\b\u0002\u0010\u000f\u001a\u00020\u00102\b\b\u0002\u0010\u0011\u001a\u00020\u00122\b\b\u0002\u0010\u0013\u001a\u00020\u00122\b\b\u0002\u0010\u0014\u001a\u00020\u00032\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0016\u001a\u00020\u00032\b\b\u0002\u0010\u0017\u001a\u00020\u00102\b\b\u0002\u0010\u0018\u001a\u00020\u00032\b\b\u0002\u0010\u0019\u001a\u00020\u0003H\u00c6\u0001\u00a2\u0006\u0002\u0010GJ\u0013\u0010H\u001a\u00020\u00122\b\u0010I\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010J\u001a\u00020\rH\u00d6\u0001J\t\u0010K\u001a\u00020\u0005H\u00d6\u0001R\u0013\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001cR\u0018\u0010\u000b\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001cR\u0016\u0010\u0018\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001fR\u0016\u0010\u000e\u001a\u00020\r8\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010!R\u0016\u0010\u0014\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010\u001fR\u0013\u0010\u0007\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010\u001cR\u0016\u0010\b\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010\u001cR\u0016\u0010\t\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u001fR\u0011\u0010\n\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\u001cR\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010\u001fR\u0016\u0010\u0013\u001a\u00020\u00128\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010(R\u0016\u0010\u0011\u001a\u00020\u00128\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010(R\u001a\u0010\u0015\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004\u00a2\u0006\n\n\u0002\u0010+\u001a\u0004\b)\u0010*R\u0016\u0010\f\u001a\u00020\r8\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010!R\u0011\u0010\u0017\u001a\u00020\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010.R\u0016\u0010\u000f\u001a\u00020\u00108\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u0010.R\u0016\u0010\u0016\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b0\u0010\u001fR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b1\u0010\u001cR\u0016\u0010\u0019\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u0010\u001f\u00a8\u0006L"}, d2 = {"Lcom/easycomic/data/database/entity/MangaEntity;", "", "id", "", "title", "", "author", "description", "filePath", "fileSize", "format", "coverPath", "pageCount", "", "currentPage", "readingProgress", "", "isFavorite", "", "isCompleted", "dateAdded", "lastRead", "readingTime", "rating", "createdAt", "updatedAt", "(JLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;JLjava/lang/String;Ljava/lang/String;IIFZZJLjava/lang/Long;JFJJ)V", "getAuthor", "()Ljava/lang/String;", "getCoverPath", "getCreatedAt", "()J", "getCurrentPage", "()I", "getDateAdded", "getDescription", "getFilePath", "getFileSize", "getFormat", "getId", "()Z", "getLastRead", "()Ljava/lang/Long;", "Ljava/lang/Long;", "getPageCount", "getRating", "()F", "getReadingProgress", "getReadingTime", "getTitle", "getUpdatedAt", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(JLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;JLjava/lang/String;Ljava/lang/String;IIFZZJLjava/lang/Long;JFJJ)Lcom/easycomic/data/database/entity/MangaEntity;", "equals", "other", "hashCode", "toString", "app_debug"})
@androidx.room.Entity(tableName = "manga", indices = {@androidx.room.Index(value = {"title"}, name = "idx_manga_title"), @androidx.room.Index(value = {"last_read"}, name = "idx_manga_last_read"), @androidx.room.Index(value = {"is_favorite", "last_read"}, name = "idx_manga_favorite")})
public final class MangaEntity {
    @androidx.room.PrimaryKey(autoGenerate = true)
    private final long id = 0L;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String title = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String author = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String description = null;
    @androidx.room.ColumnInfo(name = "file_path")
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String filePath = null;
    @androidx.room.ColumnInfo(name = "file_size")
    private final long fileSize = 0L;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String format = null;
    @androidx.room.ColumnInfo(name = "cover_path")
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String coverPath = null;
    @androidx.room.ColumnInfo(name = "page_count")
    private final int pageCount = 0;
    @androidx.room.ColumnInfo(name = "current_page")
    private final int currentPage = 0;
    @androidx.room.ColumnInfo(name = "reading_progress")
    private final float readingProgress = 0.0F;
    @androidx.room.ColumnInfo(name = "is_favorite")
    private final boolean isFavorite = false;
    @androidx.room.ColumnInfo(name = "is_completed")
    private final boolean isCompleted = false;
    @androidx.room.ColumnInfo(name = "date_added")
    private final long dateAdded = 0L;
    @androidx.room.ColumnInfo(name = "last_read")
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Long lastRead = null;
    @androidx.room.ColumnInfo(name = "reading_time")
    private final long readingTime = 0L;
    private final float rating = 0.0F;
    @androidx.room.ColumnInfo(name = "created_at")
    private final long createdAt = 0L;
    @androidx.room.ColumnInfo(name = "updated_at")
    private final long updatedAt = 0L;
    
    public final long component1() {
        return 0L;
    }
    
    public final int component10() {
        return 0;
    }
    
    public final float component11() {
        return 0.0F;
    }
    
    public final boolean component12() {
        return false;
    }
    
    public final boolean component13() {
        return false;
    }
    
    public final long component14() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Long component15() {
        return null;
    }
    
    public final long component16() {
        return 0L;
    }
    
    public final float component17() {
        return 0.0F;
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
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component5() {
        return null;
    }
    
    public final long component6() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component7() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component8() {
        return null;
    }
    
    public final int component9() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.data.database.entity.MangaEntity copy(long id, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.Nullable()
    java.lang.String author, @org.jetbrains.annotations.Nullable()
    java.lang.String description, @org.jetbrains.annotations.NotNull()
    java.lang.String filePath, long fileSize, @org.jetbrains.annotations.NotNull()
    java.lang.String format, @org.jetbrains.annotations.Nullable()
    java.lang.String coverPath, int pageCount, int currentPage, float readingProgress, boolean isFavorite, boolean isCompleted, long dateAdded, @org.jetbrains.annotations.Nullable()
    java.lang.Long lastRead, long readingTime, float rating, long createdAt, long updatedAt) {
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
    java.lang.String title, @org.jetbrains.annotations.Nullable()
    java.lang.String author, @org.jetbrains.annotations.Nullable()
    java.lang.String description, @org.jetbrains.annotations.NotNull()
    java.lang.String filePath, long fileSize, @org.jetbrains.annotations.NotNull()
    java.lang.String format, @org.jetbrains.annotations.Nullable()
    java.lang.String coverPath, int pageCount, int currentPage, float readingProgress, boolean isFavorite, boolean isCompleted, long dateAdded, @org.jetbrains.annotations.Nullable()
    java.lang.Long lastRead, long readingTime, float rating, long createdAt, long updatedAt) {
        super();
    }
    
    public final long getId() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getTitle() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getAuthor() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getDescription() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFilePath() {
        return null;
    }
    
    public final long getFileSize() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFormat() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getCoverPath() {
        return null;
    }
    
    public final int getPageCount() {
        return 0;
    }
    
    public final int getCurrentPage() {
        return 0;
    }
    
    public final float getReadingProgress() {
        return 0.0F;
    }
    
    public final boolean isFavorite() {
        return false;
    }
    
    public final boolean isCompleted() {
        return false;
    }
    
    public final long getDateAdded() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Long getLastRead() {
        return null;
    }
    
    public final long getReadingTime() {
        return 0L;
    }
    
    public final float getRating() {
        return 0.0F;
    }
    
    public final long getCreatedAt() {
        return 0L;
    }
    
    public final long getUpdatedAt() {
        return 0L;
    }
}