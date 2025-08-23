package com.easycomic.data.util

/**
 * 自然序排序比较器
 * 能够正确处理包含数字的字符串排序，例如：
 * - "Image 2.jpg" < "Image 10.jpg"
 * - "Chapter 1.zip" < "Chapter 11.zip"
 * 
 * 实现了人类直观的排序规则，避免字典序排序的问题。
 */
class NaturalOrderComparator : Comparator<String> {
    
    override fun compare(o1: String?, o2: String?): Int {
        if (o1 == null && o2 == null) return 0
        if (o1 == null) return -1
        if (o2 == null) return 1
        
        return compareNatural(o1, o2)
    }
    
    private fun compareNatural(str1: String, str2: String): Int {
        var i1 = 0
        var i2 = 0
        
        while (i1 < str1.length && i2 < str2.length) {
            val char1 = str1[i1]
            val char2 = str2[i2]
            
            if (char1.isDigit() && char2.isDigit()) {
                // 两个都是数字，比较数值
                val numResult = compareNumbers(str1, str2, i1, i2)
                if (numResult.result != 0) {
                    return numResult.result
                }
                i1 = numResult.nextIndex1
                i2 = numResult.nextIndex2
            } else {
                // 普通字符比较（忽略大小写）
                val cmp = char1.lowercaseChar().compareTo(char2.lowercaseChar())
                if (cmp != 0) {
                    return cmp
                }
                i1++
                i2++
            }
        }
        
        // 处理长度不等的情况
        return str1.length.compareTo(str2.length)
    }
    
    private fun compareNumbers(str1: String, str2: String, start1: Int, start2: Int): NumberCompareResult {
        var i1 = start1
        var i2 = start2
        
        // 跳过前导零
        while (i1 < str1.length && str1[i1] == '0') i1++
        while (i2 < str2.length && str2[i2] == '0') i2++
        
        // 提取数字部分
        val num1Start = i1
        val num2Start = i2
        
        while (i1 < str1.length && str1[i1].isDigit()) i1++
        while (i2 < str2.length && str2[i2].isDigit()) i2++
        
        val num1Length = i1 - num1Start
        val num2Length = i2 - num2Start
        
        // 比较数字长度（长度长的数字更大）
        if (num1Length != num2Length) {
            return NumberCompareResult(num1Length.compareTo(num2Length), i1, i2)
        }
        
        // 长度相同，逐位比较
        for (k in 0 until num1Length) {
            val digit1 = str1[num1Start + k]
            val digit2 = str2[num2Start + k]
            val cmp = digit1.compareTo(digit2)
            if (cmp != 0) {
                return NumberCompareResult(cmp, i1, i2)
            }
        }
        
        return NumberCompareResult(0, i1, i2)
    }
    
    private data class NumberCompareResult(
        val result: Int,
        val nextIndex1: Int,
        val nextIndex2: Int
    )
    
    companion object {
        /**
         * 便捷方法：对字符串列表进行自然序排序
         */
        fun sortNaturally(list: List<String>): List<String> {
            return list.sortedWith(NaturalOrderComparator())
        }
        
        /**
         * 便捷方法：对文件名列表进行自然序排序
         */
        fun sortFileNames(fileNames: List<String>): List<String> {
            return fileNames.sortedWith(NaturalOrderComparator())
        }
    }
}
