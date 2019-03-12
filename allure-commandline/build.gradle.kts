plugins {
    application
}

description = "Allure Commandline"

application {
    mainClassName = "io.qameta.allure.CommandLine"
}

distributions {
    main {
        contents {
            from(tasks.getByName("copyPlugins")) {
                into("plugins")
            }
        }
    }
}

tasks.distZip {
    includeEmptyDirs = false
    eachFile {
        path = path.replaceFirst("-commandline", "")
    }
}

tasks.distTar {
    includeEmptyDirs = false
    eachFile {
        path = path.replaceFirst("-commandline", "")
    }
    compression = Compression.GZIP
}

val main = sourceSets.getByName("main")

val startScripts by tasks.existing(CreateStartScripts::class) {
    applicationName = "allure"
    classpath = classpath?.plus(files("src/lib/config"))
    doLast {
        unixScript.writeText(unixScript.readText()
                .replace(Regex("(?m)^APP_HOME="), "export APP_HOME=")
                .replace("\$(uname)\" = \"Darwin", "")
        )
    }
}

tasks.build {
    dependsOn(tasks.installDist)
}

dependencies {
    allurePlugin(project(path = ":behaviors-plugin", configuration = "allurePlugin"))
    allurePlugin(project(path = ":custom-logo-plugin", configuration = "allurePlugin"))
    allurePlugin(project(path = ":jira-plugin", configuration = "allurePlugin"))
    allurePlugin(project(path = ":junit-xml-plugin", configuration = "allurePlugin"))
    allurePlugin(project(path = ":packages-plugin", configuration = "allurePlugin"))
    allurePlugin(project(path = ":screen-diff-plugin", configuration = "allurePlugin"))
    allurePlugin(project(path = ":trx-plugin", configuration = "allurePlugin"))
    allurePlugin(project(path = ":xctest-plugin", configuration = "allurePlugin"))
    allurePlugin(project(path = ":xray-plugin", configuration = "allurePlugin"))
    allurePlugin(project(path = ":xunit-xml-plugin", configuration = "allurePlugin"))
    annotationProcessor("org.projectlombok:lombok")
    compileOnly("org.projectlombok:lombok")
    implementation("com.beust:jcommander")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    implementation("commons-io:commons-io")
    implementation("org.eclipse.jetty:jetty-server")
    implementation("org.slf4j:slf4j-log4j12")
    implementation(project(":allure-generator"))
    testImplementation("io.qameta.allure:allure-junit-platform")
    testImplementation("org.apache.commons:commons-lang3")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.mockito:mockito-core")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
