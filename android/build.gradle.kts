// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.7.2" apply false
    id("org.jetbrains.kotlin.android") version "2.1.20" apply false
    // Kotlin 2.0+ moved the Compose compiler out of the Kotlin distribution
    // and into this separate Gradle plugin (needed once we're past 1.9.x,
    // which androidx.tv:tv-material's Kotlin 2.1 metadata requires).
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20" apply false
}
