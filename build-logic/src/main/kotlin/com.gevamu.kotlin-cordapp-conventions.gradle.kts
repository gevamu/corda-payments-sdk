import net.corda.plugins.CordappExtension

plugins {
    // Include Kotlin Common Conventions
    id("com.gevamu.kotlin-common-conventions")

    // Include Java-library Conventions
    id("com.gevamu.java-library-conventions")

    // Corda plugins
    id("net.corda.plugins.quasar-utils")
    id("net.corda.plugins.cordapp")
}

repositories {
    maven("https://software.r3.com/artifactory/corda")
    maven("https://repo.gradle.org/gradle/libs-releases-local")
    maven("https://jitpack.io")
}

dependencies {
    // Corda
    cordaProvided("net.corda:corda-core:4.9.3")
    cordaProvided("net.corda:corda-node-api:4.9.3")

    cordaRuntimeOnly("net.corda:corda:4.9.3")

    // logging. Corda 4.9 provides log4j 2.17.1
    cordaProvided("org.apache.logging.log4j:log4j-api:2.17.1")
    cordaProvided("org.apache.logging.log4j:log4j-core:2.17.1")

    testImplementation("net.corda:corda-node-driver:4.9.3")
}

tasks {
    test {
        javaLauncher.set(javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(8))
        })
    }
    jar {
        // This makes the JAR's SHA-256 hash repeatable.
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}

configure<CordappExtension> {
    targetPlatformVersion(8)
    minimumPlatformVersion(8)
}
