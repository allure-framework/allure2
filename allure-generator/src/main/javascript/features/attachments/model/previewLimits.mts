import filesize from "../../../helpers/filesize.mts";
import translate from "../../../helpers/t.mts";

const MiB = 1024 * 1024;

export const ATTACHMENT_PREVIEW_MAX_BYTES = 10 * MiB;
const ATTACHMENT_TEXT_PROCESSING_MAX_BYTES = 2 * MiB;
const ATTACHMENT_SYNTAX_HIGHLIGHT_MAX_BYTES = ATTACHMENT_TEXT_PROCESSING_MAX_BYTES;
export const INVALID_HTML_PREVIEW_SOURCE_MAX_BYTES = ATTACHMENT_TEXT_PROCESSING_MAX_BYTES;

export const getTextByteLength = (content: string) => {
  if (content.length > ATTACHMENT_PREVIEW_MAX_BYTES) {
    return content.length;
  }

  return new Blob([content]).size;
};

export const isAttachmentPreviewOversized = (size: unknown) =>
  typeof size === "number" && Number.isFinite(size) && size > ATTACHMENT_PREVIEW_MAX_BYTES;

export const getAttachmentPreviewOversizeReason = () =>
  translate("component.attachment.previewSizeExceeded", {
    hash: {
      size: filesize(ATTACHMENT_PREVIEW_MAX_BYTES),
    },
  });

export const isSyntaxHighlightOversized = (content: string) =>
  content.length > ATTACHMENT_SYNTAX_HIGHLIGHT_MAX_BYTES ||
  new Blob([content]).size > ATTACHMENT_SYNTAX_HIGHLIGHT_MAX_BYTES;
