// Top-level build file for Kotlin DSL.
// Defines Gradle plugins for the entire project (Android + Firebase).

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        // Android Gradle Plugin
        classpath("com.android.tools.build:gradle:8.1.0")
    }
}

// ✅ Do NOT add 'allprojects { repositories { ... } }' here —
// repositories are defined in settings.gradle.kts now.
