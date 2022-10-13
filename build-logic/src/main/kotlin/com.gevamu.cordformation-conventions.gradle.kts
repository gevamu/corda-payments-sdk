import gradle.kotlin.dsl.accessors._129ec4e424400dc84d7a193e170168bb.cordaCordapp
import net.corda.plugins.Cordform
import org.gradle.api.plugins.JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME

plugins {
    java
    id("net.corda.plugins.cordformation")
    id("net.corda.plugins.cordapp")
}

configurations {
    cordaCordapp.get().isCanBeResolved = true
}

repositories {
    mavenCentral()
    maven("https://software.r3.com/artifactory/corda")
}

dependencies{
    cordaProvided("net.corda:corda-node-api:4.9.3")

    cordaRuntimeOnly("net.corda:corda:4.9.3")

    // logging. Corda 4.9 provides log4j 2.17.1
    cordaProvided("org.apache.logging.log4j:log4j-api:2.17.1")
    cordaProvided("org.apache.logging.log4j:log4j-core:2.17.1")
}

tasks.register<Cordform>("deployNodes") {
    project.logger.info("custom_message")
    project.logger.info(RUNTIME_CLASSPATH_CONFIGURATION_NAME)
    val cordaPath = project.configurations.getByName(RUNTIME_CLASSPATH_CONFIGURATION_NAME)
    project.logger.info(cordaPath.filter { it.toString().contains("corda-4.9.3.jar") }.joinToString{it.toString()})
    node {
        name("PaymentGateway")
        p2pAddress("0.0.0.0")
        p2pPort(10001)
        cordapp(project(":xflows"))

    }
}
