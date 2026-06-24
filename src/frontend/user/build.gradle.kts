import com.diffplug.gradle.spotless.SpotlessExtension
import io.gitlab.arturbosch.detekt.extensions.DetektExtension

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.buildkonfig) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.kotlinComposeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.versions)
}

val sqliteTmpDir = file(".gradle-local/sqlite").apply { mkdirs() }
val buildTmpDir = file(".gradle-local/tmp").apply { mkdirs() }

System.setProperty("org.sqlite.tmpdir", sqliteTmpDir.absolutePath)
System.setProperty("java.io.tmpdir", buildTmpDir.absolutePath)

allprojects {
    group = "com.scapes"
    version = "0.1.0"
}

subprojects {
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    extensions.configure<DetektExtension> {
        buildUponDefaultConfig = true
        config.setFrom(rootProject.files("detekt.yml"))
        parallel = true
    }

    extensions.configure<SpotlessExtension> {
        kotlin {
            target("src/**/*.kt")
            ktfmt(libs.versions.ktfmt.get()).kotlinlangStyle()
            trimTrailingWhitespace()
            endWithNewline()
        }

        kotlinGradle {
            target("*.gradle.kts")
            ktfmt(libs.versions.ktfmt.get()).kotlinlangStyle()
            trimTrailingWhitespace()
            endWithNewline()
        }
    }
}
