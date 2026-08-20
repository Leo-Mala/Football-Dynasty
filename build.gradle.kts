plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
}

buildscript {
    dependencies {
        // AGP 9 uses built-in Kotlin. Pin the KGP runtime so the Compose
        // compiler plugin and built-in Kotlin resolve the same release.
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.21")
    }
}
