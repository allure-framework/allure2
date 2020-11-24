import com.netflix.gradle.plugins.deb.Deb
import com.netflix.gradle.plugins.rpm.Rpm
import org.gradle.kotlin.dsl.support.unzipTo

plugins {
    application
    id("nebula.ospackage") version "8.4.1"
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

val preparePackageOutput by tasks.creating {
    group = "Build"
    dependsOn(tasks.assemble)

    doLast {
        unzipTo(file("build/package"), tasks.distZip.get().archiveFile.get().asFile)
    }
}

ospackage {
    val pack = "build/package/allure-${project.version}"
    val dest = "/usr/share/allure"

    packageName = "allure"
    addParentDirs = false

    os = org.redline_rpm.header.Os.LINUX
    release = "1"

    requires("java8-runtime | java8-runtime-headless | " +
            "openjdk8-jre-headless | openjdk-8-jre | openjdk-8-jdk | " +
            "oracle-java8-installer | oracle-java8-installer")

    from("${pack}/bin", closureOf<CopySpec> {
        into("${dest}/bin")
        fileMode = 0x168
    })
    from("${pack}/config", closureOf<CopySpec> {
        into("${dest}/config")
        fileType = org.redline_rpm.payload.Directive.NOREPLACE
    })
    from("${pack}/lib", closureOf<CopySpec> {
        into("${dest}/lib")
    })
    from("${pack}/plugins", closureOf<CopySpec> {
        into("${dest}/plugins")
    })
    link("/usr/bin/allure", "${dest}/bin/allure")

}

val buildDeb by tasks.existing(Deb::class) {
    dependsOn(preparePackageOutput)
}

val buildRpm by tasks.existing(Rpm::class) {
    dependsOn(preparePackageOutput)
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
