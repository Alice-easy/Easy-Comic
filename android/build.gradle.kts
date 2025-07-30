plugins {
    id("com.android.application") version "8.6.0" apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
    id("com.google.firebase.crashlytics") version "3.0.0" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.aliyun.com/repository/public")
    }
    afterEvaluate {
        if (project.name == "file_picker") {
            project.tasks.findByName("lintVitalAnalyzeRelease")?.enabled = false
        }
    }
}

val newBuildDir: Directory = rootProject.layout.buildDirectory.dir("../../build").get()
rootProject.layout.buildDirectory.value(newBuildDir)

subprojects {
    val newSubprojectBuildDir: Directory = newBuildDir.dir(project.name)
    project.layout.buildDirectory.value(newSubprojectBuildDir)
}
subprojects {
    project.evaluationDependsOn(":app")
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
