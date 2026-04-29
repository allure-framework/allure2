import dateHelper from "../../../helpers/date.mts";
import translate from "../../../helpers/t.mts";
import timeHelper from "../../../helpers/time.mts";
import b from "../../../shared/bem/index.mts";
import { createElement } from "../../../shared/dom.mts";
import { sanitizeNavigationUrl } from "../../../shared/url.mts";

type HistoryData = NonNullable<import("../../../types/report.mts").TestResultExtra["history"]>;

type HistoryRenderOptions = {
  cls: string;
  history: HistoryData | null;
  successRate: string;
};

const historyTimeText = (time: import("../../../types/report.mts").Time | undefined) =>
  translate("testResult.history.time", {
    hash: {
      date: dateHelper(time?.start),
      time: timeHelper(time?.start),
    },
  });

const renderHistoryItem = (
  {
    reportUrl,
    status,
    time,
  }: { reportUrl?: string; status: string; time?: import("../../../types/report.mts").Time },
  cls: string,
) => {
  const safeReportUrl = sanitizeNavigationUrl(reportUrl);
  const content = createElement("div", {
    className: b(cls, "entry"),
    children: [
      createElement("span", {
        className: `label label_status_${status}`,
        text: status,
      }),
      createElement("span", {
        className: b(cls, "time"),
        text: historyTimeText(time),
      }),
    ],
  });

  return safeReportUrl
    ? createElement("div", {
        className: b(cls, "row"),
        children: createElement("a", {
          attrs: {
            href: safeReportUrl,
            rel: "noopener noreferrer",
            target: "_blank",
          },
          className: `${b(cls, "link")} link`,
          children: content,
        }),
      })
    : createElement("div", {
        className: b(cls, "row"),
        children: content,
      });
};

export const renderHistory = ({ cls, history, successRate }: HistoryRenderOptions) => {
  const statistic = history?.statistic;
  const statisticText = statistic?.total
    ? translate("testResult.history.statistic", {
        hash: {
          passed: statistic.passed,
          total: statistic.total,
        },
      })
    : "";

  return createElement("div", {
    className: b("pane", "section"),
    children: history
      ? [
          createElement("div", {
            className: b(cls, "success-rate"),
            text: `${translate("testResult.history.successRate")} ${successRate}${statisticText ? ` (${statisticText})` : ""}`,
          }),
          (history.items || []).map((item) =>
            renderHistoryItem(
              item as {
                reportUrl?: string;
                status: string;
                time?: import("../../../types/report.mts").Time;
              },
              cls,
            ),
          ),
        ]
      : translate("testResult.history.empty"),
  });
};
