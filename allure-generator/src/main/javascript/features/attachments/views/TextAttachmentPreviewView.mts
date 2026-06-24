import "./TextAttachmentPreviewView.scss";
import b from "../../../shared/bem/index.mts";
import { createElement, createFragmentFromHtml } from "../../../shared/dom.mts";
import ansi from "../../../helpers/ansi.mts";
import {
  createDiv,
  hasPreviewData,
  LoadedPreviewView,
  loadAttachmentText,
  toText,
  type AttachmentPreviewComponent,
  type AttachmentPreviewOptions,
} from "./BaseAttachmentPreviewView.mts";

const createAnsiPre = (className: string, text: unknown) => {
  const pre = createElement("pre", { className });
  pre.append(createFragmentFromHtml(ansi(toText(text)), pre));
  return pre;
};

const renderLoadedTextAttachmentPreviewView = ({
  fullScreen,
  previewData,
}: AttachmentPreviewOptions) => {
  const textContainer = createDiv(b("attachment-preview", { fullscreen: fullScreen, text: true }));
  textContainer.appendChild(
    createAnsiPre(
      b("attachment-preview", "text"),
      typeof previewData === "string" ? previewData : "",
    ),
  );
  return textContainer;
};

export const TextAttachmentPreviewView: AttachmentPreviewComponent = (options) =>
  LoadedPreviewView({
    createLoaded: renderLoadedTextAttachmentPreviewView,
    hasLoadedData: hasPreviewData,
    load: () => loadAttachmentText(options),
    options,
    toLoadedOptions: (previewData) => ({ previewData }),
  });
