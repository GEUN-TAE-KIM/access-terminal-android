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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Mobile_Access_Control"

include(":app")
include(":core")
include(":core:database")
include(":core:network")
include(":component:auth")
include(":component:access")
include(":component:scan")
include(":component:nfc")
include(":component:master")
include(":component:sync")
include(":component:history")
include(":component:stats")
include(":feature:common")
include(":feature:login")
include(":feature:scan")
include(":feature:register")
include(":feature:history")
include(":feature:stats")
