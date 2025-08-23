plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)

    

    

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
    implementation(project(":core:data"))
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
    implementation(project(":feature:bookshelf"))
    implementation(project(":feature:reader"))


}


