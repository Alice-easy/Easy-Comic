package com.easycomic.data.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * 图片格式枚举
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0005\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005\u00a8\u0006\u0006"}, d2 = {"Lcom/easycomic/data/model/ImageFormat;", "", "(Ljava/lang/String;I)V", "JPEG", "PNG", "WEBP", "app_debug"})
public enum ImageFormat {
    /*public static final*/ JPEG /* = new JPEG() */,
    /*public static final*/ PNG /* = new PNG() */,
    /*public static final*/ WEBP /* = new WEBP() */;
    
    ImageFormat() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.easycomic.data.model.ImageFormat> getEntries() {
        return null;
    }
}