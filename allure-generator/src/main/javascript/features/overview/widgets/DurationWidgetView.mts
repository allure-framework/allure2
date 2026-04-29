import { defineMountableElement } from "../../../core/view/elementView.mts";
import { attachMountable, destroyMountable } from "../../../core/view/mountables.mts";
import translate from "../../../helpers/t.mts";
import { createElement } from "../../../shared/dom.mts";
import DurationChartView from "../charts/DurationChartView.mts";

type DurationChartItems = NonNullable<ConstructorParameters<typeof DurationChartView>[0]>["items"];

type DurationWidgetOptions = {
  data?: {
    items?: DurationChartItems;
  };
};

const DurationWidgetView = (options: DurationWidgetOptions = {}) => {
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
    const container = el.querySelector(".duration-widget__content");
    if (!(container instanceof Element)) {
      return;
    }

    chart = attachMountable(
      container,
      new DurationChartView({
        items: options?.data?.items,
      }),
    );
  };

  Object.assign(el, {
    render() {
      destroyChart();
      el.className = "duration-widget";
      el.replaceChildren(
        createElement("h2", {
          className: "widget__title",
          text: translate("chart.duration.name"),
        }),
        createElement("div", {
          className: "duration-widget__content chart__body",
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

export default DurationWidgetView;
