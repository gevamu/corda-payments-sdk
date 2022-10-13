import net.corda.plugins.Cordform

plugins {
    id("net.corda.plugins.cordformation")
}


tasks.register<Cordform>("deployNodes") {
    // etc
    node {
        name("O=Notary,L=London,C=GB")

    }
}
