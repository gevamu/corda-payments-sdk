plugins {
    // Add lifecycle tasks
    base
    // Kotlin plugin should be loaded only once
    id("com.gevamu.kotlin-common-conventions") apply false
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
