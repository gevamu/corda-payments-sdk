import net.corda.plugins.CordappExtension

plugins {
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

    testImplementation("net.corda:corda-node-driver:4.9.3")
}

configurations.all {
    resolutionStrategy {
        dependencySubstitution {
            all {
                val requested = this.requested
                if (requested is ModuleComponentSelector) {
                    when (requested.group) {
                        "com.fasterxml.jackson", "com.fasterxml.jackson.core" ->
                            useTarget("${requested.group}:${requested.module}:2.13.3")
                        "org.apache.logging.log4j" ->
                            useTarget("${requested.group}:${requested.module}:2.17.1")
                        "org.slf4j" ->
                            useTarget("${requested.group}:${requested.module}:1.7.30")
                    }
                }
            }
            substitute(
                module("com.fasterxml.jackson.module:jackson-module-kotlin")
            ).using(
                module("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.7")
            )
            exclude("ch.qos.logback", "logback-classic")
        }
    }
}

tasks {
    test {
        javaLauncher.set(javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(8))
        })
        minHeapSize = "512m"
        maxHeapSize = "2048m"
    }
    jar {
        // This makes the JAR's SHA-256 hash repeatable.
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    named<Jar>("javadocJar") {
        from(named("dokkaJavadoc"))
    }
}

configure<CordappExtension> {
    targetPlatformVersion(8)
    minimumPlatformVersion(8)
}
