plugins {
    `java-library`
}

description = "Allure Jira Commons"

dependencies {
    annotationProcessor("org.projectlombok:lombok")
    api("com.squareup.retrofit2:converter-jackson")
    api("com.squareup.retrofit2:retrofit")
    compileOnly("org.projectlombok:lombok")
    compileOnly(project(":allure-plugin-api"))
}
