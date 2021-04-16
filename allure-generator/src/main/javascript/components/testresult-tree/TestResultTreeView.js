import TestResultModel from "../../data/testresult/TestResultModel";
import { className } from "../../decorators";
import EmptyView from "../empty/EmptyView";
import ErrorSplashView from "../error-splash/ErrorSplashView";
import SideBySideView from "../side-by-side/SideBySideView";
import TestResultView from "../testresult/TestResultView";
import TreeViewContainer from "../tree-view-container/TreeViewContainer";

@className("side-by-side")
class TestResultTreeView extends SideBySideView {
  initialize({ tree, routeState, csvUrl }) {
    super.initialize();
    this.csvUrl = csvUrl;
    this.tree = tree;
    this.routeState = routeState;
    this.listenTo(this.routeState, "change:treeNode", (_, treeNode) => this.showLeaf(treeNode));
  }

  showLeaf(treeNode) {
    if (treeNode && treeNode.testResult) {
      const baseUrl = `#${this.options.baseUrl}/${treeNode.testGroup}/${treeNode.testResult}`;
      const model = new TestResultModel({ uid: treeNode.testResult });
      model.fetch({
        success: () =>
          this.showChildView(
            "right",
            new TestResultView({ baseUrl, model, routeState: this.routeState }),
          ),
        error: () =>
          this.showChildView(
            "right",
            new ErrorSplashView({
              code: 404,
              message: `Test result with uid "${treeNode.testResult}" not found`,
            }),
          ),
      });
    } else {
      this.showChildView("right", new EmptyView({ message: "No item selected" }));
    }
  }

  onRender() {
    const { tabName, baseUrl } = this.options;
    const left = new TreeViewContainer({
      collection: this.tree,
      routeState: this.routeState,
      treeSorters: [],
      tabName: tabName,
      baseUrl: baseUrl,
      csvUrl: this.csvUrl,
    });
    this.showChildView("left", left);
  }

  templateContext() {
    return {
      cls: "testresult-tree",
    };
  }
}

export default TestResultTreeView;
