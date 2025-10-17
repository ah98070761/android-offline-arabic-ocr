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
        google()  // ضروري لمكتبات ML Kit و Android
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // إذا استخدمت مكتبات من GitHub
    }
}

rootProject.name = "OfflineArabicOcrApp"
include(":app")