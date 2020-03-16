import com.moowork.gradle.node.npm.NpmTask
import com.moowork.gradle.node.NodeExtension

plugins {
    `java-library`
    id("com.moowork.node") version "1.2.0"
}

description = "Allure Report Generator"

configure<NodeExtension> {
    version = "10.10.0"
    npmVersion = "6.4.1"
    download = true
}

val generatedStatic = "build/www"

val npmInstallDeps by tasks.creating(NpmTask::class) {
    group = "Build"
    inputs.file("package-lock.json")
    inputs.file("package.json")

    outputs.dir("node_modules")

    setArgs(arrayListOf("install"))
}

val buildWeb by tasks.creating(NpmTask::class) {
    group = "Build"
    dependsOn(npmInstallDeps)
    inputs.file(".prettierrc")
    inputs.file("package-lock.json")
    inputs.file("package.json")
    inputs.file(".eslintrc")
    inputs.files(fileTree("src/main/javascript"))
    inputs.files(fileTree("webpack"))

    outputs.dir(generatedStatic)

    setArgs(arrayListOf("run", "build"))
}

val testWeb by tasks.creating(NpmTask::class) {
    group = "Verification"
    dependsOn(npmInstallDeps)
    inputs.file(".prettierrc")
    inputs.file("package-lock.json")
    inputs.file("package.json")
    inputs.file(".eslintrc")
    inputs.files(fileTree("src/main/javascript"))
    inputs.files(fileTree("webpack"))

    setArgs(arrayListOf("run", "test"))
}

val cleanUpDemoReport by tasks.creating(Delete::class) {
    group = "Documentation"
    delete(file("build/demo-report"))
}

val generateDemoReport by tasks.creating(JavaExec::class) {
    group = "Documentation"
    dependsOn(cleanUpDemoReport, tasks.getByName("copyPlugins"))
    main = "io.qameta.allure.DummyReportGenerator"
    classpath = sourceSets.getByName("test").runtimeClasspath
    systemProperty("allure.plugins.directory", "build/plugins")
    setArgs(arrayListOf(file("test-data/demo"), file("build/demo-report")))
}

val dev by tasks.creating(NpmTask::class) {
    group = "Development"
    dependsOn(npmInstallDeps, generateDemoReport)
    setArgs(arrayListOf("run", "start"))
}

tasks.processResources {
    dependsOn(buildWeb)
    from(generatedStatic)
    filesMatching("**/allure-version.txt") {
        filter {
            it.replace("#project.version#", "${project.version}")
        }
    }
}

tasks.test {
    dependsOn(testWeb)
}

val allurePlugin by configurations.existing

dependencies {
    allurePlugin(project(path = ":behaviors-plugin", configuration = "allurePlugin"))
    allurePlugin(project(path = ":packages-plugin", configuration = "allurePlugin"))
    allurePlugin(project(path = ":screen-diff-plugin", configuration = "allurePlugin"))
    annotationProcessor("org.projectlombok:lombok")
    api(project(":allure-plugin-api"))
    compileOnly("org.projectlombok:lombok")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    implementation("commons-io:commons-io")
    implementation("io.qameta.allure:allure-model")
    implementation("javax.xml.bind:jaxb-api")
    implementation("org.allurefw:allure1-model")
    implementation("org.apache.httpcomponents:httpclient")
    implementation("org.apache.tika:tika-core")
    implementation("org.freemarker:freemarker")
    testImplementation("io.qameta.allure:allure-java-commons")
    testImplementation("io.qameta.allure:allure-junit-platform")
    testImplementation("org.apache.commons:commons-lang3")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.slf4j:slf4j-simple")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
