import { defineMountableElement } from "../../../core/view/elementView.mts";
import dateHelper from "../../../helpers/date.mts";
import duration from "../../../helpers/duration.mts";
import translate from "../../../helpers/t.mts";
import timeHelper from "../../../helpers/time.mts";
import { createElement } from "../../../shared/dom.mts";
import { createIconElement } from "../../../shared/icon/index.mts";

type TestResult = import("../../../types/report.mts").TestResult;

type TestResultBlockOptions = {
  data: TestResult;
};

const DurationView = (options: TestResultBlockOptions) => {
  const el = defineMountableElement(document.createElement("div"), {
    destroy() {
      el.remove();
    },
  });
  Object.assign(el, {
    render() {
      el.className = "pane__section";
      const time = options?.data.time;
      el.replaceChildren(
        time
          ? createElement("span", {
              attrs: {
                "data-tooltip": `${dateHelper(time.start)} ${timeHelper(time.start)}&nbsp;&ndash;&nbsp;${timeHelper(time.stop)}`,
              },
              children: [
                `${translate("testResult.duration.name")}: `,
                createIconElement("lineTimeClockStopwatch", { size: "s" }),
                ` ${duration(time.duration, 2)}`,
              ],
            })
          : "",
      );
      return el;
    },
  });

  return el;
};

export default DurationView;
