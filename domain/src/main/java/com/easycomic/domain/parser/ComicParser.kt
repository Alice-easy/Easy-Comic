package com.easycomic.domain.parser

import java.io.Closeable
import java.io.InputStream

/**
 * 漫画文件解析器接口
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
}