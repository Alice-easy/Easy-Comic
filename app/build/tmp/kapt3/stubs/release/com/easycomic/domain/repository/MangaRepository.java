package com.easycomic.domain.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0003\bf\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a6@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u0003H\u00a6@\u00a2\u0006\u0002\u0010\nJ\u0018\u0010\u000b\u001a\u0004\u0018\u00010\u00052\u0006\u0010\t\u001a\u00020\u0003H\u00a6@\u00a2\u0006\u0002\u0010\nJ\u0014\u0010\f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u000e0\rH&J\u001e\u0010\u000f\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u00032\u0006\u0010\u0010\u001a\u00020\u0011H\u00a6@\u00a2\u0006\u0002\u0010\u0012J\u0016\u0010\u0013\u001a\u00020\b2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a6@\u00a2\u0006\u0002\u0010\u0006J.\u0010\u0014\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u00032\u0006\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u0003H\u00a6@\u00a2\u0006\u0002\u0010\u001a\u00a8\u0006\u001b"}, d2 = {"Lcom/easycomic/domain/repository/MangaRepository;", "", "add", "", "manga", "Lcom/easycomic/domain/model/Manga;", "(Lcom/easycomic/domain/model/Manga;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "delete", "", "id", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getById", "observeAll", "Lkotlinx/coroutines/flow/Flow;", "", "setFavorite", "favorite", "", "(JZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "update", "updateProgress", "page", "", "progress", "", "lastRead", "(JIFJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_release"})
public abstract interface MangaRepository {
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.easycomic.domain.model.Manga>> observeAll();
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getById(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.easycomic.domain.model.Manga> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object add(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.model.Manga manga, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object update(@org.jetbrains.annotations.NotNull()
    com.easycomic.domain.model.Manga manga, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object delete(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateProgress(long id, int page, float progress, long lastRead, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object setFavorite(long id, boolean favorite, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}