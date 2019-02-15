plugins {
    `java-library-distribution`
}

description = "Allure Screen Diff Plugin"

artifacts.add("allurePlugin", tasks.distZip)
artifacts.add("archives", tasks.distZip)
