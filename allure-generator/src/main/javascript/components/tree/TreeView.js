import "./styles.scss";
import { View } from "backbone.marionette";
import getComparator from "../../data/tree/comparator";
import { byCriteria, byMark, byStatuses, mix } from "../../data/tree/filter";
import { behavior, className, on } from "../../decorators";
import router from "../../router";
import hotkeys from "../../utils/hotkeys";
import { SEARCH_QUERY_KEY } from "../node-search/NodeSearchView";
import template from "./TreeView.hbs";

@className("tree")
@behavior("TooltipBehavior", { position: "bottom" })
class TreeView extends View {
  template = template;

  cachedQuery = "";
  initialize({ routeState, state, tabName, baseUrl, settings }) {
    this.state = state;
    this.routeState = routeState;
    this.baseUrl = baseUrl;
    this.tabName = tabName;
    this.setState();
    this.listenTo(this.routeState, "change:treeNode", this.selectNode);
    this.listenTo(this.routeState, "change:testResultTab", this.render);

    this.settings = settings;
    this.listenTo(this.settings, "change", this.render);
    this.listenTo(this.state, "change", this.handleStateChange);

    this.listenTo(hotkeys, "key:up", this.onKeyUp, this);
    this.listenTo(hotkeys, "key:down", this.onKeyDown, this);
    this.listenTo(hotkeys, "key:esc", this.onKeyBack, this);
    this.listenTo(hotkeys, "key:left", this.onKeyBack, this);
  }

  applyFilters() {
    const visibleStatuses = this.settings.getVisibleStatuses();
    const visibleMarks = this.settings.getVisibleMarks();
    const searchQuery = this.state.get(SEARCH_QUERY_KEY);
    const filter = mix(byCriteria(searchQuery), byStatuses(visibleStatuses), byMark(visibleMarks));

    const sortSettings = this.settings.getTreeSorting();
    const sorter = getComparator(sortSettings);

    this.collection.applyFilterAndSorting(filter, sorter);
  }

  setState() {
    const treeNode = this.routeState.get("treeNode");
    if (treeNode && treeNode.testResult) {
      const uid = treeNode.testResult;
      this.state.set(uid, true);
    }
    if (treeNode && treeNode.testGroup) {
      const uid = treeNode.testGroup;
      this.state.set(uid, true);
    }
  }

  onBeforeRender() {
    this.applyFilters();
  }

  handleStateChange() {
    const query = this.state.get(SEARCH_QUERY_KEY);
    // need to check this ot to re-render nodes on folding
    if (query !== this.cachedQuery) {
      this.cachedQuery = query;
      this.render();
    }
  }

  onRender() {
    this.selectNode();
    if (this.state.get(SEARCH_QUERY_KEY)) {
      this.$(".node__title").each((i, node) => {
        this.$(node)
          .parent()
          .addClass("node__expanded");
      });
    } else {
      this.restoreState();
    }
  }

  selectNode() {
    const previous = this.routeState.previous("treeNode");
    this.toggleNode(previous, false);
    const current = this.routeState.get("treeNode");
    this.toggleNode(current, true);
    this.restoreState();
  }

  toggleNode(node, active = true) {
    if (node) {
      const el = this.findElement(node);
      el.toggleClass("node__title_active", active);
      this.changeState(node.testResult);
      this.changeState(node.testGroup);
    }
  }

  changeState(uid, active = true) {
    if (active) {
      this.state.set(uid, true);
    } else {
      this.state.unset(uid);
    }
  }

  restoreState() {
    this.$("[data-uid]").each((i, node) => {
      const el = this.$(node);
      const uid = el.data("uid");
      el.toggleClass("node__expanded", this.state.has(uid));
    });
    this.$(".node__title_active")
      .parents(".node")
      .toggleClass("node__expanded", true);
    if (this.$(".node").parents(".node__expanded").length > 0) {
      this.$(".node__expanded")
        .parents("div.node.node__expanded")
        .toggleClass("node__expanded", true);
    } else {
      this.$(".node__expanded")
        .parents(".node")
        .toggleClass("node__expanded", true);
    }
  }

  findElement(treeNode) {
    if (treeNode.testResult) {
      return this.$(`[data-uid='${treeNode.testResult}'][data-parentUid='${treeNode.testGroup}']`);
    } else {
      return this.$(`[data-uid='${treeNode.testGroup}']`);
    }
  }

  @on("click .node__title")
  onNodeClick(e) {
    const node = this.$(e.currentTarget);
    const uid = node.data("uid");
    this.changeState(uid, !this.state.has(uid));
    node.parent().toggleClass("node__expanded");
  }

  onKeyUp(event) {
    event.preventDefault();
    const current = this.routeState.get("treeNode");
    if (current && current.testResult) {
      this.selectTestResult(this.collection.getPreviousTestResult(current.testResult));
    } else {
      this.selectTestResult(this.collection.getLastTestResult());
    }
  }

  onKeyDown(event) {
    event.preventDefault();
    const current = this.routeState.get("treeNode");
    if (current && current.testResult) {
      this.selectTestResult(this.collection.getNextTestResult(current.testResult));
    } else {
      this.selectTestResult(this.collection.getFirstTestResult());
    }
  }

  onKeyBack(event) {
    event.preventDefault();
    const current = this.routeState.get("treeNode");
    if (!current) {
      return;
    }
    if (current.testGroup && current.testResult) {
      if (this.routeState.get("attachment")) {
        router.setSearch({ attachment: null });
      } else {
        router.toUrl(`${this.baseUrl}/${current.testGroup}`);
      }
    } else if (current.testGroup) {
      router.toUrl(`${this.baseUrl}`);
    }
  }

  selectTestResult(testResult) {
    if (testResult) {
      const tab = this.routeState.get("testResultTab") || "";
      router.toUrl(`${this.baseUrl}/${testResult.parentUid}/${testResult.uid}/${tab}`, {
        replace: true,
      });
    }
  }

  templateContext() {
    return {
      cls: this.className,
      baseUrl: this.baseUrl,
      showGroupInfo: this.settings.isShowGroupInfo(),
      time: this.collection.time,
      statistic: this.collection.statistic,
      uid: this.collection.uid,
      tabName: this.tabName,
      items: this.collection.toJSON(),
      testResultTab: this.routeState.get("testResultTab") || "",
    };
  }
}

export default TreeView;
