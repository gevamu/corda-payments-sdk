plugins {
    // Support convention plugins written in Kotlin.
    `kotlin-dsl`
}

repositories {
    // Use the plugin portal to apply community plugins in convention plugins.
    gradlePluginPortal()

    // Corda plugins v5.1.0 aren't published to the Gradle Plugin Portal :-(
    maven("https://software.r3.com/artifactory/corda")
}

dependencies {
    // Corda plugins
    implementation(gradlePlugin("net.corda.plugins.quasar-utils", "5.1.0"))
    implementation(gradlePlugin("net.corda.plugins.cordapp", "5.1.0"))
    implementation(gradlePlugin("net.corda.plugins.cordformation", "5.1.0"))
}

fun gradlePlugin(id: String, version: String) = "$id:$id.gradle.plugin:$version"
