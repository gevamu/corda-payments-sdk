plugins {
    // Include Kotlin Common Conventions
    id("com.gevamu.kotlin-common-conventions")
    // Apply common settings for cordapps.
    id("com.gevamu.kotlin-cordapp-conventions")
}

group = rootProject.group

dependencies {
    cordapp(project(":payments-contracts"))

    // Corda 4 provides 2.3.1
    api("javax.xml.bind:jaxb-api:2.3.1")
}

cordapp {
    contract {
        name("Payment Workflows")
        vendor("Exactpro Systems LLC")
        licence("Apache License, Version 2.0")
        versionId(1)
    }
}
