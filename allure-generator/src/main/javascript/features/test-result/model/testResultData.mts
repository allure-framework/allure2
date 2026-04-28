import { fetchReportJson } from "../../../core/services/reportData.mts";
import { makeArray } from "../../../utils/arrays.mts";

type Attachment = import("../../../types/report.mts").Attachment;
type Stage = import("../../../types/report.mts").Stage;
type Step = import("../../../types/report.mts").Step;
type TestResult = import("../../../types/report.mts").TestResult;

export type LoadedTestResultData = {
  result: TestResult;
  allAttachments: Attachment[];
  attachmentsByUid: Record<string, Attachment>;
};

export type TestResultAttachmentLookup = LoadedTestResultData["attachmentsByUid"];

const isLoadedTestResultData = (
  data: LoadedTestResultData | TestResultAttachmentLookup,
): data is LoadedTestResultData => Array.isArray((data as LoadedTestResultData).allAttachments);

const collectAttachments = ({ steps, attachments }: Partial<Stage>): Attachment[] =>
  makeArray<Step>(steps)
    .reduce<Attachment[]>((result, step) => result.concat(collectAttachments(step)), [])
    .concat(makeArray<Attachment>(attachments));

const createLoadedTestResultData = (result: TestResult): LoadedTestResultData => {
  const allAttachments = makeArray<Stage>(result.beforeStages)
    .concat(makeArray<Stage>(result.testStage))
    .concat(makeArray<Stage>(result.afterStages))
    .reduce<Attachment[]>(
      (attachments, stage) => attachments.concat(collectAttachments(stage)),
      [],
    );

  return {
    result,
    allAttachments,
    attachmentsByUid: allAttachments.reduce<Record<string, Attachment>>(
      (attachments, attachment) => {
        if (typeof attachment.uid !== "undefined") {
          attachments[String(attachment.uid)] = attachment;
        }

        return attachments;
      },
      {},
    ),
  };
};

export const loadTestResult = async (uid: string): Promise<LoadedTestResultData> => {
  const result = await fetchReportJson<TestResult>(`data/test-cases/${uid}.json`);

  return createLoadedTestResultData(result);
};

export const getTestResultAttachment = (
  data: LoadedTestResultData | TestResultAttachmentLookup,
  uid: string | number,
): Attachment | undefined => {
  const attachmentsByUid = isLoadedTestResultData(data) ? data.attachmentsByUid : data;
  return attachmentsByUid[String(uid)];
};
