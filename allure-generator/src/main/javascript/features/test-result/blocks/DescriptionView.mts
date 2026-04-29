import { defineMountableElement } from "../../../core/view/elementView.mts";
import translate from "../../../helpers/t.mts";
import { createElement, createFragment, createFragmentFromHtml } from "../../../shared/dom.mts";

type TestResult = import("../../../types/report.mts").TestResult;

type TestResultBlockOptions = {
  data: TestResult;
};

const DescriptionView = (options: TestResultBlockOptions) => {
  const el = defineMountableElement(document.createElement("div"), {
    destroy() {
      el.remove();
    },
  });
  const createDescriptionContent = (descriptionHtml?: string) =>
    descriptionHtml
      ? createFragment(
          createElement("h3", {
            className: "pane__section-title",
            text: translate("testResult.description.name"),
          }),
          createElement("div", {
            className: "description__text",
            children: createFragmentFromHtml(descriptionHtml, el),
          }),
        )
      : createFragment();
  Object.assign(el, {
    render() {
      el.className = "pane__section";
      el.replaceChildren(createDescriptionContent(options?.data.descriptionHtml));
      return el;
    },
  });

  return el;
};

export default DescriptionView;
