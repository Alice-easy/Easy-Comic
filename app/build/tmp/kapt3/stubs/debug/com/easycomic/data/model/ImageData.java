package com.easycomic.data.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * 图片数据模型
 * 用于存储图片的二进制数据和相关元信息
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000P\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0012\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u0007\n\u0002\b\u0010\n\u0002\u0018\u0002\n\u0002\b\u0011\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0087\b\u0018\u0000 :2\u00020\u0001:\u0001:BA\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0005\u0012\u0006\u0010\u0007\u001a\u00020\b\u0012\u0006\u0010\t\u001a\u00020\u0005\u0012\b\b\u0002\u0010\n\u001a\u00020\u000b\u0012\b\b\u0002\u0010\f\u001a\u00020\u000b\u00a2\u0006\u0002\u0010\rJ=\u0010\u001f\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00050 2\u0006\u0010!\u001a\u00020\u00052\u0006\u0010\"\u001a\u00020\u00052\b\u0010#\u001a\u0004\u0018\u00010\u00052\b\u0010$\u001a\u0004\u0018\u00010\u0005H\u0002\u00a2\u0006\u0002\u0010%J\t\u0010&\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\'\u001a\u00020\u0005H\u00c6\u0003J\t\u0010(\u001a\u00020\u0005H\u00c6\u0003J\t\u0010)\u001a\u00020\bH\u00c6\u0003J\t\u0010*\u001a\u00020\u0005H\u00c6\u0003J\t\u0010+\u001a\u00020\u000bH\u00c6\u0003J\t\u0010,\u001a\u00020\u000bH\u00c6\u0003J-\u0010-\u001a\u00020\u00002\b\b\u0002\u0010.\u001a\u00020\u00052\n\b\u0002\u0010#\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010$\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010/JO\u00100\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\u00052\b\b\u0002\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\u000bH\u00c6\u0001J\u0013\u00101\u001a\u0002022\b\u00103\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\u0010\u00104\u001a\u00020\u000b2\u0006\u0010\t\u001a\u000205H\u0002J\t\u00106\u001a\u00020\u0005H\u00d6\u0001J\u0006\u00107\u001a\u000208J\t\u00109\u001a\u00020\u000bH\u00d6\u0001R\u0011\u0010\u000e\u001a\u00020\u000f8F\u00a2\u0006\u0006\u001a\u0004\b\u0010\u0010\u0011R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0011\u0010\u0016\u001a\u00020\u000b8F\u00a2\u0006\u0006\u001a\u0004\b\u0017\u0010\u0018R\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u0011\u0010\f\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0018R\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u0018R\u0011\u0010\t\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001aR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001a\u00a8\u0006;"}, d2 = {"Lcom/easycomic/data/model/ImageData;", "", "data", "", "width", "", "height", "format", "Lcom/easycomic/data/model/ImageFormat;", "size", "path", "", "mimeType", "([BIILcom/easycomic/data/model/ImageFormat;ILjava/lang/String;Ljava/lang/String;)V", "aspectRatio", "", "getAspectRatio", "()F", "getData", "()[B", "getFormat", "()Lcom/easycomic/data/model/ImageFormat;", "formattedSize", "getFormattedSize", "()Ljava/lang/String;", "getHeight", "()I", "getMimeType", "getPath", "getSize", "getWidth", "calculateTargetSize", "Lkotlin/Pair;", "originalWidth", "originalHeight", "maxWidth", "maxHeight", "(IILjava/lang/Integer;Ljava/lang/Integer;)Lkotlin/Pair;", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "compress", "quality", "(ILjava/lang/Integer;Ljava/lang/Integer;)Lcom/easycomic/data/model/ImageData;", "copy", "equals", "", "other", "formatFileSize", "", "hashCode", "toBitmap", "Landroid/graphics/Bitmap;", "toString", "Companion", "app_debug"})
public final class ImageData {
    @org.jetbrains.annotations.NotNull()
    private final byte[] data = null;
    private final int width = 0;
    private final int height = 0;
    @org.jetbrains.annotations.NotNull()
    private final com.easycomic.data.model.ImageFormat format = null;
    private final int size = 0;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String path = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String mimeType = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.easycomic.data.model.ImageData.Companion Companion = null;
    
