package com.easycomic.data.util

import org.junit.Test
import org.junit.Assert.*

/**
 * 封面提取工具的单元测试
 */
class CoverExtractorTest {

    @Test
    fun `test isCoverFile with explicit cover names`() {
        assertTrue(CoverExtractor.isCoverFile("cover.jpg"))
        assertTrue(CoverExtractor.isCoverFile("Cover.PNG"))
        assertTrue(CoverExtractor.isCoverFile("FRONT.jpeg"))
        assertTrue(CoverExtractor.isCoverFile("poster.webp"))
        assertTrue(CoverExtractor.isCoverFile("thumbnail.gif"))
    }

    @Test
    fun `test isCoverFile with cover names in path`() {
        assertTrue(CoverExtractor.isCoverFile("manga_cover_001.jpg"))
        assertTrue(CoverExtractor.isCoverFile("front_page.png"))
        assertTrue(CoverExtractor.isCoverFile("cover_art.jpeg"))
        assertTrue(CoverExtractor.isCoverFile("book_poster.jpg"))
    }

    @Test
    fun `test isCoverFile with non-cover names`() {
        assertFalse(CoverExtractor.isCoverFile("page001.jpg"))
        assertFalse(CoverExtractor.isCoverFile("chapter1.png"))
        assertFalse(CoverExtractor.isCoverFile("content.jpeg"))
        assertFalse(CoverExtractor.isCoverFile("random.gif"))
    }

    @Test
    fun `test isCoverFile with international cover names`() {
        assertTrue(CoverExtractor.isCoverFile("封面.jpg"))
        assertTrue(CoverExtractor.isCoverFile("表紙.png"))
        assertTrue(CoverExtractor.isCoverFile("表纸.jpeg"))
        assertTrue(CoverExtractor.isCoverFile("カバー.webp"))
    }

    @Test
    fun `test isImageFile with valid extensions`() {
        assertTrue(CoverExtractor.isImageFile("image.jpg"))
        assertTrue(CoverExtractor.isImageFile("IMAGE.JPEG"))
        assertTrue(CoverExtractor.isImageFile("photo.png"))
        assertTrue(CoverExtractor.isImageFile("animation.gif"))
        assertTrue(CoverExtractor.isImageFile("modern.webp"))
        assertTrue(CoverExtractor.isImageFile("bitmap.bmp"))
    }

    @Test
    fun `test isImageFile with invalid extensions`() {
        assertFalse(CoverExtractor.isImageFile("document.txt"))
        assertFalse(CoverExtractor.isImageFile("archive.zip"))
        assertFalse(CoverExtractor.isImageFile("video.mp4"))
        assertFalse(CoverExtractor.isImageFile("audio.mp3"))
        assertFalse(CoverExtractor.isImageFile("data.json"))
    }

    @Test
    fun `test isImageFile with no extension`() {
        assertFalse(CoverExtractor.isImageFile("filename"))
        assertFalse(CoverExtractor.isImageFile(""))
    }

    @Test
    fun `test sortImagesByPriority with cover files first`() {
        val input = listOf(
            "page003.jpg",
            "cover.png",
            "page001.jpg",
            "front.jpeg",
            "page002.jpg"
        )
        
        val result = CoverExtractor.sortImagesByPriority(input)
        
        // 封面文件应该排在前面
        assertTrue(result.indexOf("cover.png") < result.indexOf("page001.jpg"))
        assertTrue(result.indexOf("front.jpeg") < result.indexOf("page002.jpg"))
        
        // 非封面文件应该按自然序排列
        assertTrue(result.indexOf("page001.jpg") < result.indexOf("page002.jpg"))
        assertTrue(result.indexOf("page002.jpg") < result.indexOf("page003.jpg"))
    }

    @Test
    fun `test sortImagesByPriority with extension priority`() {
        val input = listOf(
            "image.bmp",
            "image.jpg",
            "image.gif",
            "image.png",
            "image.webp"
        )
        
        val result = CoverExtractor.sortImagesByPriority(input)
        
        // JPG 应该排在最前面 (优先级最高)
        assertEquals("image.jpg", result[0])
        
        // JPEG, PNG, WebP 应该排在 BMP 和 GIF 前面
        assertTrue(result.indexOf("image.png") < result.indexOf("image.bmp"))
        assertTrue(result.indexOf("image.webp") < result.indexOf("image.gif"))
    }

    @Test
    fun `test sortImagesByPriority with mixed cover and extension priority`() {
        val input = listOf(
            "page001.bmp",
            "cover.gif",
            "page002.jpg",
            "front.png"
        )
        
        val result = CoverExtractor.sortImagesByPriority(input)
        
        // 封面文件应该优先，但在封面文件之间按扩展名优先级排序
        assertTrue(result.indexOf("front.png") < result.indexOf("cover.gif"))
        assertTrue(result.indexOf("cover.gif") < result.indexOf("page002.jpg"))
        assertTrue(result.indexOf("page002.jpg") < result.indexOf("page001.bmp"))
    }

    @Test
    fun `test sortImagesByPriority with natural order for same priority`() {
        val input = listOf(
            "page10.jpg",
            "page2.jpg",
            "page1.jpg"
        )
        
        val result = CoverExtractor.sortImagesByPriority(input)
        val expected = listOf("page1.jpg", "page2.jpg", "page10.jpg")
        
        assertEquals(expected, result)
    }

    @Test
    fun `test real world manga file sorting`() {
        val input = listOf(
            "第10页.jpg",
            "封面.png",
            "目录.jpg",
            "第1页.jpg",
            "第2页.jpg",
            "back_cover.jpeg"
        )
        
        val result = CoverExtractor.sortImagesByPriority(input)
        
        // 封面相关文件应该排在前面
        assertTrue(result.indexOf("封面.png") < result.indexOf("第1页.jpg"))
        
        // 其他页面按自然序排列
        assertTrue(result.indexOf("第1页.jpg") < result.indexOf("第2页.jpg"))
        assertTrue(result.indexOf("第2页.jpg") < result.indexOf("第10页.jpg"))
    }
}
