import "./HistoryTrendWidgetView.scss";
import { scaleOrdinal } from "d3-scale";
import { defineMountableElement } from "../../../core/view/elementView.mts";
import { attachMountable, destroyMountable } from "../../../core/view/mountables.mts";
import translate from "../../../helpers/t.mts";
import { createElement } from "../../../shared/dom.mts";
import { values } from "../../../utils/statuses.mts";
import TrendChartView from "../charts/TrendChartView.mts";

type TrendPoint = import("../../../types/report.mts").TrendPoint;

type HistoryTrendWidgetOptions = {
  data?: TrendPoint[];
};

const HistoryTrendWidgetView = (options: HistoryTrendWidgetOptions = {}) => {
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
    const container = el.querySelector(".history-trend__chart");
    if (!(container instanceof Element)) {
      return;
    }

    chart = attachMountable(
      container,
      new TrendChartView({
        items: options?.data || [],
        hideLines: true,
        hidePoints: true,
        colors: scaleOrdinal(["#fd5a3e", "#ffd050", "#97cc64", "#aaa", "#d35ebe"]).domain(values),
        keys: Array.from(values),
      }),
    );
  };

  Object.assign(el, {
    render() {
      destroyChart();
      el.className = "history-trend";
      el.replaceChildren(
        createElement("h2", {
          className: "widget__title",
          text: translate("widget.trend.name"),
        }),
        createElement("div", {
          className: "history-trend__chart",
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

export default HistoryTrendWidgetView;
