plugins {
    `java-library`
}

description = "Allure Plugin Api"

dependencies {
    annotationProcessor("org.projectlombok:lombok")
    api("com.fasterxml.jackson.core:jackson-databind")
    api("org.slf4j:slf4j-api")
    compileOnly("org.projectlombok:lombok")
    implementation("com.fasterxml.jackson.module:jackson-module-jaxb-annotations")
    implementation("com.opencsv:opencsv")
    implementation("com.vladsch.flexmark:flexmark")
    implementation("com.vladsch.flexmark:flexmark-ext-tables")
    implementation("javax.xml.bind:jaxb-api")
    implementation("org.freemarker:freemarker")
    testImplementation("io.qameta.allure:allure-junit-platform")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.slf4j:slf4j-simple")
}
