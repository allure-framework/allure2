import "./ScreenDiffView.scss";
import { fetchReportJson } from "../../../core/services/reportData.mts";
import { getSettingsForPlugin } from "../../../core/services/settings.mts";
import { bindDelegatedEvents } from "../../../core/view/domEvents.mts";
import { defineMountableElement } from "../../../core/view/elementView.mts";
import { createReportLoadErrorView, mountAsyncView } from "../../../core/view/asyncMount.mts";
import { attachMountable, destroyMountable } from "../../../core/view/mountables.mts";
import translate from "../../../helpers/t.mts";
import { createElement } from "../../../shared/dom.mts";

type ScreenDiffPayload = import("../../../types/report.mts").ScreenDiffPayload;
type ScreenDiffAttachment = { name: string; source: string };
type ScreenDiffTestResultData = {
  labels?: { name: string; value: string }[];
  testStage?: {
    attachments?: ScreenDiffAttachment[];
  };
};

const settings = getSettingsForPlugin("screen-diff", { diffType: "diff" });

const renderImage = (src: string) =>
  createElement("div", {
    className: "screen-diff__container",
    children: createElement("img", {
      attrs: { src },
      className: "screen-diff__image",
    }),
  });

const findImage = (data: ScreenDiffTestResultData | undefined, name: string) => {
  const attachments = Array.isArray(data?.testStage?.attachments) ? data.testStage.attachments : [];
  if (attachments.length) {
    const matchedImage = attachments.find((attachment) => attachment.name === name);
    if (matchedImage) {
      return `data/attachments/${matchedImage.source}`;
    }
  }

  return null;
};

const renderDiffContent = (
  type: string,
  diffImage?: string | null,
  actualImage?: string | null,
  expectedImage?: string | null,
) => {
  if (type === "diff" && diffImage) {
    return renderImage(diffImage);
  }

  if (type === "overlay" && expectedImage) {
    return createElement("div", {
      className: "screen-diff__overlay screen-diff__container",
      children: [
        createElement("img", {
          attrs: { src: expectedImage },
          className: "screen-diff__image",
        }),
        createElement("div", {
          className: "screen-diff__image-over",
          children: createElement("img", {
            attrs: { src: actualImage || "" },
            className: "screen-diff__image",
          }),
        }),
      ],
    });
  }

  if (actualImage) {
    return renderImage(actualImage);
  }

  return translate("component.screenDiff.empty");
};

type ScreenDiffOptions = {
  diffImage?: string | null;
  actualImage?: string | null;
  expectedImage?: string | null;
};

const createScreenDiffView = (options: ScreenDiffOptions) => {
  const radioName = `screen-diff-type-${Math.random().toString(36).slice(2)}`;
  const adjustImageSize = (event: Event) => {
    const target = event.target as HTMLElement;
    const { width } = target.getBoundingClientRect();
    target.style.width = `${width}px`;
  };
  const el = defineMountableElement(document.createElement("div"), {
    radioName,
    adjustImageSize,
    onOverlayMove(event: MouseEvent) {
      const pageX = event.pageX;
      const containerScroll = el.querySelector(".screen-diff__container")?.scrollLeft || 0;
      const elementX = (event.currentTarget as HTMLElement).getBoundingClientRect().left;
      const delta = pageX - elementX + containerScroll;
      const imageOver = el.querySelector(".screen-diff__image-over");
      if (imageOver instanceof HTMLElement) {
        imageOver.style.width = `${delta}px`;
      }
    },
    onDiffTypeChange(event: Event) {
      settings.save("diffType", (event.target as HTMLInputElement).value);
      el.render?.();
    },
  });
  let releaseEvents = () => {};

  Object.assign(el, {
    render() {
      releaseEvents();
      const diffType = settings.get("diffType") === "overlay" ? "overlay" : "diff";
      el.className = "pane__section";
      if (!options.diffImage && !options.actualImage && !options.expectedImage) {
        el.replaceChildren();
        return el;
      }

      el.replaceChildren(
        createElement("h3", {
          className: "pane__section-title",
          text: translate("component.screenDiff.name"),
        }),
        createElement("div", {
          className: "screen-diff__content",
          children: [
            createElement("div", {
              className: "screen-diff__switchers",
              children: [
                createElement("label", {
                  children: [
                    createElement("input", {
                      attrs: { name: radioName, type: "radio", value: "diff" },
                    }),
                    ` ${translate("component.screenDiff.showDiff")}`,
                  ],
                }),
                createElement("label", {
                  children: [
                    createElement("input", {
                      attrs: { name: radioName, type: "radio", value: "overlay" },
                    }),
                    ` ${translate("component.screenDiff.showOverlay")}`,
                  ],
                }),
              ],
            }),
            renderDiffContent(
              diffType,
              options.diffImage,
              options.actualImage,
              options.expectedImage,
            ),
          ],
        }),
      );
      releaseEvents = bindDelegatedEvents({
        root: el,
        events: {
          "mousemove .screen-diff__overlay": "onOverlayMove",
          "click .screen-diff__switchers input": "onDiffTypeChange",
          "change .screen-diff__switchers input": "onDiffTypeChange",
        },
        context: el,
      });
      const activeSwitcher = el.querySelector(
        `[name="${radioName}"][value="${diffType || "diff"}"]`,
      );
      if (activeSwitcher instanceof HTMLInputElement) {
        activeSwitcher.checked = true;
      }
      if (diffType === "overlay") {
        el.querySelector(".screen-diff__image-over img")?.addEventListener("load", adjustImageSize);
      }
      return el;
    },
    destroy() {
      releaseEvents();
      el.remove();
    },
  });

  return el;
};

