package com.easycomic.data.repository;

import com.easycomic.data.dao.ReadingHistoryDao;
import com.easycomic.data.entity.ReadingHistoryEntity;
import com.easycomic.domain.model.ReadingHistory;
import com.easycomic.domain.repository.ReadingHistoryRepository;
import kotlinx.coroutines.flow.Flow;

/**
 * 阅读历史仓库实现类
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0006\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\tJ\u0016\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u0006H\u0096@\u00a2\u0006\u0002\u0010\rJ\u001c\u0010\u000e\u001a\u00020\u000b2\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\b0\u0010H\u0096@\u00a2\u0006\u0002\u0010\u0011J\u0016\u0010\u0012\u001a\u00020\u000b2\u0006\u0010\u0007\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\tJ\u0016\u0010\u0013\u001a\u00020\u000b2\u0006\u0010\u0014\u001a\u00020\u0006H\u0096@\u00a2\u0006\u0002\u0010\rJ\u0016\u0010\u0015\u001a\u00020\u000b2\u0006\u0010\u0016\u001a\u00020\u0006H\u0096@\u00a2\u0006\u0002\u0010\rJ\u001c\u0010\u0017\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00190\u00100\u00182\u0006\u0010\u001a\u001a\u00020\u0006H\u0016J\u0018\u0010\u001b\u001a\u0004\u0018\u00010\b2\u0006\u0010\u0016\u001a\u00020\u0006H\u0096@\u00a2\u0006\u0002\u0010\rJ\u0018\u0010\u001c\u001a\u0004\u0018\u00010\u00062\u0006\u0010\u0016\u001a\u00020\u0006H\u0096@\u00a2\u0006\u0002\u0010\rJ\u001c\u0010\u001d\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00100\u00182\u0006\u0010\u0016\u001a\u00020\u0006H\u0016J\u000e\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u001f0\u0018H\u0016J\u0016\u0010 \u001a\u00020\u001f2\u0006\u0010\u0016\u001a\u00020\u0006H\u0096@\u00a2\u0006\u0002\u0010\rJ\u001c\u0010!\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00100\u00182\u0006\u0010\"\u001a\u00020\u001fH\u0016J\u0010\u0010#\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00060\u0018H\u0016J\u0016\u0010$\u001a\u00020\u000b2\u0006\u0010\u0007\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\tR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006%"}, d2 = {"Lcom/easycomic/data/repository/ReadingHistoryRepositoryImpl;", "Lcom/easycomic/domain/repository/ReadingHistoryRepository;", "readingHistoryDao", "Lcom/easycomic/data/dao/ReadingHistoryDao;", "(Lcom/easycomic/data/dao/ReadingHistoryDao;)V", "addReadingHistory", "", "history", "Lcom/easycomic/domain/model/ReadingHistory;", "(Lcom/easycomic/domain/model/ReadingHistory;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "cleanupOldReadingHistory", "", "cutoffTime", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteAllReadingHistory", "historyList", "", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteReadingHistory", "deleteReadingHistoryById", "historyId", "deleteReadingHistoryByMangaId", "mangaId", "getDailyReadingStats", "Lkotlinx/coroutines/flow/Flow;", "Lcom/easycomic/domain/repository/ReadingHistoryRepository$DailyReadingStats;", "startTime", "getLatestReadingHistory", "getReadingDurationByMangaId", "getReadingHistoryByMangaId", "getReadingHistoryCount", "", "getReadingHistoryCountByMangaId", "getRecentReadingHistory", "limit", "getTotalReadingDuration", "updateReadingHistory", "app_debug"})
public final class ReadingHistoryRepositoryImpl implements com.easycomic.domain.repository.ReadingHistoryRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.easycomic.data.dao.ReadingHistoryDao readingHistoryDao = null;
    
    public ReadingHistoryRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.easycomic.data.dao.ReadingHistoryDao readingHistoryDao) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<com.easycomic.domain.model.ReadingHistory>> getReadingHistoryByMangaId(long mangaId) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<com.easycomic.domain.model.ReadingHistory>> getRecentReadingHistory(int limit) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getLatestReadingHistory(long mangaId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.easycomic.domain.model.ReadingHistory> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object addReadingHistory(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.model.ReadingHistory history, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object updateReadingHistory(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.model.ReadingHistory history, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object deleteReadingHistory(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.model.ReadingHistory history, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object deleteReadingHistoryById(long historyId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object deleteReadingHistoryByMangaId(long mangaId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object deleteAllReadingHistory(@org.jetbrains.annotations.NotNull()
    java.util.List<com.easycomic.domain.model.ReadingHistory> historyList, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object cleanupOldReadingHistory(long cutoffTime, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.lang.Integer> getReadingHistoryCount() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getReadingHistoryCountByMangaId(long mangaId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.lang.Long> getTotalReadingDuration() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getReadingDurationByMangaId(long mangaId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<com.easycomic.domain.repository.ReadingHistoryRepository.DailyReadingStats>> getDailyReadingStats(long startTime) {
        return null;
    }
}