import { csvParseRows, tsvParseRows } from "d3-dsv";
import { PLAYWRIGHT_TRACE_MIME } from "./playwrightTrace.mts";

type IconName = import("../../../shared/icon/index.mts").IconName;

export type AttachmentTypeInfo = {
  type: string | null;
  icon: IconName;
  parser?: (content: string) => unknown;
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
    case "image/bmp":
    case "image/gif":
    case "image/tiff":
    case "image/jpeg":
    case "image/jpg":
    case "image/png":
    case "image/webp":
    case "image/*":
      return {
        type: "image",
        icon: "lineImagesImage",
      };
    case "text/xml":
    case "application/xml":
    case "text/yaml":
    case "application/yaml":
    case "application/x-yaml":
    case "text/x-yaml":
      return {
        type: "code",
        icon: "lineDevCodeSquare",
        parser: (d) => d,
      };
    case "text/plain":
    case "text/event-stream":
    case "text/*":
      return {
        type: "text",
        icon: "lineFilesFile2",
        parser: (d) => d,
      };
    case "application/xhtml+xml":
    case "text/html":
      return {
        type: "html",
        icon: "lineDevCodeSquare",
      };
    case "text/csv":
      return {
        type: "table",
        icon: "lineGeneralChecklist3",
        parser: (d) => csvParseRows(d),
      };
    case "text/tab-separated-values":
      return {
        type: "table",
        icon: "lineGeneralChecklist3",
        parser: (d) => tsvParseRows(d),
      };
    case "image/svg+xml":
      return {
        type: "svg",
        icon: "lineImagesImage",
      };
    case "video/mp4":
    case "video/ogg":
    case "video/webm":
      return {
        type: "video",
        icon: "lineHelpersPlayCircle",
      };
    case "text/uri-list":
      return {
        type: "uri",
        icon: "lineGeneralLink1",
        parser: (d) =>
          d
            .split("\n")
            .map((line) => line.trim())
            .filter((line) => line.length > 0)
            .map((line) => ({
              comment: line.indexOf("#") === 0,
              text: line,
            })),
      };
    case "application/x-tar":
    case "application/x-gtar":
    case "application/x-bzip2":
    case "application/gzip":
    case "application/zip":
      return {
        type: "archive",
        icon: "lineFilesFileAttachment2",
      };
    case PLAYWRIGHT_TRACE_MIME:
      return {
        type: "playwright-trace",
        icon: "lineDevCodeSquare",
      };
    default:
      if (isJsonAttachmentContentType(normalizedType)) {
        return {
          type: "code",
          icon: "lineDevCodeSquare",
          parser: (d) => d,
        };
      }

      return {
        type: null,
        icon: "lineFilesFile2",
      };
  }
};