type ScreenDiffAttachmentOptions = {
  sourceUrl: string;
};

export const ScreenDiffAttachmentView = (options: ScreenDiffAttachmentOptions) => {
  const el = defineMountableElement(document.createElement("div"), {});
  let requestId = 0;
  let subView: import("../../../core/view/types.mts").Mountable | null = null;

  const destroySubView = () => {
    if (!subView) {
      return;
    }

    destroyMountable(subView);
    subView = null;
  };

  const mountSubView = (view: import("../../../core/view/types.mts").Mountable) => {
    const container = el.querySelector(".screen-diff-view");
    if (!(container instanceof Element)) {
      return;
    }

    destroySubView();
    subView = attachMountable(container, view);
  };

  Object.assign(el, {
    render() {
      destroySubView();
      el.replaceChildren(createElement("div", { className: "screen-diff-view" }));
      const currentRequestId = ++requestId;
      void mountAsyncView({
        createError: (error) => createReportLoadErrorView(error),
        createSuccess: (data) =>
          createScreenDiffView({
            diffImage: data.diff,
            actualImage: data.actual,
            expectedImage: data.expected,
          }),
        load: () => fetchReportJson<ScreenDiffPayload>(options.sourceUrl),
        mount: mountSubView,
        shouldIgnore: () => currentRequestId !== requestId || !el.isConnected,
      });
      return el;
    },
    attachToDom() {
      subView?.attachToDom?.();
    },
    detachFromDom() {
      subView?.detachFromDom?.();
    },
    destroy() {
      el.detachFromDom?.();
      requestId += 1;
      destroySubView();
      el.remove();
    },
  });

  return el;
};

type ScreenDiffTestResultOptions = {
  data: ScreenDiffTestResultData;
};

export const ScreenDiffTestResultView = (options: ScreenDiffTestResultOptions) => {
  const el = defineMountableElement(document.createElement("div"), {});
  let subView: import("../../../core/view/types.mts").Mountable | null = null;

  const destroySubView = () => {
    if (!subView) {
      return;
    }

    destroyMountable(subView);
    subView = null;
  };

  const mountSubView = (view: import("../../../core/view/types.mts").Mountable) => {
    const container = el.querySelector(".screen-diff-view");
    if (!(container instanceof Element)) {
      return;
    }

    destroySubView();
    subView = attachMountable(container, view);
  };

  Object.assign(el, {
    render() {
      destroySubView();
      el.replaceChildren(createElement("div", { className: "screen-diff-view" }));
      const labels = Array.isArray(options?.data.labels) ? options.data.labels : [];
      const testType = labels.find((label) => label.name === "testType");
      if (!testType || testType.value !== "screenshotDiff") {
        return el;
      }

      mountSubView(
        createScreenDiffView({
          diffImage: findImage(options?.data, "diff"),
          actualImage: findImage(options?.data, "actual"),
          expectedImage: findImage(options?.data, "expected"),
        }),
      );
      return el;
    },
    attachToDom() {
      subView?.attachToDom?.();
    },
    detachFromDom() {
      subView?.detachFromDom?.();
    },
    destroy() {
      el.detachFromDom?.();
      destroySubView();
      el.remove();
    },
  });

  return el;
};
