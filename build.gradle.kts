import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import java.nio.charset.StandardCharsets.UTF_8

plugins {
    java
    id("com.bmuschko.docker-remote-api") version "6.7.0"
    id("com.diffplug.spotless") version "5.12.1"
    id("com.gorylenko.gradle-git-properties") version "2.2.4"
    id("com.jfrog.bintray") version "1.8.5"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("net.researchgate.release") version "2.8.1"
    id("ru.vyarus.quality") version "4.5.0"
}

tasks.withType(Wrapper::class) {
    gradleVersion = "6.8.3"
}

val linkHomepage by extra("https://qameta.io/allure")
val linkCi by extra("https://ci.qameta.io/job/allure2")
val linkScmUrl by extra("https://github.com/allure-framework/allure2")
val linkScmConnection by extra("scm:git:git://github.com/allure-framework/allure2.git")
val linkScmDevConnection by extra("scm:git:ssh://git@github.com:allure-framework/allure2.git")

val root = rootProject.projectDir
val gradleScriptDir by extra("$root/gradle")
val qualityConfigsDir by extra("$gradleScriptDir/quality-configs")
val spotlessDtr by extra("$qualityConfigsDir/spotless")

release {
    tagTemplate = "\${version}"
    failOnCommitNeeded = false
    failOnUnversionedFiles = false
}

val afterReleaseBuild by tasks.existing

configure(listOf(rootProject)) {
    description = "Allure Report"
    group = "io.qameta.allure"
}

