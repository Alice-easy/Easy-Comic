package com.easycomic.domain.repository;

import com.easycomic.domain.model.Manga;
import com.easycomic.domain.model.Bookmark;
import com.easycomic.domain.model.ReadingHistory;
import com.easycomic.domain.model.ReadingStatus;
import kotlinx.coroutines.flow.Flow;

/**
 * 漫画仓库接口
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0010\u0007\n\u0002\b\u0005\bf\u0018\u00002\u00020\u0001J\u001c\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005H\u00a6@\u00a2\u0006\u0002\u0010\u0007J\u0016\u0010\b\u001a\u00020\u00032\u0006\u0010\t\u001a\u00020\u0006H\u00a6@\u00a2\u0006\u0002\u0010\nJ\u0014\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\fH&J\u000e\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000e0\fH&J\u000e\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u000e0\fH&J\u0014\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\fH&J\u0018\u0010\u0011\u001a\u0004\u0018\u00010\u00062\u0006\u0010\u0012\u001a\u00020\u0013H\u00a6@\u00a2\u0006\u0002\u0010\u0014J\u0018\u0010\u0015\u001a\u0004\u0018\u00010\u00062\u0006\u0010\u0016\u001a\u00020\u0017H\u00a6@\u00a2\u0006\u0002\u0010\u0018J\u001c\u0010\u0019\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\f2\u0006\u0010\u001a\u001a\u00020\u001bH&J\u000e\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u000e0\fH&J\u001e\u0010\u001d\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\f2\b\b\u0002\u0010\u001e\u001a\u00020\u000eH&J\"\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00170\u00052\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005H\u00a6@\u00a2\u0006\u0002\u0010\u0007J\u0016\u0010 \u001a\u00020\u00172\u0006\u0010\t\u001a\u00020\u0006H\u00a6@\u00a2\u0006\u0002\u0010\nJ\u001c\u0010!\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\f2\u0006\u0010\"\u001a\u00020\u0013H&J\u0016\u0010#\u001a\u00020\u00032\u0006\u0010$\u001a\u00020\u0017H\u00a6@\u00a2\u0006\u0002\u0010\u0018J\u001e\u0010%\u001a\u00020\u00032\u0006\u0010$\u001a\u00020\u00172\u0006\u0010&\u001a\u00020\'H\u00a6@\u00a2\u0006\u0002\u0010(J&\u0010)\u001a\u00020\u00032\u0006\u0010$\u001a\u00020\u00172\u0006\u0010*\u001a\u00020\u000e2\u0006\u0010\u001a\u001a\u00020\u001bH\u00a6@\u00a2\u0006\u0002\u0010+\u00a8\u0006,"}, d2 = {"Lcom/easycomic/domain/repository/MangaRepository;", "", "deleteAllManga", "", "mangaList", "", "Lcom/easycomic/domain/model/Manga;", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteManga", "manga", "(Lcom/easycomic/domain/model/Manga;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllManga", "Lkotlinx/coroutines/flow/Flow;", "getCompletedCount", "", "getFavoriteCount", "getFavoriteManga", "getMangaByFilePath", "filePath", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getMangaById", "id", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getMangaByStatus", "status", "Lcom/easycomic/domain/model/ReadingStatus;", "getMangaCount", "getRecentManga", "limit", "insertAllManga", "insertOrUpdateManga", "searchManga", "query", "toggleFavorite", "mangaId", "updateRating", "rating", "", "(JFLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateReadingProgress", "currentPage", "(JILcom/easycomic/domain/model/ReadingStatus;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface MangaRepository {
    
    /**
     * 获取所有漫画
     */
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.easycomic.domain.model.Manga>> getAllManga();
    
    /**
     * 获取漫画详情
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getMangaById(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.easycomic.domain.model.Manga> $completion);
    
    /**
     * 根据文件路径获取漫画
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getMangaByFilePath(@org.jetbrains.annotations.NotNull()
    java.lang.String filePath, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.easycomic.domain.model.Manga> $completion);
    
    /**
     * 搜索漫画
     */
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.easycomic.domain.model.Manga>> searchManga(@org.jetbrains.annotations.NotNull()
    java.lang.String query);
    
    /**
     * 获取收藏的漫画
     */
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.easycomic.domain.model.Manga>> getFavoriteManga();
    
    /**
     * 根据阅读状态获取漫画
     */
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.easycomic.domain.model.Manga>> getMangaByStatus(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.model.ReadingStatus status);
    
    /**
     * 获取最近阅读的漫画
     */
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.easycomic.domain.model.Manga>> getRecentManga(int limit);
    
    /**
     * 添加或更新漫画
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertOrUpdateManga(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.model.Manga manga, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    /**
     * 批量添加漫画
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertAllManga(@org.jetbrains.annotations.NotNull()
    java.util.List<com.easycomic.domain.model.Manga> mangaList, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<java.lang.Long>> $completion);
    
    /**
     * 更新阅读进度
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateReadingProgress(long mangaId, int currentPage, @org.jetbrains.annotations.NotNull()
    com.easycomic.domain.model.ReadingStatus status, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 切换收藏状态
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object toggleFavorite(long mangaId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 更新评分
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateRating(long mangaId, float rating, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 删除漫画
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteManga(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.model.Manga manga, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 批量删除漫画
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteAllManga(@org.jetbrains.annotations.NotNull()
    java.util.List<com.easycomic.domain.model.Manga> mangaList, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 获取漫画总数
     */
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.lang.Integer> getMangaCount();
    
    /**
     * 获取收藏漫画数量
     */
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.lang.Integer> getFavoriteCount();
    
    /**
     * 获取已读漫画数量
     */
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.lang.Integer> getCompletedCount();
    
    /**
     * 漫画仓库接口
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}