import com.github.gradle.node.npm.task.NpmTask

plugins {
    `java-library`
    id("com.github.node-gradle.node") version "3.5.1"
}

description = "Allure Report Generator"

node {
    // enforce https
    distBaseUrl.set("https://nodejs.org/dist")
    version.set("16.18.0")
    download.set(true)
}

val generatedStatic = "build/www"

tasks.npmInstall {
    group = "Build"
    args.set(listOf("--silent"))
    npmCommand.set(listOf("ci"))
    environment.set(mapOf("ADBLOCK" to "true"))
    inputs.file("package-lock.json")
    inputs.file("package.json")

    outputs.dir("node_modules")
}

val buildWeb by tasks.creating(NpmTask::class) {
    group = "Build"
    dependsOn(tasks.npmInstall)
    inputs.file(".prettierrc")
    inputs.file("package-lock.json")
    inputs.file("package.json")
    inputs.file(".eslintignore")
    inputs.file(".eslintrc.js")
    inputs.file("babel.config.js")
    inputs.files(fileTree("src/main/javascript"))
    inputs.files(fileTree("webpack"))

    outputs.dir(generatedStatic)

    args.set(listOf("run", "build", "--silent"))
}

val testWeb by tasks.creating(NpmTask::class) {
    group = "Verification"
    dependsOn(tasks.npmInstall)
    inputs.file(".prettierrc")
    inputs.file("package-lock.json")
    inputs.file("package.json")
    inputs.file(".eslintignore")
    inputs.file(".eslintrc.js")
    inputs.file("babel.config.js")
    inputs.files(fileTree("src/main/javascript"))
    inputs.files(fileTree("webpack"))

    args.set(listOf("run", "test", "--silent"))
}

val cleanUpDemoReport by tasks.creating(Delete::class) {
    group = "Documentation"
    delete(file("build/demo-report"))
}

val generateDemoReport by tasks.creating(JavaExec::class) {
    group = "Documentation"
    dependsOn(cleanUpDemoReport, tasks.named("copyPlugins"))
    main = "io.qameta.allure.DummyReportGenerator"
    classpath = sourceSets.getByName("test").runtimeClasspath
    systemProperty("allure.plugins.directory", "build/plugins")
    setArgs(arrayListOf(file("test-data/new-demo"), file("build/demo-report")))
}

val dev by tasks.creating(NpmTask::class) {
    group = "Development"
    dependsOn(tasks.npmInstall, generateDemoReport)
    args.set(listOf("run", "start"))
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
    implementation("com.fasterxml.jackson.module:jackson-module-jaxb-annotations")
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
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.slf4j:slf4j-simple")
}
