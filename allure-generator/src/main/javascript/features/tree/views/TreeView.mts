import "./TreeView.scss";
import BaseElement from "../../../core/elements/BaseElement.mts";
import router from "../../../core/routing/router.mts";
import hotkeys from "../../../core/services/hotkeys.mts";
import { escapeHtml } from "../../../shared/html.mts";
import TooltipView from "../../../shared/ui/TooltipView.mts";
import getComparator from "../model/comparator.mts";
import { byCriteria, byMark, byStatuses, mix } from "../model/filter.mts";
import { SEARCH_QUERY_KEY } from "../model/searchState.mts";
import type { LoadedTreeData } from "../model/treeData.mts";
import { projectTreeData } from "../model/treeData.mts";
import { createTreeGroupChildren, createTreeViewContent } from "./renderTreeView.mts";

type TreeSettings = ReturnType<
  typeof import("../../../core/services/settings.mts").getSettingsForTreePlugin
>;
type TreeRouteState = import("../../../core/state/StateStore.mts").default<{
  treeNode?: { testGroup?: string; testResult?: string } | null;
  testResultTab?: string;
  attachment?: string;
}>;
type TreeRouteNode = {
  testGroup?: string;
  testResult?: string;
} | null;

const isSameTreeRouteNode = (left: TreeRouteNode, right: TreeRouteNode) =>
  left?.testGroup === right?.testGroup && left?.testResult === right?.testResult;

type TreeViewOptions = {
  routeState: TreeRouteState;
  state: import("../../../core/state/StateStore.mts").default;
  tabName: string;
  baseUrl: string;
  settings: TreeSettings;
  treeData: LoadedTreeData;
};

class TreeViewElement extends BaseElement {
  declare options: TreeViewOptions;

  declare state: TreeViewOptions["state"];

  declare routeState: TreeViewOptions["routeState"];

  declare baseUrl: string;

  declare tabName: string;

  declare treeData: LoadedTreeData;

  declare treeProjection: LoadedTreeData;

  declare settings: TreeSettings;

  declare tooltip: TooltipView;

  declare cachedQuery: string;

  declare selectedTreeNode: TreeRouteNode;

  declare treeNodeIndex: Map<string, LoadedTreeData["allNodes"][number]>;

  declare treeParentIndex: Map<string, string | null>;

  declare treeGroupUids: Set<string>;

  constructor() {
    super();
    this.cachedQuery = "";
    this.baseUrl = "";
    this.tabName = "";
    this.treeData = {
      items: [],
      allNodes: [],
      allResults: [],
      testResults: [],
      time: {},
      statistic: {},
    } as LoadedTreeData;
    this.treeProjection = this.treeData;
    this.tooltip = new TooltipView({ position: "bottom" });
    this.selectedTreeNode = null;
    this.treeNodeIndex = new Map();
    this.treeParentIndex = new Map();
    this.treeGroupUids = new Set();
  }

  setOptions(options: TreeViewOptions) {
    super.setOptions(options);
    this.state = options.state;
    this.routeState = options.routeState;
    this.baseUrl = options.baseUrl;
    this.tabName = options.tabName;
    this.treeData = options.treeData;
    this.treeProjection = this.treeData;
    this.settings = options.settings;

    this.resetCleanups();
    this.addCleanup(this.routeState.subscribeKey("treeNode", () => this.selectNode()));
    this.addCleanup(this.routeState.subscribeKey("testResultTab", () => this.render()));
    this.addCleanup(this.settings.subscribe(() => this.render()));
    this.addCleanup(this.state.subscribe(() => this.handleStateChange()));
    this.addCleanup(hotkeys.subscribe("up", (event) => this.onKeyUp(event)));
    this.addCleanup(hotkeys.subscribe("down", (event) => this.onKeyDown(event)));
    this.addCleanup(hotkeys.subscribe("esc", (event) => this.onKeyBack(event)));
    this.addCleanup(hotkeys.subscribe("left", (event) => this.onKeyBack(event)));
    this.addCleanup(hotkeys.subscribe("right", (event) => this.onKeyForward(event)));

    return this;
  }

  applyFilters() {
    const visibleStatuses = this.settings.getVisibleStatuses() || {
      failed: true,
      broken: true,
      skipped: true,
      unknown: true,
      passed: true,
    };
    const visibleMarks = this.settings.getVisibleMarks() || {
      flaky: false,
      newFailed: false,
      newPassed: false,
      newBroken: false,
      retriesStatusChange: false,
    };
    const searchQuery = this.getSearchQuery();
    const filter = mix(byCriteria(searchQuery), byStatuses(visibleStatuses), byMark(visibleMarks));
    const sortSettings = this.settings.getTreeSorting() || {
      sorter: "sorter.name",
      ascending: true,
    };
    const sorter = getComparator(sortSettings);
    this.treeProjection = projectTreeData(this.treeData, filter, sorter);
    this.indexTree(this.treeProjection.items);
  }

