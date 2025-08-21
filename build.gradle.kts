// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.dokka) apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.4"
}

// 应用Dokka配置
apply(from = "dokka-simple.gradle.kts")

// Detekt 配置
detekt {
    toolVersion = "1.23.4"
    config.setFrom(file("detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false
    
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
        md.required.set(true)
    }
}

// 为所有子项目应用 Detekt
subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    
    detekt {
        toolVersion = "1.23.4"
        config.setFrom(rootProject.file("detekt.yml"))
        buildUponDefaultConfig = true
        
        reports {
            html.required.set(true)
            xml.required.set(true)
        }
    }
    
    dependencies {
        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.4")
    }
}

// 代码质量检查任务
tasks.register("codeQualityCheck") {
    group = "verification"
    description = "运行所有代码质量检查工具"
    dependsOn("detekt", "lint")
}
