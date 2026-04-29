import { defineMountableElement } from "../../../core/view/elementView.mts";
import { attachMountable, destroyMountable } from "../../../core/view/mountables.mts";
import translate from "../../../helpers/t.mts";
import { createElement } from "../../../shared/dom.mts";
import PieChartView from "../charts/PieChartView.mts";

type Status = import("../../../types/report.mts").Status;
type PieChartStatistic = NonNullable<
  NonNullable<ConstructorParameters<typeof PieChartView>[0]>["statistic"]
>;

type StatusWidgetOptions = {
  data?: {
    items?: { status: Status }[];
  };
};

const getStatusChartData = (options?: StatusWidgetOptions): PieChartStatistic => {
  const items = options?.data?.items || [];
  return items.reduce(
    (stats, testResult) => {
      const status = testResult.status.toLowerCase() as Status;
      stats[status] += 1;
      return stats;
    },
    {
      total: items.length,
      failed: 0,
      broken: 0,
      skipped: 0,
      passed: 0,
      unknown: 0,
    },
  );
};

const StatusWidgetView = (options: StatusWidgetOptions = {}) => {
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
    const container = el.querySelector(".status-widget__content");
    if (!(container instanceof Element)) {
      return;
    }

    chart = attachMountable(
      container,
      new PieChartView({
        statistic: getStatusChartData(options),
        showLegend: true,
      }),
    );
  };

  Object.assign(el, {
    render() {
      destroyChart();
      el.className = "status-widget";
      el.replaceChildren(
        createElement("h2", {
          className: "widget__title",
          text: translate("chart.status.name"),
        }),
        createElement("div", {
          className: "status-widget__content chart__body",
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

export default StatusWidgetView;
