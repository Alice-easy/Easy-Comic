package com.easycomic.data.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;
import timber.log.Timber;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * 文件操作工具类
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0002\b\u000b\n\u0002\u0010\u0012\n\u0002\b\u0002\b\u00c7\u0002\u0018\u00002\u00020\u0001:\u0001 B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J \u0010\u0003\u001a\u0004\u0018\u00010\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u0004J\u000e\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u0004J\u0016\u0010\r\u001a\u00020\u000b2\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bJ\u000e\u0010\r\u001a\u00020\u000b2\u0006\u0010\u000e\u001a\u00020\u0004J\u0016\u0010\u000f\u001a\u00020\u000b2\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bJ\u000e\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0013J\u0016\u0010\u0014\u001a\u00020\u00112\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bJ\u000e\u0010\u0014\u001a\u00020\u00112\u0006\u0010\u0015\u001a\u00020\u0011J\u0016\u0010\u0016\u001a\u00020\u00112\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bJ\u0016\u0010\u0017\u001a\u00020\u00112\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bJ\u0016\u0010\u0018\u001a\u00020\u00132\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bJ\u0016\u0010\u0019\u001a\u00020\u00132\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bJ\u0018\u0010\u001a\u001a\u0004\u0018\u00010\u00112\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bJ\u001a\u0010\u001b\u001a\u0004\u0018\u00010\u00112\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0002J\u0016\u0010\u001c\u001a\u00020\u000b2\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bJ\u0016\u0010\u001d\u001a\u00020\u000b2\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bJ\u0018\u0010\u001e\u001a\u0004\u0018\u00010\u001f2\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b\u00a8\u0006!"}, d2 = {"Lcom/easycomic/data/util/FileHelper;", "", "()V", "copyFile", "Ljava/io/File;", "context", "Landroid/content/Context;", "uri", "Landroid/net/Uri;", "destDir", "createDirectory", "", "dir", "deleteFile", "file", "fileExists", "formatFileSize", "", "size", "", "getFileExtension", "fileName", "getFileName", "getFilePath", "getFileSize", "getLastModified", "getMimeType", "getRealPathFromURI", "isImageFile", "isSupportedComicFormat", "readFileToBytes", "", "Companion", "app_debug"})
public final class FileHelper {
    @org.jetbrains.annotations.NotNull()
    public static final com.easycomic.data.util.FileHelper.Companion Companion = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.easycomic.data.util.FileHelper INSTANCE = null;
    
    private FileHelper() {
        super();
    }
    
    /**
     * 获取文件名
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFileName(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.net.Uri uri) {
        return null;
    }
    
    /**
     * 获取文件大小
     */
    public final long getFileSize(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.net.Uri uri) {
        return 0L;
    }
    
    /**
     * 获取文件路径
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFilePath(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.net.Uri uri) {
        return null;
    }
    
    /**
     * 获取文件MIME类型
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getMimeType(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.net.Uri uri) {
        return null;
    }
    
    /**
     * 获取文件扩展名
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFileExtension(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.net.Uri uri) {
        return null;
    }
    
    /**
     * 获取文件扩展名
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFileExtension(@org.jetbrains.annotations.NotNull()
    java.lang.String fileName) {
        return null;
    }
    
    /**
     * 检查文件是否存在
     */
    public final boolean fileExists(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.net.Uri uri) {
        return false;
    }
    
    /**
     * 复制文件到指定目录
     */
    @org.jetbrains.annotations.Nullable()
    public final java.io.File copyFile(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.net.Uri uri, @org.jetbrains.annotations.NotNull()
    java.io.File destDir) {
        return null;
    }
    
    /**
     * 删除文件
     */
    public final boolean deleteFile(@org.jetbrains.annotations.NotNull()
    java.io.File file) {
        return false;
    }
    
    /**
     * 删除文件
     */
    public final boolean deleteFile(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.net.Uri uri) {
        return false;
    }
    
    /**
     * 创建目录
     */
    public final boolean createDirectory(@org.jetbrains.annotations.NotNull()
    java.io.File dir) {
        return false;
    }
    
    /**
     * 格式化文件大小
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String formatFileSize(long size) {
        return null;
    }
    
    /**
     * 检查是否为支持的漫画格式
     */
    public final boolean isSupportedComicFormat(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.net.Uri uri) {
        return false;
    }
    
    /**
     * 检查是否为图片文件
     */
    public final boolean isImageFile(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.net.Uri uri) {
        return false;
    }
    
    /**
     * 获取文件修改时间
     */
    public final long getLastModified(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.net.Uri uri) {
        return 0L;
    }
    
    /**
     * 读取文件为字节数组
     */
    @org.jetbrains.annotations.Nullable()
    public final byte[] readFileToBytes(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.net.Uri uri) {
        return null;
    }
    
    /**
     * 从URI获取真实文件路径
     */
    private final java.lang.String getRealPathFromURI(android.content.Context context, android.net.Uri uri) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0002\b\u0007\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u0017\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\u0007R\u0017\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\u0007\u00a8\u0006\f"}, d2 = {"Lcom/easycomic/data/util/FileHelper$Companion;", "", "()V", "COMIC_EXTENSIONS", "", "", "getCOMIC_EXTENSIONS", "()Ljava/util/List;", "IMAGE_EXTENSIONS", "getIMAGE_EXTENSIONS", "SUPPORTED_COMIC_FORMATS", "getSUPPORTED_COMIC_FORMATS", "app_debug"})
    public static final class Companion {
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<java.lang.String> SUPPORTED_COMIC_FORMATS = null;
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<java.lang.String> IMAGE_EXTENSIONS = null;
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<java.lang.String> COMIC_EXTENSIONS = null;
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<java.lang.String> getSUPPORTED_COMIC_FORMATS() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<java.lang.String> getIMAGE_EXTENSIONS() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<java.lang.String> getCOMIC_EXTENSIONS() {
            return null;
        }
    }
}