plugins {
    `java-library-distribution`
}

description = "Allure TRX Plugin"

dependencies {
    compileOnly(project(":allure-plugin-api"))
    testImplementation("io.qameta.allure:allure-java-commons")
    testImplementation("junit:junit")
    testImplementation("org.apache.commons:commons-lang3")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.slf4j:slf4j-simple")
    testImplementation(project(":allure-plugin-api"))
}

artifacts.add("allurePlugin", tasks.distZip)
