import "./PlaywrightTraceAttachmentView.scss";
import { defineMountableElement } from "../../../core/view/elementView.mts";
import {
  fetchReportBlob,
  fetchReportJson,
  normalizeReportDataError,
  reportDataUrl,
} from "../../../core/services/reportData.mts";
import { attachMountable, destroyMountable } from "../../../core/view/mountables.mts";
import translate from "../../../helpers/t.mts";
import { createElement } from "../../../shared/dom.mts";
import { sanitizeNavigationUrl, sanitizeResourceUrl } from "../../../shared/url.mts";
import ErrorSplashView from "../../../shared/ui/ErrorSplashView.mts";
import LoaderView from "../../../shared/ui/LoaderView.mts";
import { createModalHeaderActionsEvent } from "../../../shared/ui/modalHeaderActions.mts";
import {
  createAttachmentSourceUrlPreview,
  type AttachmentPreviewComponent,
} from "./BaseAttachmentPreviewView.mts";
import {
  PLAYWRIGHT_TRACE_VIEWER_INFO_URL,
  type PlaywrightTraceViewerInfo,
} from "../model/playwrightTrace.mts";

type Attachment = import("../../../types/report.mts").Attachment;
type Mountable = import("../../../core/view/types.mts").Mountable;
type ReportDataError = import("../../../core/services/reportData.mts").ReportDataError;

type PlaywrightTraceAttachmentContentOptions = {
  sourceUrl: string;
  attachment: Attachment;
};

const PLAYWRIGHT_ISSUE_URL = "https://github.com/microsoft/playwright/issues/40960";
const PLAYWRIGHT_TRACE_VIEWER_MANUAL_URL = "https://trace.playwright.dev/";

type TraceViewerUnavailableReason = "singleFile" | "nonHttp" | "missingAssets";

let traceViewerInfoPromise: Promise<PlaywrightTraceViewerInfo | null> | null = null;

const normalizeTraceViewerInfo = (
  info: PlaywrightTraceViewerInfo,
): PlaywrightTraceViewerInfo | null => {
  const url = typeof info?.url === "string" ? info.url.trim() : "";

  return url ? { url } : null;
};

const loadTraceViewerInfo = async (): Promise<PlaywrightTraceViewerInfo | null> => {
  if (typeof window.__allurePlaywrightTraceViewer !== "undefined") {
    return window.__allurePlaywrightTraceViewer;
  }

  traceViewerInfoPromise ??= fetchReportJson<PlaywrightTraceViewerInfo>(
    PLAYWRIGHT_TRACE_VIEWER_INFO_URL,
  )
    .then((info) => {
      window.__allurePlaywrightTraceViewer = normalizeTraceViewerInfo(info);
      return window.__allurePlaywrightTraceViewer;
    })
    .catch(() => {
      window.__allurePlaywrightTraceViewer = null;
      return null;
    });

  return traceViewerInfoPromise;
};

const createDownloadLink = (attachment: Attachment, href: string | null) => {
  if (!href) {
    return null;
  }

  return createElement("a", {
    attrs: {
      download: attachment.source,
      href,
      target: "_blank",
      rel: "noopener noreferrer",
    },
    className: "link attachment-preview__trace-download",
    text: translate("component.attachment.download"),
  });
};

const createNavigationLink = (href: string, text: string) =>
  createElement("a", {
    attrs: {
      href: sanitizeNavigationUrl(href),
      target: "_blank",
      rel: "noopener noreferrer",
    },
    className: "link",
    text,
  });

const isSingleFileReport = () => !!window.reportData;

const getUnavailableReason = (): TraceViewerUnavailableReason => {
  if (isSingleFileReport()) {
    return "singleFile";
  }

  if (window.location.protocol !== "http:" && window.location.protocol !== "https:") {
    return "nonHttp";
  }

  return "missingAssets";
};

const getProtocolLabel = () => window.location.protocol || "file:";

