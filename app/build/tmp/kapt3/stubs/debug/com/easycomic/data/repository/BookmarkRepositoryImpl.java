package com.easycomic.data.repository;

import com.easycomic.data.dao.BookmarkDao;
import com.easycomic.data.entity.BookmarkEntity;
import com.easycomic.domain.model.Bookmark;
import com.easycomic.domain.repository.BookmarkRepository;
import kotlinx.coroutines.flow.Flow;

/**
 * 书签仓库实现类
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\b\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\tJ\u001c\u0010\n\u001a\u00020\u000b2\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\b0\rH\u0096@\u00a2\u0006\u0002\u0010\u000eJ\u0016\u0010\u000f\u001a\u00020\u000b2\u0006\u0010\u0007\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\tJ\u0016\u0010\u0010\u001a\u00020\u000b2\u0006\u0010\u0011\u001a\u00020\u0006H\u0096@\u00a2\u0006\u0002\u0010\u0012J\u001e\u0010\u0013\u001a\u00020\u000b2\u0006\u0010\u0014\u001a\u00020\u00062\u0006\u0010\u0015\u001a\u00020\u0016H\u0096@\u00a2\u0006\u0002\u0010\u0017J\u0016\u0010\u0018\u001a\u00020\u000b2\u0006\u0010\u0014\u001a\u00020\u0006H\u0096@\u00a2\u0006\u0002\u0010\u0012J\u0018\u0010\u0019\u001a\u0004\u0018\u00010\b2\u0006\u0010\u0011\u001a\u00020\u0006H\u0096@\u00a2\u0006\u0002\u0010\u0012J$\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\b0\r2\u0006\u0010\u0014\u001a\u00020\u00062\u0006\u0010\u0015\u001a\u00020\u0016H\u0096@\u00a2\u0006\u0002\u0010\u0017J\u000e\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00160\u001cH\u0016J\u0016\u0010\u001d\u001a\u00020\u00162\u0006\u0010\u0014\u001a\u00020\u0006H\u0096@\u00a2\u0006\u0002\u0010\u0012J\u001c\u0010\u001e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\r0\u001c2\u0006\u0010\u0014\u001a\u00020\u0006H\u0016J\u001c\u0010\u001f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\r0\u001c2\u0006\u0010 \u001a\u00020\u0016H\u0016J\u001e\u0010!\u001a\u00020\"2\u0006\u0010\u0014\u001a\u00020\u00062\u0006\u0010\u0015\u001a\u00020\u0016H\u0096@\u00a2\u0006\u0002\u0010\u0017J\u0016\u0010#\u001a\u00020\u000b2\u0006\u0010\u0007\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\tR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006$"}, d2 = {"Lcom/easycomic/data/repository/BookmarkRepositoryImpl;", "Lcom/easycomic/domain/repository/BookmarkRepository;", "bookmarkDao", "Lcom/easycomic/data/dao/BookmarkDao;", "(Lcom/easycomic/data/dao/BookmarkDao;)V", "addBookmark", "", "bookmark", "Lcom/easycomic/domain/model/Bookmark;", "(Lcom/easycomic/domain/model/Bookmark;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteAllBookmarks", "", "bookmarkList", "", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteBookmark", "deleteBookmarkById", "bookmarkId", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteBookmarkByPage", "mangaId", "pageNumber", "", "(JILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteBookmarksByMangaId", "getBookmarkById", "getBookmarkByPage", "getBookmarkCount", "Lkotlinx/coroutines/flow/Flow;", "getBookmarkCountByMangaId", "getBookmarksByMangaId", "getRecentBookmarks", "limit", "hasBookmarkForPage", "", "updateBookmark", "app_debug"})
public final class BookmarkRepositoryImpl implements com.easycomic.domain.repository.BookmarkRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.easycomic.data.dao.BookmarkDao bookmarkDao = null;
    
    public BookmarkRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.easycomic.data.dao.BookmarkDao bookmarkDao) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<com.easycomic.domain.model.Bookmark>> getBookmarksByMangaId(long mangaId) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getBookmarkByPage(long mangaId, int pageNumber, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.easycomic.domain.model.Bookmark>> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getBookmarkById(long bookmarkId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.easycomic.domain.model.Bookmark> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object hasBookmarkForPage(long mangaId, int pageNumber, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object addBookmark(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.model.Bookmark bookmark, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object updateBookmark(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.model.Bookmark bookmark, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object deleteBookmark(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.model.Bookmark bookmark, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object deleteBookmarkById(long bookmarkId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object deleteBookmarksByMangaId(long mangaId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object deleteBookmarkByPage(long mangaId, int pageNumber, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object deleteAllBookmarks(@org.jetbrains.annotations.NotNull()
    java.util.List<com.easycomic.domain.model.Bookmark> bookmarkList, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.lang.Integer> getBookmarkCount() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getBookmarkCountByMangaId(long mangaId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<com.easycomic.domain.model.Bookmark>> getRecentBookmarks(int limit) {
        return null;
    }
}