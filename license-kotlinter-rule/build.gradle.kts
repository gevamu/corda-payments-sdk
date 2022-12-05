plugins {
    kotlin("jvm")
}

group = rootProject.group
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    // klint core matches kotlinter plugin's one at v3.0.2
    // https://github.com/jeremymailen/kotlinter-gradle/blob/3.0.2/build.gradle.kts
    implementation("com.pinterest.ktlint:ktlint-core:0.38.1")
    testImplementation("com.pinterest.ktlint:ktlint-core:0.38.1")
    testImplementation("com.pinterest.ktlint:ktlint-test:0.38.1")

    testImplementation("org.assertj:assertj-core:3.23.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks {
//    clean {
//        // TODO
//

    test {
        useJUnitPlatform()
    }
}
