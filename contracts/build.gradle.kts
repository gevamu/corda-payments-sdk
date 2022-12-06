plugins {
    // Include Kotlin Common Conventions
    id("com.gevamu.kotlin-common-conventions")

    // Apply common settings for cordapps
    id("com.gevamu.kotlin-cordapp-conventions")
}

group = rootProject.group

kotlinter {
    ignoreFailures = false
}

tasks {
    compileKotlin {
        dependsOn(lintKotlin)
    }
}

cordapp {
    contract {
        name("Payment Contracts")
        vendor("Exactpro Systems LLC")
        licence("Apache License, Version 2.0")
        versionId(1)
    }
}
