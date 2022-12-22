plugins {
    // Include Kotlin Common Conventions
    id("com.gevamu.kotlin-common-conventions")

    // Apply common settings for cordapps
    id("com.gevamu.kotlin-cordapp-conventions")

    id("maven-publish")
    id("java")
    id("signing")
}

group = rootProject.group

cordapp {
    contract {
        name("Payment Contracts")
        vendor("Exactpro Systems LLC")
        licence("Apache License, Version 2.0")
        versionId(1)
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("payments-contracts") {
            from(components["cordapp"])
            artifact(tasks["javadocJar"])
            artifact(tasks["sourcesJar"])
            pom {
                name.set("Corda payments SDK - Contracts")
                description.set("Corda based project implementing payment processing off ledger")
                url.set("https://github.com/gevamu/corda-payments-sdk")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/gevamu/corda-payments-sdk.git")
                    developerConnection.set("scm:git:ssh://github.com:gevamu/corda-payments-sdk.git")
                    url.set("https://github.com/gevamu/corda-payments-sdk/tree/master")
                }
                developers {
                    developer {
                        name.set("Gevamu")
                        organization.set("Gevamu")
                        organizationUrl.set("https://github.com/gevamu")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "test"
            setUrl("file://" + System.getenv("HOME") + "/.m2/repository/")
        }
        maven {
            name = "OSSRH"
            setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
        maven {
            name = "GitHubPackages"
            setUrl("https://maven.pkg.github.com/gevamu/corda-payments-sdk")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["payments-contracts"])
}
