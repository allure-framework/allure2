import "./TestResultView.scss";
import BaseElement from "../../../core/elements/BaseElement.mts";
import { getTestResultTabs } from "../../../core/registry/index.mts";
import translate from "../../../helpers/t.mts";
import { escapeHtml } from "../../../shared/html.mts";
import ErrorSplashView from "../../../shared/ui/ErrorSplashView.mts";
import ModalView from "../../../shared/ui/ModalView.mts";
import TooltipView from "../../../shared/ui/TooltipView.mts";
import { findWhere } from "../../../shared/utils/collections.mts";
import copy from "../../../utils/clipboard.mts";
import { AttachmentView } from "../../attachments/runtime.mts";
import { getTestResultAttachment } from "../model/testResultData.mts";
import { createTestResultContent } from "./renderTestResult.mts";
import TestResultOverviewView from "./TestResultOverviewView.mts";

type TestResult = import("../../../types/report.mts").TestResult;
type TestResultAttachmentLookup = import("../model/testResultData.mts").TestResultAttachmentLookup;
type TestResultTabDescriptor = import("../../../core/registry/types.mts").TestResultTabDescriptor;
type TestResultRouteState = import("../../../core/state/StateStore.mts").default<{
  testResultTab?: string;
  attachment?: string;
}>;
type ModalMountable = ReturnType<typeof ModalView>;
type RouteResetAwareMountable = {
  suppressRouteReset?: boolean;
};
type TestResultViewOptions = {
  routeState: TestResultRouteState;
  baseUrl: string;
  data: TestResult;
  attachmentsByUid: TestResultAttachmentLookup;
};

const subViews: TestResultTabDescriptor[] = [
  { id: "", name: "testResult.overview.name", create: TestResultOverviewView },
];

class TestResultElement extends BaseElement {
  declare options: TestResultViewOptions;

  declare data: TestResult;

  declare attachmentsByUid: TestResultAttachmentLookup;

  declare routeState: TestResultViewOptions["routeState"];

  declare tabs: TestResultTabDescriptor[];

  declare tabName: string;

  declare tooltip: TooltipView;

  declare modalView: ModalMountable | null;

  declare modalAttachmentUid: string | null;

  constructor() {
    super();
    this.tabs = subViews.concat(getTestResultTabs());
    this.tabName = "";
    this.tooltip = new TooltipView({ position: "left" });
    this.modalView = null;
    this.modalAttachmentUid = null;
  }

  setOptions(options: TestResultViewOptions) {
    super.setOptions(options);
    this.data = options.data;
    this.attachmentsByUid = options.attachmentsByUid;
    this.routeState = options.routeState;
    this.tabs = subViews.concat(getTestResultTabs());
    this.tabName = this.getRouteString("testResultTab");

    this.resetCleanups();
    this.addCleanup(
      this.routeState.subscribeKey("testResultTab", (_, tabName) =>
        this.onTabChange(typeof tabName === "string" ? tabName : undefined),
      ),
    );
    this.addCleanup(
      this.routeState.subscribeKey("attachment", (_, uid) =>
        this.onShowAttachment(typeof uid === "string" ? uid : undefined),
      ),
    );

    return this;
  }

  renderElement() {
    const status = this.data.status || "unknown";
    this.className = "test-result";
    this.replaceChildren(
      createTestResultContent({
        cls: this.className,
        fullName: this.data.fullName || "",
        status,
        statusName: `status.${status}`,
        flaky: Boolean(this.data.flaky),
        newFailed: Boolean(this.data.newFailed),
        newPassed: Boolean(this.data.newPassed),
        newBroken: Boolean(this.data.newBroken),
        retriesStatusChange: Boolean(this.data.retriesStatusChange),
        name: this.data.name || "",
        links: this.tabs.map((view) => ({
          href: `${this.options.baseUrl}/${view.id}`,
          name: view.name,
          active: view.id === this.tabName,
        })),
      }),
    );
    this.bindEvents(
      {
        "click .status-details__trace-toggle": "onStacktraceClick",
        "mouseover .attachment-row__icon[data-tooltip]": "onAttachmentTooltipHover",
        "mouseout .attachment-row__icon[data-tooltip]": "onAttachmentTooltipLeave",
        "mouseenter [data-tooltip]": "onTooltipHover",
        "mouseleave [data-tooltip]": "onTooltipLeave",
        "mouseenter [data-copy]": "onCopyHover",
        "mouseleave [data-copy]": "onTooltipLeave",
        "click [data-copy]": "onCopyClick",
      },
      this,
    );

    const subView = findWhere(this.tabs, { id: this.tabName });
    this.mountChild(
      "content",
      !subView
        ? ErrorSplashView({
            code: 404,
            message: translate("errors.tabNotFound", { hash: { tab: this.tabName } }),
          })
        : subView.create({
            ...this.options,
            data: this.data,
            attachmentsByUid: this.attachmentsByUid,
          }),
      ".test-result__content",
    );

    const attachment = this.getRouteString("attachment");
    if (attachment) {
      this.onShowAttachment(attachment);
    }

    return this;
  }

