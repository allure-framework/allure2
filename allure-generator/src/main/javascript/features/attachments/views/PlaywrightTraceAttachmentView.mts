import "./PlaywrightTraceAttachmentView.scss";
import { defineMountableElement } from "../../../core/view/elementView.mts";
import { fetchReportBlob, normalizeReportDataError } from "../../../core/services/reportData.mts";
import { attachMountable, destroyMountable } from "../../../core/view/mountables.mts";
import translate from "../../../helpers/t.mts";
import { createElement } from "../../../shared/dom.mts";
import { sanitizeResourceUrl } from "../../../shared/url.mts";
import ErrorSplashView from "../../../shared/ui/ErrorSplashView.mts";
import LoaderView from "../../../shared/ui/LoaderView.mts";
import { createModalHeaderActionsEvent } from "../../../shared/ui/modalHeaderActions.mts";
import {
  createAttachmentSourceUrlPreview,
  type AttachmentPreviewComponent,
} from "./BaseAttachmentPreviewView.mts";

type Attachment = import("../../../types/report.mts").Attachment;
type Mountable = import("../../../core/view/types.mts").Mountable;
type ReportDataError = import("../../../core/services/reportData.mts").ReportDataError;

type PlaywrightTraceAttachmentContentOptions = {
  sourceUrl: string;
  attachment: Attachment;
};

// The hosted Playwright Trace Viewer loads the trace handed over via postMessage
// on iframe load. It accepts cross-origin messages, so no service worker or
// bundled assets are required: the handoff works the same for single-file,
// directory, and proxied (Jenkins / third-party) reports because the trace
// bytes are read by the report and pushed into the viewer directly.
const TRACE_VIEWER_URL = "https://trace.playwright.dev/";
const TRACE_VIEWER_ORIGIN = "https://trace.playwright.dev";

// The hosted viewer registers its postMessage listener from a React passive
// effect that may run after the iframe load event fires, so a single handoff on
// load can lose the race and be dropped (the viewer stays on its empty "Drop
// Playwright Trace to load" screen). Re-post the trace a few times until the
// listener is attached. Posting again after a successful load just reloads the
// same trace, so the schedule is kept short.
const TRACE_HANDOFF_DELAYS_MS = [0, 500, 1500, 3000];

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

const PlaywrightTraceAttachmentContentView = (options: PlaywrightTraceAttachmentContentOptions) => {
  const el = defineMountableElement(document.createElement("div"), {});
  let requestId = 0;
  let contentView: Mountable | null = null;
  let overlayView: Mountable | null = null;
  let isLoading = false;
  let loadError: ReportDataError | null = null;
  let traceBlob: Blob | null = null;
  let downloadUrl: string | null = null;
  let traceFrame: HTMLIFrameElement | null = null;
  let handoffTimers: number[] = [];
  let isViewerSettling = false;
  let safeDownloadUrl: string | null = null;

  const clearHandoffTimers = () => {
    handoffTimers.forEach((id) => window.clearTimeout(id));
    handoffTimers = [];
  };

  const revokeDownloadUrl = () => {
    if (downloadUrl?.startsWith("blob:")) {
      URL.revokeObjectURL(downloadUrl);
    }
    downloadUrl = null;
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
      clearHandoffTimers();
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

  const settleViewer = (iframe: HTMLIFrameElement) => {
    if (traceFrame !== iframe) {
      return;
    }

    isViewerSettling = false;
    const body = el.querySelector(".attachment-preview__trace-body");
    const overlay = el.querySelector(".attachment-preview__trace-overlay");
    if (body instanceof HTMLElement && overlay instanceof Element) {
      updateViewerOverlay(body, overlay);
    }
  };

  const postTraceToViewer = (iframe: HTMLIFrameElement) => {
    if (!traceBlob || traceFrame !== iframe) {
      return;
    }

    iframe.contentWindow?.postMessage(
      { method: "load", params: { trace: traceBlob } },
      TRACE_VIEWER_ORIGIN,
    );
  };

  const scheduleTraceHandoff = (iframe: HTMLIFrameElement) => {
    clearHandoffTimers();
    handoffTimers = TRACE_HANDOFF_DELAYS_MS.map((delay) =>
      window.setTimeout(() => postTraceToViewer(iframe), delay),
    );
  };

  const renderTraceViewer = (frameHost: HTMLElement) => {
    if (traceFrame?.parentElement === frameHost) {
      return;
    }

    destroyContentView();
    clearHandoffTimers();
    isViewerSettling = true;

    const iframe = createElement("iframe", {
      attrs: {
        height: "100%",
        id: "pw-trace-iframe",
        src: TRACE_VIEWER_URL,
        title: "Playwright Trace Viewer",
        width: "100%",
      },
      className: "attachment-preview__trace-frame",
    });
    iframe.addEventListener("load", () => {
      if (traceFrame !== iframe) {
        return;
      }

      scheduleTraceHandoff(iframe);
      settleViewer(iframe);
    });
    traceFrame = iframe;
    frameHost.replaceChildren(iframe);
  };

  const loadTrace = async (currentRequestId: number) => {
    isLoading = true;
    loadError = null;

    try {
      const blob = await fetchReportBlob(options.sourceUrl, {
        contentType: options.attachment.type,
      });
      if (currentRequestId !== requestId) {
        return;
      }

      traceBlob = blob;
      revokeDownloadUrl();
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
        updateViewerOverlay(body, overlay);
        mountContentView(
          ErrorSplashView({
            code: loadError.status,
            message: loadError.message,
          }),
        );
        return el;
      }

      if (traceBlob) {
        renderTraceViewer(frameHost);
        updateViewerOverlay(body, overlay);
        return el;
      }

      isViewerSettling = false;
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
      traceFrame = null;
      isViewerSettling = false;
      clearHandoffTimers();
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