    @org.jetbrains.annotations.NotNull()
    public final byte[] component1() {
        return null;
    }
    
    public final int component2() {
        return 0;
    }
    
    public final int component3() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.data.model.ImageFormat component4() {
        return null;
    }
    
    public final int component5() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.data.model.ImageData copy(@org.jetbrains.annotations.NotNull()
    byte[] data, int width, int height, @org.jetbrains.annotations.NotNull()
    com.easycomic.data.model.ImageFormat format, int size, @org.jetbrains.annotations.NotNull()
    java.lang.String path, @org.jetbrains.annotations.NotNull()
    java.lang.String mimeType) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
    
    public ImageData(@org.jetbrains.annotations.NotNull()
    byte[] data, int width, int height, @org.jetbrains.annotations.NotNull()
    com.easycomic.data.model.ImageFormat format, int size, @org.jetbrains.annotations.NotNull()
    java.lang.String path, @org.jetbrains.annotations.NotNull()
    java.lang.String mimeType) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final byte[] getData() {
        return null;
    }
    
    public final int getWidth() {
        return 0;
    }
    
    public final int getHeight() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.data.model.ImageFormat getFormat() {
        return null;
    }
    
    public final int getSize() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getPath() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getMimeType() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFormattedSize() {
        return null;
    }
    
    public final float getAspectRatio() {
        return 0.0F;
    }
    
    /**
     * 转换为Bitmap
     */
    @org.jetbrains.annotations.NotNull()
    public final android.graphics.Bitmap toBitmap() {
        return null;
    }
    
    /**
     * 压缩图片数据
     * @param quality 压缩质量 (0-100)
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @return 压缩后的ImageData
     */
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.data.model.ImageData compress(int quality, @org.jetbrains.annotations.Nullable()
    java.lang.Integer maxWidth, @org.jetbrains.annotations.Nullable()
    java.lang.Integer maxHeight) {
        return null;
    }
    
    /**
     * 计算目标尺寸
     */
    private final kotlin.Pair<java.lang.Integer, java.lang.Integer> calculateTargetSize(int originalWidth, int originalHeight, java.lang.Integer maxWidth, java.lang.Integer maxHeight) {
        return null;
    }
    
    /**
     * 格式化文件大小
     */
    private final java.lang.String formatFileSize(long size) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J6\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\r\u001a\u00020\fJ\"\u0010\u000e\u001a\u00020\u00042\u0006\u0010\u000f\u001a\u00020\u00102\b\b\u0002\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\r\u001a\u00020\f\u00a8\u0006\u0011"}, d2 = {"Lcom/easycomic/data/model/ImageData$Companion;", "", "()V", "fromBitmap", "Lcom/easycomic/data/model/ImageData;", "bitmap", "Landroid/graphics/Bitmap;", "format", "Lcom/easycomic/data/model/ImageFormat;", "quality", "", "path", "", "mimeType", "fromInputStream", "inputStream", "Ljava/io/InputStream;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        /**
         * 从输入流创建ImageData
         */
        @org.jetbrains.annotations.NotNull()
        public final com.easycomic.data.model.ImageData fromInputStream(@org.jetbrains.annotations.NotNull()
        java.io.InputStream inputStream, @org.jetbrains.annotations.NotNull()
        java.lang.String path, @org.jetbrains.annotations.NotNull()
        java.lang.String mimeType) {
            return null;
        }
        
        /**
         * 从Bitmap创建ImageData
         */
        @org.jetbrains.annotations.NotNull()
        public final com.easycomic.data.model.ImageData fromBitmap(@org.jetbrains.annotations.NotNull()
        android.graphics.Bitmap bitmap, @org.jetbrains.annotations.NotNull()
        com.easycomic.data.model.ImageFormat format, int quality, @org.jetbrains.annotations.NotNull()
        java.lang.String path, @org.jetbrains.annotations.NotNull()
        java.lang.String mimeType) {
            return null;
        }
    }
}