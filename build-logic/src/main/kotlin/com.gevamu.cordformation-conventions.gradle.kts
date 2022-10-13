import net.corda.plugins.Cordform

plugins {
    id("net.corda.plugins.cordformation")
}

repositories {
    mavenCentral()
}

tasks.register<Cordform>("deployNodes") {
    // etc
    node {
        name("PaymentGateway")
        p2pAddress("10.100.84.103")
        p2pPort(10001)
    }
}
