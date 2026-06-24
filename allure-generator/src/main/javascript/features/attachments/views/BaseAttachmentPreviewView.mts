import { fetchReportText, reportDataUrl } from "../../../core/services/reportData.mts";
import { mountAsyncView } from "../../../core/view/asyncMount.mts";
import { defineMountableElement } from "../../../core/view/elementView.mts";
import { attachMountable, destroyMountable } from "../../../core/view/mountables.mts";
import translate from "../../../helpers/t.mts";
import { appendChildren, createElement } from "../../../shared/dom.mts";
import { createIconElement } from "../../../shared/icon/index.mts";
import { sanitizeNavigationUrl, sanitizeResourceUrl } from "../../../shared/url.mts";

import type { AttachmentPreviewView } from "../model/attachmentPreviewView.mts";

type Attachment = import("../../../types/report.mts").Attachment;
type Mountable = import("../../../core/view/types.mts").Mountable;

export type AttachmentPreviewOptions = {
  view: AttachmentPreviewView | null;
  attachment: Attachment;
  fullScreen: boolean;
  className?: string;
  codeLanguage?: string;
  htmlPreviewDisabledReason?: string | null;
  previewData?: unknown;
  sourceUrl?: string | null;
};

export type AttachmentPreviewComponent = (options: AttachmentPreviewOptions) => Mountable;

type AsyncAttachmentPreviewOptions<TData> = {
  createSuccess: (data: TData, setCleanup: (cleanup?: () => void) => void) => Mountable;
  load: () => Promise<TData> | TData;
};

type LoadedPreviewViewOptions<TData> = {
  createLoaded: (options: AttachmentPreviewOptions) => Mountable;
  hasLoadedData: (options: AttachmentPreviewOptions) => boolean;
  load: () => Promise<TData> | TData;
  options: AttachmentPreviewOptions;
  toLoadedOptions: (
    data: TData,
    setCleanup: (cleanup?: () => void) => void,
  ) => Partial<AttachmentPreviewOptions>;
};

export const toText = (value: unknown) => (value == null ? "" : String(value));

export const createDiv = (className: string) => createElement("div", { className });

export const createPre = (className: string, text: unknown) =>
  createElement("pre", { className, text: toText(text) });

export const joinClassNames = (...classNames: (string | null | undefined | false)[]) =>
  classNames.filter(Boolean).join(" ");

export const attachmentUrl = (attachment: Attachment) => `data/attachments/${attachment.source}`;

export const getAttachmentSubtype = (attachment: Attachment) => attachment.type.split("/").pop() || "";

export const hasPreviewData = (options: AttachmentPreviewOptions) =>
  Object.prototype.hasOwnProperty.call(options, "previewData");

export const hasSourceUrl = (options: AttachmentPreviewOptions) => Boolean(options.sourceUrl);

export const loadAttachmentText = (options: AttachmentPreviewOptions) =>
  fetchReportText(attachmentUrl(options.attachment), {
    contentType: options.attachment.type,
  });

export const loadAttachmentSourceUrl = (options: AttachmentPreviewOptions) =>
  reportDataUrl(attachmentUrl(options.attachment), options.attachment.type);

export const setResourceUrl = (
  element: Element,
  attribute: "data" | "href" | "src",
  value: unknown,
) => {
  const url = sanitizeResourceUrl(value);
  if (url) {
    element.setAttribute(attribute, url);
  }
};

export const createDownloadLink = (attachment: Attachment, sourceUrl?: string | null) => {
  const link = createElement("a", {
    attrs: { download: attachment.name || "" },
    className: "link",
    text: translate("component.attachment.download"),
  });
  setResourceUrl(link, "href", sourceUrl);
  return link;
};

export const createDownloadAction = (attachment: Attachment, sourceUrl?: string | null) => {
  const downloadAction = createDiv("attachment-preview__download");
  appendChildren(
    downloadAction,
    createIconElement("lineGeneralDownloadCloud", {
      className: "attachment-preview__download-icon",
      size: "s",
    }),
    createDownloadLink(attachment, sourceUrl),
  );
  return downloadAction;
};

export const appendTextOrLink = (container: HTMLElement, text: string) => {
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

export const createAsyncAttachmentPreview = <TData,>({
  createSuccess,
  load,
}: AsyncAttachmentPreviewOptions<TData>) => {
  const el = defineMountableElement(document.createElement("div"), {});
  let requestId = 0;
  let subView: Mountable | null = null;
  let subViewCleanup: (() => void) | null = null;
  let pendingCleanup: (() => void) | null = null;

  const destroySubView = () => {
    subViewCleanup?.();
    subViewCleanup = null;

    if (!subView) {
      return;
    }

    destroyMountable(subView);
    subView = null;
  };

  const mountSubView = (view: Mountable) => {
    destroySubView();
    subViewCleanup = pendingCleanup;
    pendingCleanup = null;
    subView = attachMountable(el, view);
  };

  Object.assign(el, {
    render() {
      destroySubView();
      const currentRequestId = ++requestId;

      void mountAsyncView({
        createSuccess: (data) =>
          createSuccess(data, (cleanup) => (pendingCleanup = cleanup || null)),
        load,
        mount: mountSubView,
        shouldIgnore: () => currentRequestId !== requestId,
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
      requestId += 1;
      destroySubView();
      el.remove();
    },
  });

  return el;
};

export const LoadedPreviewView = <TData,>({
  createLoaded,
  hasLoadedData,
  load,
  options,
  toLoadedOptions,
}: LoadedPreviewViewOptions<TData>) =>
  hasLoadedData(options)
    ? createLoaded(options)
    : createAsyncAttachmentPreview({
        createSuccess: (data, setCleanup) =>
          createLoaded({
            ...options,
            ...toLoadedOptions(data, setCleanup),
          }),
        load,
      });

export const createAttachmentSourceUrlPreview = (
  options: AttachmentPreviewOptions,
  createSuccess: (sourceUrl: string) => Mountable,
) =>
  LoadedPreviewView({
    createLoaded: ({ sourceUrl }) => createSuccess(sourceUrl || ""),
    hasLoadedData: hasSourceUrl,
    load: () => loadAttachmentSourceUrl(options),
    options,
    toLoadedOptions: (sourceUrl) => ({ sourceUrl }),
  });
