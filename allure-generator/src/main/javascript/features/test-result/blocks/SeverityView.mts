import { defineMountableElement } from "../../../core/view/elementView.mts";
import translate from "../../../helpers/t.mts";

type TestResult = import("../../../types/report.mts").TestResult;

type TestResultBlockOptions = {
  data: TestResult;
};

const SeverityView = (options: TestResultBlockOptions) => {
  const el = defineMountableElement(document.createElement("div"), {
    destroy() {
      el.remove();
    },
  });
  Object.assign(el, {
    render() {
      const extra = options?.data.extra;
      el.className = "pane__section";
      const severity = typeof extra?.severity === "string" ? extra.severity : null;
      el.replaceChildren(
        severity
          ? `${translate("testResult.severity.name")}: ${translate(`testResult.severity.${severity}`)}`
          : "",
      );
      return el;
    },
  });

  return el;
};

export default SeverityView;
