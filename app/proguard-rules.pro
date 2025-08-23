# Easy Comic ProGuard Rules
# 代码混淆和优化配置

# ======================
# 基础配置
# ======================

# 保持源文件名和行号信息（用于调试崩溃报告）
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# 保持泛型签名
-keepattributes Signature

# 保持注解
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations

# ======================
# Android基础组件
# ======================

# 保持Application类
-keep public class com.easycomic.EasyComicApplication

# 保持Activity和Fragment
-keep public class * extends android.app.Activity
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends android.app.Fragment

# 保持Service
-keep public class * extends android.app.Service

# 保持BroadcastReceiver
-keep public class * extends android.content.BroadcastReceiver

# 保持ContentProvider
-keep public class * extends android.content.ContentProvider

# ======================
# Jetpack Compose
# ======================

# Compose 编译器生成的类
-keep class androidx.compose.** { *; }
-keep class kotlin.coroutines.jvm.internal.** { *; }

# Compose UI
-keepclassmembers class androidx.compose.ui.platform.AndroidCompositionLocals_androidKt {
    *;
}

# Navigation Compose
-keep class androidx.navigation.compose.** { *; }

# ======================
# Kotlin和协程
# ======================

# Kotlin反射
-keep class kotlin.reflect.** { *; }
-keep class kotlin.Metadata { *; }

# Kotlin协程
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcher {}

# Kotlin序列化（如果使用）
-keepattributes InnerClasses
-dontwarn kotlinx.serialization.**
-keep,includedescriptorclasses class com.easycomic.**$$serializer { *; }
-keepclassmembers class com.easycomic.** {
    *** Companion;
}
-keepclasseswithmembers class com.easycomic.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ======================
# Koin依赖注入
# ======================

# 保持Koin相关类
-keep class org.koin.** { *; }
-keep class org.koin.core.** { *; }
-keep class org.koin.dsl.** { *; }

# 保持Module类
-keep class com.easycomic.di.** { *; }
-keep class com.easycomic.*.di.** { *; }

# ======================
# Room数据库
# ======================

# Room
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# 保持Entity类
-keep class com.easycomic.data.entity.** { *; }

# 保持DAO接口
-keep interface com.easycomic.data.dao.** { *; }

# 保持Database类
-keep class com.easycomic.data.database.** { *; }

# ======================
# 域模型和数据类
# ======================

# 保持Domain模型
-keep class com.easycomic.domain.model.** { *; }

# 保持数据类的构造函数和字段
-keepclassmembers class com.easycomic.domain.model.** {
    <init>(...);
    <fields>;
}

# ======================
# 文件解析器
# ======================

# 保持ComicParser接口和实现类
-keep interface com.easycomic.domain.parser.** { *; }
-keep class com.easycomic.data.parser.** { *; }

# JunRar库（RAR解析）
-keep class com.github.junrar.** { *; }
-dontwarn com.github.junrar.**

# Apache Commons Compress
-keep class org.apache.commons.compress.** { *; }
-dontwarn org.apache.commons.compress.**

# ======================
# 图片加载（Coil）
# ======================

# Coil
-keep class coil.** { *; }
-keep interface coil.** { *; }

# OkHttp (Coil依赖)
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ======================
# 测试相关（在Release中排除）
# ======================

# 移除测试代码
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Timber日志库
-keep class timber.log.** { *; }
-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# ======================
# 性能监控
# ======================

# 保持PerformanceTracker
-keep class com.easycomic.performance.** { *; }
-keep class com.easycomic.util.PerformanceMonitor { *; }

# ======================
# 反射相关
# ======================

# 保持使用反射的类
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# 保持枚举
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保持Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# 保持Serializable
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ======================
# 第三方库特殊配置
# ======================

# Gson（如果使用）
-keepattributes Signature
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ======================
# 自定义Keep规则
# ======================

# 保持所有UseCase类
-keep class com.easycomic.domain.usecase.** { *; }

# 保持所有Repository接口和实现
-keep interface com.easycomic.domain.repository.** { *; }
-keep class com.easycomic.data.repository.** { *; }

# 保持ViewModel类
-keep class com.easycomic.ui_bookshelf.BookshelfViewModel { *; }
-keep class com.easycomic.ui_reader.ReaderViewModel { *; }

# ======================
# 优化配置
# ======================

# 允许优化
-allowaccessmodification
-dontpreverify

# 移除未使用的代码
-dontshrink

# 代码优化级别
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*,!code/allocation/variable

# ======================
# 调试配置（Release时移除）
# ======================

# 在Release构建中移除调试信息
-assumenosideeffects class android.util.Log {
    public static *** println(...);
}

# 移除系统输出
-assumenosideeffects class java.io.PrintStream {
    public void println(%);
    public void println(**);
}

# ======================
# 警告抑制
# ======================

# 忽略警告
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe
-dontwarn java.lang.invoke.**