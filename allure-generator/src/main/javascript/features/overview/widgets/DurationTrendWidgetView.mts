import "./DurationTrendWidgetView.scss";
import { interpolateRgb } from "d3-interpolate";
import { scaleLinear, scaleOrdinal } from "d3-scale";
import { defineMountableElement } from "../../../core/view/elementView.mts";
import { attachMountable, destroyMountable } from "../../../core/view/mountables.mts";
import duration from "../../../helpers/duration.mts";
import translate from "../../../helpers/t.mts";
import { createElement } from "../../../shared/dom.mts";
import { readThemeColor } from "../../../shared/theme.mts";
import { last } from "../../../shared/utils/collections.mts";
import TrendChartView from "../charts/TrendChartView.mts";

type TrendPoint = import("../../../types/report.mts").TrendPoint;

type DurationTrendWidgetOptions = {
  data?: TrendPoint[];
};

const DurationTrendWidgetView = (options: DurationTrendWidgetOptions = {}) => {
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
    const container = el.querySelector(".duration-trend__chart");
    if (!(container instanceof Element)) {
      return;
    }

    const key = "duration";
    const items = options?.data || [];
    const values = items.map((item) => item.data[key] || 0);
    const lastValue = last(values) ?? 0;
    const trendDirections: Array<(delta: number) => boolean> = [
      (delta) => delta >= 0,
      (delta) => delta <= 0,
    ];
    const lastExtremum = Math.min(
      ...trendDirections.map((op) =>
        values.reduce(
          (memo, current, index) => (op(current - (values[index - 1] ?? 0)) ? index : memo),
          lastValue,
        ),
      ),
    );
    const amplitude = Math.max(...values) - Math.min(...values);
    const lastDelta = (values[lastExtremum] ?? 0) - lastValue;
    const level = scaleLinear<string>().domain([
      0,
      Math.max(amplitude, 0.25 * Math.max(...values)),
    ]);
    const neutral = readThemeColor("--color-dashboard-neutral-medium");
    const success = readThemeColor("--color-chart-heatmap-low");
    const danger = readThemeColor("--color-chart-heatmap-high");
    level.range(lastDelta > 0 ? [neutral, success] : [neutral, danger]).interpolate(interpolateRgb);
    const colors = scaleOrdinal<string, string>().range([level(Math.abs(lastDelta))]);
    chart = attachMountable(
      container,
      new TrendChartView({
        items,
        hidePoints: true,
        hideLines: true,
        colors,
        keys: [key],
        yTickFormat: (value) => duration(value, 2),
      }),
    );
  };

  Object.assign(el, {
    render() {
      destroyChart();
      el.className = "duration-trend";
      el.replaceChildren(
        createElement("h2", {
          className: "widget__title",
          text: translate("widget.durationTrend.name"),
        }),
        createElement("div", {
          className: "duration-trend__chart",
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

export default DurationTrendWidgetView;
