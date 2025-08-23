plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.dokka) apply false
    
    // 代码质量检查工具
    id("io.gitlab.arturbosch.detekt") version "1.23.4" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0" apply false
    id("org.sonarqube") version "4.4.1.3373" apply false
    id("com.github.ben-manes.versions") version "0.50.0"
    id("org.owasp.dependencycheck") version "9.0.7"
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

// 代码质量检查任务
subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    
    // Detekt 配置
    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        config.setFrom(files("$rootDir/detekt.yml"))
        buildUponDefaultConfig = true
        allRules = false
    }
    
    // ktlint 配置
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
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
        }
    }
}

// 代码质量检查集成任务
tasks.register("codeQualityCheck") {
    group = "verification"
    description = "运行所有代码质量检查"
    
    dependsOn(
        ":app:detekt",
        ":app:ktlintCheck"
    )
    
    doLast {
        println("✅ 代码质量检查完成")
        println("📊 查看报告:")
        println("  - Detekt: app/build/reports/detekt/detekt.html")
        println("  - ktlint: 输出在控制台")
    }
}
