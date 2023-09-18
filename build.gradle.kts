import java.time.LocalDate
import java.time.ZoneId

plugins {
    // Add lifecycle tasks
    base

    // Load Gevamu build support plugins (mostly to have versions in one place)
    id("com.gevamu.build.cordapp-publish-conventions") version "0.1.1" apply false
    id("com.gevamu.build.kotlin-cordapp-conventions") version "0.1.0" apply false

    // Generate api reference index with dokka
    id("org.jetbrains.dokka") version "1.7.20"
}

repositories {
    // Required for dokka
    mavenCentral()
}

group = "com.gevamu.corda"

tasks {
    listOf(clean, check, assemble, build).forEach { task ->
        listOf(
            "payments-app-sample:payments-app-backend",
            "payments-app-sample:payments-app-frontend",
            "gevamu-payment-contracts",
            "gevamu-payment-workflows"
        ).forEach { subproject ->
            task {
                dependsOn("${subproject}:${task.name}")
            }
        }
    }
}

tasks.dokkaHtmlMultiModule.configure {
    val currentYear = LocalDate.now(ZoneId.of("UTC")).year
    pluginsMapConfiguration.put(
        "org.jetbrains.dokka.base.DokkaBase",
        """ { "footerMessage": "Copyright 2022-$currentYear Exactpro Systems Limited" } """,
    )
}
