plugins {
    id("com.gevamu.kotlin-common-conventions")
    id("com.gevamu.kotlin-cordapp-conventions")
}

group = rootProject.group

dependencies {
    cordapp(project(":contracts"))
}

cordapp {
    contract {
        name("Payments Application Contracts")
        vendor("Exactpro Systems LLC")
        licence("Apache License, Version 2.0")
        versionId(1)
    }
}
