import java.time.LocalDate
import java.time.ZoneId

plugins {
    // Add lifecycle tasks
    base
    // Kotlin plugin should be loaded only once
    id("com.gevamu.kotlin-common-conventions") apply false
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
            "payments-contracts",
            "payments-workflows"
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
        """ { "footerMessage": "Copyright ${if (2022 == currentYear) currentYear else "2022-$currentYear"} Exactpro Systems Limited" } """,
    )
}
