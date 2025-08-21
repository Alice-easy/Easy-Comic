package com.easycomic.data.util

import org.junit.Test
import org.junit.Assert.*

/**
 * 自然序排序算法的单元测试
 * 验证算法能正确处理各种文件名格式
 */
class NaturalOrderComparatorTest {

    private val comparator = NaturalOrderComparator()

    @Test
    fun `test basic number sorting`() {
        val input = listOf("Image 10.jpg", "Image 2.jpg", "Image 1.jpg")
        val expected = listOf("Image 1.jpg", "Image 2.jpg", "Image 10.jpg")
        val result = input.sortedWith(comparator)
        assertEquals(expected, result)
    }

    @Test
    fun `test chapter sorting`() {
        val input = listOf("Chapter 11.zip", "Chapter 2.zip", "Chapter 1.zip")
        val expected = listOf("Chapter 1.zip", "Chapter 2.zip", "Chapter 11.zip")
        val result = input.sortedWith(comparator)
        assertEquals(expected, result)
    }

    @Test
    fun `test mixed alphanumeric sorting`() {
        val input = listOf("Page10.png", "Page2.png", "Page1.png", "Cover.jpg")
        val expected = listOf("Cover.jpg", "Page1.png", "Page2.png", "Page10.png")
        val result = input.sortedWith(comparator)
        assertEquals(expected, result)
    }

    @Test
    fun `test leading zeros`() {
        val input = listOf("IMG_0010.jpg", "IMG_0002.jpg", "IMG_0001.jpg")
        val expected = listOf("IMG_0001.jpg", "IMG_0002.jpg", "IMG_0010.jpg")
        val result = input.sortedWith(comparator)
        assertEquals(expected, result)
    }

    @Test
    fun `test same length numbers`() {
        val input = listOf("page099.png", "page010.png", "page001.png")
        val expected = listOf("page001.png", "page010.png", "page099.png")
        val result = input.sortedWith(comparator)
        assertEquals(expected, result)
    }

    @Test
    fun `test case insensitive`() {
        val input = listOf("IMAGE10.JPG", "image2.jpg", "Image1.PNG")
        val expected = listOf("Image1.PNG", "image2.jpg", "IMAGE10.JPG")
        val result = input.sortedWith(comparator)
        assertEquals(expected, result)
    }

    @Test
    fun `test special characters`() {
        val input = listOf("Page-10.jpg", "Page-2.jpg", "Page_1.jpg")
        val expected = listOf("Page-2.jpg", "Page-10.jpg", "Page_1.jpg")
        val result = input.sortedWith(comparator)
        assertEquals(expected, result)
    }

    @Test
    fun `test empty and null strings`() {
        val input = listOf("Page2.jpg", "", "Page1.jpg")
        val expected = listOf("", "Page1.jpg", "Page2.jpg")
        val result = input.sortedWith(comparator)
        assertEquals(expected, result)
    }

    @Test
    fun `test complex manga filenames`() {
        val input = listOf(
            "第10话_最终章.jpg",
            "第2话_开始.jpg", 
            "第1话_序幕.jpg",
            "封面.jpg",
            "目录.jpg"
        )
        val expected = listOf(
            "封面.jpg",
            "目录.jpg",
            "第1话_序幕.jpg",
            "第2话_开始.jpg", 
            "第10话_最终章.jpg"
        )
        val result = input.sortedWith(comparator)
        assertEquals(expected, result)
    }

    @Test
    fun `test multiple numbers in filename`() {
        val input = listOf(
            "Vol2_Chapter10_Page5.jpg",
            "Vol1_Chapter2_Page10.jpg",
            "Vol2_Chapter2_Page1.jpg"
        )
        val expected = listOf(
            "Vol1_Chapter2_Page10.jpg",
            "Vol2_Chapter2_Page1.jpg",
            "Vol2_Chapter10_Page5.jpg"
        )
        val result = input.sortedWith(comparator)
        assertEquals(expected, result)
    }

    @Test
    fun `test convenience method sortNaturally`() {
        val input = listOf("File10.txt", "File2.txt", "File1.txt")
        val expected = listOf("File1.txt", "File2.txt", "File10.txt")
        val result = NaturalOrderComparator.sortNaturally(input)
        assertEquals(expected, result)
    }

    @Test
    fun `test convenience method sortFileNames`() {
        val input = listOf("Image10.jpg", "Image2.jpg", "Image1.jpg")
        val expected = listOf("Image1.jpg", "Image2.jpg", "Image10.jpg")
        val result = NaturalOrderComparator.sortFileNames(input)
        assertEquals(expected, result)
    }

    @Test
    fun `test compare method directly`() {
        assertTrue(comparator.compare("Image1.jpg", "Image10.jpg") < 0)
        assertTrue(comparator.compare("Image10.jpg", "Image1.jpg") > 0)
        assertEquals(0, comparator.compare("Image1.jpg", "Image1.jpg"))
    }

    @Test
    fun `test real world comic archive names`() {
        val input = listOf(
            "Naruto_Vol_10.cbz",
            "Naruto_Vol_2.cbz",
            "Naruto_Vol_1.cbz",
            "Naruto_Vol_20.cbz"
        )
        val expected = listOf(
            "Naruto_Vol_1.cbz",
            "Naruto_Vol_2.cbz",
            "Naruto_Vol_10.cbz",
            "Naruto_Vol_20.cbz"
        )
        val result = input.sortedWith(comparator)
        assertEquals(expected, result)
    }
}
