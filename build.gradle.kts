buildscript {
    dependencies {
        classpath(files("libs/license-kotlinter-rule-0.1.2.jar"))
    }
}

plugins {
    // Add lifecycle tasks
    base
    // Kotlin plugin should be loaded only once
    id("com.gevamu.kotlin-common-conventions") apply false
}

group = "com.gevamu"

tasks.clean {
    dependsOn("contracts:$name", "workflows:$name")
}

tasks.check {
    dependsOn("contracts:$name", "workflows:$name")
}

tasks.assemble {
    dependsOn("contracts:$name", "workflows:$name")
}

tasks.build {
    dependsOn("contracts:$name", "workflows:$name")
}
