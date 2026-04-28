import { defineMountableElement } from "../../../core/view/elementView.mts";
import translate from "../../../helpers/t.mts";
import { createElement, createFragment } from "../../../shared/dom.mts";

type TestResult = import("../../../types/report.mts").TestResult;

type TestResultBlockOptions = {
  data: TestResult;
};

const CategoryView = (options: TestResultBlockOptions) => {
  const el = defineMountableElement(document.createElement("div"), {
    destroy() {
      el.remove();
    },
  });
  Object.assign(el, {
    render() {
      const extra = options?.data.extra;
      el.className = "pane__section";
      const categories = Array.isArray(extra?.categories)
        ? (extra.categories as { name?: string }[])
        : null;
      el.replaceChildren(
        categories?.length
          ? createFragment(
              `${translate("testResult.categories.name")}: `,
              categories.map(({ name }) => createElement("span", { text: `${name || ""} ` })),
            )
          : createFragment(),
      );
      return el;
    },
  });

  return el;
};

export default CategoryView;
