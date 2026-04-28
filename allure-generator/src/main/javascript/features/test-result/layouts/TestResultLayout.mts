import router from "../../../core/routing/router.mts";
import StateStore from "../../../core/state/StateStore.mts";
import AppLayout from "../../shell/AppLayout.mts";
import { loadTestResult } from "../model/testResultData.mts";
import TestResultView from "../views/TestResultView.mts";

type LoadedTestResultData = import("../model/testResultData.mts").LoadedTestResultData;
type TestResultRouteState = {
  testResultTab?: string;
  attachment?: string;
};

export default function TestResultLayout(
  options: { uid: string; tabName?: string } & Record<string, unknown>,
) {
  const routeState = new StateStore<TestResultRouteState>();
  let data: LoadedTestResultData | null = null;

  const onRouteUpdate = (_uid: string | null, tabName: string | null = null) => {
    routeState.set("testResultTab", tabName || undefined);

    const attachment = router.getUrlParams().attachment;
    if (attachment) {
      routeState.set("attachment", attachment);
    } else {
      routeState.unset("attachment");
    }
  };

  return AppLayout({
    ...options,
    loadData: async () => {
      data = await loadTestResult(options.uid);
    },
    createContentView: () => {
      if (!data) {
        throw new Error(`Test result "${options.uid}" is not loaded`);
      }

      return TestResultView({
        baseUrl: `#testresult/${options.uid}`,
        data: data.result,
        attachmentsByUid: data.attachmentsByUid,
        routeState,
      });
    },
    onViewReady: () => {
      onRouteUpdate(options.uid, options.tabName || null);
    },
    onRouteUpdate,
    shouldKeepState: (uid: string | null) => options.uid === uid,
  });
}
