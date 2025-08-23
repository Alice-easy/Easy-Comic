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
include(":core:domain")
include(":core:data")
include(":core:ui")
include(":core:common")
include(":feature:bookshelf")
include(":feature:reader")
