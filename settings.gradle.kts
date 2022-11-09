pluginManagement {
    repositories {
        // Use the plugin portal to apply community plugins in convention plugins.
        gradlePluginPortal()

        // Corda plugins v5.1.0 aren't published to the Gradle Plugin Portal :-(
        maven("https://software.r3.com/artifactory/corda")
    }

    includeBuild("build-logic")
}

rootProject.name = "corda-payments-sdk"
include("contracts", "workflows", "web-server")