  indexTree(items: LoadedTreeData["items"], parentUid: string | null = null) {
    this.treeNodeIndex.clear();
    this.treeParentIndex.clear();
    this.treeGroupUids.clear();

    const visit = (nodes: LoadedTreeData["items"], parent: string | null) => {
      nodes.forEach((node) => {
        if (!node.uid) {
          return;
        }

        this.treeNodeIndex.set(node.uid, node);
        this.treeParentIndex.set(node.uid, parent);
        if (node.children) {
          this.treeGroupUids.add(node.uid);
          visit(node.children, node.uid);
        }
      });
    };

    visit(items, parentUid);
  }

  renderElement() {
    this.applyFilters();
    this.className = "tree";
    this.replaceChildren(
      createTreeViewContent({
        cls: this.className,
        baseUrl: this.baseUrl,
        showGroupInfo: Boolean(this.settings.isShowGroupInfo()),
        uid: this.treeData.uid,
        items: this.treeProjection.items,
        testResultTab: this.getTestResultTab(),
      }),
    );
    this.bindEvents(
      {
        "click .node__title": "onNodeClick",
        "mouseenter [data-tooltip]": "onTooltipHover",
        "mouseleave [data-tooltip]": "onTooltipLeave",
      },
      this,
    );

    this.selectNode();

    return this;
  }

  handleStateChange() {
    const query = this.getSearchQuery();
    if (query !== this.cachedQuery) {
      this.cachedQuery = query;
      this.render();
    }
  }

  selectNode() {
    const previous = this.selectedTreeNode;
    this.toggleNode(previous, false);
    const current = this.getTreeNode();
    this.toggleNode(current, true);
    if (current?.testGroup && current.testResult) {
      this.setExpandedState(current.testGroup, true);
    }
    this.selectedTreeNode = current ? { ...current } : null;

    if (this.getSearchQuery()) {
      this.expandAllGroups(this.treeProjection.items);
    } else {
      this.restoreState({ includeCurrentPath: true });
    }

    if (current) {
      const currentElement = this.findElement(current);
      currentElement?.classList.add("node__title_active");
      this.scrollNodeIntoView(currentElement);
    }
  }

  toggleNode(node: TreeRouteNode, active = true) {
    if (!node) {
      return;
    }

    const el = this.findElement(node);
    el?.classList.toggle("node__title_active", active);
  }

  scrollNodeIntoView(node: Element | null | undefined) {
    if (!(node instanceof HTMLElement)) {
      return;
    }

    node.scrollIntoView({
      block: "nearest",
      inline: "nearest",
    });
  }

  setExpandedState(uid?: string, expanded = true) {
    if (!uid) {
      return;
    }

    if (expanded) {
      this.state.set(uid, true);
    } else {
      this.state.set(uid, false);
    }
  }

  restoreState({ includeCurrentPath = false }: { includeCurrentPath?: boolean } = {}) {
    const expandedUids = this.collectExpandedUids({ includeCurrentPath });
    this.querySelectorAll(".node__title_active").forEach((node) => {
      node.classList.remove("node__title_active");
    });
    this.querySelectorAll(".node[data-node-kind='group']").forEach((node) => {
      node.classList.remove("node__expanded");
      node.querySelector(":scope > .node__children")?.remove();
    });

    this.syncExpandedNodes(this.treeProjection.items, expandedUids);

    const current = this.getTreeNode();
    if (current) {
      this.findElement(current)?.classList.add("node__title_active");
    }
  }

  collectExpandedUids({ includeCurrentPath = false }: { includeCurrentPath?: boolean } = {}) {
    if (this.getSearchQuery()) {
      return new Set(this.treeGroupUids);
    }

    const expanded = new Set<string>();
    this.treeGroupUids.forEach((uid) => {
      if (this.state.get(uid) === true) {
        expanded.add(uid);
      }
    });

    const treeNode = this.getTreeNode();
    let currentUid = treeNode?.testGroup;
    if (includeCurrentPath && currentUid && !treeNode?.testResult) {
      currentUid = this.treeParentIndex.get(currentUid) || undefined;
    }

    while (currentUid) {
      if (includeCurrentPath || this.state.get(currentUid) !== false) {
        expanded.add(currentUid);
      }
      currentUid = this.treeParentIndex.get(currentUid) || undefined;
    }

    return expanded;
  }

