plugins {
    // Support convention plugins written in Kotlin. Convention plugins are build scripts in 'src/main' that automatically become available as plugins in the main build.
    `kotlin-dsl`
}

repositories {
    // Use the plugin portal to apply community plugins in convention plugins.
    gradlePluginPortal()

    // Corda plugins v5.1.0 aren't published to the Gradle Plugin Portal :-(
    maven("https://software.r3.com/artifactory/corda")
}

dependencies {
    // TODO pass kotlin version to conventions plugins
    implementation(gradlePlugin("org.jetbrains.kotlin.jvm", "1.7.20"))

    // Kotlin style checker
    implementation(gradlePlugin("org.jmailen.kotlinter", "3.12.0"))

    // Corda plugins
    implementation(gradlePlugin("net.corda.plugins.cordapp", "5.1.0"))
    implementation(gradlePlugin("net.corda.plugins.cordformation", "5.1.0"))
    implementation(gradlePlugin("net.corda.plugins.quasar-utils", "5.1.0"))
}

fun gradlePlugin(id: String, version: String) = "$id:$id.gradle.plugin:$version"
