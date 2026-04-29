import "./ParametersView.scss";
import { bindDelegatedEvents } from "../../../core/view/domEvents.mts";
import { defineMountableElement } from "../../../core/view/elementView.mts";
import translate from "../../../helpers/t.mts";
import { createElement, createFragment } from "../../../shared/dom.mts";
import { sanitizeNavigationUrl } from "../../../shared/url.mts";

type TestResult = import("../../../types/report.mts").TestResult;

type TestResultBlockOptions = {
  data: TestResult;
};

const createParametersView = (options: TestResultBlockOptions) => {
  const el = defineMountableElement(document.createElement("div"), {
    onParameterClick() {
      el.querySelectorAll(".environment").forEach((element) =>
        element.classList.remove("line-ellipsis"),
      );
    },
  });
  let releaseEvents = () => {};

  const appendValue = (container: HTMLElement, value: string) => {
    const safeHref = sanitizeNavigationUrl(value);
    if (!safeHref) {
      container.textContent = value;
      return;
    }

    container.appendChild(
      createElement("a", {
        attrs: {
          href: safeHref,
          rel: "noopener noreferrer",
          target: "_blank",
        },
        className: "link",
        text: value,
      }),
    );
  };

  Object.assign(el, {
    render() {
      releaseEvents();
      el.className = "pane__section";
      const parameters = options?.data.parameters;
      el.replaceChildren(
        parameters?.length
          ? createFragment(
              createElement("h3", { text: translate("testResult.parameters.name") }),
              parameters.map(({ name, value }) => {
                const row = createElement("div", {
                  className: "environment long-line line-ellipsis",
                  children: [
                    createElement("span", {
                      className: "environment__key",
                      text: name || "<null>",
                    }),
                    ": ",
                  ],
                });

                if (value) {
                  const valueContainer = createElement("span");
                  appendValue(valueContainer, value);
                  row.appendChild(valueContainer);
                } else {
                  row.append("null");
                }

                return row;
              }),
            )
          : createFragment(),
      );
      releaseEvents = bindDelegatedEvents({
        root: el,
        events: {
          "click .environment": "onParameterClick",
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

export default createParametersView;
