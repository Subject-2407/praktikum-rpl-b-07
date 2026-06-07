import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinComposeCompiler)
    alias(libs.plugins.kover)
}

android {
    namespace = "com.scapes.android"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.scapes.android"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.runtime)
    implementation(compose.ui)
    implementation(libs.androidx.activity.compose)
}
