package com.easycomic.domain.repository;

import com.easycomic.domain.model.Bookmark;
import kotlinx.coroutines.flow.Flow;

/**
 * 书签仓库接口
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\b\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a6@\u00a2\u0006\u0002\u0010\u0006J\u001c\u0010\u0007\u001a\u00020\b2\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00050\nH\u00a6@\u00a2\u0006\u0002\u0010\u000bJ\u0016\u0010\f\u001a\u00020\b2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a6@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\r\u001a\u00020\b2\u0006\u0010\u000e\u001a\u00020\u0003H\u00a6@\u00a2\u0006\u0002\u0010\u000fJ\u001e\u0010\u0010\u001a\u00020\b2\u0006\u0010\u0011\u001a\u00020\u00032\u0006\u0010\u0012\u001a\u00020\u0013H\u00a6@\u00a2\u0006\u0002\u0010\u0014J\u0016\u0010\u0015\u001a\u00020\b2\u0006\u0010\u0011\u001a\u00020\u0003H\u00a6@\u00a2\u0006\u0002\u0010\u000fJ\u0018\u0010\u0016\u001a\u0004\u0018\u00010\u00052\u0006\u0010\u000e\u001a\u00020\u0003H\u00a6@\u00a2\u0006\u0002\u0010\u000fJ$\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00050\n2\u0006\u0010\u0011\u001a\u00020\u00032\u0006\u0010\u0012\u001a\u00020\u0013H\u00a6@\u00a2\u0006\u0002\u0010\u0014J\u000e\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00130\u0019H&J\u0016\u0010\u001a\u001a\u00020\u00132\u0006\u0010\u0011\u001a\u00020\u0003H\u00a6@\u00a2\u0006\u0002\u0010\u000fJ\u001c\u0010\u001b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\n0\u00192\u0006\u0010\u0011\u001a\u00020\u0003H&J\u001e\u0010\u001c\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\n0\u00192\b\b\u0002\u0010\u001d\u001a\u00020\u0013H&J\u001e\u0010\u001e\u001a\u00020\u001f2\u0006\u0010\u0011\u001a\u00020\u00032\u0006\u0010\u0012\u001a\u00020\u0013H\u00a6@\u00a2\u0006\u0002\u0010\u0014J\u0016\u0010 \u001a\u00020\b2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a6@\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006!"}, d2 = {"Lcom/easycomic/domain/repository/BookmarkRepository;", "", "addBookmark", "", "bookmark", "Lcom/easycomic/domain/model/Bookmark;", "(Lcom/easycomic/domain/model/Bookmark;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteAllBookmarks", "", "bookmarkList", "", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteBookmark", "deleteBookmarkById", "bookmarkId", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteBookmarkByPage", "mangaId", "pageNumber", "", "(JILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteBookmarksByMangaId", "getBookmarkById", "getBookmarkByPage", "getBookmarkCount", "Lkotlinx/coroutines/flow/Flow;", "getBookmarkCountByMangaId", "getBookmarksByMangaId", "getRecentBookmarks", "limit", "hasBookmarkForPage", "", "updateBookmark", "app_debug"})
public abstract interface BookmarkRepository {
    
    /**
     * 获取指定漫画的所有书签
     */
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.easycomic.domain.model.Bookmark>> getBookmarksByMangaId(long mangaId);
    
    /**
     * 获取指定页码的书签
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getBookmarkByPage(long mangaId, int pageNumber, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.easycomic.domain.model.Bookmark>> $completion);
    
    /**
     * 获取书签详情
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getBookmarkById(long bookmarkId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.easycomic.domain.model.Bookmark> $completion);
    
    /**
     * 检查指定页码是否有书签
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object hasBookmarkForPage(long mangaId, int pageNumber, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion);
    
    /**
     * 添加书签
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object addBookmark(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.model.Bookmark bookmark, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    /**
     * 更新书签
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateBookmark(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.model.Bookmark bookmark, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 删除书签
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteBookmark(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.model.Bookmark bookmark, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 根据ID删除书签
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteBookmarkById(long bookmarkId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 删除指定漫画的所有书签
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteBookmarksByMangaId(long mangaId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 删除指定页码的书签
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteBookmarkByPage(long mangaId, int pageNumber, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 批量删除书签
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteAllBookmarks(@org.jetbrains.annotations.NotNull()
    java.util.List<com.easycomic.domain.model.Bookmark> bookmarkList, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 获取书签总数
     */
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.lang.Integer> getBookmarkCount();
    
    /**
     * 获取指定漫画的书签数量
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getBookmarkCountByMangaId(long mangaId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    /**
     * 获取最近添加的书签
     */
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.easycomic.domain.model.Bookmark>> getRecentBookmarks(int limit);
    
    /**
     * 书签仓库接口
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}