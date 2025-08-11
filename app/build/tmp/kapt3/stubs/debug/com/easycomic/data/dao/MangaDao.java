package com.easycomic.data.dao;

import androidx.room.*;
import com.easycomic.data.entity.MangaEntity;
import com.easycomic.data.entity.ReadingStatus;
import kotlinx.coroutines.flow.Flow;

/**
 * 漫画数据访问对象
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u000f\n\u0002\u0010\u0007\n\u0002\b\u0004\bg\u0018\u00002\u00020\u0001J\u001c\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0007J\u0016\u0010\b\u001a\u00020\u00032\u0006\u0010\t\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0014\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\fH\'J\u000e\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000e0\fH\'J\u000e\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u000e0\fH\'J\u0014\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\fH\'J\u0018\u0010\u0011\u001a\u0004\u0018\u00010\u00062\u0006\u0010\u0012\u001a\u00020\u0013H\u00a7@\u00a2\u0006\u0002\u0010\u0014J\u0018\u0010\u0015\u001a\u0004\u0018\u00010\u00062\u0006\u0010\u0016\u001a\u00020\u0017H\u00a7@\u00a2\u0006\u0002\u0010\u0018J\u001c\u0010\u0019\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\f2\u0006\u0010\u001a\u001a\u00020\u001bH\'J\u000e\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u000e0\fH\'J\u001e\u0010\u001d\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\f2\b\b\u0002\u0010\u001e\u001a\u00020\u000eH\'J\"\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00170\u00052\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0007J\u0016\u0010 \u001a\u00020\u00172\u0006\u0010\t\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\nJ\u001c\u0010!\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\f2\u0006\u0010\"\u001a\u00020\u0013H\'J \u0010#\u001a\u00020\u00032\u0006\u0010\u0016\u001a\u00020\u00172\b\b\u0002\u0010$\u001a\u00020\u0017H\u00a7@\u00a2\u0006\u0002\u0010%J(\u0010&\u001a\u00020\u00032\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\'\u001a\u00020\u000e2\b\b\u0002\u0010$\u001a\u00020\u0017H\u00a7@\u00a2\u0006\u0002\u0010(J(\u0010)\u001a\u00020\u00032\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010*\u001a\u00020+2\b\b\u0002\u0010$\u001a\u00020\u0017H\u00a7@\u00a2\u0006\u0002\u0010,J0\u0010-\u001a\u00020\u00032\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\'\u001a\u00020\u000e2\u0006\u0010\u001a\u001a\u00020\u001b2\b\b\u0002\u0010$\u001a\u00020\u0017H\u00a7@\u00a2\u0006\u0002\u0010.\u00a8\u0006/"}, d2 = {"Lcom/easycomic/data/dao/MangaDao;", "", "deleteAllManga", "", "mangaList", "", "Lcom/easycomic/data/entity/MangaEntity;", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteManga", "manga", "(Lcom/easycomic/data/entity/MangaEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllManga", "Lkotlinx/coroutines/flow/Flow;", "getCompletedCount", "", "getFavoriteCount", "getFavoriteManga", "getMangaByFilePath", "filePath", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getMangaById", "mangaId", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getMangaByStatus", "status", "Lcom/easycomic/data/entity/ReadingStatus;", "getMangaCount", "getRecentManga", "limit", "insertAllManga", "insertOrUpdateManga", "searchManga", "query", "toggleFavorite", "timestamp", "(JJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateCurrentPage", "currentPage", "(JIJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateRating", "rating", "", "(JFJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateReadingProgress", "(JILcom/easycomic/data/entity/ReadingStatus;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
@androidx.room.Dao()
public abstract interface MangaDao {
    
    /**
     * 插入或更新漫画
     */
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertOrUpdateManga(@org.jetbrains.annotations.NotNull()
    com.easycomic.data.entity.MangaEntity manga, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    /**
     * 批量插入漫画
     */
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertAllManga(@org.jetbrains.annotations.NotNull()
    java.util.List<com.easycomic.data.entity.MangaEntity> mangaList, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<java.lang.Long>> $completion);
    
    /**
     * 获取所有漫画（按添加时间倒序）
     */
    @androidx.room.Query(value = "\n        SELECT * FROM manga \n        ORDER BY date_added DESC\n    ")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.easycomic.data.entity.MangaEntity>> getAllManga();
    
    /**
     * 获取漫画详情
     */
    @androidx.room.Query(value = "SELECT * FROM manga WHERE id = :mangaId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getMangaById(long mangaId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.easycomic.data.entity.MangaEntity> $completion);
    
    /**
     * 根据文件路径获取漫画
     */
    @androidx.room.Query(value = "SELECT * FROM manga WHERE file_path = :filePath")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getMangaByFilePath(@org.jetbrains.annotations.NotNull()
    java.lang.String filePath, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.easycomic.data.entity.MangaEntity> $completion);
    
    /**
     * 搜索漫画
     */
    @androidx.room.Query(value = "\n        SELECT * FROM manga \n        WHERE title LIKE \'%\' || :query || \'%\' \n           OR author LIKE \'%\' || :query || \'%\' \n           OR tags LIKE \'%\' || :query || \'%\'\n        ORDER BY \n            CASE \n                WHEN title LIKE :query THEN 1\n                WHEN author LIKE :query THEN 2\n                ELSE 3\n            END,\n            title ASC\n    ")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.easycomic.data.entity.MangaEntity>> searchManga(@org.jetbrains.annotations.NotNull()
    java.lang.String query);
    
    /**
     * 获取收藏的漫画
     */
    @androidx.room.Query(value = "\n        SELECT * FROM manga \n        WHERE is_favorite = 1 \n        ORDER BY last_read DESC\n    ")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.easycomic.data.entity.MangaEntity>> getFavoriteManga();
    
    /**
     * 根据阅读状态获取漫画
     */
    @androidx.room.Query(value = "\n        SELECT * FROM manga \n        WHERE reading_status = :status \n        ORDER BY last_read DESC\n    ")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.easycomic.data.entity.MangaEntity>> getMangaByStatus(@org.jetbrains.annotations.NotNull()
    com.easycomic.data.entity.ReadingStatus status);
    
    /**
     * 获取最近阅读的漫画
     */
    @androidx.room.Query(value = "\n        SELECT * FROM manga \n        WHERE last_read > 0 \n        ORDER BY last_read DESC \n        LIMIT :limit\n    ")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.easycomic.data.entity.MangaEntity>> getRecentManga(int limit);
    
    /**
     * 更新漫画当前页码
     */
    @androidx.room.Query(value = "\n        UPDATE manga \n        SET current_page = :currentPage, \n            last_read = :timestamp \n        WHERE id = :mangaId\n    ")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateCurrentPage(long mangaId, int currentPage, long timestamp, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 更新阅读进度
     */
    @androidx.room.Query(value = "\n        UPDATE manga \n        SET current_page = :currentPage, \n            reading_status = :status,\n            last_read = :timestamp \n        WHERE id = :mangaId\n    ")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateReadingProgress(long mangaId, int currentPage, @org.jetbrains.annotations.NotNull()
    com.easycomic.data.entity.ReadingStatus status, long timestamp, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 切换收藏状态
     */
    @androidx.room.Query(value = "\n        UPDATE manga \n        SET is_favorite = NOT is_favorite, \n            date_modified = :timestamp \n        WHERE id = :mangaId\n    ")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object toggleFavorite(long mangaId, long timestamp, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 更新评分
     */
    @androidx.room.Query(value = "\n        UPDATE manga \n        SET rating = :rating, \n            date_modified = :timestamp \n        WHERE id = :mangaId\n    ")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateRating(long mangaId, float rating, long timestamp, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 删除漫画
     */
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteManga(@org.jetbrains.annotations.NotNull()
    com.easycomic.data.entity.MangaEntity manga, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 批量删除漫画
     */
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteAllManga(@org.jetbrains.annotations.NotNull()
    java.util.List<com.easycomic.data.entity.MangaEntity> mangaList, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 获取漫画总数
     */
    @androidx.room.Query(value = "SELECT COUNT(*) FROM manga")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.lang.Integer> getMangaCount();
    
    /**
     * 获取收藏漫画数量
     */
    @androidx.room.Query(value = "SELECT COUNT(*) FROM manga WHERE is_favorite = 1")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.lang.Integer> getFavoriteCount();
    
    /**
     * 获取已读漫画数量
     */
    @androidx.room.Query(value = "SELECT COUNT(*) FROM manga WHERE reading_status = \'COMPLETED\'")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.lang.Integer> getCompletedCount();
    
    /**
     * 漫画数据访问对象
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}