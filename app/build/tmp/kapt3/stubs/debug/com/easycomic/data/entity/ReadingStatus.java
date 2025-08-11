package com.easycomic.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Index;
import androidx.room.ColumnInfo;

/**
 * 阅读状态枚举
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0007\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007\u00a8\u0006\b"}, d2 = {"Lcom/easycomic/data/entity/ReadingStatus;", "", "(Ljava/lang/String;I)V", "UNREAD", "READING", "COMPLETED", "PAUSED", "DROPPED", "app_debug"})
public enum ReadingStatus {
    /*public static final*/ UNREAD /* = new UNREAD() */,
    /*public static final*/ READING /* = new READING() */,
    /*public static final*/ COMPLETED /* = new COMPLETED() */,
    /*public static final*/ PAUSED /* = new PAUSED() */,
    /*public static final*/ DROPPED /* = new DROPPED() */;
    
    ReadingStatus() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.easycomic.data.entity.ReadingStatus> getEntries() {
        return null;
    }
}