plugins {
    `java-library`
    id("io.qameta.allure")
}

description = "Allure Plugin Api"

allure {
    version.set("2.34.0")
    adapter {
        allureJavaVersion.set("2.34.0")
        aspectjVersion.set("1.9.25.1")
        autoconfigure.set(false)
        aspectjWeaver.set(true)
    }
}

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
    testImplementation("io.qameta.allure:allure-assertj")
    testImplementation("io.qameta.allure:allure-junit-platform")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.slf4j:slf4j-simple")
}
