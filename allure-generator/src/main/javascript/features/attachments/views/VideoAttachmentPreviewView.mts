import "./VideoAttachmentPreviewView.scss";
import translate from "../../../helpers/t.mts";
import b from "../../../shared/bem/index.mts";
import { appendChildren, createElement } from "../../../shared/dom.mts";
import {
  createAttachmentSourceUrlPreview,
  createDiv,
  type AttachmentPreviewComponent,
  type AttachmentPreviewOptions,
} from "./BaseAttachmentPreviewView.mts";

const renderLoadedVideoAttachmentPreviewView = ({
  attachment,
  sourceUrl,
}: AttachmentPreviewOptions) => {
  const mediaContainer = createDiv(b("attachment-preview", { video: true }));
  const video = createElement("video", { className: b("attachment-preview", "media") });
  video.controls = true;
  const source = createElement("source", {
    attrs: {
      src: sourceUrl || undefined,
      type: attachment.type,
    },
  });
  appendChildren(video, source, translate("component.attachment.videoNotSupported"));
  mediaContainer.appendChild(video);
  return mediaContainer;
};

export const VideoAttachmentPreviewView: AttachmentPreviewComponent = (options) =>
  createAttachmentSourceUrlPreview(options, (sourceUrl) =>
    renderLoadedVideoAttachmentPreviewView({ ...options, sourceUrl }),
  );
