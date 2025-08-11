package com.easycomic.domain.model;

/**
 * 阅读状态枚举 - 领域模型
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0006\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0003\u001a\u00020\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\bj\u0002\b\t\u00a8\u0006\n"}, d2 = {"Lcom/easycomic/domain/model/ReadingStatus;", "", "(Ljava/lang/String;I)V", "getDisplayName", "", "UNREAD", "READING", "COMPLETED", "PAUSED", "DROPPED", "app_debug"})
public enum ReadingStatus {
    /*public static final*/ UNREAD /* = new UNREAD() */,
    /*public static final*/ READING /* = new READING() */,
    /*public static final*/ COMPLETED /* = new COMPLETED() */,
    /*public static final*/ PAUSED /* = new PAUSED() */,
    /*public static final*/ DROPPED /* = new DROPPED() */;
    
    ReadingStatus() {
    }
    
    /**
     * 获取状态的中文名称
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDisplayName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.easycomic.domain.model.ReadingStatus> getEntries() {
        return null;
    }
}