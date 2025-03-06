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

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        mavenCentral()
        ivy {
            name = "Node.js"
            setUrl("https://nodejs.org/dist")
            patternLayout {
                artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]")
            }
            metadataSources {
                artifact()
            }
            content {
                includeModule("org.nodejs", "node")
            }
            isAllowInsecureProtocol = false
        }
    }
}

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.bmuschko.docker-remote-api") version "9.4.0"
        id("com.diffplug.spotless") version "7.0.2"
        id("com.github.node-gradle.node") version "7.1.0"
        id("com.gorylenko.gradle-git-properties") version "2.4.2"
        id("com.netflix.nebula.ospackage") version "11.10.1"
        id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
        id("io.spring.dependency-management") version "1.1.7"
        id("org.owasp.dependencycheck") version "12.1.0"
        id("com.github.spotbugs") version "6.1.7"
    }
}
