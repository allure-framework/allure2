plugins {
    `java-library-distribution`
}

description = "Allure TRX Plugin"

dependencies {
    compileOnly(project(":allure-plugin-api"))
    testImplementation("io.qameta.allure:allure-junit-platform")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.slf4j:slf4j-simple")
    testImplementation(project(":allure-generator"))
    testImplementation(project(":allure-plugin-api"))
}

artifacts.add("allurePlugin", tasks.distZip)
artifacts.add("archives", tasks.distZip)
