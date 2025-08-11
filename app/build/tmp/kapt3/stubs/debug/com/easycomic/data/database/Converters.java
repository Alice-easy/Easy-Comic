package com.easycomic.data.database;

import androidx.room.TypeConverter;
import com.easycomic.data.entity.ReadingStatus;

/**
 * Room 数据库类型转换器
 * 用于处理自定义类型的存储和读取
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\u0007\u001a\u00020\u00062\u0006\u0010\u0005\u001a\u00020\u0004H\u0007\u00a8\u0006\b"}, d2 = {"Lcom/easycomic/data/database/Converters;", "", "()V", "fromReadingStatus", "", "status", "Lcom/easycomic/data/entity/ReadingStatus;", "toReadingStatus", "app_debug"})
public final class Converters {
    
    public Converters() {
        super();
    }
    
    /**
     * ReadingStatus 枚举转字符串
     */
    @androidx.room.TypeConverter()
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String fromReadingStatus(@org.jetbrains.annotations.NotNull()
    com.easycomic.data.entity.ReadingStatus status) {
        return null;
    }
    
    /**
     * 字符串转 ReadingStatus 枚举
     */
    @androidx.room.TypeConverter()
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.data.entity.ReadingStatus toReadingStatus(@org.jetbrains.annotations.NotNull()
    java.lang.String status) {
        return null;
    }
}