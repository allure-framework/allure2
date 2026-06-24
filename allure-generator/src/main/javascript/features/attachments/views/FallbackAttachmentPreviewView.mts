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

export const renderFallbackAttachmentPreviewView = ({
  attachment,
  className,
  fullScreen,
  sourceUrl,
}: AttachmentPreviewOptions) => {
  const fallbackContainer = createDiv(
    joinClassNames(
      b("attachment-preview", { fallback: true, fullscreen: fullScreen }),
      className,
    ),
  );
  fallbackContainer.appendChild(createDownloadAction(attachment, sourceUrl));
  return fallbackContainer;
};

export const FallbackAttachmentPreviewView: AttachmentPreviewComponent = (options) =>
  createAttachmentSourceUrlPreview(options, (sourceUrl) =>
    renderFallbackAttachmentPreviewView({ ...options, sourceUrl }),
  );
