import net.corda.plugins.Cordform
// Variable to check corda jar like in cordformation source code
import org.gradle.api.plugins.JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME

plugins {
    java
    id("net.corda.plugins.cordformation")
    //id("net.corda.plugins.cordapp")
    id("com.gevamu.kotlin-cordapp-conventions")
}

configurations {
    cordaCordapp.get().isCanBeResolved = true
}

repositories {
    mavenCentral()
    maven("https://software.r3.com/artifactory/corda")
}

//dependencies{
//    cordaProvided("net.corda:corda-node-api:4.9.3")
//
//    cordaRuntimeOnly("net.corda:corda:4.9.3")
//
//    // logging. Corda 4.9 provides log4j 2.17.1
//    cordaProvided("org.apache.logging.log4j:log4j-api:2.17.1")
//    cordaProvided("org.apache.logging.log4j:log4j-core:2.17.1")
//}

tasks.register<Cordform>("deployNodes") {
    // Check corda jar like in cordformation source code
    // FIXME: Corda JAR is not found while it exists
    project.logger.info("check_corda_jar")
    project.logger.info(RUNTIME_CLASSPATH_CONFIGURATION_NAME)
    val cordaPath = project.configurations.getByName(RUNTIME_CLASSPATH_CONFIGURATION_NAME)
    project.logger.info(cordaPath
        .filter { it.toString().contains("\\Qcorda\\E(-enterprise)?-\\Q4.9.3\\E(-.+)?\\.jar\$".toRegex()) }
        .joinToString{it.toString()})
    node {
        name("PaymentGateway")
        p2pAddress("0.0.0.0")
        p2pPort(10001)
        cordapp(project(":xflows"))

    }
}