  onTabChange(tabName?: string) {
    this.tabName = tabName || "";
    this.render();
  }

  getRouteString(key: "testResultTab" | "attachment") {
    const value = this.routeState.get(key);
    return typeof value === "string" ? value : "";
  }

  getCurrentModalContentView() {
    const mountedChild = this.modalView?.getMountedChild("content");
    const configuredChild = this.modalView?.options.childView;
    return (mountedChild || configuredChild || null) as RouteResetAwareMountable | null;
  }

  onShowAttachment(uid?: string) {
    if (!uid && this.modalView) {
      const currentAttachmentView = this.getCurrentModalContentView();

      if (currentAttachmentView) {
        currentAttachmentView.suppressRouteReset = true;
      }

      this.modalView.destroy();
      this.modalView = null;
      this.modalAttachmentUid = null;
      return;
    }

    if (!uid) {
      return;
    }

    if (this.modalView && this.modalAttachmentUid === uid) {
      return;
    }

    if (this.modalView) {
      const currentAttachmentView = this.getCurrentModalContentView();

      if (currentAttachmentView) {
        currentAttachmentView.suppressRouteReset = true;
      }

      this.modalView.destroy();
    }

    const attachment = getTestResultAttachment(this.attachmentsByUid, uid);
    const childView = attachment
      ? AttachmentView({ attachment, fullScreen: true })
      : ErrorSplashView({ code: 404, message: translate("errors.missedAttachment") });

    this.modalAttachmentUid = uid;
    this.modalView = ModalView({
      childView,
      title: attachment ? attachment.name || attachment.source : translate("errors.notFound"),
    });
    this.modalView.show();
  }

  onStacktraceClick(e: Event) {
    (e.currentTarget as Element)
      .closest(".status-details")
      ?.classList.toggle("status-details__expanded");
  }

  onTooltipHover(e: Event) {
    const element = e.currentTarget as HTMLElement;
    this.tooltip.show(escapeHtml(element.dataset.tooltip || ""), element);
  }

  onTooltipLeave() {
    this.tooltip.hide();
  }

  onAttachmentTooltipHover(e: Event) {
    const element = e.currentTarget as HTMLElement;
    this.tooltip.show(escapeHtml(element.dataset.tooltip || ""), element);
  }

  onAttachmentTooltipLeave(e: Event) {
    const element = e.currentTarget as HTMLElement;
    const relatedTarget = (e as MouseEvent).relatedTarget;

    if (relatedTarget instanceof Node && element.contains(relatedTarget)) {
      return;
    }

    this.tooltip.hide();
  }

  onCopyHover(e: Event) {
    const element = e.currentTarget as HTMLElement;
    this.tooltip.show(translate("controls.clipboard"), element);
  }

  onCopyClick(e: Event) {
    const element = e.currentTarget as HTMLElement;
    const text = element.dataset.copy;
    if (text && copy(text)) {
      this.tooltip.show(translate("controls.clipboardSuccess"), element);
    } else {
      this.tooltip.show(translate("controls.clipboardError"), element);
    }
  }

  destroy() {
    this.tooltip.hide();

    if (this.modalView) {
      const currentAttachmentView = this.getCurrentModalContentView();

      if (currentAttachmentView) {
        currentAttachmentView.suppressRouteReset = true;
      }

      this.modalView.destroy();
      this.modalView = null;
      this.modalAttachmentUid = null;
    }

    super.destroy();
  }
}

if (!customElements.get("allure-test-result-view")) {
  customElements.define("allure-test-result-view", TestResultElement);
}

const createTestResultView = (options: TestResultViewOptions) => {
  const element = document.createElement("allure-test-result-view") as TestResultElement;
  element.setOptions(options);
  return element;
};

export default createTestResultView;
