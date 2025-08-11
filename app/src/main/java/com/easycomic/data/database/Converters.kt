package com.easycomic.data.database

import androidx.room.TypeConverter
import com.easycomic.data.entity.ReadingStatus

/**
 * Room 数据库类型转换器
 * 用于处理自定义类型的存储和读取
 */
class Converters {
    
    /**
     * ReadingStatus 枚举转字符串
     */
    @TypeConverter
    fun fromReadingStatus(status: ReadingStatus): String {
        return status.name
    }
    
    /**
     * 字符串转 ReadingStatus 枚举
     */
    @TypeConverter
    fun toReadingStatus(status: String): ReadingStatus {
        return ReadingStatus.valueOf(status)
    }
}