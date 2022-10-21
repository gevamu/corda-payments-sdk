plugins {
    // Include Java Common Conventions
    id("com.gevamu.java-common-conventions")

    // Apply the org.jetbrains.kotlin.jvm plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm")

    // Apply the org.jetbrains.kotlin.jvm plugin to add support for Kotlin stylechecking.
    id("org.jmailen.kotlinter")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
        javaParameters = true   // Useful for reflection.
    }
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    // TODO Corda provides older version of stdlib. Is it possible to use sdlib version earlier than kotlin version?
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // logging
    // implementation("org.apache.logging.log4j:log4j-api-kotlin:1.2.0")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}
