import { max } from "d3-array";
import { scaleBand, scaleSqrt } from "d3-scale";
import { createElement, createFragment } from "../../../shared/dom.mts";
import BaseChartView from "../../../shared/ui/BaseChartView.mts";
import PopoverView from "../../../shared/ui/PopoverView.mts";
import { values } from "../../../utils/statuses.mts";

type Status = import("../../../types/report.mts").Status;

const severities = ["blocker", "critical", "normal", "minor", "trivial"] as const;
const statuses: readonly Status[] = values;

type Severity = (typeof severities)[number];

type SeverityChartItem = {
  name: string;
  severity: string;
  status: Status;
};

type SeverityChartDatum = {
  severity: Severity;
  status: Status;
  testResults: SeverityChartItem[];
  value: number;
};

export default class SeverityChartView extends BaseChartView {
  declare x: import("d3-scale").ScaleBand<Severity>;

  declare y: import("d3-scale").ScalePower<number, number>;

  declare status: import("d3-scale").ScaleBand<Status>;

  declare items: SeverityChartItem[];

  declare data: SeverityChartDatum[][];

  constructor(options: { items?: SeverityChartItem[] } = {}) {
    super(options);
    this.x = scaleBand<Severity>().domain(severities);
    this.y = scaleSqrt();
    this.status = scaleBand<Status>().domain(statuses);
    this.tooltip = new PopoverView({ position: "right" });
    this.items = options.items || [];
    this.getChartData();
  }

  getChartData() {
    this.data = severities.map((severity) =>
      statuses.map((status): SeverityChartDatum => {
        const testResults = this.items.filter(
          (item) => item.status === status && item.severity === severity,
        );
        return {
          value: testResults.length,
          testResults,
          severity,
          status,
        };
      }),
    );
  }

  drawChart() {
    const data = this.data as SeverityChartDatum[][];
    this.setupViewport();

    this.x.range([0, this.width]);
    this.y.range([this.height, 0]);
    this.y.domain([0, max(data, (d) => max(d, (dd) => dd.value) || 0) || 0]).nice();
    this.status.rangeRound([0, this.x.step()]);

    this.makeBottomAxis({
      tickFormat: (d: Severity) => d.toLowerCase(),
      scale: this.x,
    });

    this.svg
      .selectAll(".tick")
      .select("line")
      .attr("transform", `translate(${this.x.step() / 2} , 0)`);

    this.makeLeftAxis({
      scale: this.y,
      ticks: Math.min(10, this.y.domain()[1]),
    });

    const bars = this.svg
      .select(".chart__plot")
      .selectAll(".chart__group")
      .data(data)
      .enter()
      .append("g")
      .attr("transform", (d) => `translate(${this.x(d[0].severity)},0)`)
      .selectAll(".bar")
      .data((d) => d)
      .enter()
      .append("rect");

    bars
      .attr("x", (d) => this.status(d.status) ?? 0)
      .attr("y", this.height)
      .attr("height", 0)
      .attr("width", this.status.step())
      .attr("class", (d) => `chart__bar chart__bar_status_${d.status}`);

    this.bindTooltip(bars);

    const animatedBars = this.firstRender
      ? (
          bars as unknown as {
            transition: () => { duration: (value: number) => typeof bars };
          }
        )
          .transition()
          .duration(500)
      : bars;

    animatedBars
      .attr("y", (d) => this.y(d.value))
      .attr("height", (d) => this.height - this.y(d.value));
  }

  getTooltipContent({ value, severity, status, testResults }: SeverityChartDatum) {
    const LIST_LIMIT = 10;
    const items = testResults.slice(0, LIST_LIMIT);
    const overLimit = testResults.length - items.length;
    return createFragment(
      createElement("b", {
        text: `${value} ${severity.toLowerCase()} test cases ${status.toLowerCase()}`,
      }),
      createElement("br"),
      createElement("ul", {
        className: "popover__list",
        children: items.map((testResult) =>
          createElement("li", {
            text: testResult.name,
          }),
        ),
      }),
      overLimit ? `...and ${overLimit} more` : null,
    );
  }
}
