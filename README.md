[license]: http://www.apache.org/licenses/LICENSE-2.0 "Apache License 2.0"
[site]: https://qameta.io/?source=Report_GitHub
[blog]: https://qameta.io/blog
[gitter]: https://gitter.im/allure-framework/allure-core
[gitter-ru]: https://gitter.im/allure-framework/allure-ru
[tg-ru]: https://t.me/allure_ru
[twitter]: https://twitter.com/QametaSoftware "Qameta Software"
[twitter-team]: https://twitter.com/QametaSoftware/lists/team/members "Team"
[build]: https://github.com/allure-framework/allure2/actions/workflows/build.yaml
[build-badge]: https://github.com/allure-framework/allure2/actions/workflows/build.yaml/badge.svg
[maven]: https://repo.maven.apache.org/maven2/io/qameta/allure/allure-commandline/ "Maven Central"
[maven-badge]: https://img.shields.io/maven-central/v/io.qameta.allure/allure-commandline.svg?style=flat
[release]: https://github.com/allure-framework/allure2/releases/latest "Latest release"
[release-badge]: https://img.shields.io/github/release/allure-framework/allure2.svg?style=flat
[CONTRIBUTING.md]: .github/CONTRIBUTING.md
[CODE_OF_CONDUCT.md]: CODE_OF_CONDUCT.md
[docs]: https://docs.qameta.io/allure-report/
[discussions]: https://github.com/allure-framework/allure2/discussions

# About this fork version
Allure is a very good report framework and easy to integrate with test script. However, I'm not able to open a report with more than 20000 results, because it takes 48 seconds or longer, depends on test results count. I want to open a report in no more than 60 seconds.

# What I did
Briefly speaking I applied two changes to public version:
1. Disabled attachment in the test report.
2. Changed the jackson object mapper's property naming strategy, set it to SNAKE_CASE

# Performance comparison
| | Report1(5 results) | Report2(20218 results) | Report3(40755 results) | Report4(77280 results) |
| -------- | -------- | -------- | -------- | -------- |
| Benchmark (allure v2.21) | 1859ms | 48506ms | 167868ms | 609583ms |
| allure2-perf-plus v2.25 | 3804ms | 13868ms | 23498ms | 42683ms |
| Improvement | ✕ | ▲ 71% | ▲86% | ▲93% | 

## How to get this version
There're couple of ways:
1. Clone the repo and build it yourself.
2. Get the allure executable from publish folder in this repo.

## Code of Conduct

Please note that this project is released with a [Contributor Code of Conduct][CODE_OF_CONDUCT.md]. By participating in this project you agree to abide by its terms.

## Contributors

This project exists thanks to all the people who contributed. [[Contribute]](.github/CONTRIBUTING.md).

<a href="https://github.com/allure-framework/allure2/graphs/contributors"><img src="https://opencollective.com/allure-report/contributors.svg?avatarHeight=24&width=890&showBtn=false" /></a>

## License

The Allure Framework is released under version 2.0 of the [Apache License][license].