configure(subprojects) {
    group = if (project.name.endsWith("plugin")) {
        "io.qameta.allure.plugins"
    } else {
        "io.qameta.allure"
    }
    version = version

    apply(plugin = "java")
    apply(plugin = "maven")
    apply(plugin = "ru.vyarus.quality")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "io.spring.dependency-management")
    apply(from = "$gradleScriptDir/bintray.gradle")
    apply(from = "$gradleScriptDir/maven-publish.gradle")

    dependencyManagement {
        imports {
            mavenBom("com.fasterxml.jackson:jackson-bom:2.12.3")
            mavenBom("org.junit:junit-bom:5.7.1")
        }
        dependencies {
            dependency("com.beust:jcommander:1.78")
            dependency("com.github.spotbugs:spotbugs-annotations:3.1.12")
            dependency("com.opencsv:opencsv:4.6")
            dependency("com.vladsch.flexmark:flexmark:0.50.36")
            dependency("commons-io:commons-io:2.8.0")
            dependency("javax.xml.bind:jaxb-api:2.3.1")
            dependency("org.allurefw:allure1-model:1.0")
            dependency("org.apache.commons:commons-lang3:3.9")
            dependency("org.apache.httpcomponents:httpclient:4.5.9")
            dependency("org.apache.tika:tika-core:1.22")
            dependency("org.assertj:assertj-core:3.13.2")
            dependency("org.eclipse.jetty:jetty-server:9.4.20.v20190813")
            dependency("org.freemarker:freemarker:2.3.31")
            dependency("org.mockito:mockito-core:3.0.0")
            dependency("org.projectlombok:lombok:1.18.8")
            dependency("org.zeroturnaround:zt-zip:1.13")
            dependencySet("org.slf4j:1.7.28") {
                entry("slf4j-api")
                entry("slf4j-nop")
                entry("slf4j-simple")
                entry("slf4j-log4j12")
            }
            dependencySet("io.qameta.allure:2.13.0") {
                entry("allure-java-commons")
                entry("allure-junit-platform")
                entry("allure-model")
                entry("allure-assertj")
            }
            dependencySet("com.squareup.retrofit2:2.6.1") {
                entry("converter-jackson")
                entry("retrofit")
            }
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.compileJava {
        options.encoding = "UTF-8"
    }

    tasks.compileTestJava {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    tasks.jar {
        manifest {
            attributes(mapOf(
                    "Implementation-Title" to project.name,
                    "Implementation-Version" to project.version

            ))
        }
    }

    tasks.test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    tasks.processTestResources {
        filesMatching("**/allure.properties") {
            filter {
                it.replace("#project.description#", project.description ?: project.name)
            }
        }
    }

    val sourceJar by tasks.creating(Jar::class) {
        from(sourceSets.getByName("main").allSource)
        archiveClassifier.set("sources")
    }

    val javadocJar by tasks.creating(Jar::class) {
        from(tasks.getByName("javadoc"))
        archiveClassifier.set("javadoc")
    }

    tasks.withType(Javadoc::class) {
        (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }

    artifacts.add("archives", sourceJar)
    artifacts.add("archives", javadocJar)

    val allurePlugin by configurations.creating

    val pluginsDir = "$buildDir/plugins/"
    val cleanPlugins by tasks.creating(Delete::class) {
        group = "Build"
        delete(pluginsDir)
    }
    val copyPlugins by tasks.creating(Sync::class) {
        group = "Build"
        dependsOn(allurePlugin, cleanPlugins)
        from(Callable { allurePlugin.map { if (it.isDirectory) it else zipTree(it) } })
        eachFile {
            val segments = relativePath.segments
            segments[0] = segments[0].replace("-${project.version}", "")
        }
        into(pluginsDir)
        includeEmptyDirs = false
    }

    spotless {
        java {
            target("src/**/*.java")
            removeUnusedImports()
            importOrder("", "jakarta", "javax", "java", "\\#")
            licenseHeader(file("$spotlessDtr/allure.java.license").readText(UTF_8))
            endWithNewline()
            replaceRegex("one blank line after package line", "(package .+;)\n+import", "$1\n\nimport")
            replaceRegex("one blank line after import lists", "(import .+;\n\n)\n+", "$1")
            replaceRegex("no blank line between jakarta & javax", "(import jakarta.+;\n)\n+(import javax.+;\n)", "$1$2")
            replaceRegex("no blank line between javax & java", "(import javax.+;\n)\n+(import java.+;\n)", "$1$2")
            replaceRegex("no blank line between jakarta & java", "(import jakarta.+;\n)\n+(import java.+;\n)", "$1$2")
        }
        format("misc") {
            target(
                    "*.gradle",
                    "*.gitignore",
                    "README.md",
                    "CONTRIBUTING.md",
                    "config/**/*.xml",
                    "src/**/*.xml"
            )
            trimTrailingWhitespace()
            endWithNewline()
        }

        encoding("UTF-8")
    }

    quality {
        configDir = "$gradleScriptDir/quality-configs"
        excludeSources = fileTree("build/generated-sources")
        exclude("**/*.json")
        checkstyleVersion = "8.36.1"
        pmdVersion = "6.28.0"
        spotbugsVersion = "4.1.2"
        codenarcVersion = "1.6"
        spotbugs = true
        codenarc = true
        pmd = true
        checkstyle = true
        htmlReports = false

        afterEvaluate {
            val spotbugs = configurations.findByName("spotbugs")
            if (spotbugs != null) {
                dependencies {
                    spotbugs("org.slf4j:slf4j-simple")
                    spotbugs("com.github.spotbugs:spotbugs:4.1.2")
                }
            }
//            tasks.withType(Checkstyle::class).configureEach {
//                configDirectory.set(file("$gradleScriptDir/quality-configs/checkstyle"))
//            }
        }
    }

    val bintrayUpload by tasks.existing
    afterReleaseBuild {
        dependsOn(bintrayUpload)
    }

    repositories {
        jcenter()
        mavenLocal()
    }
}

apply(plugin = "com.bmuschko.docker-remote-api")

val deleteDemoReport by tasks.creating(Delete::class) {
    group = "Build"
    delete("$root/build/docker/report")
}

val generateDemoReport by tasks.creating(Exec::class) {
    group = "Build"
    dependsOn("deleteDemoReport", "allure-commandline:build")
    executable = "$root/allure-commandline/build/install/allure/bin/allure"
    args("generate", "$root/allure-web/test-data/demo", "-o", "$root/build/docker/report")
}

val generateDemoDockerfile by tasks.creating(Dockerfile::class) {
    group = "Build"
    dependsOn("generateDemoReport")
    destFile.set(file("build/docker/Dockerfile"))
    from("nginx")
    addFile("report", "/usr/share/nginx/html")
}
