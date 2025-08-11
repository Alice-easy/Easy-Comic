package com.easycomic.util;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.activity.ComponentActivity;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

/**
 * 权限处理工具类
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0011\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0011\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nJ\u000e\u0010\u000b\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nJ*\u0010\f\u001a\u00020\r2\u0006\u0010\t\u001a\u00020\n2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\r0\u000f2\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\r0\u000fJ*\u0010\u0011\u001a\u00020\r2\u0006\u0010\t\u001a\u00020\n2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\r0\u000f2\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\r0\u000fJ\u000e\u0010\u0012\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n\u00a8\u0006\u0013"}, d2 = {"Lcom/easycomic/util/PermissionHandler;", "", "()V", "getRequiredPermissions", "", "", "()[Ljava/lang/String;", "hasManageStoragePermission", "", "activity", "Landroidx/activity/ComponentActivity;", "hasReadStoragePermission", "requestManageStoragePermission", "", "onGranted", "Lkotlin/Function0;", "onDenied", "requestReadStoragePermission", "shouldShowPermissionRationale", "app_debug"})
public final class PermissionHandler {
    @org.jetbrains.annotations.NotNull()
    public static final com.easycomic.util.PermissionHandler INSTANCE = null;
    
    private PermissionHandler() {
        super();
    }
    
    /**
     * 检查是否有读取外部存储权限
     */
    public final boolean hasReadStoragePermission(@org.jetbrains.annotations.NotNull()
    androidx.activity.ComponentActivity activity) {
        return false;
    }
    
    /**
     * 请求读取外部存储权限
     */
    public final void requestReadStoragePermission(@org.jetbrains.annotations.NotNull()
    androidx.activity.ComponentActivity activity, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onGranted, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDenied) {
    }
    
    /**
     * 检查是否有管理外部存储权限（用于 Android 11+）
     */
    public final boolean hasManageStoragePermission(@org.jetbrains.annotations.NotNull()
    androidx.activity.ComponentActivity activity) {
        return false;
    }
    
    /**
     * 请求管理外部存储权限
     */
    public final void requestManageStoragePermission(@org.jetbrains.annotations.NotNull()
    androidx.activity.ComponentActivity activity, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onGranted, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDenied) {
    }
    
    /**
     * 获取所有需要的权限列表
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String[] getRequiredPermissions() {
        return null;
    }
    
    /**
     * 检查是否需要显示权限说明
     */
    public final boolean shouldShowPermissionRationale(@org.jetbrains.annotations.NotNull()
    androidx.activity.ComponentActivity activity) {
        return false;
    }
}