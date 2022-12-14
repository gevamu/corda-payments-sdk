plugins {
    // Add lifecycle tasks
    base
    // Kotlin plugin should be loaded only once
    id("com.gevamu.kotlin-common-conventions") apply false
}

group = "com.gevamu.corda"

tasks.clean {
    dependsOn("payments-contracts:$name", "payments-workflows:$name")
}

tasks.check {
    dependsOn("payments-contracts:$name", "payments-workflows:$name")
}

tasks.assemble {
    dependsOn("payments-contracts:$name", "payments-workflows:$name")
}

tasks.build {
    dependsOn("payments-contracts:$name", "payments-workflows:$name")
}
