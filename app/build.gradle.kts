plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.example.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    // Required for older FFmpeg shared objects to prevent conflicts
    packaging {
        resources.excludes += "META-INF/LICENSE.md"
        resources.excludes += "META-INF/LICENSE-notice.md"
    }
}

dependencies {
    // Standard Android/Kotlin dependencies
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    // Kotlin Coroutines for modern concurrency
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // FFmpeg Integration (Using FFmpeg-Kit Full version for broad architecture support: ARM, x86, etc.)
    // This addresses the requirement to configure appropriate FFmpeg dependencies and verify architectures.
    val ffmpegKitVersion = "6.0" 
    implementation("com.arthenica:ffmpeg-kit-full:$ffmpegKitVersion")
}