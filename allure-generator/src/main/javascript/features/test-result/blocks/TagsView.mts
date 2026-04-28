import { defineMountableElement } from "../../../core/view/elementView.mts";
import translate from "../../../helpers/t.mts";
import { createElement, createFragment } from "../../../shared/dom.mts";

type TestResult = import("../../../types/report.mts").TestResult;

type TestResultBlockOptions = {
  data: TestResult;
};

const TagsView = (options: TestResultBlockOptions) => {
  const el = defineMountableElement(document.createElement("div"), {
    destroy() {
      el.remove();
    },
  });
  Object.assign(el, {
    render() {
      const extra = options?.data.extra;
      el.className = "pane__section";
      const tags = Array.isArray(extra?.tags) ? extra.tags : null;
      el.replaceChildren(
        tags?.length
          ? createFragment(
              `${translate("testResult.tags.name")}: `,
              tags.map((tag) =>
                createElement("span", {
                  className: "label label__info",
                  text: tag || "null",
                }),
              ),
            )
          : createFragment(),
      );
      return el;
    },
  });

  return el;
};

export default TagsView;
