pluginManagement {
    repositories {
        google()
        maven("https://maven.google.com")
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven("https://maven.google.com")
        mavenCentral()
    }
}
rootProject.name = "ScannerPlugin"
include(":app")
