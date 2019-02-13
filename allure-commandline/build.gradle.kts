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

val distZip by tasks.existing(Zip::class) {
    includeEmptyDirs = false
    eachFile {
        path = path.replaceFirst("-commandline", "")
    }
}

val distTar by tasks.existing(Tar::class) {
    includeEmptyDirs = false
    eachFile {
        path = path.replaceFirst("-commandline", "")
    }
    compression = Compression.GZIP
}

val sourceSets = project.the<SourceSetContainer>()
val main = sourceSets.getByName("main")

val startScripts by tasks.existing(CreateStartScripts::class) {
    applicationName = "allure"
    classpath = main.runtimeClasspath + files("src/lib/config")
}
//
//applicationDistribution.from(copyPlugins) {
//    into "plugins"
//}
//
//val installDist = tasks.existing(Sync::class)
//
//tasks.getByName("build") {
//    dependsOn(installDist)
//}

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
    testImplementation("junit:junit")
    testImplementation("org.apache.commons:commons-text")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito:mockito-core")
}
