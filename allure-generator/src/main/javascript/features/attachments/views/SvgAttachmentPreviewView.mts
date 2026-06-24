import "./SvgAttachmentPreviewView.scss";
import {
  hasSourceUrl,
  LoadedPreviewView,
  loadAttachmentText,
  type AttachmentPreviewComponent,
  type AttachmentPreviewOptions,
} from "./BaseAttachmentPreviewView.mts";
import { renderLoadedImageAttachmentPreviewView } from "./ImageAttachmentPreviewView.mts";

const renderLoadedSvgAttachmentPreviewView = (options: AttachmentPreviewOptions) =>
  renderLoadedImageAttachmentPreviewView({
    ...options,
    className: "attachment-preview_svg",
  });

export const SvgAttachmentPreviewView: AttachmentPreviewComponent = (options) =>
  LoadedPreviewView({
    createLoaded: renderLoadedSvgAttachmentPreviewView,
    hasLoadedData: hasSourceUrl,
    load: () => loadAttachmentText(options),
    options,
    toLoadedOptions: (svgText, setCleanup) => {
      const sourceUrl = URL.createObjectURL(
        new Blob([svgText], {
          type: options.attachment.type || "image/svg+xml",
        }),
      );
      setCleanup(() => URL.revokeObjectURL(sourceUrl));
      return { sourceUrl };
    },
  });
