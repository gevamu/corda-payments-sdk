plugins {
    id("net.corda.plugins.cordformation")
}

// XXX or by project() ?
val corda_release_version: String by extra("4.9.3")

repositories {
    mavenCentral()
    maven("https://software.r3.com/artifactory/corda")
    maven("https://repo.gradle.org/gradle/libs-releases-local")
    maven("https://jitpack.io")
}

dependencies {
    corda("net.corda:corda:4.9.3")
    cordaBootstrapper("net.corda:corda-node-api:4.9.3")
}

configurations.all {
    resolutionStrategy {
        dependencySubstitution {
            // caffein has 2 variants: runtimeElements and shadowRuntimeElements.
            // We need to help gradle choosing one of them
            substitute(
                module("com.github.ben-manes.caffeine:caffeine:2.9.3")
            ).using(
                variant(module("com.github.ben-manes.caffeine:caffeine:2.9.3")) {
                    attributes {
                        // Chose runtimeElements (external dependencies)
                        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
                    }
                }
            )
        }
    }
}
