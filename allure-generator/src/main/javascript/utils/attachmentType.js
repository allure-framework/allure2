import { csvParseRows, tsvParseRows } from "d3-dsv";
import pluginsRegistry from "./pluginsRegistry";

export default function typeByMime(type) {
  if (pluginsRegistry.attachmentViews[type]) {
    return {
      type: "custom",
      ...pluginsRegistry.attachmentViews[type],
    };
  }
  switch (type) {
    case "image/bmp":
    case "image/gif":
    case "image/tiff":
    case "image/jpeg":
    case "image/jpg":
    case "image/png":
    case "image/*":
      return {
        type: "image",
        icon: "fa fa-file-image-o",
      };
    case "text/xml":
    case "application/xml":
    case "application/json":
    case "text/json":
    case "text/yaml":
    case "application/yaml":
    case "application/x-yaml":
    case "text/x-yaml":
      return {
        type: "code",
        icon: "fa fa-file-code-o",
        parser: (d) => d,
      };
    case "text/plain":
    case "text/*":
      return {
        type: "text",
        icon: "fa fa-file-text-o",
        parser: (d) => d,
      };
    case "text/html":
      return {
        type: "html",
        icon: "fa fa-file-code-o",
      };
    case "text/csv":
      return {
        type: "table",
        icon: "fa fa-table",
        parser: (d) => csvParseRows(d),
      };
    case "text/tab-separated-values":
      return {
        type: "table",
        icon: "fa fa-table",
        parser: (d) => tsvParseRows(d),
      };
    case "image/svg+xml":
      return {
        type: "svg",
        icon: "fa fa-file-image-o",
      };
    case "video/mp4":
    case "video/ogg":
    case "video/webm":
      return {
        type: "video",
        icon: "fa fa-file-video-o",
      };
    case "text/uri-list":
      return {
        type: "uri",
        icon: "fa fa-list-alt",
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
        icon: "fa fa-file-archive-o",
      };
    default:
      return {
        type: null,
        icon: "fa fa-file-o",
      };
  }
}
