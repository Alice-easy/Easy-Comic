package com.easycomic.domain.parser

import java.io.Closeable
import java.io.InputStream

/**
 * 漫画文件解析器接口
 * 支持页面读取、封面提取和文件信息获取
 */
interface ComicParser : Closeable {
    /**
     * 获取总页数
     */
    fun getPageCount(): Int

    /**
     * 获取指定页面的输入流
     */
    fun getPageStream(pageIndex: Int): InputStream?
    
    /**
     * 获取所有页面的文件名列表
     * 用于封面提取和调试
     */
    fun getPageNames(): List<String>
    
    /**
     * 获取封面图片流
     * 默认实现返回第一页
     */
    fun getCoverStream(): InputStream? = getPageStream(0)
    
    /**
     * 获取页面文件大小（字节）
     * 用于内存管理和进度显示
     */
    fun getPageSize(pageIndex: Int): Long
    
    /**
     * 检查解析器是否支持随机访问
     * 影响翻页性能和缓存策略
     */
    fun supportsRandomAccess(): Boolean = true
}