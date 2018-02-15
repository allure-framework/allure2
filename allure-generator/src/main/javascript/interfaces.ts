export type AllureStatus = "failed" | "broken" | "passed" | "skipped" | "unknown";

export const statuses: Array<AllureStatus> = ["failed", "broken", "passed", "skipped", "unknown"];

export interface AllureStatistic {
    failed?: number;
    broken?: number;
    passed?: number;
    skipped?: number;
    unknown?: number;
}

export interface AllureStep {
    name: string;
    steps?: Array<AllureStep>;
    attachments?: Array<AllureAttachmentLink>;
    parameters?: Array<AllureTestParameter>;

    status: AllureStatus;
    statusMessage?: string;

    start?: number;
    stop?: number;
    duration?: number;
}

export interface AllureAttachmentLink {
    uid: string;
    name: string;
    source: string;
    type: string;
    size: number;
}

export interface AllureTestParameter {
    name: string;
    value: string;
}

export interface AllureTestResultExecution {
    steps?: Array<AllureStep>;
    attachments?: Array<AllureAttachmentLink>;
}

export interface AllureTestResult {
    id: number;

    fullName?: string;
    name: string;
    status: AllureStatus;

    message?: string;
    trace?: string;

    parameters?: Array<AllureTestParameter>;
}

export interface AllureNodeContext {
    key?: string,
    value?: string
}

export interface AllureTreeLeaf {
    id: number,
    name: string,
    parentUid: string,
    status: AllureStatus,
    start: number,
    stop: number,
    duration: number
    flaky: boolean,
    parameters: Array<string>
}

export interface AllureTreeGroup {
    uid: string,
    name: string,
    context: AllureNodeContext,
    statistic: AllureStatistic
    groups?: Array<AllureTreeGroup>,
    leafs?: Array<AllureTreeLeaf>

    total?: number;
    shown?: number;
}

