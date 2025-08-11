package com.easycomic.data.dao;

import androidx.room.*;
import com.easycomic.data.entity.BookmarkEntity;
import kotlinx.coroutines.flow.Flow;

/**
 * 书签数据访问对象
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\t\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\t\bg\u0018\u00002\u00020\u0001J\u001c\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0007J\u0016\u0010\b\u001a\u00020\u00032\u0006\u0010\t\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0016\u0010\u000b\u001a\u00020\u00032\u0006\u0010\f\u001a\u00020\rH\u00a7@\u00a2\u0006\u0002\u0010\u000eJ\u001e\u0010\u000f\u001a\u00020\u00032\u0006\u0010\u0010\u001a\u00020\r2\u0006\u0010\u0011\u001a\u00020\u0012H\u00a7@\u00a2\u0006\u0002\u0010\u0013J\u0016\u0010\u0014\u001a\u00020\u00032\u0006\u0010\u0010\u001a\u00020\rH\u00a7@\u00a2\u0006\u0002\u0010\u000eJ\u0018\u0010\u0015\u001a\u0004\u0018\u00010\u00062\u0006\u0010\f\u001a\u00020\rH\u00a7@\u00a2\u0006\u0002\u0010\u000eJ$\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\u0006\u0010\u0010\u001a\u00020\r2\u0006\u0010\u0011\u001a\u00020\u0012H\u00a7@\u00a2\u0006\u0002\u0010\u0013J\u000e\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00120\u0018H\'J\u0016\u0010\u0019\u001a\u00020\u00122\u0006\u0010\u0010\u001a\u00020\rH\u00a7@\u00a2\u0006\u0002\u0010\u000eJ\u001c\u0010\u001a\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u00182\u0006\u0010\u0010\u001a\u00020\rH\'J\u001e\u0010\u001b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u00182\b\b\u0002\u0010\u001c\u001a\u00020\u0012H\'J\u001e\u0010\u001d\u001a\u00020\u00122\u0006\u0010\u0010\u001a\u00020\r2\u0006\u0010\u0011\u001a\u00020\u0012H\u00a7@\u00a2\u0006\u0002\u0010\u0013J\"\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\r0\u00052\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0007J\u0016\u0010\u001f\u001a\u00020\r2\u0006\u0010\t\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0016\u0010 \u001a\u00020\u00032\u0006\u0010\t\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\n\u00a8\u0006!"}, d2 = {"Lcom/easycomic/data/dao/BookmarkDao;", "", "deleteAllBookmarks", "", "bookmarkList", "", "Lcom/easycomic/data/entity/BookmarkEntity;", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteBookmark", "bookmark", "(Lcom/easycomic/data/entity/BookmarkEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteBookmarkById", "bookmarkId", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteBookmarkByPage", "mangaId", "pageNumber", "", "(JILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteBookmarksByMangaId", "getBookmarkById", "getBookmarkByPage", "getBookmarkCount", "Lkotlinx/coroutines/flow/Flow;", "getBookmarkCountByMangaId", "getBookmarksByMangaId", "getRecentBookmarks", "limit", "hasBookmarkForPage", "insertAllBookmarks", "insertBookmark", "updateBookmark", "app_debug"})
@androidx.room.Dao()
public abstract interface BookmarkDao {
    
    /**
     * 插入书签
     */
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertBookmark(@org.jetbrains.annotations.NotNull()
    com.easycomic.data.entity.BookmarkEntity bookmark, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    /**
     * 批量插入书签
     */
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertAllBookmarks(@org.jetbrains.annotations.NotNull()
    java.util.List<com.easycomic.data.entity.BookmarkEntity> bookmarkList, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<java.lang.Long>> $completion);
    
    /**
     * 获取指定漫画的所有书签
     */
    @androidx.room.Query(value = "\n        SELECT * FROM bookmark \n        WHERE manga_id = :mangaId \n        ORDER BY page_number ASC, created_at ASC\n    ")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.easycomic.data.entity.BookmarkEntity>> getBookmarksByMangaId(long mangaId);
    
    /**
     * 获取指定页码的书签
     */
    @androidx.room.Query(value = "\n        SELECT * FROM bookmark \n        WHERE manga_id = :mangaId AND page_number = :pageNumber\n        ORDER BY created_at DESC\n    ")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getBookmarkByPage(long mangaId, int pageNumber, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.easycomic.data.entity.BookmarkEntity>> $completion);
    
    /**
     * 获取书签详情
     */
    @androidx.room.Query(value = "SELECT * FROM bookmark WHERE id = :bookmarkId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getBookmarkById(long bookmarkId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.easycomic.data.entity.BookmarkEntity> $completion);
    
    /**
     * 检查指定页码是否有书签
     */
    @androidx.room.Query(value = "\n        SELECT COUNT(*) FROM bookmark \n        WHERE manga_id = :mangaId AND page_number = :pageNumber\n    ")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object hasBookmarkForPage(long mangaId, int pageNumber, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    /**
     * 更新书签
     */
    @androidx.room.Update()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateBookmark(@org.jetbrains.annotations.NotNull()
    com.easycomic.data.entity.BookmarkEntity bookmark, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 删除书签
     */
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteBookmark(@org.jetbrains.annotations.NotNull()
    com.easycomic.data.entity.BookmarkEntity bookmark, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 根据ID删除书签
     */
    @androidx.room.Query(value = "DELETE FROM bookmark WHERE id = :bookmarkId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteBookmarkById(long bookmarkId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 删除指定漫画的所有书签
     */
    @androidx.room.Query(value = "DELETE FROM bookmark WHERE manga_id = :mangaId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteBookmarksByMangaId(long mangaId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 删除指定页码的书签
     */
    @androidx.room.Query(value = "DELETE FROM bookmark WHERE manga_id = :mangaId AND page_number = :pageNumber")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteBookmarkByPage(long mangaId, int pageNumber, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 批量删除书签
     */
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteAllBookmarks(@org.jetbrains.annotations.NotNull()
    java.util.List<com.easycomic.data.entity.BookmarkEntity> bookmarkList, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * 获取书签总数
     */
    @androidx.room.Query(value = "SELECT COUNT(*) FROM bookmark")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.lang.Integer> getBookmarkCount();
    
    /**
     * 获取指定漫画的书签数量
     */
    @androidx.room.Query(value = "SELECT COUNT(*) FROM bookmark WHERE manga_id = :mangaId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getBookmarkCountByMangaId(long mangaId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    /**
     * 获取最近的添加的书签
     */
    @androidx.room.Query(value = "\n        SELECT * FROM bookmark \n        ORDER BY created_at DESC \n        LIMIT :limit\n    ")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.easycomic.data.entity.BookmarkEntity>> getRecentBookmarks(int limit);
    
    /**
     * 书签数据访问对象
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}