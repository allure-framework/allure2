import { defineMountableElement } from "../../../core/view/elementView.mts";
import { attachMountable, destroyMountable } from "../../../core/view/mountables.mts";
import translate from "../../../helpers/t.mts";
import { createElement } from "../../../shared/dom.mts";
import SeverityChartView from "../charts/SeverityChartView.mts";

type SeverityChartItems = NonNullable<ConstructorParameters<typeof SeverityChartView>[0]>["items"];

type SeverityWidgetOptions = {
  data?: {
    items?: SeverityChartItems;
  };
};

const SeverityWidgetView = (options: SeverityWidgetOptions = {}) => {
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
    const container = el.querySelector(".severity-widget__content");
    if (!(container instanceof Element)) {
      return;
    }

    chart = attachMountable(
      container,
      new SeverityChartView({
        items: options?.data?.items,
      }),
    );
  };

  Object.assign(el, {
    render() {
      destroyChart();
      el.className = "severity-widget";
      el.replaceChildren(
        createElement("h2", {
          className: "widget__title",
          text: translate("chart.severity.name"),
        }),
        createElement("div", {
          className: "severity-widget__content chart__body",
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

export default SeverityWidgetView;
