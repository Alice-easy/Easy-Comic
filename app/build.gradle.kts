plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    id("jacoco") // Phase 4: 测试覆盖率
    
    // 代码质量检查工具
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
    
    // Firebase plugins (需要时取消注释)
    // id("com.google.gms.google-services")
    // id("com.google.firebase.crashlytics")
    // id("com.google.firebase.firebase-perf")
}

android {
    namespace = "com.easycomic"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.easycomic"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.6.0-alpha"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }
    
    signingConfigs {
        create("release") {
            // 从环境变量或gradle.properties读取签名信息
            keyAlias = project.findProperty("SIGNING_KEY_ALIAS") as String? ?: System.getenv("SIGNING_KEY_ALIAS")
            keyPassword = project.findProperty("SIGNING_KEY_PASSWORD") as String? ?: System.getenv("SIGNING_KEY_PASSWORD")
            storeFile = file(project.findProperty("SIGNING_STORE_FILE") as String? ?: "keystore/release.keystore")
            storePassword = project.findProperty("SIGNING_STORE_PASSWORD") as String? ?: System.getenv("SIGNING_STORE_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // 使用签名配置（如果可用）
            if (signingConfigs.findByName("release")?.keyAlias != null) {
                signingConfig = signingConfigs.getByName("release")
            }
            
            // 发布版本配置
            isDebuggable = false
            isJniDebuggable = false
            isPseudoLocalesEnabled = false
            
            // 版本命名
            versionNameSuffix = ""
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
            // 禁用代码覆盖率以避免DEX错误
            enableAndroidTestCoverage = false
            enableUnitTestCoverage = false
            
            // 调试版本不使用混淆
            isMinifyEnabled = false
            isShrinkResources = false
        }
        
        create("beta") {
            initWith(getByName("release"))
            applicationIdSuffix = ".beta"
            versionNameSuffix = "-beta"
            isDebuggable = false
        }
    }
    
    // Phase 4: 测试覆盖率配置
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
        unitTests.all {
            it.useJUnitPlatform()
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

// Phase 4: Jacoco 测试覆盖率任务
tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    
    val fileFilter = listOf(
        "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
        "**/*Test*.*", "android/**/*.*", "**/databinding/*", "**/*Binding.*",
        "**/di/*", "**/injection/*", "**/*Module*.*", "**/*Component*.*"
    )
    
    val debugTree = fileTree("${layout.buildDirectory}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }
    val mainSrc = "${project.projectDir}/src/main/java"
    
    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree("${layout.buildDirectory}") {
        include("**/*.exec", "**/*.ec")
    })
}

dependencies {
    // Core Android & Jetpack
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Koin for Dependency Injection
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // Room for Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Utility
    implementation(libs.timber)
    implementation(libs.junrar)
    implementation("org.apache.commons:commons-compress:1.26.2")
    
    // Crash Reporting & Performance Monitoring (取消注释以启用)
    // Firebase (需要配置 google-services.json)
    // implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    // implementation("com.google.firebase:firebase-crashlytics-ktx")
    // implementation("com.google.firebase:firebase-analytics-ktx")
    // implementation("com.google.firebase:firebase-perf-ktx")
    
    // Alternative: ACRA for crash reporting (开源选项)
    // implementation("ch.acra:acra-core:5.11.3")
    // implementation("ch.acra:acra-mail:5.11.3")
    // implementation("ch.acra:acra-dialog:5.11.3")
    
    // Memory Leak Detection (debug only)
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")

    // Project Modules
    implementation(project(":core:data"))
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
    // 临时注释，待修复
    // implementation(project(":feature:bookshelf"))
    // implementation(project(":feature:reader"))

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(libs.koin.test.junit4)
    testImplementation(libs.robolectric)

    // Android Testing
    androidTestImplementation(libs.junit) // Add base JUnit for @Rule support
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(platform(libs.kotlin.bom))
    androidTestImplementation(libs.koin.test.junit4)
    androidTestImplementation("androidx.startup:startup-runtime:1.1.1")

    // Debug
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// 代码质量检查工具配置

// Detekt 配置
detekt {
    config = files("$rootDir/detekt.yml")
    buildUponDefaultConfig = true
    allRules = false
    
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(false)
        sarif.required.set(false)
    }
}

// ktlint 配置
ktlint {
    version.set("1.0.1")
    debug.set(true)
    verbose.set(true)
    android.set(true)
    outputToConsole.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(false)
    enableExperimentalRules.set(false)
    
    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
        include("**/src/**")
    }
}

// 代码质量检查集成任务
tasks.register("codeQuality") {
    group = "verification"
    description = "运行所有代码质量检查"
    
    dependsOn(
        "detekt",
        "ktlintCheck",
        "jacocoTestReport",
        "lint"
    )
    
    doLast {
        println("✅ 代码质量检查完成")
        println("📊 查看报告:")
        println("  - Detekt: build/reports/detekt/detekt.html")
        println("  - ktlint: 输出在控制台")
        println("  - Jacoco: build/reports/jacoco/jacocoTestReport/html/index.html")
        println("  - Lint: build/reports/lint-results.html")
    }
}

// 自动修复ktlint格式问题
tasks.register("formatCode") {
    group = "formatting"
    description = "自动修复代码格式问题"
    
    dependsOn("ktlintFormat")
    
    doLast {
        println("✨ 代码格式修复完成")
    }
}

// Pre-commit hook 模拟
tasks.register("preCommitCheck") {
    group = "verification"
    description = "提交前的代码检查"
    
    dependsOn(
        "ktlintCheck",
        "detekt",
        "testDebugUnitTest",
        "lint"
    )
    
    doFirst {
        println("🔍 开始提交前检查...")
    }
    
    doLast {
        println("✅ 提交前检查通过")
    }
}
