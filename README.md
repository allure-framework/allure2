### Allure Report

[![release-badge][]][release]
[![mavencentral-badge][]][mavencentral]
[![build-badge][]][build]

Allure Report v2

You can find the roadmap [here](https://github.com/allurefw/allure-report/wiki/Roadmap).

[release]: https://github.com/allurefw/allure-report/releases/latest "Latest release"
[release-badge]: https://img.shields.io/github/release/allurefw/allure-report.svg?style=flat

[mavencentral]: https://mvnrepository.com/artifact/org.allurefw.report/allure-report "Maven central"
[mavencentral-badge]: https://img.shields.io/maven-central/v/org.allurefw.report/allure-report.svg?style=flat

[build]: https://ci.qameta.in/job/allure2_deploy/lastBuild "Jenkins build"
[build-badge]: https://ci.qameta.in/buildStatus/icon?job=allure2_deploy

## Development

To start dev server you can run

```bash
$ ./gradlew dev
```

And then open `http://localhost:3000`

Also you can choose different demo data using `results` parameter:

```bash
$ ./gradlew dev -Presults=allure2
```