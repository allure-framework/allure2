import "./CategoriesTrendWidgetView.scss";
import { scaleOrdinal } from "d3-scale";
import { defineMountableElement } from "../../../core/view/elementView.mts";
import { attachMountable, destroyMountable } from "../../../core/view/mountables.mts";
import translate from "../../../helpers/t.mts";
import { createElement } from "../../../shared/dom.mts";
import { chartCategoricalColors } from "../../../shared/theme.mts";
import TrendChartView from "../charts/TrendChartView.mts";
import { getSortedTrendKeysByLastValue } from "../model/widgetData.mts";

type TrendPoint = import("../../../types/report.mts").TrendPoint;

type CategoriesTrendWidgetOptions = {
  data?: TrendPoint[];
};

const CategoriesTrendWidgetView = (options: CategoriesTrendWidgetOptions = {}) => {
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
    const container = el.querySelector(".categories-trend__chart");
    if (!(container instanceof Element)) {
      return;
    }

    const items = options?.data || [];
    const keys = getSortedTrendKeysByLastValue(items);
    const colors = scaleOrdinal(chartCategoricalColors).domain(keys);
    chart = attachMountable(
      container,
      new TrendChartView({
        items,
        keys,
        colors,
        hideLines: true,
        hidePoints: true,
      }),
    );
  };

  Object.assign(el, {
    render() {
      destroyChart();
      el.className = "categories-trend";
      el.replaceChildren(
        createElement("h2", {
          className: "widget__title",
          text: translate("widget.categoriesTrend.name"),
        }),
        createElement("div", {
          className: "categories-trend__chart",
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

export default CategoriesTrendWidgetView;
