# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable

# Keep source file name for debugging
-keepattributes SourceFile

# Keep annotations for reflection and dependency injection
-keepattributes *Annotation*, InnerClasses
-keepattributes Signature
-keepattributes EnclosingMethod

# Keep Hilt dependency injection
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class * extends dagger.hilt.android.internal.managers.ApplicationComponentManager {
    <init>(...);
}

# Keep Room database classes and annotations
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keep @androidx.room.Database class *
-keep @androidx.room.TypeConverter class *
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}

# Keep data classes and models
-keepclassmembers class com.easycomic.core.database.** { *; }
-keepclassmembers class com.easycomic.data.** { *; }
-keepclassmembers class com.easycomic.domain.model.** { *; }

# Keep Gson serialization
-keepattributes *Annotation*, InnerClasses
-dontnote com.google.gson.**
-keep class com.google.gson.** { *; }
-keepclassmembers class com.easycomic.** {
    @com.google.gson.annotations.SerializedName <fields>;
    @com.google.gson.annotations.Expose <fields>;
}

# Keep Coil image loading library
-keep class coil.** { *; }
-keep interface coil.** { *; }
-keepclassmembers class coil.** { *; }

# Keep WebDAV client (Sardine)
-keep class com.github.sardine.** { *; }
-keep class com.github.sardine.impl.** { *; }

# Keep RAR extraction library (Junrar)
-keep class com.github.junrar.** { *; }
-keep class com.github.junrar.rarfile.** { *; }

# Keep WorkManager
-keep class androidx.work.** { *; }
-keepclassmembers class androidx.work.** { *; }
-keep @androidx.work.Worker class * {
    <init>(...);
}

# Keep Compose UI
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }
-keep class androidx.compose.ui.** { *; }
-keepclassmembers class androidx.compose.ui.** { *; }

# Keep ViewModels
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Keep lifecycle components
-keep class androidx.lifecycle.** { *; }
-keepclassmembers class androidx.lifecycle.** { *; }

# Keep navigation
-keep class androidx.navigation.** { *; }
-keepclassmembers class androidx.navigation.** { *; }

# Keep Coroutines
-keep class kotlinx.coroutines.** { *; }
-keepclassmembers class kotlinx.coroutines.** { *; }
-keepinterface class kotlinx.coroutines.flow.** { *; }

# Keep serialization (if using kotlinx.serialization)
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.easycomic.**$$serializer { *; }
-keepclassmembers class com.easycomic.** {
    *** Companion;
}
-keepclasseswithmembers class com.easycomic.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# Keep custom adapters
-keep public class * extends android.widget.BaseAdapter {
    public <init>();
    public void set*(...);
    public *** getView(...);
}

# Keep parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep serializable implementations
-keep class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom exceptions
-keep public class * extends java.lang.Exception

# Optimize and obfuscate
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-allowaccessmodification

# Remove logging in production
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# Remove debug code
-assumenosideeffects class * {
    public *** debug*(...);
    public *** log*(...);
    public *** trace*(...);
}

# Keep application class
-keep public class com.easycomic.EasyComicApplication {
    <init>();
    void onCreate();
}

# Keep application components
-keep public class * extends android.app.Application
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep custom exceptions
-keep public class * extends java.lang.Exception

# Keep bitmap and image processing classes
-keep class android.graphics.Bitmap { *; }
-keep class android.graphics.BitmapFactory { *; }
-keep class android.graphics.BitmapRegionDecoder { *; }
-keep class androidx.exifinterface.media.ExifInterface { *; }

# Keep WebDAV security classes
-keep class androidx.security.crypto.** { *; }
-keep class androidx.security.crypto.EncryptedSharedPreferences { *; }
-keep class androidx.security.crypto.MasterKey { *; }

# Keep file and directory operations
-keep class java.io.File { *; }
-keep class java.io.RandomAccessFile { *; }
-keep class java.io.FileInputStream { *; }
-keep class java.io.FileOutputStream { *; }
-keep class java.nio.file.** { *; }

# Keep zip and rar operations
-keep class java.util.zip.** { *; }
-keep class java.util.jar.** { *; }
-keep class java.util.stream.** { *; }

# Keep database operations
-keep class androidx.sqlite.** { *; }
-keep class androidx.sqlite.db.** { *; }
-keep class net.sqlcipher.** { *; }