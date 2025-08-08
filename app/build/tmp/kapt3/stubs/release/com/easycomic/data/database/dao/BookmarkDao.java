package com.easycomic.data.database.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u001c\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\t0\b2\u0006\u0010\n\u001a\u00020\u000bH\'J\u0016\u0010\f\u001a\u00020\u000b2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\r"}, d2 = {"Lcom/easycomic/data/database/dao/BookmarkDao;", "", "delete", "", "entity", "Lcom/easycomic/data/database/entity/BookmarkEntity;", "(Lcom/easycomic/data/database/entity/BookmarkEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "observeByManga", "Lkotlinx/coroutines/flow/Flow;", "", "mangaId", "", "upsert", "app_release"})
@androidx.room.Dao()
public abstract interface BookmarkDao {
    
    @androidx.room.Query(value = "SELECT * FROM bookmark WHERE manga_id = :mangaId ORDER BY page_number ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.easycomic.data.database.entity.BookmarkEntity>> observeByManga(long mangaId);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object upsert(@org.jetbrains.annotations.NotNull()
    com.easycomic.data.database.entity.BookmarkEntity entity, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object delete(@org.jetbrains.annotations.NotNull()
    com.easycomic.data.database.entity.BookmarkEntity entity, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}