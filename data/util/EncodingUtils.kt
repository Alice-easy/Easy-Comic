package com.easycomic.data.util

import timber.log.Timber
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * 编码处理工具类
 * 处理ZIP/RAR文件中的文件名编码问题
 */
object EncodingUtils {
    
    // 常见的文件名编码格式
    private val COMMON_ENCODINGS = listOf(
        StandardCharsets.UTF_8,
        Charset.forName("GBK"),        // 中文简体
        Charset.forName("Big5"),       // 中文繁体
        Charset.forName("Shift_JIS"),  // 日文
        Charset.forName("EUC-KR"),     // 韩文
        Charset.forName("CP437"),      // MS-DOS
        Charset.forName("CP932"),      // Windows日文
        StandardCharsets.ISO_8859_1    // 西欧
    )
    
    /**
     * 尝试修复文件名编码
     * @param originalName 原始文件名
     * @return 修复后的文件名
     */
    fun fixEncoding(originalName: String): String {
        // 如果已经是有效的UTF-8且包含非ASCII字符，直接返回
        if (isValidUTF8(originalName) && containsNonAscii(originalName)) {
            return originalName
        }
        
        // 尝试不同编码进行转换
        for (encoding in COMMON_ENCODINGS) {
            try {
                val bytes = originalName.toByteArray(StandardCharsets.ISO_8859_1)
                val decoded = String(bytes, encoding)
                
                // 检查解码结果是否合理
                if (isReasonableFileName(decoded)) {
                    Timber.d("编码修复成功: $originalName -> $decoded (使用 ${encoding.name()})")
                    return decoded
                }
            } catch (e: Exception) {
                // 继续尝试下一个编码
            }
        }
        
        // 如果所有编码都失败，返回原始名称
        Timber.w("无法修复文件名编码: $originalName")
        return originalName
    }
    
    /**
     * 检查字符串是否为有效的UTF-8
     */
    private fun isValidUTF8(str: String): Boolean {
        return try {
            val bytes = str.toByteArray(StandardCharsets.UTF_8)
            val decoded = String(bytes, StandardCharsets.UTF_8)
            str == decoded
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 检查字符串是否包含非ASCII字符
     */
    private fun containsNonAscii(str: String): Boolean {
        return str.any { it.code > 127 }
    }
    
    /**
     * 检查解码结果是否为合理的文件名
     */
    private fun isReasonableFileName(fileName: String): Boolean {
        // 排除明显的乱码特征
        if (fileName.isBlank() || fileName.length > 255) {
            return false
        }
        
        // 检查是否包含过多的控制字符
        val controlCharCount = fileName.count { it.isISOControl() }
        if (controlCharCount > fileName.length * 0.1) {
            return false
        }
        
        // 检查是否包含不合理的字符序列
        val suspiciousPatterns = listOf("????", "����", "□□")
        if (suspiciousPatterns.any { pattern -> fileName.contains(pattern) }) {
            return false
        }
        
        return true
    }
    
    /**
     * 智能检测文本编码
     * @param bytes 字节数组
     * @return 最可能的编码格式
     */
    fun detectEncoding(bytes: ByteArray): Charset {
        // 检查BOM
        if (bytes.size >= 3 && 
            bytes[0] == 0xEF.toByte() && 
            bytes[1] == 0xBB.toByte() && 
            bytes[2] == 0xBF.toByte()) {
            return StandardCharsets.UTF_8
        }
        
        // 尝试UTF-8解码
        try {
            val decoded = String(bytes, StandardCharsets.UTF_8)
            if (decoded.all { !it.isISOControl() || it == '\n' || it == '\r' || it == '\t' }) {
                return StandardCharsets.UTF_8
            }
        } catch (e: Exception) {
            // UTF-8解码失败
        }
        
        // 根据字节特征判断可能的编码
        val stats = analyzeByteStats(bytes)
        
        return when {
            stats.highBitRatio > 0.3 -> {
                // 高位字节较多，可能是中文
                if (stats.isGBKLike) Charset.forName("GBK")
                else if (stats.isBig5Like) Charset.forName("Big5")
                else Charset.forName("GBK") // 默认GBK
            }
            stats.highBitRatio > 0.1 -> {
                // 中等高位字节，可能是日韩文
                Charset.forName("Shift_JIS")
            }
            else -> {
                // 主要是ASCII，使用默认编码
                StandardCharsets.UTF_8
            }
        }
    }
    
    private data class ByteStats(
        val highBitRatio: Double,
        val isGBKLike: Boolean,
        val isBig5Like: Boolean
    )
    
    private fun analyzeByteStats(bytes: ByteArray): ByteStats {
        var highBitCount = 0
        var gbkLikeCount = 0
        var big5LikeCount = 0
        
        for (i in bytes.indices) {
            val byte = bytes[i].toInt() and 0xFF
            
            if (byte > 127) {
                highBitCount++
                
                // 简单的GBK/Big5特征检测
                if (i < bytes.size - 1) {
                    val nextByte = bytes[i + 1].toInt() and 0xFF
                    
                    // GBK特征：首字节 0xA1-0xFE，次字节 0xA1-0xFE
                    if (byte in 0xA1..0xFE && nextByte in 0xA1..0xFE) {
                        gbkLikeCount++
                    }
                    
                    // Big5特征：首字节 0xA1-0xF9，次字节 0x40-0x7E或0xA1-0xFE
                    if (byte in 0xA1..0xF9 && 
                        (nextByte in 0x40..0x7E || nextByte in 0xA1..0xFE)) {
                        big5LikeCount++
                    }
                }
            }
        }
        
        val totalBytes = bytes.size
        return ByteStats(
            highBitRatio = highBitCount.toDouble() / totalBytes,
            isGBKLike = gbkLikeCount > totalBytes * 0.1,
            isBig5Like = big5LikeCount > totalBytes * 0.1
        )
    }
}
