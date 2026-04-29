import "./LinksView.scss";
import { defineMountableElement } from "../../../core/view/elementView.mts";
import translate from "../../../helpers/t.mts";
import { createElement, createFragment } from "../../../shared/dom.mts";
import { createIconElement } from "../../../shared/icon/index.mts";
import { sanitizeNavigationUrl } from "../../../shared/url.mts";

type TestResult = import("../../../types/report.mts").TestResult;

type TestResultBlockOptions = {
  data: TestResult;
};

const LinksView = (options: TestResultBlockOptions) => {
  const el = defineMountableElement(document.createElement("div"), {
    destroy() {
      el.remove();
    },
  });
  const firstTruthy = (...values: Array<string | null | undefined>) =>
    values.find((value) => value) || null;
  const renderLinks = (
    links: Array<{ type?: string; url?: string; name?: string }> | null | undefined,
  ) => {
    const fragment = createFragment();
    if (!links?.length) {
      return fragment;
    }

    fragment.appendChild(
      createElement("h3", {
        className: "pane__section-title",
        text: translate("testResult.links.name"),
      }),
    );

    links.forEach(({ type, url, name }) => {
      const href = firstTruthy(url, name);
      const label = firstTruthy(name, url, "link") || "link";
      const row = createElement("span", { className: "testresult-link" });

      if (type === "issue") {
        row.appendChild(createIconElement("lineDevBug2", { size: "s" }));
      } else if (type === "tms") {
        row.appendChild(createIconElement("lineFilesClipboardCheck", { size: "s" }));
      }

      const safeHref = sanitizeNavigationUrl(href);
      if (safeHref) {
        const link = createElement("a", {
          attrs: {
            href: safeHref,
            rel: "noopener noreferrer",
            target: "_blank",
          },
          className: "link",
          text: label,
        });
        row.appendChild(link);
      } else {
        row.append(label);
      }

      fragment.appendChild(row);
    });

    return fragment;
  };
  Object.assign(el, {
    render() {
      el.className = "pane__section";
      el.replaceChildren(
        renderLinks(
          Array.isArray(options?.data.links)
            ? (options.data.links as Array<{ type?: string; url?: string; name?: string }>)
            : null,
        ),
      );
      return el;
    },
  });

  return el;
};

export default LinksView;
