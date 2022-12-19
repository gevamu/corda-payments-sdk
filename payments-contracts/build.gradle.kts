plugins {
    // Include Kotlin Common Conventions
    id("com.gevamu.kotlin-common-conventions")

    // Apply common settings for cordapps
    id("com.gevamu.kotlin-cordapp-conventions")

    id("maven-publish")
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

publishing {
    publications {
        create<MavenPublication>("paymentsSDK-contracts") {
            artifactId = "corda-payments-sdk-contracts"
            from(components["cordapp"])
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
                    developerConnection.set("scm:git:git://github.com/gevamu/corda-payments-sdk.git")
                    url.set("https://github.com/gevamu/corda-payments-sdk")
                }
            }
        }
    }
    repositories {
//        maven {
//            name = "OSSRH"
//            setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
//            credentials {
//                username = System.getenv("MAVEN_USERNAME")
//                password = System.getenv("MAVEN_PASSWORD")
//            }
//        }
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
