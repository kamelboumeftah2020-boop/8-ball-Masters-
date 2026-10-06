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
        // Same content as Maven Central; used when the main host rate-limits.
        maven("https://repo1.maven.org/maven2")
    }
}
rootProject.name = "Fluently"
include(":app")
