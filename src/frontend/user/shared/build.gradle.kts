import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinComposeCompiler)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.kover)
    alias(libs.plugins.sqldelight)
}

val sqliteTmpDir = rootProject.file(".gradle-local/sqlite").apply { mkdirs() }
val buildTmpDir = rootProject.file(".gradle-local/tmp").apply { mkdirs() }

System.setProperty("org.sqlite.tmpdir", sqliteTmpDir.absolutePath)

System.setProperty("java.io.tmpdir", buildTmpDir.absolutePath)

kotlin {
    compilerOptions { freeCompilerArgs.add("-Xexpect-actual-classes") }

    androidTarget { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }

    jvm("desktop") { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.components.resources)
            implementation(compose.foundation)
            implementation(compose.materialIconsExtended)
            implementation(compose.material3)
            implementation(compose.runtime)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            implementation(libs.kermit)
            implementation(libs.koin.compose)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.io.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.coroutines)
            implementation(libs.sqldelight.runtime)
            implementation(libs.voyager.navigator)
        }

        commonTest.dependencies {
            implementation(libs.koin.test)
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.turbine)
        }

        androidMain.dependencies {
            implementation(libs.androidx.security.crypto)
            implementation(libs.firebase.messaging)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.sqldelight.android.driver)
        }

        androidUnitTest.dependencies { implementation(libs.mockk) }

        val desktopMain by getting {
            dependencies {
                implementation(libs.jna.platform)
                implementation(libs.ktor.client.cio)
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.sqldelight.sqlite.driver)
            }
        }
    }
}

android {
    namespace = "com.scapes.shared"
    compileSdk = 36

    defaultConfig { minSdk = 26 }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
        disable += "NullSafeMutableLiveData"
    }
}

sqldelight {
    databases { create("ScapesDatabase") { packageName.set("com.scapes.data.local.db") } }
}

tasks.withType<app.cash.sqldelight.gradle.VerifyMigrationTask>().configureEach {
    // sqlite-jdbc extraction is failing on this Windows setup before verification can begin.
    enabled = false
}
