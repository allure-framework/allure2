import { defineMountableElement } from "../../core/view/elementView.mts";
import statusBar from "../../helpers/status-bar.mts";
import translate from "../../helpers/t.mts";
import b from "../bem/index.mts";
import { createElement } from "../dom.mts";

type TranslationKey = import("../../core/i18n/types.mts").TranslationKey;

type WidgetStatusItem = {
  uid: string;
  name: string;
  statistic: Record<string, number>;
};

type WidgetStatusData = {
  total?: number;
  items?: WidgetStatusItem[];
};

export type WidgetStatusConfig = {
  title: TranslationKey;
  baseUrl: string;
  showLinks?: boolean;
  showAll?: boolean;
  showAllKey?: TranslationKey;
};

type WidgetStatusOptions = {
  data?: WidgetStatusData;
  config: WidgetStatusConfig;
};

const WidgetStatusView = (options: WidgetStatusOptions) => {
  const el = defineMountableElement(document.createElement("div"), {});

  Object.assign(el, {
    render() {
      const { data, config } = options || {};
      const { total, items } = data || {};
      const {
        baseUrl = "",
        showAll = true,
        showAllKey = "component.widgetStatus.showAll",
        showLinks = true,
        title = "",
      } = config || {};
      const totalText = translate("component.widgetStatus.total", { hash: { count: total } });
      const table = createElement("div", { className: "table table_hover widget__table" });

      (items || []).forEach(({ uid, name, statistic }) => {
        const row = createElement(showLinks ? "a" : "span", {
          attrs: showLinks ? { href: `#${baseUrl}/${uid}` } : {},
          className: "table__row",
          children: [
            createElement("div", {
              className: "table__col",
              text: name,
            }),
            createElement("div", {
              className: "table__col",
              children: statusBar(statistic),
            }),
          ],
        });
        table.appendChild(row);
      });

      if (showAll) {
        table.appendChild(
          createElement("a", {
            attrs: { href: `#${baseUrl}` },
            className: "table__row",
            children: createElement("div", {
              className: b("table", "col", { center: true }),
              text: translate(showAllKey),
            }),
          }),
        );
      }

      el.replaceChildren(
        createElement("h2", {
          className: "widget__title",
          children: [
            translate(title),
            createElement("span", {
              className: "widget__subtitle",
              text: totalText,
            }),
          ],
        }),
        table,
      );
      return el;
    },
    destroy() {
      el.remove();
    },
  });

  return el;
};

export const createWidgetStatusFactory = (config: WidgetStatusConfig) => {
  return (options: Omit<WidgetStatusOptions, "config"> = {}) =>
    WidgetStatusView({ ...options, config });
};
