plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    id("jacoco") // Phase 4: 测试覆盖率
}

android {
    namespace = "com.easycomic"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.easycomic"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            enableAndroidTestCoverage = true
            enableUnitTestCoverage = true
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

    // Project Modules
    implementation(project(":data"))
    implementation(project(":domain"))
    implementation(project(":ui_bookshelf"))
    implementation(project(":ui_reader"))

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
