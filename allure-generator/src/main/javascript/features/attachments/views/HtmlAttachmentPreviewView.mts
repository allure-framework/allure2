import "./HtmlAttachmentPreviewView.scss";
import b from "../../../shared/bem/index.mts";
import { createElement } from "../../../shared/dom.mts";
import translate from "../../../helpers/t.mts";
import { defineMountableElement } from "../../../core/view/elementView.mts";
import {
  createAsyncAttachmentPreview,
  createDiv,
  createDownloadAction,
  createPre,
  joinClassNames,
  loadAttachmentSourceUrl,
  loadAttachmentText,
  type AttachmentPreviewComponent,
  type AttachmentPreviewOptions,
} from "./BaseAttachmentPreviewView.mts";
import {
  createHtmlPreviewSrcDoc,
  getHtmlPreviewByteLength,
  getHtmlPreviewInvalidReason,
  getHtmlPreviewOversizeReason,
  HTML_PREVIEW_INITIAL_HEIGHT,
  HTML_PREVIEW_MAX_BYTES,
  HTML_PREVIEW_MAX_INLINE_HEIGHT,
  HTML_PREVIEW_MIN_HEIGHT,
  HTML_PREVIEW_RESIZE_MESSAGE_TYPE,
  HTML_PREVIEW_SOURCE_MAX_BYTES,
  isHtmlPreviewOversized,
  isRenderableHtmlPreview,
} from "./HtmlAttachmentPreviewView.helpers.mts";

const getHtmlAttachmentFrameTitle = ({
  name,
  source,
}: AttachmentPreviewOptions["attachment"]) =>
  translate("component.attachment.htmlPreviewTitle", {
    hash: { name: name || source },
  });

const HtmlFrameAttachmentPreviewView = ({
  attachment,
  fullScreen,
  previewData,
}: AttachmentPreviewOptions) => {
  const htmlContent = typeof previewData === "string" ? previewData : "";
  const htmlPreviewToken = `${attachment.uid || attachment.source}-${Math.random()
    .toString(36)
    .slice(2)}`;
  const frame = createElement("iframe", {
    attrs: {
      "data-html-preview-token": htmlPreviewToken,
      frameborder: "0",
      referrerpolicy: "no-referrer",
      sandbox: "allow-scripts",
      srcdoc: createHtmlPreviewSrcDoc(htmlContent, htmlPreviewToken),
      style: fullScreen ? null : `height: ${HTML_PREVIEW_INITIAL_HEIGHT}px`,
      title: getHtmlAttachmentFrameTitle(attachment),
    },
    className: joinClassNames(
      b("attachment-preview", { html: true }),
      b("attachment-preview", "frame", { fullscreen: fullScreen }),
    ),
  });

  if (fullScreen) {
    return frame;
  }

  let releaseMessages: (() => void) | null = null;
  const onMessage = (event: MessageEvent) => {
    if (event.source !== frame.contentWindow) {
      return;
    }

    const data = event.data as { height?: unknown; token?: unknown; type?: unknown };
    if (
      !data ||
      data.type !== HTML_PREVIEW_RESIZE_MESSAGE_TYPE ||
      data.token !== htmlPreviewToken
    ) {
      return;
    }

    const height = Number(data.height);
    if (!Number.isFinite(height) || height <= 0) {
      return;
    }

    const clampedHeight = Math.min(
      Math.max(Math.ceil(height), HTML_PREVIEW_MIN_HEIGHT),
      HTML_PREVIEW_MAX_INLINE_HEIGHT,
    );

    frame.style.height = `${clampedHeight}px`;
    frame.dataset.htmlPreviewHeight = String(clampedHeight);
    frame.dataset.htmlPreviewOverflow = height > HTML_PREVIEW_MAX_INLINE_HEIGHT ? "true" : "false";
  };

  return defineMountableElement(frame, {
    render() {
      return frame;
    },
    attachToDom() {
      if (releaseMessages) {
        return;
      }

      window.addEventListener("message", onMessage);
      releaseMessages = () => {
        window.removeEventListener("message", onMessage);
        releaseMessages = null;
      };
    },
    detachFromDom() {
      releaseMessages?.();
    },
    destroy() {
      releaseMessages?.();
      frame.remove();
    },
  });
};

const HtmlFallbackAttachmentPreviewView = ({
  attachment,
  fullScreen,
  htmlPreviewDisabledReason,
  previewData,
  sourceUrl,
}: AttachmentPreviewOptions) => {
  const htmlContent = typeof previewData === "string" ? previewData : "";
  const htmlFallbackContainer = createDiv(
    joinClassNames(
      b("attachment-preview", { fallback: true, fullscreen: fullScreen, html: true }),
    ),
  );
  const htmlFallbackStatus = createDiv(b("attachment-preview", "html-status"));
  htmlFallbackStatus.append(
    createElement("div", {
      className: b("attachment-preview", "html-message"),
      text: htmlPreviewDisabledReason || translate("errors.loadingFailed"),
    }),
    createDownloadAction(attachment, sourceUrl),
  );
  htmlFallbackContainer.appendChild(htmlFallbackStatus);

  if (htmlContent && getHtmlPreviewByteLength(htmlContent) <= HTML_PREVIEW_SOURCE_MAX_BYTES) {
    htmlFallbackContainer.appendChild(
      createPre(
        `${b("attachment-preview", "code")} ${b("attachment-preview", "html-source")}`,
        htmlContent,
      ),
    );
  }

  return htmlFallbackContainer;
};

export const HtmlAttachmentPreviewView: AttachmentPreviewComponent = (options) => {
  const loadSourceUrl = () => loadAttachmentSourceUrl(options);

  if (isHtmlPreviewOversized(options.attachment.size)) {
    return createAsyncAttachmentPreview({
      createSuccess: (sourceUrl) =>
        HtmlFallbackAttachmentPreviewView({
          ...options,
          htmlPreviewDisabledReason: getHtmlPreviewOversizeReason(),
          sourceUrl,
        }),
      load: loadSourceUrl,
    });
  }

  return createAsyncAttachmentPreview({
    createSuccess: ({ content, sourceUrl }) => {
      const htmlSize = getHtmlPreviewByteLength(content);
      if (htmlSize > HTML_PREVIEW_MAX_BYTES) {
        return HtmlFallbackAttachmentPreviewView({
          ...options,
          htmlPreviewDisabledReason: getHtmlPreviewOversizeReason(),
          sourceUrl,
        });
      }

      if (!isRenderableHtmlPreview(content)) {
        return HtmlFallbackAttachmentPreviewView({
          ...options,
          htmlPreviewDisabledReason: getHtmlPreviewInvalidReason(),
          previewData: content,
          sourceUrl,
        });
      }

      return HtmlFrameAttachmentPreviewView({
        ...options,
        previewData: content,
        sourceUrl,
      });
    },
    load: async () => {
      const content = await loadAttachmentText(options);
      const sourceUrl = await loadSourceUrl();

      return { content, sourceUrl };
    },
  });
};
