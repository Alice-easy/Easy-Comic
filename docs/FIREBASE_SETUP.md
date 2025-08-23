# Firebase 集成指南

## 📋 概述

本文档说明如何为 Easy Comic 集成 Firebase 服务，包括 Crashlytics（崩溃报告）、Performance Monitoring（性能监控）和 Analytics（数据分析）。

## 🔧 前置准备

### 1. 创建 Firebase 项目

1. 访问 [Firebase 控制台](https://console.firebase.google.com/)
2. 点击"创建项目"或"添加项目"
3. 输入项目名称：`Easy Comic`
4. 选择是否启用 Google Analytics（推荐启用）
5. 选择或创建 Google Analytics 账户
6. 点击"创建项目"

### 2. 添加 Android 应用

1. 在 Firebase 项目中点击"添加应用" > Android 图标
2. 填写应用信息：
   - **Android 包名**: `com.easycomic`
   - **应用昵称**: `Easy Comic`
   - **调试签名证书 SHA-1**: （可选，用于某些功能）
3. 点击"注册应用"

### 3. 下载配置文件

1. 下载 `google-services.json` 文件
2. 将文件放置在 `app/` 目录下
3. **重要**: 将 `google-services.json` 添加到 `.gitignore` 中（如果包含敏感信息）

## 🚀 代码集成

### 1. 更新项目级 build.gradle

在项目根目录的 `build.gradle.kts` 中添加：

```kotlin
plugins {
    // 现有插件...
    id("com.google.gms.google-services") version "4.4.0" apply false
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
    id("com.google.firebase.firebase-perf") version "1.4.2" apply false
}
```

### 2. 更新应用级 build.gradle

在 `app/build.gradle.kts` 中：

#### 添加插件

```kotlin
plugins {
    // 现有插件...
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
}
```

#### 添加依赖

```kotlin
dependencies {
    // Firebase BoM (统一版本管理)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // Firebase Analytics
    implementation("com.google.firebase:firebase-analytics-ktx")

    // Firebase Crashlytics
    implementation("com.google.firebase:firebase-crashlytics-ktx")

    // Firebase Performance Monitoring
    implementation("com.google.firebase:firebase-perf-ktx")

    // 其他现有依赖...
}
```

### 3. 更新 CrashReportingManager

在 `CrashReportingManager.kt` 中取消注释 Firebase 相关代码：

```kotlin
private fun initializeFirebaseCrashlytics() {
    try {
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!isDebugMode)
        FirebaseCrashlytics.getInstance().setCustomKey("app_version", BuildConfig.VERSION_NAME)
        FirebaseCrashlytics.getInstance().setCustomKey("app_build", BuildConfig.VERSION_CODE)

        Timber.d("Firebase Crashlytics initialized")
    } catch (e: Exception) {
        Timber.w(e, "Failed to initialize Firebase Crashlytics")
    }
}
```

## 🔧 配置说明

### Crashlytics 配置

#### 自动收集崩溃报告

```kotlin
// 在Application.onCreate()中
FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
```

#### 手动记录异常

```kotlin
// 记录非致命异常
FirebaseCrashlytics.getInstance().recordException(throwable)

// 添加自定义键值
FirebaseCrashlytics.getInstance().setCustomKey("page", "comic_reader")
FirebaseCrashlytics.getInstance().setCustomKey("comic_id", comicId)

// 设置用户标识符
FirebaseCrashlytics.getInstance().setUserId(userId)
```

### Performance Monitoring 配置

#### 自动追踪

Performance Monitoring 会自动追踪：

- 应用启动时间
- Activity 渲染时间
- 网络请求（如果有）

#### 手动追踪

```kotlin
// 开始自定义追踪
val trace = FirebasePerformance.getInstance().newTrace("comic_loading")
trace.start()

// 添加自定义指标
trace.putMetric("file_size_mb", fileSizeMB)
trace.putAttribute("comic_format", "zip")

// 停止追踪
trace.stop()
```

### Analytics 配置

#### 记录自定义事件

```kotlin
// 记录页面浏览
FirebaseAnalytics.getInstance(context).logEvent("page_view") {
    param("page_name", "bookshelf")
    param("user_engagement", "high")
}

// 记录用户行为
FirebaseAnalytics.getInstance(context).logEvent("comic_opened") {
    param("comic_format", "zip")
    param("file_size", fileSizeBytes)
}
```

## 🎯 推荐的监控策略

### 1. 关键指标监控

```kotlin
// 启动性能
val startupTrace = FirebasePerformance.getInstance().newTrace("app_startup")

// 漫画加载性能
val loadingTrace = FirebasePerformance.getInstance().newTrace("comic_loading")
loadingTrace.putAttribute("format", comicFormat)
loadingTrace.putMetric("file_size_mb", fileSizeMB.toLong())

// 内存使用情况
FirebaseCrashlytics.getInstance().setCustomKey("memory_usage_mb", memoryUsageMB)
```

### 2. 用户行为分析

```kotlin
// 用户偏好分析
FirebaseAnalytics.getInstance(context).logEvent("reading_preference") {
    param("theme", selectedTheme)
    param("zoom_mode", zoomMode)
    param("page_turn_mode", pageTurnMode)
}

// 错误上下文
FirebaseCrashlytics.getInstance().setCustomKey("last_action", "opening_comic")
FirebaseCrashlytics.getInstance().setCustomKey("comic_path", comicPath)
```

### 3. 性能阈值设置

```kotlin
// 设置性能警报阈值
class PerformanceTracker {
    fun trackComicLoading(loadTimeMs: Long) {
        if (loadTimeMs > 5000) { // 5秒阈值
            FirebaseCrashlytics.getInstance().recordException(
                Exception("Slow comic loading: ${loadTimeMs}ms")
            )
        }

        // 记录性能指标
        val trace = FirebasePerformance.getInstance().newTrace("comic_load_time")
        trace.putMetric("load_time_ms", loadTimeMs)
        trace.stop()
    }
}
```

## 🔒 隐私和数据保护

### 1. 用户隐私控制

```kotlin
// 允许用户控制数据收集
class PrivacySettings {
    fun setCrashReportingEnabled(enabled: Boolean) {
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(enabled)
        FirebasePerformance.getInstance().isPerformanceCollectionEnabled = enabled
        FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(enabled)
    }
}
```

### 2. 数据最小化

```kotlin
// 只收集必要的数据
FirebaseCrashlytics.getInstance().setCustomKey("app_version", BuildConfig.VERSION_NAME)
// 不记录文件路径等敏感信息
// FirebaseCrashlytics.getInstance().setCustomKey("file_path", filePath) // ❌ 避免
```

## 📊 监控面板设置

### Firebase 控制台配置

1. **Crashlytics 面板**

   - 设置崩溃警报
   - 配置团队通知
   - 设置崩溃率阈值

2. **Performance 面板**

   - 监控启动时间趋势
   - 设置性能下降警报
   - 分析用户体验指标

3. **Analytics 面板**
   - 创建自定义受众群体
   - 设置转化事件
   - 配置实时报告

## 🧪 测试配置

### 开发环境测试

```kotlin
// 在Debug模式下测试崩溃报告
if (BuildConfig.DEBUG) {
    // 强制发送测试崩溃
    FirebaseCrashlytics.getInstance().recordException(
        RuntimeException("Test crash from ${BuildConfig.VERSION_NAME}")
    )
}
```

### 发布前检查清单

- [ ] Firebase 项目配置正确
- [ ] `google-services.json` 文件已添加
- [ ] 所有 Firebase 插件已添加到 build.gradle
- [ ] 崩溃报告测试正常
- [ ] 性能监控数据正常收集
- [ ] 用户隐私设置已实现

## 🔗 相关链接

- [Firebase Android 文档](https://firebase.google.com/docs/android/setup)
- [Crashlytics 集成指南](https://firebase.google.com/docs/crashlytics/get-started?platform=android)
- [Performance Monitoring 指南](https://firebase.google.com/docs/perf-mon/get-started-android)
- [Analytics 集成指南](https://firebase.google.com/docs/analytics/get-started?platform=android)

---

**注意**: Firebase 集成是可选的。如果不使用 Firebase，应用仍然可以通过 ACRA 或自定义日志系统进行崩溃报告。
