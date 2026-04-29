import { createAllureIconElement } from "../../../helpers/allure-icon.mts";
import duration from "../../../helpers/duration.mts";
import isDef from "../../../helpers/is-def.mts";
import { createStatisticBarFragment } from "../../../helpers/statistic-bar.mts";
import translate from "../../../helpers/t.mts";
import b from "../../../shared/bem/index.mts";
import { createElement } from "../../../shared/dom.mts";
import { createIconElement } from "../../../shared/icon/index.mts";
import type { LoadedTreeData } from "../model/treeData.mts";

type TreeRenderItem = LoadedTreeData["items"][number];

type TreeGroupRenderOptions = {
  baseUrl: string;
  testResultTab: string;
  showGroupInfo: boolean;
};

type TreeViewRenderOptions = TreeGroupRenderOptions & {
  cls: string;
  uid?: string;
  items: LoadedTreeData["items"];
};

const createArrowIcon = (className = "") =>
  createIconElement("lineArrowsChevronRight", {
    className: ["angle", className].filter(Boolean).join(" "),
    size: "s",
  });

const createTreeTime = ({
  value,
  name,
  tooltip,
}: {
  value?: number;
  name: string;
  tooltip: string;
}) =>
  isDef(value)
    ? createElement("span", {
        attrs: { "data-tooltip": tooltip },
        className: "node__time",
        children: [name, ": ", createElement("b", { text: duration(value) })],
      })
    : null;

const createMarkElement = (name: string, shown: boolean) =>
  shown
    ? createElement("div", {
        className: b("node", "mark", { shown }),
        children: createAllureIconElement(name),
      })
    : null;

const createTreeLeaf = (
  item: TreeRenderItem,
  { baseUrl, testResultTab }: TreeGroupRenderOptions,
) => {
  const statusIcon = createAllureIconElement(item.status || "unknown", { size: "s" });
  return createElement("a", {
    attrs: {
      href: `#${baseUrl}/${item.parentUid}/${item.uid}/${testResultTab}`,
    },
    className: "node node__leaf",
    children: createElement("div", {
      attrs: {
        "data-parentUid": item.parentUid,
        "data-uid": item.uid,
      },
      className: "node__title",
      children: [
        createElement("div", {
          className: "node__anchor",
          children: statusIcon,
        }),
        createElement("div", {
          className: "node__order",
          text: `#${item.order}`,
        }),
        createElement("div", {
          className: "node__name",
          text: item.name,
        }),
        item.parameters
          ? createElement("div", {
              className: "node__parameters long-line line-ellipsis",
              children: item.parameters.flatMap((parameter) => [
                parameter ?? "null",
                createElement("span", {
                  className: "node__parameters_separator",
                  text: ",",
                }),
              ]),
            })
          : null,
        createElement("div", {
          className: "tree__strut",
        }),
        createMarkElement("flaky", Boolean(item.flaky)),
        createMarkElement("newFailed", Boolean(item.newFailed)),
        createMarkElement("newBroken", Boolean(item.newBroken)),
        createMarkElement("newPassed", Boolean(item.newPassed)),
        createMarkElement("retriesStatusChange", Boolean(item.retriesStatusChange)),
        createElement("div", {
          className: "node__stats",
          text: duration(item.time?.duration),
        }),
      ],
    }),
  });
};

const createTreeGroupTitle = (item: TreeRenderItem) =>
  createElement("div", {
    attrs: { "data-uid": item.uid },
    className: "node__title long-line",
    children: [
      createElement("span", {
        className: "node__arrow block__arrow",
        children: createArrowIcon(),
      }),
      item.name
        ? createElement("div", {
            className: "node__name",
            text: item.name,
          })
        : createElement("span", {
            className: "node__unknown",
            text: translate("component.tree.unknown"),
          }),
      createElement("div", {
        className: "tree__strut",
      }),
      createElement("span", {
        className: "node__stats",
        children: createStatisticBarFragment(item.statistic),
      }),
    ],
  });

const createTreeGroupInfo = (item: TreeRenderItem) =>
  createElement("div", {
    className: "node",
    children: createElement("div", {
      attrs: { "data-uid": item.uid },
      className: "node__info node__expanded long-line",
      children: [
        createIconElement("lineTimeClockStopwatch", { size: "s" }),
        createTreeTime({
          value: item.time?.duration,
          name: translate("component.tree.time.total.name"),
          tooltip: translate("component.tree.time.total.tooltip"),
        }),
        createTreeTime({
          value: item.time?.maxDuration,
          name: translate("component.tree.time.max.name"),
          tooltip: translate("component.tree.time.max.tooltip"),
        }),
        createTreeTime({
          value: item.time?.sumDuration,
          name: translate("component.tree.time.sum.name"),
          tooltip: translate("component.tree.time.sum.tooltip"),
        }),
      ],
    }),
  });

const createTreeNodeElement = (
  item: TreeRenderItem,
  options: TreeGroupRenderOptions,
): HTMLElement => {
  if (!item.children) {
    return createTreeLeaf(item, options);
  }

  return createElement("div", {
    attrs: {
      "data-node-kind": "group",
      "data-node-uid": item.uid || "",
    },
    className: "node",
    children: [createTreeGroupTitle(item)],
  });
};

export const createTreeGroupChildren = (
  item: TreeRenderItem,
  options: TreeGroupRenderOptions,
): HTMLElement =>
  createElement("div", {
    className: "node__children",
    children: [
      options.showGroupInfo ? createTreeGroupInfo(item) : null,
      ...(item.children || []).map((child) => createTreeNodeElement(child, options)),
    ],
  });

export const createTreeViewContent = ({ cls, uid, items, ...options }: TreeViewRenderOptions) =>
  createElement("div", {
    attrs: { "data-uid": uid || "" },
    className: b(cls, "content"),
    children: items?.length
      ? items.map((item) => createTreeNodeElement(item, options))
      : createElement("div", {
          className: b(cls, "empty"),
          text: translate("component.tree.empty"),
        }),
  });
