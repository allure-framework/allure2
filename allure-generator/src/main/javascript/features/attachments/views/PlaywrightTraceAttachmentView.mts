import { defineMountableElement } from "../../../core/view/elementView.mts";
import { fetchReportBlob, normalizeReportDataError } from "../../../core/services/reportData.mts";
import { attachMountable, destroyMountable } from "../../../core/view/mountables.mts";
import translate from "../../../helpers/t.mts";
import { createElement } from "../../../shared/dom.mts";
import { sanitizeResourceUrl } from "../../../shared/url.mts";
import ErrorSplashView from "../../../shared/ui/ErrorSplashView.mts";
import LoaderView from "../../../shared/ui/LoaderView.mts";
import {
  PLAYWRIGHT_TRACE_VIEWER_ORIGIN,
  PLAYWRIGHT_TRACE_VIEWER_URL,
} from "../model/playwrightTrace.mts";

type Attachment = import("../../../types/report.mts").Attachment;
type Mountable = import("../../../core/view/types.mts").Mountable;
type ReportDataError = import("../../../core/services/reportData.mts").ReportDataError;

type PlaywrightTraceAttachmentOptions = {
  sourceUrl: string;
  attachment: Attachment;
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
    className: "link attachment__trace-download",
    text: translate("component.attachment.download"),
  });
};

export const PlaywrightTraceAttachmentView = (options: PlaywrightTraceAttachmentOptions) => {
  const el = defineMountableElement(document.createElement("div"), {});
  const viewerUrl = PLAYWRIGHT_TRACE_VIEWER_URL;
  const viewerOrigin = PLAYWRIGHT_TRACE_VIEWER_ORIGIN;
  let requestId = 0;
  let contentView: Mountable | null = null;
  let overlayView: Mountable | null = null;
  let isLoading = false;
  let loadError: ReportDataError | null = null;
  let traceBlob: Blob | null = null;
  let downloadUrl: string | null = null;
  let traceFrame: HTMLIFrameElement | null = null;
  let traceLoadTimeoutId: number | null = null;
  let viewerRevealTimeoutId: number | null = null;
  let hasPostedTraceToViewer = false;
  let isViewerSettling = false;

  const revokeDownloadUrl = () => {
    if (downloadUrl?.startsWith("blob:")) {
      URL.revokeObjectURL(downloadUrl);
    }
    downloadUrl = null;
  };

  const clearTraceLoad = () => {
    if (traceLoadTimeoutId === null) {
      return;
    }

    window.clearTimeout(traceLoadTimeoutId);
    traceLoadTimeoutId = null;
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
    const frameHost = el.querySelector(".attachment__trace-frame-host");
    if (!(frameHost instanceof Element)) {
      return;
    }

    destroyContentView();
    if (traceFrame?.parentElement === frameHost) {
      traceFrame = null;
      hasPostedTraceToViewer = false;
      isViewerSettling = false;
      clearTraceLoad();
      clearViewerReveal();
    }
    contentView = attachMountable(frameHost, view);
  };

  const ensureTraceLayout = () => {
    const currentActions = el.querySelector(".attachment__trace-actions");
    const currentBody = el.querySelector(".attachment__trace-body");
    const currentFrameHost = el.querySelector(".attachment__trace-frame-host");
    const currentOverlay = el.querySelector(".attachment__trace-overlay");

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
      className: "attachment__trace-actions",
    });
    const body = createElement("div", {
      className: "attachment__trace-body attachment__trace-body_loading",
      children: [
        createElement("div", {
          className: "attachment__trace-frame-host",
        }),
        createElement("div", {
          className: "attachment__trace-overlay",
        }),
      ],
    });
    el.replaceChildren(actions, body);

    return {
      actions,
      body,
      frameHost: body.querySelector(".attachment__trace-frame-host") as HTMLDivElement,
      overlay: body.querySelector(".attachment__trace-overlay") as HTMLDivElement,
    };
  };

  const updateViewerOverlay = (body: HTMLElement, overlay: Element) => {
    body.classList.toggle("attachment__trace-body_loading", isViewerSettling);

    if (!isViewerSettling) {
      destroyOverlayView();
      overlay.replaceChildren();
      return;
    }

    destroyOverlayView();
    overlayView = attachMountable(overlay, LoaderView());
  };

  const postTraceToViewer = (iframe: HTMLIFrameElement, blob: Blob) => {
    iframe.contentWindow?.postMessage(
      {
        method: "load",
        params: {
          trace: blob,
        },
      },
      viewerOrigin,
    );
  };

  const scheduleTraceLoad = (iframe: HTMLIFrameElement, blob: Blob) => {
    if (hasPostedTraceToViewer) {
      return;
    }

    clearTraceLoad();

    // The hosted viewer adds its `message` listener shortly after the iframe `load`
    // event and may still be finishing its own startup work, so hand the blob off
    // once after a short settle window.
    traceLoadTimeoutId = window.setTimeout(() => {
      traceLoadTimeoutId = null;
      if (!iframe.isConnected || traceBlob !== blob || hasPostedTraceToViewer) {
        return;
      }

      hasPostedTraceToViewer = true;
      postTraceToViewer(iframe, blob);
    }, 1000);
  };

  const renderTraceViewer = (frameHost: HTMLElement) => {
    if (traceFrame?.parentElement === frameHost) {
      return;
    }

    destroyContentView();
    clearTraceLoad();
    clearViewerReveal();
    hasPostedTraceToViewer = false;
    isViewerSettling = true;

    const iframe = createElement("iframe", {
      attrs: {
        height: "100%",
        id: "pw-trace-iframe",
        src: viewerUrl,
        title: "Playwright Trace Viewer",
        width: "100%",
      },
      className: "attachment__trace-frame",
    });
    iframe.addEventListener("load", () => {
      if (!traceBlob) {
        return;
      }

      scheduleTraceLoad(iframe, traceBlob);
    });
    traceFrame = iframe;
    frameHost.replaceChildren(iframe);
    viewerRevealTimeoutId = window.setTimeout(() => {
      viewerRevealTimeoutId = null;
      if (traceFrame !== iframe) {
        return;
      }

      isViewerSettling = false;
      const body = el.querySelector(".attachment__trace-body");
      const overlay = el.querySelector(".attachment__trace-overlay");
      if (body instanceof HTMLElement && overlay instanceof Element) {
        updateViewerOverlay(body, overlay);
      }
    }, 2000);
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
      hasPostedTraceToViewer = false;
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
      const safeDownloadUrl = sanitizeResourceUrl(downloadUrl || options.sourceUrl);

      el.className = "attachment attachment_trace";
      const { actions, body, frameHost, overlay } = ensureTraceLayout();
      const downloadLink = createDownloadLink(options.attachment, safeDownloadUrl);
      actions.replaceChildren();
      if (downloadLink) {
        actions.appendChild(downloadLink);
      }

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

      if (traceBlob) {
        renderTraceViewer(frameHost);
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
      clearTraceLoad();
      clearViewerReveal();
      hasPostedTraceToViewer = false;
      isViewerSettling = false;
      destroyContentView();
      destroyOverlayView();
      revokeDownloadUrl();
      el.remove();
    },
  });

  return el;
};
