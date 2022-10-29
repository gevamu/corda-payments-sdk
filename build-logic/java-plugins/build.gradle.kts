plugins {
    `java-gradle-plugin`
}

repositories {
    gradlePluginPortal()
}

// Kotlin 1.4 is the last version supporting Kotlin API 1.2 which is bundled with Corda 4
val kotlinVersion = "1.4.30"

dependencies {
    implementation(gradlePlugin("org.jetbrains.kotlin.jvm", kotlinVersion))
    // Kotlin style checker (3.0.x is for Kotlin 1.4)
    implementation(gradlePlugin("org.jmailen.kotlinter", "3.0.2"))
}

configurations.all {
    resolutionStrategy.dependencySubstitution {
        substituteKotlinModule("org.jetbrains.kotlin:kotlin-stdlib-jdk7")
        substituteKotlinModule("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    }
}

gradlePlugin {
    plugins {
        create("kotlinCommon") {
            id = "com.gevamu.kotlin-common-conventions"
            implementationClass = "com.gevamu.KotlinCommonPlugin"
        }
    }
}

fun gradlePlugin(id: String, version: String) = "$id:$id.gradle.plugin:$version"

fun DependencySubstitutions.substituteKotlinModule(moduleNotation: String) {
    substitute(module(moduleNotation)).using(module("$moduleNotation:$kotlinVersion"))
}
