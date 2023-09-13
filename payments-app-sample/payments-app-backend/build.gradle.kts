plugins {
    application
    pmd
    id("org.springframework.boot") version "2.7.5"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
}

group = rootProject.group

repositories {
    mavenCentral()
    maven("https://download.corda.net/maven/corda-dependencies")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

application {
    mainClass.set("com.gevamu.corda.web.server.Main")
}

distributions {
    main {
        contents {
            from("config") {
                into("config")
            }
        }
    }
}

configurations {
    compileOnly.get().extendsFrom(annotationProcessor.get())
    testCompileOnly.get().extendsFrom(testAnnotationProcessor.get())
}

dependencies {
    annotationProcessor("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    implementation(project(":gevamu-payment-contracts"))
    implementation(project(":gevamu-payment-workflows"))

    implementation("co.paralleluniverse:quasar-core:0.8.0")
    implementation("net.corda:corda-rpc:4.9.6")
    implementation("net.openhft:chronicle-map:3.24ea0")

    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks {
    getByName<JavaExec>("run") {
        dependsOn(":payments-app-sample:payments-app-frontend:assemble")
    }

    test {
        useJUnitPlatform()
    }
}
