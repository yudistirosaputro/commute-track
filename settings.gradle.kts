pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
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

rootProject.name = "commuteTrack"
include(":app")
include(":core:common")
include(":core:domain")
include(":core:data")
include(":core:ui")
include(":core:network")
include(":core:database")
include(":feature:dashboard")
include(":feature:tracking")
include(":feature:history")
include(":feature:statistics")
include(":feature:settings")
