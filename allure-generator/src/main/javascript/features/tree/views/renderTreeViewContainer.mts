import translate from "../../../helpers/t.mts";
import b from "../../../shared/bem/index.mts";
import { createElement } from "../../../shared/dom.mts";
import { createIconElement } from "../../../shared/icon/index.mts";

type TreeViewContainerRenderOptions = {
  cls: string;
  tabName: string;
  filtered: boolean;
  totalCases: number;
  shownCases: number;
  csvUrl?: string | null;
};

export const renderTreeViewContainer = ({
  cls,
  tabName,
  filtered,
  totalCases,
  shownCases,
  csvUrl,
}: TreeViewContainerRenderOptions) => [
  createElement("div", {
    className: b("pane", "title", { borderless: true }),
    children: [
      createElement("span", {
        className: b("pane", "title-text"),
        text: translate(tabName),
      }),
      filtered
        ? createElement("span", {
            className: b("pane", "subtitle"),
            text: `${translate("component.tree.filtered.total", { hash: { count: totalCases } })}, ${translate("component.tree.filtered.shown", { hash: { count: shownCases } })}`,
          })
        : null,
      createElement("div", {
        className: b("pane", "search"),
      }),
      createElement("span", {
        className: "pane__controls",
        children: [
          createElement("span", {
            attrs: {
              "data-tooltip": translate("component.tree.groups"),
            },
            className: `${b(cls, "control")} ${b(cls, "info")}`,
            children: createIconElement("lineGeneralInfoCircle"),
          }),
          csvUrl
            ? createElement("span", {
                attrs: {
                  "data-download": csvUrl,
                  "data-download-type": "text/csv",
                  "data-ga4-event": "csv_download_click",
                  "data-tooltip": translate("component.tree.download"),
                },
                className: `${b(cls, "control")} ${b(cls, "download")}`,
                children: createIconElement("lineGeneralDownloadCloud"),
              })
            : null,
        ],
      }),
    ],
  }),
  createElement("div", {
    className: b(cls, "ctrl"),
    children: [
      createElement("div", { className: b(cls, "sorter") }),
      createElement("div", { className: b(cls, "strut") }),
      createElement("div", { className: b(cls, "filter") }),
      createElement("div", { className: b(cls, "filter-marks") }),
    ],
  }),
  createElement("div", {
    className: b(cls, "content"),
  }),
];