const PlaywrightTraceAttachmentContentView = (options: PlaywrightTraceAttachmentContentOptions) => {
  const el = defineMountableElement(document.createElement("div"), {});
  let requestId = 0;
  let contentView: Mountable | null = null;
  let overlayView: Mountable | null = null;
  let isLoading = false;
  let loadError: ReportDataError | null = null;
  let traceBlob: Blob | null = null;
  let traceUrl: string | null = null;
  let traceViewerInfo: PlaywrightTraceViewerInfo | null = null;
  let downloadUrl: string | null = null;
  let traceFrame: HTMLIFrameElement | null = null;
  let viewerRevealTimeoutId: number | null = null;
  let isViewerSettling = false;
  let safeDownloadUrl: string | null = null;

  const revokeDownloadUrl = () => {
    if (downloadUrl?.startsWith("blob:")) {
      URL.revokeObjectURL(downloadUrl);
    }
    downloadUrl = null;
  };

  const clearViewerReveal = () => {
    if (viewerRevealTimeoutId === null) {
      return;
    }

    window.clearTimeout(viewerRevealTimeoutId);
    viewerRevealTimeoutId = null;
  };

  const destroyContentView = () => {
    if (!contentView) {
      return;
    }

    destroyMountable(contentView);
    contentView = null;
  };

  const destroyOverlayView = () => {
    if (!overlayView) {
      return;
    }

    destroyMountable(overlayView);
    overlayView = null;
  };

  const mountContentView = (view: Mountable) => {
    const frameHost = el.querySelector(".attachment-preview__trace-frame-host");
    if (!(frameHost instanceof Element)) {
      return;
    }

    destroyContentView();
    if (traceFrame?.parentElement === frameHost) {
      traceFrame = null;
      isViewerSettling = false;
      clearViewerReveal();
    }
    contentView = attachMountable(frameHost, view);
  };

  const ensureTraceLayout = () => {
    const currentActions = el.querySelector(".attachment-preview__trace-actions");
    const currentBody = el.querySelector(".attachment-preview__trace-body");
    const currentFrameHost = el.querySelector(".attachment-preview__trace-frame-host");
    const currentOverlay = el.querySelector(".attachment-preview__trace-overlay");

    if (
      currentActions instanceof HTMLDivElement &&
      currentBody instanceof HTMLDivElement &&
      currentFrameHost instanceof HTMLDivElement &&
      currentOverlay instanceof HTMLDivElement
    ) {
      return {
        actions: currentActions,
        body: currentBody,
        frameHost: currentFrameHost,
        overlay: currentOverlay,
      };
    }

    const actions = createElement("div", {
      className: "attachment-preview__trace-actions",
    });
    const body = createElement("div", {
      className: "attachment-preview__trace-body attachment-preview__trace-body_loading",
      children: [
        createElement("div", {
          className: "attachment-preview__trace-frame-host",
        }),
        createElement("div", {
          className: "attachment-preview__trace-overlay",
        }),
      ],
    });
    el.replaceChildren(actions, body);

    return {
      actions,
      body,
      frameHost: body.querySelector(".attachment-preview__trace-frame-host") as HTMLDivElement,
      overlay: body.querySelector(".attachment-preview__trace-overlay") as HTMLDivElement,
    };
  };

  const renderDownloadActions = (actions: HTMLElement) => {
    actions.replaceChildren();

    const downloadLink = createDownloadLink(options.attachment, safeDownloadUrl);
    const headerActionsEvent = createModalHeaderActionsEvent(downloadLink ? [downloadLink] : []);
    const handledByModalHeader = !el.dispatchEvent(headerActionsEvent);

    if (!handledByModalHeader && downloadLink) {
      actions.appendChild(downloadLink);
    }
  };

  const createTraceUnavailableView = (): HTMLElement => {
    const reason = getUnavailableReason();
    const downloadLink = createDownloadLink(options.attachment, safeDownloadUrl);

    if (downloadLink) {
      downloadLink.textContent = translate("component.playwrightTrace.stepDownload");
      downloadLink.className = "link";
    }

    const issueRow =
      reason === "singleFile"
        ? createElement("p", {
            className: "attachment-preview__trace-instructions-note",
            children: [
              `${translate("component.playwrightTrace.issuePrefix")} `,
              createNavigationLink(
                PLAYWRIGHT_ISSUE_URL,
                translate("component.playwrightTrace.issueLabel"),
              ),
              `.`,
            ],
          })
        : null;

    return createElement("section", {
      className: "attachment-preview__trace-instructions",
      attrs: {
        "aria-labelledby": "playwright-trace-unavailable-title",
      },
      children: createElement("div", {
        className: "attachment-preview__trace-instructions-content",
        children: [
          createElement("h1", {
            attrs: {
              id: "playwright-trace-unavailable-title",
            },
            className: "attachment-preview__trace-instructions-title",
            text: translate("component.playwrightTrace.unavailableTitle"),
          }),
          createElement("p", {
            className: "attachment-preview__trace-instructions-reason",
            text: translate(`component.playwrightTrace.${reason}Reason`, {
              hash: {
                protocol: getProtocolLabel(),
              },
            }),
          }),
          issueRow,
          createElement("h2", {
            className: "attachment-preview__trace-instructions-subtitle",
            text: translate("component.playwrightTrace.stepsTitle"),
          }),
          createElement("ol", {
            className: "attachment-preview__trace-instructions-list",
            children: [
              createElement("li", {
                children: downloadLink || translate("component.playwrightTrace.stepDownload"),
              }),
              createElement("li", {
                children: createNavigationLink(
                  PLAYWRIGHT_TRACE_VIEWER_MANUAL_URL,
                  translate("component.playwrightTrace.stepOpenViewer"),
                ),
              }),
              createElement("li", {
                text: translate("component.playwrightTrace.stepUpload"),
              }),
            ],
          }),
        ],
      }),
    });
  };

  const updateViewerOverlay = (body: HTMLElement, overlay: Element) => {
    body.classList.toggle("attachment-preview__trace-body_loading", isViewerSettling);

    if (!isViewerSettling) {
      destroyOverlayView();
      overlay.replaceChildren();
      return;
    }

    destroyOverlayView();
    overlayView = attachMountable(overlay, LoaderView());
  };

  const canRenderTraceViewer = () =>
    window.location.protocol === "http:" || window.location.protocol === "https:";

  const toAbsoluteTraceUrl = (url: string): string => {
    if (url.startsWith("data:") || url.startsWith("blob:")) {
      return url;
    }

    return new URL(url, window.location.href).toString();
  };

  const resolveTraceUrl = async (): Promise<string> => {
    if (options.sourceUrl.startsWith("data:") || options.sourceUrl.startsWith("blob:")) {
      return options.sourceUrl;
    }

    return reportDataUrl(options.sourceUrl, options.attachment.type);
  };

  const buildTraceViewerUrl = (): string | null => {
    if (!traceViewerInfo || !traceUrl) {
      return null;
    }

    const url = new URL(traceViewerInfo.url, window.location.href);
    url.searchParams.set("trace", traceUrl);

    return url.toString();
  };

  const renderTraceViewer = (frameHost: HTMLElement) => {
    const viewerUrl = buildTraceViewerUrl();
    if (!viewerUrl) {
      return;
    }

    if (traceFrame?.parentElement === frameHost) {
      return;
    }

    destroyContentView();
    clearViewerReveal();
    isViewerSettling = true;

    const iframe = createElement("iframe", {
      attrs: {
        height: "100%",
        id: "pw-trace-iframe",
        src: viewerUrl,
        title: "Playwright Trace Viewer",
        width: "100%",
      },
      className: "attachment-preview__trace-frame",
    });
    traceFrame = iframe;
    frameHost.replaceChildren(iframe);
    viewerRevealTimeoutId = window.setTimeout(() => {
      viewerRevealTimeoutId = null;
      if (traceFrame !== iframe) {
        return;
      }

      isViewerSettling = false;
      const body = el.querySelector(".attachment-preview__trace-body");
      const overlay = el.querySelector(".attachment-preview__trace-overlay");
      if (body instanceof HTMLElement && overlay instanceof Element) {
        updateViewerOverlay(body, overlay);
      }
    }, 2000);
  };

  const loadTrace = async (currentRequestId: number) => {
    isLoading = true;
    loadError = null;

    try {
      const [viewerInfo, resolvedTraceUrl] = await Promise.all([
        loadTraceViewerInfo(),
        resolveTraceUrl(),
      ]);
      if (currentRequestId !== requestId) {
        return;
      }

      traceViewerInfo = viewerInfo;
      traceUrl = toAbsoluteTraceUrl(resolvedTraceUrl);
      revokeDownloadUrl();

      if (viewerInfo && canRenderTraceViewer()) {
        downloadUrl = traceUrl;
        isLoading = false;
        loadError = null;

        if (el.isConnected) {
          el.render?.();
        }

        return;
      }

      if (!isSingleFileReport()) {
        downloadUrl = traceUrl;
        isLoading = false;
        loadError = null;

        if (el.isConnected) {
          el.render?.();
        }

        return;
      }

      const blob = await fetchReportBlob(options.sourceUrl, {
        contentType: options.attachment.type,
      });
      if (currentRequestId !== requestId) {
        return;
      }

      traceBlob = blob;
      downloadUrl = URL.createObjectURL(blob);
      isLoading = false;
      loadError = null;

      if (el.isConnected) {
        el.render?.();
      }
    } catch (error: unknown) {
      if (currentRequestId !== requestId) {
        return;
      }

      isLoading = false;
      loadError = normalizeReportDataError(error, {
        message: translate("errors.loadingFailed"),
        url: options.sourceUrl,
      });

      if (el.isConnected) {
        el.render?.();
      }
    }
  };

  Object.assign(el, {
    render() {
      safeDownloadUrl = sanitizeResourceUrl(downloadUrl || options.sourceUrl);

      el.className = "attachment-preview attachment-preview_trace";
      const { actions, body, frameHost, overlay } = ensureTraceLayout();
      renderDownloadActions(actions);

      if (loadError) {
        isViewerSettling = false;
        clearViewerReveal();
        updateViewerOverlay(body, overlay);
        mountContentView(
          ErrorSplashView({
            code: loadError.status,
            message: loadError.message,
          }),
        );
        return el;
      }

      if (traceUrl || traceBlob) {
        if (canRenderTraceViewer() && traceViewerInfo && traceUrl) {
          renderTraceViewer(frameHost);
        } else {
          isViewerSettling = false;
          clearViewerReveal();
          mountContentView(createTraceUnavailableView());
        }
        updateViewerOverlay(body, overlay);
        return el;
      }

      isViewerSettling = false;
      clearViewerReveal();
      updateViewerOverlay(body, overlay);
      mountContentView(LoaderView());
      if (!isLoading) {
        const currentRequestId = ++requestId;
        void loadTrace(currentRequestId);
      }

      return el;
    },
    attachToDom() {
      const actions = el.querySelector(".attachment-preview__trace-actions");
      if (actions instanceof HTMLElement) {
        renderDownloadActions(actions);
      }
      contentView?.attachToDom?.();
    },
    detachFromDom() {
      contentView?.detachFromDom?.();
    },
    destroy() {
      requestId += 1;
      isLoading = false;
      loadError = null;
      traceBlob = null;
      traceUrl = null;
      traceViewerInfo = null;
      traceFrame = null;
      clearViewerReveal();
      isViewerSettling = false;
      destroyContentView();
      destroyOverlayView();
      if (el.isConnected) {
        el.dispatchEvent(createModalHeaderActionsEvent([]));
      }
      revokeDownloadUrl();
      el.remove();
    },
  });

  return el;
};

export const PlaywrightTraceAttachmentView: AttachmentPreviewComponent = (options) =>
  createAttachmentSourceUrlPreview(options, (sourceUrl) =>
    PlaywrightTraceAttachmentContentView({
      attachment: options.attachment,
      sourceUrl,
    }),
  );
