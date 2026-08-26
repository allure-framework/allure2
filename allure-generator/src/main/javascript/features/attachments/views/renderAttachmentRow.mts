import fileicon from "../../../helpers/fileicon.mts";
import filesize from "../../../helpers/filesize.mts";
import translate from "../../../helpers/t.mts";
import b from "../../../shared/bem/index.mts";
import { createElement } from "../../../shared/dom.mts";
import { createIconElement } from "../../../shared/icon/index.mts";
import { AttachmentPreviewView } from "../model/attachmentPreviewView.mts";
import attachmentType from "../model/attachmentType.mts";

type Attachment = import("../../../types/report.mts").Attachment;

export const createAttachmentRow = ({ uid, type, name, source, size }: Attachment) => {
  const attachmentInfo = attachmentType(type || "");
  const isTraceAttachment = attachmentInfo.view === AttachmentPreviewView.PlaywrightTrace;

  return createElement("div", {
    children: [
      createElement("div", {
        attrs: {
          "data-type": type,
          "data-uid": uid,
          ...(isTraceAttachment ? { "data-viewer": "playwright-trace" } : {}),
        },
        className: "attachment-row",
        children: [
          createElement("span", {
            className: "attachment-row__arrow block__arrow",
            children: createIconElement(
              isTraceAttachment ? "lineGeneralLinkExternal" : "lineArrowsChevronRight",
              {
                className: "angle",
                size: "s",
              },
            ),
          }),
          createElement("div", {
            attrs: type ? { "data-tooltip": type } : {},
            className: "attachment-row__icon",
            children: createIconElement(fileicon(type), {
              size: "s",
              title: type || "",
            }),
          }),
          createElement("div", {
            className: "attachment-row__name long-line",
            text: name || source,
          }),
          createElement("div", {
            className: "attachment-row__control attachment-row__link",
            children: createElement("div", {
              attrs: {
                "data-download": `data/attachments/${source}`,
                "data-download-target": "_blank",
                "data-download-type": type,
                "data-tooltip": translate("testResult.execution.downloadAttachment.tooltip"),
              },
              className: "link",
              children: [
                createIconElement("lineGeneralDownloadCloud", {
                  inline: true,
                  size: "s",
                }),
                " ",
                filesize(size),
              ],
            }),
          }),
          createElement("div", {
            className: "attachment-row__control attachment-row__fullscreen",
            children: createElement("a", {
              className: "link",
              children: createIconElement("lineLayoutsMaximize2", {
                inline: true,
                size: "s",
              }),
            }),
          }),
        ],
      }),
      createElement("div", {
        className: "attachment-row__preview",
        children: createElement("div", {
          className: `attachment-row__content ${b("attachment", String(uid))}`,
        }),
      }),
    ],
  });
};
