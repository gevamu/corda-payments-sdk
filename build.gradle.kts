plugins {
    // Add lifecycle tasks
    base
    // Kotlin plugin should be loaded only once
    id("com.gevamu.kotlin-common-conventions") apply false
    // Generate api reference with dokka
    id("org.jetbrains.dokka") version "1.7.20"
}

// Apply dokka to subprojects and dokka dependencies
allprojects {
    repositories {
        mavenCentral()
    }

    apply(plugin = "org.jetbrains.dokka")

    dependencies {
        dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.7.20")
    }
}

group = "com.gevamu.corda"

tasks {
    listOf(clean, check, assemble, build).forEach { task ->
        listOf(
            "payments-app-sample:payments-app-backend",
            "payments-app-sample:payments-app-frontend",
            "payments-contracts",
            "payments-workflows"
        ).forEach { subproject ->
            task {
                dependsOn("${subproject}:${task.name}")
            }
        }
    }
}
