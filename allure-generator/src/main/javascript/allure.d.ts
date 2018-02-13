import {ReactChild} from "react";

interface ReportTabConfiguration {
    id: string;
    name?: string;
    order?: number;
    icon?: string;
    render: () => ReactChild;
}

interface TestResultTabConfiguration {
    id: string;
    name: string;
    render: () => ReactChild;
}

interface TestResultBlockConfiguration {
    id: string;
    name?: string;
    order?: number;
    displayCondition?: () => boolean;
    render: () => ReactChild;
}

interface AllurePlugins {

    addReportTab(config: ReportTabConfiguration): void;

    addTestResultTab(config: TestResultTabConfiguration): void;

    addTestResultBlock(config: TestResultBlockConfiguration): void;

    getReportTabs(): Array<ReportTabConfiguration>;

    getTestResultTabs(): Array<TestResultTabConfiguration>;

    getTestResultBlocks(): Array<TestResultBlockConfiguration>;
}

interface AllureApi {
    api: AllurePlugins;
}

declare global {
    interface Window {
        allure: AllureApi;
    }
}
