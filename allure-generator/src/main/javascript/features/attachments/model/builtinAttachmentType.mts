import { AttachmentPreviewView } from "./attachmentPreviewView.mts";
import { PLAYWRIGHT_TRACE_MIME } from "./playwrightTrace.mts";

type IconName = import("../../../shared/icon/index.mts").IconName;

export type AttachmentTypeInfo = {
  view: AttachmentPreviewView | null;
  icon: IconName;
};

export const normalizeAttachmentContentType = (type: string) =>
  type.split(";")[0].trim().toLowerCase();

export const isJsonAttachmentContentType = (type: string) => {
  const normalized = normalizeAttachmentContentType(type);

  return (
    normalized === "application/json" || normalized === "text/json" || normalized.endsWith("+json")
  );
};

export const getBuiltinAttachmentType = (type: string): AttachmentTypeInfo => {
  const normalizedType = normalizeAttachmentContentType(type);

  switch (normalizedType) {
    case "application/vnd.allure.http+json":
    case "application/vnd.allure.http":
      return {
        view: AttachmentPreviewView.HttpExchange,
        icon: "lineDevDataflow3",
      };
    case "image/bmp":
    case "image/gif":
    case "image/tiff":
    case "image/jpeg":
    case "image/jpg":
    case "image/png":
    case "image/webp":
    case "image/*":
      return {
        view: AttachmentPreviewView.Image,
        icon: "lineImagesImage",
      };
    case "text/xml":
    case "application/xml":
    case "text/yaml":
    case "application/yaml":
    case "application/x-yaml":
    case "text/x-yaml":
      return {
        view: AttachmentPreviewView.Code,
        icon: "lineDevCodeSquare",
      };
    case "text/plain":
    case "text/event-stream":
    case "text/*":
      return {
        view: AttachmentPreviewView.Text,
        icon: "lineFilesFile2",
      };
    case "application/xhtml+xml":
    case "text/html":
      return {
        view: AttachmentPreviewView.Html,
        icon: "lineDevCodeSquare",
      };
    case "text/csv":
      return {
        view: AttachmentPreviewView.Table,
        icon: "lineGeneralChecklist3",
      };
    case "text/tab-separated-values":
      return {
        view: AttachmentPreviewView.Table,
        icon: "lineGeneralChecklist3",
      };
    case "image/svg+xml":
      return {
        view: AttachmentPreviewView.Svg,
        icon: "lineImagesImage",
      };
    case "video/mp4":
    case "video/ogg":
    case "video/webm":
      return {
        view: AttachmentPreviewView.Video,
        icon: "lineHelpersPlayCircle",
      };
    case "text/uri-list":
      return {
        view: AttachmentPreviewView.Uri,
        icon: "lineGeneralLink1",
      };
    case "application/x-tar":
    case "application/x-gtar":
    case "application/x-bzip2":
    case "application/gzip":
    case "application/zip":
      return {
        view: AttachmentPreviewView.Archive,
        icon: "lineFilesFileAttachment2",
      };
    case PLAYWRIGHT_TRACE_MIME:
      return {
        view: AttachmentPreviewView.PlaywrightTrace,
        icon: "lineDevCodeSquare",
      };
    case "application/vnd.allure.image.diff":
    case "application/vnd.allure.image.diff+json":
      return {
        view: AttachmentPreviewView.ScreenDiff,
        icon: "lineLayoutsColumns2",
      };
    default:
      if (isJsonAttachmentContentType(normalizedType)) {
        return {
          view: AttachmentPreviewView.Code,
          icon: "lineDevCodeSquare",
        };
      }

      return {
        view: null,
        icon: "lineFilesFile2",
      };
  }
};
