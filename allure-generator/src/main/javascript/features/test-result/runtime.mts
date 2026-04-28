import TestResultLayout from "./layouts/TestResultLayout.mts";
import { loadTestResult } from "./model/testResultData.mts";
import TestResultView from "./views/TestResultView.mts";

export { TestResultLayout };

type CreateTestResultPaneOptions = {
  uid: string;
  baseUrl: string;
  routeState: import("../../core/state/StateStore.mts").default<{
    testResultTab?: string;
    attachment?: string;
  }>;
};

export const createTestResultPane = async ({
  uid,
  baseUrl,
  routeState,
}: CreateTestResultPaneOptions): Promise<import("../../core/view/types.mts").Mountable> =>
  loadTestResult(uid).then(({ result, attachmentsByUid }) =>
    TestResultView({
      baseUrl,
      data: result,
      attachmentsByUid,
      routeState,
    }),
  );
