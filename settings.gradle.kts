pluginManagement {
    includeBuild("build-logic")
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

rootProject.name = "CryptoBook"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":app")
include(":home:domain")
include(":home:data")
include(":home:presentation")
include(":coin-detail:presentation")
include(":settings:presentation")
include(":core:presentation")
include(":core:network")
include(":settings:data")
include(":settings:domain")
include(":core:data")
include(":core:domain")
include(":core:data")
include(":core:designsystem")
include(":main:presentation")
include(":main:data")
include(":main:domain")
include(":splash:presentation")
include(":splash:data")
include(":splash:domain")
include(":coin-detail:domain")
include(":coin-detail:data")
include(":search:presentation")
include(":search:domain")
include(":watchlist:domain")
include(":watchlist:presentation")
include(":diary:presentation")
