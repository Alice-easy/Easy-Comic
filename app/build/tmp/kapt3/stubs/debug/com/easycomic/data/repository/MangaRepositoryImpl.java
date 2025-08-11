package com.easycomic.data.repository;

import com.easycomic.data.dao.MangaDao;
import com.easycomic.data.entity.MangaEntity;
import com.easycomic.domain.model.Manga;
import com.easycomic.domain.repository.MangaRepository;
import kotlinx.coroutines.flow.Flow;
import com.easycomic.data.entity.ReadingStatus;

/**
 * 漫画仓库实现类
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0010\u0007\n\u0002\b\u0005\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001c\u0010\u0005\u001a\u00020\u00062\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bH\u0096@\u00a2\u0006\u0002\u0010\nJ\u0016\u0010\u000b\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\tH\u0096@\u00a2\u0006\u0002\u0010\rJ\u0014\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b0\u000fH\u0016J\u000e\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00110\u000fH\u0016J\u000e\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00110\u000fH\u0016J\u0014\u0010\u0013\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b0\u000fH\u0016J\u0018\u0010\u0014\u001a\u0004\u0018\u00010\t2\u0006\u0010\u0015\u001a\u00020\u0016H\u0096@\u00a2\u0006\u0002\u0010\u0017J\u0018\u0010\u0018\u001a\u0004\u0018\u00010\t2\u0006\u0010\u0019\u001a\u00020\u001aH\u0096@\u00a2\u0006\u0002\u0010\u001bJ\u001c\u0010\u001c\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b0\u000f2\u0006\u0010\u001d\u001a\u00020\u001eH\u0016J\u000e\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00110\u000fH\u0016J\u001c\u0010 \u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b0\u000f2\u0006\u0010!\u001a\u00020\u0011H\u0016J\"\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u001a0\b2\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bH\u0096@\u00a2\u0006\u0002\u0010\nJ\u0016\u0010#\u001a\u00020\u001a2\u0006\u0010\f\u001a\u00020\tH\u0096@\u00a2\u0006\u0002\u0010\rJ\u001c\u0010$\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b0\u000f2\u0006\u0010%\u001a\u00020\u0016H\u0016J\u0016\u0010&\u001a\u00020\u00062\u0006\u0010\'\u001a\u00020\u001aH\u0096@\u00a2\u0006\u0002\u0010\u001bJ\u001e\u0010(\u001a\u00020\u00062\u0006\u0010\'\u001a\u00020\u001a2\u0006\u0010)\u001a\u00020*H\u0096@\u00a2\u0006\u0002\u0010+J&\u0010,\u001a\u00020\u00062\u0006\u0010\'\u001a\u00020\u001a2\u0006\u0010-\u001a\u00020\u00112\u0006\u0010\u001d\u001a\u00020\u001eH\u0096@\u00a2\u0006\u0002\u0010.R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006/"}, d2 = {"Lcom/easycomic/data/repository/MangaRepositoryImpl;", "Lcom/easycomic/domain/repository/MangaRepository;", "mangaDao", "Lcom/easycomic/data/dao/MangaDao;", "(Lcom/easycomic/data/dao/MangaDao;)V", "deleteAllManga", "", "mangaList", "", "Lcom/easycomic/domain/model/Manga;", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteManga", "manga", "(Lcom/easycomic/domain/model/Manga;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllManga", "Lkotlinx/coroutines/flow/Flow;", "getCompletedCount", "", "getFavoriteCount", "getFavoriteManga", "getMangaByFilePath", "filePath", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getMangaById", "id", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getMangaByStatus", "status", "Lcom/easycomic/domain/model/ReadingStatus;", "getMangaCount", "getRecentManga", "limit", "insertAllManga", "insertOrUpdateManga", "searchManga", "query", "toggleFavorite", "mangaId", "updateRating", "rating", "", "(JFLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateReadingProgress", "currentPage", "(JILcom/easycomic/domain/model/ReadingStatus;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class MangaRepositoryImpl implements com.easycomic.domain.repository.MangaRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.easycomic.data.dao.MangaDao mangaDao = null;
    
    public MangaRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.easycomic.data.dao.MangaDao mangaDao) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<com.easycomic.domain.model.Manga>> getAllManga() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getMangaById(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.easycomic.domain.model.Manga> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getMangaByFilePath(@org.jetbrains.annotations.NotNull()
    java.lang.String filePath, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.easycomic.domain.model.Manga> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<com.easycomic.domain.model.Manga>> searchManga(@org.jetbrains.annotations.NotNull()
    java.lang.String query) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<com.easycomic.domain.model.Manga>> getFavoriteManga() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<com.easycomic.domain.model.Manga>> getMangaByStatus(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.model.ReadingStatus status) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<com.easycomic.domain.model.Manga>> getRecentManga(int limit) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object insertOrUpdateManga(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.model.Manga manga, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object insertAllManga(@org.jetbrains.annotations.NotNull()
    java.util.List<com.easycomic.domain.model.Manga> mangaList, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<java.lang.Long>> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object updateReadingProgress(long mangaId, int currentPage, @org.jetbrains.annotations.NotNull()
    com.easycomic.domain.model.ReadingStatus status, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object toggleFavorite(long mangaId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object updateRating(long mangaId, float rating, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object deleteManga(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.model.Manga manga, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object deleteAllManga(@org.jetbrains.annotations.NotNull()
    java.util.List<com.easycomic.domain.model.Manga> mangaList, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.lang.Integer> getMangaCount() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.lang.Integer> getFavoriteCount() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.lang.Integer> getCompletedCount() {
        return null;
    }
}