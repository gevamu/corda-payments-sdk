import net.corda.plugins.Cordform

plugins {
    id("net.corda.plugins.cordformation")
}

repositories {
    mavenCentral()
}

setProperty("corda_release_version", "5.1.0")

tasks.register<Cordform>("deployNodes") {
    // etc
    node {
        name("PaymentGateway")
        p2pAddress("0.0.0.0")
        p2pPort(10001)
        cordapp(project(":xflows"))
    }
}
