import "./CodeAttachmentPreviewView.scss";
import b from "../../../shared/bem/index.mts";
import highlight from "../../../utils/highlight.mts";
import {
  createDiv,
  createPre,
  getAttachmentSubtype,
  hasPreviewData,
  LoadedPreviewView,
  loadAttachmentText,
  type AttachmentPreviewComponent,
  type AttachmentPreviewOptions,
} from "./BaseAttachmentPreviewView.mts";

const renderLoadedCodeAttachmentPreviewView = ({
  attachment,
  codeLanguage,
  fullScreen,
  previewData,
}: AttachmentPreviewOptions) => {
  const textContainer = createDiv(b("attachment-preview", { code: true, fullscreen: fullScreen }));
  const codeBlock = createPre(
    b("attachment-preview", "code"),
    typeof previewData === "string" ? previewData : "",
  );
  const language = codeLanguage ?? getAttachmentSubtype(attachment);

  if (language) {
    codeBlock.classList.add(`language-${language}`);
  }

  highlight.highlightElement(codeBlock);
  textContainer.appendChild(codeBlock);
  return textContainer;
};

export const CodeAttachmentPreviewView: AttachmentPreviewComponent = (options) =>
  LoadedPreviewView({
    createLoaded: renderLoadedCodeAttachmentPreviewView,
    hasLoadedData: hasPreviewData,
    load: () => loadAttachmentText(options),
    options,
    toLoadedOptions: (previewData) => ({ previewData }),
  });
