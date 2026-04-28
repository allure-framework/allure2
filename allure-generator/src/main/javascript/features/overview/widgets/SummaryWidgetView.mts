import "./SummaryWidgetView.scss";
import { defineMountableElement } from "../../../core/view/elementView.mts";
import { attachMountable, destroyMountable } from "../../../core/view/mountables.mts";
import date from "../../../helpers/date.mts";
import duration from "../../../helpers/duration.mts";
import translate from "../../../helpers/t.mts";
import time from "../../../helpers/time.mts";
import b from "../../../shared/bem/index.mts";
import { createElement } from "../../../shared/dom.mts";
import PieChartView from "../charts/PieChartView.mts";

type SummaryWidgetOptions = {
  data?: {
    statistic?: Record<string, number>;
    testRuns?: unknown[];
    reportName?: string;
    time?: { start?: number; stop?: number; duration?: number };
  };
};

const SummaryWidgetView = (options: SummaryWidgetOptions = {}) => {
  const el = defineMountableElement(document.createElement("div"), {});
  let chart: import("../../../core/view/types.mts").Mountable | null = null;

  const destroyChart = () => {
    if (!chart) {
      return;
    }

    destroyMountable(chart);
    chart = null;
  };

  const mountChart = () => {
    const container = el.querySelector(".summary-widget__chart");
    if (!(container instanceof Element)) {
      return;
    }

    chart = attachMountable(
      container,
      new PieChartView({
        statistic: options?.data?.statistic,
        showLegend: false,
      }),
    );
  };

  Object.assign(el, {
    render() {
      destroyChart();
      const data = options?.data;
      const testRuns = data?.testRuns;
      const launchesCount = testRuns?.length;
      const isAggregated = (launchesCount || 0) > 1;
      const { reportName, statistic, time: timing } = data || {};
      const launchesText = translate("widget.summary.launches", { hash: { count: launchesCount } });
      const testResultsText = translate("widget.summary.testResults", {
        hash: { count: statistic?.total },
      });
      const title = createElement("h2", {
        className: b("widget", "title"),
        children: [
          isAggregated
            ? [
                translate("widget.summary.aggregatedName"),
                createElement("span", {
                  className: b("widget", "subtitle"),
                  text: `${launchesCount || 0} ${launchesText}`,
                }),
              ]
            : `${reportName || ""} ${date(timing?.stop)}`,
          createElement("div", {
            className: b("widget", "subtitle"),
            text: `${time(timing?.start)} - ${time(timing?.stop)} (${duration(timing?.duration, 2)})`,
          }),
        ],
      });

      el.replaceChildren(
        createElement("div", {
          className: b("widget", "flex-line"),
          children: [
            createElement("div", {
              className: b("widget", "column"),
              children: [
                title,
                createElement("div", {
                  className: `${b("summary-widget", "stats")} splash`,
                  children: [
                    createElement("div", {
                      className: b("splash", "title"),
                      text: statistic?.total ?? "",
                    }),
                    createElement("div", {
                      className: b("splash", "subtitle"),
                      text: testResultsText,
                    }),
                  ],
                }),
              ],
            }),
            createElement("div", {
              className: `${b("widget", "column")} ${b("summary-widget", "chart")}`,
            }),
          ],
        }),
      );
      mountChart();
      return el;
    },
    attachToDom() {
      chart?.attachToDom?.();
    },
    detachFromDom() {
      chart?.detachFromDom?.();
    },
    destroy() {
      el.detachFromDom?.();
      destroyChart();
      el.remove();
    },
  });

  return el;
};

export default SummaryWidgetView;
