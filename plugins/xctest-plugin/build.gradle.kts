plugins {
    `java-library-distribution`
}

description = "Allure XCTest Plugin"

dependencies {
    implementation("xmlwise:xmlwise:1.2.11")
    compileOnly(project(":allure-plugin-api"))
    testImplementation("io.qameta.allure:allure-junit-platform")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.slf4j:slf4j-simple")
    testImplementation(project(":allure-generator"))
    testImplementation(project(":allure-plugin-api"))
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

artifacts.add("allurePlugin", tasks.distZip)
artifacts.add("archives", tasks.distZip)