  syncExpandedNodes(items: LoadedTreeData["items"], expandedUids: Set<string>) {
    items.forEach((item) => {
      if (!item.children || !item.uid) {
        return;
      }

      const groupElement = this.getGroupElement(item.uid);
      if (!groupElement) {
        return;
      }

      const shouldExpand = expandedUids.has(item.uid);
      groupElement.classList.toggle("node__expanded", shouldExpand);
      if (!shouldExpand) {
        groupElement.querySelector(":scope > .node__children")?.remove();
        return;
      }

      const children = this.ensureGroupChildren(groupElement, item);
      if (!children) {
        return;
      }

      this.syncExpandedNodes(item.children, expandedUids);
    });
  }

  expandAllGroups(items: LoadedTreeData["items"]) {
    this.querySelectorAll(".node[data-node-kind='group']").forEach((node) => {
      node.classList.remove("node__expanded");
      node.querySelector(":scope > .node__children")?.remove();
    });

    const visit = (nodes: LoadedTreeData["items"]) => {
      nodes.forEach((item) => {
        if (!item.children || !item.uid) {
          return;
        }

        const groupElement = this.getGroupElement(item.uid);
        if (!groupElement) {
          return;
        }

        groupElement.classList.add("node__expanded");
        this.ensureGroupChildren(groupElement, item);
        visit(item.children);
      });
    };

    visit(items);
  }

  getGroupElement(uid: string) {
    return this.querySelector(
      `.node[data-node-kind='group'][data-node-uid='${uid}']`,
    ) as HTMLElement | null;
  }

  ensureGroupChildren(groupElement: HTMLElement, item: LoadedTreeData["items"][number]) {
    if (!item.children) {
      return null;
    }

    const existing = groupElement.querySelector(":scope > .node__children");
    if (existing) {
      return existing as HTMLElement;
    }

    const children = createTreeGroupChildren(item, {
      baseUrl: this.baseUrl,
      testResultTab: this.getTestResultTab(),
      showGroupInfo: Boolean(this.settings.isShowGroupInfo()),
    });
    groupElement.appendChild(children);
    return children;
  }

  collapseGroup(uid?: string) {
    if (!uid) {
      return;
    }

    this.setExpandedState(uid, false);
    const node = this.treeNodeIndex.get(uid);
    node?.children?.forEach((child) => {
      if (child.uid && child.children) {
        this.collapseGroup(child.uid);
      }
    });
  }

  isCurrentNodeInsideGroup(uid: string) {
    const current = this.getTreeNode();
    if (!current?.testGroup) {
      return false;
    }

    let currentUid: string | undefined = current.testGroup;
    while (currentUid) {
      if (currentUid === uid) {
        return true;
      }

      currentUid = this.treeParentIndex.get(currentUid) || undefined;
    }

    return false;
  }

  findElement(treeNode: { testResult?: string; testGroup?: string }) {
    if (treeNode.testResult) {
      return this.querySelector(
        `[data-uid='${treeNode.testResult}'][data-parentUid='${treeNode.testGroup}']`,
      );
    }

    return this.querySelector(
      `.node[data-node-kind='group'][data-node-uid='${treeNode.testGroup}'] > .node__title`,
    );
  }

  getSearchQuery() {
    const query = this.state.get(SEARCH_QUERY_KEY);
    return typeof query === "string" ? query : "";
  }

  getTreeNode() {
    const treeNode = this.routeState.get("treeNode");
    return (treeNode ?? null) as TreeRouteNode;
  }

  getTestResultTab() {
    const tabName = this.routeState.get("testResultTab");
    return typeof tabName === "string" ? tabName : "";
  }

  onNodeClick(e: Event) {
    const node = e.currentTarget as HTMLElement;
    const uid = node.dataset.uid;
    const treeNode = node.parentElement;
    if (!treeNode || !uid || !this.treeGroupUids.has(uid)) {
      return;
    }

    const isExpanding = !treeNode.classList.contains("node__expanded");
    if (isExpanding) {
      this.setExpandedState(uid, true);
      this.restoreState();
      return;
    }

    const current = this.getTreeNode();
    this.collapseGroup(uid);
    if (
      current?.testGroup &&
      (current.testResult || current.testGroup !== uid) &&
      this.isCurrentNodeInsideGroup(uid)
    ) {
      this.navigateToTreeNode({ testGroup: uid });
      return;
    }

    this.restoreState();
  }

  onTooltipHover(e: Event) {
    const element = e.currentTarget as HTMLElement;
    this.tooltip.show(escapeHtml(element.dataset.tooltip || ""), element);
  }

  onTooltipLeave() {
    this.tooltip.hide();
  }

  onKeyUp(event: Event) {
    event.preventDefault();
    this.selectVisibleNode("up");
  }

