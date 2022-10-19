import net.corda.plugins.Cordform

plugins {
    //java
    id("net.corda.plugins.cordformation")
    // Cordapp plugin provides cordaRuntineOnly() method
    id("net.corda.plugins.cordapp")
    //id("com.gevamu.kotlin-cordapp-conventions")
}

configurations {
    // Unless Cordapp can be resolved, deployNodes task can't be created
    cordaCordapp {
        isCanBeResolved = true
    }
}

val corda_group: String by project
val corda_bundle_version: String by project
val corda_release_version: String by project

val cordaCPK by configurations.creating

repositories {
    mavenCentral()
    maven("https://software.r3.com/artifactory/corda")
}

dependencies{
    //cordaProvided("net.corda:corda-core:4.9.3")
    cordaRuntimeOnly("net.corda:corda-node-api:4.9")
    cordaRuntimeOnly("net.corda:corda:4.9")
    cordapp(project(":xflows"))

    // logging. Corda 4.9 provides log4j 2.17.1
//    cordaProvided("org.apache.logging.log4j:log4j-api:2.17.1")
//    cordaProvided("org.apache.logging.log4j:log4j-core:2.17.1")
}

val cpk = tasks.register<Jar>("cpk") {
    archiveBaseName.set("locally-built")
    archiveClassifier.set("cordapp")
    archiveExtension.set("cpk")
}

artifacts {
    add("cordaCPK", cpk)
}

tasks.register<Cordform>("deployNodes") {
    dependsOn.add("cpk")
    // FIXME: Corda JAR is not found while it exists
    checkCordaJAR()
    node {
        name("PaymentGateway")
        p2pAddress("0.0.0.0")
        p2pPort(10001)
        cordapp(project(":xflows"))
    }
}

// Check corda jar like in cordformation source code
fun checkCordaJAR() {
    project.logger.info("check_corda_jar")
    project.logger.info(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME)
    val cordaPath = project.configurations.getByName(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME)
    project.logger.info(cordaPath
        .filter { it.toString().contains("\\Qcorda\\E(-enterprise)?-\\Q4.9\\E(-.+)?\\.jar\$".toRegex()) }
        .joinToString{it.toString()})
}
