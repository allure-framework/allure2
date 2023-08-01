rootProject.name = "allure2"

include("allure-jira-commons")
include("allure-plugin-api")
include("allure-generator")
include("allure-commandline")

val plugins = listOf(
        "behaviors-plugin",
        "custom-logo-plugin",
        "jira-plugin",
        "junit-xml-plugin",
        "packages-plugin",
        "screen-diff-plugin",
        "trx-plugin",
        "xctest-plugin",
        "xray-plugin",
        "xunit-xml-plugin"
)

plugins.forEach {
    include("plugins/$it")
    project(":plugins/$it").name = it
}

pluginManagement {
    plugins {
        id("com.bmuschko.docker-remote-api") version "9.3.2"
        id("com.diffplug.spotless") version "6.20.0"
        id("com.github.node-gradle.node") version "5.0.0"
        id("com.gorylenko.gradle-git-properties") version "2.4.1"
        id("com.netflix.nebula.ospackage") version "11.4.0"
        id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
        id("io.spring.dependency-management") version "1.1.2"
        id("org.owasp.dependencycheck") version "8.3.1"
        id("ru.vyarus.quality") version "4.9.0"
    }
}
