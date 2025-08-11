package com.easycomic.data.dao;

import androidx.room.*;
import com.easycomic.data.entity.ReadingHistoryEntity;
import kotlinx.coroutines.flow.Flow;

/**
 * 阅读历史数据访问对象
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\b\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u001c\u0010\u0007\u001a\u00020\u00032\f\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\tH\u00a7@\u00a2\u0006\u0002\u0010\u000bJ\u0016\u0010\f\u001a\u00020\u00032\u0006\u0010\r\u001a\u00020\nH\u00a7@\u00a2\u0006\u0002\u0010\u000eJ\u0016\u0010\u000f\u001a\u00020\u00032\u0006\u0010\u0010\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0011\u001a\u00020\u00032\u0006\u0010\u0012\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0018\u0010\u0013\u001a\u0004\u0018\u00010\n2\u0006\u0010\u0012\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0018\u0010\u0014\u001a\u0004\u0018\u00010\u00052\u0006\u0010\u0012\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u001c\u0010\u0015\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\u00162\u0006\u0010\u0012\u001a\u00020\u0005H\'J\u000e\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00180\u0016H\'J\u0016\u0010\u0019\u001a\u00020\u00182\u0006\u0010\u0012\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u001e\u0010\u001a\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\u00162\b\b\u0002\u0010\u001b\u001a\u00020\u0018H\'J\u0010\u0010\u001c\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00050\u0016H\'J\"\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u00050\t2\f\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\tH\u00a7@\u00a2\u0006\u0002\u0010\u000bJ\u0016\u0010\u001e\u001a\u00020\u00052\u0006\u0010\r\u001a\u00020\nH\u00a7@\u00a2\u0006\u0002\u0010\u000eJ\u0016\u0010\u001f\u001a\u00020\u00032\u0006\u0010\r\u001a\u00020\nH\u00a7@\u00a2\u0006\u0002\u0010\u000e\u00a8\u0006 "}, d2 = {"Lcom/easycomic/data/dao/ReadingHistoryDao;", "", "cleanupOldReadingHistory", "", "cutoffTime", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteAllReadingHistory", "historyList", "", "Lcom/easycomic/data/entity/ReadingHistoryEntity;", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteReadingHistory", "history", "(Lcom/easycomic/data/entity/ReadingHistoryEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteReadingHistoryById", "historyId", "deleteReadingHistoryByMangaId", "mangaId", "getLatestReadingHistory", "getReadingDurationByMangaId", "getReadingHistoryByMangaId", "Lkotlinx/coroutines/flow/Flow;", "getReadingHistoryCount", "", "getReadingHistoryCountByMangaId", "getRecentReadingHistory", "limit", "getTotalReadingDuration", "insertAllReadingHistory", "insertReadingHistory", "updateReadingHistory", "app_debug"})
@androidx.room.Dao()
public abstract interface ReadingHistoryDao {
    
    /**
     * 插入阅读记录
     */
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertReadingHistory(@org.jetbrains.annotations.NotNull()
    com.easycomic.data.entity.ReadingHistoryEntity history, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    /**
     * 批量插入阅读记录
     */
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertAllReadingHistory(@org.jetbrains.annotations.NotNull()
    java.util.List<com.easycomic.data.entity.ReadingHistoryEntity> historyList, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<java.lang.Long>> $completion);
    
    /**
     * 获取指定漫画的阅读历史
     */
    @androidx.room.Query(value = "\n        SELECT * FROM reading_history \n        WHERE manga_id = :mangaId \n        ORDER BY read_at DESC\n    ")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.easycomic.data.entity.ReadingHistoryEntity>> getReadingHistoryByMangaId(long mangaId);
    
    /**
     * 获取最近的阅读记录
     */
    @androidx.room.Query(value = "\n        SELECT * FROM reading_history \n        ORDER BY read_at DESC \n        LIMIT :limit\n    ")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.easycomic.data.entity.ReadingHistoryEntity>> getRecentReadingHistory(int limit);
    
    /**
     * 获取指定漫画的最新阅读记录
     */
    @androidx.room.Query(value = "\n        SELECT * FROM reading_history \n        WHERE manga_id = :mangaId \n        ORDER BY read_at DESC \n        LIMIT 1\n    ")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getLatestReadingHistory(long mangaId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.easycomic.data.entity.ReadingHistoryEntity> $completion);
    
    /**
     * 更新阅读记录
     */
    @androidx.room.Update()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateReadingHistory(@org.jetbrains.annotations.NotNull()
    com.easycomic.data.entity.ReadingHistoryEntity history, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 删除阅读记录
     */
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteReadingHistory(@org.jetbrains.annotations.NotNull()
    com.easycomic.data.entity.ReadingHistoryEntity history, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 根据ID删除阅读记录
     */
    @androidx.room.Query(value = "DELETE FROM reading_history WHERE id = :historyId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteReadingHistoryById(long historyId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 删除指定漫画的所有阅读记录
     */
    @androidx.room.Query(value = "DELETE FROM reading_history WHERE manga_id = :mangaId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteReadingHistoryByMangaId(long mangaId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 批量删除阅读记录
     */
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteAllReadingHistory(@org.jetbrains.annotations.NotNull()
    java.util.List<com.easycomic.data.entity.ReadingHistoryEntity> historyList, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 清理旧的阅读记录
     */
    @androidx.room.Query(value = "\n        DELETE FROM reading_history \n        WHERE read_at < :cutoffTime\n    ")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object cleanupOldReadingHistory(long cutoffTime, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 获取阅读记录总数
     */
    @androidx.room.Query(value = "SELECT COUNT(*) FROM reading_history")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.lang.Integer> getReadingHistoryCount();
    
    /**
     * 获取指定漫画的阅读记录数量
     */
    @androidx.room.Query(value = "SELECT COUNT(*) FROM reading_history WHERE manga_id = :mangaId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getReadingHistoryCountByMangaId(long mangaId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    /**
     * 获取总阅读时长
     */
    @androidx.room.Query(value = "SELECT SUM(reading_duration) FROM reading_history")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.lang.Long> getTotalReadingDuration();
    
    /**
     * 获取指定漫画的总阅读时长
     */
    @androidx.room.Query(value = "\n        SELECT SUM(reading_duration) \n        FROM reading_history \n        WHERE manga_id = :mangaId\n    ")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getReadingDurationByMangaId(long mangaId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    /**
     * 阅读历史数据访问对象
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}