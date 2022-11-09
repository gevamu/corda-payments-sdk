import com.github.gradle.node.npm.task.NpmTask

plugins {
  base // Add lifecycle tasks
  id("com.github.node-gradle.node") version "3.5.0"
}

node {
  version.set("16.16.0")
  npmVersion.set("8.19.3")
  // yarnVersion.set("")
  npmInstallCommand.set("ci")
  distBaseUrl.set("https://nodejs.org/dist")
  download.set(true)
  workDir.set(file(".cache/nodejs"))
  npmWorkDir.set(file(".cache/npm"))
  yarnWorkDir.set(file(".cache/yarn"))
  nodeProjectDir.set(file("."))
}

tasks.npmInstall {
  // args.set(listOf("ci"))
  nodeModulesOutputFilter {
    exclude("notExistingFile")
  }
}

tasks {
  val npmBuild = register<NpmTask>("npmBuild") {
    dependsOn(npmInstall)
    args.set(listOf("run", "build"))
    inputs.dir("src")
    inputs.dir("public")
    inputs.dir("node_modules")
    inputs.files("quasar.config.js", "index.html", "tsconfig.json", "postcss.config.js")
    outputs.dir("${project.buildDir}/dist")
  }

  val npmLint = register<NpmTask>("npmLint") {
    dependsOn(npmInstall)
    args.set(listOf("run", "lint"))
    inputs.dir("src")
    inputs.dir("public")
    inputs.dir("node_modules")
    inputs.files("quasar.config.js", "index.html", "tsconfig.json", "postcss.config.js")
  }

  assemble {
    dependsOn(npmBuild)
  }

  check {
    dependsOn(npmLint)
  }

  clean {
    // TODO
  }
}