  onKeyDown(event: Event) {
    event.preventDefault();
    this.selectVisibleNode("down");
  }

  onKeyBack(event: Event) {
    event.preventDefault();
    const current = this.getTreeNode();
    if (!current) {
      return;
    }

    if (this.routeState.get("attachment")) {
      router.setSearch({
        attachment: null,
      });
      return;
    }

    if (!current.testGroup) {
      return;
    }

    const currentGroup = current.testGroup;
    const searchQuery = this.getSearchQuery();
    const parentGroup = this.treeParentIndex.get(currentGroup) || undefined;

    if (current.testResult) {
      if (!searchQuery) {
        this.collapseGroup(currentGroup);
      }
      this.navigateToTreeNode({ testGroup: currentGroup });
      return;
    }

    const groupElement = this.getGroupElement(currentGroup);
    if (groupElement?.classList.contains("node__expanded") && !searchQuery) {
      this.collapseGroup(currentGroup);
      this.restoreState();
      return;
    }

    if (parentGroup) {
      if (!searchQuery) {
        this.collapseGroup(parentGroup);
      }
      this.navigateToTreeNode({ testGroup: parentGroup });
      return;
    }

    router.toUrl(`${this.baseUrl}`);
  }

  onKeyForward(event: Event) {
    event.preventDefault();
    const current = this.getTreeNode();
    if (!current?.testGroup || current.testResult) {
      return;
    }

    const groupNode = this.treeNodeIndex.get(current.testGroup);
    const groupElement = this.getGroupElement(current.testGroup);
    if (!groupNode?.children || !groupElement) {
      return;
    }

    if (!groupElement.classList.contains("node__expanded")) {
      this.setExpandedState(current.testGroup, true);
      this.restoreState();
      return;
    }

    const firstChild = groupNode.children.find((child) => Boolean(child.uid));
    if (!firstChild?.uid) {
      return;
    }

    this.navigateToTreeNode(
      firstChild.children
        ? {
            testGroup: firstChild.uid,
          }
        : {
            testGroup: current.testGroup,
            testResult: firstChild.uid,
          },
    );
  }

  selectTestResult(testResult?: { parentUid?: string; uid?: string }) {
    if (!testResult?.parentUid || !testResult.uid) {
      return;
    }

    const tab = this.getTestResultTab();
    router.toUrl(`${this.baseUrl}/${testResult.parentUid}/${testResult.uid}/${tab}`, {
      replace: true,
    });
  }

  getVisibleTreeNodes(): Exclude<TreeRouteNode, null>[] {
    return Array.from(this.querySelectorAll<HTMLElement>(".node__title"))
      .map((element) => this.getRouteNodeFromElement(element))
      .filter((node): node is Exclude<TreeRouteNode, null> => Boolean(node));
  }

  getRouteNodeFromElement(element: HTMLElement): TreeRouteNode {
    const uid = element.getAttribute("data-uid");
    if (!uid) {
      return null;
    }

    const parentUid = element.getAttribute("data-parentUid");
    if (parentUid) {
      return {
        testGroup: parentUid,
        testResult: uid,
      };
    }

    return {
      testGroup: uid,
    };
  }

  selectVisibleNode(direction: "up" | "down") {
    const visibleNodes = this.getVisibleTreeNodes();
    if (visibleNodes.length === 0) {
      return;
    }

    const current = this.getTreeNode();
    const currentIndex = visibleNodes.findIndex((node) => isSameTreeRouteNode(node, current));

    if (currentIndex === -1) {
      this.navigateToTreeNode(direction === "down" ? visibleNodes[0] : visibleNodes.at(-1) || null);
      return;
    }

    const offset = direction === "down" ? 1 : -1;
    const nextIndex = Math.min(Math.max(currentIndex + offset, 0), visibleNodes.length - 1);
    this.navigateToTreeNode(visibleNodes[nextIndex] || null);
  }

  navigateToTreeNode(treeNode: TreeRouteNode) {
    if (!treeNode || isSameTreeRouteNode(treeNode, this.getTreeNode())) {
      return;
    }

    if (treeNode.testResult) {
      this.selectTestResult({
        parentUid: treeNode.testGroup,
        uid: treeNode.testResult,
      });
      return;
    }

    router.toUrl(`${this.baseUrl}/${treeNode.testGroup || ""}`, {
      replace: true,
    });
  }

  destroy() {
    this.tooltip.hide();
    super.destroy();
  }
}

if (!customElements.get("allure-tree-view")) {
  customElements.define("allure-tree-view", TreeViewElement);
}

const createTreeView = (options: TreeViewOptions) => {
  const element = document.createElement("allure-tree-view") as TreeViewElement;
  element.setOptions(options);
  return element;
};

export default createTreeView;
