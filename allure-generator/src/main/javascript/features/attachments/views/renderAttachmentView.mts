import ansi from "../../../helpers/ansi.mts";
import translate from "../../../helpers/t.mts";
import b from "../../../shared/bem/index.mts";
import { appendChildren, createElement, createFragmentFromHtml } from "../../../shared/dom.mts";
import { createIconElement } from "../../../shared/icon/index.mts";
import { sanitizeNavigationUrl, sanitizeResourceUrl } from "../../../shared/url.mts";
import {
  createHtmlPreviewSrcDoc,
  getHtmlPreviewByteLength,
  HTML_PREVIEW_INITIAL_HEIGHT,
  HTML_PREVIEW_SOURCE_MAX_BYTES,
} from "../model/htmlPreview.mts";

type Attachment = import("../../../types/report.mts").Attachment;

type UriContent = {
  comment?: boolean;
  text: string;
};

type AttachmentRenderOptions = {
  type: string | null;
  content?: unknown;
  sourceUrl?: string | null;
  attachment: Attachment;
  fullScreen: boolean;
  htmlPreviewDisabledReason?: string | null;
  htmlPreviewToken?: string;
};

const toText = (value: unknown) => (value == null ? "" : String(value));

const createDiv = (className: string) => createElement("div", { className });

const createPre = (className: string, text: unknown) =>
  createElement("pre", { className, text: toText(text) });

const createAnsiPre = (className: string, text: unknown) => {
  const pre = createElement("pre", { className });
  pre.append(createFragmentFromHtml(ansi(toText(text)), pre));
  return pre;
};

const getHtmlAttachmentFrameTitle = (attachment: Attachment) =>
  translate("component.attachment.htmlPreviewTitle", {
    hash: { name: attachment.name || attachment.source },
  });

const setResourceUrl = (element: Element, attribute: "data" | "href" | "src", value: unknown) => {
  const url = sanitizeResourceUrl(value);
  if (url) {
    element.setAttribute(attribute, url);
  }
};

const createDownloadLink = (attachment: Attachment, sourceUrl?: string | null) => {
  const link = createElement("a", {
    attrs: { download: attachment.name || "" },
    className: "link",
    text: translate("component.attachment.download"),
  });
  setResourceUrl(link, "href", sourceUrl);
  return link;
};

const createDownloadAction = (attachment: Attachment, sourceUrl?: string | null) => {
  const downloadAction = createDiv("attachment__download");
  appendChildren(
    downloadAction,
    createIconElement("lineGeneralDownloadCloud", {
      className: "attachment__download-icon",
      size: "s",
    }),
    createDownloadLink(attachment, sourceUrl),
  );
  return downloadAction;
};

const appendTextOrLink = (container: HTMLElement, text: string) => {
  const safeHref = sanitizeNavigationUrl(text);
  if (!safeHref) {
    container.textContent = text;
    return;
  }

  const link = createElement("a", {
    attrs: {
      href: safeHref,
      rel: "noopener noreferrer",
      target: "_blank",
    },
    className: "link",
    text,
  });
  container.appendChild(link);
};

