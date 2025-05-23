pluginManagement {
    repositories {
        google() // 🔥 это должен быть просто google(), без фильтров!
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

rootProject.name = "Weekend Guide"
include(":app")
