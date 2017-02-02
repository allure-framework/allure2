### Allure Report

[![release-badge][]][release]
[![bintray-badge][]][bintray]
[![build-badge][]][build]

Allure Report v2

You can find the roadmap [here](https://github.com/allurefw/allure-report/wiki/Roadmap).

[release]: https://github.com/allure-framework/allure2/releases/latest "Latest release"
[release-badge]: https://img.shields.io/github/release/allure-framework/allure2.svg?style=flat

[bintray]: https://bintray.com/qameta/generic/allure2 "Bintray"
[bintray-badge]: https://img.shields.io/bintray/v/qameta/generic/allure2.svg?style=flat

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
