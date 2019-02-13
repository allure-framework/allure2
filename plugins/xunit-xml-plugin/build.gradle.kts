plugins {
    `java-library-distribution`
}

description = "Allure Xunit.net XML v2 Plugin"

dependencies {
    compileOnly(project(":allure-plugin-api"))
    testImplementation("junit:junit")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.slf4j:slf4j-simple")
    testImplementation(project(":allure-plugin-api"))
}

artifacts.add("allurePlugin", tasks.distZip)
