package com.easycomic.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import kotlinx.coroutines.Dispatchers;
import timber.log.Timber;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * 简化的图像加载器，支持大图像的区域解码
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010%\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0012\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\t\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J(\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\b2\u0006\u0010\n\u001a\u00020\b2\u0006\u0010\u000b\u001a\u00020\b2\u0006\u0010\f\u001a\u00020\bH\u0002J\u0006\u0010\r\u001a\u00020\u000eJ\u0006\u0010\u000f\u001a\u00020\u0005J\u0006\u0010\u0010\u001a\u00020\u0005J$\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00130\u00122\u0006\u0010\u0014\u001a\u00020\u0015H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0016\u0010\u0017JB\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00060\u00122\u0006\u0010\u0014\u001a\u00020\u00152\b\b\u0002\u0010\u000b\u001a\u00020\b2\b\b\u0002\u0010\f\u001a\u00020\b2\b\b\u0002\u0010\u0019\u001a\u00020\u001aH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u001b\u0010\u001cJ<\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u00060\u00122\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010\u000b\u001a\u00020\b2\u0006\u0010\f\u001a\u00020\bH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b \u0010!J4\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u00060\u00122\u0006\u0010#\u001a\u00020\u00062\u0006\u0010$\u001a\u00020\b2\u0006\u0010%\u001a\u00020\bH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b&\u0010\'R\u001a\u0010\u0003\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00060\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006("}, d2 = {"Lcom/easycomic/image/ImageLoader;", "", "()V", "bitmapCache", "", "", "Landroid/graphics/Bitmap;", "calculateInSampleSize", "", "srcWidth", "srcHeight", "reqWidth", "reqHeight", "clearCache", "", "getCacheInfo", "getMemoryInfo", "loadForCompose", "Lkotlin/Result;", "Landroidx/compose/ui/graphics/ImageBitmap;", "imageData", "", "loadForCompose-gIAlu-s", "([BLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "loadImage", "useRegionDecoder", "", "loadImage-yxL6bBk", "([BIIZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "loadImageRegion", "rect", "Landroid/graphics/Rect;", "loadImageRegion-yxL6bBk", "([BLandroid/graphics/Rect;IILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "scaleBitmap", "bitmap", "targetWidth", "targetHeight", "scaleBitmap-BWLJW6A", "(Landroid/graphics/Bitmap;IILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class ImageLoader {
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<java.lang.String, android.graphics.Bitmap> bitmapCache = null;
    
    public ImageLoader() {
        super();
    }
    
    /**
     * 清理内存缓存
     */
    public final void clearCache() {
    }
    
    /**
     * 计算适当的采样率
     */
    private final int calculateInSampleSize(int srcWidth, int srcHeight, int reqWidth, int reqHeight) {
        return 0;
    }
    
    /**
     * 获取内存使用情况
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getMemoryInfo() {
        return null;
    }
    
    /**
     * 获取缓存信息
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getCacheInfo() {
        return null;
    }
}