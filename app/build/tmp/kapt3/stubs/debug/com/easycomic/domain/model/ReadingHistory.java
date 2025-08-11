package com.easycomic.domain.model;

/**
 * 阅读历史领域模型
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\t\n\u0002\u0010\u000b\n\u0002\b\u001d\b\u0087\b\u0018\u00002\u00020\u0001BG\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u0012\b\b\u0002\u0010\t\u001a\u00020\u0003\u0012\b\b\u0002\u0010\n\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u000b\u001a\u00020\f\u00a2\u0006\u0002\u0010\rJ\t\u0010!\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\"\u001a\u00020\u0003H\u00c6\u0003J\t\u0010#\u001a\u00020\u0006H\u00c6\u0003J\t\u0010$\u001a\u00020\bH\u00c6\u0003J\t\u0010%\u001a\u00020\u0003H\u00c6\u0003J\t\u0010&\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\'\u001a\u00020\fH\u00c6\u0003JO\u0010(\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\u00032\b\b\u0002\u0010\n\u001a\u00020\u00032\b\b\u0002\u0010\u000b\u001a\u00020\fH\u00c6\u0001J\u0013\u0010)\u001a\u00020\u00162\b\u0010*\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\u0010\u0010+\u001a\u00020\f2\u0006\u0010,\u001a\u00020\u0003H\u0002J\u0010\u0010-\u001a\u00020\f2\u0006\u0010.\u001a\u00020\u0003H\u0002J\t\u0010/\u001a\u00020\u0006H\u00d6\u0001J\u0010\u00100\u001a\u00020\u00162\u0006\u0010.\u001a\u00020\u0003H\u0002J\u0010\u00101\u001a\u00020\u00162\u0006\u0010.\u001a\u00020\u0003H\u0002J\t\u00102\u001a\u00020\fH\u00d6\u0001R\u0011\u0010\u000e\u001a\u00020\f8F\u00a2\u0006\u0006\u001a\u0004\b\u000f\u0010\u0010R\u0011\u0010\u0011\u001a\u00020\f8F\u00a2\u0006\u0006\u001a\u0004\b\u0012\u0010\u0010R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0011\u0010\u0015\u001a\u00020\u00168F\u00a2\u0006\u0006\u001a\u0004\b\u0015\u0010\u0017R\u0011\u0010\u0018\u001a\u00020\u00168F\u00a2\u0006\u0006\u001a\u0004\b\u0018\u0010\u0017R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0014R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001bR\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001dR\u0011\u0010\n\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u0014R\u0011\u0010\t\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u0014R\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u0010\u00a8\u00063"}, d2 = {"Lcom/easycomic/domain/model/ReadingHistory;", "", "id", "", "mangaId", "pageNumber", "", "progressPercentage", "", "readingDuration", "readAt", "sessionId", "", "(JJIFJJLjava/lang/String;)V", "formattedReadAt", "getFormattedReadAt", "()Ljava/lang/String;", "formattedReadingDuration", "getFormattedReadingDuration", "getId", "()J", "isThisWeek", "", "()Z", "isToday", "getMangaId", "getPageNumber", "()I", "getProgressPercentage", "()F", "getReadAt", "getReadingDuration", "getSessionId", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "equals", "other", "formatDuration", "duration", "formatTimestamp", "timestamp", "hashCode", "isDateThisWeek", "isDateToday", "toString", "app_debug"})
public final class ReadingHistory {
    private final long id = 0L;
    private final long mangaId = 0L;
    private final int pageNumber = 0;
    private final float progressPercentage = 0.0F;
    private final long readingDuration = 0L;
    private final long readAt = 0L;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String sessionId = null;
    
    public final long component1() {
        return 0L;
    }
    
    public final long component2() {
        return 0L;
    }
    
    public final int component3() {
        return 0;
    }
    
    public final float component4() {
        return 0.0F;
    }
    
    public final long component5() {
        return 0L;
    }
    
    public final long component6() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.domain.model.ReadingHistory copy(long id, long mangaId, int pageNumber, float progressPercentage, long readingDuration, long readAt, @org.jetbrains.annotations.NotNull()
    java.lang.String sessionId) {
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
    
    public ReadingHistory(long id, long mangaId, int pageNumber, float progressPercentage, long readingDuration, long readAt, @org.jetbrains.annotations.NotNull()
    java.lang.String sessionId) {
        super();
    }
    
    public final long getId() {
        return 0L;
    }
    
    public final long getMangaId() {
        return 0L;
    }
    
    public final int getPageNumber() {
        return 0;
    }
    
    public final float getProgressPercentage() {
        return 0.0F;
    }
    
    public final long getReadingDuration() {
        return 0L;
    }
    
    public final long getReadAt() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getSessionId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFormattedReadingDuration() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFormattedReadAt() {
        return null;
    }
    
    public final boolean isToday() {
        return false;
    }
    
    public final boolean isThisWeek() {
        return false;
    }
    
    /**
     * 格式化时长
     */
    private final java.lang.String formatDuration(long duration) {
        return null;
    }
    
    /**
     * 格式化时间戳
     */
    private final java.lang.String formatTimestamp(long timestamp) {
        return null;
    }
    
    /**
     * 判断是否为今天
     */
    private final boolean isDateToday(long timestamp) {
        return false;
    }
    
    /**
     * 判断是否为本周
     */
    private final boolean isDateThisWeek(long timestamp) {
        return false;
    }
}