import { createAllureIconElement } from "../../../helpers/allure-icon.mts";
import translate from "../../../helpers/t.mts";
import b from "../../../shared/bem/index.mts";
import { createElement, createFragment } from "../../../shared/dom.mts";
import { createIconElement } from "../../../shared/icon/index.mts";

type TabLink = {
  href: string;
  name: string;
  active: boolean;
};

type TestResultRenderOptions = {
  cls: string;
  fullName?: string;
  status: string;
  statusName: string;
  flaky: boolean;
  newFailed: boolean;
  newPassed: boolean;
  newBroken: boolean;
  retriesStatusChange: boolean;
  name: string;
  links: TabLink[];
};

const createClipboardCopy = ({ value }: { value: string }) =>
  createIconElement("lineGeneralCopy3", {
    attributes: {
      "data-copy": value,
      "data-ga4-event": "clipboard_copy_click",
    },
    className: "fullname__copy",
    title: translate("controls.clipboard"),
  });

const createTabs = ({ links }: { links: TabLink[] }) =>
  createElement("ul", {
    className: "tabs",
    children: (links || []).map(({ href, name, active }) =>
      createElement("li", {
        className: b("tab", { active }),
        children: createElement("a", {
          attrs: {
            href,
            "data-ga4-event": "tab_change_click",
            "data-ga4-param-tab": name,
          },
          className: "link link__no-decoration",
          text: translate(name),
        }),
      }),
    ),
  });

const createMarkIcons = (options: TestResultRenderOptions) => {
  const marks: Array<[boolean, string]> = [
    [options.flaky, "flaky"],
    [options.newFailed, "newFailed"],
    [options.newPassed, "newPassed"],
    [options.newBroken, "newBroken"],
    [options.retriesStatusChange, "retriesStatusChange"],
  ];

  return marks
    .filter(([enabled]) => enabled)
    .map(([, name]) => createAllureIconElement(name))
    .filter((icon): icon is SVGElement => Boolean(icon));
};

export const createTestResultContent = ({
  cls,
  fullName,
  status,
  statusName,
  name,
  links,
  ...rest
}: TestResultRenderOptions) =>
  createFragment(
    fullName
      ? createElement("div", {
          className: "pane__subtitle long-line line-ellipsis",
          children: [
            createClipboardCopy({ value: fullName }),
            createElement("span", {
              className: "fullname__body",
              text: fullName,
            }),
          ],
        })
      : null,
    createElement("h2", {
      className: b("pane", "title", { borderless: true }),
      children: [
        createElement("div", {
          className: b(cls, "status"),
          children: createElement("span", {
            className: `label label_status_${status}`,
            text: translate(statusName),
          }),
        }),
        createElement("div", {
          className: b(cls, "name"),
          children: [
            createElement("span", {
              className: b(cls, "marks"),
              children: createMarkIcons({
                cls,
                fullName,
                status,
                statusName,
                name,
                links,
                ...rest,
              }),
            }),
            createElement("span", {
              className: "long-line",
              text: name,
            }),
          ],
        }),
      ],
    }),
    createTabs({ links }),
    createElement("div", {
      className: b(cls, "content"),
    }),
  );
