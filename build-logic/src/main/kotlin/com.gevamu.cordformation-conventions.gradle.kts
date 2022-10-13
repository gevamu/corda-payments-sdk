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
        p2pAddress("0.0.0.0")
        p2pPort(10001)
        cordapp(project(":xflows"))
    }
}
