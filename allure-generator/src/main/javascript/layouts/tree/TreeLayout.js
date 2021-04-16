import { Model } from "backbone";
import TestResultTreeView from "../../components/testresult-tree/TestResultTreeView";
import TreeCollection from "../../data/tree/TreeCollection";
import router from "../../router";
import AppLayout from "../application/AppLayout";

export default class TreeLayout extends AppLayout {
  initialize({ url }) {
    super.initialize();
    this.tree = new TreeCollection([], { url });
    this.routeState = new Model();
  }

  loadData() {
    return this.tree.fetch();
  }

  getContentView() {
    const { baseUrl, tabName, csvUrl } = this.options;
    return new TestResultTreeView({
      tree: this.tree,
      routeState: this.routeState,
      tabName,
      baseUrl,
      csvUrl,
    });
  }

  onViewReady() {
    const { testGroup, testResult, testResultTab } = this.options;
    this.onRouteUpdate(testGroup, testResult, testResultTab);
  }

  onRouteUpdate(testGroup, testResult, testResultTab) {
    this.routeState.set("treeNode", { testGroup, testResult });
    this.routeState.set("testResultTab", testResultTab);

    const attachment = router.getUrlParams().attachment;
    if (attachment) {
      this.routeState.set("attachment", attachment);
    } else {
      this.routeState.unset("attachment");
    }
  }
}
