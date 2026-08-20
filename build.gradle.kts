plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
}

buildscript {
    dependencies {
        // AGP 9 uses built-in Kotlin. Pin KGP/KSP so Compose, Room processing,
        // and built-in Kotlin resolve supported toolchain releases.
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.21")
        classpath("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.3.10")
    }
}
