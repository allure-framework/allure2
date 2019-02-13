plugins {
    `java-library-distribution`
}

description = "Allure Screen Diff Plugin"

tasks.jar {
    enabled = false
}

artifacts.add("allurePlugin", tasks.distZip)
