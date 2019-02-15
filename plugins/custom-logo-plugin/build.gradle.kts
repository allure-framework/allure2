plugins {
    `java-library-distribution`
}

description = "Allure Custom Logo Plugin"

artifacts.add("allurePlugin", tasks.distZip)
artifacts.add("archives", tasks.distZip)
