plugins {
    `java-library-distribution`
}

description = "Allure Custom Logo Plugin"

tasks.jar {
    enabled = false
}

artifacts.add("allurePlugin", tasks.distZip)
