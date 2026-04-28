import "./NodeSorterView.scss";
import { bindDelegatedEvents } from "../../../core/view/domEvents.mts";
import { defineMountableElement } from "../../../core/view/elementView.mts";
import translate from "../../../helpers/t.mts";
import b from "../../../shared/bem/index.mts";
import { createElement } from "../../../shared/dom.mts";
import { createIconElement } from "../../../shared/icon/index.mts";

type TreeSorterName = import("../../../core/services/settings.mts").TreeSorterName;

const AVAILABLE_SORTERS = [
  "sorter.order",
  "sorter.name",
  "sorter.duration",
  "sorter.status",
] as const satisfies readonly TreeSorterName[];

type NodeSorterOptions = {
  settings: ReturnType<
    typeof import("../../../core/services/settings.mts").getSettingsForTreePlugin
  >;
};

const createNodeSorterView = (options: NodeSorterOptions) => {
  const el = defineMountableElement(document.createElement("div"), {
    settings: options.settings,
    onChangeSorting(event: Event) {
      const element = event.currentTarget as HTMLElement;
      const sorter = element.dataset.name as TreeSorterName | undefined;
      if (!sorter) {
        return;
      }

      const ascending = element.dataset.asc === "true";
      options.settings.setTreeSorting({
        sorter,
        ascending: !ascending,
      });
      el.render?.();
    },
  });
  let releaseEvents = () => {};

  Object.assign(el, {
    render() {
      releaseEvents();
      const sortSettings = options.settings.getTreeSorting() || {
        sorter: "sorter.name",
        ascending: true,
      };
      el.className = "sorter";
      el.replaceChildren(
        createElement("div", {
          className: "sorter",
          children: AVAILABLE_SORTERS.map((sorter) => {
            const asc = sortSettings.sorter === sorter && sortSettings.ascending;
            const desc = sortSettings.sorter === sorter && !sortSettings.ascending;
            const iconName = asc
              ? "lineArrowsSortLineAsc"
              : desc
                ? "lineArrowsSortLineDesc"
                : "lineArrowsSwitchVertical1";
            const sorterClass = `${b("sorter", { enabled: asc || desc })} ${b("sorter", "name")}`;

            return createElement("div", {
              attrs: {
                "data-asc": String(asc),
                "data-ga4-event": "sort_click",
                "data-ga4-param-name": sorter,
                "data-name": sorter,
              },
              className: b("sorter", "item"),
              children: [
                createElement("span", {
                  className: sorterClass,
                  text: translate(sorter),
                }),
                createIconElement(iconName, {
                  className: `${b("sorter", "icon")} ${iconName} ${asc || desc ? b("sorter", { enabled: true }) : ""}`,
                  size: "s",
                }),
              ],
            });
          }),
        }),
      );
      releaseEvents = bindDelegatedEvents({
        root: el,
        events: {
          "click .sorter__item": "onChangeSorting",
        },
        context: el,
      });
      return el;
    },
    destroy() {
      releaseEvents();
      el.remove();
    },
  });

  return el;
};

export default createNodeSorterView;