export const renderAttachmentView = ({
  type,
  content,
  sourceUrl,
  attachment,
  fullScreen,
  htmlPreviewDisabledReason,
  htmlPreviewToken = "",
}: AttachmentRenderOptions) => {
  if (type === "custom") {
    return createDiv("attachment__custom-view");
  }

  if (type === "playwright-trace") {
    return createDiv("attachment__trace-view");
  }

  if (type === "code") {
    const textContainer = createDiv(b("attachment__text-container", { fullscreen: fullScreen }));
    textContainer.appendChild(
      createPre("attachment__code", typeof content === "string" ? content : ""),
    );
    return textContainer;
  }

  if (type === "text") {
    const textContainer = createDiv(b("attachment__text-container", { fullscreen: fullScreen }));
    textContainer.appendChild(
      createAnsiPre("attachment__text", typeof content === "string" ? content : ""),
    );
    return textContainer;
  }

  if (type === "table") {
    const tableContainer = createDiv(b("attachment__table-container", { fullscreen: fullScreen }));
    const table = createElement("table", { className: "table attachment__table" });
    const tbody = createElement("tbody");

    const tableContent = Array.isArray(content) ? content : [];
    tableContent.forEach((row) => {
      const tr = createElement("tr");
      (Array.isArray(row) ? row : []).forEach((cell) => {
        tr.appendChild(createElement("td", { text: toText(cell) }));
      });
      tbody.appendChild(tr);
    });

    table.appendChild(tbody);
    tableContainer.appendChild(table);
    return tableContainer;
  }

  if (type === "image" || type === "svg") {
    const mediaContainer = createDiv(b("attachment__media-container", { fullscreen: fullScreen }));
    const image = createElement("img", { className: "attachment__media" });
    setResourceUrl(image, "src", sourceUrl);
    mediaContainer.appendChild(image);
    return mediaContainer;
  }

  if (type === "video") {
    const mediaContainer = createDiv("attachment__media-container");
    const video = createElement("video", { className: "attachment__media" });
    video.controls = true;
    const source = createElement("source", { attrs: { type: attachment.type } });
    setResourceUrl(source, "src", sourceUrl);
    appendChildren(video, source, translate("component.attachment.videoNotSupported"));
    mediaContainer.appendChild(video);
    return mediaContainer;
  }

  if (type === "uri") {
    const uriContainer = createDiv(b("attachment__text-container", { fullscreen: fullScreen }));
    const uriContent = Array.isArray(content) ? (content as UriContent[]) : [];

    uriContent.forEach(({ comment, text }) => {
      const paragraph = createElement("p", {
        className: b("attachment", "url", { comment }),
      });
      if (comment) {
        paragraph.textContent = text;
      } else {
        appendTextOrLink(paragraph, text);
      }
      uriContainer.appendChild(paragraph);
    });

    return uriContainer;
  }

  if (type === "html") {
    const htmlContent = typeof content === "string" ? content : "";

    if (htmlPreviewDisabledReason || !htmlContent) {
      const htmlFallbackContainer = createDiv(
        `${b("attachment__text-container", { fullscreen: fullScreen })} attachment__html-preview-fallback`,
      );
      const htmlFallbackStatus = createDiv("attachment__html-preview-status");
      appendChildren(
        htmlFallbackStatus,
        createElement("div", {
          className: "attachment__html-preview-message",
          text: htmlPreviewDisabledReason || translate("errors.loadingFailed"),
        }),
        createDownloadAction(attachment, sourceUrl),
      );
      htmlFallbackContainer.appendChild(htmlFallbackStatus);

      if (htmlContent && getHtmlPreviewByteLength(htmlContent) <= HTML_PREVIEW_SOURCE_MAX_BYTES) {
        htmlFallbackContainer.appendChild(
          createPre("attachment__code attachment__html-preview-source", htmlContent),
        );
      }

      return htmlFallbackContainer;
    }

    const iframe = createElement("iframe", {
      attrs: {
        "data-html-preview-token": htmlPreviewToken,
        frameborder: "0",
        referrerpolicy: "no-referrer",
        sandbox: "allow-scripts",
        srcdoc: createHtmlPreviewSrcDoc(htmlContent, htmlPreviewToken),
        style: fullScreen ? null : `height: ${HTML_PREVIEW_INITIAL_HEIGHT}px`,
        title: getHtmlAttachmentFrameTitle(attachment),
      },
      className: b("attachment__iframe", { fullscreen: fullScreen }),
    });
    return iframe;
  }

  const fallbackContainer = createDiv(b("attachment__text-container", { fullscreen: fullScreen }));
  fallbackContainer.appendChild(createDownloadAction(attachment, sourceUrl));
  return fallbackContainer;
};
