import "./TreeViewContainer.scss";
import BaseElement from "../../../core/elements/BaseElement.mts";
import { getSettingsForTreePlugin } from "../../../core/services/settings.mts";
import StateStore from "../../../core/state/StateStore.mts";
import TooltipView from "../../../shared/ui/TooltipView.mts";
import gtag from "../../../utils/gtag.mts";
import MarksToggleView from "../controls/MarksToggleView.mts";
import NodeSearchView from "../controls/NodeSearchView.mts";
import NodeSorterView from "../controls/NodeSorterView.mts";
import StatusToggleView from "../controls/StatusToggleView.mts";
import { renderTreeViewContainer } from "./renderTreeViewContainer.mts";
import TreeView from "./TreeView.mts";

type TreeRouteState = StateStore<{
  treeNode?: { testGroup?: string; testResult?: string } | null;
  testResultTab?: string;
  attachment?: string;
}>;

type TreeViewContainerOptions = {
  routeState: TreeRouteState;
  state?: StateStore;
  treeData: import("../model/treeData.mts").LoadedTreeData;
  tabName: string;
  baseUrl: string;
  csvUrl?: string | null;
  settings?: ReturnType<typeof getSettingsForTreePlugin>;
};

class TreeViewContainerElement extends BaseElement {
  declare options: TreeViewContainerOptions;

  declare state: StateStore;

  declare routeState: TreeRouteState;

  declare treeData: import("../model/treeData.mts").LoadedTreeData;

  declare baseUrl: string;

  declare csvUrl: string | null;

  declare tabName: string;

  declare settings: ReturnType<typeof getSettingsForTreePlugin>;

  declare tooltip: TooltipView;

  constructor() {
    super();
    this.state = new StateStore();
    this.routeState = new StateStore();
    this.csvUrl = null;
    this.baseUrl = "";
    this.tabName = "";
    this.treeData = {} as import("../model/treeData.mts").LoadedTreeData;
    this.settings = getSettingsForTreePlugin("");
    this.tooltip = new TooltipView({ position: "bottom" });
  }

  setOptions(options: TreeViewContainerOptions) {
    super.setOptions(options);
    this.state = options.state || new StateStore();
    this.routeState = options.routeState;
    this.treeData = options.treeData;
    this.baseUrl = options.baseUrl;
    this.csvUrl = options.csvUrl || null;
    this.tabName = options.tabName;
    this.settings = options.settings || getSettingsForTreePlugin(options.baseUrl);

    this.resetCleanups();
    this.addCleanup(this.routeState.subscribeKey("testResultTab", () => this.render()));

    return this;
  }

  renderElement() {
    this.className = "tree";
    this.replaceChildren(
      ...renderTreeViewContainer({
        cls: this.className,
        tabName: this.tabName,
        shownCases: 0,
        totalCases: 0,
        filtered: false,
        csvUrl: this.csvUrl,
      }),
    );
    this.bindEvents(
      {
        "click .tree__info": "onInfoClick",
        "mouseenter [data-tooltip]": "onTooltipHover",
        "mouseleave [data-tooltip]": "onTooltipLeave",
      },
      this,
    );

    this.mountChild(
      "content",
      TreeView({
        state: this.state,
        routeState: this.routeState,
        tabName: this.tabName,
        baseUrl: this.baseUrl,
        settings: this.settings,
        treeData: this.treeData,
      }),
      ".tree__content",
    );
    this.mountChild(
      "search",
      NodeSearchView({
        state: this.state,
      }),
      ".pane__search",
    );
    this.mountChild(
      "sorter",
      NodeSorterView({
        settings: this.settings,
      }),
      ".tree__sorter",
    );
    this.mountChild(
      "filter",
      StatusToggleView({
        settings: this.settings,
        statistic: this.treeData.statistic,
      }),
      ".tree__filter",
    );
    this.mountChild(
      "filterMarks",
      MarksToggleView({
        settings: this.settings,
      }),
      ".tree__filter-marks",
    );

    return this;
  }

  onInfoClick() {
    const show = this.settings.isShowGroupInfo();
    this.settings.setShowGroupInfo(!show);
    gtag("tree_info_click", {
      enable: !show,
    });
  }

  onTooltipHover(e: Event) {
    const element = e.currentTarget as HTMLElement;
    this.tooltip.show(element.dataset.tooltip || "", element);
  }

  onTooltipLeave() {
    this.tooltip.hide();
  }

  destroy() {
    this.tooltip.hide();
    super.destroy();
  }
}

if (!customElements.get("allure-tree-view-container")) {
  customElements.define("allure-tree-view-container", TreeViewContainerElement);
}

const createTreeViewContainer = (options: TreeViewContainerOptions) => {
  const element = document.createElement("allure-tree-view-container") as TreeViewContainerElement;
  element.setOptions(options);
  return element;
};

export default createTreeViewContainer;
