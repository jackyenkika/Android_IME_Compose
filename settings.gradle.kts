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

rootProject.name = "IMEService"

include(":app", ":kika-t9-engine-library", ":kika-pinyin-engine-library")
project(":kika-t9-engine-library").projectDir =
    File(rootDir, "Android_Kika_T9_Engine_Library/kika-t9-engine-library")

project(":kika-pinyin-engine-library").projectDir =
    File(
        rootDir,
        "Android_Kika_T9_Engine_Library/Android_Kika_Pinyin_Engine_Library/kika-pinyin-engine-library"
    )