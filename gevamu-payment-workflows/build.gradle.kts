import com.gevamu.build.Cordapp_publish_conventions_gradle.LicenseType
import java.time.LocalDate
import java.time.ZoneId

plugins {
    // Include Kotlin Common Conventions
    id("com.gevamu.build.kotlin-common-conventions")
    // Apply common settings for cordapps.
    id("com.gevamu.build.kotlin-cordapp-conventions")
    // Publish to mvn
    id("com.gevamu.build.cordapp-publish-conventions")
}

group = rootProject.group

dependencies {
    cordapp(project(":gevamu-payment-contracts"))

    // Corda 4 provides 2.3.1
    api("javax.xml.bind:jaxb-api:2.3.1")
}

cordapp {
    workflow {
        name("Gevamu Payment Workflows")
        vendor("Exactpro Systems LLC")
        licence("Apache License, Version 2.0")
        versionId(1)
    }
}

cordappPublishing {
    name.set("Gevamu Payment Workflows")
    description.set("Gevamu Payment for Corda Workflows CorDapp")
    vcsUrl.set("https://github.com/gevamu/corda-payments-sdk")
    license.set(LicenseType.APACHE_2)
}

tasks.dokkaHtmlPartial.configure {
    val currentYear = LocalDate.now(ZoneId.of("UTC")).year
    pluginsMapConfiguration.put(
        "org.jetbrains.dokka.base.DokkaBase",
        """ { "footerMessage": "Copyright 2022-$currentYear Exactpro Systems Limited" } """,
    )
}
