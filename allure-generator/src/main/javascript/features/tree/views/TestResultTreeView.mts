import BaseElement from "../../../core/elements/BaseElement.mts";
import { createReportLoadErrorView } from "../../../core/view/asyncMount.mts";
import { normalizeReportDataError } from "../../../core/services/reportData.mts";
import translate from "../../../helpers/t.mts";
import { createElement } from "../../../shared/dom.mts";
import EmptyView from "../../../shared/ui/EmptyView.mts";
import LoaderView from "../../../shared/ui/LoaderView.mts";
import SideBySideView, { SideBySideElement } from "../../../shared/ui/SideBySideView.mts";
import { createTestResultPane } from "../../test-result/runtime.mts";
import TreeViewContainer from "./TreeViewContainer.mts";

type TreeRouteState = import("../../../core/state/StateStore.mts").default<{
  treeNode?: { testGroup?: string; testResult?: string } | null;
  testResultTab?: string;
  attachment?: string;
}>;

type TestResultTreeViewOptions = {
  treeData: import("../model/treeData.mts").LoadedTreeData;
  routeState: TreeRouteState;
  baseUrl: string;
  tabName: string;
  csvUrl?: string | null;
};

class TestResultTreeElement extends BaseElement {
  declare options: TestResultTreeViewOptions;

  declare treeData: TestResultTreeViewOptions["treeData"];

  declare routeState: TestResultTreeViewOptions["routeState"];

  declare csvUrl: string | null;

  declare showLeafRequestId: number;

  constructor() {
    super();
    this.csvUrl = null;
    this.showLeafRequestId = 0;
  }

  setOptions(options: TestResultTreeViewOptions) {
    super.setOptions(options);
    this.treeData = options.treeData;
    this.routeState = options.routeState;
    this.csvUrl = options.csvUrl || null;

    this.resetCleanups();
    this.addCleanup(
      this.routeState.subscribeKey("treeNode", () => {
        const treeNode = this.routeState.get("treeNode");
        this.showLeaf(
          typeof treeNode === "object" && treeNode
            ? (treeNode as { testGroup?: string; testResult?: string })
            : undefined,
        );
      }),
    );

    return this;
  }

  renderElement() {
    this.className = "test-result-tree";
    this.replaceChildren(createElement("div", { className: "test-result-tree__layout" }));
    const left = TreeViewContainer({
      treeData: this.treeData,
      routeState: this.routeState,
      tabName: this.options.tabName,
      baseUrl: this.options.baseUrl,
      csvUrl: this.csvUrl,
    });
    const right = EmptyView({
      message: translate("component.tree.noItemSelected"),
    });
    this.mountChild(
      "layout",
      SideBySideView({
        left,
        right,
      }),
      ".test-result-tree__layout",
    );
    this.showLeaf(this.routeState.get("treeNode") || undefined);
    return this;
  }

  showLeaf(treeNode?: { testGroup?: string; testResult?: string } | null) {
    const layout = this.getMountedChild<SideBySideElement>("layout");
    if (!layout) {
      return;
    }

    if (treeNode?.testResult) {
      const requestId = ++this.showLeafRequestId;
      const testResultUid = treeNode.testResult;
      const baseUrl = `#${this.options.baseUrl}/${treeNode.testGroup}/${testResultUid}`;
      layout.mountChild("right", LoaderView(), ".side-by-side__right");

      Promise.resolve()
        .then(() =>
          createTestResultPane({
            uid: testResultUid,
            baseUrl,
            routeState: this.routeState,
          }),
        )
        .then((view) => {
          if (requestId !== this.showLeafRequestId || !this.isConnected) {
            return;
          }

          layout.mountChild("right", view, ".side-by-side__right");
        })
        .catch((error: unknown) => {
          if (requestId !== this.showLeafRequestId || !this.isConnected) {
            return;
          }

          layout.mountChild(
            "right",
            createReportLoadErrorView(
              normalizeReportDataError(error, {
                status: 500,
              }),
              {
                fallbackStatus: 500,
              },
            ),
            ".side-by-side__right",
          );
        });
      return;
    }

    this.showLeafRequestId += 1;
    layout.mountChild(
      "right",
      EmptyView({
        message: translate("component.tree.noItemSelected"),
      }),
      ".side-by-side__right",
    );
  }
}

if (!customElements.get("allure-test-result-tree")) {
  customElements.define("allure-test-result-tree", TestResultTreeElement);
}

const createTestResultTreeView = (options: TestResultTreeViewOptions) => {
  const element = document.createElement("allure-test-result-tree") as TestResultTreeElement;
  element.setOptions(options);
  return element;
};

export default createTestResultTreeView;
