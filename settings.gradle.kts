pluginManagement {
    repositories {
        gradlePluginPortal()

        // Repository with Gevamu build support plugins
        maven {
            name = "buildPluginPackages"
            url = uri("https://maven.pkg.github.com/gevamu/gradle-convention-plugins")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?:
                    providers.gradleProperty("com.gevamu.build.githubActor").orNull
                password = System.getenv("GITHUB_TOKEN") ?:
                    providers.gradleProperty("com.gevamu.build.githubToken").orNull
            }
        }
    }

    if (java.nio.file.Files.isDirectory(java.nio.file.Paths.get("../gradle-convention-plugins"))) {
        includeBuild("../gradle-convention-plugins")
    }
}

rootProject.name = "corda-payments-sdk"
include(
    "gevamu-payment-contracts",
    "gevamu-payment-workflows",
    "payments-app-sample:payments-app-backend",
    "payments-app-sample:payments-app-frontend",
)
