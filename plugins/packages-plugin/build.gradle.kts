plugins {
    `java-library-distribution`
}

description = "Allure Packages Plugin"

dependencies {
    compileOnly(project(":allure-plugin-api"))
    testImplementation(project(":allure-generator"))
    testImplementation("io.qameta.allure:allure-java-commons")
    testImplementation("junit:junit")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito:mockito-core")
}

artifacts.add("allurePlugin", tasks.distZip)
