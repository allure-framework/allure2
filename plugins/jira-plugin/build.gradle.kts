plugins {
    `java-library-distribution`
}

description = "Allure Jira Plugin"

dependencies {
    api(project(":allure-plugin-api"))
    implementation(project(":allure-jira-commons"))

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    implementation("com.squareup.retrofit2:retrofit")
    implementation("com.squareup.retrofit2:converter-jackson")
    implementation("com.squareup.okhttp3:okhttp")

    implementation("com.fasterxml.jackson.core:jackson-databind")

    testImplementation(project(":allure-generator"))
    testImplementation(project(":allure-plugin-api"))
    testImplementation("io.qameta.allure:allure-junit-platform")
    testImplementation("org.apache.commons:commons-lang3")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

artifacts.add("allurePlugin", tasks.distZip)
artifacts.add("archives", tasks.distZip)
