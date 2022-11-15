plugins {
    id("com.gevamu.kotlin-common-conventions")
    id("com.gevamu.kotlin-cordapp-conventions")
}

group = rootProject.group

dependencies {
    cordapp(project(":contracts"))
    cordapp(project(":workflows"))
    cordapp(project(":payments-app:app-contracts"))
    api("javax.xml.bind:jaxb-api:2.3.1")
}

cordapp {
    contract {
        name("Payments Application Workflows")
        vendor("Exactpro Systems LLC")
        licence("Apache License, Version 2.0")
        versionId(1)
    }
}
