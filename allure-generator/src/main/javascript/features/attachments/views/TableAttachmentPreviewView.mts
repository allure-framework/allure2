import "./TableAttachmentPreviewView.scss";
import { csvParseRows, tsvParseRows } from "d3-dsv";
import b from "../../../shared/bem/index.mts";
import { createElement } from "../../../shared/dom.mts";
import { normalizeAttachmentContentType } from "../model/builtinAttachmentType.mts";
import {
  createDiv,
  hasPreviewData,
  LoadedPreviewView,
  loadAttachmentText,
  toText,
  type AttachmentPreviewComponent,
  type AttachmentPreviewOptions,
} from "./BaseAttachmentPreviewView.mts";

type Attachment = import("../../../types/report.mts").Attachment;

const parseTablePreviewData = (attachment: Attachment, previewData: unknown) => {
  if (Array.isArray(previewData)) {
    return previewData;
  }

  const content = typeof previewData === "string" ? previewData : "";
  const contentType = normalizeAttachmentContentType(attachment.type);

  return contentType === "text/tab-separated-values"
    ? tsvParseRows(content)
    : csvParseRows(content);
};

export const renderLoadedTableAttachmentPreviewView = ({
  attachment,
  fullScreen,
  previewData,
}: AttachmentPreviewOptions) => {
  const tableContainer = createDiv(
    b("attachment-preview", { fullscreen: fullScreen, table: true }),
  );
  const table = createElement("table", {
    className: `table ${b("attachment-preview", "table")}`,
  });
  const tbody = createElement("tbody");

  const tableContent = parseTablePreviewData(attachment, previewData);
  tableContent.forEach((row) => {
    const tr = createElement("tr");
    (Array.isArray(row) ? row : []).forEach((cell) => {
      tr.appendChild(createElement("td", { text: toText(cell) }));
    });
    tbody.appendChild(tr);
  });

  table.appendChild(tbody);
  tableContainer.appendChild(table);
  return tableContainer;
};

export const TableAttachmentPreviewView: AttachmentPreviewComponent = (options) =>
  LoadedPreviewView({
    createLoaded: renderLoadedTableAttachmentPreviewView,
    hasLoadedData: hasPreviewData,
    load: () => loadAttachmentText(options),
    options,
    toLoadedOptions: (previewData) => ({ previewData }),
  });
