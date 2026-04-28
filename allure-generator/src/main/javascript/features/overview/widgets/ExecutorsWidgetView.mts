import { defineMountableElement } from "../../../core/view/elementView.mts";
import translate from "../../../helpers/t.mts";
import b from "../../../shared/bem/index.mts";
import { appendChildren, createElement } from "../../../shared/dom.mts";
import { createIconElement } from "../../../shared/icon/index.mts";
import { sanitizeNavigationUrl } from "../../../shared/url.mts";

type ExecutorItem = {
  type?: string;
  name?: string;
  buildName?: string;
  buildUrl?: string;
};

type ExecutorsWidgetOptions = {
  data?: {
    items?: ExecutorItem[];
  };
};

const ExecutorsWidgetView = (options: ExecutorsWidgetOptions = {}) => {
  const el = defineMountableElement(document.createElement("div"), {});

  Object.assign(el, {
    render() {
      const items = options?.data?.items;
      const title = createElement("h2", {
        className: "widget__title",
        text: translate("widget.executors.name"),
      });

      const table = createElement("div", { className: "table table_hover widget__table" });

      if (items?.length) {
        items.forEach(({ type, name, buildName, buildUrl }) => {
          const row = createElement("span", { className: "table__row" });

          const nameCol = createElement("div", { className: `${b("table", "col")} executor` });
          const executorIcon = createElement("span", {
            className: `executor-icon executor-icon__${type || ""}`,
            text: " ",
          });
          appendChildren(nameCol, executorIcon, name || "");
          row.appendChild(nameCol);

          const detailsCol = createElement("div", {
            className: buildName
              ? `${b("table", "col", { right: true })} executor`
              : b("table", "col", { right: true }),
          });

          if (buildName) {
            const safeBuildUrl = sanitizeNavigationUrl(buildUrl);
            if (safeBuildUrl) {
              const link = createElement("a", {
                attrs: {
                  href: safeBuildUrl,
                  rel: "noopener noreferrer",
                  target: "_blank",
                },
                className: "link",
              });
              link.append(buildName);
              link.appendChild(
                createIconElement("lineGeneralLinkExternal", {
                  inline: true,
                  size: "s",
                }),
              );
              detailsCol.appendChild(link);
            } else {
              detailsCol.textContent = buildName;
            }
          } else {
            detailsCol.textContent = translate("widget.executors.unknown");
          }

          row.appendChild(detailsCol);
          table.appendChild(row);
        });
      } else {
        const row = createElement("div", { className: "table__row" });
        const cell = createElement("div", {
          className: b("table", "col", { center: true }),
          text: translate("widget.executors.empty"),
        });
        row.appendChild(cell);
        table.appendChild(row);
      }

      el.replaceChildren(title, table);
      return el;
    },
    destroy() {
      el.remove();
    },
  });

  return el;
};

export default ExecutorsWidgetView;
