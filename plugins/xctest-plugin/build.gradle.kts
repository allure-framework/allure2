plugins {
    `java-library-distribution`
}

description = "Allure XCTest Plugin"

dependencies {
    implementation("xmlwise:xmlwise:1.2.11")
    compileOnly(project(":allure-plugin-api"))
    testImplementation(project(":allure-plugin-api"))
    testImplementation("org.slf4j:slf4j-simple")
    testImplementation("junit:junit")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito:mockito-core")
}

artifacts.add("allurePlugin", tasks.distZip)
