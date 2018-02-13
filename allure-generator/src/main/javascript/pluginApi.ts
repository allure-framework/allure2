import {
    AllurePlugins,
    ReportTabConfiguration,
    TestResultBlockConfiguration,
    TestResultTabConfiguration
} from "./allure";

class AllurePluginsRegistry implements AllurePlugins {

    private reportTabs: Array<ReportTabConfiguration> = [];
    private testResultTabs: Array<TestResultTabConfiguration> = [];
    private testResultBlocks: Array<TestResultBlockConfiguration> = [];

    addReportTab(config: ReportTabConfiguration) {
        this.reportTabs.push(config);
    }

    addTestResultBlock(config: TestResultBlockConfiguration) {
        this.testResultBlocks.push(config);
    }

    addTestResultTab(config: TestResultTabConfiguration) {
        this.testResultTabs.push(config);
    }

    getReportTabs() {
        return this.reportTabs;
    }

    getTestResultTabs(): Array<TestResultTabConfiguration> {
        return this.testResultTabs;
    }

    getTestResultBlocks(): Array<TestResultBlockConfiguration> {
        return this.testResultBlocks;
    }
}

window.allure = {
    api: new AllurePluginsRegistry()
};
