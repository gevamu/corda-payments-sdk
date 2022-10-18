plugins {
    java
    id("net.corda.plugins.cordformation")
    id("net.corda.plugins.cordapp")
    //id("com.gevamu.kotlin-cordapp-conventions")
}

configurations {
    cordaCordapp.get().isCanBeResolved = true
}

repositories {
    mavenCentral()
    maven("https://software.r3.com/artifactory/corda")
}

dependencies{
    //cordaProvided("net.corda:corda-core:4.9.3")
    cordaRuntimeOnly("net.corda:corda-node-api:4.9")
    cordaRuntimeOnly("net.corda:corda:4.9")

    // logging. Corda 4.9 provides log4j 2.17.1
    cordaProvided("org.apache.logging.log4j:log4j-api:2.17.1")
    cordaProvided("org.apache.logging.log4j:log4j-core:2.17.1")
}
