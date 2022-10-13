import gradle.kotlin.dsl.accessors._027823db16a7648d7c1bfd0b52d83d37.javaToolchains
import gradle.kotlin.dsl.accessors._027823db16a7648d7c1bfd0b52d83d37.test
import net.corda.plugins.CordappExtension

plugins {
    // Include Kotlin Common Conventions
    id("com.gevamu.kotlin-common-conventions")

    // Include Java-library Conventions
    id("com.gevamu.java-library-conventions")

    // Corda plugins
    id("net.corda.plugins.cordapp")
    //id("net.corda.plugins.cordformation")
    id("net.corda.plugins.quasar-utils")
}

repositories {
    maven("https://software.r3.com/artifactory/corda")
    maven("https://repo.gradle.org/gradle/libs-releases-local")
    maven("https://jitpack.io")
}

dependencies {
    // Corda
    cordaProvided("net.corda:corda-core:4.9.3")
//    cordaProvided("net.corda:corda-node-api:4.9.3")
//
//    cordaRuntimeOnly("net.corda:corda:4.9.3")
//
//    // logging. Corda 4.9 provides log4j 2.17.1
//    cordaProvided("org.apache.logging.log4j:log4j-api:2.17.1")
//    cordaProvided("org.apache.logging.log4j:log4j-core:2.17.1")
//
    testImplementation("net.corda:corda-node-driver:4.9.3")
}

tasks {
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

tasks.test {
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(8))
    })
}

//quasar {
//    excludePackages.addAll(
//        "org.junit**",
//        "org.gradle**",
//        "org.apache**",
//        "org.slf4j**",
//        "org.jboss**",
//        "worker.org.gradle**",
//        "com.lmax**",
//        "kotlin**",
//        "com.esotericsoftware**",
//        "nonapi.io**", // nonapi.io.github.classgraph
//        "io.github.classgraph**",
//        "com.google**",
////        "net.corda**",
//        "net.corda.testing**",
//        "net.corda.core**", // ???
////        "net.corda.node**", // ???
//        "net.corda.nodeapi**", // ???
//        "net.corda.serialization**", // ???
//        "net.corda.notary**", // ???
//        "org.bouncycastle**",
//        "org.w3c**",
//        "rx**",
//        "com.fasterxml**",
//        "netscape**",
//        "junit**",
//        "org.xml**",
//        "org.jcp**",
//        "net.i2p**",
//        "org.objectweb**",
//        "groovy**",
//        "net.rubygrapefruit**",
//        "net.bytebuddy**",
//        "org.mockito**",
//        "org.h2**",
//        "org.hibernate**",
//        "com.github.benmanes**",
//        "com.codahale**",
//        "liquibase**",
//        "com.typesafe**",
//        "org.objenesis**",
//        "com.nhaarman**",
//        "io.netty**",
//        "co.paralleluniverse**", // it's quasar itsefl...
//        "com.zaxxer**",
//        "org.yaml**",
//        "javax**",
//        "antlr**",
//        "org.dom4j**",
//        "de.javakaffee**",
//        "com.gevamu.flows.PaymentFlowTests"
//    )
//}
