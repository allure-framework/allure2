export const statuses = ["failed", "broken", "passed", "skipped", "unknown"];

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

    status: string;
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

export interface AllureTestStage {
    steps?: Array<AllureStep>;
    attachments?: Array<AllureAttachmentLink>;
}

export interface AllureTestResult {
    fullName?: string;
    name: string;
    status: string;

    statusMessage?: string;
    statusTrace?: string;

    parameters?: Array<AllureTestParameter>;

    testStage?: AllureTestStage;
}