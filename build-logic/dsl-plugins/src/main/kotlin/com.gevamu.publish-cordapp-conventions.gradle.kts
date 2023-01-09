plugins {
    // Include Java Common Conventions
    id("com.gevamu.java-common-conventions")

    id("maven-publish")
    id("signing")
}

publishing {
    publications {
        create<MavenPublication>(project.name) {
            from(project.components["cordapp"])
            artifact(project.tasks["javadocJar"])
            artifact(project.tasks["sourcesJar"])
            pom {
                // FIXME move to the plugin extension
                name.set("Corda payments SDK - ${project.name.substring(project.name.indexOf("-") + 1).capitalize()}")
                description.set("Corda based project implementing payment processing off ledger")
                url.set("https://github.com/gevamu/corda-payments-sdk")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/gevamu/corda-payments-sdk.git")
                    developerConnection.set("scm:git:https://github.com/gevamu/corda-payments-sdk.git")
                    url.set("https://github.com/gevamu/corda-payments-sdk.git")
                }
                developers {
                    developer {
                        name.set("Gevamu")
                        organization.set("Exactpro Systems Limited")
                        organizationUrl.set("https://gevamu.com/")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "OSSRH"
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
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
    sign(publishing.publications[project.name])
}
