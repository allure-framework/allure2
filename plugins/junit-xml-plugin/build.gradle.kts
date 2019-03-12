plugins {
    `java-library-distribution`
}

description = "Allure JUnit Plugin"

dependencies {
    annotationProcessor("org.projectlombok:lombok")
    compileOnly("org.projectlombok:lombok")
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
