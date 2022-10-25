plugins {
    // Apply common settings for cordapps.
    id("com.gevamu.kotlin-cordapp-conventions")

    id("com.intershop.gradle.jaxb") version "5.2.1"
}

group = rootProject.group

dependencies {
    // TODO for some reason JAXB plugin doesn't add it automatically (should it?)
//    api("com.sun.xml.bind:jaxb-core:3.0.1")
    api("jakarta.xml.bind:jakarta.xml.bind-api:3.0.1")
}

jaxb {
    javaGen {
        register("iso20022pain") {
            schema = file("src/main/resources/com/gevamu/iso20022/schema/xsd/pain.001.001.11.xsd")
            packageName = "com.gevamu.iso20022.pain"
        }
        register("iso20022head") {
            schema = file("src/main/resources/com/gevamu/iso20022/schema/xsd/head.001.001.03.xsd")
            packageName = "com.gevamu.iso20022.head"
        }
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
