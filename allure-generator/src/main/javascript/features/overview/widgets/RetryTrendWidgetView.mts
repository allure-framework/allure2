import "./RetryTrendWidgetView.scss";
import { scaleOrdinal } from "d3-scale";
import { interpolateYlOrRd } from "d3-scale-chromatic";
import { defineMountableElement } from "../../../core/view/elementView.mts";
import { attachMountable, destroyMountable } from "../../../core/view/mountables.mts";
import translate from "../../../helpers/t.mts";
import { createElement } from "../../../shared/dom.mts";
import TrendChartView from "../charts/TrendChartView.mts";

type TrendPoint = import("../../../types/report.mts").TrendPoint;

type RetryTrendWidgetOptions = {
  data?: TrendPoint[];
};

const RetryTrendWidgetView = (options: RetryTrendWidgetOptions = {}) => {
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
    const container = el.querySelector(".retry-trend__chart");
    if (!(container instanceof Element)) {
      return;
    }

    const items = options?.data || [];
    const lastItem = items[items.length - 1];
    const retry = lastItem?.data.retry || 0;
    const run = lastItem?.data.run || 1;
    const retriesPercent = Math.min(0.3 + Math.min(retry, run) / run, 1);
    const colors = scaleOrdinal(["#4682b4", interpolateYlOrRd(retriesPercent)]);
    chart = attachMountable(
      container,
      new TrendChartView({
        items,
        keys: ["run", "retry"],
        colors,
        hideLines: true,
        hidePoints: true,
      }),
    );
  };

  Object.assign(el, {
    render() {
      destroyChart();
      el.className = "retry-trend";
      el.replaceChildren(
        createElement("h2", {
          className: "widget__title",
          text: translate("widget.retryTrend.name"),
        }),
        createElement("div", {
          className: "retry-trend__chart",
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

export default RetryTrendWidgetView;
