import { interpolate } from "d3-interpolate";
import { select } from "d3-selection";
import { arc, pie } from "d3-shape";
import translate from "../../../helpers/t.mts";
import { createElement, createFragment } from "../../../shared/dom.mts";
import BaseChartView from "../../../shared/ui/BaseChartView.mts";
import TooltipView from "../../../shared/ui/TooltipView.mts";
import { omit } from "../../../shared/utils/collections.mts";
import { values } from "../../../utils/statuses.mts";

const PADDING = 5;

type PieChartStatistic = Record<string, number> & {
  total?: number;
  passed?: number;
  failed?: number;
  broken?: number;
};

type PieChartDatum = {
  name: string;
  part: number;
  value: number;
};

type PieChartOptions = {
  showLegend?: boolean;
  statistic?: PieChartStatistic;
};

class PieChartView extends BaseChartView {
  declare statistic: PieChartStatistic;

  declare showLegend: boolean;

  declare arc: import("d3-shape").Arc<
    SVGPathElement,
    import("d3-shape").PieArcDatum<PieChartDatum>
  >;

  declare pie: import("d3-shape").Pie<unknown, PieChartDatum>;

  declare data: PieChartDatum[];

  constructor(options: PieChartOptions = {}) {
    super(options);
    this.statistic = options.statistic || {};
    this.showLegend = options.showLegend || false;
    this.arc = arc<SVGPathElement, import("d3-shape").PieArcDatum<PieChartDatum>>();
    this.pie = pie<PieChartDatum>()
      .sort(null)
      .value((d: PieChartDatum) => d.value);
    this.tooltip = new TooltipView({
      position: "center",
    });
    this.getChartData();
  }

  getChartData() {
    const total = this.statistic.total || 0;
    const stats = omit(this.statistic, "total") as Record<string, number | undefined>;
    this.data = Object.keys(stats)
      .map((key) => ({
        name: key.toUpperCase(),
        value: stats[key] || 0,
        part: total > 0 ? (stats[key] || 0) / total : 0,
      }))
      .filter((item): item is PieChartDatum => item.value > 0);
  }
  setupViewport() {
    super.setupViewport();
    if (this.showLegend) {
      this.el.appendChild(this.getLegendElement());
    }
    return this.svg;
  }
  drawChart() {
    const data = this.data;
    const arcGenerator = this.arc as unknown as (
      datum: import("d3-shape").PieArcDatum<PieChartDatum>,
    ) => string | null;
    const { width, height } = this.el.getBoundingClientRect();
    const radius = Math.min(width, height) / 2 - 2 * PADDING;
    const topOffset = height / 2;
    let leftOffset = width / 2;
    if (this.showLegend) {
      leftOffset -= 70;
    }
    this.arc.innerRadius(0.8 * radius).outerRadius(radius);
    this.svg = this.setupViewport();
    const sectors = this.svg
      .select(".chart__plot")
      .attr("transform", `translate(${leftOffset},${topOffset})`)
      .selectAll<SVGPathElement, import("d3-shape").PieArcDatum<PieChartDatum>>(".chart__arc")
      .data(this.pie(data))
      .enter()
      .append("path")
      .attr("class", (d) => `chart__arc chart__arc_status_${d.data.name.toLowerCase()}`);
    this.bindTooltip(sectors);
    this.svg
      .select(".chart__plot")
      .append("text")
      .classed("chart__caption", true)
      .attr("dy", "0.4em")
      .text(this.getChartTitle());
    if (this.firstRender) {
      (
        sectors as unknown as {
          transition: () => {
            duration: (value: number) => {
              attrTween: (
                name: string,
                factory: (
                  datum: import("d3-shape").PieArcDatum<PieChartDatum>,
                ) => (time: number) => string | null,
              ) => void;
            };
          };
        }
      )
        .transition()
        .duration(750)
        .attrTween("d", (d: import("d3-shape").PieArcDatum<PieChartDatum>) => {
          const startAngleFn = interpolate(0, d.startAngle);
          const endAngleFn = interpolate(0, d.endAngle);
          return (t) =>
            arcGenerator({
              ...d,
              startAngle: startAngleFn(t),
              endAngle: endAngleFn(t),
            });
        });
    } else {
      sectors.attr("d", (d) => arcGenerator(d));
    }
  }
  formatNumber(n: number) {
    return (Math.floor(n * 100) / 100).toString();
  }
  getChartTitle() {
    const { passed = 0, failed = 0, broken = 0, total = 0 } = this.statistic;
    if (!total) {
      return "???";
    }
    if (!passed) {
      return "0%";
    }
    return `${this.formatNumber((passed / (passed + failed + broken)) * 100)}%`;
  }
  getTooltipContent({ data }: { data: PieChartDatum }) {
    const value = data.value || 0;
    const part = data.part || 0;
    const status = data.name.toLowerCase();
    const name = translate(`status.${status}`, {});
    return createFragment(
      `${value} tests (${this.formatNumber(part * 100)}%)`,
      createElement("br"),
      name,
    );
  }
  getLegendElement() {
    return createElement("div", {
      className: "chart__legend",
      children: values.map((status) =>
        createElement("div", {
          attrs: { "data-status": status },
          className: "chart__legend-row",
          children: [
            createElement("span", {
              className: `chart__legend-icon chart__legend-icon_status_${status}`,
            }),
            ` ${translate(`status.${status}`)}`,
          ],
        }),
      ),
    });
  }
  onLegendOut() {
    this.hideTooltip();
  }
  onLegendHover(e: MouseEvent) {
    const status = (e.currentTarget as HTMLElement | null)?.dataset.status;
    const sector = this.el.querySelector(`.chart__arc_status_${status}`);
    if (sector) {
      const data = select<SVGPathElement, import("d3-shape").PieArcDatum<PieChartDatum>>(
        sector as SVGPathElement,
      ).datum();
      this.showTooltip(data, sector);
    }
  }
  getDelegatedEvents() {
    return {
      "mouseleave .chart__legend-row": "onLegendOut",
      "mouseenter .chart__legend-row": "onLegendHover",
    };
  }
}

export default PieChartView;
