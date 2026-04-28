import router from "../../../core/routing/router.mts";
import StateStore from "../../../core/state/StateStore.mts";
import AppLayout from "../../shell/AppLayout.mts";
import { loadTreeData, type LoadedTreeData } from "../model/treeData.mts";
import TestResultTreeView from "../views/TestResultTreeView.mts";

type TreeRouteNode = {
  testGroup?: string;
  testResult?: string;
};

type TreeRouteState = {
  treeNode?: TreeRouteNode | null;
  testResultTab?: string;
  attachment?: string;
};

const isSameTreeNode = (
  left: TreeRouteNode | null | undefined,
  right: TreeRouteNode | null | undefined,
) => left?.testGroup === right?.testGroup && left?.testResult === right?.testResult;

export default function TreeLayout(
  options: {
    url: string;
    baseUrl: string;
    tabName: string;
    csvUrl?: string | null;
    testGroup?: string;
    testResult?: string;
    testResultTab?: string;
  } & Record<string, unknown>,
) {
  const routeState = new StateStore<TreeRouteState>();
  let treeData: LoadedTreeData | null = null;

  const onRouteUpdate = (
    testGroup: string | null = null,
    testResult: string | null = null,
    testResultTab: string | null = null,
  ) => {
    const nextTreeNode = {
      testGroup: testGroup || undefined,
      testResult: testResult || undefined,
    };
    if (!isSameTreeNode(routeState.get("treeNode"), nextTreeNode)) {
      routeState.set("treeNode", nextTreeNode);
    }
    routeState.set("testResultTab", testResultTab || undefined);

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
      treeData = await loadTreeData(options.url);
    },
    createContentView: () => {
      if (!treeData) {
        throw new Error(`Tree data "${options.url}" is not loaded`);
      }

      return TestResultTreeView({
        treeData,
        routeState,
        tabName: options.tabName,
        baseUrl: options.baseUrl,
        csvUrl: options.csvUrl,
      });
    },
    onViewReady: () => {
      onRouteUpdate(
        options.testGroup || null,
        options.testResult || null,
        options.testResultTab || null,
      );
    },
    onRouteUpdate,
  });
}
