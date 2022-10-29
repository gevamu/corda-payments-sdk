plugins {
    // Include Kotlin Common Conventions
    id("com.gevamu.kotlin-common-conventions")

    // Apply common settings for cordapps
    id("com.gevamu.kotlin-cordapp-conventions")

    id("com.intershop.gradle.jaxb") version "5.2.1"
}

group = rootProject.group

dependencies {
    // Corda 4 provides 2.3.1
    jaxb("javax.xml.bind:jaxb-api:2.3.1")
    jaxb("javax.activation:activation:1.1.1")
    jaxb("com.sun.xml.bind:jaxb-xjc:2.3.1")
    jaxb("com.sun.xml.bind:jaxb-impl:2.3.1")
    jaxb("com.sun.xml.bind:jaxb-core:2.3.0.1")
    jaxb("org.glassfish.jaxb:jaxb-runtime:2.3.1")
}

jaxb {
    javaGen {
        register("iso20022pain") {
            schema = file("src/main/xsd/pain.001.001.09.xsd")
            packageName = "com.gevamu.iso20022.pain"
        }
        register("iso20022head") {
            schema = file("src/main/xsd/head.001.001.03.xsd")
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
