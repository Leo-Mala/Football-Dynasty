plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
}

buildscript {
    dependencies {
        // AGP 9 uses built-in Kotlin. This explicitly raises the KGP runtime
        // so the Compose compiler plugin can use the selected Kotlin release.
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.10")
    }
}
