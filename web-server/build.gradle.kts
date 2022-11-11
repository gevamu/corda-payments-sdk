plugins {
    application
    pmd
    id("org.springframework.boot") version "2.7.5"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
}

group = rootProject.group

repositories {
    mavenCentral()
    maven("https://software.r3.com/artifactory/corda")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

application {
    mainClass.set("com.gevamu.web.server.Main")
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

    compileOnly("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")

    implementation(project(":contracts"))
    implementation(project(":workflows"))

    implementation("co.paralleluniverse:quasar-core:0.8.0")
    implementation("net.corda:corda-rpc:4.9.3")
    implementation("net.openhft:chronicle-map:3.24ea0")

    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks {
    getByName("run") {
        dependsOn(":web-ui:assemble")
    }

    test {
        useJUnitPlatform()
    }
}
