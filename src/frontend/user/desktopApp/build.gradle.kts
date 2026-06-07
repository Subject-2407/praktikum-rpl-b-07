import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinComposeCompiler)
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kover)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.runtime)
}

compose.desktop {
    application {
        mainClass = "com.scapes.desktop.MainKt"

        nativeDistributions {
            packageName = "Scapes"
            packageVersion = "0.1.0"
            targetFormats(TargetFormat.Exe, TargetFormat.Msi)
        }
    }
}
