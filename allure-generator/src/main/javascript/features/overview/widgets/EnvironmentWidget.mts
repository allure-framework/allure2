import { bindDelegatedEvents } from "../../../core/view/domEvents.mts";
import { defineMountableElement } from "../../../core/view/elementView.mts";
import translate from "../../../helpers/t.mts";
import b from "../../../shared/bem/index.mts";
import { createElement } from "../../../shared/dom.mts";
import { sanitizeNavigationUrl } from "../../../shared/url.mts";

type EnvironmentWidgetOptions = {
  data?: {
    items?: { name: string; values?: string[] }[];
  };
};

type EnvironmentWidgetMountable = import("../../../core/view/types.mts").MountableElement & {
  onExpandClick: () => void;
};

const createEnvironmentWidget = (options: EnvironmentWidgetOptions = {}) => {
  let listLimit = 5;
  const el = defineMountableElement(document.createElement("div"), {
    onExpandClick() {
      listLimit = options.data?.items?.length || 0;
      el.render?.();
    },
  });
  let releaseEvents = () => {};

  Object.assign(el, {
    render() {
      releaseEvents();
      const allItems = options?.data?.items || [];
      const items = allItems.slice(0, listLimit);
      const overLimit = allItems.length > listLimit;
      const rows = items.map(({ name, values = [] }) =>
        createElement("div", {
          attrs: { disabled: true },
          className: b("table", "row"),
          children: [
            createElement("div", {
              className: `${b("table", "col")} long-line`,
              text: name,
            }),
            createElement("div", {
              className: `${b("table", "col")} long-line`,
              children: values.flatMap((value, index) => {
                const safeHref = sanitizeNavigationUrl(value);
                const content = safeHref
                  ? createElement("a", {
                      attrs: {
                        href: safeHref,
                        rel: "noopener noreferrer",
                        target: "_blank",
                      },
                      className: "link",
                      text: value,
                    })
                  : value;
                return index === values.length - 1
                  ? [content]
                  : [content, ",", createElement("br")];
              }),
            }),
          ],
        }),
      );

      el.replaceChildren(
        createElement("h2", {
          className: "widget__title",
          text: translate("widget.environment.name"),
        }),
        items.length
          ? createElement("div", {
              className: "table  table_hover widget__table",
              children: [
                rows,
                overLimit
                  ? createElement("a", {
                      className: `${b("table", "row")} clickable environment-widget__expand`,
                      children: createElement("div", {
                        className: b("table", "col", { center: true }),
                        text: translate("widget.environment.showAll"),
                      }),
                    })
                  : null,
              ],
            })
          : createElement("div", {
              className: "widget__noitems",
              text: translate("widget.environment.empty"),
            }),
      );
      releaseEvents = bindDelegatedEvents({
        root: el,
        events: {
          "click .environment-widget__expand": "onExpandClick",
        },
        context: el,
      });
      return el;
    },
    destroy() {
      releaseEvents();
      el.remove();
    },
  } satisfies Partial<EnvironmentWidgetMountable>);

  return el;
};

export default createEnvironmentWidget;
