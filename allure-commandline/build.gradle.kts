import com.netflix.gradle.plugins.deb.Deb
import com.netflix.gradle.plugins.rpm.Rpm
import org.gradle.kotlin.dsl.support.unzipTo

plugins {
    application
    id("com.netflix.nebula.ospackage")
    id("org.graalvm.buildtools.native")
}

description = "Allure Commandline"

application {
    mainClass.set("io.qameta.allure.CommandLine")
}

distributions {
    main {
        contents {
            from(tasks.named("copyPlugins")) {
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
    classpath = files("src/lib/*", "src/lib/config")
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

    requires("default-jre-headless")

    // Remove closureOf when https://github.com/nebula-plugins/gradle-ospackage-plugin/issues/399 is fixed
    from("${pack}/bin", closureOf<CopySpec> {
        into("${dest}/bin")
        fileMode = 0x1ED
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

publishing {
    publications {
        named<MavenPublication>("maven") {
            artifact(tasks.distZip)
            artifact(tasks.distTar)
        }
    }
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
    implementation("ch.qos.logback:logback-classic")
    implementation("com.beust:jcommander")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    implementation("commons-io:commons-io")
    implementation("org.eclipse.jetty:jetty-server")
    implementation(project(":allure-generator"))
    testImplementation("io.qameta.allure:allure-junit-platform")
    testImplementation("org.apache.commons:commons-lang3")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core")
}

graalvmNative {
    toolchainDetection.set(true)

    binaries {
        named("main") {
            imageName.set("allure")

            useFatJar.set(false)
            verbose.set(true)

            javaLauncher.set(javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(21))
            })
            requiredVersion.set("23.1.2")

            metadataRepository {
                enabled.set(true)
                version.set("0.3.6")
            }

            configurationFileDirectories.from(
                    file("src/main/resources/META-INF/native-image")
            )

            buildArgs.addAll(
                    "-march=compatibility",
                    "-H:+AddAllCharsets",
                    "-Djava.awt.headless=false",
                    "--initialize-at-run-time=io.netty.handler.ssl.util.ThreadLocalInsecureRandom",
            )
            jvmArgs.addAll(
                    "-Dfile.encoding=UTF-8",
                    "-Dsun.jnu.encoding=UTF-8",
                    "-Djava.awt.headless=false",
                    "-Djava.net.preferIPv4Stack=true",
                    "-Djava.net.preferIPv4Addresses=true",
            )
            runtimeArgs.addAll(
                    "generate",
                    "$rootDir/allure-generator/test-data/allure2",
                    "-o",
                    "$projectDir/build/allure-report-demo-native",
                    "-c",
                    "--single-file",
            )
        }
    }

    agent {
        enabled.set(true)

        // for generate
//        defaultMode.set("standard")
        // For serve and open
//        defaultMode.set("direct")

        // Copies metadata collected from tasks into the specified directories.
        metadataCopy {
            inputTaskNames.addAll("runAndGenerate", "runAndServe", "runAndOpen") // Tasks previously executed with the agent attached.
            outputDirectories.add("src/main/resources/META-INF/native-image")
            mergeWithExisting.set(true) // Instead of copying, merge with existing metadata in the output directories.
        }

        modes {
            direct {
                options.add("config-write-period-secs=1")
                options.add("config-output-dir={output_dir}")
            }
        }
    }
}

task("runAndGenerate", JavaExec::class) {
    group = "nativeTasks"
    mainClass = application.mainClass
    classpath = sourceSets["main"].runtimeClasspath
    args = arrayListOf("generate", "$rootDir/allure-generator/test-data/allure2", "-o", "$projectDir/build/allure-report-demo-generate", "-c", "--single-file")
}

task("runAndServe", JavaExec::class) {
    group = "nativeTasks"
    mainClass = application.mainClass
    classpath = sourceSets["main"].runtimeClasspath
    args = arrayListOf("serve", "$rootDir/allure-generator/test-data/allure2")
}

task("runAndOpen", JavaExec::class) {
    group = "nativeTasks"
    mainClass = application.mainClass
    classpath = sourceSets["main"].runtimeClasspath
    args = arrayListOf("open", "$projectDir/build/allure-report-demo-generate")
}
