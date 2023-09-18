import com.gevamu.build.Cordapp_publish_conventions_gradle.LicenseType
import java.time.LocalDate
import java.time.ZoneId

plugins {
    // Apply common settings for cordapps
    id("com.gevamu.build.kotlin-cordapp-conventions")
    // Publish to maven
    id("com.gevamu.build.cordapp-publish-conventions")
}

group = rootProject.group

cordapp {
    contract {
        name("Gevamu Payment Contracts")
        vendor("Exactpro Systems LLC")
        licence("Apache License, Version 2.0")
        versionId(1)
    }
}

cordappPublishing {
    name.set("Gevamu Payment Contracts")
    description.set("Gevamu Payment for Corda Contracts CorDapp")
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
