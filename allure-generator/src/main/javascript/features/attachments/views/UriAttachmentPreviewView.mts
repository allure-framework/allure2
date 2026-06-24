import "./UriAttachmentPreviewView.scss";
import b from "../../../shared/bem/index.mts";
import { createElement } from "../../../shared/dom.mts";
import {
  appendTextOrLink,
  createDiv,
  hasPreviewData,
  LoadedPreviewView,
  loadAttachmentText,
  type AttachmentPreviewComponent,
  type AttachmentPreviewOptions,
} from "./BaseAttachmentPreviewView.mts";

type UriContent = {
  comment?: boolean;
  text: string;
};

const parseUriPreviewData = (previewData: unknown): UriContent[] => {
  if (Array.isArray(previewData)) {
    return previewData as UriContent[];
  }

  const content = typeof previewData === "string" ? previewData : "";

  return content
    .split("\n")
    .map((line) => line.trim())
    .filter((line) => line.length > 0)
    .map((line) => ({
      comment: line.indexOf("#") === 0,
      text: line,
    }));
};

const renderLoadedUriAttachmentPreviewView = ({
  fullScreen,
  previewData,
}: AttachmentPreviewOptions) => {
  const uriContainer = createDiv(b("attachment-preview", { fullscreen: fullScreen, uri: true }));
  const uriContent = parseUriPreviewData(previewData);

  uriContent.forEach(({ comment, text }) => {
    const paragraph = createElement("p", {
      className: b("attachment-preview", "url", { comment }),
    });
    if (comment) {
      paragraph.textContent = text;
    } else {
      appendTextOrLink(paragraph, text);
    }
    uriContainer.appendChild(paragraph);
  });

  return uriContainer;
};

export const UriAttachmentPreviewView: AttachmentPreviewComponent = (options) =>
  LoadedPreviewView({
    createLoaded: renderLoadedUriAttachmentPreviewView,
    hasLoadedData: hasPreviewData,
    load: () => loadAttachmentText(options),
    options,
    toLoadedOptions: (previewData) => ({ previewData }),
  });
