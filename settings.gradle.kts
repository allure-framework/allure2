rootProject.name = "allure2"

include("allure-jira-commons")
include("allure-plugin-api")
include("allure-generator")
include("allure-commandline")

val plugins = listOf(
        "behaviors-plugin",
        "custom-logo-plugin",
        "jira-plugin",
        "junit-xml-plugin",
        "packages-plugin",
        "screen-diff-plugin",
        "trx-plugin",
        "xctest-plugin",
        "xray-plugin",
        "xunit-xml-plugin"
)

plugins.forEach {
    include("plugins/$it")
    project(":plugins/$it").name = it
}
