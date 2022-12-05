plugins {
    // Include Kotlin Common Conventions
    id("com.gevamu.kotlin-common-conventions")

    // Apply common settings for cordapps
    id("com.gevamu.kotlin-cordapp-conventions")
}

group = rootProject.group

buildscript {
    dependencies {
        project(":license-kotlinter-rule")
    }
}

kotlinter {
    ignoreFailures = true
    indentSize = 4
    reporters = arrayOf("checkstyle", "plain")
    experimentalRules = true
    disabledRules = emptyArray<String>()
}

cordapp {
    contract {
        name("Payment Contracts")
        vendor("Exactpro Systems LLC")
        licence("Apache License, Version 2.0")
        versionId(1)
    }
}
