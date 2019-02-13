plugins {
    `java-library-distribution`
}

description = "Allure Jira Plugin"

dependencies {
    compileOnly(project(":allure-plugin-api"))
    implementation(project(":allure-jira-commons"))
    testImplementation("com.github.stefanbirkner:system-rules")
    testImplementation("junit:junit")
    testImplementation("org.apache.commons:commons-lang3")
    testImplementation("org.mockito:mockito-core")
    testImplementation(project(":allure-plugin-api"))
}

artifacts.add("allurePlugin", tasks.distZip)
