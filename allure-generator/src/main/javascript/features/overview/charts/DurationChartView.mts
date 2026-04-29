import { histogram, max } from "d3-array";
import { scaleLinear, scaleSqrt } from "d3-scale";
import duration from "../../../helpers/duration.mts";
import translate from "../../../helpers/t.mts";
import { createElement, createFragment } from "../../../shared/dom.mts";
import BaseChartView from "../../../shared/ui/BaseChartView.mts";
import PopoverView from "../../../shared/ui/PopoverView.mts";

type DurationChartItem = {
  name: string;
  time: {
    duration: number | null;
  };
};

type DurationChartDatum = {
  name: string;
  value: number;
};

type DurationChartBin = {
  x0: number;
  x1: number;
  y: number;
  testResults: import("d3-array").Bin<DurationChartDatum, number>;
};

export default class DurationChart extends BaseChartView {
  declare items: DurationChartItem[];

  declare data: DurationChartDatum[];

  declare x: import("d3-scale").ScaleLinear<number, number>;

  declare y: import("d3-scale").ScalePower<number, number>;

  constructor(options: { items?: DurationChartItem[] } = {}) {
    super(options);
    this.items = options.items || [];
    this.getChartData();
  }

  getChartData() {
    this.data = this.items
      .map((testResult) => ({
        value: testResult.time.duration,
        name: testResult.name,
      }))
      .filter((testResult): testResult is DurationChartDatum => {
        return testResult.value !== null;
      });
  }

  drawChart() {
    if (this.data && this.data.length) {
      this.doShow();
    } else {
      this.el.replaceChildren(
        createElement("div", {
          className: "widget__noitems",
          text: translate("chart.duration.empty"),
        }),
      );
    }
  }

  doShow() {
    this.x = scaleLinear();
    this.y = scaleSqrt();
    this.tooltip = new PopoverView({ position: "right" });

    this.setupViewport();

    this.x.range([0, this.width]);
    this.y.range([this.height, 0]);

    const data = this.data as DurationChartDatum[];
    const maxDuration = max(data, (d) => d.value);
    this.x.domain([0, Math.max(maxDuration || 0, 10)] as [number, number]).nice();

    const bins: DurationChartBin[] = histogram<DurationChartDatum, number>()
      .value((d) => d.value)
      .domain(this.x.domain() as [number, number])
      .thresholds(this.x.ticks())(data)
      .map((bin) => ({
        x0: bin.x0 || 0,
        x1: bin.x1 || 0,
        y: bin.length,
        testResults: bin,
      }));

    const maxY = max(bins, (d) => d.y) || 0;
    this.y.domain([0, maxY]).nice();

    this.makeBottomAxis({
      scale: this.x,
      tickFormat: (time: number) => duration(time, 1),
    });

    this.makeLeftAxis({
      scale: this.y,
      ticks: Math.min(10, maxY),
    });

    const bars = this.plot
      .selectAll(".chart__bar")
      .data(bins)
      .enter()
      .append("rect")
      .classed("chart__bar", true);

    this.bindTooltip(bars);

    bars
      .attr("x", (d) => this.x(d.x0) + 2)
      .attr("y", this.height)
      .attr("width", (d) => Math.max(this.x(d.x1) - this.x(d.x0) - 2, 0))
      .attr("height", 0);

    const animatedBars = this.firstRender
      ? (
          bars as unknown as {
            transition: () => { duration: (value: number) => typeof bars };
          }
        )
          .transition()
          .duration(500)
      : bars;

    animatedBars.attr("y", (d) => this.y(d.y)).attr("height", (d) => this.height - this.y(d.y));
  }

  getTooltipContent({ testResults }: DurationChartBin) {
    const LIST_LIMIT = 10;
    const items = testResults.slice(0, LIST_LIMIT);
    const overLimit = testResults.length - items.length;
    return createFragment(
      createElement("b", {
        text: `${testResults.length} test cases`,
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
