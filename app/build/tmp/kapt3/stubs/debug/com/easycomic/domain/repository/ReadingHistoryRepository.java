package com.easycomic.domain.repository;

import com.easycomic.domain.model.ReadingHistory;
import kotlinx.coroutines.flow.Flow;

/**
 * 阅读历史仓库接口
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0007\bf\u0018\u00002\u00020\u0001:\u0001\"J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a6@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u0003H\u00a6@\u00a2\u0006\u0002\u0010\nJ\u001c\u0010\u000b\u001a\u00020\b2\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00050\rH\u00a6@\u00a2\u0006\u0002\u0010\u000eJ\u0016\u0010\u000f\u001a\u00020\b2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a6@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0010\u001a\u00020\b2\u0006\u0010\u0011\u001a\u00020\u0003H\u00a6@\u00a2\u0006\u0002\u0010\nJ\u0016\u0010\u0012\u001a\u00020\b2\u0006\u0010\u0013\u001a\u00020\u0003H\u00a6@\u00a2\u0006\u0002\u0010\nJ\u001c\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00160\r0\u00152\u0006\u0010\u0017\u001a\u00020\u0003H&J\u0018\u0010\u0018\u001a\u0004\u0018\u00010\u00052\u0006\u0010\u0013\u001a\u00020\u0003H\u00a6@\u00a2\u0006\u0002\u0010\nJ\u0018\u0010\u0019\u001a\u0004\u0018\u00010\u00032\u0006\u0010\u0013\u001a\u00020\u0003H\u00a6@\u00a2\u0006\u0002\u0010\nJ\u001c\u0010\u001a\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\r0\u00152\u0006\u0010\u0013\u001a\u00020\u0003H&J\u000e\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u001c0\u0015H&J\u0016\u0010\u001d\u001a\u00020\u001c2\u0006\u0010\u0013\u001a\u00020\u0003H\u00a6@\u00a2\u0006\u0002\u0010\nJ\u001e\u0010\u001e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\r0\u00152\b\b\u0002\u0010\u001f\u001a\u00020\u001cH&J\u0010\u0010 \u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00030\u0015H&J\u0016\u0010!\u001a\u00020\b2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a6@\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006#"}, d2 = {"Lcom/easycomic/domain/repository/ReadingHistoryRepository;", "", "addReadingHistory", "", "history", "Lcom/easycomic/domain/model/ReadingHistory;", "(Lcom/easycomic/domain/model/ReadingHistory;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "cleanupOldReadingHistory", "", "cutoffTime", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteAllReadingHistory", "historyList", "", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteReadingHistory", "deleteReadingHistoryById", "historyId", "deleteReadingHistoryByMangaId", "mangaId", "getDailyReadingStats", "Lkotlinx/coroutines/flow/Flow;", "Lcom/easycomic/domain/repository/ReadingHistoryRepository$DailyReadingStats;", "startTime", "getLatestReadingHistory", "getReadingDurationByMangaId", "getReadingHistoryByMangaId", "getReadingHistoryCount", "", "getReadingHistoryCountByMangaId", "getRecentReadingHistory", "limit", "getTotalReadingDuration", "updateReadingHistory", "DailyReadingStats", "app_debug"})
public abstract interface ReadingHistoryRepository {
    
    /**
     * 获取指定漫画的阅读历史
     */
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.easycomic.domain.model.ReadingHistory>> getReadingHistoryByMangaId(long mangaId);
    
    /**
     * 获取最近的阅读记录
     */
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.easycomic.domain.model.ReadingHistory>> getRecentReadingHistory(int limit);
    
    /**
     * 获取指定漫画的最新阅读记录
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getLatestReadingHistory(long mangaId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.easycomic.domain.model.ReadingHistory> $completion);
    
    /**
     * 添加阅读记录
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object addReadingHistory(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.model.ReadingHistory history, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    /**
     * 更新阅读记录
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateReadingHistory(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.model.ReadingHistory history, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 删除阅读记录
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteReadingHistory(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.model.ReadingHistory history, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 根据ID删除阅读记录
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteReadingHistoryById(long historyId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 删除指定漫画的所有阅读记录
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteReadingHistoryByMangaId(long mangaId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 批量删除阅读记录
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteAllReadingHistory(@org.jetbrains.annotations.NotNull()
    java.util.List<com.easycomic.domain.model.ReadingHistory> historyList, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 清理旧的阅读记录
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object cleanupOldReadingHistory(long cutoffTime, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 获取阅读记录总数
     */
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.lang.Integer> getReadingHistoryCount();
    
    /**
     * 获取指定漫画的阅读记录数量
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getReadingHistoryCountByMangaId(long mangaId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    /**
     * 获取总阅读时长
     */
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.lang.Long> getTotalReadingDuration();
    
    /**
     * 获取指定漫画的总阅读时长
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getReadingDurationByMangaId(long mangaId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    /**
     * 获取每日阅读统计
     */
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.easycomic.domain.repository.ReadingHistoryRepository.DailyReadingStats>> getDailyReadingStats(long startTime);
    
    /**
     * 每日阅读统计数据
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0002\b\f\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0087\b\u0018\u00002\u00020\u0001B\u001d\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\t\u0010\u000f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0010\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0011\u001a\u00020\u0007H\u00c6\u0003J\'\u0010\u0012\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u0007H\u00c6\u0001J\u0013\u0010\u0013\u001a\u00020\u00142\b\u0010\u0015\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0016\u001a\u00020\u0005H\u00d6\u0001J\t\u0010\u0017\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000e\u00a8\u0006\u0018"}, d2 = {"Lcom/easycomic/domain/repository/ReadingHistoryRepository$DailyReadingStats;", "", "date", "", "sessionCount", "", "totalDuration", "", "(Ljava/lang/String;IJ)V", "getDate", "()Ljava/lang/String;", "getSessionCount", "()I", "getTotalDuration", "()J", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "toString", "app_debug"})
    public static final class DailyReadingStats {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String date = null;
        private final int sessionCount = 0;
        private final long totalDuration = 0L;
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        public final int component2() {
            return 0;
        }
        
        public final long component3() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.easycomic.domain.repository.ReadingHistoryRepository.DailyReadingStats copy(@org.jetbrains.annotations.NotNull()
        java.lang.String date, int sessionCount, long totalDuration) {
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
        
        public DailyReadingStats(@org.jetbrains.annotations.NotNull()
        java.lang.String date, int sessionCount, long totalDuration) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getDate() {
            return null;
        }
        
        public final int getSessionCount() {
            return 0;
        }
        
        public final long getTotalDuration() {
            return 0L;
        }
    }
    
    /**
     * 阅读历史仓库接口
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}