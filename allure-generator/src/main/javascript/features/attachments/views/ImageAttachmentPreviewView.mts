import "./ImageAttachmentPreviewView.scss";
import router from "../../../core/routing/router.mts";
import b from "../../../shared/bem/index.mts";
import { createElement } from "../../../shared/dom.mts";
import {
  createAttachmentSourceUrlPreview,
  createDiv,
  joinClassNames,
  setResourceUrl,
  type AttachmentPreviewComponent,
  type AttachmentPreviewOptions,
} from "./BaseAttachmentPreviewView.mts";

export const renderLoadedImageAttachmentPreviewView = ({
  attachment,
  className,
  fullScreen,
  sourceUrl,
}: AttachmentPreviewOptions) => {
  const mediaContainer = createDiv(
    joinClassNames(
      b("attachment-preview", { fullscreen: fullScreen, image: true }),
      className,
    ),
  );
  const image = createElement("img", { className: b("attachment-preview", "media") });
  setResourceUrl(image, "src", sourceUrl);
  mediaContainer.appendChild(image);
  mediaContainer.addEventListener("click", () => {
    router.setSearch({
      attachment: fullScreen ? null : attachment.uid,
    });
  });
  return mediaContainer;
};

export const ImageAttachmentPreviewView: AttachmentPreviewComponent = (options) =>
  createAttachmentSourceUrlPreview(options, (sourceUrl) =>
    renderLoadedImageAttachmentPreviewView({ ...options, sourceUrl }),
  );
