import "./FallbackAttachmentPreviewView.scss";
import b from "../../../shared/bem/index.mts";
import {
  createAttachmentSourceUrlPreview,
  createDiv,
  createDownloadAction,
  joinClassNames,
  type AttachmentPreviewComponent,
  type AttachmentPreviewOptions,
} from "./BaseAttachmentPreviewView.mts";

const renderFallbackAttachmentPreviewView = ({
  attachment,
  className,
  fullScreen,
  previewDisabledReason,
  sourceUrl,
}: AttachmentPreviewOptions) => {
  const fallbackContainer = createDiv(
    joinClassNames(b("attachment-preview", { fallback: true, fullscreen: fullScreen }), className),
  );
  if (previewDisabledReason) {
    const message = createDiv(b("attachment-preview", "preview-message"));
    message.textContent = previewDisabledReason;
    fallbackContainer.appendChild(message);
  }
  fallbackContainer.appendChild(createDownloadAction(attachment, sourceUrl));
  return fallbackContainer;
};

export const FallbackAttachmentPreviewView: AttachmentPreviewComponent = (options) =>
  createAttachmentSourceUrlPreview(options, (sourceUrl) =>
    renderFallbackAttachmentPreviewView({ ...options, sourceUrl }),
  );
