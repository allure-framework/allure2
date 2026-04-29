import { defineMountableElement } from "../../../core/view/elementView.mts";
import translate from "../../../helpers/t.mts";
import { createElement, createFragment } from "../../../shared/dom.mts";

type TestResult = import("../../../types/report.mts").TestResult;

type TestResultBlockOptions = {
  data: TestResult;
};

const OwnerView = (options: TestResultBlockOptions) => {
  const el = defineMountableElement(document.createElement("div"), {
    destroy() {
      el.remove();
    },
  });
  Object.assign(el, {
    render() {
      const extra = options?.data.extra;
      el.className = "pane__section";
      const owner = typeof extra?.owner === "string" ? extra.owner : null;
      el.replaceChildren(
        owner
          ? createFragment(
              createElement("h3", {
                className: "pane__section-title",
                text: translate("testResult.owner.name"),
              }),
              createElement("div", { text: owner }),
            )
          : createFragment(),
      );
      return el;
    },
  });

  return el;
};

export default OwnerView;
