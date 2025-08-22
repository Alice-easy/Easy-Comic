pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Easy-Comic"

include(":app")
include(":domain")
include(":data")
include(":ui_bookshelf")
include(":ui_reader")
include(":ui_di")
