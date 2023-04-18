import net.corda.plugins.CordappExtension
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.ZoneId

plugins {
    // Include Java-library Conventions
    id("com.gevamu.java-library-conventions")

    // Corda plugins
    id("net.corda.plugins.quasar-utils")
    id("net.corda.plugins.cordapp")
}

repositories {
    maven("https://software.r3.com/artifactory/corda")
    maven("https://repo.gradle.org/gradle/libs-releases-local")
    maven("https://jitpack.io")
}

dependencies {
    // Corda
    cordaProvided("net.corda:corda-core:4.9.3")
    cordaProvided("net.corda:corda-node-api:4.9.3")

    cordaRuntimeOnly("net.corda:corda:4.9.3")

    // logging. Corda 4.9 provides log4j 2.17.1
    cordaProvided("org.apache.logging.log4j:log4j-api:2.17.1")
    cordaProvided("org.apache.logging.log4j:log4j-core:2.17.1")

    testImplementation("net.corda:corda-node-driver:4.9.3")
}

fun gitHash(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
        standardOutput = stdout
    }
    return stdout.toString().trim()
}

fun gitHasChanges(): Boolean {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "status", "--porcelain", "--untracked-files=no")
        // Returns either empty line or "XX YY"
        // Where XX is change type ([M]odified, [A]dded, ...) and YY is filename
        standardOutput = stdout
    }
    return stdout.toString().trim().isNotEmpty()
}

fun gitBranch(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "branch", "--show-current")
        standardOutput = stdout
    }
    return stdout.toString().trim()
}

tasks {
    test {
        javaLauncher.set(javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(8))
        })
    }
    jar {
        // This makes the JAR's SHA-256 hash repeatable.
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest {
            attributes["Build-Version"] = // 
                // Add version or branch name if no version present
                "${if (project.version == "unspecified") gitBranch() else project.version}" +
                    // Add last git hash commit, short form, in brackets
                    // Append +M if there are local changes (ignoring untracked files)
                    "(${gitHash()}${if (gitHasChanges()) "+M" else ""})" +
                    // Add Unix timestamp of build date
                    " built on ${LocalDateTime.now(ZoneId.of("UTC"))}"
        }
    }
    named<Jar>("javadocJar") {
        from(named("dokkaJavadoc"))
    }
}

configure<CordappExtension> {
    targetPlatformVersion(8)
    minimumPlatformVersion(8)
}
